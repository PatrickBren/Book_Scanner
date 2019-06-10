package com.example.patrick.books;

public class General_Settings {

    Boolean is_custom;
    Boolean is_borrowed;
    Boolean is_bought;

    public General_Settings(Boolean is_custom, Boolean is_borrowed, Boolean is_bought){
        this.is_custom = is_custom;
        this.is_borrowed = is_borrowed;
        this.is_bought = is_bought;
    }

    public Boolean getIs_custom() {
        return is_custom;
    }

    public void setIs_custom(Boolean is_custom) {
        this.is_custom = is_custom;
    }

    public Boolean getIs_borrowed() {
        return is_borrowed;
    }

    public void setIs_borrowed(Boolean is_borrowed) {
        this.is_borrowed = is_borrowed;
    }

    public Boolean getIs_bought() {
        return is_bought;
    }

    public void setIs_bought(Boolean is_bought) {
        this.is_bought = is_bought;
    }
}
