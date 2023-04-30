package com.gvvp.roadcrackdetector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialStyledDatePickerDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    private TextInputLayout fname_tf, lname_tf, username_tf, empno_tf, email_tf, phoneno_tf, dob_tf, password_tf, cpassword_tf;
    private Button calendar_btn, signup_btn;
    private TextView login_btn;
    private ProgressBar progress_bar;

    private FirebaseAuth mAuth;

    private int cyear, cmonth, cdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_signup);

        fname_tf = findViewById(R.id.register_fname_textfield);
        lname_tf = findViewById(R.id.register_lname_textfield);
        username_tf = findViewById(R.id.register_username_textfield);
        empno_tf = findViewById(R.id.register_empno_textfield);
        email_tf = findViewById(R.id.register_email_textfield);
        phoneno_tf = findViewById(R.id.register_phoneno_textfield);
        dob_tf = findViewById(R.id.register_dob_textfield);
        password_tf = findViewById(R.id.register_password_textfield);
        cpassword_tf = findViewById(R.id.register_cpassword_textfield);

        calendar_btn = findViewById(R.id.dob_calendar_btn);

        signup_btn = findViewById(R.id.signup_button);

        login_btn = findViewById(R.id.login_register_label);

        progress_bar = findViewById(R.id.register_progressbar);

        progress_bar.setVisibility(View.GONE);

        calendar_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCalendar();
            }
        });
    }

    public void loginbuttonclick(View view) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void openCalendar(){
        Date currentTime = Calendar.getInstance().getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentTime);
        cyear = calendar.get(Calendar.YEAR);
        cmonth = calendar.get(Calendar.MONTH);
        cdate = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, R.style.CalendarTheme, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                dob_tf.getEditText().setText(String.format("%02d/%02d/%d", dayOfMonth, month + 1, year));
            }
        }, cyear, cmonth, cdate);
        dialog.show();
    }
    private Boolean validateFname() {
        String val = fname_tf.getEditText().getText().toString();
        if (val.isEmpty()) {
            fname_tf.setError("Field cannot be empty");
            return false;
        }
        else {
            fname_tf.setError(null);
            fname_tf.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateLname() {
        String val = lname_tf.getEditText().getText().toString();
        if (val.isEmpty()) {
            lname_tf.setError("Field cannot be empty");
            return false;
        }
        else {
            lname_tf.setError(null);
            lname_tf.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateUsername() {
        String val = username_tf.getEditText().getText().toString();
        String noWhiteSpace = "\\A\\w{4,20}\\z";
        if (val.isEmpty()) {
            username_tf.setError("Field cannot be empty");
            return false;
        } else if (val.length() >= 20) {
            username_tf.setError("Username too long");
            return false;
        } else if (!val.matches(noWhiteSpace)) {
            username_tf.setError("White Spaces are not allowed");
            return false;
        } else {
            username_tf.setError(null);
            username_tf.setErrorEnabled(false);
            return true;
        }
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

    private Boolean validatePhoneNo() {
        String val = phoneno_tf.getEditText().getText().toString();
        if (val.isEmpty()) {
            phoneno_tf.setError("Field cannot be empty");
            return false;
        } else {
            phoneno_tf.setError(null);
            phoneno_tf.setErrorEnabled(false);
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

    private Boolean validateCpassword() {
        String pass = password_tf.getEditText().getText().toString();
        String val = cpassword_tf.getEditText().getText().toString();
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
            cpassword_tf.setError("Field cannot be empty");
            return false;
        } else if (!pass.equals(val)) {
            cpassword_tf.setError("Password does'nt match");
            return false;
        } else {
            cpassword_tf.setError(null);
            cpassword_tf.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateEmpno(){
        String val = empno_tf.getEditText().getText().toString();
        if (val.isEmpty()) {
            empno_tf.setError("Field cannot be empty");
            return false;
        }
        else {
            empno_tf.setError(null);
            empno_tf.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateDob() {
        String val = dob_tf.getEditText().getText().toString();

        // Check if value is empty
        if (val.isEmpty()) {
            dob_tf.setError("Field cannot be empty");
            return false;
        }

        // Check if value matches date format dd/mm/yyyy
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false);
        try {
            Date date = dateFormat.parse(val);

            // Check if age is greater than 18
            Calendar dob = Calendar.getInstance();
            dob.setTime(date);
            Calendar now = Calendar.getInstance();
            if (now.get(Calendar.YEAR) - dob.get(Calendar.YEAR) < 18) {
                dob_tf.setError("You must be at least 18 years old");
                return false;
            }

            // Clear any previous errors
            dob_tf.setError(null);
            dob_tf.setErrorEnabled(false);
            return true;

        } catch (ParseException e) {
            dob_tf.setError("Please enter a valid date in the format of dd/mm/yyyy");
            return false;
        }
    }


    public void registerbuttonclick(View view) {
        if(!validateFname() | !validateLname() | !validatePassword() | !validateCpassword() | !validatePhoneNo() | !validateEmail() | !validateUsername() | !validateEmpno() | !validateDob()){
            return;
        }
        else {
            String fname = fname_tf.getEditText().getText().toString();
            String lname = lname_tf.getEditText().getText().toString();
            String fullname = fname + " " + lname;
            String username = username_tf.getEditText().getText().toString();
            String empno = empno_tf.getEditText().getText().toString();
            String email = email_tf.getEditText().getText().toString();
            String phoneno = phoneno_tf.getEditText().getText().toString();
            String dob = dob_tf.getEditText().getText().toString();
            String password = password_tf.getEditText().getText().toString();
            progress_bar.setVisibility(view.VISIBLE);

            mAuth = FirebaseAuth.getInstance();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                storingdata user = new storingdata(fullname, username, empno, email, phoneno, dob);
                                FirebaseFirestore db  = FirebaseFirestore.getInstance();
                                DocumentReference newUserRef = db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                newUserRef.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                        Toast.makeText(SignupActivity.this,"User has been registered successfully", Toast.LENGTH_LONG).show();
                                        progress_bar.setVisibility(view.VISIBLE);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SignupActivity.this,"Failed to Register! Try Again!", Toast.LENGTH_LONG).show();
                                        progress_bar.setVisibility(view.GONE);
                                    }
                                });
                            }else {
                                Toast.makeText(SignupActivity.this, "User with same email already exists! Try using another email id", Toast.LENGTH_LONG).show();
                                progress_bar.setVisibility(view.GONE);
                            }
                        }
                    });
        }
    }
}