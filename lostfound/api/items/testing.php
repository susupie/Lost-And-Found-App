<?php

$botToken = getenv('TELEGRAM_BOT_TOKEN');
$url = "https://api.telegram.org/bot$botToken/sendMessage";
$data = ['chat_id' => "487040687", 'text' => "hello"];
$options = [
    'http' => [
        'header' => "Content-type: application/json\r\n",
        'method' => 'POST',
        'content' => json_encode($data),
    ],
];

$context = stream_context_create($options);
$response = file_get_contents($url, false, $context);

echo $response;