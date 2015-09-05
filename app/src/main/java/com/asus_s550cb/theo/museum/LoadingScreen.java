package com.asus_s550cb.theo.museum;


import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;


public class LoadingScreen extends Activity {

    private int progressStatus = 0;//for handling the loading time
    private Handler handler = new Handler();
    private TextView textView;//For say Loading...



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideNavBar();
        setContentView(R.layout.activity_loading_screen);


        textView = (TextView) findViewById(R.id.textViewProgressCircle);

        // Start long running operation in a background thread
        new Thread(new Runnable() {
            public void run() {
                while (progressStatus < 15) {
                    progressStatus++;

                    handler.post(new Runnable() {
                        public void run() {
                            textView.setText(R.string.loading);
                        }
                    });
                    try {
                        // Sleep for 200 milliseconds.
                        //Just to display the progress
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //
               startActivityAfterLoading();
            }
        }).start();




    }
    //Just start the next activity and finish this one
    public void startActivityAfterLoading (){
        startActivity(new Intent(getApplicationContext(),StartGame.class));
        this.finish();
    }
    //HIDE the status an the navigation bars
    public void hideNavBar() {
        if (Build.VERSION.SDK_INT >= 19) {
            View v = getWindow().getDecorView();
            v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

    }
}
