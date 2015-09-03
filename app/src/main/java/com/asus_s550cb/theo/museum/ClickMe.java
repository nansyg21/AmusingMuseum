package com.asus_s550cb.theo.museum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;


public class ClickMe extends Activity {

    int currentApiVersion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);//hide status bar
        setContentView(new ClickMeScreen(this));

        // ------------------ Code in order to hide the navigation bar -------------------- //
        // The navigation bar is hiden and comes up only if the user swipes down the status bar
        currentApiVersion = Build.VERSION.SDK_INT; //get the current api

        // Initialize flags for full screen and hide navitation bar, immersive approach
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // This work only for android 4.4+
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT)
        {

            getWindow().getDecorView().setSystemUiVisibility(flags);

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            final View decorView = getWindow().getDecorView();
            decorView
                    .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
                    {

                        @Override
                        public void onSystemUiVisibilityChange(int visibility)
                        {
                            if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                            {
                                decorView.setSystemUiVisibility(flags);
                            }
                        }
                    });
        }

        //Start help screen
        Intent itn= new Intent(getApplicationContext(), HelpDialogActivity.class);
        itn.putExtra("appNum",3);
        startActivity(itn);

    }

    // Reset the flags to hide the navigation bar
    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus)
        {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }


    //#  #0B0075 blue royal   pio skoteinh apoxrosh :#080054
    //This is actually drawing on screen the game
    public class ClickMeScreen extends View implements Runnable
    {
        int ScreenWidth,ScreenHeight;
        int PlayerTouchX, PlayerTouchY;
        MediaPlayer click_me_hit_sound ;
        int hits=0;
        Paint backgroundPaint, txtPaint, missedImagePaint;
        Bitmap backImg;
        Date dt= new Date();
        long startTime, respawn_timer=3000, lowerTimeLimit=500, deltaTime=500;//On select moving image the time is decreased, from 3 seconds to 0.5
        long elapsedTime = 0L;                                                // from 0.5 it goes to 3 again
        Random rand= new Random();

        int M=5, N=5;                         // M(HEIGHT-rows) x N(WIDTH-columns) images
        ArrayList<Bitmap> croppedImages = new ArrayList<Bitmap>();      //each bitmap is a small piece of the whole image
        ArrayList<Rect> croppedOriginalRects = new ArrayList<Rect>();
        boolean[] selectedList = new boolean[M*N];
        int croppedWidth,croppedHeight;     //percentage based on the loaded image      -- create image
        int imgWidth,imgHeight;             //percentage based on the screen dimensions -- draw image
        int movingImgWidth,movingImgHeight; //moving image is bigger than usual for easy pick

        Bitmap movingImg;          //this object is moving
        Rect movingImgRect;
        int nextIndex=0;           //index from cropped images we read next

        boolean done=false;     //on true all pieces appear on screen for doneTimer milliseconds
        long doneTimer=5000;

        public ClickMeScreen(Context context)
        {
            super(context);
            click_me_hit_sound= MediaPlayer.create(this.getContext(), R.raw.click_me_hit_sound);

            //get screen size
            Point size = new Point();
          //  getWindowManager().getDefaultDisplay().getSize(size);
          //  ScreenWidth=size.x;
          //  ScreenHeight=size.y;

            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            ScreenHeight = displaymetrics.heightPixels;
            ScreenWidth = displaymetrics.widthPixels;

            //for background color
            backgroundPaint = new Paint();
            backgroundPaint.setStyle(Paint.Style.FILL);
            backgroundPaint.setColor(getResources().getColor(R.color.royal_blue));//background: dark blue royal

            //color for missed images
            missedImagePaint = new Paint();
            missedImagePaint.setStyle(Paint.Style.FILL);
            missedImagePaint.setAlpha(100);

            //for text color
            txtPaint = new Paint();
            txtPaint.setStyle(Paint.Style.FILL);
            txtPaint.setColor(Color.WHITE);//white
            txtPaint.setTextSize(getResources().getDimension(R.dimen.click_me_text_size));


            //back image
            backImg = BitmapFactory.decodeResource(getResources(), R.drawable.click_me_prize);  // no rescale: drawable-nodpi
            croppedWidth= backImg.getWidth()/N;
            croppedHeight= backImg.getHeight()/M;


            //Moving Image
            imgWidth=ScreenWidth/N;
            imgHeight=ScreenHeight/M;
            movingImgWidth=ScreenWidth/N;
            movingImgHeight=ScreenHeight/M;

            CropImage();
            setCroppedOriginalRects(); //calculate where each image should be
            ShuffleImages();
            changeMovingImage();
            startTime = System.currentTimeMillis();

        }

        public void CropImage()
        {
            for(int i=0;i<M;i++)         //M(HEIGHT-rows-i) x N(WIDTH-columns-j)   --- here width and height work different from Rect
                for (int j = 0; j < N; j++)
                    croppedImages.add(Bitmap.createBitmap(backImg, j*croppedWidth, i*croppedHeight,
                            croppedWidth,  croppedHeight) );

        }

        public void setCroppedOriginalRects()
        {
            int padding=5;
            for(int i=0;i<M;i++)        //M(HEIGHT-rows-i) x N(WIDTH-columns-j)
                for(int j=0;j<N;j++)
                {
                    croppedOriginalRects.add(new Rect(j*imgWidth, i*imgHeight, j*imgWidth+imgWidth+padding, i*imgHeight+imgHeight+padding));
                }
        }

        public void ShuffleImages()
        {
            int index;
            Bitmap tempBmp;
            Rect tempRect;
            for (int i = croppedImages.size() - 1; i > 0; i--)
            {
                index = rand.nextInt(i + 1);
                // Swap images
                tempBmp = croppedImages.get(index);
                croppedImages.set(index,croppedImages.get(i));
                croppedImages.set(i, tempBmp);

                // Swap original rects
                tempRect=croppedOriginalRects.get(index);
                croppedOriginalRects.set(index,croppedOriginalRects.get(i));
                croppedOriginalRects.set(i, tempRect);
            }
        }

        public void changeMovingImage()
        {
            movingImg= croppedImages.get(nextIndex);

            int x =rand.nextInt(ScreenWidth-movingImgWidth);  //3*.. because of navigation bar
            int y =rand.nextInt(ScreenHeight-movingImgHeight);
            movingImgRect = new Rect(x, y, x+movingImgWidth, y+movingImgHeight);

            nextIndex++;
            Log.w("Warn", "nextIndex:"+nextIndex);    //mini game almost completed

            if(nextIndex>=croppedImages.size())
            {
                Log.w("Warn", "No more images");    //mini game almost completed
                done=true;
                startTime= System.currentTimeMillis();  //restart counter
                elapsedTime=0L;
            }

        }

        public boolean TouchedImage()
        {
            if( movingImgRect.contains(PlayerTouchX,PlayerTouchY) )
                return true;
            return false;
        }

        @Override
        public void run()
        {   // Update state of what we draw

            elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime >= respawn_timer && !done)          //still playing
            {
                selectedList[nextIndex]=false;  //image wasn't selected
                changeMovingImage();
                startTime= System.currentTimeMillis();
                elapsedTime=0L;
            }

            if(done && elapsedTime>=doneTimer)                   //done playing
            {
                QrCodeScanner.questionMode=true;
                finish();
                //TODO Calculate score
               // Log.w("Warn","Click Me exits..");       //click me exits from here
            }

            // onDraw(Canvas) will be called
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            super.onDraw(canvas);

            //Draw color on background
            canvas.drawPaint(backgroundPaint);


            if(!done)            // Draw img
                canvas.drawBitmap(movingImg, null, movingImgRect, null);
            else               // Draw all images together
                for (int i = 0; i < croppedImages.size(); i++)
                    if(selectedList[i])
                        canvas.drawBitmap(croppedImages.get(i), null, croppedOriginalRects.get(i), null);
                    else
                        canvas.drawBitmap(croppedImages.get(i), null, croppedOriginalRects.get(i), missedImagePaint);


            //Draw hits
           // canvas.drawText(hits+"/"+maxHits, 50, 50, txtPaint );


            // Invalidate view at about 60fps
            postDelayed(this, 16);
        }



        @Override
        public boolean onTouchEvent(MotionEvent ev)
        {
            if(ev.getAction()== MotionEvent.ACTION_DOWN)
            {
                    final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                    final float x = MotionEventCompat.getX(ev, pointerIndex);
                    final float y = MotionEventCompat.getY(ev, pointerIndex);

                    PlayerTouchX = (int) x;
                    PlayerTouchY = (int) y;

                    if(TouchedImage() && !done)
                    {
                        click_me_hit_sound.start();
                        hits++;
                        selectedList[nextIndex]=true;   //successfully selected image
                        changeMovingImage();
                        startTime = System.currentTimeMillis();
                        elapsedTime = 0L;

                        if (respawn_timer > lowerTimeLimit)
                            respawn_timer -= deltaTime;
                        else
                            respawn_timer=3000;     //if times reaches 0.5 it goes to 3 again
                    }
            }
            return true;
        }
    }
}
