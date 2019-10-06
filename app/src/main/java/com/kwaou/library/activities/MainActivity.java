package com.kwaou.library.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.kwaou.library.R;
import com.kwaou.library.adapters.BookAdapter;
import com.kwaou.library.fragments.BooksFragment;
import com.kwaou.library.fragments.NotificationFragment;
import com.kwaou.library.fragments.ProfileFragment;
import com.kwaou.library.helper.Config;
import com.kwaou.library.helper.NonSwipingViewPager;
import com.kwaou.library.models.BookDeal;
import com.kwaou.library.models.BookPackage;
import com.kwaou.library.models.User;
import com.kwaou.library.retrofit.RetrofitClient;
import com.kwaou.library.sqlite.KeyValueDb;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView textViewLogout, textViewbooks, textViewNotifications, textViewprofile;
    private ImageView imageViewAdd;
    private int lastSelected = -1;
    public static NonSwipingViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        textViewLogout = findViewById(R.id.logout);
        textViewbooks = findViewById(R.id.books);
        textViewNotifications = findViewById(R.id.notifications);
        textViewprofile = findViewById(R.id.profile);
        imageViewAdd = findViewById(R.id.add_book);
        viewPager = findViewById(R.id.viewpager);

        textViewLogout.setOnClickListener(this);
        textViewbooks.setOnClickListener(this);
        textViewNotifications.setOnClickListener(this);
        textViewprofile.setOnClickListener(this);
        imageViewAdd.setOnClickListener(this);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        viewPager.setCurrentItem(0);
        textViewbooks.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_books_stack_of_three_orange),null,null);
        updateLastSelected();
        lastSelected = 0;



    }

    @Override
    public void onClick(View view) {
        if(view == textViewbooks){
            viewPager.setCurrentItem(0);
            textViewbooks.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_books_stack_of_three_orange),null,null);
            updateLastSelected();
            lastSelected = 0;
        }
        else if(view == imageViewAdd){
            startActivity(new Intent(this, AddBookPackageActivity.class));
        }
        else if(view == textViewNotifications){
            viewPager.setCurrentItem(1);
            textViewNotifications.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_icon___notify_orange),null,null);
            updateLastSelected();
            lastSelected = 1;
        }
        else if(view == textViewprofile){
            viewPager.setCurrentItem(2);
            textViewprofile.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_user_orange),null,null);
            updateLastSelected();
            lastSelected = 2;
        }
        else if(view == textViewLogout){
            showAlertDialogForLogout();
        }
    }

    private void showAlertDialogForLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                logout();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private void logout() {

        KeyValueDb.set(this, Config.LOGIN_STATE, "0",1);

        Intent intent = new Intent(this, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter{


        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;

            switch (i){

                case 0:
                    Log.d(TAG,"" + i);
                    fragment = new BooksFragment();
                    break;

                case 1:
                    Log.d(TAG,"" + i);
                    fragment = new NotificationFragment();
                    break;
                case 2:
                    Log.d(TAG,"" + i);
                    fragment = new ProfileFragment();
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    private void updateLastSelected() {
        Log.d(TAG,"last" + lastSelected);
        switch (lastSelected){
            case 0:
                textViewbooks.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_books_stack_of_three),null,null);
                break;
            case 1:
                textViewNotifications.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_icon___notify),null,null);
                break;

            case 2:
                textViewprofile.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_user),null,null);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult");
        if(requestCode == BookAdapter.PICK_BOOK_REQUEST_CODE ) {
            if (resultCode == RESULT_OK) {
                BookPackage old = (BookPackage) data.getSerializableExtra("old");
                BookPackage newbook = (BookPackage) data.getSerializableExtra("new");

                Log.d(TAG, "old" + old.getBookArrayList().get(0).getTitle());
                Log.d(TAG, "new" + newbook.getBookArrayList().get(0).getTitle());
                showAlertDialogForExchange(old, newbook);
            } else {
                Log.e(TAG, "RESLT NOT Okay");
            }
        }else{
            Log.e(TAG,"result code wrong");
        }
    }

    private void showAlertDialogForExchange(final BookPackage old, final BookPackage newbook) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Send Exchange Request");
        builder.setMessage("Are you sure you want to exchange your set  of " + old.getBookArrayList().size() + " books with this set of "+

        newbook.getBookArrayList().size() + " books?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                sendExchangeRequest(old, newbook);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private void sendExchangeRequest(final BookPackage old, final BookPackage newbook) {
        BooksFragment.progressDialog.show();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_USERS).child(newbook.getUserid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user!=null){
                    sendPush(user, old, newbook);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendPush(final User user, final BookPackage old, final BookPackage newbook) {
        final Gson gson = new Gson();
        String userstr = KeyValueDb.get(MainActivity.this, Config.USER,"");
        final User requester = gson.fromJson(userstr, User.class);
        Map<String, Object> map = new HashMap<>();
        map.put("old", old);
        map.put("new", newbook);
        map.put("from", requester);

        String booktxt = gson.toJson(map);
        Log.d(TAG, booktxt);
        RetrofitClient.getInstance().getApi().sendPush(user.getToken(), booktxt , Config.EXCHANGE_REQUEST)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        BooksFragment.progressDialog.dismiss();
                        saveExchange(old, newbook, requester, user );
                        Toast.makeText(MainActivity.this, "Request sent", Toast.LENGTH_SHORT).show();
                        try {
                            JSONObject obj = new JSONObject(response.body().string());
                            Log.d(TAG, response.body().string());
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        BooksFragment.progressDialog.dismiss();
                        Log.d(TAG, t.getMessage());
                    }
                });
    }

    private void saveExchange(BookPackage old, BookPackage newbook, User requester, User lender) {
        DatabaseReference dealsRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_BOOK_DEALS);
        String dealid = dealsRef.push().getKey();

        BookDeal bookDeal = new BookDeal(dealid, old, newbook, lender, requester, false,1, getCurrentDate());
        dealsRef.child(dealid).setValue(bookDeal);

    }
    public String getCurrentDate(){
        Date date = new Date();
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(MainActivity.this);
        return  dateFormat.format(date);
    }
}
