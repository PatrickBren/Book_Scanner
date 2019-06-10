package com.example.patrick.books;

public class Notification_Settings {

    Object return_date;
    Boolean is_new;

    public Notification_Settings(Object return_date, Boolean is_new){
        this.return_date = return_date;
        this.is_new = is_new;
    }

    public Object getReturn_date() {
        return return_date;
    }

    public void setReturn_date(Object return_date) {
        this.return_date = return_date;
    }

    public Boolean getIs_new() {
        return is_new;
    }

    public void setIs_new(Boolean is_new) {
        this.is_new = is_new;
    }
}
