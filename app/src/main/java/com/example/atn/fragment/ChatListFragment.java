package com.example.atn.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.atn.R;
import com.example.atn.adapter.AdapterChatList;
import com.example.atn.model.ModelChat;
import com.example.atn.model.ModelChatList;
import com.example.atn.model.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private List<ModelChatList> mChatLists;
    private List<ModelUser> mUserList;
    private DatabaseReference mReference;
    private FirebaseUser currentUser;
    private AdapterChatList mAdapterChatList;

    public ChatListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        mRecyclerView = view.findViewById(R.id.recyclerView);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        mChatLists = new ArrayList<>();
        mReference =
                FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChatLists.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChatList chatList = ds.getValue(ModelChatList.class);
                    mChatLists.add(chatList);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return view;
    }

    private void loadChats() {
        mUserList = new ArrayList<>();
        mReference = FirebaseDatabase.getInstance().getReference("Users");
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUserList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelUser modelUser = ds.getValue(ModelUser.class);
                    for (ModelChatList chatList : mChatLists) {
                        if (Objects.requireNonNull(modelUser).getUid() != null && modelUser.getUid()
                                .equals(chatList.getId())) {
                            mUserList.add(modelUser);
                            break;
                        }
                    }

                    mAdapterChatList = new AdapterChatList(getContext(), mUserList);
                    mRecyclerView.setAdapter(mAdapterChatList);

                    for (int i = 0; i < mUserList.size(); i++) {
                        lastMessage(mUserList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void lastMessage(final String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String theLastMessage = "default";
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat == null) {
                        continue;
                    }
                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();
                    if (sender == null || receiver == null) {
                        continue;
                    }
                    if (chat.getReceiver().equals(currentUser.getUid()) && chat.getSender()
                            .equals(userId) || chat.getReceiver().equals(userId) && chat.getSender()
                            .equals(currentUser.getUid())) {
                        theLastMessage = chat.getMessage();
                    }
                }

                mAdapterChatList.setLastMessageMap(userId, theLastMessage);
                mAdapterChatList.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
