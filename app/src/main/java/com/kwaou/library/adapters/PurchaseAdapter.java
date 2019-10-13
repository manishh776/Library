package com.kwaou.library.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.kwaou.library.R;
import com.kwaou.library.activities.PurchaseActivity;
import com.kwaou.library.activities.RequestActivity;
import com.kwaou.library.helper.Config;
import com.kwaou.library.models.Admin;
import com.kwaou.library.models.BookDeal;
import com.kwaou.library.models.User;
import com.kwaou.library.retrofit.RetrofitClient;
import com.kwaou.library.sqlite.KeyValueDb;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.ViewHolder> {

    Context context;
    ArrayList<BookDeal> bookDealArrayList;
    String TAG = PurchaseAdapter.class.getSimpleName();

    public PurchaseAdapter(Context context, ArrayList<BookDeal> bookDealArrayList){
        this.context = context;
        this.bookDealArrayList = bookDealArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.purchase_item_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
            BookDeal bookDeal = bookDealArrayList.get(i);
            holder.bookname.setText(bookDeal.getOld().getBookArrayList().get(0).getTitle());
            holder.time.setText(bookDeal.getDate());
            Glide.with(context).load(bookDeal.getReceiver().getPicUrl()).diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(holder.bookImage);
    }

    @Override
    public int getItemCount() {
        return bookDealArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CircleImageView bookImage;
        TextView bookname, time, markReceivedBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            bookImage = itemView.findViewById(R.id.book_image);
            bookname = itemView.findViewById(R.id.bookname);
            time = itemView.findViewById(R.id.time);
            markReceivedBtn = itemView.findViewById(R.id.markReceivedBtn);
            markReceivedBtn.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            BookDeal bookDeal = bookDealArrayList.get(getAdapterPosition());
            if(view == markReceivedBtn){
                fetchAdminToken(bookDeal);
            }
        }
    }

    private void fetchAdminToken(final BookDeal bookDeal) {
        PurchaseActivity.progressDialog.show();
        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_ADMIN);
        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Admin admin = dataSnapshot.getValue(Admin.class);
                sendPushAndSaveChanges(bookDeal, admin.getToken());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendPushAndSaveChanges(final BookDeal bookDeal, String adminToken) {
        final Gson gson = new Gson();
        String userstr = KeyValueDb.get(context, Config.USER,"");
        final User requester = gson.fromJson(userstr, User.class);
        Map<String, Object> map = new HashMap<>();
        map.put("old", bookDeal.getOld());
        map.put("new", bookDeal.getNewbook());
        map.put("from", requester);
        String booktxt = gson.toJson(map);
        Log.d(TAG, booktxt);
        String type = Config.PURCHASED_BOOK_RECEIVED;
        RetrofitClient.getInstance().getApi().sendPush(adminToken, booktxt , type)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        updatePurchase(bookDeal);
                        PurchaseActivity.progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, t.getCause().toString());
                        PurchaseActivity.progressDialog.dismiss();
                    }
                });
    }

    private void updatePurchase(BookDeal bookDeal) {
        DatabaseReference bookDealsRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_BOOK_DEALS).child(bookDeal.getId());
        bookDealsRef.child("status").setValue(4);
    }
}
