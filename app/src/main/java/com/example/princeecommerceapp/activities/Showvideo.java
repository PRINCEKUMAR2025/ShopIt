package com.example.princeecommerceapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.princeecommerceapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Showvideo extends AppCompatActivity {
    DatabaseReference databaseReference,likesrefernce;
    RecyclerView recyclerView;
    FirebaseDatabase database;
    String name,url,uname;
    Boolean likechecker =false;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showvideo);
        recyclerView=findViewById(R.id.recyclerview_Showvideo);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        database=FirebaseDatabase.getInstance();
        databaseReference=database.getReference("video");
        likesrefernce=database.getReference("likes");
        button=findViewById(R.id.upload_video);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Showvideo.this,UploadVideo.class);
                startActivity(intent);
            }
        });

//        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(Showvideo.this,LinearLayoutManager.
//                HORIZONTAL,false);
//
//        recyclerView.setLayoutManager(linearLayoutManager);

    }



    private void firebaseSearch(String searchtext){
        String query=searchtext.toLowerCase();
        Query firebaseQuery=databaseReference.orderByChild("search").startAt(query).endAt(query+"\uf8ff");

        FirebaseRecyclerOptions<Member> options=
                new FirebaseRecyclerOptions.Builder<Member>()
                        .setQuery(firebaseQuery,Member.class).build();

        FirebaseRecyclerAdapter<Member,ViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Member, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Member model) {

                holder.setExoplayer(getApplication(),model.getName(),model.getVideourl(),model.getUsername());
                holder.setOnClicklistener(new ViewHolder.Clicklistener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        name=getItem(position).getName();
                        url=getItem(position).getVideourl();
                        uname=getItem(position).getUsername();
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                        name=getItem(position).getName();
                        showDeleteDialog(name);
                    }
                });
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false);

                return new ViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Member> options=
                new FirebaseRecyclerOptions.Builder<Member>()
                        .setQuery(databaseReference,Member.class).build();

        FirebaseRecyclerAdapter<Member,ViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Member, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Member model) {
                FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
                String currentUserId =user.getUid();
                final String postkey = getRef(position).getKey();

                holder.setExoplayer(getApplication(),model.getName(),model.getVideourl(), model.getUsername());

                holder.setOnClicklistener(new ViewHolder.Clicklistener() {
                    @Override
                    public void onItemClick(View view, int position) {


                        name=getItem(position).getName();
                        url=getItem(position).getVideourl();
//                        holder.exoPlayer.release();
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                        name=getItem(position).getName();
                        showDeleteDialog(name);
                    }
                });

                holder.setLikesbuttonStatus(postkey);

                holder.likebutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        likechecker = true;

                        likesrefernce.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if (likechecker.equals(true)){
                                    if (snapshot.child(postkey).hasChild(currentUserId)){
                                        likesrefernce.child(postkey).child(currentUserId).removeValue();
                                        likechecker=false;
                                    }else {
                                        likesrefernce.child(postkey).child(currentUserId).setValue(true);
                                        likechecker=false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false);

                return new ViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        switch (item.getItemId()){
//            case R.id.username_item:
//                Intent intent =new Intent(Showvideo.this,Username.class);
//                startActivity(intent);
//                return true;
//        }
        if (item.getItemId()==R.id.username_item){
            Toast.makeText(this, "Nothing..", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId()==R.id.get_premium) {
            Toast.makeText(this, "Nothing..", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu,menu);
        MenuItem item=menu.findItem(R.id.search_firebase);
        SearchView searchView=(SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                firebaseSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                firebaseSearch(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void showDeleteDialog(String name){
        AlertDialog.Builder builder=new AlertDialog.Builder(Showvideo.this);
        builder.setTitle("Delete");
        builder.setMessage("Are you sure want to delete this video");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Query query=databaseReference.orderByChild("name").equalTo(name);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot1 : snapshot.getChildren()){
                            dataSnapshot1.getRef().removeValue();
                        }
                        Toast.makeText(Showvideo.this, "Video deleted successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }

}