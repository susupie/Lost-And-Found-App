### **Lost and Found Application**
<br>

**Introduction**

The Lost and Found System is developed to streamline the process of reporting and managing lost and found items in shared environments. Misplaced belongings are a common issue in busy public areas such as schools, universities, offices, and shopping malls, often resulting in inconvenience and frustration. This system aims to address that problem by providing a centralized and accessible platform for users and administrators.

Users can easily submit reports for items they have lost or found, while administrators are equipped with tools to verify, manage, and match these reports efficiently. The system fosters community responsibility by increasing the chances of reuniting items with their rightful owners through effective communication.

One of the key features includes integration with Telegram for real-time notifications. Once users interact with the bot and their Telegram user ID is recorded, administrators can quickly notify them when a potential match is found. This automation reduces manual communication efforts and ensures prompt updates, ultimately improving the success rate of item recovery.

<br>

**Project Overview**

The Lost and Found System is an application developed to assist users in reporting lost or found items and help administrators manage, match, and notify relevant users. It aims to solve the common problem of item misplacement in shared environments such as schools, offices, and public spaces. 

This system addresses a frequent issue faced in schools, universities, office buildings, shopping malls, and public institutions—items being lost and difficult to recover. By offering an intuitive user interface and real-time communication capabilities via Telegram Bot API, the system significantly increases the likelihood of reuniting owners with their lost possessions. 

It not only minimizes manual tracking efforts but also promotes a responsible and connected community. Integration with Telegram allows for immediate notifications, reducing the time and communication gap between item recovery and owner notification. The solution is both scalable and practical for real-world deployment.

<br>

**Commercial Value / Third-Party Integration**

This system can be implemented in real-world environments like universities or shopping malls, reducing manual efforts to locate item owners. It integrates the Telegram Bot API to automate communication. Once users interact with the bot, the system stores their Telegram user ID, and administrators can notify them automatically via Telegram if their item is found.

<br>

**System Architecture**

<img width="940" height="393" alt="image" src="https://github.com/user-attachments/assets/4d398a1b-23da-421c-b1ea-cfc5fde3f2ee" />

<br><br>

### **Backend Application**

**Technology Stack:** 

i.	Language: Java, PHP

ii.	IDE: Eclipse

iii.	Database: MySQL

iv.	Integration: Telegram Bot API (via HTTP requests), RESTful API

v.	Libraries: Java Swing, JDBC, org.json

<br>

**API Documentation**

**(1) List of all API endpoints** 

| No | Endpoint(s)                                                                 | HTTP Method | Description                                |
|----|------------------------------------------------------------------------------|--------|--------------------------------------------|
| 1  | `lostfound/api/items/get_chat_id.php`,<br>`lostfound/api/items/match_item.php` | POST   | Sends a message via Telegram Bot API       |
| 2  | `lostfound/api/items/create.php`                                            | POST   | Saves lost item details to database        |
| 3  | `lostfound/api/items/create.php`                                            | POST   | Saves found item details to database       |
| 4  | `lostfound/api/items/view.php`                                              | GET    | Retrieves list of found items              |
| 5  | `lostfound/api/items/view.php`                                              | GET    | Retrieves status of user's lost report     |
| 6  | `lostfound/api/items/match_item.php`                                        | PUT    | Updates database with matched reports      |


<br>

Example: Telegram Message (Java HTTP call)

```php
$botToken = getenv('TELEGRAM_BOT_TOKEN');
$url = "https://api.telegram.org/bot$botToken/sendMessage";
$data = ['chat_id' => $chatId, 'text' => $text, 'parse_mode' => 'HTML'];

$message = "<b>📢 [Lost & Found Notification]</b>\n\n" .
           "Hi <b>$userName</b>, good news!\n\n" .
           "Your lost item <i>\"$itemName\"</i> has been successfully matched with a found report in our system. 🎉\n\n" .
           "Please check the app for more details or contact the administrator to proceed with the collection.\n\n" .
           "Thank you for using our Lost & Found service!";
```

