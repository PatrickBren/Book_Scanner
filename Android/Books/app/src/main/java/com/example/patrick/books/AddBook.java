package com.example.patrick.books;

public class AddBook {
    String book_id;
    String title;
    String author;
    String publisher;
    String description;
    String category;
    Object general_settings;
    Object notification_settings;

    public AddBook(String book_id,String title, String author, String publisher, String description, String category, Object general_settings, Object notification_settings){
        this.book_id = book_id;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.description = description;
        this.category = category;
        this.general_settings = general_settings;
        this.notification_settings = notification_settings;
    }

    public Object getGeneral_settings() {
        return general_settings;
    }

    public void setGeneral_settings(Object general_settings) {
        this.general_settings = general_settings;
    }

    public Object getNotification_settings() {
        return notification_settings;
    }

    public void setNotification_settings(Object notification_settings) {
        this.notification_settings = notification_settings;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBook_id() {
        return book_id;
    }

    public void setBook_id(String book_id) {
        this.book_id = book_id;
    }


}
