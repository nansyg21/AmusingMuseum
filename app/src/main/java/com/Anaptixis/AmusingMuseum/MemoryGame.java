package com.Anaptixis.AmusingMuseum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by panos on 15/12/2015.
 *
 * 16 cards appear on screen
 * for a few seconds we see the front side, then they turn around
 * we pick pairs of two by selecting them
 * if the pair is correct, the front side is show until the end
 * if not, it turns around after a few milliseconds
 * Once all pairs are done, score is calculated and the screen exits
 */
public class MemoryGame extends Activity {

    int currentApiVersion;
    PauseMenuButton pauseBt;

    int screenWidth, screenHeight;
    MemoryGameScreen memoryGameScreen;

    static int onFocusChangedCounterMemoryGame =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);//hide status bar
        memoryGameScreen = new MemoryGameScreen(this);
        setContentView(memoryGameScreen);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenWidth = displaymetrics.widthPixels;
        screenHeight =displaymetrics.heightPixels;
        pauseBt = new PauseMenuButton(screenWidth, this);


        // ------------------ Code in order to hide the navigation bar -------------------- //
        menu.hideNavBar(this.getWindow());

        //Start help screen
        Intent itn = new Intent(getApplicationContext(), HelpDialogActivity.class);
        itn.putExtra("appNum", 9);
        startActivity(itn);

        //Change and store the question Mode
        QrCodeScanner.questionMode=true;
        QrCodeScanner.storeQuestionMode(true);
    }

    // Reset the flags to hide the navigation bar
    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        menu.hideNavBar(this.getWindow());

        if(onFocusChangedCounterMemoryGame ==0)    //On return from HelpDialogActivity Screen this method is triggered
                                        //it is also triggered any other time the app changes focus so we restart the game only once
        {
           // Log.w("Warn", "Returned!");
            onFocusChangedCounterMemoryGame++;
            memoryGameScreen.StartGame();
        }
    }

    //This is actually drawing on screen the game
    public class MemoryGameScreen extends View implements Runnable {

        int PlayerTouchX, PlayerTouchY;
        MediaPlayer memory_game_pair_sound;
        int pairs, M = 4, N = 4, imgWidth, imgHeight, startingX, startingY, GapX, GapY,currentSelectedRectIndex1,currentSelectedRectIndex2, userClicks;
        Paint backgroundPaint;
        Bitmap backSideImg;

        long noSelectTimer, elapsedNoSelectTime, limitNoSelectTime; //timer to freeze game between 2 selections
        long showAllImagesTimer, elapsedShowAllImagesTime, limitShowAllImagesTime; //timer for showing images on start

        ArrayList<MemoryGameCard> cardList = new ArrayList<MemoryGameCard>();
        ArrayList<Rect> rectList = new ArrayList<Rect>();

        Boolean canSelectState, showCardsState;

        public MemoryGameScreen(Context context) {
            super(context);

            //for background color
            backgroundPaint = new Paint();
            backgroundPaint.setStyle(Paint.Style.FILL);
            backgroundPaint.setColor(getResources().getColor(R.color.neutral_beize));

            //back image
            backSideImg = BitmapFactory.decodeResource(getResources(), R.drawable.mg_back);  // no rescale: drawable-nodpi

            DisplayMetrics displaymetrics = new DisplayMetrics();   //for some reason we have to re-read screen resolution
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            screenWidth = displaymetrics.widthPixels;
            screenHeight =displaymetrics.heightPixels;

            imgWidth = screenWidth / (N+1); //split by 5:  4 for each image 1 for free space
            imgHeight = screenHeight / (M+1);

            startingX= (screenWidth / (N+1))/3; //1/3 space from the left   1/3 space from the right  1/3 in between images
            startingY= (screenHeight / (M+1))/3;

            GapX = ((screenWidth / (N+1))/3)/(N-1);  //for N images we have N-1 gaps
            GapY = ((screenHeight / (M+1))/3)/(M-1);
           // Log.w("Warn","screenWidth:"+screenWidth+" screenHeight:"+screenHeight+ "  imgWidth:"+imgWidth +" imgHeight:"+imgHeight+"  stX:"+startingY+" stY:"+startingY+" gapX"+GapX+" gapY:"+GapY);

            loadCards();
            setPairsDeselected();

            limitNoSelectTime = 1000; //1 second
            limitShowAllImagesTime=5000;
            pairs = 0;
            userClicks =0; //how many images has the user selected, for score purposes

            showAllImagesTimer=System.currentTimeMillis();
            showCardsState=true;
            canSelectState=false;
            currentSelectedRectIndex1=-1;   //on start nothing is selected
            currentSelectedRectIndex2=-1;
        }

        public void StartGame(){
            showAllImagesTimer=System.currentTimeMillis();
            showCardsState=true;
            canSelectState=false;
        }

        public void loadCards(){    //1)create cards   2)create rects   3)shuffle rects   4)assign rects to cards
            cardList.add(new MemoryGameCard( BitmapFactory.decodeResource(getResources(), R.drawable.mg1),null,1));
            cardList.add(new MemoryGameCard( BitmapFactory.decodeResource(getResources(), R.drawable.mg2),null,2));
            cardList.add(new MemoryGameCard( BitmapFactory.decodeResource(getResources(), R.drawable.mg3),null,3));
            cardList.add(new MemoryGameCard( BitmapFactory.decodeResource(getResources(), R.drawable.mg4),null,4));
            cardList.add(new MemoryGameCard( BitmapFactory.decodeResource(getResources(), R.drawable.mg5),null,5));
            cardList.add(new MemoryGameCard( BitmapFactory.decodeResource(getResources(), R.drawable.mg6),null,6));
            cardList.add(new MemoryGameCard( BitmapFactory.decodeResource(getResources(), R.drawable.mg7),null,7));
            cardList.add(new MemoryGameCard( BitmapFactory.decodeResource(getResources(), R.drawable.mg8),null,8));

            cardList.add(new MemoryGameCard( BitmapFactory.decodeResource(getResources(), R.drawable.mg1),null,1));
            cardList.add(new MemoryGameCard( BitmapFactory.decodeResource(getResources(), R.drawable.mg2),null,2));
            cardList.add(new MemoryGameCard( BitmapFactory.decodeResource(getResources(), R.drawable.mg3),null,3));
            cardList.add(new MemoryGameCard( BitmapFactory.decodeResource(getResources(), R.drawable.mg4),null,4));
            cardList.add(new MemoryGameCard( BitmapFactory.decodeResource(getResources(), R.drawable.mg5),null,5));
            cardList.add(new MemoryGameCard( BitmapFactory.decodeResource(getResources(), R.drawable.mg6),null,6));
            cardList.add(new MemoryGameCard( BitmapFactory.decodeResource(getResources(), R.drawable.mg7),null,7));
            cardList.add(new MemoryGameCard(BitmapFactory.decodeResource(getResources(), R.drawable.mg8), null, 8));



            rectList.add(new Rect(startingX, startingY, startingX + imgWidth, startingY + imgHeight)); //1
            rectList.add(new Rect(getLastRect().right+ GapX, getLastRect().top, getLastRect().right+ GapX+imgWidth, getLastRect().bottom));//2
            rectList.add(new Rect(getLastRect().right+ GapX, getLastRect().top, getLastRect().right+ GapX+imgWidth, getLastRect().bottom));//3
            rectList.add(new Rect(getLastRect().right+ GapX, getLastRect().top, getLastRect().right+ GapX+imgWidth, getLastRect().bottom));//4

            rectList.add(new Rect(rectList.get(0).left, rectList.get(0).bottom+GapY, rectList.get(0).right, rectList.get(0).bottom+GapY +imgHeight));//5
            rectList.add(new Rect(getLastRect().right+ GapX, getLastRect().top, getLastRect().right+ GapX+imgWidth, getLastRect().bottom));//6
            rectList.add(new Rect(getLastRect().right+ GapX, getLastRect().top, getLastRect().right+ GapX+imgWidth, getLastRect().bottom));//7
            rectList.add(new Rect(getLastRect().right+ GapX, getLastRect().top, getLastRect().right+ GapX+imgWidth, getLastRect().bottom));//8

            rectList.add(new Rect(rectList.get(4).left, rectList.get(4).bottom+GapY, rectList.get(4).right, rectList.get(4).bottom+GapY +imgHeight));//9
            rectList.add(new Rect(getLastRect().right+ GapX, getLastRect().top, getLastRect().right+ GapX+imgWidth, getLastRect().bottom));//10
            rectList.add(new Rect(getLastRect().right+ GapX, getLastRect().top, getLastRect().right+ GapX+imgWidth, getLastRect().bottom));//11
            rectList.add(new Rect(getLastRect().right+ GapX, getLastRect().top, getLastRect().right+ GapX+imgWidth, getLastRect().bottom));//12

            rectList.add(new Rect(rectList.get(8).left, rectList.get(8).bottom+GapY, rectList.get(8).right, rectList.get(8).bottom+GapY +imgHeight));//13
            rectList.add(new Rect(getLastRect().right+ GapX, getLastRect().top, getLastRect().right+ GapX+imgWidth, getLastRect().bottom));//14
            rectList.add(new Rect(getLastRect().right+ GapX, getLastRect().top, getLastRect().right+ GapX+imgWidth, getLastRect().bottom));//15
            rectList.add(new Rect(getLastRect().right + GapX, getLastRect().top, getLastRect().right + GapX + imgWidth, getLastRect().bottom));//16

            Collections.shuffle(rectList);



            //Matches: 0-8, 1-9, 2-10, ... ,7-15 so we shuffle

            Random r = new Random();
            for(int i =0;i<cardList.size();i++)
            {
               cardList.get(i).rect=rectList.get(i);
            }

        }


        public void setPairsDeselected() { //Set all 'cards' to deselected
            for(int i=0;i<cardList.size();i++)
                cardList.get(i).turned=false;
        }

        public Rect getLastRect() {//returns the last inserted Rect
            return rectList.get(rectList.size()-1);
        }

        public int findSelectedRect() {//Find where the user touched using the touch coordinates
            int k=0;
            for(MemoryGameCard mgc : cardList) {
                if (mgc.rect.contains(PlayerTouchX, PlayerTouchY)) {
                    userClicks++;
                    //Log.w("Warn","Selections: "+ userClicks);
                    return k;
                }
                k++;
            }
            return -1;
        }

        public void LeaveMemoryGame() {

            //Save and Show Score
            int perferctValue = M*N; //user gets full score if only he clicks 16 times

            Score.currentRiddleScore = (int) Math.max(20,Math.ceil(70 - (userClicks -perferctValue)*(70/perferctValue)));//score starts from 20
            Intent itn = new Intent(getApplicationContext(), Score.class);
            itn.putExtra("nextStage", 9);
            startActivity(itn);

            finish();
        }

        @Override
        public void run() {   // Update state of what we draw

            if(showCardsState) {
                if ((elapsedShowAllImagesTime = System.currentTimeMillis() - showAllImagesTimer) >= limitShowAllImagesTime)  //time is up for showing images
                {
                    showCardsState = false;
                    canSelectState = true;
                }
            }
            else if(!canSelectState && !showCardsState)
                if ( (elapsedNoSelectTime= System.currentTimeMillis() - noSelectTimer) >= limitNoSelectTime)//time is up
                {
                    canSelectState=true;        //user can now reselect
                    currentSelectedRectIndex1=-1;
                    currentSelectedRectIndex2=-1;
                }
            // onDraw(Canvas) will be called
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            //Draw color on background
            canvas.drawPaint(backgroundPaint);

            pauseBt.getPauseMenuButton().draw(canvas);

            if(showCardsState)  //showing cards on game start for a few seconds
                for (int i = 0; i < cardList.size(); i++)
                    canvas.drawBitmap( cardList.get(i).img, null, cardList.get(i).rect, null);
            else
            {
                for (int i = 0; i < cardList.size(); i++)//draw back side or normal side in all images
                {
                    if ( cardList.get(i).turned)
                        canvas.drawBitmap( cardList.get(i).img, null, cardList.get(i).rect, null);
                    else
                        canvas.drawBitmap(backSideImg, null, cardList.get(i).rect, null);
                }

                if (currentSelectedRectIndex1 != -1)
                    canvas.drawBitmap( cardList.get(currentSelectedRectIndex1).img, null, cardList.get(currentSelectedRectIndex1).rect, null);// draw current selected images
                if (currentSelectedRectIndex2 != -1)
                    canvas.drawBitmap( cardList.get(currentSelectedRectIndex2).img, null, cardList.get(currentSelectedRectIndex2).rect, null);
            }
            // Invalidate view at about 60fps
            postDelayed(this, 16);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {

            // Check if pause button is hit
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                float touchX = ev.getX();
                float touchY = ev.getY();

                if (pauseBt.getRect().contains((int) touchX, (int) touchY)) {
                    if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                       //LeaveMemoryGame();
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

                if(!showCardsState)
                {
                    if (currentSelectedRectIndex1 == -1 && currentSelectedRectIndex2 == -1 && (currentSelectedRectIndex1 = findSelectedRect()) != -1)  //nothing is selected: read first
                    {
                        Log.w("Warn", "Selected 111: " + currentSelectedRectIndex1 + " 2 is: " + currentSelectedRectIndex2);
                    } else if (currentSelectedRectIndex1 != -1 && currentSelectedRectIndex2 == -1 && (currentSelectedRectIndex2 = findSelectedRect()) != -1)//selected 1: read second
                    {
                       // Log.w("Warn", "Selected 222: " + currentSelectedRectIndex2 + "  1 is " + currentSelectedRectIndex1);

                        //search for pairs
                       // if (KeysArray[currentSelectedRectIndex1] == currentSelectedRectIndex2 && KeysArray[currentSelectedRectIndex2] == currentSelectedRectIndex1) {
                        if (cardList.get(currentSelectedRectIndex1).code == cardList.get(currentSelectedRectIndex2).code) {

                            // Log.w("Warn", "Pair found");
                            SoundHandler.PlaySound(SoundHandler.correct_sound_id3);
                            pairs++;
                            if(pairs== (M*N)/2)
                                LeaveMemoryGame();

                            cardList.get(currentSelectedRectIndex1).turned = true; //reveal 'cards'
                            cardList.get(currentSelectedRectIndex2).turned = true;

                            currentSelectedRectIndex1 = -1;
                            currentSelectedRectIndex2 = -1;
                        } else    //if no pairs found activate 'no select timer'
                        {
                            canSelectState = false;
                            noSelectTimer = System.currentTimeMillis(); //start counting time..
                        }
                    } else if (currentSelectedRectIndex1 != -1 && currentSelectedRectIndex2 != -1) {
                        Log.w("Warn", "Cant Select, waiting timer..");
                    }
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

    public class MemoryGameCard     //represents an object drawn on screen on mini game: MemoryGame
    {
        private Bitmap img;
        private Rect rect;
        private Boolean turned;
        private int code;

        public MemoryGameCard(Bitmap _img, Rect _rect, int _code)
        {
            img= _img;
            rect= _rect;
            code = _code;
        }


    }

}

