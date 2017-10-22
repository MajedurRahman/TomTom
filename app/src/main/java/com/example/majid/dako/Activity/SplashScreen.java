package com.example.majid.dako.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;

/**
 * Created by Majedur Rahman on 10/19/2017.
 */

public class SplashScreen extends Activity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startActivity(new Intent(SplashScreen.this,MainActivity.class));
        finish();
    }


}
