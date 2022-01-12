package com.example.mychatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.Adapter.MessageAdapter;
import com.example.Fragment.APIService;
import com.example.Model.Chat;
import com.example.Model.User;
import com.example.Notification.Client;
import com.example.Notification.Data;
import com.example.Notification.MyResponse;
import com.example.Notification.Sender;
import com.example.Notification.Token;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.util.SharedPreferencesUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    CircleImageView imgUserC;
    TextView txtUsernameC;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    Intent intent;

    EditText edSend;
    ImageButton btnSend;

    MessageAdapter messageAdapter;
    ArrayList<Chat> chats;
    RecyclerView recyclerView;

    ValueEventListener seenListener;
    String userid;

    APIService apiService;

    boolean notify = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message2);

        Toolbar toolbar = findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        recyclerView = findViewById(R.id.recyclerViewChat);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
        manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(manager);

        imgUserC = findViewById(R.id.imgUserC);
        txtUsernameC = findViewById(R.id.txtUsernameC);
        edSend = findViewById(R.id.edSend);
        btnSend = findViewById(R.id.btnSend);

        intent = getIntent();
        userid = intent.getStringExtra("userid");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String msg = edSend.getText().toString();
                if (!msg.isEmpty()){
                    sendMessage(firebaseUser.getUid(), userid, msg);
                }else {
                    Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                edSend.setText("");
            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                txtUsernameC.setText(user.getUsername());
                if (user.getImageURL().equals("default")){
                    imgUserC.setImageResource(R.mipmap.ic_launcher_round);
                }else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(imgUserC);
                }

                readMessagges(firebaseUser.getUid(), userid, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        seenMessage(userid);
    }

    private void seenMessage(final String userid){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String sender, String receiver, String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("isseen",false);

        reference.child("Chats").push().setValue(hashMap);

        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(firebaseUser.getUid())
                .child(userid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final String msg = message;
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (notify){
                    sendNotification(receiver, user.getUsername(), msg);
                }

                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification(String receiver, String username, String msg) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(firebaseUser.getUid(), R.mipmap.ic_launcher, username+": "+msg, "New Message",
                            userid);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if (response.code() == 200){
                                if (response.body().success !=1 ){
                                    Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessagges(String myID, String userID, String imageURL){
        chats = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chats.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myID) && chat.getSender().equals(userID) ||
                            chat.getReceiver().equals(userID) && chat.getSender().equals(myID) ){
                        chats.add(chat);
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this, chats, imageURL);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void currentUser(String userid){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
        currentUser("none");
    }
}