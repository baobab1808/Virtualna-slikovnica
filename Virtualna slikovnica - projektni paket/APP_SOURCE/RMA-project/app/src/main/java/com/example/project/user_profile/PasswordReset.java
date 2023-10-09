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

public class PasswordReset extends AppCompatActivity {

    EditText email;
    Button resetPassword;
    Button back;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Project);
        setContentView(R.layout.activity_password_reset);

        email = findViewById(R.id.inputEmail);
        resetPassword = findViewById(R.id.buttonSendEmail);
        back = findViewById(R.id.backToLogin);

        auth = FirebaseAuth.getInstance();

        back.setOnClickListener(view -> {
            Intent in = new Intent(view.getContext(), Login.class);
            view.getContext().startActivity(in);
        });

        resetPassword.setOnClickListener(view -> {

            String emailStr = email.getText().toString();
            if (emailStr == null || emailStr.isEmpty()) {
                Toast.makeText(this, "Please enter email.", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.sendPasswordResetEmail(emailStr)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(PasswordReset.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(PasswordReset.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

    }
}