package com.example.patrick.books;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class FavouriteActivity extends AppCompatActivity {

    //Header items variables
    public ListView lv_favourite_books;

    //Firebase database reference variable
    public DatabaseReference dref;


    //Book List and Adapter / Category List
    public FavouriteBook_Adapter favourite_book_adapter;
    public ArrayList<Book> favourite_book_list = new ArrayList<>();

//    Dialog Variables
    public String book_id;
    public Integer book_position;
    public String book_title;


    //public String user_id_message;
    public String user_ID;

    //User Authentication
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);

        //Assign firebase Authentication signed in user to variable
        mAuth = FirebaseAuth.getInstance();

        //Book List View
        lv_favourite_books = (ListView) findViewById(R.id.lv_favourite_books);
        favourite_book_adapter = new FavouriteBook_Adapter(this,R.layout.favourite_book_item,favourite_book_list);
        lv_favourite_books.setAdapter(favourite_book_adapter);


        getUser_ID();
        getDatabaseRef();
        getBooks();

        //Opens Book_info Activity with the clicked books image
        lv_favourite_books.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Book book_item = favourite_book_list.get(position);
                String book_id = book_item.getId();
                Intent intent = new Intent(getBaseContext(), Book_Info.class);
                intent.putExtra("BOOK", book_id);
                intent.putExtra("USER_ID", user_ID);
                startActivity(intent);
            }
        });

        //Opens FavouriteDeleteDialog Activity with the user holds press
        lv_favourite_books.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Book book_item = favourite_book_list.get(position);
                 book_id = book_item.getId();
                 book_title = book_item.getTitle();
                 book_position = position;
                openDeleteDialog();

                return true;
            }
        });

    }


    //Get The Current User Logged in
    public void getUser_ID(){
        user_ID = mAuth.getCurrentUser().getUid();
    }

    //Get Firebase Reference
    public void getDatabaseRef(){
        dref=FirebaseDatabase.getInstance().getReference("Users/"+user_ID+"/saved_books/");
    }

    //Get Book Data
    public void getBooks(){

        dref.addChildEventListener(new ChildEventListener() {
            @Override
            public  void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String book_title = dataSnapshot.child("book").child("title").getValue(String.class);
                String book_author = dataSnapshot.child("book").child("author").getValue(String.class);
                String book_category = dataSnapshot.child("book").child("category").getValue(String.class);
                String book_id = dataSnapshot.getKey();
                String img_id = dataSnapshot.child("book").child("book_id").getValue(String.class);
                Boolean book_new = dataSnapshot.child("book").child("notification_settings").child("is_new").getValue(Boolean.class);
                Boolean book_custom = dataSnapshot.child("book").child("general_settings").child("is_custom").getValue(Boolean.class);
                String book_key = dataSnapshot.getKey();

                Book book = new Book(book_title, book_author, book_id, img_id, book_new, book_custom, book_key, book_category);

                favourite_book_list.add(book);
                favourite_book_adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Dialog Functions
    public void openDeleteDialog(){
        FavouriteDeleteDialog favouriteDeleteDialog = new FavouriteDeleteDialog();
        favouriteDeleteDialog.show(getSupportFragmentManager(), "Delete Book");
    }

}
