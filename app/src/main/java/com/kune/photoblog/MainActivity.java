package com.kune.photoblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseUser currentUser;
    private Toolbar mToolbar;
    private FloatingActionButton mainAddPostBtn;
    private BottomNavigationView mainBottomNav;

    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("MAIN ACTIVITY", "I'm MainActivity");
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        mainAddPostBtn = findViewById(R.id.main_add_post_btn);

        mainBottomNav = findViewById(R.id.main_bottom_nav);

        mToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Photo Blog");

        // Fragments
        homeFragment = new HomeFragment();
        notificationFragment = new NotificationFragment();
        accountFragment = new AccountFragment();

        ReplaceFragment(homeFragment); // Default is Home Fragment

        mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.bottom_home:
                        ReplaceFragment(homeFragment);
                        return true;

                    case R.id.bottom_notification:
                        ReplaceFragment(notificationFragment);
                        return true;

                    case R.id.bottom_account:
                        ReplaceFragment(accountFragment);
                        return true;

                    default:
                        return false;
                }

            }
        });

        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();

                if (currentUser == null) {
                    // User not Logged In
                    sendToLoginActivity();

                } else {
                    // Stay here itself
                }
            }
        });

        mainAddPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send to NewActivityPost
                Intent intent = new Intent(MainActivity.this, NewPostActivity.class);
                startActivity(intent);
            }
        });

    }

    private void sendToLoginActivity() {

        Log.i("MAIN ACTIVITY", "I'm sending to LoginActivity!");
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (currentUser == null) {
            // User not Logged In
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();

        } else {
            //User Logged In
            mFirebaseFirestore.collection("Users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {

                        if (!task.getResult().exists()) {
                            // If the user firestore data doesn't Exists
                            Intent intent = new Intent(MainActivity.this,SetupActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {

                        String e = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "Error: " + e, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        Intent intent = null;

        switch (item.getItemId()) {
            case R.id.action_logout_btn:
                mAuth.signOut();
                sendToLoginActivity();
                return true;

            case R.id.action_all_users:
                intent = new Intent(MainActivity.this, UsersActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_settings_btn:
                // TODO go to settings Activity
                intent = new Intent(MainActivity.this, SetupActivity.class);
                startActivity(intent);
                return true;

            default:
                return false;
        }

    }

    private void ReplaceFragment(Fragment fragment) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();

    }
}
