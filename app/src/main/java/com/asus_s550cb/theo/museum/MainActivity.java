package com.asus_s550cb.theo.museum;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

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

        // CONSTRUCTOR
        public SampleView(Context context) {
            super(context);
            setFocusable(true);

                logo= BitmapFactory.decodeResource(getResources(), R.drawable.menu_logo);
                logo= Bitmap.createScaledBitmap(logo,width/2, height/4, true);
        }

        @Override
        protected void onDraw(final Canvas canvas) {

            //Set Background Color
            canvas.drawColor(Color.rgb(236, 253, 149));
            //Draw the logo
            canvas.drawBitmap(logo, (width / 2) / 2, (height * 3 / 10), null);

         /*   //Create Pseudo loading
            Paint p = new Paint();
            // smooths
            p.setAntiAlias(true);
            p.setColor(Color.BLUE);
            //after 5 seconds draw the second line
            canvas.drawRect(width / 2 - 50, (height * 3 / 5), width / 2 - 30, (height * 3 / 5) + 20, p);
            canvas.drawRect(width / 2 - 10, (height * 3 / 5), width / 2 + 10, (height * 3 / 5) + 20, p);
            canvas.drawRect(width / 2 + 30, (height * 3 / 5), width / 2 + 50, (height * 3 / 5) + 20, p);
            */

        }

    }

}
