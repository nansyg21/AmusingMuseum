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
import android.widget.Button;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;


public class ClickMe extends Activity {

    int currentApiVersion;
    PauseMenuButton pauseBt;

    int screenWidth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);//hide status bar
        setContentView(new ClickMeScreen(this));

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenWidth = displaymetrics.widthPixels;
        pauseBt=new PauseMenuButton(screenWidth,this);


        // ------------------ Code in order to hide the navigation bar -------------------- //
        menu.hideNavBar(this.getWindow());

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
        menu.hideNavBar(this.getWindow());
    }


    //#  #0B0075 blue royal   pio skoteinh apoxrosh :#080054
    //This is actually drawing on screen the game
    public class ClickMeScreen extends View implements Runnable
    {
        int ScreenWidth,ScreenHeight;
        int PlayerTouchX, PlayerTouchY;
        MediaPlayer click_me_hit_sound ;
        int hits;
        Paint backgroundPaint, txtPaint, missedImagePaint;
        Bitmap backImg;

        long startTime, respawn_timer, lowerTimeLimit, deltaTime;//On select moving image the time is decreased, from 3 seconds to 0.5
        long elapsedTime ;                                                // from 0.5 it goes to 3 again
        Random rand;

        int M=5, N=5;                         // M(HEIGHT-rows) x N(WIDTH-columns) images
        ArrayList<Bitmap> croppedImages;      //each bitmap is a small piece of the whole image
        ArrayList<Rect> croppedOriginalRects ;
        boolean[] selectedList;
        int croppedWidth,croppedHeight;     //percentage based on the loaded image      -- create image
        int imgWidth,imgHeight;             //percentage based on the screen dimensions -- draw image
        int movingImgWidth,movingImgHeight; //moving image is bigger than usual for easy pick

        Bitmap movingImg;          //this object is moving
        Rect movingImgRect;
        int nextIndex;           //index from cropped images we read next

        boolean done, leave;     //on true all pieces appear on screen for doneTimer milliseconds
        long doneTimer;
        Button invBtn;          //invisible button: triggered on exit to finish mini game
        public ClickMeScreen(Context context)
        {
            super(context);
            click_me_hit_sound= MediaPlayer.create(this.getContext(), R.raw.click_me_hit_sound);
            respawn_timer=3000; lowerTimeLimit=500; deltaTime=500;
            elapsedTime = 0L;
            hits=0;
            rand= new Random();
            croppedImages = new ArrayList<Bitmap>();      //each bitmap is a small piece of the whole image
            croppedOriginalRects = new ArrayList<Rect>();
            selectedList = new boolean[M*N];
            nextIndex=0;
            done=false; //on true all pieces appear on screen for doneTimer milliseconds
            leave=false;
            doneTimer=3000;
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

            invBtn = new Button(getContext());
            invBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.w("Warn","Clicked button");
                    LeaveClickMe();
                }
            });


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

        public void LeaveClickMe() {

             //Save and Show Score
             int corrects=0;
             for(boolean b: selectedList)
                if(b) corrects++;

            Score.currentRiddleScore= (int) Math.ceil( corrects*2.8) ;
            Intent itn= new Intent(getApplicationContext(), Score.class);
            startActivity(itn);


            QrCodeScanner.questionMode=true;
            finish();

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

            if(done && elapsedTime>=doneTimer&& !leave)                   //done playing
            {
               leave=true;
                invBtn.performClick();
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

            pauseBt.getPauseMenuButton().draw(canvas);


            if(!done)            // Draw img
                canvas.drawBitmap(movingImg, null, movingImgRect, null);
            else               // Draw all images together
            {
                for (int i = 0; i < croppedImages.size(); i++)
                    if (selectedList[i])
                        canvas.drawBitmap(croppedImages.get(i), null, croppedOriginalRects.get(i), null);
                    else
                        canvas.drawBitmap(croppedImages.get(i), null, croppedOriginalRects.get(i), missedImagePaint);

            }


            // Invalidate view at about 60fps
            postDelayed(this, 16);
        }



        @Override
        public boolean onTouchEvent(MotionEvent ev)
        {

            // Check if pause button is hit
            if(ev.getAction()== MotionEvent.ACTION_DOWN)
            {
                float touchX = ev.getX();
                float touchY = ev.getY();

                if(pauseBt.getRect().contains((int)touchX,(int)touchY))
                {
                    if(ev.getAction()==MotionEvent.ACTION_DOWN) {
                        Intent itn;
                        itn = new Intent(getApplicationContext(), PauseMenuActivity.class);
                        startActivity(itn);
                    }

                }



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

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_exit_small)
                .setMessage(R.string.confirm_exit_large)
                .setNegativeButton(R.string.confirm_exit_cancel, null)
                .setPositiveButton(R.string.confirm_exit_οκ, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1)
                    {
                        System.exit(0);
                    }
                }).create().show();
    }
}
