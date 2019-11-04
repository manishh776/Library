package com.kwaou.library.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.kwaou.library.BooksInCategoryActivity;
import com.kwaou.library.BooksInPackageActivity;
import com.kwaou.library.activities.AllMyBooksActivity;
import com.kwaou.library.R;
import com.kwaou.library.activities.MainActivity;
import com.kwaou.library.activities.RequestActivity;
import com.kwaou.library.activities.SplashActivity;
import com.kwaou.library.helper.Config;
import com.kwaou.library.models.Admin;
import com.kwaou.library.models.Book;
import com.kwaou.library.models.BookDeal;
import com.kwaou.library.models.BookPackage;
import com.kwaou.library.models.User;
import com.kwaou.library.retrofit.RetrofitClient;
import com.kwaou.library.sqlite.KeyValueDb;
import com.manojbhadane.PaymentCardView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BookPackageAdapter extends RecyclerView.Adapter<BookPackageAdapter.ViewHolder> {

    private Context context;
    private ArrayList<BookPackage> bookPackageArrayList;
    private ArrayList<BookPackage> bookArrayListUncleared, unchangedBooklist;
    private String TAG = BookPackageAdapter.class.getSimpleName();
    public  static int PICK_BOOK_REQUEST_CODE = 100;
    private ProgressDialog progressDialog;

    public BookPackageAdapter(Context context, ArrayList<BookPackage> bookPackages){
        this.context = context;
        this.bookPackageArrayList = bookPackages;
        unchangedBooklist = new ArrayList<>();
        unchangedBooklist.addAll(bookPackageArrayList);
        bookArrayListUncleared =  new ArrayList<>();
        bookArrayListUncleared.addAll(unchangedBooklist);

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.book_package_item_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            BookPackage book = bookPackageArrayList.get(i);
            if(book.getPrice() > 0){
                viewHolder.linearLayoutPrice.setVisibility(View.VISIBLE);
                viewHolder.price.setText(book.getPrice()+"");
            }
            if(!book.getBookArrayList().isEmpty()) {
                viewHolder.noofbooks.setText(book.getBookArrayList().size() + "");
                Glide.with(context).load(book.getBookArrayList().get(0).getPicUrl()).diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(viewHolder.image);
            }
            viewHolder.packageName.setText(book.getPackageName());
    }

    @Override
    public int getItemCount() {
        return bookPackageArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image, pop_up_menu;
        TextView price, noofbooks, packageName;
        LinearLayout linearLayoutPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.image);
            price = itemView.findViewById(R.id.price);
            noofbooks = itemView.findViewById(R.id.noofbooks);
            pop_up_menu = itemView.findViewById(R.id.pop_up_menu);
            linearLayoutPrice = itemView.findViewById(R.id.priceLinear);
            packageName = itemView.findViewById(R.id.packageName);

            pop_up_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    showBookPackageAlertDialog(pos);
                }
            });
        }
    }

    private void showBookPackageAlertDialog(final int pos) {
        BookPackage bookPackage = bookPackageArrayList.get(pos);
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.book_package_alert_dialog);

        final TextView ownerPhone, ownerAddress;
        TextView packageDesc = dialog.findViewById(R.id.packageDesc);
        ownerPhone = dialog.findViewById(R.id.phone);
        ownerAddress = dialog.findViewById(R.id.address);
        final Button buttonShowBooks = dialog.findViewById(R.id.showBook);
        final Button buttonExchange = dialog.findViewById(R.id.exchangeBook);
        final Button buttonBuy = dialog.findViewById(R.id.buyBook);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_USERS);
        userRef.child(bookPackage.getUserid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user!=null){
                    ownerPhone.setText(user.getPhone());
                    ownerAddress.setText(user.getAddress());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        packageDesc.setText(bookPackage.getBookArrayList().get(0).getDesc());
        if(bookPackage.getPrice() == 0)
            buttonBuy.setVisibility(View.GONE);
        else
            buttonExchange.setVisibility(View.GONE);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view == buttonShowBooks){
                    displayBookList(pos);
                } else if (view == buttonBuy || view == buttonExchange) {
                    handleExchange(pos);
                }
                dialog.dismiss();
            }
        };

        buttonShowBooks.setOnClickListener(listener);
        buttonExchange.setOnClickListener(listener);
        buttonBuy.setOnClickListener(listener);

        dialog.show();
    }


    private void handleExchange(int position) {
        final int login_state = Integer.parseInt(KeyValueDb.get(context, Config.LOGIN_STATE, "0"));
        if(login_state == 1)
            initiateExchange(position);
        else
            startRegisterActivity();
    }

    private void startRegisterActivity() {
        context.startActivity(new Intent(context, SplashActivity.class));
    }

    private void displayBookList(int position) {
        BookPackage bookPackage = bookPackageArrayList.get(position);
        Intent intent = new Intent(context, BooksInPackageActivity.class);
        intent.putExtra("book_package", bookPackage);
        context.startActivity(intent);
    }

    public void initiateExchange(int position){
        BookPackage book = bookPackageArrayList.get(position);
        Log.d(TAG, "Clicked");
        if(context instanceof BooksInCategoryActivity) {
            if (book.getPrice() == 0)
                startPickABookActivity(book);
            else{
                showPaymentDialog(book);
            }
            Log.d(TAG, "Clicked MainActivity");
        }
        else if(context instanceof AllMyBooksActivity){
            Intent data = new Intent();
            data.putExtra("old", book);
            data.putExtra("new", AllMyBooksActivity.newbook);
            ((Activity)context).setResult(Activity.RESULT_OK, data);
            ((Activity)context).finish();
            Log.d(TAG, "Clicked AllMyActivity");
        }
    }

    private void showPaymentDialog(final BookPackage book) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.payment_dialog_layout);
        PaymentCardView paymentCardView = dialog.findViewById(R.id.paycardview);
        paymentCardView.setOnPaymentCardEventListener(new PaymentCardView.OnPaymentCardEventListener() {
            @Override
            public void onCardDetailsSubmit(String month, String year, String cardNumber, String cvv) {
                dialog.dismiss();
                notifyOwner(book);
                Toast.makeText(context, "Paid", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(context, error , Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelClick() {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void notifyOwner(final BookPackage book) {
        progressDialog.show();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_USERS);
        userRef.child(book.getUserid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user!=null){
                    markPackageSold(book, user);
                    sendPushToOwner(book, user);
                }else{
                    Log.e(TAG,"user is null");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendPushToOwner(final BookPackage book, User user) {
        progressDialog.show();
        String type = Config.SALE;
        Gson gson = new Gson();
        String userstr = KeyValueDb.get(context, Config.USER,"");
        final User requester = gson.fromJson(userstr, User.class);
        Map<String, Object> map = new HashMap<>();
        map.put("old", book);
        map.put("from", requester);
        String booktxt = gson.toJson(map);
        RetrofitClient.getInstance().getApi().sendPush(user.getToken(), booktxt , type)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        try {
                            JSONObject obj = new JSONObject(response.body().string());
                            Log.d(TAG, response.body().string());
                            notifyAdmin(book);
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
    }

    private void notifyAdmin(final BookPackage book) {
        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_ADMIN);
        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Admin admin = dataSnapshot.getValue(Admin.class);
                if(admin!=null){
                    sendPush(book, admin.getToken());
                }else{
                    Log.e(TAG,"admin is null");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendPush(BookPackage book, String token) {
        final Gson gson = new Gson();
        String userstr = KeyValueDb.get(context, Config.USER,"");
        final User requester = gson.fromJson(userstr, User.class);
        Map<String, Object> map = new HashMap<>();
        map.put("old", book);
        map.put("from", requester);
        String booktxt = gson.toJson(map);
        Log.d(TAG, booktxt);
        String type = Config.SALE;
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
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                    }
                });
    }

    private void markPackageSold(BookPackage book, User user) {
        DatabaseReference books = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_BOOKPACKAGES);
        books.child(book.getId()).child("status").setValue(2);

        String userstr = KeyValueDb.get(context, Config.USER,"");
        Gson gson = new Gson();
        User receiver = gson.fromJson(userstr, User.class);
        DatabaseReference bookDealsRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_BOOK_DEALS);
        String id = bookDealsRef.push().getKey();
        BookDeal deal = new BookDeal(id, book,null, user,  receiver, true,3, getCurrentDate());
        bookDealsRef.child(id).setValue(deal);
    }

    private void startPickABookActivity(BookPackage book) {
        Intent intent = new Intent(context, AllMyBooksActivity.class);
        intent.putExtra("new", book);
        ((Activity)context).startActivityForResult(intent, PICK_BOOK_REQUEST_CODE);
    }


    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        bookPackageArrayList.clear();

        if (charText.length() == 0) {
            bookPackageArrayList.addAll(bookArrayListUncleared);
        }
        else {

            for (BookPackage  book : bookArrayListUncleared) {

                if ((book.getBookArrayList().get(0).getTitle() +book.getBookArrayList().get(0).getDesc()).toLowerCase(Locale.getDefault()).contains(charText)) {

                    bookPackageArrayList.add(book);
                }
            }
        }
        notifyDataSetChanged();
    }

    public String getCurrentDate(){
        Date date = new Date();
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        return  dateFormat.format(date);
    }

}
