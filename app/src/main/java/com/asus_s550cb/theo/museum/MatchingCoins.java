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
import java.util.Random;


public class MatchingCoins extends Activity {

    int currentApiVersion;

    PauseMenuButton pauseBt;

    int screenWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);//hide status bar
        setContentView(new MatcingCoinsScreen(this));

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenWidth = displaymetrics.widthPixels;
        pauseBt=new PauseMenuButton(screenWidth,this);

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
        itn.putExtra("appNum",5);
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



    //This is actually drawing on screen the game : Matching Coins
    //A couple of pictures appear for each coin. The player matches the coins together
    public class MatcingCoinsScreen extends View implements Runnable
    {
        int ScreenWidth,ScreenHeight;
        int PlayerTouchX, PlayerTouchY;
        int mPosX, mPosY;   //coordinates in the middle on dragging

        MediaPlayer correct_match_sound ;

        Paint backgroundPaint, linePaint;
        Bitmap frameImg;
        ArrayList<Bitmap> coinsList= new ArrayList<Bitmap>();//1 matches with 2, 3 with 4 etc
        Rect frame1Rect,frame2Rect;
        ArrayList<Rect> coinsRectList= new ArrayList<Rect>();
        int frameWidth,frameHeight, imgWidth,imgHeight;     //sizes of frames and coins
        Random rand = new Random();

        boolean movingSomething=false;
        Rect currentMovingCoin;
        int currentMovingCoinId; //index of rect coin object

        int leftCoin,rightCoin;  //id

        boolean wave2=false;    //true once wave 1 is done

        private int mActivePointerId = -1;      //-1 instead of INVALID_POINTER_ID

        public MatcingCoinsScreen(Context context)
        {
            super(context);

            correct_match_sound= MediaPlayer.create(this.getContext(), R.raw.correct_match_sound);

            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            ScreenHeight = displaymetrics.heightPixels;
            ScreenWidth = displaymetrics.widthPixels;

            //for background color
            backgroundPaint = new Paint();
            backgroundPaint.setStyle(Paint.Style.FILL);
            backgroundPaint.setColor(Color.parseColor("#8F0014"));//background: bussini

            //Paint for line
            linePaint = new Paint();
            linePaint.setStyle(Paint.Style.FILL);
            linePaint.setColor(Color.parseColor("#ff0000"));//background: dark blue royal

            //frame image
            frameWidth=ScreenWidth/5;
            frameHeight=frameWidth;
            frameImg = BitmapFactory.decodeResource(getResources(), R.drawable.match_frame);
            frame1Rect= new Rect(ScreenWidth/2-frameWidth,ScreenHeight/2-frameHeight/2,
                    ScreenWidth/2-frameWidth+frameWidth,ScreenHeight/2-frameHeight/2+frameHeight);


            frame2Rect= new Rect(ScreenWidth/2,frame1Rect.top, ScreenWidth/2+frameWidth,frame1Rect.bottom);


            //coin images
            imgWidth=ScreenWidth/6;
            imgHeight=imgWidth;

            InitializeCoinImages("1"); //call wave 1

            leftCoin=-1;
            rightCoin=-1;

        }

        public void InitializeCoinImages(String wave) {

            coinsList.clear();
            if(wave.equals("1"))
            {
                coinsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_1));
                coinsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_2));
                coinsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_3));
                coinsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_4));
                coinsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_5));
                coinsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_6));
                coinsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_7));
                coinsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_8));
            }
            else if (wave.equals("2"))
            {
                coinsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_9));
                coinsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_10));
                coinsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_11));
                coinsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_12));
                coinsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_13));
                coinsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_14));
                coinsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_15));
                coinsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_16));
            }
            else
                Log.w("Warn","Wrong wave called.");
            InitializeCoinRects();

        }

        public void InitializeCoinRects() {
            coinsRectList.clear();
            int x,y;

            for(int i=0;i<coinsList.size()/2;i++)
            {
                //left side
                x = rand.nextInt(ScreenWidth / 2 - imgWidth);
                y = rand.nextInt(ScreenHeight - imgHeight);
                coinsRectList.add( new Rect(x, y, x + imgWidth, y + imgHeight));

                //right side
                x = rand.nextInt(ScreenWidth / 2 - imgWidth) + ScreenWidth / 2;
                y = rand.nextInt(ScreenHeight - imgHeight);
                coinsRectList.add( new Rect(x, y, x + imgWidth, y + imgHeight));
            }


        }


        public void DroppedPiece() //Player was dragging a piece, check if its near the original position
        {

            Log.w("Warn","Left:"+leftCoin+" right:"+rightCoin);

            //frame 1
            if( currentMovingCoin.intersect(new Rect( frame1Rect.left,frame1Rect.top,
                    frame1Rect.right-50,frame1Rect.bottom-50)) && leftCoin==-1)
            {
                currentMovingCoin.left = frame1Rect.left; //assign by value
                currentMovingCoin.top = frame1Rect.top;
                currentMovingCoin.right = frame1Rect.right;
                currentMovingCoin.bottom = frame1Rect.bottom;

                leftCoin=currentMovingCoinId;
            }
            else if(currentMovingCoinId%2==0)   //coin removed from frame1
                leftCoin=-1;

            //frame 2
            if( currentMovingCoin.intersect(new Rect( frame2Rect.left,frame2Rect.top,
                    frame2Rect.right-50,frame2Rect.bottom-50))&& rightCoin==-1)
            {
                currentMovingCoin.left = frame2Rect.left; //assign by value
                currentMovingCoin.top = frame2Rect.top;
                currentMovingCoin.right = frame2Rect.right;
                currentMovingCoin.bottom = frame2Rect.bottom;

                rightCoin=currentMovingCoinId;
            }
            else if(currentMovingCoinId%2==1) //coin removed from frame2
                rightCoin=-1;

            CheckMatch();

        }

        public void CheckMatch()
        {
            if(leftCoin+1==rightCoin)   //0 matches with 1 , 2 with 3 etc..
            {

                correct_match_sound.start();
                coinsList.remove(rightCoin);
                coinsList.remove(leftCoin);
                coinsRectList.remove(rightCoin);
                coinsRectList.remove(leftCoin);


                if(coinsList.size()==0)
                {
                    InitializeCoinImages("2");  //call wave 2

                    if(wave2)
                    {
                        //Save and Show Score
                        Score.setRiddleScore(70) ;//some score
                        Intent itn= new Intent(getApplicationContext(), Score.class);
                        startActivity(itn);

                        QrCodeScanner.questionMode=true;
                        finish();// Log.w("Warn","Done!"); //Game exits from here
                    }
                    else
                        wave2=true;
                }


                Log.w("Warn", "still " + coinsList.size() + " coins");
                leftCoin=-1;
                rightCoin=-1;
                currentMovingCoin=null;
                currentMovingCoinId=-1;
            }


        }

        public boolean CheckCollision() //finds the piece the user selected: returns true
        {
            for(int i=0;i<coinsRectList.size();i++)
                if( coinsRectList.get(i).contains(PlayerTouchX, PlayerTouchY))//cant move correctly places pieces
                {
                    currentMovingCoin=coinsRectList.get(i);
                    currentMovingCoinId=i;

                    return true;
                }
            return false;
        }


        @Override
        public void run()
        {   // Update state of what we draw

            // onDraw(Canvas) will be called
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            super.onDraw(canvas);

            //Draw color on background
            canvas.drawPaint(backgroundPaint);

            //draw line in the middle
            canvas.drawLine(ScreenWidth / 2, 0, ScreenWidth / 2, ScreenHeight, linePaint);

            //draw 2 frames
            canvas.drawBitmap(frameImg,null,frame1Rect,null);
            canvas.drawBitmap(frameImg,null,frame2Rect,null);

            // Draw coins
            for(int i=0;i<coinsList.size();i++)
                canvas.drawBitmap(coinsList.get(i), null, coinsRectList.get(i), null);

            pauseBt.getPauseMenuButton().draw(canvas);



            // Invalidate view at about 60fps
            postDelayed(this, 16);
        }



        @Override
        public boolean onTouchEvent(MotionEvent ev)
        {

            switch (ev.getAction())
            {
                case MotionEvent.ACTION_DOWN:       //New touch started
                {


                    //Check if pause button is hit
                        float touchX = ev.getX();
                        float touchY = ev.getY();

                        if(pauseBt.getRect().contains((int)touchX,(int)touchY)) {
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

                    if(!movingSomething && CheckCollision())    //CheckCollision:true:selected piece  false: selected empty
                        movingSomething = true;

                    break;
                }

                case MotionEvent.ACTION_MOVE:       //Finger is moving
                {
                    if(movingSomething)
                    {
                        //  Log.w("Warn","Dragging...");
                        // Find the index of the active pointer and fetch its position
                        final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);

                        final float x = MotionEventCompat.getX(ev, pointerIndex);
                        final float y = MotionEventCompat.getY(ev, pointerIndex);

                        // Calculate the distance moved
                        final float dx = x - PlayerTouchX;
                        final float dy = y - PlayerTouchY;

                        mPosX += dx;
                        mPosY += dy;

                        invalidate();

                        // Remember this touch position for the next move event
                        PlayerTouchX = (int) x;
                        PlayerTouchY = (int) y;

                        if(currentMovingCoin!=null && PlayerTouchX+imgWidth<ScreenWidth && PlayerTouchY+imgHeight<ScreenHeight)
                            if(currentMovingCoinId%2==0 && PlayerTouchX+imgWidth<ScreenWidth/2) //cant move a left piece from the right side
                            {
                                currentMovingCoin.left = PlayerTouchX;
                                currentMovingCoin.top = PlayerTouchY;
                                currentMovingCoin.right = PlayerTouchX + imgWidth;
                                currentMovingCoin.bottom = PlayerTouchY + imgHeight;
                            }
                            else if(currentMovingCoinId%2==1 && PlayerTouchX>ScreenWidth/2) //cant move a right piece from the left side
                            {
                                currentMovingCoin.left = PlayerTouchX;
                                currentMovingCoin.top = PlayerTouchY;
                                currentMovingCoin.right = PlayerTouchX + imgWidth;
                                currentMovingCoin.bottom = PlayerTouchY + imgHeight;
                            }


                      //  else
                         //   Log.w("Warn","currentMovingPiece IS NULL");
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:     //Finger left screen
                {
                    if(movingSomething)
                    {
                        DroppedPiece();

                        movingSomething = false;
                    }

                    break;
                }
                case MotionEvent.ACTION_CANCEL: //Current event has been canceled, something else took control of the touch event
                {

                    movingSomething=false;
                    break;
                }
                default:            //whatever happens
                {

                    movingSomething=false;
                    break;
                }

            }

            return true;

        }

    }


}