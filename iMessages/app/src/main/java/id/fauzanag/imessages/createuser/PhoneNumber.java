package id.fauzanag.imessages.createuser;


import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.hbb20.CountryCodePicker;


import id.fauzanag.imessages.R;

public class PhoneNumber extends AppCompatActivity {

    private EditText phoneText;
    private FirebaseAuth phoneAuth;
    CountryCodePicker ccp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_phone_number);

        phoneText = findViewById(R.id.phoneText);

        ccp = findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phoneText);

        phoneAuth = FirebaseAuth.getInstance();


        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String number = phoneText.getText().toString().trim();

                if (number.isEmpty() || number.length() < 10){
                    phoneText.setError("Valid number is required");
                    phoneText.requestFocus();
                    return;
                }

                Intent moveCode = new Intent(PhoneNumber.this, CodeNumber.class);
                moveCode.putExtra("phone_number", ccp.getFullNumberWithPlus());
                startActivity(moveCode);

                Toast.makeText(PhoneNumber.this,   ccp.getFullNumberWithPlus() , Toast.LENGTH_SHORT).show();
            }
        });
    }

}
