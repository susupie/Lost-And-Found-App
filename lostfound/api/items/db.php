<?php
$pdo = new PDO("mysql:host=localhost;dbname=lostandfound", "root", "");
$pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
?>
