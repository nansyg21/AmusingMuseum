package com.asus_s550cb.theo.museum;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;


public class CreditsActivity extends Activity {

    int currentApiVersion;
    TextView creditsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_credits);

        creditsTextView=(TextView)findViewById(R.id.aboutTextView);

        // Used to scroll down the text view
        creditsTextView.setMovementMethod(new ScrollingMovementMethod());

        // ------------------ Code in order to hide the navigation bar -------------------- //
        menu.hideNavBar(this.getWindow());

    }

    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        menu.hideNavBar(this.getWindow());
    }


    public void buttonOnClick(View v)
    {
        finish();

    }

}
