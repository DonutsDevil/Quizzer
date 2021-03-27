package com.example.quizzeradmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.email_et);
        etPassword = findViewById(R.id.password_et);
        btnLogin = findViewById(R.id.login_btn);
        progressBar = findViewById(R.id.progressBar);
       final  Intent intent = new Intent(this,CategoryActivity.class);
        if (mAuth.getCurrentUser()!=null){
            // intent to category Activity;
            startActivity(intent);
            finish();
            return;
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etEmail.getText().toString().isEmpty()){
                    etEmail.setError("Required");
                    return;
                }else{
                    etEmail.setError(null);
                }
                if (etPassword.getText().toString().isEmpty()){
                    etPassword.setError("Required");
                    return;
                }else{
                    etPassword.setError(null);
                }
                progressBar.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(etEmail.getText().toString(),etPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            // intent to category Activity;
                            startActivity(intent);
                            finish();
                        }else{
                            Toast.makeText(MainActivity.this,"Failed", Toast.LENGTH_LONG).show();
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });
    }
}