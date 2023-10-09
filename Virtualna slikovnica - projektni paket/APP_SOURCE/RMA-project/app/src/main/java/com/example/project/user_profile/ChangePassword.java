package com.example.project.user_profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class ChangePassword extends AppCompatActivity {

    EditText newPassword;
    EditText newPasswordConfirm;
    Button confirmBtn;
    FirebaseAuth auth;
    FirebaseUser loggedInUser;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Project);
        setContentView(R.layout.activity_change_password);

        newPassword = findViewById(R.id.inputNewPassword);
        newPasswordConfirm = findViewById(R.id.inputNewPasswordConfirm);
        confirmBtn = findViewById(R.id.buttonChangePasswordConfirm);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        loggedInUser = auth.getCurrentUser();

        // if user is not logged in, redirect to login page
        if (loggedInUser == null) {
            Intent in = new Intent(this, Login.class);
            startActivity(in);
        }

        confirmBtn.setOnClickListener(view -> {

            if (checkEnteredData()) {
                loggedInUser.updatePassword(newPassword.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ChangePassword.this, "Password is updated!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ChangePassword.this, "Failed to update password!", Toast.LENGTH_SHORT).show();
                                    //progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
            }

        });
    }

    boolean checkEnteredData() {
        boolean valid = true;

        if (newPassword == null || newPassword.getText().toString().isEmpty() || newPasswordConfirm == null || newPasswordConfirm.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter password two times.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (!newPassword.getText().toString().equals(newPasswordConfirm.getText().toString())) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (!PASSWORD_PATTERN.matcher(newPassword.getText().toString()).matches()) {
            Toast.makeText(this, "Password is not valid! It should contain uppercase letter, lowercase letter, one number, minimum 8 characters without whitespaces.", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;

    }
}