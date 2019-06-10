package com.example.patrick.books;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    //Establish Variables
    Button btn_login_activity;
    Button btn_finish;
    EditText et_username;
    EditText et_email;
    EditText et_password;

    //User Authentication
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Assign Android items to variables
        btn_login_activity = (Button) findViewById(R.id.btn_login_activity);
        btn_finish = (Button) findViewById(R.id.btn_finish);
        et_username = (EditText) findViewById(R.id.et_username);
        et_email = (EditText) findViewById(R.id.et_email);
        et_password = (EditText) findViewById(R.id.et_password);

        //Authentication
        mAuth = FirebaseAuth.getInstance();

        //Onclick method that fires the goMain() method
        btn_login_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oepnLoginActivity();
            }
        });

        //Onclick method that fires the SignUp() method
        btn_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FinishRegister();

            }
        });

        //Check if user is already signed in/ if user is logged in, directed to TodoActivity
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser() != null){
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                }
            }
        };
    }

    //Listen for a change in the user state
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    //SignUp Method that gets edit text data, checks if the the edit text fields are empty,
    // if not it makes an account with firebase authentication and if successful it logs the user in
    public void FinishRegister(){
        String email = et_email.getText().toString();
        String username = et_username.getText().toString();
        String password = et_password.getText().toString();

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(username)){
            Toast.makeText(RegisterActivity.this,"Email, Username or Password is empty", Toast.LENGTH_LONG).show();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Toast.makeText(RegisterActivity.this,"Account Registered", Toast.LENGTH_LONG).show();
                    setUser();
                    openMainActivty();
                }
            });
        }
    }

    //Creates a user object in the firebase database
    public void setUser(){
        String email = et_email.getText().toString();
        String username = et_username.getText().toString();
        String userId = mAuth.getCurrentUser().getUid();

        User user = new User( email, username);
        FirebaseDatabase.getInstance().getReference("Users")
                .child(userId).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(RegisterActivity.this,"Database Updated", Toast.LENGTH_LONG).show();
            }
        });
    }

    //Goes to logIn activity
    public void oepnLoginActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    //Goes to TodoActivity
    public void openMainActivty(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


}
