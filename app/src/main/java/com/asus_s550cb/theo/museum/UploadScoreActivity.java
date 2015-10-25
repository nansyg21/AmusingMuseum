package com.asus_s550cb.theo.museum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by panos on 7/10/2015.
 */
public class UploadScoreActivity extends Activity {

    ImageView imgv;
    EditText nameField;
    public static String LOCALLY_SAVED_DATA_PREFERENCE_NAME = "LOCALLY_SAVED_DATA"; //use these to save data locally
    public static String LOCALLY_SAVED_NAME = "LOCALLY_SAVED_NAME";
    public static String LOCALLY_SAVED_SCORE= "LOCALLY_SAVED_SCORE";
    public static String LOCALLY_SAVED_DATE = "LOCALLY_SAVED_DATE";
    boolean scoreUploaded=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);//hide status bar
        setContentView(R.layout.activity_upload_score);

        // ------------------ Code in order to hide the navigation bar -------------------- //
        menu.hideNavBar(this.getWindow());

        TextView scoreTxtView = (TextView) findViewById(R.id.upload_score_num_txt);
        scoreTxtView.setText(Score.TotalScore+"");

    }

    public void UploadScoreSubmit(View v)
    {
        //Server at:83.212.117.226      DB: AmusingMuseumDB       Table: AmusingMuseumScores
        nameField = (EditText) findViewById(R.id.upload_score_name_txt);
        if(nameField.length()==0)
        {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.upload_empty_name_small)
                    .setMessage(R.string.upload_empty_name_large)
                    .setPositiveButton(R.string.confirm_exit_οκ, null).create().show();
        }
        else {
            // try to upload to server
            //if there is no connection it can be saved locally
            if (!scoreUploaded) {
                Thread thread = new Thread(new Runnable()
                {
                    @Override
                    public void run() {
                        try {
                            URL url;
                            String response = "";
                            String name = nameField.getText().toString();   //collect data: name-score-date
                            Calendar c = Calendar.getInstance();
                            String date = c.get(Calendar.DAY_OF_MONTH) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.YEAR);
                            ShowErrorOnView("");    //clear error log

                            //UBUNTU LTS Server on okeanos.grnet.gr
                            url = new URL("http://83.212.117.226/SaveData.php?date=" + date + "&score=" + Score.TotalScore + "&name=" + name);
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

                                Log.w("Warn", "Response: " + response);
                                if (response.contains("Integrity constraint violation")) //name already used on database
                                    ShowErrorOnView(getBaseContext().getString(R.string.error_on_upload_name_used));
                                else if (response.equals("Success")) {
                                    ShowErrorOnView(getBaseContext().getString(R.string.score_successfully_uploaded));
                                    scoreUploaded = true;
                                }
                            } else
                                ShowErrorOnView(getBaseContext().getString(R.string.error_try_later_large));

                        } catch (ConnectException ce) //connection exception
                        {
                            ShowErrorOnView(getBaseContext().getString(R.string.error_no_internet_connection_large));
                        } catch (Exception e)     //general exception
                        {
                            ShowErrorOnView(getBaseContext().getString(R.string.error_try_later_large));
                        }
                    }
                });
                thread.start();
            }
            else
            {
                TextView errView = (TextView) findViewById(R.id.ErrorView);
                errView.setText(R.string.upload_locally_already_saved);

            }

        }


    }

    public void ShowErrorOnView(final String err)   //use UI Thread to update view from worker thread
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView errorview = (TextView) findViewById(R.id.ErrorView);

                errorview.setText(err);
            }
        });
    }

    public void UploadScoreQuit(View v)   //On cancel button app closes
    {
        finish();
        Intent itn= new Intent(getApplicationContext(), menu.class); //go to menu screen with proper flag set
        itn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        itn.putExtra("leaving", true);
        startActivity(itn);
    }

    public void SaveScoreLocally(View v)
    {
        nameField = (EditText) findViewById(R.id.upload_score_name_txt);
        if(nameField.length()==0)
        {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.upload_empty_name_small)
                    .setMessage(R.string.upload_empty_name_large)
                    .setPositiveButton(R.string.confirm_exit_οκ, null).create().show();
        }
        else {

            if (!scoreUploaded)
            {
                nameField = (EditText) findViewById(R.id.upload_score_name_txt);
                String name = nameField.getText().toString();   //collect data: name-score-date
                Calendar c = Calendar.getInstance();
                String date = c.get(Calendar.DAY_OF_MONTH) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.YEAR);

                SharedPreferences settings = getApplicationContext().getSharedPreferences(LOCALLY_SAVED_DATA_PREFERENCE_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(LOCALLY_SAVED_NAME, name);
                editor.putInt(LOCALLY_SAVED_SCORE, Score.TotalScore);
                editor.putString(LOCALLY_SAVED_DATE, date);

                // Apply the edits!
                editor.apply();

                TextView errView = (TextView) findViewById(R.id.ErrorView);
                errView.setText(R.string.save_score_locally_successfully);
                scoreUploaded = true;
            } else {
                TextView errView = (TextView) findViewById(R.id.ErrorView);
                errView.setText(R.string.upload_locally_already_saved);
            }
        }
    }

    // Reset the flags to hide the navigation bar
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
