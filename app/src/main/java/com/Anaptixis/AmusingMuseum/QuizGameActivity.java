package com.Anaptixis.AmusingMuseum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.net.ssl.HttpsURLConnection;


public class QuizGameActivity extends Activity implements OnClickListener{

    public static boolean firstQuiz;


    private int currentApiVersion;

    //question counter Overall...
    public static int questionCountPublic = 0;
    public static int questionRightAnsPublic = 0;

    //question for specific room
    private int questionCounter;
    private int questionRightAns;

    //Screen dimensions
    private int screenWidth;
    private int screenHeight;

    private TextView timer; //Show the remaining time
    private TextView txtVquestion, txtVquestionCounter, txtVresult;
    private Button ans1, ans2, ans3, ans4;

    private String[] questions;
    private String[] answers;
    private String[] rightAnswers;

    String nextApp;
    private int appToStart;

    private int correctAnswers;
    private int wrongAnswers;

    CountDownTimer countDownTimer; //Timer

    private Drawable d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.quizgame_activity);

        nextApp = "nextApp";

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            appToStart = extras.getInt("nextApp");
        }

        // Get screen dimensions -- pixels
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenHeight = displaymetrics.heightPixels;
        screenWidth = displaymetrics.widthPixels;

        txtVquestion = (TextView) findViewById(R.id.textViewQuestion);


        txtVquestionCounter = (TextView) findViewById(R.id.textViewQuestionCounter);
        txtVresult = (TextView) findViewById(R.id.textViewResult);
        ans1 = (Button) findViewById(R.id.buttonAns1);
        ans2 = (Button) findViewById(R.id.buttonAns2);
        ans3 = (Button) findViewById(R.id.buttonAns3);
        ans4 = (Button) findViewById(R.id.buttonAns4);

        d=ans1.getBackground();

        ans1.setOnClickListener(this);
        ans2.setOnClickListener(this);
        ans3.setOnClickListener(this);
        ans4.setOnClickListener(this);

        questions = getResources().getStringArray(R.array.Questions);
        answers = getResources().getStringArray(R.array.Answers);
        rightAnswers = getResources().getStringArray(R.array.RightAnswers);
        if (MainActivity.WORKING_ON_EXTERNAL_MUSEUM) {
            questions = MainActivity.GetAllQuestions();
            answers = MainActivity.GetAllAnswers();
            rightAnswers = MainActivity.GetAllRightAnswers();
        }


        /**--------------------TIMER START----------------------------------**/
        timer = (TextView) findViewById(R.id.txtViewTimer);
        countDownTimer = new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                long seconds = (millisUntilFinished / 1000) % 60;
                long minutes = (millisUntilFinished / 1000 * 60) % 60;

                if (seconds > 9)
                    timer.setText(+minutes + " : " + seconds);
                else
                    timer.setText(+minutes + " : 0" + seconds);

            }

            public void onFinish() {

                timer.setText("Time's up!");
                // Finish game when the timer is zero
                fixQuestionCounter();
                QrCodeScanner.questionMode = false;
                Intent itns = new Intent(getApplicationContext(), QrCodeScanner.class);
                itns.putExtra(nextApp, appToStart);
                itns.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(itns);
                finish();
            }
        }.start();
        /**-----------------------TIMER END--------------------------**/

        //call the next question
        nextQuestion();

        // ------------------ Code in order to hide the navigation bar -------------------- //
        menu.hideNavBar(this.getWindow());

    }


    public void handleClick(CharSequence text,View v) {

        Button btn=(Button) v;
        CharSequence test =text;
        txtVresult.setVisibility(View.VISIBLE);

      //  Log.w("Warn","SELECTED:"+((Button) v).getText()+"|");
      //  Log.w("Warn", "CORRECT IS:" + rightAnswers[questionCountPublic]+"|");
        //Check for the right answer
        if (test.toString().equals(rightAnswers[questionCountPublic])) {
            SoundHandler.PlaySound(SoundHandler.correct_sound_id3);
            correctAnswers++;
            UploadAnswerResults(questionCountPublic);
            txtVresult.setText(getResources().getString(R.string.rightAnswer));
            //toastText=getResources().getString(R.string.rightAnswer);
            //RightAnswer so increase the proper counter
            questionRightAnsPublic++;
            questionRightAns++;
            btn.setBackgroundResource(R.drawable.quiz_button_correct);
          //  Log.w("Warn","CORRECT");

        } else {
            SoundHandler.PlaySound(SoundHandler.wrong_sound_id);
            wrongAnswers++;
            txtVresult.setText(getResources().getString(R.string.wrongAnswer));
           // toastText=getResources().getString(R.string.wrongAnswer) + getResources().getString(R.string.correct_is) +rightAnswers[questionCountPublic];
           // Toast.makeText(this.getBaseContext(), toastText, Toast.LENGTH_SHORT).show();
            UploadAnswerResults(questionCountPublic);
            btn.setBackgroundResource(R.drawable.quiz_button_wrong);;
            setColorsOnButtons();
            //  Log.w("Warn", "WRONG");
        }

        //increase the public & private counter
        ++questionCountPublic;
        ++questionCounter;


        //Set AsyncTask to handle the question switch
        MyTask task=new MyTask();
        task.execute();



    }
    //Color green the correct answer
    private void setColorsOnButtons() {
        if(ans1.getText().equals(rightAnswers[questionCountPublic]))
        {
            ans1.setBackgroundResource(R.drawable.quiz_button_correct);
        }
        else if(ans2.getText().equals(rightAnswers[questionCountPublic]))
        {
            ans2.setBackgroundResource(R.drawable.quiz_button_correct);
        }
        else if(ans3.getText().equals(rightAnswers[questionCountPublic]))
        {
            ans3.setBackgroundResource(R.drawable.quiz_button_correct);
        }
        else if(ans4.getText().equals(rightAnswers[questionCountPublic]))
        {
            ans4.setBackgroundResource(R.drawable.quiz_button_correct);
        }
    }

    // Reset the flags to hide the navigation bar
    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        menu.hideNavBar(this.getWindow());
    }

    public void nextQuestion() {
        txtVquestion.setText(questions[questionCountPublic]);
        //Find to get the number of correct and wrong answers
        findQuestionOnDB(questions[questionCountPublic]);
        txtVquestionCounter.setText(getResources().getString(R.string.question) + " " + (questionCounter + 1) + " " + getResources().getString(R.string.from));

        ans1.setText(answers[questionCountPublic * 4]);
        ans2.setText(answers[questionCountPublic * 4 + 1]);
        ans3.setText(answers[questionCountPublic * 4 + 2]);
        ans4.setText(answers[questionCountPublic * 4 + 3]);
    }

    //When the time end finish quiz finish Activity
    //and make sure that you are in the right question

    public void fixQuestionCounter() {

        if (questionCountPublic < 3)
            questionCountPublic = 3;
        else if (questionCountPublic < 6)
            questionCountPublic = 6;
        else if (questionCountPublic < 9)
            questionCountPublic = 9;
        else if (questionCountPublic < 12)
            questionCountPublic = 12;
        else if (questionCountPublic < 15)
            questionCountPublic = 15;
        else if (questionCountPublic < 18)
            questionCountPublic = 18;
        else if (questionCountPublic < 21)
            questionCountPublic = 21;
        else if (questionCountPublic < 24)
            questionCountPublic = 24;
        else if (questionCountPublic < 27)
            questionCountPublic = 27;
        else if (questionCountPublic < 30)
            questionCountPublic = 30;
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

    public void findQuestionOnDB(final String questionText) //Contact with server using php, method POST and receive all museums
    {
        //     currentActivity = this;
        Thread thread = new Thread(new Runnable() {//using thread to handle connections
            @Override
            public void run() {

                try {
                    URL url;
                    String response = "";
                    String question_text=questionText;
                    Log.w("Warn", "Question: " + questionText);
                    //UBUNTU LTS Server on okeanos.grnet.gr
                    //Increase questionCountPublic by 1 because in DB id starts from 1 not 0
                    int id_num=questionCountPublic+1;
                    Log.w("Warn", "Id: "+id_num);
                    //Pass id as a parameter
                    url = new URL("http://83.212.117.226/GetQuestionStats.php?question_id="+id_num);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(15000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    int responseCode = conn.getResponseCode();
                    Log.w("Warn", "ResponseCode: " + responseCode); // 200: OK, The request was fulfilled.
                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        String line;
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        while ((line = br.readLine()) != null) {
                            response += line;
                        }
                        Log.w("Warn", "Response: " + response);
                        SeparateResponseAndShowDataOnView(response);
                    }


                } catch (ConnectException ce) //connection exception
                {
                    //ce.printStackTrace();
                    //           ShowErrors(getBaseContext().getString(R.string.error_no_internet_connection_large));
                } catch (Exception e)     //general exception
                {
                    e.printStackTrace();
                    //          ShowErrors(getBaseContext().getString(R.string.error_try_later_large));
                }
            }
        });
        thread.start();
    }

    public void SeparateResponseAndShowDataOnView(final String res) {  //shows all museums on List View and handles selection
        runOnUiThread(new Runnable() {      //only main thread interacts with Views
            @Override
            public void run() {

                StringTokenizer st = new StringTokenizer(res, "|");      //m_id1,museum_name1|m_id2,museum_name2|..
                String str = "";

                while (st.hasMoreElements())                            //separate
                {
                    str = (String) st.nextElement();
                    String[] parts = str.split(",");
                    correctAnswers=Integer.valueOf(parts[0]);
                    wrongAnswers=Integer.valueOf(parts[1]);

                    Log.w("Warn", "Correct Ans:" + parts[0] + " Wrong Ans: " + parts[1]);
                }
            }
        });
    }

    public void UploadAnswerResults(int id) {
        //Server at:83.212.117.226      DB: AmusingMuseumDB       Table: AmusingMuseumScores
        //nameField = (EditText) findViewById(R.id.upload_score_name_txt);
       // if (nameField.length() == 0) {

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url;
                        String response = "";
                 //       String name = nameField.getText().toString();   //collect data: name-score-date
                 //       String name="nasia";
                //        Calendar c = Calendar.getInstance();
                //        String date = c.get(Calendar.DAY_OF_MONTH) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.YEAR);
                        //        ShowErrorOnView("");    //clear error log

                        int q_id=questionCountPublic;
                        //UBUNTU LTS Server on okeanos.grnet.gr
                        url = new URL("http://83.212.117.226/QuestionStatsUpdate.php?a_id=" + q_id + "&correct=" + correctAnswers + "&wrong=" + wrongAnswers);
                        // $sql=mysql_query("INSERT INTO AmusingMuseumScores (name,score,date) VALUES (".$_GET['name'].",".$_GET['score'].",".$_GET['date'].")");
                        //create table AmusingMuseumScores ( name varchar(30), score int(10), date varchar(10));
                        //alter table AmusingMuseumScores add Primary Key (name);

                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setReadTimeout(15000);
                        conn.setConnectTimeout(15000);
                        conn.setRequestMethod("POST");
                        conn.setDoInput(true);
                        conn.setDoOutput(true);

                        OutputStream os = conn.getOutputStream();
                        BufferedWriter writer = new BufferedWriter(
                                new OutputStreamWriter(os, "UTF-8"));

                        writer.flush();
                        writer.close();
                        os.close();
                        int responseCode = conn.getResponseCode();
                        Log.w("Warn", "ResponseCode for Upload: " + responseCode); // 200: OK, The request was fulfilled.
                        if (responseCode == HttpsURLConnection.HTTP_OK) {
                            String line;
                            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            while ((line = br.readLine()) != null) {
                                response += line;
                            }

                            Log.w("Warn", "Response: " + response);
                            //                  if (response.contains("Integrity constraint violation")) //name already used on database
                            //                      ShowErrorOnView(getBaseContext().getString(R.string.error_on_upload_name_used));
                            //                 else if (response.equals("Success")) {
                            //                      ShowErrorOnView(getBaseContext().getString(R.string.score_successfully_uploaded));
                            //                       scoreUploaded = true;
                            //                   }
                        }
                        //else
                        //                 ShowErrorOnView(getBaseContext().getString(R.string.error_try_later_large));

                    } catch (ConnectException ce) //connection exception
                    {
                        //              ShowErrorOnView(getBaseContext().getString(R.string.error_no_internet_connection_large));
                    } catch (Exception e)     //general exception
                    {
                        //              ShowErrorOnView(getBaseContext().getString(R.string.error_try_later_large));
                    }
                }
            });
            thread.start();

        //      }
        //         else
        //         {
        //             TextView errView = (TextView) findViewById(R.id.ErrorView);
        //             errView.setText(R.string.upload_locally_already_saved);

        //         }


    }


    private long mLastClickTime=0;
    @Override
    public void onClick(View v) {
        // Preventing multiple clicks, using threshold of 1 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        Button btn = (Button) v;

        if(btn.getId()== R.id.buttonAns1)
        {
            handleClick(btn.getText(), v);
        }
        else if (btn.getId()== R.id.buttonAns2)
        {
            handleClick(btn.getText(),v);
        }
        else if (btn.getId()== R.id.buttonAns3)
        {
            handleClick(btn.getText(),v);
        }
        else if(btn.getId()== R.id.buttonAns4)
        {
            handleClick(btn.getText(),v);
        }
    }

    //Restore the button style
    private void resetButtonBackground(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ans1.setBackground(d);
            ans2.setBackground(d);
            ans3.setBackground(d);
            ans4.setBackground(d);
        }
        else
        {
            ans1.setBackgroundDrawable(d);
            ans2.setBackgroundDrawable(d);
            ans3.setBackgroundDrawable(d);
            ans4.setBackgroundDrawable(d);
        }

    }

    //Proceed to new question or to mini game
    private void goNextEvent() {

        //Toast.makeText(this.getBaseContext(), toastText, Toast.LENGTH_SHORT).show();

        //resetButtonColor();

        if (questionCounter < 3) {
            nextQuestion();
        } else {
            //Stop timer or else it continues running after the activity is finished
            countDownTimer.cancel();

            //Save Quiz Score
            Score.setQuizScore(questionRightAnsPublic * 10);

            // When done open the qr scaner again to play the riddle
            // To do so pass back the next activity number
            // And make sure the user cannot go but by hitting the back button
            QrCodeScanner.questionMode = false;
            Intent itns = new Intent(getApplicationContext(), QrCodeScanner.class);
            itns.putExtra(nextApp, appToStart);
            itns.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(itns);
            finish();

        }

    }



    class MyTask extends AsyncTask<String,Integer,Void>
    {

        @Override
        protected void onPreExecute() {
            buttonsActivationSwitcher(false);
            txtVresult.setVisibility(View.VISIBLE);

        }
        @Override
        protected Void doInBackground(String... params) {

            try {
                Thread.sleep(2000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            buttonsActivationSwitcher(true);
            resetButtonBackground();
            txtVresult.setVisibility(View.INVISIBLE);
            goNextEvent();
        }

        private void buttonsActivationSwitcher(boolean b)
        {

            ans1.setClickable(b);
            ans2.setClickable(b);
            ans3.setClickable(b);
            ans4.setClickable(b);
        }
    }
}
