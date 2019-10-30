package com.kwaou.library.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.kwaou.library.R;
import com.kwaou.library.activities.AllMyBooksActivity;
import com.kwaou.library.activities.MainActivity;
import com.kwaou.library.models.Book;

import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BookInPackageAdapter extends RecyclerView.Adapter<BookInPackageAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Book> bookArrayList;
    private ArrayList<Book> bookArrayListUncleared, unchangedBooklist;
    private String TAG = BookInPackageAdapter.class.getSimpleName();

    public BookInPackageAdapter(Context context, ArrayList<Book> bookArrayList){
        this.context = context;
        this.bookArrayList = bookArrayList;
        unchangedBooklist = new ArrayList<>();
        unchangedBooklist.addAll(bookArrayList);
        bookArrayListUncleared =  new ArrayList<>();
        bookArrayListUncleared.addAll(unchangedBooklist);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.books_in_package_item_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            Book book = bookArrayList.get(i);
            viewHolder.name.setText(book.getTitle());
        Glide.with(context).load(book.getPicUrl()).diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(viewHolder.image);
    }

    @Override
    public int getItemCount() {
        return bookArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Book book = bookArrayList.get(getAdapterPosition());
                    if(!book.getUrl().isEmpty())
                    openPdfByUrl(book.getUrl());
                    else
                        Toast.makeText(context, "No pdf is available for this book", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void openPdfByUrl(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }
}
