package com.example.patrick.books;

import android.content.Context;
import android.media.Image;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class Book_Adapter extends ArrayAdapter<Book> implements Filterable {

    private static final String TAG = "Book_Adapter";

    private Context mContext;
    private ArrayList<Book> books = new ArrayList<>();

    //Firebase database reference variable
    public DatabaseReference dref_main;
    public DatabaseReference dref_favourite;

    //public String user_id_message;
    public String user_ID;

    //User Authentication
    private FirebaseAuth mAuth;

    int mResource;

    public Book_Adapter(Context context, int resource, ArrayList<Book> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        //Assign firebase Authentication signed in user to variable
        mAuth = FirebaseAuth.getInstance();

        getUser_ID();
        String key = getItem(position).getBook_key();
        Boolean is_custom = getItem(position).getIs_custom();


        String title = getItem(position).getTitle();
        String author = getItem(position).getAuthor();
        String id = getItem(position).getImg_id();

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        final TextView tv_title  = (TextView) convertView.findViewById(R.id.tv_title);
        final TextView tv_author = (TextView) convertView.findViewById(R.id.tv_author);
        ImageView iv_book_main = (ImageView) convertView.findViewById(R.id.iv_book_main);
        final ToggleButton btn_favourite_toggle = (ToggleButton) convertView.findViewById(R.id.btn_favourite_toggle);


        tv_title.setText(title);
        tv_author.setText(author);

        if(is_custom == true){
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/book-scann.appspot.com/o/book_covers%2FkbNojTrH0OWz1bTPIByY9OuIBbE3%2F"+id+"?alt=media&token=f946069c-da0f-4731-b49b-374b0405ccb6").into(iv_book_main);
        }
        else if(is_custom == false){
            Picasso.get().load("https://books.google.com/books/content/images/frontcover/"+id+"?fife=w500-h900").into(iv_book_main);
        }


        dref_main = FirebaseDatabase.getInstance().getReference("Users/"+user_ID+"/books/"+key);
        dref_favourite = FirebaseDatabase.getInstance().getReference("Users/"+user_ID+"/saved_books/"+key);

        //Checks what books are favourites and changes the correct toggle
        dref_favourite.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean is_saved = (dataSnapshot.child("is_saved").getValue(Boolean.class));

                if(is_saved != null) {
                    if (is_saved == true) {
                        btn_favourite_toggle.setChecked(true);

                    } else {
                        btn_favourite_toggle.setChecked(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //gets the books key and copies the book object in the mainlist and saves it to the favourites list
        btn_favourite_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    String key = getItem(position).getBook_key();
                    FirebaseDatabase.getInstance().getReference("Users/"+user_ID+"/books/"+key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Object book = dataSnapshot.getValue();
                            Boolean is_saved = true;
                            SaveBook saveBook = new SaveBook(book,is_saved);
                            String book_key = dataSnapshot.getKey();
                            FirebaseDatabase.getInstance().getReference("Users/"+user_ID+"/saved_books/"+book_key).setValue(saveBook);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else{

                    String key = getItem(position).getBook_key();
                    FirebaseDatabase.getInstance().getReference("Users/"+user_ID+"/saved_books/"+key).removeValue();

                }
            }
        });

        return convertView;
    }

    //Get The Current User Logged in
    public void getUser_ID(){
        user_ID = mAuth.getCurrentUser().getUid();
    }


}
