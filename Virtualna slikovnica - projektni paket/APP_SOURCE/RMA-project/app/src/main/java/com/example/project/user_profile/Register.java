package com.example.project.user_profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.project.MainMenu;
import com.example.project.R;
import com.example.project.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class Register extends AppCompatActivity {

    Button redirectToLoginBtn;
    Button registerBtn;
    EditText firstName;
    EditText lastName;
    EditText email;
    EditText password;
    EditText passwordConfirm;
    FirebaseAuth auth;
    DatabaseReference database;

    // No whitespaces, minimum eight characters, at least one uppercase letter, one lowercase letter and one number
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Project);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        redirectToLoginBtn = findViewById(R.id.buttonRedirectToLogin);
        registerBtn = findViewById(R.id.buttonConfirm);
        firstName = findViewById(R.id.editTextFirstName);
        lastName = findViewById(R.id.editTextLastName);
        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        passwordConfirm = findViewById(R.id.editTextPassword2);


        redirectToLoginBtn.setOnClickListener(view -> {
            Intent in = new Intent(view.getContext(), Login.class);
            view.getContext().startActivity(in);
        });

        registerBtn.setOnClickListener(view -> {

            if(checkEnteredData()) {
                // firebase auth - registers user only with email and password
                auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Toast.makeText(Register.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                                if (!task.isSuccessful()) {
                                    Toast.makeText(Register.this, "Authentication failed." + task.getException(),
                                            Toast.LENGTH_SHORT).show();
                                } else {

                                    // save aditional user info to database
                                    User user = new User(firstName.getText().toString(), lastName.getText().toString());
                                    FirebaseUser loggedInUser = FirebaseAuth.getInstance().getCurrentUser();
                                    database.child("users").child(loggedInUser.getUid()).setValue(user);

                                    // redirect to mainmenu
                                    Intent in = new Intent(view.getContext(), MainMenu.class);
                                    view.getContext().startActivity(in);
                                }
                            }
                        });

            }

        });

    }

    boolean checkEnteredData() {
        boolean valid = true;

        if (firstName == null || firstName.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter your first name.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (firstName.getText().length() < 3 || firstName.getText().length() > 20) {
            Toast.makeText(this, "Please enter valid first name.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (lastName == null || lastName.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter your last name.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (lastName.getText().length() < 3 || firstName.getText().length() > 20) {
            Toast.makeText(this, "Please enter valid last name.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (email == null || email.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter your e-mail address.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (password == null || password.getText().toString().isEmpty() || passwordConfirm == null || passwordConfirm.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter password two times.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (!password.getText().toString().equals(passwordConfirm.getText().toString())) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (!PASSWORD_PATTERN.matcher(password.getText().toString()).matches()) {
            Toast.makeText(this, "Password is not valid! It should contain uppercase letter, lowercase letter, one number, minimum 8 characters without whitespaces.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            Toast.makeText(this, "E-mail address is not valid!", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;

    }
}