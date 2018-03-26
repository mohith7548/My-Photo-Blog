package com.kune.photoblog;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private ArrayList<User> userList;
    private RecyclerView userListView;
    private UsersRecyclerAdapter usersRecyclerAdapter;
    private FirebaseFirestore firebaseFirestore;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        userList = new ArrayList<>();
        userListView = findViewById(R.id.users_list);
        mToolbar = findViewById(R.id.users_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");

        usersRecyclerAdapter = new UsersRecyclerAdapter();

        userListView.setLayoutManager(new LinearLayoutManager(this));
        userListView.setAdapter(usersRecyclerAdapter);

        firebaseFirestore = FirebaseFirestore.getInstance();
        Toast.makeText(this, "This is UsersActivity", Toast.LENGTH_LONG).show();

        firebaseFirestore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    for (DocumentSnapshot documentSnapshot: task.getResult()) {

//                        User user = documentSnapshot.toObject(User.class);
                        String name = documentSnapshot.getString("name");
                        String image = documentSnapshot.getString("image");
                        User user = new User(name, image);
                        Log.i("DETAILS", "Name: "+user.getName()+"//Image: "+user.getImage());
                        userList.add(user);
                        usersRecyclerAdapter.notifyDataSetChanged();
                    }

                }
            }
        });

    }

    public class UsersRecyclerAdapter extends RecyclerView.Adapter<UsersRecyclerAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String name = userList.get(position).getName();
            holder.setName(name);

            String image = userList.get(position).getImage();
            holder.setImage(image);
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private View mView;

            private TextView userName;
            private CircleImageView userImage;

            public ViewHolder(View itemView) {
                super(itemView);
                mView = itemView;
            }

            public void setName(String name) {
                userName = mView.findViewById(R.id.user_name);
                userName.setText(name);
            }

            public void setImage(String imageUrl) {
                userImage = mView.findViewById(R.id.user_image);
                Glide.with(getApplicationContext()).load(imageUrl).into(userImage);
            }
        }
    }



    public class User {
        private String name, image;

        public User(String name, String image) {
            this.name = name;
            this.image = image;
        }
        public User() { }

        public String getName() {
            return name;
        }

        public String getImage() {
            return image;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setImage(String imageUrl) {
            this.image = imageUrl;
        }
    }
}
