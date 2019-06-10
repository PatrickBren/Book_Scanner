package com.example.patrick.books;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.widget.DatePicker;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FavouriteDeleteDialog extends AppCompatDialogFragment {


    public DatabaseReference dref_saved;
    public AlertDialog.Builder builder_delete;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final FavouriteActivity activity = (FavouriteActivity) getActivity();
        final String book_id = activity.book_id;
        final String book_title = activity.book_title;
        final String user_ID = activity.user_ID;

        //Firebase
        dref_saved =FirebaseDatabase.getInstance().getReference("Users/"+user_ID+"/saved_books/"+book_id);

        //Alert Dialog
        builder_delete = new AlertDialog.Builder(getActivity());

        //Asks user if they want to delete the books from the favourites page
        builder_delete.setTitle("Delete Book from Favourites");
        builder_delete.setMessage("Do you want to remove "+'"'+book_title+'"');
        builder_delete.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dref_saved.removeValue();
                Book book_item = activity.favourite_book_list.get(activity.book_position);
                activity.favourite_book_list.remove(book_item);
                activity.favourite_book_adapter.notifyDataSetChanged();

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
