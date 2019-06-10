package com.example.patrick.books;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class Book_Info extends AppCompatActivity {

    public DatabaseReference dref;
    Button btn_playstore_link;
    TextView tv_title;
    TextView tv_return_date;
    TextView tv_author;
    TextView tv_description;
    ImageView iv_book;
    String book_buylink;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_info);

        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_return_date = (TextView) findViewById(R.id.tv_return_date);
        tv_author = (TextView) findViewById(R.id.tv_author);
        tv_description = (TextView) findViewById(R.id.tv_description);
        iv_book = (ImageView) findViewById(R.id.iv_book);
        btn_playstore_link = (Button) findViewById(R.id.btn_playstore_link);


        //Pulls data from the book list data
        String book_id = getIntent().getStringExtra("BOOK");
        String user_ID = getIntent().getStringExtra("USER_ID");


        //Firebase Database reference
        dref=FirebaseDatabase.getInstance().getReference("Users/"+user_ID+"/books/"+book_id);

        //pulls the book data based off the the book_id, the book_id was given when the user clicked on the item in the list
        dref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String book_title = dataSnapshot.child("title").getValue(String.class);
                book_buylink = dataSnapshot.child("buy_link").getValue(String.class);
                String book_author = dataSnapshot.child("author").getValue(String.class);
                String book_description = dataSnapshot.child("description").getValue(String.class);
                String book_id = dataSnapshot.child("book_id").getValue(String.class);
                Integer book_return_day = dataSnapshot.child("notification_settings").child("return_date").child("day").getValue(Integer.class);
                Integer book_return_month = dataSnapshot.child("notification_settings").child("return_date").child("month").getValue(Integer.class);
                Integer book_return_Year  = dataSnapshot.child("notification_settings").child("return_date").child("year").getValue(Integer.class);
                Boolean is_custom = dataSnapshot.child("general_settings").child("is_custom").getValue(Boolean.class);


                tv_title.setText(book_title);
                tv_return_date.setText("Return Date: "+book_return_day+"/"+book_return_month+"/"+book_return_Year);
                tv_author.setText(book_author);
                tv_description.setText(book_description);

                //if the book was custom, then it uses the custom saved image
                if(is_custom == true){
                    Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/book-scann.appspot.com/o/book_covers%2FkbNojTrH0OWz1bTPIByY9OuIBbE3%2F"+book_id+"?alt=media&token=f946069c-da0f-4731-b49b-374b0405ccb6").into(iv_book);
                    btn_playstore_link.setEnabled(false);
                }

                //if the book was scanned, it used the google books profile cover image
                else if(is_custom == false){
                    Picasso.get().load("https://books.google.com/books/content/images/frontcover/"+book_id+"?fife=w500-h900").into(iv_book);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //sets up the buy link
        btn_playstore_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(book_buylink); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }


}
