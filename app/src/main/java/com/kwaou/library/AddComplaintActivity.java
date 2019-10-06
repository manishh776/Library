package com.kwaou.library;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.kwaou.library.helper.Config;
import com.kwaou.library.models.Admin;
import com.kwaou.library.models.BookPackage;
import com.kwaou.library.models.Complaint;
import com.kwaou.library.models.User;
import com.kwaou.library.retrofit.RetrofitClient;
import com.kwaou.library.sqlite.KeyValueDb;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AddComplaintActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = AddComplaintActivity.class.getSimpleName();
    ImageView back;
    EditText title, desc;
    TextView registerBtn;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_complaint);


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        back = findViewById(R.id.back);
        title = findViewById(R.id.title);
        desc = findViewById(R.id.desc);
        registerBtn = findViewById(R.id.registerBtn);

        registerBtn.setOnClickListener(this);
        back.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if(view == back){
            finish();
        }
        else if(view == registerBtn){
            if(valid()){
                notifyAdmin();
            }
        }
    }
    private void notifyAdmin() {
        progressDialog.show();
        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_ADMIN);
        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Admin admin = dataSnapshot.getValue(Admin.class);
                if(admin!=null){
                    sendPush(admin.getToken());
                }else{
                    Log.e(TAG,"admin is null");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendPush(String token) {
        final Gson gson = new Gson();
        String userstr = KeyValueDb.get(this, Config.USER,"");
        final User requester = gson.fromJson(userstr, User.class);
        Map<String, Object> map = new HashMap<>();
        map.put("from", requester);


        String booktxt = gson.toJson(map);
        Log.d(TAG, booktxt);
        String type = Config.COMPLAINT;
        RetrofitClient.getInstance().getApi().sendPush(token, booktxt , type)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        try {
                            JSONObject obj = new JSONObject(response.body().string());
                            Log.d(TAG, response.body().string());
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                        registerComplaint();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
    }

    private void registerComplaint() {
        DatabaseReference comRefs = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_COMPLAINTS);
        String id = comRefs.push().getKey();
        String userstr = KeyValueDb.get(this, Config.USER,"");
        Gson gson = new Gson();
        User user = gson.fromJson(userstr, User.class);
        String titlext = title.getText().toString();
        String descxt = desc.getText().toString();
        Complaint complaint = new Complaint(id, titlext, descxt,"", user);

        comRefs.child(id).setValue(complaint);
        Toast.makeText(this, "complaint added", Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean valid() {
        boolean allokay = true;

        if(TextUtils.isEmpty(title.getText())){
            allokay = false;
            title.setError("Can't be empty");
        }

        if(TextUtils.isEmpty(desc.getText())){
            allokay = false;
            desc.setError("Can't be empty");
        }

        return  allokay;
    }
}
