package ftmk.utem.edu.my;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LostItemAPIClient {

    private static final String BASE_URL = "http://localhost/lostfound/api/items/";
    private static final String CREATE_URL = BASE_URL + "create.php";
    private static final String VIEW_URL = BASE_URL + "view.php";
    private static final String UPDATE_URL = BASE_URL + "update_status.php";
    private static final String MATCH_URL = BASE_URL + "match_item.php";

    // ===================================================================================
    // Synchronous (original) Method — optionally kept for unit testing or background use
    public static List<Item> fetchItems() throws IOException, JSONException {
        URL url = new URL(VIEW_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        List<Item> items = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }

            JSONArray jsonArray = new JSONArray(response.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObj = jsonArray.getJSONObject(i);
                Item item = new Item();
                item.setId(jsonObj.getInt("id"));
                item.setItemName(jsonObj.getString("item_name"));
                item.setDate(jsonObj.getString("date"));
                item.setLocation(jsonObj.getString("location"));
                item.setDescription(jsonObj.optString("description", ""));
                item.setStatus(jsonObj.getString("status"));
                item.setFormType(jsonObj.getString("form_type"));
                item.setImageUrl(jsonObj.isNull("image_base64") ? null : jsonObj.getString("image_base64"));
                item.setUserName(jsonObj.getString("user_name"));
                item.setUserPhone(jsonObj.getString("user_phone"));
                items.add(item);
            }
        } finally {
            conn.disconnect();
        }
        return items;
    }

    // ===================================================================================
    // Asynchronous version with SwingWorker
    public static void fetchItemsAsync(Consumer<List<Item>> onSuccess, Consumer<Exception> onError) {
        new SwingWorker<List<Item>, Void>() {
            @Override
            protected List<Item> doInBackground() throws Exception {
                return fetchItems();
            }

            @Override
            protected void done() {
                try {
                    onSuccess.accept(get());
                } catch (Exception e) {
                    onError.accept(e);
                }
            }
        }.execute();
    }

    // ===================================================================================
    // Synchronous Submit
    public static String submitLostItem(String name, String phone, String itemName,
                                        String date, String location, String description,
                                        String status, String form_type, File imageFile) {
        String boundary = "===" + System.currentTimeMillis() + "===";
        String LINE_FEED = "\r\n";

        try {
            URL url = new URL(CREATE_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream output = conn.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true)) {

                addFormField(writer, boundary, "name", name);
                addFormField(writer, boundary, "phone", phone);
                addFormField(writer, boundary, "item_name", itemName);
                addFormField(writer, boundary, "date", date);
                addFormField(writer, boundary, "location", location);
                addFormField(writer, boundary, "description", description);
                addFormField(writer, boundary, "status", status);
                addFormField(writer, boundary, "form_type", form_type);

                if (imageFile != null && imageFile.exists()) {
                    addFilePart(writer, output, boundary, "image", imageFile);
                }

                writer.append("--").append(boundary).append("--").append(LINE_FEED).flush();
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error submitting item: " + e.getMessage();
        }
    }

    // ===================================================================================
    // Asynchronous Submit
    public static void submitLostItemAsync(String name, String phone, String itemName,
                                           String date, String location, String description,
                                           String status, String form_type, File imageFile,
                                           Consumer<String> onSuccess, Consumer<Exception> onError) {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return submitLostItem(name, phone, itemName, date, location, description, status, form_type, imageFile);
            }

            @Override
            protected void done() {
                try {
                    onSuccess.accept(get());
                } catch (Exception e) {
                    onError.accept(e);
                }
            }
        }.execute();
    }

    // ===================================================================================
    // Synchronous Match
    public static String matchItems(int lostId, int foundId) throws IOException {
        URL url = new URL(UPDATE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String postData = "lost_id=" + lostId + "&found_id=" + foundId;

        try (OutputStream os = conn.getOutputStream()) {
            os.write(postData.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                responseCode == 200 ? conn.getInputStream() : conn.getErrorStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }

    // ===================================================================================
    // Asynchronous Match
    public static void matchItemsAsync(int lostId, int foundId,
                                       Consumer<String> onSuccess, Consumer<Exception> onError) {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return matchItems(lostId, foundId);
            }

            @Override
            protected void done() {
                try {
                    onSuccess.accept(get());
                } catch (Exception e) {
                    onError.accept(e);
                }
            }
        }.execute();
    }

    // ===================================================================================
    // Internal Helpers
    private static void addFormField(PrintWriter writer, String boundary, String name, String value) {
        String LINE_FEED = "\r\n";
        writer.append("--").append(boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"").append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=UTF-8").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }

    private static void addFilePart(PrintWriter writer, OutputStream output, String boundary, String fieldName, File uploadFile) throws IOException {
        String LINE_FEED = "\r\n";
        String fileName = uploadFile.getName();
        writer.append("--").append(boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"").append(fieldName)
                .append("\"; filename=\"").append(fileName).append("\"").append(LINE_FEED);
        writer.append("Content-Type: ").append(java.nio.file.Files.probeContentType(uploadFile.toPath())).append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        try (FileInputStream inputStream = new FileInputStream(uploadFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            output.flush();
        }

        writer.append(LINE_FEED).flush();
    }
    
    public static String matchItemsViaAPI(int lostId, int foundId) {
        try {
            URL url = new URL(MATCH_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            // JSON body
            String jsonInput = String.format("{\"lost_item_id\": %d, \"found_item_id\": %d}", lostId, foundId);

            // Send JSON
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInput.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read response
            int responseCode = conn.getResponseCode();
            InputStream responseStream = (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream, "utf-8"));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line.trim());
            }

            return response.toString(); // sent back to AdminDashboardApp

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"Exception: " + e.getMessage() + "\"}";
        }
    }


    // ===================================================================================
    // Functional Interface for Callbacks
    public interface Consumer<T> {
        void accept(T t);
    }
}
