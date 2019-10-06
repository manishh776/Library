package com.kwaou.library;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.kwaou.library.adapters.BookInPackageAdapter;
import com.kwaou.library.models.BookPackage;

public class BooksInPackageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books_in_package);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        RecyclerView recyclerViewBooks = findViewById(R.id.recyclerViewBooks);
        recyclerViewBooks.setLayoutManager(new GridLayoutManager(this,3));
        BookPackage bookPackage = (BookPackage) getIntent().getSerializableExtra("book_package");
        BookInPackageAdapter adapter = new BookInPackageAdapter(this, bookPackage.getBookArrayList());
        recyclerViewBooks.setAdapter(adapter);
    }
}
