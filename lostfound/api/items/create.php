<?php
require_once 'db.php'; // Contains your DB connection

$name = $_POST['name'] ?? '';
$phone = $_POST['phone'] ?? '';
$item_name = $_POST['item_name'] ?? '';
$date = $_POST['date'] ?? '';
$location = $_POST['location'] ?? '';
$description = $_POST['description'] ?? '';
$status = $_POST['status'] ?? '';
$form_type = $_POST['form_type'] ?? '';


if (!$name || !$phone || !$item_name || !$date || !$location) {
    echo "Missing required fields.";
    exit;
}
$imagePath = null;

// Handle image upload if present
if (isset($_FILES['image']) && $_FILES['image']['error'] === UPLOAD_ERR_OK) {
    $uploadDir = 'uploads/';
    if (!is_dir($uploadDir)) mkdir($uploadDir, 0755, true);
    $imageName = uniqid() . "_" . basename($_FILES['image']['name']);
    $targetFile = $uploadDir . $imageName;
    if (move_uploaded_file($_FILES['image']['tmp_name'], $targetFile)) {
        $imagePath = $targetFile;
    }
}

// Insert or find user
$stmt = $pdo->prepare("SELECT id FROM users WHERE name = ? AND phone = ?");
$stmt->execute([$name, $phone]);
$user = $stmt->fetch();

if (!$user) {
    $stmt = $pdo->prepare("INSERT INTO users (name, phone) VALUES (?, ?)");
    $stmt->execute([$name, $phone]);
    $user_id = $pdo->lastInsertId();
} else {
    $user_id = $user['id'];
}

// Insert item
$stmt = $pdo->prepare("INSERT INTO items (item_name, date, location, description, status, user_id, form_type, image)
                       VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
$stmt->execute([$item_name, $date, $location, $description, $status, $user_id, $form_type, $imagePath]);

echo "Report submitted successfully.";
?>
