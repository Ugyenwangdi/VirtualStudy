package com.boilerplate.firestore.virtualstudy;



import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends Fragment {

    //RecycleView
    private RecyclerView notifications_list_view;
    private List<ModelNotification> notification_list;


    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    String currentUserId;


    private AdapterNotification adapterNotification;

    private DocumentSnapshot lastVisible;

    private boolean isFirstPageFirstLoad = true;


    public NotificationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        notification_list = new ArrayList<>();

        notifications_list_view = view.findViewById(R.id.notificationRV);

        adapterNotification = new AdapterNotification(notification_list, this);


        notifications_list_view.setHasFixedSize(true);
        notifications_list_view.setLayoutManager(new LinearLayoutManager(view.getContext()));
        notifications_list_view.setAdapter(adapterNotification);

//        loadMoreNotifications();
//
//        return view;

        if (firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            loadNotifications();

            notifications_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if (reachedBottom) {

                        loadMoreNotifications();

                    }

                }
            });

        }

        return view;

    }

    public void loadNotifications() {

        if (currentUserId != null) {

            Query firstQuery = firebaseFirestore.collection("Users").document(currentUserId).collection("Likes")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(5);
            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (e == null) {

                        if (!documentSnapshots.isEmpty()) {

                            if (isFirstPageFirstLoad) {

                                lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                                notification_list.clear();

                            }

                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String notificationID = doc.getDocument().getId();
                                    final ModelNotification notification = doc.getDocument().toObject(ModelNotification.class).withId(notificationID);

                                    if (isFirstPageFirstLoad) {

                                        notification_list.add(notification);

                                    } else {

                                        notification_list.add(0, notification);
                                    }


                                    adapterNotification.notifyDataSetChanged();


                                }
                            }

                            isFirstPageFirstLoad = true;

                        }

                    }
                }

            });
        }

    }

    public void loadMoreNotifications() {

        if (currentUserId != null) {

            Query nextQuery = firebaseFirestore.collection("Users").document(currentUserId).collection("Likes")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(5);

            nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (e == null) {

                        if (!documentSnapshots.isEmpty()) {

                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String notificationID = doc.getDocument().getId();
                                    final ModelNotification notification = doc.getDocument().toObject(ModelNotification.class).withId(notificationID);

                                    notification_list.add(notification);

                                    adapterNotification.notifyDataSetChanged();

                                }

                            }
                        }

                    }

                }
            });
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        loadNotifications();

    }
}

