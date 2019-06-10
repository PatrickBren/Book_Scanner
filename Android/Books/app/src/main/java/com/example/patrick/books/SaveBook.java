package com.example.patrick.books;

public class SaveBook {

    Object book;
    Boolean is_saved;

    public SaveBook(Object book, Boolean is_saved){

        this.book = book;
        this.is_saved = is_saved;

    }

    public Object getBook() {
        return book;
    }

    public void setBook(Object book) {
        this.book = book;
    }

    public Boolean getIs_saved() {
        return is_saved;
    }

    public void setIs_saved(Boolean is_saved) {
        this.is_saved = is_saved;
    }
}
