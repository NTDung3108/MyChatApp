package com.example.mychatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.Adapter.ViewPagerAdapter;
import com.example.Fragment.ChatsFragment;
import com.example.Fragment.ProfileFragment;
import com.example.Fragment.UsersFragment;
import com.example.Model.Chat;
import com.example.Model.User;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    CircleImageView imgProfile;
    TextView txtUsername;
    TabLayout tablayout;
    ViewPager viewpager;

    FirebaseUser firebaseUser;
    DatabaseReference reference;


    public static boolean isLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        isLogin = true;

        imgProfile = findViewById(R.id.imgProfile);
        txtUsername = findViewById(R.id.txtUsername);

        tablayout = findViewById(R.id.tablayout);
        viewpager = findViewById(R.id.viewpager);

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
                int unread = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isIsseen()){
                        unread++;
                    }
                }

                if (unread == 0){
                    viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
                }else {
                    viewPagerAdapter.addFragment(new ChatsFragment(), "("+unread+") Chats");
                }
                viewPagerAdapter.addFragment(new UsersFragment(), "Users");
                viewPagerAdapter.addFragment(new ProfileFragment(), "Profile");

                viewpager.setAdapter(viewPagerAdapter);

                tablayout.setupWithViewPager(viewpager);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("UID::::::::::", firebaseUser.getUid());
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                txtUsername.setText(user.getUsername());
                if (user.getImageURL().equals("default")){
                    imgProfile.setImageResource(R.mipmap.ic_launcher);
                }else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(imgProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                isLogin = false;
                startActivity(new Intent(MainActivity.this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
                return true;
        }
        return false;
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}