package com.asus_s550cb.theo.museum;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;


/**
 * Created by theo on 9/7/2015.
 */
public class StartGame extends Activity {

    Ourview v;
    int height,width,newInt;
    static int startingStage=0;

    boolean scan=true;

    String nextApp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            //Hide everything...:)
            hideNavBar();

            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            height = displaymetrics.heightPixels;
            width = displaymetrics.widthPixels;

            nextApp="nextApp";

            v = new Ourview(this);
            setContentView(v);
        }


    @Override
    protected void onPause() {
        super.onPause();
        v.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideNavBar();//hide everything on Resume
        try {
            v.resume();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //StageCounter
        startingStage++;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public class Ourview extends SurfaceView implements Runnable {

        Thread t;
        SurfaceHolder holder;
        boolean isItok = false;
        Bitmap ppenguin,background;
        Sprite sprite;

        //CONSTRUCTOR
        public Ourview(Context context) {
            super(context);
            holder = getHolder();
            ppenguin = BitmapFactory.decodeResource(getResources(), R.drawable.penguinsheet);
            background= BitmapFactory.decodeResource(getResources(), R.drawable.museum_map);
            background = Bitmap.createScaledBitmap(background,width,  (int) Math.ceil(height * 0.95), false);


        }

        @Override
        public void run() {

            sprite = new Sprite(Ourview.this, ppenguin,width,height,startingStage);
            while(isItok){
                //perform drawing
                if(!holder.getSurface().isValid()){
                    continue;
                }

                Canvas c =holder.lockCanvas();
                onDraw(c);
                holder.unlockCanvasAndPost(c);

            }

        }
        public void onDraw(Canvas canvas){
            canvas.drawARGB(255, 18, 34, 94);
            canvas.drawBitmap(background, 0, 0, null);
            newInt=sprite.onDraw(canvas);

            // When character is in a room, start the qr scanner activity and pass the next riddle number to it
                if (newInt != 0)
                {
                    Intent itns = new Intent(getApplicationContext(), QrCodeScanner.class);
                    itns.putExtra(nextApp, newInt);
                    startActivity(itns);
                }
        }

        public void pause(){
            isItok = false;
            while(true){
                try{
                    t.join();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                break;
            }
        }
        //EDW KSEKINAEI NEO THREAD KATHE FORA
        public void resume() throws InterruptedException {
            isItok = true;
            t=new Thread(this);
            //This sleep is needed beacause switch activity takes time
            Thread.sleep(1000);
            t.start();
        }

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