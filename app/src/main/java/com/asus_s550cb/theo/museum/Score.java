package com.asus_s550cb.theo.museum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
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
 *  Hangman:  Finished : 70 - mistakes*2        Unfinished: correct_letters*2
 *  Matching Coins: 70 the user has to complete it to move on
 *  Puzzle:         70 the user has to complete it to move on
 *  Right Order:    60 seconds: 1 point per second left  (+10 to reach 70)

 */
public class Score extends Activity {


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
        menu.hideNavBar(this.getWindow());

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
                if(starsToShow==-1) //done showing stars
                    showScore=true;

                else if(showScore)
                {
                    msg.setText("Quiz: "+ currentQuizScore +"\nMini Game: "+currentRiddleScore+ "\nTotal: "+TotalScore);
                }
                else
                {
                    if(currentStarsShown==1)
                    {
                        PlaySound();
                        ImageView s1 = (ImageView) findViewById(R.id.starView1);
                        s1.setImageResource(R.drawable.score_star_full);
                    }
                    else  if(currentStarsShown==2)
                    {
                        PlaySound();
                        ImageView s2 = (ImageView) findViewById(R.id.starView2);
                        s2.setImageResource(R.drawable.score_star_full);
                    }
                    else  if(currentStarsShown==3)
                    {
                        PlaySound();
                        ImageView s3 = (ImageView) findViewById(R.id.starView3);
                        s3.setImageResource(R.drawable.score_star_full);
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
        menu.hideNavBar(this.getWindow());
    }

    private void CalculateStarsBasedOnScore() {
        //0-10: 0 star    11-40 1 star  41-70 2 stars   71-100  3 stars
        if(currentQuizScore>=30) currentQuizScore=30;   //selecting fast answers on quiz gives you more than 3 corrects answers


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

