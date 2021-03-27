package com.example.quizzeradmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.UUID;


public class SetsActivity extends AppCompatActivity {
    GridView gridView;
    private Dialog loadingDialog;
    private GridAdapter gridAdapter;
    private String categoryName;
    private DatabaseReference myRef;
    private List<String> sets;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sets);
        // this is intent is received from CategoryAdapter
        Intent categoryIntent = getIntent();
        categoryName= categoryIntent.getStringExtra("title");
        // This tells the number of set the particular category will have.
        int set = categoryIntent.getIntExtra("position",0);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(categoryName);
        myRef = FirebaseDatabase.getInstance().getReference();

        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);

        gridView = findViewById(R.id.grid_view);
        sets = CategoryActivity.list.get(set).getSets();
        gridAdapter = new GridAdapter(sets, categoryName, new GridAdapter.GridListener() {
            @Override
            public void addSet() {
                loadingDialog.show();
                final String id = UUID.randomUUID().toString();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                database.getReference().child("Categories").child(getIntent().getStringExtra("key")).child("sets").child(id).
                        setValue("SET ID").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            sets.add(id);
                            gridAdapter.notifyDataSetChanged();
                        }else{
                            Toast.makeText(SetsActivity.this,"Something went wrong..!",Toast.LENGTH_LONG).show();
                        }
                        loadingDialog.dismiss();
                    }
                });
            }

            @Override
            public void onLongClick(final String setId,int position) {
                new AlertDialog.Builder(SetsActivity.this,R.style.Theme_AppCompat_DayNight_Dialog)
                        .setTitle("Delete SET "+position )
                        .setMessage("Are you sure, You want to delete this Set")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadingDialog.show();
                                myRef.child("SETS").child(setId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            myRef.child("Categories").child(CategoryActivity.list.get(set).getKey())
                                                    .child("sets").child(setId)
                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        sets.remove(setId);
                                                        gridAdapter.notifyDataSetChanged();
                                                    }else{
                                                        Toast.makeText(SetsActivity.this, "Something went wrong..!", Toast.LENGTH_SHORT).show();
                                                    }
                                                    loadingDialog.dismiss();
                                                }
                                            });

                                        } else{
                                            Toast.makeText(SetsActivity.this, "Something went wrong..!", Toast.LENGTH_SHORT).show();
                                            loadingDialog.dismiss();
                                        }

                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
        gridView.setAdapter(gridAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}