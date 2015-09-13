package com.asus_s550cb.theo.museum;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.os.Handler;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    public int height,width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        menu.hideNavBar(this.getWindow());
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        height = displaymetrics.heightPixels;
        width = displaymetrics.widthPixels;


        setContentView(new SampleView(this));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), menu.class));
                finish();
            }
        }, 1000);

    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    private class SampleView extends View {

        Bitmap logo= null;
        Bitmap museum_logo=null;

        // CONSTRUCTOR
        public SampleView(Context context) {
            super(context);
            setFocusable(true);

            logo= BitmapFactory.decodeResource(getResources(), R.drawable.logo);
            logo= Bitmap.createScaledBitmap(logo, width / 2, height / 4, true);

            museum_logo= BitmapFactory.decodeResource(getResources(), R.drawable.museum_logo);
            museum_logo= Bitmap.createScaledBitmap(museum_logo, height / 2, height / 2, true);
        }

        @Override
        protected void onDraw(final Canvas canvas) {

            //Set Background Color
            canvas.drawColor(getResources().getColor(R.color.royal_blue));
            //Draw the logo
            canvas.drawBitmap(logo, (width / 2) / 2, (height * 2 / 10), null);
            canvas.drawBitmap(museum_logo,(width/2)-(height/4),(height*5/10),null);

        }
    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_exit_small)
                .setMessage(R.string.confirm_exit_large)
                .setNegativeButton(R.string.confirm_exit_cancel, null)
                .setPositiveButton(R.string.confirm_exit_οκ, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1)
                    {
                        finish();
                        Intent itn= new Intent(getApplicationContext(), menu.class); //go to menu screen with proper flag set
                        itn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        itn.putExtra("leaving", true);
                        startActivity(itn);
                    }
                }).create().show();
    }
}
