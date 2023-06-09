package com.gvvp.roadcrackdetector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordActivity extends AppCompatActivity {

    private TextInputLayout email_tf;
    private Button reset_password_btn, fp_login_btn;
    private ProgressBar progressbar;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_forget_password);

        email_tf = findViewById(R.id.fp_email_textfield);
        reset_password_btn = findViewById(R.id.reset_password_button);
        fp_login_btn = findViewById(R.id.fp_login_button);
        progressbar = findViewById(R.id.fp_progressbar);

        auth = FirebaseAuth.getInstance();

        progressbar.setVisibility(View.GONE);

        reset_password_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        fp_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ForgetPasswordActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    private void resetPassword(){
        String email = email_tf.getEditText().getText().toString().trim();

        if(email.isEmpty()){
            email_tf.setError("Email is Required");
            email_tf.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            email_tf.setError("Please provide valid email!");
            email_tf.requestFocus();
            return;
        }

        progressbar.setVisibility(View.VISIBLE);
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    progressbar.setVisibility(View.VISIBLE);
                    Toast.makeText(ForgetPasswordActivity.this,"Check your email to reset your Password!", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(ForgetPasswordActivity.this,"No user Registered with this Email Id", Toast.LENGTH_LONG).show();
                }progressbar.setVisibility(View.GONE);
            }
        });
    }
}