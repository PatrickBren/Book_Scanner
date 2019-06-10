package com.example.patrick.books;


import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;


public class Borrow_Bought_Dialog extends DialogFragment {

    private Context mContext;
    public DatabaseReference dref;
    public AlertDialog.Builder builder;
    public AlertDialog.Builder builder_date;
    public DatePicker datePickerDialog;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final MainActivity activity = (MainActivity) getActivity();
        final String new_book_id = activity.new_book_id;
        final String new_book_title = activity.new_book_title;
        final String user_ID = activity.user_ID;


        //Firebase Reference
        dref=FirebaseDatabase.getInstance().getReference("Users/"+user_ID+"/books/"+new_book_id);

        //Date Picker Dialog
        datePickerDialog = new DatePicker(activity);

        //Alert Dialog
        builder = new AlertDialog.Builder(getActivity());
        builder_date = new AlertDialog.Builder(getActivity());


        //Dialog Box to set the Books Return Data
        builder_date.setCancelable(false);
        builder_date.setTitle("Add Return Date");
        builder_date.setMessage("When will you return "+'"'+new_book_title+'"'+" ?");
        builder_date.setView(datePickerDialog);
        builder_date.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                  Integer month = datePickerDialog.getMonth() + 1;
                  Integer day = datePickerDialog.getDayOfMonth();
                  Integer year = datePickerDialog.getYear();
                  DateObject dateObject = new DateObject(day,month,year);
                  dref.child("notification_settings").child("return_date").setValue(dateObject);
                  dref.child("notification_settings").child("is_new").setValue(false);

            }
        });




        //Dialog Box For when a new book is added
        builder.setCancelable(false);
        builder.setTitle("New Book Added");
        builder.setMessage("Did You Borrow "+'"'+new_book_title+'"'+" ?");
        builder.setPositiveButton("Bought", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dref.child("notification_settings").child("is_new").setValue(false);
                    }
                });
        builder.setNegativeButton("Borrow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        builder_date.create().show();
                    }

                });
        return builder.create();
    }


    }


