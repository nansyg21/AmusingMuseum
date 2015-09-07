package com.asus_s550cb.theo.museum;



import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class HelpDialogActivity extends Activity{

    TextView advicesTextView;
    ImageView titleImageView;
    int screenHeight, screenWidth;
    int currentApiVersion;
    int textNum;
    String[] adviceText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_help_dialog);

        advicesTextView=(TextView) findViewById(R.id.helpTextView);
        titleImageView=(ImageView) findViewById(R.id.titleImageView);

        // ------------------ Code in order to hide the navigation bar -------------------- //
        menu.hideNavBar(this.getWindow());

        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
           textNum = extras.getInt("appNum");
        }

        // Get screen dimensions -- pixels
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenHeight = displaymetrics.heightPixels;
        screenWidth = displaymetrics.widthPixels;

        adviceText=getResources().getStringArray(R.array.advices);

        advicesTextView.setText(adviceText[textNum]);
        advicesTextView.invalidate();

        titleImageView.getLayoutParams().height=screenHeight/4;
     //   titleImageView.requestLayout();
    }

    // Reset the flags to hide the navigation bar
    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        menu.hideNavBar(this.getWindow());
    }

    public void setText(int textNum)
    {
        advicesTextView.setText("nasia");
    }

    public void buttonOnClick(View v)
    {
        finish();
    }

}
