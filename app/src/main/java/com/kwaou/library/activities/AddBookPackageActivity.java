package com.kwaou.library.activities;

import android.app.ProgressDialog;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kwaou.library.R;
import com.kwaou.library.helper.Config;
import com.kwaou.library.models.Book;
import com.kwaou.library.models.BookPackage;
import com.kwaou.library.models.Category;
import com.kwaou.library.sqlite.KeyValueDb;

import java.util.ArrayList;

public class AddBookPackageActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = AddBookPackageActivity.class.getSimpleName();
    ImageView back;
    TextView noofBooks, addBooks, savePackage;
    private int price = 0;
    ArrayList<Book> bookArrayList;
    private int ADD_BOOK_REQUEST_CODE = 123;
    private Spinner spinnerCategories;
    private ArrayList<Category> categoryArrayList;
    private ProgressDialog progressDialog;
    private ArrayList<String> stringList;
    private int CATEGORY_SELECTED = 0;
    private EditText packageName;

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
        spinnerCategories = findViewById(R.id.categories);
        back.setOnClickListener(this);
        addBooks.setOnClickListener(this);
        savePackage.setOnClickListener(this);
        stringList = new ArrayList<>();
        categoryArrayList = new ArrayList<>();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);
        packageName = findViewById(R.id.packageName);

        fetchCategories();
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
                if(!TextUtils.isEmpty(packageName.getText()))
                saveBookPackage();
                else
                    packageName.setError("Can't be empty");
            }
        }else if(view == back){
            finish();
        }
    }

    private void saveBookPackage() {
        String setName = packageName.getText().toString();
        String userid = KeyValueDb.get(this, Config.USERID,"");
        DatabaseReference packageRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_BOOKPACKAGES);
        String id = packageRef.push().getKey();
        BookPackage bookPackage = new BookPackage(setName, id, userid, bookArrayList, price, 0, categoryArrayList.get(CATEGORY_SELECTED));
        packageRef.child(id).setValue(bookPackage);
        Toast.makeText(this, "book package added", Toast.LENGTH_SHORT).show();
        finish();

    }

    private void startAddBookActivity() {
        Intent  intent = new Intent(this, AddBookActivity.class);
        intent.putExtra("category", categoryArrayList.get(CATEGORY_SELECTED));
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
            if(count > 0){
                spinnerCategories.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void fetchCategories() {
        progressDialog.show();
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_CATEGORIES);
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                stringList = new ArrayList<>();
                categoryArrayList = new ArrayList<>();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Category category = ds.getValue(Category.class);
                    if(category!=null) {
                        stringList.add(category.getName());
                        categoryArrayList.add(category);
                    }
                }
                ArrayAdapter<String> deptAdapter = new ArrayAdapter<String>(AddBookPackageActivity.this, android.R.layout.simple_spinner_item, stringList);
                deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategories.setAdapter(deptAdapter);

                spinnerCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if(spinnerCategories.getVisibility() == View.VISIBLE)
                        CATEGORY_SELECTED = i;
                        Log.d(TAG, "index" + CATEGORY_SELECTED +"  " + i);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }

}
