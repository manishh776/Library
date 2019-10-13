package com.kwaou.library;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.kwaou.library.activities.MainActivity;
import com.kwaou.library.adapters.BookAdapter;
import com.kwaou.library.adapters.BookPackageAdapter;
import com.kwaou.library.fragments.BooksFragment;
import com.kwaou.library.helper.Config;
import com.kwaou.library.models.BookDeal;
import com.kwaou.library.models.BookPackage;
import com.kwaou.library.models.User;
import com.kwaou.library.retrofit.RetrofitClient;
import com.kwaou.library.sqlite.KeyValueDb;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BooksInCategoryActivity extends AppCompatActivity {

    private static final String TAG = BooksInCategoryActivity.class.getSimpleName();
    ProgressDialog progressDialog;
    ArrayList<BookPackage> bookPackageArrayList;
    BookPackageAdapter adapter;
    RecyclerView recyclerViewBooks;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books_in_category);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);
        id = getIntent().getStringExtra("category_id");
        recyclerViewBooks = findViewById(R.id.recyclerViewBooks);
        recyclerViewBooks.setLayoutManager(new GridLayoutManager(this, 3));
        fetchBooks();
    }

    private void fetchBooks() {
        final String user_id = KeyValueDb.get(this, Config.USERID, "");
        Log.d(TAG, "fetchBooks" + user_id);
        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_BOOKPACKAGES);
        booksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bookPackageArrayList = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    BookPackage bookPackage = ds.getValue(BookPackage.class);
                    if (bookPackage != null && bookPackage.getCategory().getId().equals(id)) {
                        Log.d(TAG, "onDataChange" + bookPackage.getUserid());
                        //if it available
                        if (bookPackage.getStatus() == 0) {
                            if (!user_id.isEmpty()) {
                                if (!bookPackage.getUserid().equals(user_id))
                                    bookPackageArrayList.add(bookPackage);
                            } else {
                                bookPackageArrayList.add(bookPackage);
                            }
                        }
                        Log.d(TAG, bookPackage.getId());
                    }
                }
                adapter = new BookPackageAdapter(BooksInCategoryActivity.this, bookPackageArrayList);
                recyclerViewBooks.setAdapter(adapter);
                if (bookPackageArrayList.isEmpty()) {
                    recyclerViewBooks.setVisibility(View.GONE);
                    findViewById(R.id.nobooks).setVisibility(View.VISIBLE);
                } else {
                    recyclerViewBooks.setVisibility(View.VISIBLE);
                    findViewById(R.id.nobooks).setVisibility(View.GONE);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult");
        if (requestCode == BookAdapter.PICK_BOOK_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                BookPackage old = (BookPackage) data.getSerializableExtra("old");
                BookPackage newbook = (BookPackage) data.getSerializableExtra("new");

                Log.d(TAG, "old" + old.getBookArrayList().get(0).getTitle());
                Log.d(TAG, "new" + newbook.getBookArrayList().get(0).getTitle());
                showAlertDialogForExchange(old, newbook);
            } else {
                Log.e(TAG, "RESLT NOT Okay");
            }
        } else {
            Log.e(TAG, "result code wrong");
        }
    }

    @Override
    protected void onResume() {
        fetchBooks();
        super.onResume();
    }

    private void showAlertDialogForExchange(final BookPackage old, final BookPackage newbook) {
        AlertDialog.Builder builder = new AlertDialog.Builder(BooksInCategoryActivity.this);
        builder.setTitle("Send Exchange Request");
        builder.setMessage("Are you sure you want to exchange your set  of " + old.getBookArrayList().size() + " books with this set of " +

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
        progressDialog.show();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_USERS).child(newbook.getUserid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
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
        String userstr = KeyValueDb.get(this, Config.USER, "");
        final User requester = gson.fromJson(userstr, User.class);
        Map<String, Object> map = new HashMap<>();
        map.put("old", old);
        map.put("new", newbook);
        map.put("from", requester);

        String booktxt = gson.toJson(map);
        Log.d(TAG, booktxt);
        RetrofitClient.getInstance().getApi().sendPush(user.getToken(), booktxt, Config.EXCHANGE_REQUEST)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        progressDialog.dismiss();
                        saveExchange(old, newbook, requester, user);
                        Toast.makeText(BooksInCategoryActivity.this, "Request sent", Toast.LENGTH_SHORT).show();
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

        BookDeal bookDeal = new BookDeal(dealid, old, newbook, lender, requester, false, 1, getCurrentDate());
        dealsRef.child(dealid).setValue(bookDeal);

    }

    public String getCurrentDate() {
        Date date = new Date();
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);
        return dateFormat.format(date);
    }


}
