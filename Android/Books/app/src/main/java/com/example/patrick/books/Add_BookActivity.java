package com.example.patrick.books;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.UUID;

public class Add_BookActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private static final String TAG = "Add_BookActivity";

    //XMl Elements
    public EditText et_book_title;
    public EditText et_book_author;
    public EditText et_book_publisher;
    public EditText et_book_description;
    public EditText et_book_category;
    public Button btn_add_book_confirm;
    public Button btn_cancel;
    public CheckBox cb_bought;
    public CheckBox cb_borrowed;

    public Button btn_pick_image;
    private ImageView imageView;

    //Image Path and Media Request
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;

    //Firebase database reference variable / storage ref
    public DatabaseReference dref;
    private StorageReference mStorage;

    //public variables
    public String user_ID;
    public String book_ID;


    //Date Object
    public DateObject dateObject;

    //User Authentication
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add__book);

        //Assign firebase Authentication signed in user to variable
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();

        et_book_title = (EditText) findViewById(R.id.et_book_title);
        et_book_author = (EditText) findViewById(R.id.et_book_author);
        et_book_publisher = (EditText) findViewById(R.id.et_book_publisher);
        et_book_description = (EditText) findViewById(R.id.et_book_description);
        et_book_category = (EditText) findViewById(R.id.et_book_category);
        btn_add_book_confirm = (Button) findViewById(R.id.btn_add_book_confirm);
        btn_pick_image = (Button) findViewById(R.id.btn_pick_image);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        cb_borrowed = (CheckBox) findViewById(R.id.cb_Borrowed);
        cb_bought = (CheckBox) findViewById(R.id.cb_Bought);
        imageView = (ImageView) findViewById(R.id.imgView);


        cb_borrowed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBorrow_CheckboxClicked(v);
            }
        });

        cb_bought.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBought_CheckboxClicked(v);
            }
        });

        btn_pick_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    pickImage();
                }
            });

        //Checks if all fields have been filed before publishing data
        btn_add_book_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(et_book_title.getText())
                        || TextUtils.isEmpty(et_book_author.getText())
                        || TextUtils.isEmpty(et_book_publisher.getText())
                        || TextUtils.isEmpty(et_book_description.getText())
                        || TextUtils.isEmpty(et_book_category.getText())){
                    Toast.makeText(Add_BookActivity.this, "Please Enter Book Details", Toast.LENGTH_SHORT).show();
                }
                else if(cb_borrowed.isChecked() == false && cb_bought.isChecked() == false){
                    Toast.makeText(Add_BookActivity.this, "Please Pick An Option", Toast.LENGTH_SHORT).show();
                }
                else if(cb_borrowed.isChecked() == true && cb_bought.isChecked() == false && dateObject == null){
                    Toast.makeText(Add_BookActivity.this, "Please Pick A Return Date", Toast.LENGTH_SHORT).show();
                }
                else if(filePath == null){
                    Toast.makeText(Add_BookActivity.this, "Please Pick A Image", Toast.LENGTH_SHORT).show();
                }
                    else{
                        getUser_ID();
                        setBook_ID();
                        uploadImage();
                        add_Book();
                    }
                }

        });

        //Open Main Activity
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_MainActivity();
            }
        });


    }

    //Opens up the users media folder so they can pick and image to upload
    public void pickImage(){

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    //Triggers when the image is selected and gives a preview
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
            && data != null && data.getData() != null )
    {
        filePath = data.getData();

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
            imageView.setImageBitmap(bitmap);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}

    //Uploads the selected image to firebase storage, the storage contains a users folder and the image name is
    //Randomly generated to keep them all unqiue
    //this ID is assigned to the book_id in the book object
    private void uploadImage() {

        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = mStorage.child("book_covers/" +user_ID+"/"+book_ID);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(Add_BookActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    }) .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                            .getTotalByteCount());
                    progressDialog.setMessage("Uploaded "+(int)progress+"%");
                }

            });

        }
    }


    //Get The Current User Logged in
    public void getUser_ID(){
        user_ID = mAuth.getCurrentUser().getUid();
    }

    //Generate Random ID
    public void setBook_ID(){
        book_ID =  UUID.randomUUID().toString();
    }

    //Cancel Book Add
    public void open_MainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    //Grabs, default values, random book id and filed values, then adds the object to the logged in users firebase
    public void add_Book(){
        Boolean is_new = false;
        Boolean is_custom = true;
        Boolean is_borrowed = cb_borrowed.isChecked();
        Boolean is_bought = cb_bought.isChecked();
        General_Settings general_settings = new General_Settings(is_custom,is_borrowed,is_bought);
        Notification_Settings notification = new Notification_Settings(dateObject,is_new);
        String title = et_book_title.getText().toString();
        String author = et_book_author.getText().toString();
        String publisher = et_book_publisher.getText().toString();
        String description = et_book_description.getText().toString();
        String category = et_book_category.getText().toString();
        AddBook book = new AddBook(book_ID,title,author,publisher,description,category, general_settings,notification);

        //Add the book to the firebase
        FirebaseDatabase.getInstance().getReference("Users/"+user_ID+"/books").push().setValue(book).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {


            }
        });
    }


    //When the bought check box is checked, the borrowed check box is unchecked
    public void onBought_CheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();
        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.cb_Bought:
                if (checked){
                    cb_borrowed.setChecked(false);


                }
        }
    }

    //When the borrowed box is checked, the bought box is unchecked and a date dialog
    //asks the user to enter a return date
    public void onBorrow_CheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();
        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.cb_Borrowed:
                if (checked){
                    if(checked == true){
                        cb_bought.setChecked(false);
                        DialogFragment datePicker = new DatePickerFragment();
                        datePicker.show(getSupportFragmentManager(),"date picker");

                    }
                }
        }
    }

    @Override
    //Saves the data selected from the date dialog
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.YEAR, year);
        dateObject = new DateObject(dayOfMonth,month,year);
    }


}
