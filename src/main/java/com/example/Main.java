package com.example;


public class Main {
    public static void main(String[] args) {
        DbManager dbManager = new DbManager("url of your database", "user of your database", "password of your database");
        dbManager.createTableIfNotExists();
        // Other Codes to work with the database

    }
}