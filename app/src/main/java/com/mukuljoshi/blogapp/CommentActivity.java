package com.mukuljoshi.blogapp;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.mukuljoshi.blogapp.Adapter.CommentAdapter;
import com.mukuljoshi.blogapp.Model.Comments;
import com.mukuljoshi.blogapp.Model.Users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {


    private RecyclerView commentRecyclerView;
    private EditText commentEditText;
    private Button addCommentBtn;
    private FirebaseFirestore firestore;
    private String post_id;
    private String currentUserId;
    private FirebaseAuth auth;
    private CommentAdapter adapter;
    private List<Comments> mList;
    private List<Users> usersList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.dark_blue));

        commentRecyclerView = findViewById(R.id.comment_recyclerView);
        commentEditText = findViewById(R.id.comment_editText);
        addCommentBtn = findViewById(R.id.comment_button);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        mList = new ArrayList<>();
        usersList = new ArrayList<>();

        currentUserId = auth.getCurrentUser().getUid();

        post_id = getIntent().getStringExtra("postid");


        commentRecyclerView.setHasFixedSize(true);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommentAdapter(this,mList,usersList);
        commentRecyclerView.setAdapter(adapter);

        firestore.collection("Posts/" + post_id + "/Comments").addSnapshotListener( CommentActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                for (DocumentChange documentChange : value.getDocumentChanges()){

                    if (documentChange.getType() == DocumentChange.Type.ADDED){
                        Comments comments = documentChange.getDocument().toObject(Comments.class);

                        String userId = documentChange.getDocument().getString("user");

                        firestore.collection("Users").document(userId).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if (task.isSuccessful()){
                                            Users users = task.getResult().toObject(Users.class);
                                            usersList.add(users);
                                            mList.add(comments);
                                            adapter.notifyDataSetChanged();

                                        }
                                        else {
                                            Toast.makeText(CommentActivity.this,
                                                    task.getException().getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
//
//                        mList.add(comments);
//                        adapter.notifyDataSetChanged();

                    }else {
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });

        addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = commentEditText.getText().toString();
                if (!comment.isEmpty()){
                    Map<String,Object> commentMap = new HashMap<>();
                    commentMap.put("comment",comment);
                    commentMap.put("time", FieldValue.serverTimestamp());
                    commentMap.put("user",currentUserId);
                    firestore.collection("Posts/" + post_id + "/Comments").add(commentMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()){

                            Toast.makeText(CommentActivity.this, "Comment Added !!",
                                    Toast.LENGTH_SHORT).show();
                            commentEditText.setText("");

                        }else {
                            Toast.makeText(CommentActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                                    
                        }
                        }
                    });
                    
                }else {
                    Toast.makeText(CommentActivity.this, "Please add comment",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}