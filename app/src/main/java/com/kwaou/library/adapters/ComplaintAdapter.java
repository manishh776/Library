package com.kwaou.library.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.kwaou.library.R;
import com.kwaou.library.models.Complaint;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ViewHolder> {

    Context context;
    ArrayList<Complaint> complaintArrayList;

    public ComplaintAdapter(Context context, ArrayList<Complaint> complaintArrayList){
        this.context = context;
        this.complaintArrayList = complaintArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.complaint_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Complaint complaint = complaintArrayList.get(position);
        holder.title.setText(complaint.getTitle());
        holder.desc.setText(complaint.getDesc());
        holder.reply.setText(complaint.getReply().isEmpty()?"Not Replied yet": "Reply:" + complaint.getReply());
    }

    @Override
    public int getItemCount() {
        return complaintArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, desc, reply;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            desc = itemView.findViewById(R.id.desc);
            reply = itemView.findViewById(R.id.reply);
        }
    }
}
