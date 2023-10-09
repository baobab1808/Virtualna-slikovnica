package com.example.project.user_profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.project.MainMenu;
import com.example.project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    Button redirectToRegisterBtn;
    Button signinBtn;
    Button passwordResetBtn;
    EditText username;
    EditText password;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Project);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        redirectToRegisterBtn = findViewById(R.id.buttonRedirectToRegister);
        username = findViewById(R.id.editTextUsername);
        password = findViewById(R.id.editTextPassword);
        signinBtn = findViewById(R.id.buttonConfirm);
        passwordResetBtn = findViewById(R.id.buttonPasswordReset);

        redirectToRegisterBtn.setOnClickListener(view -> {
            Intent in = new Intent(view.getContext(), Register.class);
            view.getContext().startActivity(in);
        });

        passwordResetBtn.setOnClickListener(view -> {
            Intent in = new Intent(view.getContext(), PasswordReset.class);
            view.getContext().startActivity(in);
        });

        signinBtn.setOnClickListener(view -> {

            if(checkEnteredData()) {
                // firebase auth - checks if email and password are valid
                auth.signInWithEmailAndPassword(username.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                //progressBar.setVisibility(View.GONE);
                                if (!task.isSuccessful()) {
                                    Toast.makeText(Login.this, "Authentication failed." + task.getException(),
                                            Toast.LENGTH_SHORT).show();
                                } else {
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

        if (username == null || username.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter your username.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (password == null || password.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter your password.", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

}