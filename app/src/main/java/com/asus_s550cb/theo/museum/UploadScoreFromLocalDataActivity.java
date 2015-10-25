package com.asus_s550cb.theo.museum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by panos on 13/10/2015.
 */
public class UploadScoreFromLocalDataActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);//hide status bar
        setContentView(R.layout.activity_upload_score_from_local_data);

        // ------------------ Code in order to hide the navigation bar -------------------- //
        menu.hideNavBar(this.getWindow());

        FindAndUploadLocallySavedData();
    }

    public void FindAndUploadLocallySavedData()
    {


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Get data from SharedPreferences
                SharedPreferences settings = getApplicationContext().getSharedPreferences(UploadScoreActivity.LOCALLY_SAVED_DATA_PREFERENCE_NAME, 0);
                String name = settings.getString(UploadScoreActivity.LOCALLY_SAVED_NAME, null);
                int score = settings.getInt(UploadScoreActivity.LOCALLY_SAVED_SCORE, -1);
                String date = settings.getString(UploadScoreActivity.LOCALLY_SAVED_DATE, null);

                if(name==null || score==-1 || date==null)   //no data found: do nothing
                {
                    TextView errorview = (TextView) findViewById(R.id.UploadLocalDataScreenText);
                    errorview.setText(R.string.upload_locally_no_saved_data_found);
                }
                else
                {
                    try {
                        URL url;
                        String response = "";


                        //UBUNTU LTS Server on okeanos.grnet.gr
                        url = new URL("http://83.212.117.226/SaveData.php?date=" + date + "&score=" + score + "&name=" + name);
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
                        Log.w("Warn", "ResponseCode: " + responseCode); // 200: OK, The request was fulfilled.
                        if (responseCode == HttpsURLConnection.HTTP_OK) {
                            String line;
                            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            while ((line = br.readLine()) != null) {
                                response += line;
                            }

                            // Log.w("Warn","Response: "+response);
                            if (response.contains("Integrity constraint violation")) //name already used on database
                            {
                                ShowErrorOnLocalUploadView(name, score, date, getBaseContext().getString(R.string.error_on_upload_name_used));
                                EnableNameChangingLayout();

                            } else if (response.equals("Success"))
                            {    //on successfully upload: delete locally saved data
                                ShowErrorOnLocalUploadView(name, score, date, getBaseContext().getString(R.string.score_successfully_uploaded));
                                SharedPreferences.Editor editor = settings.edit();
                                editor.remove(UploadScoreActivity.LOCALLY_SAVED_NAME);
                                editor.remove(UploadScoreActivity.LOCALLY_SAVED_SCORE);
                                editor.remove(UploadScoreActivity.LOCALLY_SAVED_DATE);
                                editor.apply();

                                runOnUiThread(new Runnable() {  //remove upload button etc
                                    @Override
                                    public void run() {
                                        LinearLayout change_name_layout = (LinearLayout) findViewById(R.id.Upload_Score_from_local_change_name_layout);
                                        change_name_layout.setVisibility(View.INVISIBLE);
                                    }
                                });

                                //    finish();

                            }
                        } else
                            ShowErrorOnLocalUploadView(name, score, date, getBaseContext().getString(R.string.error_try_later_large));

                    } catch (ConnectException ce) //connection exception
                    {
                        ShowErrorOnLocalUploadView(name, score, date, getBaseContext().getString(R.string.error_no_internet_connection_large));
                    } catch (Exception e)     //general exception
                    {
                        e.printStackTrace();
                        ShowErrorOnLocalUploadView(name, score, date, getBaseContext().getString(R.string.error_try_later_large));
                    }
                }
            }
        });
        thread.start();
    }

    public void ShowErrorOnLocalUploadView(final String name, final int score, final String date, final String err)   //use UI Thread to update view from worker thread
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView errorview = (TextView) findViewById(R.id.UploadLocalDataScreenText);
                errorview.setText("Name: " + name + "\nScore: " + score + "\n" + err);
            }
        });
    }

    public void EnableNameChangingLayout()        //user can change the saved name if it's already used in the database
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {         //using the views only through the UI Thread
                LinearLayout change_name_layout = (LinearLayout) findViewById(R.id.Upload_Score_from_local_change_name_layout);
                change_name_layout.setVisibility(View.VISIBLE);

                Button uploadBtn = (Button) findViewById(R.id.Upload_Score_from_local_change_name_btn);
                uploadBtn.setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View v) {
                        TextView errorview = (TextView) findViewById(R.id.UploadLocalDataScreenText);
                        EditText tempNameField = (EditText) findViewById(R.id.Upload_Score_from_local_name_txt);

                        if (tempNameField.length() == 0)   //empty name field
                            errorview.setText(R.string.upload_empty_name_large);

                        else                //override saved name and upload to server
                        {
                            SharedPreferences settings = getApplicationContext().getSharedPreferences(UploadScoreActivity.LOCALLY_SAVED_DATA_PREFERENCE_NAME, 0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString(UploadScoreActivity.LOCALLY_SAVED_NAME, tempNameField.getText().toString());
                            // Apply the edits!
                            editor.apply();

                            FindAndUploadLocallySavedData();
                        }
                    }
                });


            }
        });
    }

    public void UploadLocalDataBackBtnOnClick(View v)
    {
        finish();
    }

    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
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

                    public void onClick(DialogInterface arg0, int arg1)
                    {
                        finish();
                        Intent itn= new Intent(getApplicationContext(), menu.class); //go to menu screen with proper flag set
                        itn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        itn.putExtra("leaving", true);
                        startActivity(itn);
                    }
                }).create().show();
    }

}


