package com.example.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.Adapter.UserAdapter;
import com.example.Model.Chat;
import com.example.Model.ChatList;
import com.example.Model.User;
import com.example.Notification.Token;
import com.example.mychatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {
    private RecyclerView recyclerViewChatsF;

    private UserAdapter userAdapter;
    private ArrayList<User> users;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    private ArrayList<ChatList> chatLists;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerViewChatsF = view.findViewById(R.id.recyclerViewChatsF);
        recyclerViewChatsF.setHasFixedSize(true);
        recyclerViewChatsF.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        chatLists = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatLists.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatList chatList = snapshot.getValue(ChatList.class);
                    chatLists.add(chatList);
                }
                
                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w("ChatsFragment", "Fetching FCM registration token failed", task.getException());
                    return;
                }

                // Get new FCM registration token
                String refreshedToken = task.getResult();
                if (firebaseUser != null){
                    updateToken(refreshedToken);
                }
            }
        });
        return view;
    }

    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(firebaseUser.getUid()).setValue(token1);
    }

    private void chatList() {
        users = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    for (ChatList chatList : chatLists){
                        if (user.getId().equals(chatList.getId())){
                            users.add(user);
                        }
                    }
                }
                userAdapter = new UserAdapter(getContext(), users, true);
                recyclerViewChatsF.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}