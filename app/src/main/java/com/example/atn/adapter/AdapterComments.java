package com.example.atn.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.atn.R;
import com.example.atn.model.ModelComment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyHolder> {
    private Context mContext;
    private List<ModelComment> mCommentList;
    private String myUid, postId;

    public AdapterComments(Context mContext, List<ModelComment> mCommentList, String myUid,
            String postId) {
        this.mContext = mContext;
        this.mCommentList = mCommentList;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_comments, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int i) {
        final String uid = mCommentList.get(i).getUid();
        String name = mCommentList.get(i).getuName();
        String image = mCommentList.get(i).getuDp();
        final String cid = mCommentList.get(i).getcId();
        String comment = mCommentList.get(i).getComment();
        String timeStamp = mCommentList.get(i).getTimeStamp();

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        if (timeStamp != null) {
            cal.setTimeInMillis(Long.parseLong(timeStamp));
        }
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

        holder.nameTv.setText(name);
        holder.timeTv.setText(dateTime);
        holder.commentTv.setText(comment);

        try {
            Picasso.get().load(image).placeholder(R.drawable.bg_avatar).into(holder.avatarIv);
        } catch (Exception ignored) {

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myUid.equals(uid)) {
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(v.getRootView().getContext());
                    builder.setTitle("Xóa bình luận");
                    builder.setMessage("Bạn có chắc chắn muốn xóa bình luận ?")
                            .setCancelable(false)
                            .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteComment(cid);
                                }
                            })
                            .setNegativeButton("Không", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    builder.create().show();
                } else {
                    Toast.makeText(mContext, "Không xóa được bình luận!", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }

    private void deleteComment(String cid) {
        final DatabaseReference ref =
                FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.child("Comments").child(cid).removeValue();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String comments = "" + dataSnapshot.child("pComments").getValue();
                int newCommentVal = Integer.parseInt(comments) - 1;
                ref.child("pComments").setValue("" + newCommentVal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mCommentList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        CircleImageView avatarIv;
        TextView nameTv, commentTv, timeTv;

        MyHolder(@NonNull View view) {
            super(view);

            avatarIv = view.findViewById(R.id.avatarIv);
            nameTv = view.findViewById(R.id.nameTv);
            commentTv = view.findViewById(R.id.commentTv);
            timeTv = view.findViewById(R.id.timeTv);
        }
    }
}
