package ftmk.utem.edu.my;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Item {
    private int id;
    private String itemName;
    private LocalDate date;
    private String location;
    private String description;
    private String status;
    private String formType; // "Lost" or "Found"
    private String imageUrl;
    private String userName;
    private String userPhone;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Getters
    public int getId() { return id; }
    public String getItemName() { return itemName; }
    public LocalDate getDate() { return date; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getFormType() { return formType; }
    public String getImageUrl() { return imageUrl; }
    public String getUserName() { return userName; }
    public String getUserPhone() { return userPhone; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setDate(String dateString) {
        try {
            this.date = LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            System.err.println("Could not parse date: " + dateString);
            this.date = null;
        }
    }
    public void setLocation(String location) { this.location = location; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status) { this.status = status; }
    public void setFormType(String formType) { this.formType = formType; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
}