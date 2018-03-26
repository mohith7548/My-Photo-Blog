    package com.kune.photoblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout loginEmail;
    private TextInputLayout loginPassword;
    private Button loginBtn;
    private Button loginRegBtn;
    private ProgressBar loginProgress;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("LOGIN ACTIVITY", "I'm LoginActivity");
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        loginEmail = findViewById(R.id.reg_email);
        loginPassword = findViewById(R.id.reg_password);
        loginBtn = findViewById(R.id.login_log_in_btn);
        loginRegBtn = findViewById(R.id.login_new_btn);
        loginProgress = findViewById(R.id.login_progress);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String myEmail = loginEmail.getEditText().getText().toString();
                String myPassword = loginPassword.getEditText().getText().toString();

                if (!TextUtils.isEmpty(myEmail) && !TextUtils.isEmpty(myPassword)) {

                    loginProgress.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(myEmail, myPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            loginProgress.setVisibility(View.INVISIBLE);

                            if (task.isSuccessful()) {
                                // User is logged in successfully   ----->   Send to MainActivity

                                sendToMainActivity();

                            } else {
                                // If Not SuccessFul   ----> Give a Toast
                                String e = task.getException().getMessage();

                                Toast.makeText(LoginActivity.this, "Error: " + e, Toast.LENGTH_LONG).show();

                            }
                        }
                    });

                } else {
                    // If provided Blank Details
                    Toast.makeText(LoginActivity.this, "Don't leave any fields empty!", Toast.LENGTH_SHORT).show();
                }

            }
        });


        loginRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("LOGIN ACTIVITY", "I'm sending to RegisterActivity!");
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            sendToMainActivity();
        }
    }

    private void sendToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        finish();
    }


}
