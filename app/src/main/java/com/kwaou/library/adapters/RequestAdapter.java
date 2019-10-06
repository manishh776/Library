package com.kwaou.library.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.kwaou.library.R;
import com.kwaou.library.activities.RequestActivity;
import com.kwaou.library.helper.Config;
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

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    Context context;
    ArrayList<BookDeal> bookDealArrayList;
    String TAG = RequestAdapter.class.getSimpleName();

    public RequestAdapter(Context context, ArrayList<BookDeal> bookDealArrayList){
        this.context = context;
        this.bookDealArrayList = bookDealArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.request_item_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
            BookDeal bookDeal = bookDealArrayList.get(i);
            holder.name.setText(bookDeal.getReceiver().getName());
            holder.time.setText(bookDeal.getDate());
            holder.exchange.setText("I want to replace set of my " + bookDeal.getOld().getBookArrayList().size() + " books "+ " with your set of " + bookDeal.getNewbook().getBookArrayList().size() + " books.");

            if(bookDeal.getStatus() == 2){
                holder.rejectBtn.setVisibility(View.GONE);
                holder.acceptBtn.setText("Accepted");
                holder.acceptBtn.setEnabled(false);
            }
            else if(bookDeal.getStatus() == 0){
                holder.acceptBtn.setVisibility(View.GONE);
                holder.rejectBtn.setText("Rejected");
                holder.rejectBtn.setEnabled(false);
            }

        Glide.with(context).load(bookDeal.getReceiver().getPicUrl()).diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(holder.circleImageView);
    }

    @Override
    public int getItemCount() {
        return bookDealArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CircleImageView circleImageView;
        TextView name, time, exchange, acceptBtn, rejectBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            circleImageView = itemView.findViewById(R.id.profile_image);
            name = itemView.findViewById(R.id.name);
            time = itemView.findViewById(R.id.time);
            exchange = itemView.findViewById(R.id.exchange);
            acceptBtn = itemView.findViewById(R.id.acceptBtn);
            rejectBtn = itemView.findViewById(R.id.rejectBtn);

            acceptBtn.setOnClickListener(this);
            rejectBtn.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            BookDeal bookDeal = bookDealArrayList.get(getAdapterPosition());
            if(view == acceptBtn){
                sendPushAndSaveChanges(bookDeal, true);
            }
            else if(view == rejectBtn){
                sendPushAndSaveChanges(bookDeal, false);
            }
        }
    }

    private void sendPushAndSaveChanges(final BookDeal bookDeal, final boolean accepted) {
        RequestActivity.progressDialog.show();

        final Gson gson = new Gson();
        String userstr = KeyValueDb.get(context, Config.USER,"");
        final User requester = gson.fromJson(userstr, User.class);
        Map<String, Object> map = new HashMap<>();
        map.put("old", bookDeal.getOld());
        map.put("new", bookDeal.getNewbook());
        map.put("from", requester);

        String booktxt = gson.toJson(map);
        Log.d(TAG, booktxt);
        String type = accepted?Config.EXCHANGE_REQUEST_ACCEPT:Config.EXCHANGAE_REQUEST_REJECT;
        RetrofitClient.getInstance().getApi().sendPush(bookDeal.getReceiver().getToken(), booktxt , type)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        updateExchange(bookDeal, accepted);
                        if(accepted){
                            markBooksExchanged(bookDeal);
                            Toast.makeText(context, "Exchange complete", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(context, "Exchange rejected", Toast.LENGTH_SHORT).show();
                        }


                        try {
                            JSONObject obj = new JSONObject(response.body().string());
                            Log.d(TAG, response.body().string());
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                        RequestActivity.progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, t.getCause().toString());
                        RequestActivity.progressDialog.dismiss();
                    }
                });
    }

    private void markBooksExchanged(BookDeal bookDeal) {
        DatabaseReference books = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_BOOKPACKAGES);
        books.child(bookDeal.getOld().getId()).child("status").setValue(1);
        books.child(bookDeal.getNewbook().getId()).child("status").setValue(1);
    }

    private void updateExchange(BookDeal bookDeal, boolean accepted) {
        DatabaseReference bookDealsRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_BOOK_DEALS).child(bookDeal.getId());
        int status = accepted?2:0;
        bookDealsRef.child("status").setValue(status);

    }
}
