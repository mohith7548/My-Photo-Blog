package com.kune.photoblog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    List<BlogPost> blogList;
    private Context context;
    private CollectionReference userReference;

    public BlogRecyclerAdapter(List<BlogPost> blogList) {

        this.blogList = blogList;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);

        context = parent.getContext();
        userReference = FirebaseFirestore.getInstance().collection("Users");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        String descData = blogList.get(position).getDescription();
        holder.setDescText(descData);

        String image_url = blogList.get(position).getImage_url();
        holder.setBlogImage(image_url);

        Date time = blogList.get(position).getTimestamp();

        holder.setTimeStamp(new SimpleDateFormat("dd/MM/yyyy").format(time));

        String user_id = blogList.get(position).getUser_id();
        userReference.document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String user_name = task.getResult().getString("name");
                        holder.setUserName(user_name);

                        String user_pic = task.getResult().getString("image");
                        holder.setUserPic(user_pic);
                    }
                } else {
                    String e = task.getException().getMessage();
                    Toast.makeText(context, "Error: " + e, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private TextView descView;
        private ImageView blogImageView;
        private TextView userName;
        private TextView timeStamp;
        private CircleImageView userPic;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDescText(String text) {
            descView = mView.findViewById(R.id.home_blog_post_desc);
            descView.setText(text);
        }

        public void setBlogImage(String downloadUri) {
            blogImageView = mView.findViewById(R.id.home_blog_post_image);
            Glide.with(context).load(downloadUri).into(blogImageView);
        }

        public void setUserName(String user_name) {
            userName = mView.findViewById(R.id.home_blogger_name);
            userName.setText(user_name);
        }

        public void setUserPic(String user_pic_url) {
            userPic = mView.findViewById(R.id.home_blogger_image);
            Glide.with(context).load(user_pic_url).into(userPic);
        }

        public void setTimeStamp(String time) {
            timeStamp = mView.findViewById(R.id.home_blog_post_time);
            timeStamp.setText(time);
        }
    }
}
