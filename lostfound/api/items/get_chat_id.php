<?php
require_once 'db.php'; // Your PDO DB connection

$botToken =getenv('TELEGRAM_BOT_TOKEN');
$content = file_get_contents("php://input");
$update = json_decode($content, true);

if (!isset($update["message"])) {
    exit;
}

$message = $update["message"];
$chatId = "487040687";
$userName = $message["from"]["first_name"];
$text = $message["text"] ?? "";
$contact = $message["contact"] ?? null;

// 1. If user sends /start, ask for phone number
if ($text === "/start") {
    $replyText = "Hi $userName! Please share your phone number to receive Lost & Found updates.";
    $keyboard = [
        "keyboard" => [
            [[
                "text" => "📱 Share Phone Number",
                "request_contact" => true
            ]]
        ],
        "resize_keyboard" => true,
        "one_time_keyboard" => true
    ];

    sendTelegramReply($chatId, $replyText, $keyboard);
}

// 2. If user sends contact info (phone number)
elseif ($contact) {
    $phone = $contact["phone_number"];
    $telegramChatId = $contact["user_id"]; // same as $chatId

    // Match user by phone number
    $sql = "UPDATE users SET telegram_chat_id = ? WHERE phone = ?";
    $stmt = $pdo->prepare($sql);
    $stmt->execute([$telegramChatId, $phone]);

    if ($stmt->rowCount() > 0) {
        sendTelegramReply($chatId, "✅ Thank you! Your account is now linked. You will receive item match updates.");
    } else {
        sendTelegramReply($chatId, "⚠️ Your phone number was not found in the system. Please contact admin.");
    }
}

// ========== Function to Send Message ==========
function sendTelegramReply($chatId, $text, $replyMarkup = null) {
    global $botToken;

    $data = [
        "chat_id" => $chatId,
        "text" => $text,
    ];

    if ($replyMarkup) {
        $data["reply_markup"] = json_encode($replyMarkup);
    }

    $url = "https://api.telegram.org/bot$botToken/sendMessage";
    $options = [
        "http" => [
            "method"  => "POST",
            "header"  => "Content-Type: application/json",
            "content" => json_encode($data),
        ]
    ];

    $context = stream_context_create($options);
    file_get_contents($url, false, $context);
}
?>
