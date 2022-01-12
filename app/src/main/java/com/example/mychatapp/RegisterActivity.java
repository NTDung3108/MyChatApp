package com.example.mychatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    MaterialEditText edUsername, edEmail, edPassword;
    Button btnRegister;

    FirebaseAuth auth;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();

        auth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = edUsername.getText().toString();
                String email = edEmail.getText().toString();
                String password = edPassword.getText().toString();

                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                    Toast.makeText(RegisterActivity.this, "All fileds  are required", Toast.LENGTH_SHORT).show();
                }else if (password.length() < 6){
                    Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                }else {
                    register(username, email, password);
                }
            }
        });
    }

    private void initView() {
        edUsername = findViewById(R.id.edUsername);
        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassword);
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void register(String username, String email, String password){
        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            assert firebaseUser != null;
                            String userid = firebaseUser.getUid();

                            reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id",userid);
                            hashMap.put("username", username);
                            hashMap.put("imageURL", "default");
                            hashMap.put("status", "offline");
                            hashMap.put("search", username.toLowerCase());

                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                        }else {
                            Toast.makeText(RegisterActivity.this,"You can't register woth this email or password", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}