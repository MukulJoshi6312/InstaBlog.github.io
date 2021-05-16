package com.mukuljoshi.blogapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText forgotEmail;
    TextView goBack;
    TextView emailSend, emailNotSend;
    Button forgotButton;
    FirebaseAuth auth;
    Animation side, bottomAnim;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.dark_blue));

        forgotEmail = findViewById(R.id.forgot_email);
        goBack = findViewById(R.id.goback);
        forgotButton = findViewById(R.id.forgot_button);
        emailSend = findViewById(R.id.email_send);
        emailNotSend = findViewById(R.id.email_not_send);
        progressBar = findViewById(R.id.forgotProg);
        auth = FirebaseAuth.getInstance();

        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.side_animation);
        side = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        forgotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                resetPassword();

            }
        });

    }

    private void resetPassword() {

        String email = forgotEmail.getText().toString();

        if (email.isEmpty()) {
            forgotEmail.setError("Email is required!");
            forgotEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            forgotEmail.setError("Please provide valid email!");
            forgotEmail.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Check your email to rest your " +
                            "password", Toast.LENGTH_SHORT).show();
                    emailSend.setVisibility(View.VISIBLE);
                    emailSend.setAnimation(side);
                    emailNotSend.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                }else {
                    Toast.makeText(ForgotPasswordActivity.this, "Try again! Something wrong " +
                                    "happened!!",
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    emailSend.setVisibility(View.GONE);
                    emailNotSend.setVisibility(View.VISIBLE);
                    emailNotSend.setAnimation(bottomAnim);

                }

            }
        });


    }
}