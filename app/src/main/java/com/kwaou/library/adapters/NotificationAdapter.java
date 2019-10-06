package com.kwaou.library.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.kwaou.library.R;
import com.kwaou.library.models.Notification;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    Context context;
    ArrayList<Notification> notificationArrayList;

    public NotificationAdapter(Context context, ArrayList<Notification> notifications){
        this.context = context;
        notificationArrayList = notifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_item_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
            Notification n = notificationArrayList.get(i);
            holder.name.setText(n.getFrom().getName());
            holder.time.setText(n.getTime());
            holder.title.setText(n.getTitle());
            holder.message.setText(n.getMessage());

        Glide.with(context).load(n.getFrom().getPicUrl()).placeholder(R.drawable.ic_user).diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(holder.circleImageView);
    }

    @Override
    public int getItemCount() {
        return notificationArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView circleImageView;
        TextView name, time, title, message;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            circleImageView = itemView.findViewById(R.id.profile_image);
            name = itemView.findViewById(R.id.name);
            time = itemView.findViewById(R.id.time);
            title = itemView.findViewById(R.id.title);
            message = itemView.findViewById(R.id.message);
        }
    }
}
