package id.fauzanag.imessages.splashscreen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import id.fauzanag.imessages.R;
import id.fauzanag.imessages.termconditions.TermAndConditions;

public class SplashScreenActivity extends AppCompatActivity {

    private int SleepTimer = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash_screen);
        LogoLauncher logoLauncher = new LogoLauncher();
        logoLauncher.start();
    }

    private class LogoLauncher extends Thread{
        public void run(){
            try {
                sleep(1000 * SleepTimer);
            }

            catch (InterruptedException e){
                e.printStackTrace();
            }

            Intent Homeintent = new Intent(SplashScreenActivity.this, TermAndConditions.class);
            startActivity(Homeintent);
            SplashScreenActivity.this.finish();
        }
    }
}
