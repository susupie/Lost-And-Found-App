<?php
require_once 'db.php'; // This should define your $pdo connection

header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *"); // For development only

try {
    // Fetch all item data including image as base64
    $sql = "SELECT 
                i.id, 
                i.item_name, 
                i.date, 
                i.location, 
                i.description, 
                i.status,      
                i.form_type, 
                i.image,
                u.name AS user_name, 
                u.phone AS user_phone
            FROM 
                items i 
            LEFT JOIN 
                users u ON i.user_id = u.id
            ORDER BY 
                i.id DESC";

    $stmt = $pdo->prepare($sql);
    $stmt->execute();

    $items = [];

    while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        // If image exists, convert to base64
        $imageBase64 = null;
        if (!empty($row['image']) && file_exists($row['image'])) {
            $fileType = pathinfo($row['image'], PATHINFO_EXTENSION);
            $fileData = file_get_contents($row['image']);
            $imageBase64 = 'data:image/' . $fileType . ';base64,' . base64_encode($fileData);
        }

        $items[] = [
            'id' => $row['id'],
            'item_name' => $row['item_name'],
            'date' => $row['date'],
            'location' => $row['location'],
            'description' => $row['description'],
            'status' => $row['status'],
            'form_type' => $row['form_type'],
            'user_name' => $row['user_name'],
            'user_phone' => $row['user_phone'],
            'image_base64' => $imageBase64
        ];
    }
    
    echo json_encode($items);

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(["error" => "Database query failed", "message" => $e->getMessage()]);
}
?>
