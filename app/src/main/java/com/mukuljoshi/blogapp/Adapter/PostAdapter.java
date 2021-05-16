package com.mukuljoshi.blogapp.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mukuljoshi.blogapp.CommentActivity;
import com.mukuljoshi.blogapp.Model.Post;
import com.mukuljoshi.blogapp.Model.Users;
import com.mukuljoshi.blogapp.R;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> mList;
    private List<Users> usersList;
    private Activity context;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    public PostAdapter(List<Post> mList,List<Users> usersList, Activity context) {
        this.mList = mList;
        this.usersList = usersList;
        this.context = context;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.each_post, parent, false);
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {

        Post post = mList.get(position);

        holder.setPostPic(post.getImage());
        holder.setPostCaption(post.getCaption());
        long milliseconds = post.getTime().getTime();
        String date = DateFormat.format("MM/dd/yyyy", new Date(milliseconds)).toString();
        holder.setPostDate(date);


        String username = usersList.get(position).getName();
        String image = usersList.get(position).getImage();
        holder.setProfilePic(image);
        holder.setPostUsername(username);



        // this comment rescon is that imporve tha funcationality of the application

//        String userId = post.getUser();
//        firestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//
//                    String username = task.getResult().getString("name");
//                    String image = task.getResult().getString("image");
//                    holder.setProfilePic(image);
//                    holder.setPostUsername(username);
//
//                } else {
//                    Toast.makeText(context, task.getException().toString(), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

        // like button
        String postId = post.PostId;
        String currentUserId = auth.getCurrentUser().getUid();

        holder.likePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firestore.collection("Posts/" + postId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (!task.getResult().exists()) {
                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());
                            firestore.collection("Posts/" + postId + "/Likes").document(currentUserId).set(likesMap);

                        } else {
                            firestore.collection("Posts/" + postId + "/Likes").document(currentUserId).delete();

                        }


                    }
                });

            }
        });

        // change the like button
        firestore.collection("Posts/" + postId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if (error == null) {
                    if (value.exists()) {
                        holder.likePic.setImageDrawable(context.getDrawable(R.drawable.after_like));
                    } else {
                        holder.likePic.setImageDrawable(context.getDrawable(R.drawable.befor_like));

                    }
                }

            }
        });

        // likes counts
        firestore.collection("Posts/" + postId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error == null){
                    if (!value.isEmpty()){

                        int count = value.size();
                        holder.setPostLikes(count);
                    }
                    else {
                        holder.setPostLikes(0);
                    }
                }

            }
        });

        // comments implementation
        holder.commentPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("postid",postId);
                context.startActivity(intent);
            }
        });


        if (currentUserId.equals(post.getUser())) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setClickable(true);
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    AlertDialog.Builder alert = new AlertDialog.Builder(context)
                            .setTitle("Delete")
                            .setIcon(R.drawable.ic_baseline_delete_24)
                            .setMessage("Are you sure you want to delete this !!")
                            .setNegativeButton("No",null)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    firestore.collection("Posts/" + postId + "/Comments").get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                for (QueryDocumentSnapshot snapshot : task.getResult()){
                                                    firestore.collection("Posts/" + postId +
                                                            "/Comments").document(snapshot.getId()).delete();
                                                }

                                                }
                                            });
                                    firestore.collection("Posts/" + postId + "/Likes").get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    for (QueryDocumentSnapshot snapshot : task.getResult()){
                                                        firestore.collection("Posts/" + postId +
                                                                "/Likes").document(snapshot.getId()).delete();
                                                    }

                                                }
                                            });
                                    firestore.collection("Posts").document(postId).delete();
                                    mList.remove(position);
                                    notifyDataSetChanged();
                                }
                            });

                    alert.show();
                }
            });
        }


    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {

        ImageView postPic, commentPic, likePic;
        CircleImageView profilePic;
        TextView postUserName, postDate, postCaption, postLikes;
        View mView;
        ImageView deleteButton;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            likePic = mView.findViewById(R.id.like_button);
            commentPic = mView.findViewById(R.id.post_comment);
            deleteButton = mView.findViewById(R.id.delete_button);
        }

        public void setPostLikes(int count) {
            postLikes = mView.findViewById(R.id.likes_count_tv);
            postLikes.setText(count + "  Likes");
        }

        public void setPostPic(String urlPost) {
            postPic = mView.findViewById(R.id.user_post_image);
            //Glide.with(context).load(urlPost).into(postPic);
            Picasso.get().load(urlPost).into(postPic);
        }

        public void setProfilePic(String UrlProfile) {
            profilePic = mView.findViewById(R.id.post_profile_image);
           // Glide.with(context).load(UrlProfile).into(profilePic);
            Picasso.get().load(UrlProfile).into(profilePic);
        }

        public void setPostUsername(String username) {
            postUserName = mView.findViewById(R.id.username_tv);
            postUserName.setText(username);
        }

        public void setPostDate(String date) {
            postDate = mView.findViewById(R.id.date_vt);
            postDate.setText(date);

        }

        public void setPostCaption(String caption) {
            postCaption = mView.findViewById(R.id.post_caption);
            postCaption.setText(caption);
        }

    }

}
