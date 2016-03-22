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
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
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
            Log.w("Warn", "Returned!");
            onFocusChangedCounterMemoryGame++;
            memoryGameScreen.StartGame();
        }

    }

    //This is actually drawing on screen the game
    public class MemoryGameScreen extends View implements Runnable {

        int PlayerTouchX, PlayerTouchY;
        MediaPlayer memory_game_pair_sound;
        int pairs, M = 4, N = 4, imgWidth, imgHeight, startingX, startingY, GapX, GapY,currentSelectedRectIndex1,currentSelectedRectIndex2, userClicks;
        Paint backgroundPaint, txtPaint;
        Bitmap backSideImg;


        long noSelectTimer, elapsedNoSelectTime, limitNoSelectTime; //timer to freeze game between 2 selections
        long showAllImagesTimer, elapsedShowAllImagesTime, limitShowAllImagesTime; //timer for showing images on start


        ArrayList<Bitmap> ImagesList = new ArrayList<Bitmap>();
        ArrayList<Rect> RectList  = new ArrayList<Rect>();
        Boolean[] TurnedList  = new Boolean[M*N];//binary values for completed pairs
        int[] KeysArray  = new int[M*N];//keys used for pairs validation

        Boolean canSelectState, showCardsState;
        Button invBtn;          //invisible button: triggered on exit to finish mini game

        public MemoryGameScreen(Context context) {
            super(context);
           // memory_game_pair_sound = MediaPlayer.create(this.getContext(), something..);

            //for background color
            backgroundPaint = new Paint();
            backgroundPaint.setStyle(Paint.Style.FILL);
            backgroundPaint.setColor(getResources().getColor(R.color.neutral_beize));

            //for text color
            txtPaint = new Paint();
            txtPaint.setStyle(Paint.Style.FILL);
            txtPaint.setColor(Color.WHITE);//white
            txtPaint.setTextSize(getResources().getDimension(R.dimen.memory_game_text_size));

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

            invBtn = new Button(getContext());
            invBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.w("Warn", "Clicked button");
                    LeaveMemoryGame();
                }
            });

            LoadImages();
            LoadRects();
            SetPairsDeselected();
            RandomizePairs();

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

        public void LoadImages(){
            ImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mg1) );
            ImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mg2) );
            ImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mg3) );
            ImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mg4) );
            ImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mg5) );
            ImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mg6) );
            ImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mg7) );
            ImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mg8) );

            ImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mg1) );
            ImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mg2) );
            ImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mg3) );
            ImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mg4) );
            ImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mg5) );
            ImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mg6) );
            ImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mg7) );
            ImagesList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mg8) );
            for(int i=0;i<M*N/2;i++)    //0-8, 1-9, 2-10, ... ,7-15
            {
                KeysArray[i] = i + (M * N) / 2;
                KeysArray[i + (M * N) / 2] = i;
            }
        }

        public void LoadRects() { //All rectangles gain proper values according to screen resolution
            RectList.add(new Rect(startingX, startingY, startingX + imgWidth, startingY + imgHeight)); //1
            RectList.add(new Rect(getLastRect().right+ GapX, getLastRect().top, getLastRect().right+ GapX+imgWidth, getLastRect().bottom));//2
            RectList.add(new Rect(getLastRect().right+ GapX, getLastRect().top, getLastRect().right+ GapX+imgWidth, getLastRect().bottom));//3
            RectList.add(new Rect(getLastRect().right+ GapX, getLastRect().top, getLastRect().right+ GapX+imgWidth, getLastRect().bottom));//4

            RectList.add(new Rect(RectList.get(0).left, RectList.get(0).bottom+GapY, RectList.get(0).right, RectList.get(0).bottom+GapY +imgHeight));//5
            RectList.add(new Rect(getLastRect().right+ GapX, getLastRect().top, getLastRect().right+ GapX+imgWidth, getLastRect().bottom));//6
            RectList.add(new Rect(getLastRect().right+ GapX, getLastRect().top, getLastRect().right+ GapX+imgWidth, getLastRect().bottom));//7
            RectList.add(new Rect(getLastRect().right+ GapX, getLastRect().top, getLastRect().right+ GapX+imgWidth, getLastRect().bottom));//8

            RectList.add(new Rect(RectList.get(4).left, RectList.get(4).bottom+GapY, RectList.get(4).right, RectList.get(4).bottom+GapY +imgHeight));//9
            RectList.add(new Rect(getLastRect().right+ GapX, getLastRect().top, getLastRect().right+ GapX+imgWidth, getLastRect().bottom));//10
            RectList.add(new Rect(getLastRect().right+ GapX, getLastRect().top, getLastRect().right+ GapX+imgWidth, getLastRect().bottom));//11
            RectList.add(new Rect(getLastRect().right+ GapX, getLastRect().top, getLastRect().right+ GapX+imgWidth, getLastRect().bottom));//12

            RectList.add(new Rect(RectList.get(8).left, RectList.get(8).bottom+GapY, RectList.get(8).right, RectList.get(8).bottom+GapY +imgHeight));//13
            RectList.add(new Rect(getLastRect().right+ GapX, getLastRect().top, getLastRect().right+ GapX+imgWidth, getLastRect().bottom));//14
            RectList.add(new Rect(getLastRect().right+ GapX, getLastRect().top, getLastRect().right+ GapX+imgWidth, getLastRect().bottom));//15
            RectList.add(new Rect(getLastRect().right + GapX, getLastRect().top, getLastRect().right + GapX + imgWidth, getLastRect().bottom));//16
        }

        public void SetPairsDeselected() { //Set all 'cards' to deselected
            for(int i=0;i<M*N;i++)
                TurnedList[i]=false;
        }

        public void RandomizePairs() {
        //Shuffle images, rects, keys etc
            //as implemented in LoadImages method pairs are: 0-8, 1-9, 2-10, ... ,7-15
            Random random = new Random();
            for (int i = 0; i < M*N; i++)
            {
                int j = random.nextInt(15); //swap i with j

                Bitmap b= ImagesList.get(i); //image
                ImagesList.set(i, ImagesList.get(j));
                ImagesList.set(j,b);

               // Rect r= RectList.get(i); //rect
               // RectList.set(i, RectList.get(j));
             //   RectList.set(j,r);
                int temp=KeysArray[j], temp2=KeysArray[i];
                KeysArray[i]=temp;  //Match [i] with previous [j]
                KeysArray[j]=temp2; //and   [j] with previous [i]
                KeysArray[temp]=i;  //when i points to j, then j MUST point back to i
                KeysArray[temp2]=j;

                //Log.w("Warn", "Swapped "+i+" with "+j );
            }
        }

        public Rect getLastRect() { //returns the last inserted Rect from RectList
            return RectList.get(RectList.size()-1);
        }

        public int FindSelectedRect() {//Find where the user touched using the touch coordinates
            int k=0;
            for(Rect r : RectList) {
                if (r.contains(PlayerTouchX, PlayerTouchY)) {
                    userClicks++;
                    Log.w("Warn","Selections: "+ userClicks);
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
            startActivity(itn);

            QrCodeScanner.questionMode = true;
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
                for (int i = 0; i < M * N; i++)
                    canvas.drawBitmap(ImagesList.get(i), null, RectList.get(i), null);
            else
            {
                for (int i = 0; i < M * N; i++)//draw back side or normal side in all images
                {
                    if (TurnedList[i])
                        canvas.drawBitmap(ImagesList.get(i), null, RectList.get(i), null);
                    else
                        canvas.drawBitmap(backSideImg, null, RectList.get(i), null);
                }

                if (currentSelectedRectIndex1 != -1)
                    canvas.drawBitmap(ImagesList.get(currentSelectedRectIndex1), null, RectList.get(currentSelectedRectIndex1), null);// draw current selected images
                if (currentSelectedRectIndex2 != -1)
                    canvas.drawBitmap(ImagesList.get(currentSelectedRectIndex2), null, RectList.get(currentSelectedRectIndex2), null);
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
                    if (currentSelectedRectIndex1 == -1 && currentSelectedRectIndex2 == -1 && (currentSelectedRectIndex1 = FindSelectedRect()) != -1)  //nothing is selected: read first
                    {
                        Log.w("Warn", "Selected 111: " + currentSelectedRectIndex1 + " 2 is: " + currentSelectedRectIndex2);
                    } else if (currentSelectedRectIndex1 != -1 && currentSelectedRectIndex2 == -1 && (currentSelectedRectIndex2 = FindSelectedRect()) != -1)//selected 1: read second
                    {
                       // Log.w("Warn", "Selected 222: " + currentSelectedRectIndex2 + "  1 is " + currentSelectedRectIndex1);

                        //search for pairs
                        if (KeysArray[currentSelectedRectIndex1] == currentSelectedRectIndex2 && KeysArray[currentSelectedRectIndex2] == currentSelectedRectIndex1) {
                            Log.w("Warn", "Pair found");
                            SoundHandler.PlaySound(SoundHandler.correct_sound_id3);
                            pairs++;
                            if(pairs== (M*N)/2)
                                LeaveMemoryGame();

                            TurnedList[currentSelectedRectIndex1] = true; //reveal 'cards'
                            TurnedList[currentSelectedRectIndex2] = true;

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
}