<br>

**(2) The HTTP method for each endpoint (GET, POST, PUT, DELETE).**

**•	Method: POST**

o	Description: Sends a message via Telegram Bot API

o	Description: Saves lost item details to database

o	Description: Saves found item details to database

**•	Method: GET**

o	Description: Retrieves list of found items

o	Description: Retrieves status of user's lost report

**•	Method: PUT**

o	Description: Updates database with matched reports


<br>

**(3) Required request parameters, headers, and body formats (with JSON examples).**


**Headers (for all requests using JSON)**
```http
Content-Type: application/json
```

---

**POST /submitLostItem**

Required Fields in JSON Body:

```json
{
  "user_id": 101,
  "item_name": "Red Backpack",
  "date": "2025-07-15",
  "location": "Library",
  "description": "Contains books and a laptop",
  "image": "uploads/backpack1.jpg"
}
```

---

**POST /submitFoundItem**

Required Fields in JSON Body:

```json
{
  "admin_id": 1,
  "item_name": "Red Backpack",
  "date": "2025-07-15",
  "location": "Library",
  "description": "Found near desk",
  "image": "uploads/found.jpg"
}
```

---

**POST /sendTelegramMessage**

Required Fields in JSON Body:

```json
{
  "chat_id": "123456789",
  "text": "Your item has been found!"
}
```

---

**PUT /matchItems**

Required Fields in JSON Body:

```json
{
  "lost_item_id": 5,
  "found_item_id": 8,
  "matched_by_admin_id": 1
}
```

---

**Success Response (200 OK)**

Required Fields in JSON Body:

```json
{
  "status": "success",
  "message": "Item submitted successfully."
}
```

---

**Error Response (400 Bad Request)**

Required Fields in JSON Body:

```json
{
  "status": "error",
  "message": "Missing required fields."
}
```

---

**Error Response (500 Internal Server Error)**

Required Fields in JSON Body:

```json
{
  "status": "error",
  "message": "Database connection failed."
}
```

<br>

**(5) Security:**


•	**Authentication**: Admin authentication via username/password in the desktop application.

•	**Telegram Bot Token**: Stored securely in backend Java class; not exposed in UI.

•	**Input Validation**: All data inputs are validated server-side to prevent SQL injection and malformed data.

•	**Access Control**: Only admins can match items and send notifications.

<br>

### **Frontend Applications**

**(1) User Frontend Application**

**Purpose:**

Designed for general users such as students, staff, and the public, the user-facing application provides the following functionalities:

•	Reporting of lost and found items.

•	Browsing the list of found items.

•	Checking the status of submitted reports.

•	Receiving updates and notifications (via Telegram).

<br>

**Technology Stack:**

•	**Platform**: Java Desktop Application

•	**UI Framework**: Swing (UserDashboardApp.java)

•	**Dependencies**: Standard Java libraries, org.json for JSON parsing

<br>

**API Integration (REST API):**

The application communicates with backend services using Java’s HttpURLConnection to access PHP-based RESTful API endpoints:

| Functionality           | API Endpoint                                         | Method |
|-------------------------|------------------------------------------------------|--------|
| Submit Lost Item        | `lostfound/api/items/submit_lost_item.php`          | POST   |
| Submit Found Item       | `lostfound/api/items/submit_found_item.php`         | POST   |
| Retrieve Found Items    | `lostfound/api/items/view_found_items.php`          | GET    |
| Check Report Status     | `lostfound/api/items/get_user_report_status.php`    | GET    |
| Receive Telegram Updates| Triggered via backend integration (Telegram Bot)    | POST   |


•	**Data Format**: JSON (for request/response)

•	**File Uploads**: Multipart/form-data for images (handled via LostItemAPIClient.java)

•	**UI Update**: Dynamic rendering of item lists, report status, and feedback from API responses.

<br>

**(2) Admin Frontend Application**

**Purpose:**

Targeted at system administrators and lost-and-found staff, the admin application provides management tools to:

