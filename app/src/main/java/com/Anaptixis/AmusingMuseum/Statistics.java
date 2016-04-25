package com.Anaptixis.AmusingMuseum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.net.ssl.HttpsURLConnection;


/**
 * Created by nasia on 12/11/2015.
 */
public class Statistics extends Activity {


    Activity currentActivity;
    private TextView questionTxtV;
    private TextView correctTxtV;
    private TextView wrongTxtV;

    private ArrayList<String> questionList;
    private ArrayList<Integer> correctAns;
    private ArrayList<Integer> wrongAns;

    private String[] questionTable;

    private String correctText;
    private String wrongText;

    ImageView titleImageView;
    int screenHeight, screenWidth;
 //   static Bitmap bmImg;

 //   public DownloadableMuseum myNewMuseum= new DownloadableMuseum();
//    public ArrayList<DownloadableMuseum.RoomsForNewMuseum> rooms= new ArrayList<DownloadableMuseum.RoomsForNewMuseum>();
//    int currentRoom=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        questionList=new ArrayList<String>();
        correctAns=new ArrayList<Integer>();
        wrongAns=new ArrayList<Integer>();

        questionTable = getResources().getStringArray(R.array.Questions);
        for(int i=0;i<questionTable.length;i++)
        {
            questionList.add(questionTable[i]);
        }

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);//hide status bar
        setContentView(R.layout.activity_statistics);

        // ------------------ Code in order to hide the navigation bar -------------------- //
        menu.hideNavBar(this.getWindow());

        questionTxtV=(TextView) findViewById(R.id.question_text);
        questionTxtV.setText(R.string.choose_question);
        correctTxtV=(TextView)findViewById(R.id.correctAnsTxtV);
        correctTxtV.setText(R.string.correct_answers);
        wrongTxtV=(TextView)findViewById(R.id.wrongAnsTxtV);
        wrongTxtV.setText(R.string.wrong_answers);
        titleImageView=(ImageView) findViewById(R.id.logoImageView);

        // Get screen dimensions -- pixels
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenHeight = displaymetrics.heightPixels;
        screenWidth = displaymetrics.widthPixels;

    //    correctText=;

        if(menu.lang.equals("uk")) {
            titleImageView.setImageResource(R.drawable.stats_en);

        }
        else
        {
            titleImageView.setImageResource(R.drawable.stats_el);
        }
        titleImageView.getLayoutParams().height=screenHeight/4;


        ImageView bt = (ImageView) findViewById(R.id.download_other_museums_back_button);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SeeAllQuestions();//Find out all available Museums to download
    }

    public void SeeAllQuestions() //Contact with server using php, method POST and receive all museums
    {
        currentActivity = this;
        Thread thread = new Thread(new Runnable() {//using thread to handle connections
            @Override
            public void run() {

                try {
                    URL url;
                    String response = "";

                    //UBUNTU LTS Server on okeanos.grnet.gr
                    url = new URL("http://83.212.117.226/SeeAllQuestions.php");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
     //               ShowErrors(getBaseContext().getString(R.string.error_no_internet_connection_large));
                } catch (Exception e)     //general exception
                {
                    e.printStackTrace();
       //             ShowErrors(getBaseContext().getString(R.string.error_try_later_large));
                }
            }
        });
        thread.start();
    }

    public void ShowErrors(final String err)   //use UI Thread to update view from worker thread
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView errorview = (TextView) findViewById(R.id.download_other_museums_info);
                errorview.setText(err);
            }
        });
    }

    public void SeparateResponseAndShowDataOnView(final String res) {  //shows all museums on List View and handles selection
        runOnUiThread(new Runnable() {      //only main thread interacts with Views
            @Override
            public void run() {

                ListView lv = (ListView) findViewById(R.id.choose_question_list_view);
                ArrayList<String> values = new ArrayList<String>();
                StringTokenizer st = new StringTokenizer(res, "|");      //m_id1,museum_name1|m_id2,museum_name2|..
                String str = "";

                while (st.hasMoreElements())                            //separate
                {
                    str = (String) st.nextElement();
                    String[] parts = str.split("\\*");
                    Log.w("Warn", "Added to list:  0:" + parts[0] + " 1: " + parts[1] + " 2: " + parts[2] + " 3: " + parts[3]);
                    correctAns.add(Integer.valueOf(parts[2]));
                    wrongAns.add(Integer.valueOf(parts[3]));


                }

                final CustomAdapter adapter = new CustomAdapter(currentActivity, questionList);
                lv.setAdapter(adapter);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {   //User selects one of these museums
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        DecimalFormat percentageFormat = new DecimalFormat("00.00");
                        String finalPercentage;

                        questionTxtV.setText(questionList.get(position));
                        int totalAns=correctAns.get(position)+wrongAns.get(position);
                        double corPerCent=(double)correctAns.get(position)/totalAns;
                        corPerCent=corPerCent*100;
                        finalPercentage= percentageFormat.format(corPerCent);
                        correctTxtV.setText(getResources().getString(R.string.correct_answers) + " ("+correctAns.get(position)+") "+finalPercentage+"%");
                        double wroPerCent=(double)wrongAns.get(position)/totalAns;
                        wroPerCent=wroPerCent*100;
                        finalPercentage= percentageFormat.format(wroPerCent);
                        wrongTxtV.setText(getResources().getString(R.string.wrong_answers)+ " ("+wrongAns.get(position)+") "+finalPercentage+"%");

                    }
                });
            }
        });
    }





    public void OnClickSwitchBackToMBP(View iv)     //return playing with museum ob byzantine culture
    {
        SharedPreferences settings = getApplicationContext().getSharedPreferences(MainActivity.WORKING_ON_EXTERNAL_MUSEUM_PREF_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(MainActivity.WORKING_ON_EXTERNAL_MUSEUM_VAR_KEY, false);   //save that we are not using external museum
        // Apply the edits!
        editor.apply();
        //Restart
        Intent refresh = new Intent(getBaseContext(), MainActivity.class);
        finish();
        startActivity(refresh);
    }

    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        menu.hideNavBar(this.getWindow());
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




