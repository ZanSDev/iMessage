package id.fauzanag.imessages.fragmentcontacts;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    private View ContactsView;
    private RecyclerView myContactsLis;

    private DatabaseReference contacsRef, usersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);

        myContactsLis = ContactsView.findViewById(R.id.contacts_list);
        myContactsLis.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        contacsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contacsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ContacsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contacts, ContacsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContacsViewHolder holder, int position, @NonNull Contacts model) {

                final String userIDs = getRef(position).getKey();

                usersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists())
                        {

                            if (dataSnapshot.child("userState").hasChild("state"))
                            {
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                if (state.equals("online"))
                                {
                                    holder.onlineIcon.setVisibility(View.VISIBLE);
                                }
                                else if (state.equals("offline"))
                                {
                                    holder.onlineIcon.setVisibility(View.INVISIBLE);
                                }
                            }
                            else
                            {
                                holder.onlineIcon.setVisibility(View.INVISIBLE);
                            }

                            if (dataSnapshot.hasChild("image")){

                                String profileUser = dataSnapshot.child("image").getValue().toString();
                                String profileName = dataSnapshot.child("name").getValue().toString();
                                String profileInfo = dataSnapshot.child("info").getValue().toString();

                                holder.userName.setText(profileName);
                                holder.userInfo.setText(profileInfo);
                                Picasso.get().load(profileUser).placeholder(R.drawable.profile).into(holder.profileImage);
                            } else {

                                String profileName = dataSnapshot.child("name").getValue().toString();
                                String profileInfo = dataSnapshot.child("info").getValue().toString();

                                holder.userName.setText(profileName);
                                holder.userInfo.setText(profileInfo);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ContacsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                ContacsViewHolder viewHolder = new ContacsViewHolder(view);

                return viewHolder;
            }
        };

        myContactsLis.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContacsViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userInfo;
        CircleImageView profileImage;
        ImageView onlineIcon;

        public ContacsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userInfo = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_profileImage);
            onlineIcon = itemView.findViewById(R.id.user_online_status);
        }
    }
}
