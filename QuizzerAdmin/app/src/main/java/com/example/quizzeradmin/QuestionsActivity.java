 package com.example.quizzeradmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

 public class QuestionsActivity extends AppCompatActivity {
    private Button addBtn,excelBtn;
    private RecyclerView recyclerView;
    private QuestionsAdapter adapter;
    private Dialog loadingDialog;
    public static List<QuestionModel> list;
    private DatabaseReference myRef;
    private String setId;
    private TextView loadingText;
    private String categoryName;
    public static final int CELL_COUNT = 6;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        categoryName = getIntent().getStringExtra("category");
        setId = getIntent().getStringExtra("setId");
        getSupportActionBar().setTitle(categoryName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addBtn = findViewById(R.id.addBtn);
        excelBtn = findViewById(R.id.excelBtn);
        recyclerView = findViewById(R.id.recycler_view);
        myRef = FirebaseDatabase.getInstance().getReference();

        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);
        loadingText = loadingDialog.findViewById(R.id.textView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        list = new ArrayList<>();
        recyclerView.setLayoutManager(layoutManager);
        adapter = new QuestionsAdapter(list, categoryName, new QuestionsAdapter.DeleteListener() {
            @Override
            public void onLongClick(final int position, final String id) {

                new AlertDialog.Builder(QuestionsActivity.this,R.style.Theme_AppCompat_DayNight_Dialog)
                        .setTitle("Delete Question")
                        .setMessage("Are you sure, You want to delete this question")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadingDialog.show();
                                myRef.child("SETS").child(setId).child(id)
                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            list.remove(position);
                                            adapter.notifyItemChanged(position);
                                            Log.d("TAG", "onComplete: Task is successful");
                                        }else{
                                            Toast.makeText(QuestionsActivity.this, "Failed to delete.", Toast.LENGTH_SHORT).show();
                                        }
                                        loadingDialog.dismiss();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });
        recyclerView.setAdapter(adapter);
        getData(categoryName,setId);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addQuestions = new Intent(QuestionsActivity.this,AddQuestionActivity.class);
                addQuestions.putExtra("categoryName",categoryName);
                addQuestions.putExtra("setId",setId);
                startActivity(addQuestions);
            }
        });

        excelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ActivityCompat.checkSelfPermission(QuestionsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED){
                    selectFile();
                }else{
                    ActivityCompat.requestPermissions(QuestionsActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},101);
                }
            }
        });

    }

     @Override
     public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults);
         if(requestCode == 101){
             if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectFile();
             }else{
                 Toast.makeText(QuestionsActivity.this,"Please Grant Permission",Toast.LENGTH_LONG).show();
             }
         }
     }

     private void selectFile(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent,"Select File"),102);
     }

     @Override
     protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (requestCode == 102){
             if(resultCode == RESULT_OK){
                 String filePath = data.getData().getPath();
                 if(filePath.endsWith(".xlsx")){
                     try {
                         readFile(data.getData());
                     } catch (InvocationTargetException e) {
                         e.printStackTrace();
                     }
                 }else{
                     Toast.makeText(QuestionsActivity.this, "Please choose an Excel File!", Toast.LENGTH_SHORT).show();
                 }
             }
         }
     }

     private void readFile(final Uri fileUri) throws InvocationTargetException {
        loadingText.setText("Scanning Question");
        loadingDialog.show();
         AsyncTask.execute(new Runnable() {
             @Override
             public void run() {
                 HashMap<String , Object > parentMap = new HashMap<>();
                 List<QuestionModel> tempList = new ArrayList<>();
                 try {
                     InputStream inputStream = getContentResolver().openInputStream(fileUri);
                     XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                     XSSFSheet sheet = workbook.getSheetAt(0);
                     FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

                     int rowsCount = sheet.getPhysicalNumberOfRows();
                     if(rowsCount > 0){
                        for(int r = 0 ; r < rowsCount ; r++){
                            Row row = sheet.getRow(r);
                            if (row.getPhysicalNumberOfCells() == CELL_COUNT) {
                                String question = getCellData(row,0,formulaEvaluator);
                                String optionA = getCellData(row,1,formulaEvaluator);
                                String optionB = getCellData(row,2,formulaEvaluator);
                                String optionC = getCellData(row,3,formulaEvaluator);
                                String optionD = getCellData(row,4,formulaEvaluator);
                                String correctAns = getCellData(row,5,formulaEvaluator);

                                if (correctAns.equals(optionA) || correctAns.equals(optionB) || correctAns.equals(optionC) || correctAns.equals(optionD)){
                                    HashMap<String, Object> questionMap = new HashMap<>();
                                    questionMap.put("question",question);
                                    questionMap.put("optionA",optionA);
                                    questionMap.put("optionB",optionB);
                                    questionMap.put("optionC",optionC);
                                    questionMap.put("optionD",optionD);
                                    questionMap.put("correctAns",correctAns);
                                    questionMap.put("setId",setId);
                                    String id = UUID.randomUUID().toString();

                                    parentMap.put(id,questionMap);

                                    tempList.add(new QuestionModel(id,question,optionA,optionB,optionC,optionD,correctAns,setId));

                                }else{
                                    final int finalR = r;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(QuestionsActivity.this, "Row No. "+(finalR +1)+" has no correct option", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }else{
                                final int finalR1 = r;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(QuestionsActivity.this, "Row No. "+(finalR1 +1)+" has incorrect data", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingText.setText("Uploading...");
                            }
                        });

                        FirebaseDatabase.getInstance().getReference()
                                .child("SETS").child(setId).updateChildren(parentMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    list.addAll(tempList);
                                    adapter.notifyDataSetChanged();
                                }else{
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(QuestionsActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
                        loadingDialog.dismiss();

                     }
                     else{
                         runOnUiThread(new Runnable() {
                             @Override
                             public void run() {
                                 Toast.makeText(QuestionsActivity.this, "File is Empty!", Toast.LENGTH_SHORT).show();
                             }
                         });

                     }
                 }
                 catch (final FileNotFoundException e) {
                     e.printStackTrace();
                     runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             Toast.makeText(QuestionsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                         }
                     });

                 }

                 catch (final IOException e) {
                     e.printStackTrace();
                     runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             Toast.makeText(QuestionsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                         }
                     });
                 }

                 finally {
                     runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             loadingText.setText("Loading...");
                             loadingDialog.dismiss();

                         }
                     });                 }
             }
         });
     }

     private String getCellData(Row row, int cellPosition, FormulaEvaluator formulaEvaluator){
        String value = "";
         Cell cell = row.getCell(cellPosition);
         switch(cell.getCellType()){
             case Cell.CELL_TYPE_BOOLEAN : return value+cell.getBooleanCellValue();
             case Cell.CELL_TYPE_NUMERIC : return value+cell.getNumericCellValue();
             case Cell.CELL_TYPE_STRING : return value+cell.getStringCellValue();
             default: return value;
         }
     }

     @Override
     public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         if (item.getItemId() == android.R.id.home){
                finish();
         }
         return super.onOptionsItemSelected(item);
     }

     @Override
     protected void onStart() {
         super.onStart();
         adapter.notifyDataSetChanged();
     }

     // Fetches Question from database.
     private void getData(String categoryName, final String setId){
         loadingDialog.show();
         myRef.child("SETS").child(setId)
                 .addListenerForSingleValueEvent(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot snapshot) {
                         for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                             String id = dataSnapshot.getKey();
                             String question = dataSnapshot.child("question").getValue().toString();
                             String optionA = dataSnapshot.child("optionA").getValue().toString();
                             String optionB = dataSnapshot.child("optionB").getValue().toString();
                             String optionC = dataSnapshot.child("optionC").getValue().toString();
                             String optionD = dataSnapshot.child("optionD").getValue().toString();
                             String correctAns = dataSnapshot.child("correctAns").getValue().toString();
                             list.add(new QuestionModel(id,question,optionA,optionB,optionC,optionD,correctAns,setId));
                         }
                         loadingDialog.dismiss();
                         adapter.notifyDataSetChanged();
                     }
                     @Override
                     public void onCancelled(@NonNull DatabaseError error) {
                         Toast.makeText(QuestionsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                         loadingDialog.dismiss();
                         finish();
                     }
                 });
     }
 }