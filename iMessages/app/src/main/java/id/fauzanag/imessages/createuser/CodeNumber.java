package id.fauzanag.imessages.createuser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import id.fauzanag.imessages.R;
import id.fauzanag.imessages.homechat.HomeChatActivity;

public class CodeNumber extends AppCompatActivity {


    private String verificationId;
    private ImageButton imageBack;
    private FirebaseAuth mAuth;
    private EditText textCode;

    private DatabaseReference RootRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_code_number);

        TextView getNumber = findViewById(R.id.getPhoneNumber);
        getNumber.setText(getIntent().getStringExtra("phone_number"));


        textCode = findViewById(R.id.textinputCode);
        textCode.setInputType(InputType.TYPE_CLASS_PHONE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        String phonenumber = getIntent().getStringExtra("phone_number");
        sendVerificationCode(phonenumber);

        findViewById(R.id.btnAccCode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String code = textCode.getText().toString().trim();

                if (code.isEmpty() || code.length() < 6) {

                    textCode.setError("Enter code...");
                    textCode.requestFocus();
                    return;

                }
                verifyCode(code);
            }
        });

        imageBack = findViewById(R.id.imgBack);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               onBackPressed();
            }
        });
    }

    private void sendVerificationCode(String number) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks
        );
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                textCode.setText(code);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(CodeNumber.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken token) {
            super.onCodeSent(s, token);
            verificationId = s;
        }
    };

    private void verifyCode(String otpcode) {

        try {

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otpcode);
            signInWithCredential(credential);

        } catch (Exception e) {

            Toast toast = Toast.makeText(this, "Verification Code is wrong", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

        }
    }

    private void signInWithCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {


                        if (task.isSuccessful()) {

                            Intent intent = new Intent(CodeNumber.this, HomeChatActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                            String phoneNumber = mAuth.getCurrentUser().getPhoneNumber();
                            String currentUserID = mAuth.getCurrentUser().getUid();


                            HashMap<String , String> userMap = new HashMap<>();
                            userMap.put("phone_number", phoneNumber);
                            userMap.put("uid", " ");

                            RootRef.child("Users").child(currentUserID).setValue(userMap);


                        } else {
                            Toast.makeText(CodeNumber.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
