package com.mukuljoshi.blogapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {
    private EditText email, password;
    private TextView signUpText;
    private Button signUpButton;
    private FirebaseAuth auth;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.dark_blue));

        dialog = new ProgressDialog(this);

        // get the instance of firebase authtication == object reference variable
        auth = FirebaseAuth.getInstance();

        email = findViewById(R.id.singUp_email);
        password = findViewById(R.id.signUp_password);
        signUpText = findViewById(R.id.SignUpText);
        signUpButton = findViewById(R.id.signUpButton);

        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String stringEmail = email.getText().toString();
                String stringPassword = password.getText().toString();


                if (stringEmail.isEmpty()) {
                    email.setError("Email is required!");
                    email.requestFocus();
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(stringEmail).matches()) {
                    email.setError("please provide valid email");
                    email.requestFocus();
                    return;
                }
                if (stringPassword.isEmpty()) {
                    password.setError("Password is required!");
                    password.requestFocus();
                    return;
                }
                if (stringPassword.length() < 6) {
                    password.setError("Min password should be 6 character!");
                    password.requestFocus();
                    return;
                } else {
                    dialog.setTitle("Sign Up");
                    dialog.setMessage("Please wait...");
                    dialog.show();
                    auth.createUserWithEmailAndPassword(stringEmail, stringPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                startActivity(new Intent(SignUpActivity.this, SetUpActivity.class));
                                dialog.dismiss();
                                Toast.makeText(SignUpActivity.this, "Registration is Successfully done.", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }

                        }
                    });
                }
            }
        });
    }
}