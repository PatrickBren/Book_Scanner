package com.example.patrick.books;

public class Book {
    String title;
    String author;
    String id;
    String img_id;
    Boolean is_new;
    Boolean is_custom;
    Boolean is_bought;
    Boolean is_borrowed;
    String book_key;
    String category;

    public Book(String title, String author, String id, String img_id, Boolean is_new, Boolean is_custom, String book_key, String category) {
        this.title = title;
        this.author = author;
        this.id = id;
        this.is_new = is_new;
        this.is_custom = is_custom;
        this.img_id = img_id;
        this.book_key = book_key;
        this.category = category;
    }

    public String getImg_id() {
        return img_id;
    }

    public void setImg_id(String img_id) {
        this.img_id = img_id;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getIs_new() {
        return is_new;
    }

    public void setIs_new(Boolean is_new) {
        this.is_new = is_new;
    }

    public String getBook_key() {
        return book_key;
    }

    public void setBook_key(String book_key) {
        this.book_key = book_key;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getIs_custom() {
        return is_custom;
    }

    public void setIs_custom(Boolean is_custom) {
        this.is_custom = is_custom;
    }
}
