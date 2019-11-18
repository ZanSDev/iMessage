package id.fauzanag.imessages.fragmentchat;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import id.fauzanag.imessages.R;
import id.fauzanag.imessages.adapter.Contacts;
import id.fauzanag.imessages.chatactivity.ChatActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private View PrivateChatsView;
    private RecyclerView chatlist;

    private DatabaseReference chatRef, userRef;
    private FirebaseAuth mAuth;
    private String currentUserID;


    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatsView = inflater.inflate(R.layout.fragment_chat, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        chatRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        chatlist = PrivateChatsView.findViewById(R.id.chat_list);
        chatlist.setLayoutManager(new LinearLayoutManager(getContext()));

        return PrivateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {

                        final String userIDs = getRef(position).getKey();
                        final String[] retImage = {"default_image"};

                        userRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                               if (dataSnapshot.exists()){
                                   if (dataSnapshot.hasChild("image"))
                                   {
                                       retImage[0] = dataSnapshot.child("image").getValue().toString();
                                       Picasso.get().load(retImage[0]).into(holder.profileImage);
                                   }

                                   final String retName = dataSnapshot.child("name").getValue().toString();
                                   final String retInfo = dataSnapshot.child("info").getValue().toString();

                                   holder.userName.setText(retName);

                                   if (dataSnapshot.child("userState").hasChild("state"))
                                   {
                                       String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                       String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                       String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                       if (state.equals("online"))
                                       {
                                           holder.userInfo.setText("online");
                                       }
                                       else if (state.equals("offline"))
                                       {
                                           holder.userInfo.setText("Last Seen: " +  date + " " + time);
                                       }
                                   }
                                   else
                                   {
                                       holder.userInfo.setText("offline");
                                   }

                                   holder.itemView.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View view) {

                                           Intent moveUser = new Intent(getContext(), ChatActivity.class);
                                           moveUser.putExtra("visit_user_id", userIDs);
                                           moveUser.putExtra("visit_user_name", retName);
                                           moveUser.putExtra("visit_image", retImage[0]);
                                           startActivity(moveUser);
                                       }
                                   });
                               }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        return new ChatsViewHolder(view);
                    }
                };
        chatlist.setAdapter(adapter);
        adapter.startListening();
    }


    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profileImage;
        TextView userInfo, userName;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.user_profileImage);
            userInfo = itemView.findViewById(R.id.user_status);
            userName = itemView.findViewById(R.id.user_profile_name);
        }
    }
}
