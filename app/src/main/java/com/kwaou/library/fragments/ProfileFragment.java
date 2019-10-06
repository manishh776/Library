package com.kwaou.library.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.kwaou.library.ComplaintActivity;
import com.kwaou.library.activities.AddBookPackageActivity;
import com.kwaou.library.activities.AllMyBooksActivity;
import com.kwaou.library.R;
import com.kwaou.library.activities.RequestActivity;
import com.kwaou.library.helper.Config;
import com.kwaou.library.helper.ImagePicker;
import com.kwaou.library.models.BookDeal;
import com.kwaou.library.models.BookPackage;
import com.kwaou.library.models.User;
import com.kwaou.library.sqlite.KeyValueDb;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private static final int PICK_IMAGE = 111;
    CircleImageView circleImageView;
    ImageView edit;
    TextView pending, exchanged, mybooks;
    TextView addnewbooks, showmybooks, requests, complaints;
    ProgressDialog progressDialog;
    Uri filePathUri;
    String TAG = ProfileFragment.class.getSimpleName();
    TextView name;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {


        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);



        name = view.findViewById(R.id.name);
        circleImageView = view.findViewById(R.id.profile_image);
        edit = view.findViewById(R.id.edit);
        pending = view.findViewById(R.id.pending);
        exchanged = view.findViewById(R.id.exchanged);
        mybooks = view.findViewById(R.id.mybooks);
        complaints = view.findViewById(R.id.complaints);
        addnewbooks = view.findViewById(R.id.addnewbook);
        showmybooks = view.findViewById(R.id.showallmybooks);
        requests = view.findViewById(R.id.requests);

        edit.setOnClickListener(this);
        addnewbooks.setOnClickListener(this);
        showmybooks.setOnClickListener(this);
        requests.setOnClickListener(this);
        complaints.setOnClickListener(this);

        String userstr = KeyValueDb.get(getActivity(), Config.USER,"");
        Gson gson = new Gson();
        User user = gson.fromJson(userstr, User.class);
        name.setText(user.getName());
        Glide.with(this).load(user.getPicUrl()).placeholder(R.drawable.ic_user).diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(circleImageView);


        fetchData();

    }

    private void fetchData() {
        progressDialog.show();

        //pending is bookdeals in which user has requested to exchange
        DatabaseReference bookdeals = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_BOOK_DEALS);
        final String userid = KeyValueDb.get(getActivity(), Config.USERID, "");
        final int[] pendingarray = {0};
        final int[] exchangedarray = {0};
        bookdeals.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    BookDeal deal = ds.getValue(BookDeal.class);
                    if(deal!= null && deal.getStatus() == 1 && (deal.getReceiver().getId().equals(userid)
                    || deal.getLender().getId().equals(userid)
                    )){
                        pendingarray[0]++;
                    }
                    if(deal!= null && deal.getStatus() == 2 && (deal.getReceiver().getId().equals(userid) ||
                            deal.getLender().getId().equals(userid)
                            ) ){
                        exchangedarray[0]++;
                    }
                }
                pending.setText(pendingarray[0] + "");
                exchanged.setText(exchangedarray[0] + "");
                fetchBooksCount(userid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchBooksCount(final String userid) {

        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_BOOKPACKAGES);
        booksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final int[] bookscount = {0};
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    BookPackage bookPackage = ds.getValue(BookPackage.class);
                    if(bookPackage!=null && bookPackage.getUserid().equals(userid)){
                        bookscount[0]+=bookPackage.getBookArrayList().size();
                    }
                }
                mybooks.setText(bookscount[0] + "");
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        if(view == addnewbooks){
            startActivity(new Intent(getActivity(), AddBookPackageActivity.class));
        }
        else if(view == showmybooks){
            startActivity(new Intent(getActivity(), AllMyBooksActivity.class));
        }
        else if(view == requests){
            startActivity(new Intent(getActivity(), RequestActivity.class));
        }
        else if(view == edit){
            openGallery();
        }
        else if(view == complaints){
            startActivity(new Intent(getActivity(), ComplaintActivity.class));
        }
    }

    private void openGallery() {
        Intent intent = ImagePicker.getPickImageIntent(getActivity());
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch (requestCode) {
            case PICK_IMAGE: {
                if (resultCode == RESULT_OK) {
                    filePathUri = ImagePicker.getImageFromResult(getActivity(), resultCode, data);
                    Bitmap bitmapAvatar;
                    try {
                        bitmapAvatar = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePathUri);
                        circleImageView.setImageBitmap(bitmapAvatar);
                        uploadImage();
                    } catch (FileNotFoundException e) {
                        Drawable drawableAvatar = getResources().getDrawable(R.drawable.ic_image_black_24dp);
                        bitmapAvatar = ((BitmapDrawable) drawableAvatar).getBitmap();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    private void uploadImage() {
        Log.d(TAG, filePathUri.getPath());
        progressDialog.show();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final StorageReference booksRef = storageReference.child("books/images/" + System.currentTimeMillis());
        booksRef.putFile(filePathUri)
                .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return booksRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    String imageUrl = String.valueOf(task.getResult());
                    String userid = KeyValueDb.get(getActivity(), Config.USERID,"");
                    FirebaseDatabase.getInstance().
                            getReference(Config.FIREBASE_USERS).child(userid).child("picUrl").setValue(imageUrl);
                    KeyValueDb.set(getActivity(), Config.USER_PIC_URL, imageUrl,1);
                    updateUserObject(imageUrl);
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUserObject(String imageUrl) {
        String str = KeyValueDb.get(getActivity(), Config.USER,"");
        Gson gson = new Gson();
        User user = gson.fromJson(str, User.class);
        user.setPicUrl(imageUrl);
        str = gson.toJson(user);
        KeyValueDb.set(getActivity(), Config.USER, str,1);
    }

}
