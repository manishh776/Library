package com.kwaou.library.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kwaou.library.R;
import com.kwaou.library.adapters.NotificationAdapter;
import com.kwaou.library.helper.Config;
import com.kwaou.library.models.Notification;
import com.kwaou.library.sqlite.KeyValueDb;

import java.util.ArrayList;

public class NotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    ArrayList<Notification> notifications;
    private ProgressDialog progressDialog;
    TextView noNotifications;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        noNotifications = view.findViewById(R.id.nonotify);
        recyclerView = view.findViewById(R.id.recyclerViewNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        fetchNotifications();
    }

    private void fetchNotifications() {
        progressDialog.show();

        final String userid = KeyValueDb.get(getActivity(), Config.USERID, "");
        DatabaseReference notRefs = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_NOTIFICATIONS);
        notRefs.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notifications = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Notification notification = ds.getValue(Notification.class);
                    if (notification != null && notification.getUserid().equals(userid))
                        notifications.add(notification);
                }
                NotificationAdapter adapter = new NotificationAdapter(getActivity(), notifications);
                recyclerView.setAdapter(adapter);
                progressDialog.dismiss();
                if(notifications.isEmpty()){
                    recyclerView.setVisibility(View.GONE);
                    noNotifications.setVisibility(View.VISIBLE);
                }else{
                    recyclerView.setVisibility(View.VISIBLE);
                    noNotifications.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
