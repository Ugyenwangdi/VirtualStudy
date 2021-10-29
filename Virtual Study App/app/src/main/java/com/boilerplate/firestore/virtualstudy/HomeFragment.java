package com.boilerplate.firestore.virtualstudy;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    private RecyclerView blog_list_view;
    private List<BlogPost> blog_list;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private BlogRecyclerAdapter blogRecyclerAdapter;

    private DocumentSnapshot lastVisible;

    private boolean isFirstPageFirstLoad = true;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        blog_list = new ArrayList<>();

        blog_list_view = view.findViewById(R.id.blog_list_view);

        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list, view.getContext());

        blog_list_view.setHasFixedSize(true);
        blog_list_view.setLayoutManager(new LinearLayoutManager(view.getContext()));
        blog_list_view.setAdapter(blogRecyclerAdapter);

        if (firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if (reachedBottom) {

                        loadMorePosts();

                    }

                }
            });

            Query firstQuery = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(5);
            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (e == null) {

                        if (!documentSnapshots.isEmpty()) {

                            if (isFirstPageFirstLoad) {

                                lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                                blog_list.clear();

                            }

                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String blogPostId = doc.getDocument().getId();
                                    final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);

                                    if (isFirstPageFirstLoad) {

                                        blog_list.add(blogPost);

                                    } else {

                                        blog_list.add(0, blogPost);
                                    }


                                    blogRecyclerAdapter.notifyDataSetChanged();


                                }
                            }

                            isFirstPageFirstLoad = false;

                        }

                    }
                }

            });


        }

        return view;
    }

    public void loadMorePosts() {

        if (firebaseAuth.getCurrentUser() != null) {

            Query nextQuery = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(5);

            nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {


                    if(e == null) {

                        if (!documentSnapshots.isEmpty()) {

                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String blogPostId = doc.getDocument().getId();
                                    final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);

                                    blog_list.add(blogPost);

                                    blogRecyclerAdapter.notifyDataSetChanged();

                                }

                            }
                        }
                    }

                }
            });

        }

    }

}
