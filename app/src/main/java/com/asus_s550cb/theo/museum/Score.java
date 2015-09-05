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
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by panos on 4/9/2015.
 *
 *  Score is from 0 to 100 for each room
 *  Quiz: 30 (3 questions)    Riddle 70
 *
 *  ChurchMap: 17 churches  70/17=4,1 points for each correct    -- ceil because 17*4.1=69.7
 *  Click Me:  25 pieces    70/25=2.8 points for each piece
 *  Hangman:
 *  Matching Coins: 70 the user has to completed to move on
 *  Puzzle:         70 the user has to completed to move on

 */
public class Score extends Activity {

    int currentApiVersion;
    public static int TotalScore=0,currentQuizScore,currentRiddleScore;

    private Button OkButton;
    private  boolean  showScore;
    private  CountDownTimer EffectsCountDownTimer;
    private TextView msg;
    int starsToShow,currentStarsShown;   //calculated according to score
    MediaPlayer score_sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);//hide status bar
        setContentView(R.layout.activity_score);

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

        showScore=false;
        score_sound= MediaPlayer.create(getBaseContext(), R.raw.score_sound);
        msg = (TextView) findViewById( R.id.scoreTextView);
        CalculateStarsBasedOnScore();


        EffectsCountDownTimer = new CountDownTimer(6000, 1000)
        {

            public void onTick(long millisUntilFinished) {
                long seconds=(millisUntilFinished / 1000)%60;
                long minutes=(millisUntilFinished/1000*60)%60;

                starsToShow--;
                currentStarsShown++;
                if(starsToShow==-1)
                    showScore=true;
                else if(showScore)
                {
                    msg.setText("Quiz: "+ currentQuizScore +" Mini Game: "+currentRiddleScore+ " Total: "+TotalScore);
                }
                else
                {
                    switch (currentStarsShown)
                    {
                        case 1:
                        {
                            ImageView s1 = (ImageView) findViewById(R.id.starView1);
                            s1.setImageResource(R.drawable.score_star_full);
                        }
                        case 2:
                        {
                            ImageView s2 = (ImageView) findViewById(R.id.starView2);
                            s2.setImageResource(R.drawable.score_star_full);
                        }
                        case 3:
                        {
                            ImageView s3 = (ImageView) findViewById(R.id.starView3);
                            s3.setImageResource(R.drawable.score_star_full);
                        }
                        default:
                            Log.w("Warn","More than 3 stars..?");
                    }
                }


            }

            public void onFinish()
            {
                finish();
            }
        }.start();


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

    private void CalculateStarsBasedOnScore() {
        //0-10: 0 star    11-40 1 star  41-70 2 stars   71-100  3 stars
        int riddle_score= currentRiddleScore+currentQuizScore;
        TotalScore+=currentQuizScore+currentRiddleScore;

        if (riddle_score >= 0 && riddle_score <= 10)
        {
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

    public static void setQuizScore(int score)
    {
     currentQuizScore=score;
    }

    public static void setRiddleScore(int score)
    {
        currentRiddleScore=score;
    }

}

