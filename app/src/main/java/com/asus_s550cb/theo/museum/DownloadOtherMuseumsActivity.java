package com.asus_s550cb.theo.museum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.net.ssl.HttpsURLConnection;


/**
 * Created by panos on 21/10/2015.
 */
public class DownloadOtherMuseumsActivity extends Activity {


    Activity currentActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);//hide status bar
        setContentView(R.layout.activity_download_other_museums);

        // ------------------ Code in order to hide the navigation bar -------------------- //
        menu.hideNavBar(this.getWindow());

        ImageView bt=(ImageView) findViewById(R.id.download_other_museums_back_button);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //DB Test
        OtherMuseumsSQLiteDB dbtemp = new OtherMuseumsSQLiteDB(this.getBaseContext());


        //TODO: Contact with server using php, method POST and receive all museums
        SeeAllMuseums();
        //TODO: A dropdown list is shown: 2 rows (1: museum id, 2: museum name)
        //TODO: User selects one of these museums
        //TODO: Contact with server telling the museum we want
        //TODO: Server sends all data for the museum
    }

    public void SeeAllMuseums() //Contact with server using php, method POST and receive all museums
    {
        currentActivity= this;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    URL url;
                    String response = "";

                    //UBUNTU LTS Server on okeanos.grnet.gr
                    url = new URL("http://83.212.117.226/SeeAllMuseums.php");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    int responseCode = conn.getResponseCode();
                    Log.w("Warn", "ResponseCode: " + responseCode); // 200: OK, The request was fulfilled.
                    if (responseCode == HttpsURLConnection.HTTP_OK)
                    {
                        String line;
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        while ((line = br.readLine()) != null) {
                            response += line;
                        }
                        Log.w("Warn","Response: "+response);
                        SeparateResponseAndShowDataOnView(response);
                    }


                }
                catch (ConnectException ce) //connection exception
                {
                    ce.printStackTrace();
                    // ShowErrorOnView(getBaseContext().getString(R.string.error_no_internet_connection_large ));
                }
                catch (Exception e)     //general exception
                {
                    e.printStackTrace();
                    // ShowErrorOnView(getBaseContext().getString(R.string.error_try_later_large ));
                }
            }
        });
        thread.start();
    }

    public void SeparateResponseAndShowDataOnView(final String res)
    {
        runOnUiThread(new Runnable() {      //only main thread interacts with Views
            @Override
            public void run() {

                ListView lv =  (ListView) findViewById(R.id.download_other_museums_list_view);
                ArrayList<String> values = new ArrayList<String>();
                StringTokenizer st = new StringTokenizer(res, "|");      //m_id1,museum_name1|m_id2,museum_name2|..
                String str="";

                while (st.hasMoreElements())                            //separate
                {
                    str= (String)st.nextElement();
                    String[] parts = str.split(",");
                    values.add(parts[1]);
                    Log.w("Warn", "Added to list ");

                }

                CustomAdapter adapter = new CustomAdapter(currentActivity, values);
                lv.setAdapter(adapter);


            }


        });

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


