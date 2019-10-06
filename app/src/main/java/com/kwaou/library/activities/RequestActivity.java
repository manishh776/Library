package com.kwaou.library.activities;

import android.app.ProgressDialog;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kwaou.library.R;
import com.kwaou.library.adapters.RequestAdapter;
import com.kwaou.library.helper.Config;
import com.kwaou.library.models.BookDeal;
import com.kwaou.library.sqlite.KeyValueDb;

import java.util.ArrayList;

public class RequestActivity extends AppCompatActivity {

    ImageView back;
    RecyclerView recyclerViewRequests;
    ArrayList<BookDeal> deals;
    public  static ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        back = findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }

        });

        recyclerViewRequests = findViewById(R.id.recyclerViewRequests);
        recyclerViewRequests.setLayoutManager(new LinearLayoutManager(this));

        fetchBookDeals();
    }

    private void fetchBookDeals() {
        progressDialog.show();

        final String userid = KeyValueDb.get(this, Config.USERID, "");
        DatabaseReference bookDealsRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_BOOK_DEALS);

        bookDealsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                deals = new ArrayList<>();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    BookDeal deal = ds.getValue(BookDeal.class);
                    if(deal!=null && deal.getStatus() == 1 && deal.getLender().getId().equals(userid)){
                        deals.add(deal);
                    }
                }
                RequestAdapter adapter = new RequestAdapter(RequestActivity.this, deals);
                recyclerViewRequests.setAdapter(adapter);
                if(deals.isEmpty()){
                    recyclerViewRequests.setVisibility(View.GONE);
                    findViewById(R.id.norequest).setVisibility(View.VISIBLE);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
