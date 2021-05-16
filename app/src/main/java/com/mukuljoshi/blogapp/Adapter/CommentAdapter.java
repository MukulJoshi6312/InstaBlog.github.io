package com.mukuljoshi.blogapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mukuljoshi.blogapp.Model.Comments;
import com.mukuljoshi.blogapp.Model.Users;
import com.mukuljoshi.blogapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context context;
    private List<Comments> commentsList;
    private List<Users> usersList;

    public CommentAdapter(Context context, List<Comments> commentsList,List<Users> usersList) {
        this.context = context;
        this.commentsList = commentsList;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.each_comment,parent,false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comments comments = commentsList.get(position);
        holder.setsComment(comments.getComment());

        Users users = usersList.get(position);
        holder.setmUsername(users.getName());
        holder.setUserImage(users.getImage());

        
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder{

        TextView mComment;
        CircleImageView circleImageView;
        TextView userName;
        View mView;
        ImageView deleteComment;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setsComment(String comment){
            mComment = mView.findViewById(R.id.comment_tv);
            mComment.setText(comment);
            deleteComment = mView.findViewById(R.id.comment_delete);
        }

        public void setmUsername(String username){
            userName = mView.findViewById(R.id.comment_user_name);
            userName.setText(username);
        }
        public void setUserImage(String image) {
            circleImageView = mView.findViewById(R.id.comment_user_image);
            Picasso.get().load(image).into(circleImageView);

        }

    }

}
