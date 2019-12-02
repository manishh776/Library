package com.kwaou.library.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.kwaou.library.R;
import com.kwaou.library.helper.Config;
import com.kwaou.library.models.User;
import com.kwaou.library.sqlite.KeyValueDb;

public class SplashActivity extends AppCompatActivity implements View.OnClickListener {

    RelativeLayout noaccount;
    LinearLayout content;
    EditText phoneemail, password;
    Button buttonLogin;
    boolean isEmail = false;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        content = findViewById(R.id.content);
        noaccount = findViewById(R.id.noaccount);
        noaccount.setOnClickListener(this);
        phoneemail = findViewById(R.id.phoneemail);
        password = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.loginBtn);
        buttonLogin.setOnClickListener(this);
    }

    private void gotoMainActivity() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        };
        new Handler().postDelayed(runnable,2000);
    }

    @Override
    public void onClick(View view) {
        if (view == noaccount){
            startActivity(new Intent(this, RegisterActivity.class));
        }
        else if(view == buttonLogin){
            if(valid()){
                loginUser();
            }
        }
    }

    private void loginUser() {
        progressDialog.show();
        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_USERS);
        final String username = phoneemail.getText().toString();
        final String pass = password.getText().toString();
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        User user = ds.getValue(User.class);
                        if (user != null) {
                            if (isEmail && user.getEmail().equals(username) && user.getPassword().equals(pass)) {
                                Toast.makeText(SplashActivity.this, "login success", Toast.LENGTH_SHORT).show();
                                saveandMoveForward(user);
                                progressDialog.dismiss();
                                return;
                            }
                            if (!isEmail && user.getPhone().equals(username) && user.getPassword().equals(pass)) {
                                Toast.makeText(SplashActivity.this, "login success", Toast.LENGTH_SHORT).show();
                                saveandMoveForward(user);
                                progressDialog.dismiss();
                                return;
                            }
                        }
                }
                progressDialog.dismiss();
                Toast.makeText(SplashActivity.this, "invalid details", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void saveandMoveForward(User user) {
        KeyValueDb.set(this, Config.USERID, user.getId(),1);
        KeyValueDb.set(this, Config.LOGIN_STATE,"1",1);


        Gson gson = new Gson();
        String token = KeyValueDb.get(this, Config.USER_TOKEN,"");
        user.setToken(token);
        String userStr = gson.toJson(user);
        KeyValueDb.set(this, Config.USER, userStr,1);


        //update token
        FirebaseDatabase.getInstance().getReference(Config.FIREBASE_USERS).child(user.getId()).child("token").setValue(token);

        gotoMainActivity();
    }

    private boolean valid() {
        boolean allokay = true;
        String username = phoneemail.getText().toString();
        String pass = password.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            if (username.length() == 8) {
                try {
                    int num = Integer.parseInt(username);
                    isEmail = false;
                } catch (NumberFormatException e) {
                    phoneemail.setError("Invalid phone or email");
                    allokay =  false;
                }
            }else{
                phoneemail.setError("Invalid phone or email");
                allokay =  false;
            }
        }else{
            isEmail = true;
        }
        if(pass.isEmpty()){
            password.setError("Can't be empty");
            allokay =  false;
        }
        return  allokay;
    }
}
