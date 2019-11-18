package id.fauzanag.imessages.termconditions;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import id.fauzanag.imessages.homechat.HomeChatActivity;
import id.fauzanag.imessages.R;
import id.fauzanag.imessages.createuser.PhoneNumber;

public class TermAndConditions extends AppCompatActivity {

    private static final String TAG = TermAndConditions.class.getSimpleName();
    private TextView textAgre, textConti;
    private Button btnContinue;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_term_and_conditions);

        String tapAgree = "<font>Baca</font> <font color=#3355FF>Kebijakan Privasi</font> <font>kami. Ketuk 'Setuju dan</font>";
        String tapTermsOfService = "<font>lanjutkan' untuk menerima</font> <font color=#3355FF>Ketentuan Layanan</font><font>.</font>";

        textAgre = findViewById(R.id.tapAgree);
        textAgre.setText(Html.fromHtml(tapAgree));

        textConti = findViewById(R.id.tapContinue);
        textConti.setText(Html.fromHtml(tapTermsOfService));

        String buttonState = LoadButtonState();

        if(buttonState.equals("setuju_lanjutkan")){
        }

        btnContinue = findViewById(R.id.btnNext);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveButtonState("setuju_lanjutkan");

                Intent movePhoneNumber = new Intent(TermAndConditions.this, PhoneNumber.class);
                startActivity(movePhoneNumber);
                finish();
            }
        });

    }
    public void SaveButtonState(String bState){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(TermAndConditions.this);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString("focus_value", bState);
        edit.commit();
    }

    public String LoadButtonState(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String buttonState = preferences.getString("setuju_lanjutkan", "DEFAULT");
        return buttonState;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(this, HomeChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }


}


