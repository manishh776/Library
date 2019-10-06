package com.kwaou.library.activities;

import android.app.ProgressDialog;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kwaou.library.R;
import com.kwaou.library.adapters.BookPackageAdapter;
import com.kwaou.library.helper.Config;
import com.kwaou.library.models.BookPackage;
import com.kwaou.library.sqlite.KeyValueDb;

import java.util.ArrayList;

public class AllMyBooksActivity extends AppCompatActivity implements View.OnClickListener {

    private ArrayList<BookPackage> bookPackages;
    private ImageView back;
    private TextView txtinfo;
    private RecyclerView recyclerViewBooks;
    private ProgressDialog progressDialog;
    public  static BookPackage newbook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_my_books);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        bookPackages = new ArrayList<>();

        back = findViewById(R.id.back);
        txtinfo = findViewById(R.id.txtinfo);
        recyclerViewBooks = findViewById(R.id.recyclerViewBooks);
        recyclerViewBooks.setLayoutManager(new GridLayoutManager(this,3));

        back.setOnClickListener(this);

        newbook = (BookPackage) getIntent().getSerializableExtra("new");
        if(newbook!=null)
            txtinfo.setText("Pick a book package to exchange with");

        fetchBooks();

    }

    private void fetchBooks() {
        progressDialog.show();

        final String userid = KeyValueDb.get(this, Config.USERID,"");
        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_BOOKPACKAGES);
        booksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bookPackages = new ArrayList<>();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    BookPackage book = ds.getValue(BookPackage.class);
                    if(newbook!=null) {
                        if (book != null && book.getStatus() == 0 && book.getUserid().equals(userid))
                            bookPackages.add(book);
                    }else{
                        if (book != null  && book.getUserid().equals(userid))
                            bookPackages.add(book);
                    }
                }
                BookPackageAdapter adapter = new BookPackageAdapter(AllMyBooksActivity.this, bookPackages);
                recyclerViewBooks.setAdapter(adapter);
                if(bookPackages.isEmpty()){
                    findViewById(R.id.nobooks).setVisibility(View.VISIBLE);
                    recyclerViewBooks.setVisibility(View.GONE);
                }

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        if(view == back){
            finish();
        }
    }
}
