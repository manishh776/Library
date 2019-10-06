package com.kwaou.library.activities;

import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kwaou.library.R;
import com.kwaou.library.helper.Config;
import com.kwaou.library.models.Book;
import com.kwaou.library.models.BookPackage;
import com.kwaou.library.sqlite.KeyValueDb;

import java.util.ArrayList;

public class AddBookPackageActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = AddBookPackageActivity.class.getSimpleName();
    ImageView back;
    TextView noofBooks, addBooks, savePackage;
    private int price = 0;
    ArrayList<Book> bookArrayList;
    private int ADD_BOOK_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book_package);

        initViews();

        bookArrayList = new ArrayList<>();

    }

    private void initViews() {
        back = findViewById(R.id.back);
        noofBooks = findViewById(R.id.noofbooks);
        addBooks = findViewById(R.id.addbook);
        savePackage = findViewById(R.id.saveBtn);

        back.setOnClickListener(this);
        addBooks.setOnClickListener(this);
        savePackage.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == addBooks){
            startAddBookActivity();
        }
        else if(view == savePackage){
            int count = Integer.parseInt(noofBooks.getText().toString());
            if(count == 0){
                Toast.makeText(this, "Please add at least one book in the package", Toast.LENGTH_SHORT).show();
            }else{
                saveBookPackage();
            }
        }else if(view == back){
            finish();
        }
    }

    private void saveBookPackage() {
        String userid = KeyValueDb.get(this, Config.USERID,"");
        DatabaseReference packageRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_BOOKPACKAGES);
        String id = packageRef.push().getKey();
        BookPackage bookPackage = new BookPackage(id, userid, bookArrayList, price, 0);
        packageRef.child(id).setValue(bookPackage);

        Toast.makeText(this, "book package added", Toast.LENGTH_SHORT).show();
        finish();

    }

    private void startAddBookActivity() {
        Intent  intent = new Intent(this, AddBookActivity.class);
        startActivityForResult(intent, ADD_BOOK_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ADD_BOOK_REQUEST_CODE && resultCode == RESULT_OK){
            Book book = (Book) data.getSerializableExtra("book");
            int count = Integer.parseInt(noofBooks.getText().toString());
            ++count;
            noofBooks.setText(count + "");
            price+=book.getPrice();
            bookArrayList.add(book);
            Log.e(TAG, book.getTitle() + "added");
        }
    }
}
