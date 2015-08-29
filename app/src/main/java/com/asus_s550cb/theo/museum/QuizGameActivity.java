package com.asus_s550cb.theo.museum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class QuizGameActivity extends Activity {



    private int currentApiVersion;

    //question counter...
    private int questionCount;
    private int rightAnsCounter;
    //Screen dimensions
    private int screenWidth;
    private int screenHeight;

    private TextView txtVquestion,txtVquestionCounter,txtVresult;
    private Button ans1,ans2,ans3,ans4;
    private String [] questions;
    private String [] answers;
    private String [] rightAnswers;

    String nextApp;
    private int appToStart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.quizgame_activity);

        // Get screen dimensions -- pixels
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenHeight = displaymetrics.heightPixels;
        screenWidth = displaymetrics.widthPixels;

        txtVquestion = (TextView) findViewById(R.id.textViewQuestion);
        txtVquestionCounter=  (TextView) findViewById(R.id.textViewQuestionCounter);
        txtVresult = (TextView) findViewById(R.id.textViewResult);
        ans1 = (Button) findViewById(R.id.buttonAns1);
        ans2 = (Button) findViewById(R.id.buttonAns2);
        ans3 = (Button) findViewById(R.id.buttonAns3);
        ans4 = (Button) findViewById(R.id.buttonAns4);

        questions= getResources().getStringArray(R.array.Questions);
        answers= getResources().getStringArray(R.array.Answers);
        rightAnswers= getResources().getStringArray(R.array.RightAnswers);

        questionCount=0;
        nextApp="nextApp";

        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            appToStart = extras.getInt("nextApp");
        }

        //call the next question
        nextQuestion();

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
    }


    public void buttonOnClick (View v){
        Button btn= (Button) v;

        CharSequence test=((Button) v).getText();
        txtVresult.setVisibility(View.VISIBLE);


        //Check for the right answer
        if (test.toString().equals(rightAnswers[questionCount]))
        {
            txtVresult.setText(getResources().getString(R.string.rightAnswer));
            rightAnsCounter++;
        }
        else
            txtVresult.setText(getResources().getString(R.string.wrongAnswer));

        //increase the counter
        ++questionCount;

        //sleep and procceed to the nextQuestion
        SystemClock.sleep(1000);
        if(questionCount <5)
            nextQuestion();
        else
        {
            // When done open the qr scaner again to play the riddle
            // To do so pass back the next activity number
            // And make sure the user cannot go but by hitting the back button
            QrCodeScanner.questionMode=false;
            Intent itns = new Intent(getApplicationContext(), QrCodeScanner.class);
            itns.putExtra(nextApp, appToStart);
            itns.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(itns);
            finish();
        /*    new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.result))
                    .setMessage(getResources().getString(R.string.correct_answers_num)+ rightAnsCounter)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();*/
        }
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

    public void nextQuestion(){
        txtVquestion.setText(questions[questionCount]);
        txtVquestionCounter.setText(getResources().getString(R.string.question)+ (questionCount +1) + getResources().getString(R.string.from));

        ans1.setText(answers[questionCount *4]);
        ans2.setText(answers[questionCount *4+1]);
        ans3.setText(answers[questionCount *4+2]);
        ans4.setText(answers[questionCount *4+3]);
    }
}
