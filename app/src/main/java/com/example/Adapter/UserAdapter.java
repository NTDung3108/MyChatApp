package com.example.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.Model.Chat;
import com.example.Model.User;
import com.example.mychatapp.MessageActivity;
import com.example.mychatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.mychatapp.MainActivity.isLogin;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<User> users;
    private boolean isChat;
    String theLastMessage;

    public UserAdapter(Context mContext, ArrayList<User> users, boolean isChat) {
        this.mContext = mContext;
        this.users = users;
        this.isChat = isChat;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.txtUser.setText(user.getUsername());
        if (user.getImageURL().equals("default")){
            holder.imgUser.setImageResource(R.mipmap.ic_launcher_round);
        }else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.imgUser);
        }

        if (isChat){
            lastMessage(user.getId(), holder.txtLastMSG);
        }else {
            holder.txtLastMSG.setVisibility(View.GONE);
        }

        if (isChat){
            if (user.getStatus().equals("online")){
                holder.imgOn.setVisibility(View.VISIBLE);
                holder.imgOff.setVisibility(View.GONE);
            }else {
                holder.imgOn.setVisibility(View.GONE);
                holder.imgOff.setVisibility(View.VISIBLE);
            }
        }else {
            holder.imgOn.setVisibility(View.GONE);
            holder.imgOff.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userid",user.getId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        public TextView txtUser;
        public CircleImageView imgUser;
        private CircleImageView imgOn;
        private CircleImageView imgOff;
        private TextView txtLastMSG;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUser = itemView.findViewById(R.id.txtUser);
            imgUser = itemView.findViewById(R.id.imgUser);
            imgOn = itemView.findViewById(R.id.imgOn);
            imgOff = itemView.findViewById(R.id.imgOff);
            txtLastMSG = itemView.findViewById(R.id.txtLastMSG);
        }
    }

    private void lastMessage(final String userid, final TextView txtLastMSG){
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if (isLogin){
                        Chat chat = snapshot.getValue(Chat.class);
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                                chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())){
                            theLastMessage = chat.getMessage();
                        }
                    }

                }

                switch (theLastMessage){
                    case "default":
                        txtLastMSG.setText("No Message");
                        break;
                    default:
                        txtLastMSG.setText(theLastMessage);
                        break;
                }

                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
