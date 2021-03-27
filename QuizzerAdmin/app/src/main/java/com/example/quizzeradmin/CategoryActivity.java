package com.example.quizzeradmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class CategoryActivity extends AppCompatActivity {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mCategoryReference;
    private RecyclerView recyclerView;
    public static List<CategoryModel> list;
    private CategoryAdapter categoryAdapter;
    private Dialog loadingDialog,categoryDialog;
    private CircleImageView addImage;
    private EditText etCategoryName;
    private Button addBtn;
    private Uri image;
    private String downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Categories");

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mCategoryReference = mFirebaseDatabase.getReference();


        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);

        setCategoryDialog();

        recyclerView = findViewById(R.id.rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        list = new ArrayList<>();

        categoryAdapter = new CategoryAdapter(list, new CategoryAdapter.deleteListener() {
            @Override
            public void onDelete(final String key, final int position) {
                new AlertDialog.Builder(CategoryActivity.this,R.style.Theme_AppCompat_DayNight_Dialog)
                        .setTitle("Delete Category")
                        .setMessage("Are you sure, You want to delete this category?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadingDialog.show();
                                mCategoryReference.child("Categories").child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            for(String setIds : list.get(position).getSets()){
                                                mCategoryReference.child("SETS").child(setIds).removeValue();
                                            }
                                            list.remove(position);
                                            categoryAdapter.notifyDataSetChanged();
                                            loadingDialog.dismiss();
                                        }else{
                                            Toast.makeText(CategoryActivity.this,"Failed to delete" ,Toast.LENGTH_LONG).show();
                                        }

                                    }
                                });
                                loadingDialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel",null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

        recyclerView.setAdapter(categoryAdapter);
        loadingDialog.show();
        mCategoryReference.child("Categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    List<String> sets = new ArrayList<>();
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.child("sets").getChildren()){
                        sets.add(dataSnapshot1.getKey());
                    }
                    list.add(new CategoryModel(dataSnapshot.child("url").getValue().toString(),
                            dataSnapshot.child("name").getValue().toString(),
                            dataSnapshot.getKey(),sets));
                }
                categoryAdapter.notifyDataSetChanged();
                loadingDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CategoryActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addMenu){
            categoryDialog.show();
        }else if (item.getItemId() == R.id.logoutMenu){
            FirebaseAuth.getInstance().signOut();
            Intent logoutIntent = new Intent(CategoryActivity.this,MainActivity.class);
            startActivity(logoutIntent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setCategoryDialog(){
        categoryDialog = new Dialog(this);
        categoryDialog.setContentView(R.layout.add_category_dialog);
        categoryDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_box));
        categoryDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        categoryDialog.setCancelable(true);

        addImage = categoryDialog.findViewById(R.id.image);
        etCategoryName = categoryDialog.findViewById(R.id.categoryName);
        addBtn = categoryDialog.findViewById(R.id.addBtn);

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent,101);
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etCategoryName.getText().toString().trim().isEmpty()){
                    etCategoryName.setError("Required");
                    return;
                }
                for(CategoryModel model : list){
                    if (etCategoryName.getText().toString().equals(model.getName())){
                        etCategoryName.setError("Category name already exists");
                        return;
                    }
                }

                if(image == null){
                    Toast.makeText(CategoryActivity.this,"Please select a image", Toast.LENGTH_SHORT).show();
                    return;
                }
                categoryDialog.dismiss();
                // upload data
                uploadData();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101){
            if (resultCode == RESULT_OK){
                image = data.getData();
                addImage.setImageURI(image);
            }
        }
    }

    private void uploadData(){
        loadingDialog.show();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final StorageReference imageReference = storageReference.child("categories").child(image.getLastPathSegment());
        UploadTask uploadTask = imageReference.putFile(image);

        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return imageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    downloadUrl = task.getResult().toString();
                    uploadCategoryName();
                }else{
                    loadingDialog.dismiss();
                    Toast.makeText(CategoryActivity.this,"Something went wrong..!",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void uploadCategoryName(){
        Map<String, Object> map = new HashMap<>();
        map.put("name",etCategoryName.getText().toString());
        map.put("sets",0);
        map.put("url",downloadUrl);
        final String id = UUID.randomUUID().toString();
        mCategoryReference.child("Categories").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                        list.add(new CategoryModel(downloadUrl,etCategoryName.getText().toString(),id,new ArrayList<String>()));
                        categoryAdapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(CategoryActivity.this,"Something went wrong",Toast.LENGTH_LONG).show();
                }
                loadingDialog.dismiss();
            }
        });
    }
}