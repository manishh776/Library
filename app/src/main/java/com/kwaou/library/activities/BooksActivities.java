package com.kwaou.library.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BooksActivities extends AppCompatActivity {

    EditText search;
    RecyclerView recyclerViewBooks;
    BookPackageAdapter adapter;
    public  static ProgressDialog progressDialog;
    ArrayList<BookPackage> bookPackages;
    View view;
    String TAG = BooksActivities.class.getSimpleName();
    Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_books);
        initViews();

    }

    private void initViews() {
        recyclerViewBooks = findViewById(R.id.recyclerViewBooks);
        recyclerViewBooks.setLayoutManager(new GridLayoutManager(this, 3));
        search = view.findViewById(R.id.search);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        fetchBooks();

        setUpSearch();

    }

    private void setUpSearch() {
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(adapter!=null && MainActivity.viewPager.getCurrentItem() == 0)
                adapter.filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
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
                    if(book!=null && book.getStatus() == 0 && !book.getUserid().equals(userid))
                        bookPackages.add(book);
                }
                adapter = new BookPackageAdapter(BooksActivities.this, bookPackages);
                recyclerViewBooks.setAdapter(adapter);
                if(bookPackages.isEmpty()){
                    recyclerViewBooks.setVisibility(View.GONE);
                    view.findViewById(R.id.nobooks).setVisibility(View.VISIBLE);
                }else{
                    recyclerViewBooks.setVisibility(View.VISIBLE);
                    view.findViewById(R.id.nobooks).setVisibility(View.GONE);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




}
