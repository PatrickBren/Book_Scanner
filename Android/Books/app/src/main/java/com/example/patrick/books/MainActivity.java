package com.example.patrick.books;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.dynamic.ObjectWrapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Context;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //Header items variables
    public TextView tv_user;
    public EditText et_book_search;
    public Button btn_logout;
    public ListView lv_books;

    //Firebase database reference variable
    public DatabaseReference dref_main;

    //Book List and Adapter / Category List
    public Book_Adapter book_adapter;
    public ArrayList<Book> book_list = new ArrayList<>();
    public ArrayList<String> category_list = new ArrayList<>();
    public static final String ANDROID_CHANNEL_ID = "com.chikeandroid.tutsplustalerts.ANDROID";

    //Dialog Variables
    public String book_id;
    public String photo_id;
    public String new_book_id;
    public Integer book_position;
    public String book_title;
    public String new_book_title;
    public Boolean book_is_custom;

    //public Strings
    public String user_ID;
    public String book_list_category;

    //User Authentication
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        book_list_category = "";


        //Header Items
        tv_user = (TextView) findViewById(R.id.tv_user);
        et_book_search = (EditText) findViewById(R.id.et_book_search);
        btn_logout = (Button) findViewById(R.id.btn_logout);


        //Assign firebase Authentication signed in user to variable
        mAuth = FirebaseAuth.getInstance();

        //Book List View
        lv_books = (ListView) findViewById(R.id.lv_books);
        book_adapter = new Book_Adapter(this,R.layout.book_item,book_list);
        lv_books.setAdapter(book_adapter);

        createNotificationChannel();
        getUser_ID();
        Pi_Auth_Settings();
        getUser();
        getDatabaseRef();
        getBooks();

        //Opens Book info for item clicked on the list
        lv_books.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Book book_item = book_list.get(position);

                String book_id = book_item.getId();
                Intent intent = new Intent(getBaseContext(), Book_Info.class);
                intent.putExtra("BOOK", book_id);
                intent.putExtra("USER_ID", user_ID);
                startActivity(intent);
            }
        });

        //Opens Update or Delete Dialog
        lv_books.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Book book_item = book_list.get(position);
                book_id = book_item.getId();
                photo_id = book_item.getImg_id();
                book_title = book_item.getTitle();
                book_is_custom = book_item.getIs_custom();
                book_position = position;
                openDeleteDialog();

                return true;
            }
        });

        //When Text in the text box is changed, it will filter the list with text in the box
        et_book_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                book_list.clear();
                book_adapter.notifyDataSetChanged();

                dref_main.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        String book_title = dataSnapshot.child("title").getValue(String.class);
                        String book_author = dataSnapshot.child("author").getValue(String.class);
                        String book_category = dataSnapshot.child("category").getValue(String.class);
                        String book_id = dataSnapshot.getKey();
                        String img_id = dataSnapshot.child("book_id").getValue(String.class);
                        Boolean book_new = dataSnapshot.child("notification_settings").child("is_new").getValue(Boolean.class);
                        Boolean book_custom = dataSnapshot.child("general_settings").child("is_custom").getValue(Boolean.class);
                        String book_key = dataSnapshot.getKey();

                        Book book = new Book(book_title, book_author, book_id, img_id, book_new, book_custom, book_key, book_category);

                        if(et_book_search.getText().toString() == ""){
                            book_list.add(book);
                        }
                        else if(book.getTitle().toLowerCase().contains(et_book_search.getText().toString().toLowerCase())){
                            book_list.add(book);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Logout button that calls the logout function
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();

            }
        });

    }



    @Override
    //Creates menu items, and the list of menu items from the category list
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        menu.add(0,1000000000,0,"Borrowed");
        menu.add(0,1000000001,0,"Bought");

        for (int i = 0; i < category_list.size(); i++) {
            String category_item = category_list.get(i);
            menu.add(0, i, 0, category_item);
        }

        return true;
    }


    //When an item is the menu is clicked, it filters the book list by that item,
    //You can filter by borrowed/bought or by the category list
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        for ( int i = 0; i < category_list.size();  i++) {
            if(item.getItemId() == i){
                book_list.clear();
                book_list_category = category_list.get(i);
                getBooks();
            }
        }
        if(item.getTitle() == "Borrowed"){
            book_list.clear();
            getBorrowedBooks();
        }
        if(item.getTitle() == "Bought"){
            book_list.clear();
            getBoughtBooks();
        }
        switch (item.getItemId()) {
            case R.id.itm_add_book:
                Add_Book();
                return true;
            case R.id.itm_saved_book:
                Saved_Books();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Get The Current User Logged in
    public void getUser_ID(){
        user_ID = mAuth.getCurrentUser().getUid();
    }


    //Get the user that is currently logged on
    public void getUser(){
        FirebaseDatabase.getInstance().getReference("Users/"+user_ID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = (dataSnapshot.child("username").getValue(String.class));
                tv_user.setText(username);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Get Firebase Reference
    public void getDatabaseRef(){
        dref_main=FirebaseDatabase.getInstance().getReference("Users/"+user_ID+"/books");
    }

    //Dialog Functions
    public void openDeleteDialog(){
        DeleteDialog deleteDialog = new DeleteDialog();
        deleteDialog.show(getSupportFragmentManager(), "Delete Book");
    }

    public void openBorrow_Bought_Dialog(){
        Borrow_Bought_Dialog borrow_bought = new Borrow_Bought_Dialog();
        borrow_bought.show(getSupportFragmentManager(), "New Book");
    }

    //Get Book Data
    public void getBooks(){
        dref_main.addChildEventListener(new ChildEventListener() {
            @Override
            public  void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String category_select = book_list_category;
                String book_title = dataSnapshot.child("title").getValue(String.class);
                String book_author = dataSnapshot.child("author").getValue(String.class);
                String book_category = dataSnapshot.child("category").getValue(String.class);
                String book_id = dataSnapshot.getKey();
                String img_id = dataSnapshot.child("book_id").getValue(String.class);
                Boolean book_new = dataSnapshot.child("notification_settings").child("is_new").getValue(Boolean.class);
                Boolean book_custom = dataSnapshot.child("general_settings").child("is_custom").getValue(Boolean.class);
                Integer return_date_month = dataSnapshot.child("notification_settings").child("return_date").child("month").getValue(Integer.class);
                Integer return_date_day = dataSnapshot.child("notification_settings").child("return_date").child("day").getValue(Integer.class);
                String book_key = dataSnapshot.getKey();

                Book book = new Book(book_title, book_author, book_id, img_id, book_new, book_custom, book_key, book_category);


                if(category_select == ""){
                    book_list.add(book);
                }
                else if(book.getCategory().contains(category_select)){
                    book_list.add(book);
                }


                if(category_list.contains(book_category)){

                }
                else{
                    category_list.add(book_category);
                }


                if (book_new == true) {
                  new_book_id = book_id;
                  new_book_title = book_title;
                  openBorrow_Bought_Dialog();
                }

                Calendar c = Calendar.getInstance();
                Integer day = c.get(Calendar.DAY_OF_MONTH);
                Integer month = c.get(Calendar.MONTH);


                if(return_date_month != null && month == return_date_month && day < return_date_day){
                    Integer notification_ID = new Random().nextInt((1000 - 2) + 1) + 2;
                    Notification(book_title,return_date_day,return_date_month, notification_ID);
                }
                else if(return_date_month != null && month == return_date_month && return_date_day != null && day > return_date_day){
                    Integer notification_ID = new Random().nextInt((1000 - 2) + 1) + 2;
                    Urgent_notification(book_title,return_date_day,return_date_month, notification_ID);
                }

                book_adapter.notifyDataSetChanged();
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

    //Get Borrowed books
    public void getBorrowedBooks(){
        dref_main.addChildEventListener(new ChildEventListener() {
            @Override
            public  void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String book_title = dataSnapshot.child("title").getValue(String.class);
                String book_author = dataSnapshot.child("author").getValue(String.class);
                String book_category = dataSnapshot.child("category").getValue(String.class);
                String book_id = dataSnapshot.getKey();
                String img_id = dataSnapshot.child("book_id").getValue(String.class);
                Boolean book_new = dataSnapshot.child("notification_settings").child("is_new").getValue(Boolean.class);
                Boolean book_custom = dataSnapshot.child("general_settings").child("is_custom").getValue(Boolean.class);
                Boolean book_borrowed = dataSnapshot.child("general_settings").child("is_borrowed").getValue(Boolean.class);
                String book_key = dataSnapshot.getKey();

                Book book = new Book(book_title, book_author, book_id, img_id, book_new, book_custom, book_key, book_category);

                if(book_borrowed == true){
                    book_list.add(book);
                }
                book_adapter.notifyDataSetChanged();
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

    //Get Bought books
    public void getBoughtBooks(){
        dref_main.addChildEventListener(new ChildEventListener() {
            @Override
            public  void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String book_title = dataSnapshot.child("title").getValue(String.class);
                String book_author = dataSnapshot.child("author").getValue(String.class);
                String book_category = dataSnapshot.child("category").getValue(String.class);
                String book_id = dataSnapshot.getKey();
                String img_id = dataSnapshot.child("book_id").getValue(String.class);
                Boolean book_new = dataSnapshot.child("notification_settings").child("is_new").getValue(Boolean.class);
                Boolean book_custom = dataSnapshot.child("general_settings").child("is_custom").getValue(Boolean.class);
                Boolean book_bought = dataSnapshot.child("general_settings").child("is_bought").getValue(Boolean.class);
                String book_key = dataSnapshot.getKey();

                Book book = new Book(book_title, book_author, book_id, img_id, book_new, book_custom, book_key, book_category);




                if(book_bought == true){
                    book_list.add(book);
                }
                book_adapter.notifyDataSetChanged();
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


    //Execute the Socket Message
    public void Pi_Auth_Settings(){
        send sendcode = new send();
        sendcode.execute();
    }

    class send extends AsyncTask<Void,Void,Void> {
        Socket s;
        PrintWriter pw;
        @Override
        protected Void doInBackground(Void...params){
            try {
                s = new Socket("192.168.43.10",8001);
                pw = new PrintWriter(s.getOutputStream());
                pw.write(user_ID);
                pw.flush();
                pw.close();
                s.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    //Logout method that logouts the current user and goes back to the mainActivity
    public void logOut(){
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    //Started Add_book activity
    public void Add_Book(){
        Intent intent = new Intent(this, Add_BookActivity.class);
        startActivity(intent);
    }

    //Started Saved_book activity
    public void Saved_Books(){
        Intent intent = new Intent(this, FavouriteActivity.class);
        startActivity(intent);
    }

    //Notification channel for when a book is nearly due
    public void Notification(String title, Integer day, Integer month, Integer notification_ID){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ANDROID_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle("Warning, This book is overdue")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText('"'+title+'"'+" Is overdue and must be returned"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notification_ID, builder.build());



    }

    //Notification for when books overdue
    public void Urgent_notification(String title, Integer day, Integer month, Integer notification_ID){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ANDROID_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle("Warning, Book Nearly Due")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText('"'+title+'"'+" Needs to be returned by "+day+"/"+month))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notification_ID, builder.build());



    }

    //Create notification channel for return date books
    public void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(ANDROID_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }



}
