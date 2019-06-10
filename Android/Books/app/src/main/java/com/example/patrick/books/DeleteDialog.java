package com.example.patrick.books;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class DeleteDialog extends AppCompatDialogFragment {

    public DatabaseReference dref;
    public DatabaseReference dref_saved;
    public StorageReference mStorage;
    public StorageReference mImageStorage;
    public AlertDialog.Builder builder_delete;
    public AlertDialog.Builder builder_update;
    public AlertDialog.Builder builder_date;
    public DatePicker datePickerDialog;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final MainActivity activity = (MainActivity) getActivity();
        final String book_id = activity.book_id;
        final String photo_id = activity.photo_id;
        final String book_title = activity.book_title;
        final String user_ID = activity.user_ID;
        final Boolean book_is_custom = activity.book_is_custom;

        //Firebase
        dref=FirebaseDatabase.getInstance().getReference("Users/"+user_ID+"/books/"+book_id);
        dref_saved =FirebaseDatabase.getInstance().getReference("Users/"+user_ID+"/saved_books/"+book_id);
        mStorage = FirebaseStorage.getInstance().getReference("book_covers/"+user_ID+"/");
        mImageStorage = mStorage.child(photo_id);

        //Date Picker Dialog
        datePickerDialog = new DatePicker(activity);

        //Alert Dialog
        builder_delete = new AlertDialog.Builder(getActivity());
        builder_update = new AlertDialog.Builder(getActivity());
        builder_date = new AlertDialog.Builder(getActivity());

        //Dialog Box to set the Books Return Data
        builder_date.setCancelable(false);
        builder_date.setTitle("Update Return Date");
        builder_date.setMessage("Please select the new return date for "+'"'+book_title);
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

        //Dialog Box When the user clicks the update button
        builder_update.setTitle("Update Return Date");
        builder_update.setMessage("Do you want to update the return date for "+'"'+book_title+'"'+" ?");
        builder_update.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                builder_date.create().show();
            }

        });
        builder_update.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                builder_delete.create().show();
            }
        });

        //Dialog Box for Delete or Update
        builder_delete.setTitle("Update or Delete Book");
        builder_delete.setMessage("Do you want to update or remove "+'"'+book_title+'"');
        builder_delete.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dref.removeValue();
                        dref_saved.removeValue();
                        if(book_is_custom == true){
                            mImageStorage.delete();
                        }
                        Book book_item = activity.book_list.get(activity.book_position);
                        activity.book_list.remove(book_item);
                        activity.book_adapter.notifyDataSetChanged();

                    }
                });
        builder_delete.setNegativeButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                builder_update.create().show();
            }
        });
        builder_delete.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return builder_delete.create();
    }
}
