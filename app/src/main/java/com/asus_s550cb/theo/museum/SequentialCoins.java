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
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by panos on 3/3/2016.
 */
public class SequentialCoins extends Activity {
    PauseMenuButton pauseBt;

    int screenWidth, focusCounter =0;
    SequentialCoinsScreen sequentialCoinsScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);//hide status bar
        sequentialCoinsScreen = new SequentialCoinsScreen(this);
        setContentView(sequentialCoinsScreen);

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
        // Log.w("Warn", "FOCUSED: " + focusCounter);
        if(focusCounter==0)         //start game after returning from help, when counter is 0 (first time focused)
            sequentialCoinsScreen.StartGame();
        focusCounter++;

    }


    //This is actually drawing on screen the game : Sequential Coins
    //A couple of pictures appear for each stamp. The player matches the stamps together
    public class SequentialCoinsScreen extends View implements Runnable {
        int ScreenWidth, ScreenHeight, PlayerTouchX, PlayerTouchY, mPosX, mPosY, imgWidth, imgHeight, widthGap, heightGap;
        int ROWS=2, COLUMNS =5,selectedCoin, currentIlluminateRound,currentPlayRound, stateRounds =3; //starting with 3 rounds and increasing
        int[] correctCoins;                 //coins chosen randomly
        int currentRound=0, TotalRounds=7, mistakes=0;   // 0<= currentRound <=TotalRounds
        Paint backgroundPaint, overlayPaint;
        ColorFilter filter;
        Bitmap lightImg;                    //draw this on top of an image to make it look like it is illuminated
        Random rand = new Random();
        Boolean drawingSelectedEffectOnCoin =false,illuminateState=false, playState=false;              //states about coins
        ArrayList<Bitmap> coinImagesList = new ArrayList<Bitmap>();
        ArrayList<Rect> coinRectsList = new ArrayList<Rect>();

        //timers
        long elapsedEffectTimer, effectTimer, effectTimerLimit=250; //for pressed coin effect
        long delayTimer, elapsedDelayTimer, delayBetween=2000;      //for switching lightening coins
        long delayBetweenRounds=2000;
        private int mActivePointerId = -1;      //-1 instead of INVALID_POINTER_ID

        public SequentialCoinsScreen(Context context) {
            super(context);
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            ScreenHeight = displaymetrics.heightPixels;
            ScreenWidth = displaymetrics.widthPixels;

            //for background color
            backgroundPaint = new Paint();
            backgroundPaint.setStyle(Paint.Style.FILL);
            backgroundPaint.setColor(Color.parseColor("#8F0014"));//background: bussini

            //for selected effect on selected coin
            overlayPaint = new Paint();
            filter = new LightingColorFilter(Color.GRAY,1);
            overlayPaint.setColorFilter(filter);

            //coins images
            imgWidth = ScreenWidth / 6;
            imgHeight = imgWidth;
            widthGap=10;
            heightGap=30;

            InitializeImages();
            InitializeRects();
            // Log.w("Warn", "Total: " + coinImagesList.size());
        }

        public void StartGame() {
            if(++currentRound > TotalRounds)
                LeaveSequentialCoins();
            else {
                // Log.w("Warn", "currentRound is: " + currentRound);
                illuminateState = false; //nothing is illuminated and nothing can be selected until the game starts
                playState = false;
                CountDownTimer countDownTimer = new CountDownTimer(delayBetweenRounds, 1000) {
                    public void onTick(long millisUntilFinished) {
                    }

                    public void onFinish() {//  after a few seconds have passed the game starts
                        illuminateState = true;
                        playState = false;
                        PrepareIlluminateState();
                    }
                }.start();
            }
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

            lightImg = BitmapFactory.decodeResource(getResources(), R.drawable.sclight);
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
            coinRectsList.add(new Rect(getLastRect().right + widthGap, getLastRect().top, getLastRect().right + widthGap + imgWidth, getLastRect().bottom));

            //second row of coins
            coinRectsList.add(new Rect(getUpperRect().left, getUpperRect().bottom + heightGap, getUpperRect().right, getUpperRect().bottom + heightGap + imgHeight));
            coinRectsList.add(new Rect(getUpperRect().left, getUpperRect().bottom + heightGap, getUpperRect().right, getUpperRect().bottom + heightGap + imgHeight));
            coinRectsList.add(new Rect(getUpperRect().left, getUpperRect().bottom+heightGap,getUpperRect().right,getUpperRect().bottom+heightGap+imgHeight));
            coinRectsList.add(new Rect(getUpperRect().left, getUpperRect().bottom+heightGap,getUpperRect().right,getUpperRect().bottom+heightGap+imgHeight));
            coinRectsList.add(new Rect(getUpperRect().left, getUpperRect().bottom + heightGap, getUpperRect().right, getUpperRect().bottom + heightGap + imgHeight));
        }

        public Rect getLastRect(){
            //Returns last inserted rect
            return coinRectsList.get(coinRectsList.size()-1);
        }

        public Rect getUpperRect(){
            //Returns the rect located in the same position on the upper row
            return coinRectsList.get(coinRectsList.size()- COLUMNS);
        }

        public void PrepareIlluminateState() {          //chose some coins randomly and start counting time
            correctCoins = new int[stateRounds];
            for(int i =0; i< stateRounds;i++ )
            {
                correctCoins[i]= rand.nextInt(ROWS* COLUMNS -1);
                // Log.w("Warn","At index " +i+ ": "+correctCoins[i]);
            }
            currentIlluminateRound =0;                         //start by drawing light on this coin from correctCoins array
            SoundHandler.PlaySound(SoundHandler.beep_sound_id3);
            delayTimer=System.currentTimeMillis();
        }

        public Boolean SelectedSomeCoin() {             //find whether the user has selected any coin or not
            for(int i=0;i< coinRectsList.size();i++)
                if( coinRectsList.get(i).contains(PlayerTouchX, PlayerTouchY)) {
                    selectedCoin = i;
                    drawingSelectedEffectOnCoin =true;
                    effectTimer=System.currentTimeMillis();
                    return true;
                }
            return false;

        }

        public void LeaveSequentialCoins()
        {
            //Save and Show Score
            Score.currentRiddleScore= (int) Math.ceil( 70- mistakes*10) ;
            Intent itn= new Intent(getApplicationContext(), Score.class);
            startActivity(itn);

            QrCodeScanner.questionMode=true;
            finish();
        }

        @Override
        public void run() {   // Update state of what we draw
            if(illuminateState)
            {
                elapsedDelayTimer = System.currentTimeMillis() - delayTimer;
                if(elapsedDelayTimer>delayBetween)          //update coin and restart clock
                {
                    currentIlluminateRound++;
                    if(currentIlluminateRound >=stateRounds)       //switch states if done
                    {
                        playState=true;
                        illuminateState=false;
                        currentPlayRound=0;     //user must start by selecting this index from correctCoins array
                    }
                    else
                        SoundHandler.PlaySound(SoundHandler.beep_sound_id3);
                    delayTimer= System.currentTimeMillis();
                }
            }

            if(drawingSelectedEffectOnCoin)     //draw selected effect on coin for a short period
                if( (elapsedEffectTimer= System.currentTimeMillis()-effectTimer) > effectTimerLimit)
                    drawingSelectedEffectOnCoin=false;

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

            //draw selected effect on coin if needed
            if(drawingSelectedEffectOnCoin)
                canvas.drawBitmap(coinImagesList.get(selectedCoin), null, coinRectsList.get(selectedCoin), overlayPaint);

            //draw light if necessary
            if(illuminateState) {
                canvas.drawBitmap(lightImg, null, coinRectsList.get(correctCoins[currentIlluminateRound]), null);
            }

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

                    if(playState && !illuminateState && SelectedSomeCoin())    //CheckCollision:true:selected piece  false: selected empty
                    {
                        if(selectedCoin== correctCoins[currentPlayRound])
                        {
                            // Log.w("Warn", "CORRECT: " + selectedCoin);
                            SoundHandler.PlaySound(SoundHandler.correct_sound_id3);

                            filter = new LightingColorFilter(Color.GREEN,1);    //on correct coin is green
                            overlayPaint.setColorFilter(filter);

                            currentPlayRound++;
                            if(currentPlayRound>=stateRounds)//all correct: one more round with one extra coin to choose
                            {
                                stateRounds++;               //on the next round we will have one more coin to select
                                StartGame();
                            }
                        }
                        else{
                            //On one wrong choice, game restarts from the beginning
                            //Log.w("Warn", "WRONG: Selected:" + selectedCoin +" correct was: "+(correctCoins[currentPlayRound]));
                            mistakes++;
                            SoundHandler.PlaySound(SoundHandler.wrong_sound_id4);

                            filter = new LightingColorFilter(Color.RED,1);//on wrong coin is red
                            overlayPaint.setColorFilter(filter);

                            stateRounds=3;
                            StartGame();
                        }
                    }

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



