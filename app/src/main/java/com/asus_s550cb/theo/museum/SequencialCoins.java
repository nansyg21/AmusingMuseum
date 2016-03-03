package com.asus_s550cb.theo.museum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by panos on 3/3/2016.
 */
public class SequencialCoins extends Activity {
    PauseMenuButton pauseBt;

    int screenWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);//hide status bar
        setContentView(new SequencialCoinsScreen(this));

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenWidth = displaymetrics.widthPixels;
        pauseBt = new PauseMenuButton(screenWidth, this);

        // ------------------ Code in order to hide the navigation bar -------------------- //
        menu.hideNavBar(this.getWindow());

        //Start help screen
        Intent itn = new Intent(getApplicationContext(), HelpDialogActivity.class);
        itn.putExtra("appNum", 10);
        startActivity(itn);
    }

    // Reset the flags to hide the navigation bar
    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        menu.hideNavBar(this.getWindow());
    }


    //This is actually drawing on screen the game : Matching Stamps
    //A couple of pictures appear for each stamp. The player matches the stamps together
    public class SequencialCoinsScreen extends View implements Runnable {
        int ScreenWidth, ScreenHeight, PlayerTouchX, PlayerTouchY, mPosX, mPosY, imgWidth, imgHeight, widthGap, heightGap;
        int ROWS=2,COLLUMMS=5;
        Paint backgroundPaint;
        Bitmap frameImg;
        Random rand = new Random();

        ArrayList<Bitmap> coinImagesList = new ArrayList<Bitmap>();
        ArrayList<Rect> coinRectsList = new ArrayList<Rect>();


        private int mActivePointerId = -1;      //-1 instead of INVALID_POINTER_ID

        public SequencialCoinsScreen(Context context) {
            super(context);
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            ScreenHeight = displaymetrics.heightPixels;
            ScreenWidth = displaymetrics.widthPixels;

            //for background color
            backgroundPaint = new Paint();
            backgroundPaint.setStyle(Paint.Style.FILL);
            backgroundPaint.setColor(Color.parseColor("#8F0014"));//background: bussini

            //coins images
            imgWidth = ScreenWidth / 6;
            imgHeight = imgWidth;
            widthGap=10;
            heightGap=30;

            InitializeImages();
            InitializeRects();
            Log.w("Warn","Total: "+coinImagesList.size());

        }

        public void  InitializeImages (){
            coinImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.sc1));
            coinImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.sc2));
            coinImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.sc3));
            coinImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.sc4));
            coinImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.sc5));
            coinImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.sc6));
            coinImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.sc7));
            coinImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.sc8));
            coinImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.sc9));
            coinImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.sc10));
        }

        public void  InitializeRects(){
            int startingX, startingY; //coordinates of the upper-left coin
            startingX=ScreenWidth/10;
            startingY=ScreenHeight/4;
            //first row of coins
            coinRectsList.add(new Rect(startingX,startingY,startingX+imgWidth,startingY+imgHeight));
            coinRectsList.add(new Rect(getLastRect().right+widthGap,getLastRect().top,getLastRect().right+widthGap+imgWidth,getLastRect().bottom));
            coinRectsList.add(new Rect(getLastRect().right+widthGap,getLastRect().top,getLastRect().right+widthGap+imgWidth,getLastRect().bottom));
            coinRectsList.add(new Rect(getLastRect().right+widthGap,getLastRect().top,getLastRect().right+widthGap+imgWidth,getLastRect().bottom));
            coinRectsList.add(new Rect(getLastRect().right+widthGap,getLastRect().top,getLastRect().right+widthGap+imgWidth,getLastRect().bottom));

            //second row of coins
            coinRectsList.add(new Rect(getUpperRect().left, getUpperRect().bottom+heightGap,getUpperRect().right,getUpperRect().bottom+heightGap+imgHeight));
            coinRectsList.add(new Rect(getUpperRect().left, getUpperRect().bottom+heightGap,getUpperRect().right,getUpperRect().bottom+heightGap+imgHeight));
            coinRectsList.add(new Rect(getUpperRect().left, getUpperRect().bottom+heightGap,getUpperRect().right,getUpperRect().bottom+heightGap+imgHeight));
            coinRectsList.add(new Rect(getUpperRect().left, getUpperRect().bottom+heightGap,getUpperRect().right,getUpperRect().bottom+heightGap+imgHeight));
            coinRectsList.add(new Rect(getUpperRect().left, getUpperRect().bottom+heightGap,getUpperRect().right,getUpperRect().bottom+heightGap+imgHeight));
        }

        public Rect getLastRect(){
            return coinRectsList.get(coinRectsList.size()-1);
        }

        public Rect getUpperRect(){
            return coinRectsList.get(coinRectsList.size()-COLLUMMS);
        }

        @Override
        public void run() {   // Update state of what we draw

            // onDraw(Canvas) will be called
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            //Draw color on background
            canvas.drawPaint(backgroundPaint);

             //Draw coins
             for(int i=0;i< coinImagesList.size();i++)
                 canvas.drawBitmap(coinImagesList.get(i), null, coinRectsList.get(i), null);

            pauseBt.getPauseMenuButton().draw(canvas);

            // Invalidate view at about 60fps
            postDelayed(this, 16);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:       //New touch started
                {
                    //Check if pause button is hit
                    float touchX = ev.getX();
                    float touchY = ev.getY();

                    if (pauseBt.getRect().contains((int) touchX, (int) touchY)) {
                        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                            Intent itn;
                            itn = new Intent(getApplicationContext(), PauseMenuActivity.class);
                            startActivity(itn);
                        }
                    }

                    final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                    final float x = MotionEventCompat.getX(ev, pointerIndex);
                    final float y = MotionEventCompat.getY(ev, pointerIndex);

                    // Remember where we started (for dragging)
                    PlayerTouchX = (int) x;
                    PlayerTouchY = (int) y;

                    // Save the ID of this pointer (for dragging)
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0);

                    //do stuff here
                    break;
                }

                case MotionEvent.ACTION_UP:     //Finger left screen
                {
                    break;
                }
                case MotionEvent.ACTION_CANCEL: //Current event has been canceled, something else took control of the touch event
                {
                    break;
                }
                default:            //whatever happens
                {
                    break;
                }
            }
            return true;
        }
    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_exit_small)
                .setMessage(R.string.confirm_exit_large)
                .setNegativeButton(R.string.confirm_exit_cancel, null)
                .setPositiveButton(R.string.confirm_exit_οκ, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                        Intent itn = new Intent(getApplicationContext(), menu.class); //go to menu screen with proper flag set
                        itn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        itn.putExtra("leaving", true);
                        startActivity(itn);
                    }
                }).create().show();
    }
}



