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

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private TextInputLayout regEmail;
    private TextInputLayout regPassword;
    private TextInputLayout regConfirmPassword;
    private Button regButton;
    private Button regLoginBtn;
    private ProgressBar regProgressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("REGISTER ACTIVITY", "I'm RegisterActivity");
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        regEmail = findViewById(R.id.reg_email);
        regPassword = findViewById(R.id.reg_password);
        regConfirmPassword = findViewById(R.id.reg_confirm_password);
        regButton = findViewById(R.id.reg_btn);
        regLoginBtn = findViewById(R.id.reg_login_btn);
        regProgressbar = findViewById(R.id.reg_progress);

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String myEmail = regEmail.getEditText().getText().toString();
                String myPassword = regPassword.getEditText().getText().toString();
                String myConfirmPassword = regConfirmPassword.getEditText().getText().toString();

                if (!TextUtils.isEmpty(myEmail) && !TextUtils.isEmpty(myPassword) && !TextUtils.isEmpty(myConfirmPassword)) {

                    if (myPassword.equals(myConfirmPassword)) {

                        regProgressbar.setVisibility(View.VISIBLE);

                        mAuth.createUserWithEmailAndPassword(myEmail, myPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                regProgressbar.setVisibility(View.INVISIBLE);

                                if (task.isSuccessful()) {

                                    //sendToMainActivity();
                                    Intent intent = new Intent(RegisterActivity.this,SetupActivity.class);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    String e = task.getException().getMessage();

                                    Toast.makeText(RegisterActivity.this, "Error: " + e , Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

                    } else {
                        Toast.makeText(RegisterActivity.this, "Both Password fields donot match!!", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });



        regLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        if (currentUser != null) {
            sendToMainActivity();
        }
    }

    private void sendToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        finish();
    }
}
