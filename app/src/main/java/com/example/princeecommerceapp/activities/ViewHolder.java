package com.example.princeecommerceapp.activities;

import android.app.Application;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.princeecommerceapp.R;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;

public class ViewHolder extends RecyclerView.ViewHolder {
    SimpleExoPlayer exoPlayer;
    PlayerView playerView;
    ImageView likebutton,commentbtn;
    TextView likesdisplay,commentdisplay;
    int likescount,commentcount;
    DatabaseReference likesref,postref;
    public ViewHolder(@NonNull View itemView) {

        super(itemView);
        itemView .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClicklistener.onItemClick(view,getAdapterPosition());
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mClicklistener.onItemLongClick(view,getAdapterPosition());
                return false;
            }
        });
    }

    public void setLikesbuttonStatus(final String postkey){
            likebutton=itemView.findViewById(R.id.like_btn);
            likesdisplay=itemView.findViewById(R.id.likes_textview);
            likesref = FirebaseDatabase.getInstance().getReference("likes");
            postref=FirebaseDatabase.getInstance().getReference().child("video").child(postkey).child("comments");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        String likes="likes";

        postref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    commentcount = (int) snapshot.getChildrenCount();
                    commentdisplay.setText(Integer.toString(commentcount));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        likesref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child(postkey).hasChild(userId)){
                    likescount=(int) snapshot.child(postkey).getChildrenCount();
                    likebutton.setImageResource(R.drawable.like);
                    likesdisplay.setText(Integer.toString(likescount)+likes);
                }else {
                    likescount=(int) snapshot.child(postkey).getChildrenCount();
                    likebutton.setImageResource(R.drawable.unlike);
                    likesdisplay.setText(Integer.toString(likescount)+likes);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setExoplayer(Application application, String name, String Videourl, String username){

        TextView textView=itemView.findViewById(R.id.tv_item_name);
        TextView textView1=itemView.findViewById(R.id.tv_uname);
        playerView =itemView.findViewById(R.id.exoplayer_item);

        textView.setText(name);
        textView1.setText(username);

        try{
            SimpleExoPlayer simpleExoPlayer=new SimpleExoPlayer.Builder(application).build();
            playerView.setPlayer(simpleExoPlayer);
            MediaItem mediaItem=MediaItem.fromUri(Videourl);
            simpleExoPlayer.addMediaItems(Collections.singletonList(mediaItem));
            simpleExoPlayer.prepare();
            simpleExoPlayer.setPlayWhenReady(false);

        }catch (Exception e){
            Log.e("ViewHolder","exoplayer error"+e.toString());
        }
    }

    private Clicklistener mClicklistener;
    public interface Clicklistener{
        void onItemClick(View view,int position);
        void onItemLongClick(View view,int position);
    }

    public void setOnClicklistener(Clicklistener clicklistener){
        mClicklistener=clicklistener;
    }
}
