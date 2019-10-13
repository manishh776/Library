package com.kwaou.library.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kwaou.library.R;
import com.kwaou.library.activities.MainActivity;
import com.kwaou.library.adapters.BookPackageAdapter;
import com.kwaou.library.adapters.CategoryAdapter;
import com.kwaou.library.helper.Config;
import com.kwaou.library.models.BookPackage;
import com.kwaou.library.models.Category;
import com.kwaou.library.sqlite.KeyValueDb;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CategoryFragment extends Fragment {
    RecyclerView recyclerViewCategories;
    CategoryAdapter adapter;
    public  static ProgressDialog progressDialog;
    View view;
    String TAG = CategoryFragment.class.getSimpleName();
    Context context;
    ArrayList<Category> categoryArrayList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_categories, container, false);
        initViews();
        return view;
    }

    private void initViews() {
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager( getActivity(), LinearLayoutManager.VERTICAL, false));
        context = getActivity();
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);
        fetchCategories();
    }

    private void fetchCategories() {
        progressDialog.show();
        DatabaseReference categoriesRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_CATEGORIES);
        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categoryArrayList = new ArrayList<>();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Category category = ds.getValue(Category.class);
                    if(category!=null)
                        categoryArrayList.add(category);
                }
                adapter = new CategoryAdapter(getActivity(), categoryArrayList);
                recyclerViewCategories.setAdapter(adapter);
                if(categoryArrayList.isEmpty()){
                    recyclerViewCategories.setVisibility(View.GONE);
                    view.findViewById(R.id.nocategories).setVisibility(View.VISIBLE);
                }else{
                    recyclerViewCategories.setVisibility(View.VISIBLE);
                    view.findViewById(R.id.nocategories).setVisibility(View.GONE);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




}
