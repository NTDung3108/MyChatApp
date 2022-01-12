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

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private ArrayList<Chat> chats;
    private String imageURL;

    FirebaseUser firebaseUser;

    public MessageAdapter(Context mContext, ArrayList<Chat> chats, String imageURL) {
        this.mContext = mContext;
        this.chats = chats;
        this.imageURL = imageURL;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new ViewHolder(view);
        }else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Chat chat = chats.get(position);

        holder.txtMessage.setText(chat.getMessage());

        if (imageURL.equals("default")){
            holder.imgItemC.setImageResource(R.mipmap.ic_launcher_round);
        }else {
            Glide.with(mContext).load(imageURL).into(holder.imgItemC);
        }

        if (position == chats.size()-1){
            if (chat.isIsseen()){
                holder.txtSeen.setText("Seen");
            }else {
                holder.txtSeen.setText("Delivered");
            }
        }else {
            holder.txtSeen.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        public TextView txtMessage;
        public CircleImageView imgItemC;
        public TextView txtSeen;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            imgItemC = itemView.findViewById(R.id.imgItemC);
            txtSeen = itemView.findViewById(R.id.txtSeen);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chats.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_LEFT;
        }else {
            return MSG_TYPE_RIGHT;
        }
    }
}
