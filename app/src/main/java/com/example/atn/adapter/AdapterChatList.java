package com.example.atn.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.atn.R;
import com.example.atn.chat.ChatActivity;
import com.example.atn.model.ModelUser;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.HashMap;
import java.util.List;

public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.MyHolder> {
    private Context mContext;
    private List<ModelUser> mUsers;
    private HashMap<String, String> lastMessageMap;

    public AdapterChatList(Context context, List<ModelUser> users) {
        this.mContext = context;
        this.mUsers = users;
        lastMessageMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_chatlist, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int i) {
        final String hisUid = mUsers.get(i).getUid();
        String userImage = mUsers.get(i).getImage();
        String userName = mUsers.get(i).getName();
        String lastMessage = lastMessageMap.get(hisUid);
        holder.nameTv.setText(userName);

        if (lastMessage == null || lastMessage.equals("default")) {
            holder.lastMessageTv.setVisibility(View.GONE);
        } else {
            holder.lastMessageTv.setVisibility(View.VISIBLE);
            holder.lastMessageTv.setText(lastMessage);
        }

        try {
            Picasso.get().load(userImage).placeholder(R.drawable.bg_avatar).into(holder.profileIv);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.bg_avatar).into(holder.profileIv);
        }

        if (mUsers.get(i).getOnlineStatus().equals("online")) {
            holder.onlineStatusIv.setImageResource(R.drawable.circle_online);
        } else {
            holder.onlineStatusIv.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("hisUid", hisUid);
                mContext.startActivity(intent);
            }
        });
    }

    public void setLastMessageMap(String userId, String lastMessage) {
        lastMessageMap.put(userId, lastMessage);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        CircleImageView profileIv, onlineStatusIv;
        TextView lastMessageTv, nameTv;

        MyHolder(@NonNull View itemView) {
            super(itemView);
            profileIv = itemView.findViewById(R.id.profileIv);
            onlineStatusIv = itemView.findViewById(R.id.onlineStatusIv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);
            nameTv = itemView.findViewById(R.id.nameTv);
        }
    }
}
