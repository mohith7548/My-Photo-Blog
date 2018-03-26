package com.kune.photoblog;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple  subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView blogListView;
    private List<BlogPost> blogList;

    private FirebaseFirestore firebaseFirestore;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private DocumentSnapshot lastVisible;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        blogList = new ArrayList<>();
        blogListView = view.findViewById(R.id.home_blog_list);

        blogRecyclerAdapter = new BlogRecyclerAdapter(blogList);

        firebaseFirestore = FirebaseFirestore.getInstance();

        blogListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                boolean isReachedBottom = !recyclerView.canScrollVertically(1);

                if (isReachedBottom) {
                    String desc = lastVisible.getString("description");
                    Toast.makeText(container.getContext(), "Reached: " + desc, Toast.LENGTH_SHORT).show();

                    loadMorePosts();

                }
            }
        });

        Query firstQuery =  firebaseFirestore.collection("Posts")
                                            .orderBy("timestamp", Query.Direction.DESCENDING)
                                            .limit(3);

        firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (documentSnapshots != null) {
                    lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() -1);

                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            // If data is Added
                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class);  // Populate the object into the BlogPost type object by passing the class
                            blogList.add(blogPost);

                            blogRecyclerAdapter.notifyDataSetChanged();

                        }

                    }
                }
            }
        });

        blogListView.setLayoutManager(new LinearLayoutManager(getContext()));
        blogListView.setAdapter(blogRecyclerAdapter);

        // Inflate the layout for this fragment
        return view;
    }

    private void loadMorePosts() {
        Query nextQuery =  firebaseFirestore.collection("Posts")
                                            .orderBy("timestamp", Query.Direction.DESCENDING)
                                            .startAfter(lastVisible)
                                            .limit(3);

        nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (!documentSnapshots.isEmpty()) {

                    lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() -1);
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            // If data is Added
                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class);  // Populate the object into the BlogPost type object by passing the class
                            blogList.add(blogPost);

                            blogRecyclerAdapter.notifyDataSetChanged();

                        }

                    }
                }
            }
        });

    }

}
