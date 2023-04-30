package com.gvvp.roadcrackdetector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextView signupbtn;

    private Button loginbtn, forgetpasswordbtn;

    private TextInputLayout email_tf, password_tf;

    private ProgressBar progressbar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // User is already logged in, start the DashboardActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish(); // Optional: close the LoginActivity so it's not on the back stack
        }

        signupbtn = findViewById(R.id.login_register_label);
        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
                finish();
            }
        });

        loginbtn = findViewById(R.id.login_button);
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userlogin();
            }
        });

        forgetpasswordbtn = findViewById(R.id.forget_password_button);
        forgetpasswordbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgetPassword.class));
            }
        });

        email_tf = findViewById(R.id.login_email_textfield);
        password_tf = findViewById(R.id.login_password_textfield);
        progressbar = findViewById(R.id.login_progressbar);

        progressbar.setVisibility(View.GONE);
    }

    private Boolean validateEmail() {
        String val = email_tf.getEditText().getText().toString();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (val.isEmpty()) {
            email_tf.setError("Field cannot be empty");
            return false;
        } else if (!val.matches(emailPattern)) {
            email_tf.setError("Invalid email address");
            return false;
        } else {
            email_tf.setError(null);
            email_tf.setErrorEnabled(false);
            return true;
        }
    }

    String passwordVal = "^" +
            "(?=.*[0-9])" +         //at least 1 digit
            "(?=.*[a-z])" +         //at least 1 lower case letter
            "(?=.*[A-Z])" +         //at least 1 upper case letter
            "(?=.*[@#$%^&+=])" +    //at least 1 special character
            "(?=\\S+$)" +           //no white spaces
            ".{4,}" +               //at least 4 characters
            "$";

    private Boolean validatePassword() {
        String val = password_tf.getEditText().getText().toString();
        String passwordVal = "^" +
                //"(?=.*[0-9])" +         //at least 1 digit
                //"(?=.*[a-z])" +         //at least 1 lower case letter
                //"(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +      //any letter
                "(?=.*[@#$%^&+=])" +    //at least 1 special character
                "(?=\\S+$)" +           //no white spaces
                ".{4,}" +               //at least 4 characters
                "$";
        if (val.isEmpty()) {
            password_tf.setError("Field cannot be empty");
            return false;
        } else if (!val.matches(passwordVal)) {
            password_tf.setError("Password is too weak");
            return false;
        } else {
            password_tf.setError(null);
            password_tf.setErrorEnabled(false);
            return true;
        }
    }

    private void userlogin() {
        String email = email_tf.getEditText().getText().toString().trim();
        String password = password_tf.getEditText().getText().toString().trim();

        if (!validatePassword() | !validateEmail()) {
            return;
        } else {
            final Context context = this;
            mAuth = FirebaseAuth.getInstance();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        progressbar.setVisibility(View.VISIBLE);
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.apply();

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }else{
                        Toast.makeText(LoginActivity.this, "Failed to login! Please Check your Credentials", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}