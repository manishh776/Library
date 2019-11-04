package com.kwaou.library.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.kwaou.library.R;
import com.kwaou.library.helper.Config;
import com.kwaou.library.models.Book;
import com.kwaou.library.models.BookPackage;
import com.kwaou.library.models.Category;
import com.kwaou.library.models.User;
import com.kwaou.library.sqlite.KeyValueDb;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private static final CharSequence INVALID_DOB_ERROR = "Invalid date of birth";
    private static final CharSequence INVALID_PASS_ERROR = "Invalid password";
    private static final CharSequence INVALID_CPASS_ERROR = "Passwords and Repeat Passwords do not match";
    private static final int PICKBOOK_RESULT_CODE = 123 ;
    private static final int READ_STORAGE_REQUEST_CODE = 111;
    private Spinner categories;
    private EditText name, email, phone, pass, cpass;
    private TextView dateofBirth;
    private ImageView uploadBtn;
    private List<String> stringList;
    private ArrayList<Category> categoryArrayList;
    private Button registerBtn;
    private String EMPTY_ERROR = "Can't be empty";
    private String INVALID_EMAIL_ERROR = "Invalid email address";
    private CharSequence INVALID_PHONE_ERROR = "Invalid phone";
    private boolean bookUploaded = false;
    private int CATEGORY_SELECTED = 0;
    private ProgressDialog progressDialog;
    private StorageReference storageReference;
    private String downloadUri;
    private String TAG = RegisterActivity.class.getSimpleName();
    private String userid;
    private DatabaseReference userRef;
    private Book book = null;
    private TextView noofBooks;
    private ArrayList<Book> bookArrayList;
    private int price = 0;
    private String selectedDate = "";
    private EditText address;
    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        bookArrayList = new ArrayList<>();
        storageReference = FirebaseStorage.getInstance().getReference();
        stringList = new ArrayList<>();
        categoryArrayList = new ArrayList<>();
        initViews();
        fetchCategories();
        userRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_USERS);
        userid = userRef.push().getKey();
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
                progressDialog.dismiss();
                ArrayAdapter<String> deptAdapter = new ArrayAdapter<String>(RegisterActivity.this, android.R.layout.simple_spinner_item, stringList);
                deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categories.setAdapter(deptAdapter);

                categories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        CATEGORY_SELECTED = i;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initViews() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        dateofBirth = findViewById(R.id.dateofbirth);
        pass = findViewById(R.id.password);
        cpass = findViewById(R.id.cpassword);
        uploadBtn = findViewById(R.id.upload);
        categories = findViewById(R.id.categories);
        registerBtn = findViewById(R.id.registerBtn);
        noofBooks = findViewById(R.id.noofbooks);
        address = findViewById(R.id.address);

        registerBtn.setOnClickListener(this);
        uploadBtn.setOnClickListener(this);
        dateofBirth.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        if(view == registerBtn){
            if(valid()){
                checkForExistingmailandPhone();
            }
        } else if(view == uploadBtn){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getPermissionToReadStorage();
            }else {
                startAddBookActivity();
            }
        } else if(view == dateofBirth){
            selectDate();
        }
    }

    private void selectDate() {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        // TODO Auto-generated method stub
                        Calendar myCalendar = Calendar.getInstance();
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        String dateFormat = "dd/MM/yyyy"; //In which you need put here
                        SimpleDateFormat showdf = new SimpleDateFormat(dateFormat);
                        dateofBirth.setText(showdf.format(myCalendar.getTime()));
                        //gettintime in diff format to be used to as primary key for the reminder
                        selectedDate =  showdf.format(myCalendar.getTime());
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();

    }

    private void checkForExistingmailandPhone() {
            progressDialog.show();
            final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_USERS);
            final String mail= email.getText().toString();
            final String mobile  = phone.getText().toString();

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        User user = ds.getValue(User.class);
                        if (user != null) {
                            if (user.getEmail().equals(mail)) {
                                progressDialog.dismiss();
                                Toast.makeText(RegisterActivity.this, "This email is already in use.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (user.getPhone().equals(mobile)) {
                                progressDialog.dismiss();
                                Toast.makeText(RegisterActivity.this, "This mobile is already in use.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    }
                    registerUser();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICKBOOK_RESULT_CODE && resultCode == RESULT_OK){
            book = (Book) data.getSerializableExtra("book");
            if(book == null)
                Toast.makeText(this, "Some error occurred. Please try again.", Toast.LENGTH_SHORT).show();
            else{
                count = Integer.parseInt(noofBooks.getText().toString());
                ++count;
                noofBooks.setText(count + "");
                price+=book.getPrice();
                bookArrayList.add((Book) data.getSerializableExtra("book"));
                Log.e(TAG, book.getTitle() + "added");
                Toast.makeText(this, "book added", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void registerUser() {
        String namestr = name.getText().toString();
        String mail = email.getText().toString();
        String mobile = phone.getText().toString();
        String password = pass.getText().toString();
        String addresstxt = address.getText().toString();
        if(count > 0)
        addBookPackage(userid);
        String token = KeyValueDb.get(this, Config.USER_TOKEN,"");
        User user = new User(userid, namestr, mail, mobile, selectedDate, password, "",addresstxt);
        user.setToken(token);
        userRef.child(userid).setValue(user);
        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
        saveAndGotoMainActivity(user);
    }

    private void addBookPackage(String userid) {
        DatabaseReference packageRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_BOOKPACKAGES);
        String id = packageRef.push().getKey();
        BookPackage bookPackage = new BookPackage("",id, userid, bookArrayList, price, 0, categoryArrayList.get(CATEGORY_SELECTED));
        packageRef.child(id).setValue(bookPackage);
    }

    private void saveAndGotoMainActivity(User user) {
        Gson gson = new Gson();
        String userStr = gson.toJson(user);
        KeyValueDb.set(this, Config.USERID, userid,1);
        KeyValueDb.set(this, Config.USER, userStr,1);
        KeyValueDb.set(this, Config.LOGIN_STATE,"1",1);


        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

    private boolean valid() {
        boolean allOkay = true;

        if(TextUtils.isEmpty(name.getText())){
            name.setError(EMPTY_ERROR);
            allOkay = false;
        }
        if(TextUtils.isEmpty(email.getText())){
            email.setError(EMPTY_ERROR);
            allOkay = false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email.getText()).matches()){
            email.setError(INVALID_EMAIL_ERROR);
            allOkay = false;
        }

        if(TextUtils.isEmpty(phone.getText()) || phone.getText().toString().length()!=8){
            phone.setError(INVALID_PHONE_ERROR);
            allOkay = false;
        }

        if(!validateDate()){
            dateofBirth.setError(INVALID_DOB_ERROR);
            allOkay = false;
        }
        if(TextUtils.isEmpty(pass.getText()) || pass.getText().toString().length() < 8){
            pass.setError(INVALID_PASS_ERROR);
            allOkay = false;
        }
        if(!cpass.getText().toString().equals(pass.getText().toString())){
            cpass.setError(INVALID_CPASS_ERROR);
            allOkay = false;
        }

        if(address.getText().toString().isEmpty()){
            address.setError("Can't be empty");
            allOkay = false;
        }
        return allOkay;
    }

    private boolean validateDate() {
        return !selectedDate.isEmpty();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getPermissionToReadStorage() {
        // 1) Use the support library version ContextCompat.checkSelfPermission(...) to avoid
        // checking the build version since Context.checkSelfPermission(...) is only available
        // in Marshmallow
        // 2) Always check for permission (even if permission has already been granted)
        // since the user can revoke permissions at any time through Settings
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ) {

            // The permission is NOT already granted.
            // Check if the user has been asked about this permission already and denied
            // it. If so, we want to give more explanation about why the permission is needed.
            // Fire off an async request to actually get the permission
            // This will show the standard permission request dialog UI
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_STORAGE_REQUEST_CODE);

        }else{
            startAddBookActivity();
        }
    }

    private void startAddBookActivity() {
        Intent intent = new Intent(this, AddBookActivity.class);
        intent.putExtra("firstBook", true);
        intent.putExtra("category", categoryArrayList.get(CATEGORY_SELECTED));
        intent.putExtra("userid", userid);
        startActivityForResult(intent, PICKBOOK_RESULT_CODE);
    }

    // Callback with the request from calling requestPermissions(...)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_STORAGE_REQUEST_CODE) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                   ){
                startAddBookActivity();
            } else {
                Toast.makeText(this, "You must give permissions to use this app. App is exiting.", Toast.LENGTH_SHORT).show();
                finishAffinity();
            }
        }

    }

}
