<?php
require_once 'db.php'; // This should define your $pdo connection

header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *"); // For development only
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

try {
    // Get the JSON input
    $data = json_decode(file_get_contents("php://input"), true);

    if (!isset($data['lost_item_id']) || !isset($data['found_item_id'])) {
        http_response_code(400);
        echo json_encode(["error" => "Missing item IDs"]);
        exit;
    }

    $lostId = $data['lost_item_id'];
    $foundId = $data['found_item_id'];

    // Update item statuses
    $sql = "UPDATE items SET status = 'Completed' WHERE id IN (?, ?)";
    $stmt = $pdo->prepare($sql);
    $stmt->execute([$lostId, $foundId]);

    if ($stmt->rowCount() === 2) {

        // 🔍 Get lost item's user info
        $userQuery = "SELECT u.name, i.item_name
                      FROM items i
                      JOIN users u ON i.user_id = u.id
                      WHERE i.id = ?";
        $stmt = $pdo->prepare($userQuery);
        $stmt->execute([$lostId]);
        $user = $stmt->fetch(PDO::FETCH_ASSOC);

        // Send Telegram message if chat ID exists
        if ($user) {
            $userName = $user['name'];
            $itemName = $user['item_name'];
            $chatId = "487040687";

            // $message = "Hello $userName, your lost item \"$itemName\" has been found and matched! Please check the app or contact the admin for details.";
           $message = "<b>📢 [Lost & Found Notification]</b>\n\n" .
           "Hi <b>$userName</b>, good news!\n\n" .
           "Your lost item <i>\"$itemName\"</i> has been successfully matched with a found report in our system. 🎉\n\n" .
           "Please check the app for more details or contact the administrator to proceed with the collection.\n\n" .
           "Thank you for using our Lost & Found service!";


            $tg_response = sendTelegramMessage($chatId, $message);
            echo json_encode(["message" => "Items matched.", "telegram" => "sent",  "response" => $tg_response ?? "Telegram response not available"]);
        }

        echo json_encode(["message" => "User not found!", "telegram" => "error"]);
    } else {
        http_response_code(500);
        echo json_encode(["error" => "One or both items not updated."]);
    }

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        "error" => "Database error",
        "message" => $e->getMessage()
    ]);
}

// 📩 Telegram Bot Messaging Function
function sendTelegramMessage($chatId, $text) {
    $botToken = getenv('TELEGRAM_BOT_TOKEN');
    $url = "https://api.telegram.org/bot$botToken/sendMessage";
	$data = ['chat_id' => $chatId, 'text' => $text, 'parse_mode' => 'HTML'];
    $options = [
        'http' => [
            'header' => "Content-type: application/json\r\n",
            'method' => 'POST',
            'content' => json_encode($data),
        ],
    ];

    $context = stream_context_create($options);
    $response = file_get_contents($url, false, $context);
	return $response;
}
?>
