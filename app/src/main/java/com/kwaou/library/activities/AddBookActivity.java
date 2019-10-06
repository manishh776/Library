package com.kwaou.library.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kwaou.library.R;
import com.kwaou.library.helper.Config;
import com.kwaou.library.helper.ImagePicker;
import com.kwaou.library.models.Admin;
import com.kwaou.library.models.Book;
import com.kwaou.library.models.BookDeal;
import com.kwaou.library.models.Category;
import com.kwaou.library.sqlite.KeyValueDb;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class AddBookActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = AddBookActivity.class.getSimpleName();
    private static final int PICK_IMAGE = 111;
    ImageView back, pic;
    EditText title, desc, price;
    RadioButton exchange, sell;
    Button selectPdf, saveBtn;
    private static final int PICKPDF_RESULT_CODE = 123;
    private ProgressDialog progressDialog;
    private boolean bookUploaded = false;
    private String pdfUrl = "";
    private Uri filePathUri;
    private boolean image_selected = false;
    private boolean firstBook = false;
    private Category category;
    private String userid = null, imageUrl;
    private Spinner spinnerCategories;
    private ArrayList<String> stringList;
    private ArrayList<Category> categoryArrayList;
    private int CATEGORY_SELECTED = 0;
    private int  exchanged = 0;
    boolean cannotSell = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        firstBook = getIntent().getBooleanExtra("firstBook", false);
        category = (Category) getIntent().getSerializableExtra("category");
        userid = getIntent().getStringExtra("userid");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);


        initViews();

        if (firstBook) {
            exchange.setVisibility(View.GONE);
            sell.setVisibility(View.GONE);
            spinnerCategories.setVisibility(View.GONE);
        }else {
            stringList = new ArrayList<>();
            categoryArrayList = new ArrayList<>();
            fetchCategories();

        }

        handleRadiobuttons();

    }

    private void fetchCategories() {
        progressDialog.show();

        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_CATEGORIES);
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Category category = ds.getValue(Category.class);
                    if(category!=null) {
                        stringList.add(category.getName());
                        categoryArrayList.add(category);
                    }
                }
                ArrayAdapter<String> deptAdapter = new ArrayAdapter<String>(AddBookActivity.this, android.R.layout.simple_spinner_item, stringList);
                deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategories.setAdapter(deptAdapter);

                spinnerCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        CATEGORY_SELECTED = i;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
                fetchData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchData() {
        //pending is bookdeals in which user has requested to exchange
        DatabaseReference bookdeals = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_BOOK_DEALS);
        final String userid = KeyValueDb.get(this, Config.USERID, "");

        bookdeals.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    BookDeal deal = ds.getValue(BookDeal.class);
                    if(deal!= null && deal.getStatus() == 2 && (deal.getReceiver().getId().equals(userid) ||
                            deal.getLender().getId().equals(userid)
                    ) ){
                        exchanged++;
                    }
                }
                fetchAdminN();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchAdminN() {
        DatabaseReference adminref = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_ADMIN);
        adminref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                Admin admin = dataSnapshot.getValue(Admin.class);
                if(admin!=null && exchanged < admin.getN()){
                    sell.setVisibility(View.GONE);
                    exchange.setVisibility(View.GONE);
                    cannotSell = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void handleRadiobuttons() {
        exchange.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    price.setVisibility(View.INVISIBLE);
                }else{
                    price.setVisibility(View.VISIBLE);
                }
            }
        });

        sell.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    price.setVisibility(View.VISIBLE);
                }else{
                    price.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void initViews() {
        spinnerCategories = findViewById(R.id.categories);
        back = findViewById(R.id.back);
        pic = findViewById(R.id.pic);
        title = findViewById(R.id.title);
        desc = findViewById(R.id.desc);
        price = findViewById(R.id.price);

        exchange = findViewById(R.id.radioexchange);
        sell = findViewById(R.id.radioSell);

        selectPdf = findViewById(R.id.Btnpickpdf);
        saveBtn = findViewById(R.id.saveBtn);

        selectPdf.setOnClickListener(this);
        pic.setOnClickListener(this);
        back.setOnClickListener(this);
        saveBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if (view == selectPdf) {
            selectPdf();
        } else if (view == pic) {
            openGallery();
        }
        else if(view == back){
            finish();
        }
        else if(view == saveBtn){
                if(valid()){
                    saveBook();
                }
        }
    }

    private void saveBook() {
        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_BOOKS);

        String id = booksRef.push().getKey();
        String name = title.getText().toString();
        String description = desc.getText().toString();
        int cost = 0;
        if(!TextUtils.isEmpty(price.getText())){
            cost = Integer.parseInt(price.getText().toString());
        }
        if(category == null)
            category = categoryArrayList.get(CATEGORY_SELECTED);

        if(userid == null || userid.isEmpty()){
            userid = KeyValueDb.get(this, Config.USERID,"");
            Log.d(TAG, userid);
        }

        updateBookCountInCategory(category);

        Book book = new Book(id, pdfUrl, userid, name, description, imageUrl, category, cost);
        booksRef.child(id).setValue(book);
        Intent intent = new Intent();
        intent.putExtra("book", book);
        setResult(RESULT_OK, intent);
        Toast.makeText(this, "book added", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void updateBookCountInCategory(Category category) {
        int count = category.getBookCount() + 1;
        DatabaseReference cateRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_CATEGORIES);
        cateRef.child(category.getId()).child("bookCount").setValue(count);
    }

    private boolean valid() {
        boolean  allokay = true;

        if(TextUtils.isEmpty(title.getText())){
            title.setError("Can't be empty");
            allokay = false;
        }

        if(TextUtils.isEmpty(desc.getText())){
            desc.setError("Can't be empty");
        }

        if(sell.isChecked() && TextUtils.isEmpty(price.getText())){
            allokay = false;
            price.setError("Can't be empty");
        }

        if(!bookUploaded){
            allokay = false;
            Toast.makeText(this, "please upload pdf", Toast.LENGTH_SHORT).show();
        }
        if(!image_selected){
            allokay = false;
            Toast.makeText(this, "please select image", Toast.LENGTH_SHORT).show();
        }

        return allokay;
    }

    private void openGallery() {
        Intent intent = ImagePicker.getPickImageIntent(this);
        startActivityForResult(intent, PICK_IMAGE);
    }


    private void selectPdf() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, PICKPDF_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch (requestCode) {
            case PICKPDF_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    uploadPdf(data.getData());
                }
                break;

            case PICK_IMAGE: {
                if (resultCode == RESULT_OK) {
                    filePathUri = ImagePicker.getImageFromResult(this, resultCode, data);
                    Bitmap bitmapAvatar;
                    try {
                        bitmapAvatar = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePathUri);
                        pic.setImageBitmap(bitmapAvatar);
                        uploadImage();
                    } catch (FileNotFoundException e) {
                        Drawable drawableAvatar = getResources().getDrawable(R.drawable.ic_image_black_24dp);
                        bitmapAvatar = ((BitmapDrawable) drawableAvatar).getBitmap();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    private void uploadImage() {
        Log.d(TAG, filePathUri.getPath());
        progressDialog.show();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final StorageReference booksRef = storageReference.child("books/images/" + System.currentTimeMillis());
        booksRef.putFile(filePathUri)
                .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return booksRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    image_selected = true;
                    imageUrl = String.valueOf(task.getResult());
                    progressDialog.dismiss();
                    Toast.makeText(AddBookActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadPdf(Uri fileUri) {
        Log.d(TAG, fileUri.getPath());
        progressDialog.show();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final StorageReference booksRef = storageReference.child("books/pdfs/" + System.currentTimeMillis());
        booksRef.putFile(fileUri)
                .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return booksRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    bookUploaded = true;
                    pdfUrl = String.valueOf(task.getResult());
                    progressDialog.dismiss();
                    Toast.makeText(AddBookActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
