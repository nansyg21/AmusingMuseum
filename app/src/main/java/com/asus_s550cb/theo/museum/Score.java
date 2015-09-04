package com.asus_s550cb.theo.museum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by panos on 4/9/2015.
 *
 *  Score is from 0 to 100 for each room
 *  Quiz: 30 (3 questions)    Riddle 70
 *
 *  ChurchMap: 17 churches  70/17=4,1 points for each correct    -- ceil because 17*4.1=69.7

 */
public class Score extends Activity {

    int currentApiVersion;
    public static int TotalScore=0,currentQuizScore,currentRiddleScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);//hide status bar
        setContentView(new ScoreScreen(this));

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
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT) {

            getWindow().getDecorView().setSystemUiVisibility(flags);
            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            final View decorView = getWindow().getDecorView();
            decorView
                    .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                decorView.setSystemUiVisibility(flags);
                            }
                        }
                    });
        }
    }

    // Reset the flags to hide the navigation bar

    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }



    public class ScoreScreen extends View implements Runnable
    {
        int ScreenWidth,ScreenHeight;
        MediaPlayer score_sound;
        Paint txtPaint, missedImagePaint;

        Bitmap backImg;
        Rect backRect;

        Paint framePaint;
        Rect frameRect;
        int frameWidth, frameHeight;
        private Bitmap star1;
        private Bitmap star2;
        private Bitmap star3;

        private Rect star1Rect;
        private Rect star2Rect;
        private Rect star3Rect;
        int starWidth,starHeight, padding;

        private Bitmap star_full;
        private Bitmap star_empty;


        int starsToShow,currentStarsShown;   //calculated according to score
        boolean showStar1,showStar2,showStar3, showScore;
        long startTime, toNextStar=2000;
        long elapsedTime = 0L;



        boolean doneShowingStars =false ;     //doneShowingStars showing score
        long doneTimer=5000;    //waiting time before closing



        public ScoreScreen(Context context)
        {
            super(context);
            score_sound= MediaPlayer.create(this.getContext(), R.raw.score_sound);

            //get screen size
            Point size = new Point();
            //  getWindowManager().getDefaultDisplay().getSize(size);
            //  ScreenWidth=size.x;
            //  ScreenHeight=size.y;

            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            ScreenHeight = displaymetrics.heightPixels;
            ScreenWidth = displaymetrics.widthPixels;


            //for text color
            txtPaint = new Paint();
            txtPaint.setStyle(Paint.Style.FILL);
            txtPaint.setColor(Color.WHITE);
            txtPaint.setTextSize(40);
            txtPaint.setTextSize(getResources().getDimension(R.dimen.score_text_size));

            //back image
            backImg = BitmapFactory.decodeResource(getResources(), R.drawable.menu_logo);
            backRect= new Rect(0,0,ScreenWidth,ScreenHeight);

            //frame paint
            framePaint = new Paint();
            framePaint.setStyle(Paint.Style.FILL);
            framePaint.setColor(Color.parseColor("#6a3732"));
            frameWidth=9*ScreenWidth/10;
            frameHeight=8*ScreenHeight/9;
            frameRect = new Rect(ScreenWidth/10, ScreenHeight/9, frameWidth, frameHeight);


            star_full = BitmapFactory.decodeResource(getResources(), R.drawable.score_star_full);  // no rescale: drawable-nodpi
            star_empty = BitmapFactory.decodeResource(getResources(), R.drawable.score_star_empty);
            star1=star_empty;       //at first all stars are empty - then according to score the are filled
            star2=star_empty;
            star3=star_empty;

            starWidth=100;
            starHeight=100;
            padding=10;
            int star1X=frameRect.left+frameWidth/3-starWidth/2;
            int star1Y=frameRect.top+10;
            star1Rect = new Rect(star1X, star1Y, star1X+ starWidth, star1Y+starHeight);//calculate star1Rect
            star2Rect = new Rect(star1Rect.right+padding, star1Rect.top, star1Rect.right+padding+ starWidth, star1Rect.top+starHeight);//according to 1 calculate others
            star3Rect = new Rect(star2Rect.right+padding, star2Rect.top, star2Rect.right+padding+ starWidth, star2Rect.top+starHeight);


            showStar1=true;
            showStar2=false;
            showStar3=false;
            showScore=false;

            CalculateStarsBasedOnScore();
            startTime = System.currentTimeMillis();
        }

        private void CalculateStarsBasedOnScore() {
            //0-10: 0 star    11-40 1 star  41-70 2 stars   71-100  3 stars
            int riddle_score= currentRiddleScore+currentQuizScore;
            TotalScore+=currentQuizScore+currentRiddleScore;

            if (riddle_score >= 0 && riddle_score <= 10)
            {
                doneShowingStars=true;
                starsToShow = 0;
            }
            else if(riddle_score >=11 && riddle_score <=40)
                starsToShow=1;
            else if(riddle_score >=41 && riddle_score <=70)
                starsToShow=2;
            else if(riddle_score >=71 && riddle_score <=100)
                starsToShow=3;
            else
                Log.w("Warn","Something wrong with score received");

            currentStarsShown=0;


        }

        private void PlaySound() {
            if (score_sound.isPlaying()) {
                score_sound.stop();
            }
            score_sound.start();

        }


        @Override
        public void run()
        {   // Update state of what we draw

            elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime >= toNextStar && !doneShowingStars)
            {
                currentStarsShown++;
                if(currentStarsShown==1)
                {
                    showStar1=true;
                    PlaySound();
                    star1=star_full;
                }
                else if(currentStarsShown==2)
                {
                    showStar2=true;
                    PlaySound();
                    star2=star_full;
                }
                else if(currentStarsShown==3)
                {
                    showStar3=true;
                    PlaySound();
                    star3=star_full;
                }

                if(currentStarsShown==starsToShow)
                    doneShowingStars = true;

                startTime= System.currentTimeMillis();
                elapsedTime=0L;
            }
            else if (elapsedTime >= toNextStar && !showScore)
            {
                showScore=true;

            }
            else if(showScore && elapsedTime>=doneTimer)                   //doneShowingStars playing
                finish();

            // onDraw(Canvas) will be called
            invalidate();
        }


        @Override
        protected void onDraw(Canvas canvas)
        {
            super.onDraw(canvas);

            //Draw background
            canvas.drawBitmap(backImg, null, backRect, null);

            //Draw frame
            canvas.drawRect(frameRect, framePaint);

            //Draw stars
            canvas.drawBitmap(star1, null, star1Rect, null);
            canvas.drawBitmap(star2, null, star2Rect, null);
            canvas.drawBitmap(star3, null, star3Rect, null);

            //write score
            if(showScore)
                canvas.drawText("Quiz: "+ currentQuizScore +" Mini Game: "+currentRiddleScore+ " Total: "+TotalScore, frameRect.left+10, star1Rect.bottom + starHeight, txtPaint);

            // Invalidate view at about 60fps
            postDelayed(this, 16);
        }

        public boolean onTouchEvent(MotionEvent ev)
        {

            return true;
        }

    }



}