•	View and manage all lost/found reports

•	Add new found items

•	Match lost items with found items

•	Update item statuses and notify users

<br>

**Technology Stack:**

•	**Platform**: Java Desktop Application

•	**UI Framework**: Swing (AdminDashboardApp.java)

•	**Dependencies**: Java libraries for Swing, image decoding (Base64), and JSON handling

<br>

**API Integration (REST API):**

The admin application interfaces with the same RESTful PHP API, utilizing additional administrative endpoints:

| Functionality               | API Endpoint                                      | Method |
|-----------------------------|---------------------------------------------------|--------|
| View All Reports            | `lostfound/api/items/view.php`                   | GET    |
| Submit New Lost / Found Item| `lostfound/api/items/create.php`                 | POST   |
| Match Lost and Found Items  | `lostfound/api/items/match_items.php`            | PUT    |
| Update Item Status          | `lostfound/api/items/match_item.php`             | PUT    |
| Notify User via Telegram    | `lostfound/api/telegram/match_item.php`          | POST   |


•	**Data Format**: JSON

•	**Image Handling**: Images received in Base64 are decoded and displayed in tables

•	**Access Control**: Elevated permissions assumed for admin actions (e.g., matching, updating statuses)

<br>

**(3) Backend API (Shared by Both Frontends)**

The frontend of the Lost and Found App, built using Java Swing, communicates with a PHP-based RESTful backend via HttpURLConnection. Each feature in the app—such as viewing items, submitting reports, updating item status, or matching items—triggers HTTP requests to specific API endpoints under the lostfound/api/items/ directory. Data is exchanged in JSON or form-data format, while image uploads use multipart/form-data. The backend handles requests, stores or retrieves data from a MySQL database, and responds with JSON for the frontend to parse and display.

**Implementation Details:**

•	**Language**: PHP

•	**Database**: MySQL (defined in db.php)

•	**Directory**: lostfound/api/items/

•	**Communication Protocol**: REST (stateless HTTP requests with JSON payloads)

**•	Features:**

o	Full CRUD for lost/found item records

o	Image upload and retrieval

o	User report tracking

o	Matching logic and automated user notification (Telegram integration via Bot API)

<br>

 ### **Database Design**

**(1) Entity Relationship Diagram (ERD)**

<img width="940" height="388" alt="image" src="https://github.com/user-attachments/assets/50597ef1-0463-4b00-bae3-9bdb0b976c84" />

<br>

**(2) Schema Justification**

The database is designed to support the functionality of a lost and found management system. The rationale behind this schema includes:

•	Normalization and Clarity: Separating users from items improves clarity and reduces redundancy.

•	Relational Integrity: The foreign key (user_id) ensures that each item is associated with a valid user.

•	Flexibility: Fields like form_type and status allow the system to handle both lost and found reports in various stages (e.g., reported, resolved).

•	Multimedia Support: The image column (of type longblob) allows users to upload item pictures, which helps in better identification.

•	Contact Integration: telegram_chat_id supports Telegram Bot integration for notifying users.

<br>

### **Business Logic and Data Validation**

**(1) Use Case Diagrams / Flowcharts**

<img width="883" height="1113" alt="image" src="https://github.com/user-attachments/assets/36e00792-3159-4ab1-ae6a-b8ad797ec8b0" />

<br><br>

**(2) Data Validation**

**Frontend Validation**

•	Required fields: All item fields (item_name, date, location, etc.) are mandatory.

•	File upload: Only image file types allowed (.jpg, .png).

•	Phone number: Must be numeric and within valid length.

•	Form type/status: Must be selected from predefined dropdowns (no free text input).

**Backend Validation**

•	Foreign key constraint on user_id ensures item is always linked to a valid user.

•	Date format and image content are validated on the server side.

•	Input sanitization to prevent SQL injection.

•	Duplicate item entries can be avoided by checking for similar item names and locations submitted within the same date range.

•	Unique constraint can be applied on telegram_chat_id in the users table to prevent duplicate user entries.
