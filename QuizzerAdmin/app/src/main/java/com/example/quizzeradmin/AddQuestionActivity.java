package com.example.quizzeradmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.UUID;

public class  AddQuestionActivity extends AppCompatActivity {
    private EditText questionEt;
    private RadioGroup options;
    private LinearLayout answers;
    private Button uploadBtn;
    private String categoryName;
    private int position;
    private String setId;
    private Dialog loadingDialog;
    private QuestionModel questionModel;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);
        categoryName = getIntent().getStringExtra("categoryName");
        setId = getIntent().getStringExtra("setId");
        position = getIntent().getIntExtra("position",-1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        questionEt = findViewById(R.id.questionEt);
        options = findViewById(R.id.options);
        answers = findViewById(R.id.answers);
        uploadBtn = findViewById(R.id.uploadBtn);

        getSupportActionBar().setTitle("Add Question");
        if(setId == null){
            finish();
            return;
        }
        if (position != -1){
            getSupportActionBar().setTitle(categoryName+"/ Q:"+(position+1));
            questionModel = QuestionsActivity.list.get(position);
            setData();
        }

        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (questionEt.getText().toString().trim().isEmpty()){
                    questionEt.setError("Required");
                    return;
                }
                uploadQuestion();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadQuestion(){
        int correct = -1;
        for(int i = 0 ; i < options.getChildCount(); i++){
            EditText answer = (EditText) answers.getChildAt(i);
            if (answer.getText().toString().trim().isEmpty()){
                answer.setError("Required");
                return;
            }
            RadioButton radioButton = (RadioButton)options.getChildAt(i);
            if (radioButton.isChecked()){
                correct = i;
                break;
            }
        }
        if (correct == -1){
            Toast.makeText(AddQuestionActivity.this,"Please mark the correct option",Toast.LENGTH_LONG).show();
            return;
        }
        HashMap<String , Object> map = new HashMap<String, Object>();
        map.put("correctAns",((EditText)answers.getChildAt(correct)).getText().toString());
        map.put("optionA",((EditText)answers.getChildAt(0)).getText().toString());
        map.put("optionB",((EditText)answers.getChildAt(1)).getText().toString());
        map.put("optionC",((EditText)answers.getChildAt(2)).getText().toString());
        map.put("optionD",((EditText)answers.getChildAt(3)).getText().toString());
        map.put("question",questionEt.getText().toString());
        map.put("setId",setId);
        if (position != -1){
            id = questionModel.getId();
        }else {
            id = UUID.randomUUID().toString();
        }
        loadingDialog.show();
        FirebaseDatabase.getInstance().getReference().child("SETS")
                .child(setId).child(id)
                .setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    QuestionModel questionModel = new QuestionModel(id,map.get("question").toString(),
                            map.get("optionA").toString(),
                            map.get("optionB").toString(),
                            map.get("optionC").toString(),
                            map.get("optionD").toString(),
                            map.get("correctAns").toString(),
                            map.get("setNo").toString() );
                    if (position != -1){
                        QuestionsActivity.list.set(position,questionModel);
                    }else{
                        QuestionsActivity.list.add(questionModel);
                    }
                    loadingDialog.dismiss();
                    finish();
                }else{
                    Toast.makeText(AddQuestionActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();
            }
        });
    }

    private void setData(){
        questionEt.setText(questionModel.getQuestion());
        ((EditText)answers.getChildAt(0)).setText(questionModel.getOptionA());
        ((EditText)answers.getChildAt(1)).setText(questionModel.getOptionB());
        ((EditText)answers.getChildAt(2)).setText(questionModel.getOptionC());
        ((EditText)answers.getChildAt(3)).setText(questionModel.getOptionD());

        for(int i = 0 ; i < answers.getChildCount() ; i++){
            if (((EditText)answers.getChildAt(i)).getText().toString().equals(questionModel.getCorrectAns())){
                RadioButton radioButton = (RadioButton) options.getChildAt(i);
                radioButton.setChecked(true);
                break;
            }
        }
    }
}