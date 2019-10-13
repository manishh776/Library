package com.kwaou.library.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.kwaou.library.BooksInCategoryActivity;
import com.kwaou.library.R;
import com.kwaou.library.activities.AllMyBooksActivity;
import com.kwaou.library.activities.MainActivity;
import com.kwaou.library.models.Book;
import com.kwaou.library.models.Category;

import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Category> categoryArraylist;
    private String TAG = CategoryAdapter.class.getSimpleName();
    public  static int PICK_BOOK_REQUEST_CODE = 100;

    public CategoryAdapter(Context context, ArrayList<Category> categoryArrayList){
        this.context = context;
        this.categoryArraylist = categoryArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.category_item_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Category category = categoryArraylist.get(i);
        viewHolder.category_txt.setText(category.getName());
    }

    @Override
    public int getItemCount() {
        return categoryArraylist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView category_txt;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            category_txt = itemView.findViewById(R.id.category_txt);
            category_txt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    startBookActivity(pos);
                }
            });
        }
    }

    private void startBookActivity(int pos) {
        Category category = categoryArraylist.get(pos);
        Intent intent = new Intent(context, BooksInCategoryActivity.class);
        intent.putExtra("category_id", category.getId());
        context.startActivity(intent);
    }
}
