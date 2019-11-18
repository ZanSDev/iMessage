package id.fauzanag.imessages.createuser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import id.fauzanag.imessages.R;
import id.fauzanag.imessages.homechat.HomeChatActivity;


public class ProfileCreateActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextView getNumber;
    private TextInputEditText editTextName, editTextInfo;
    private CircleImageView imageUp;

    private Button btnUP;
    private String currentUserID;

    private static final int GalleryPick = 1;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef, UsersRef;
    private StorageReference UserProfileImgRef;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_create);

        getNumber = findViewById(R.id.getNumber);

        imageUp = findViewById(R.id.profileImage);
        editTextName = findViewById(R.id.textInputName);
        editTextInfo = findViewById(R.id.textInputInfo);
        btnUP = findViewById(R.id.btnUP);
        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UserProfileImgRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        mToolbar = findViewById(R.id.profile_activity_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportActionBar().setTitle("Profile");

        imageUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileChooser();
            }
        });

        btnUP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfileUser();
            }
        });

        RetrieveUserProfile();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GalleryPick && resultCode==RESULT_OK && data!=null){
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK){

                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait, your profile image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();

                final StorageReference filePatch = UserProfileImgRef.child(currentUserID + ".jpg");

                filePatch.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                        filePatch.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                if (task.isSuccessful()){
                                    Toast.makeText(ProfileCreateActivity.this, "Profile image uploaded successfully..", Toast.LENGTH_SHORT).show();

                                    //to get Donload URL
                                    final String downloadURL = uri.toString();

                                    RootRef.child("Users").child(currentUserID).child("image")
                                            .setValue(downloadURL)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        Toast.makeText(ProfileCreateActivity.this, "Image save in database, Successfully..", Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();

                                                    } else {
                                                        String message = task.getException().toString();
                                                        Toast.makeText(ProfileCreateActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    }
                                                }
                                            });

                                } else {
                                    String message = task.getException().toString();
                                    Toast.makeText(ProfileCreateActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });
                    }
                });
            }
        }
    }



    private void updateProfileUser() {

        String getNumber = mAuth.getCurrentUser().getPhoneNumber();
        String setUsername = editTextName.getText().toString().trim();
        String setInfo = editTextInfo.getText().toString().trim();

        if (TextUtils.isEmpty(setUsername)){
            Toast.makeText(this, "Please write your name", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(setInfo)){
            Toast.makeText(this, "Set your info here", Toast.LENGTH_SHORT).show();
        }

        else {
            String deviceToken = FirebaseInstanceId.getInstance().getToken();

            HashMap<String, Object> profileMap = new HashMap<>();
                profileMap.put("uid", currentUserID);
                profileMap.put("phone_number", getNumber);
                profileMap.put("name", setUsername);
                profileMap.put("info", setInfo);
                profileMap.put("device_token", deviceToken);


            RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                Intent moveHome = new Intent(ProfileCreateActivity.this, HomeChatActivity.class);
                                moveHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(moveHome);
                                finish();

                                Toast.makeText(ProfileCreateActivity.this, "Profile updated successfully..", Toast.LENGTH_SHORT).show();
                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(ProfileCreateActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }

    }

    private void RetrieveUserProfile() {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))){

                            String retrieveName = dataSnapshot.child("name").getValue().toString();
                            String retrieveInfo = dataSnapshot.child("info").getValue().toString();
                            String retrievePhone = dataSnapshot.child("phone_number").getValue().toString();
                            String retrieveImage = dataSnapshot.child("image").getValue().toString();

                            editTextName.setText(retrieveName);
                            editTextInfo.setText(retrieveInfo);
                            getNumber.setText(retrievePhone);
                            Picasso.get().load(retrieveImage).into(imageUp);

                        }

                        else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){

                            String retrieveName = dataSnapshot.child("name").getValue().toString();
                            String retrievePhone = dataSnapshot.child("phone_number").getValue().toString();
                            String retrieveInfo = dataSnapshot.child("info").getValue().toString();

                            editTextName.setText(retrieveName);
                            editTextInfo.setText(retrieveInfo);
                            getNumber.setText(retrievePhone);
                        }

                        else {
                            Toast.makeText(ProfileCreateActivity.this, "Please setup and update your profile..", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void FileChooser() {
        Intent galerryPhone = new Intent();
        galerryPhone.setAction(Intent.ACTION_GET_CONTENT);
        galerryPhone.setType("image/*");
        startActivityForResult(galerryPhone, GalleryPick);
    }

}
