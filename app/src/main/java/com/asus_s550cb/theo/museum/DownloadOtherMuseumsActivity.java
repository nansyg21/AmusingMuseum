package com.asus_s550cb.theo.museum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
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
    static Bitmap bmImg;

    public DownloadableMuseum myNewMuseum= new DownloadableMuseum();
    public ArrayList<DownloadableMuseum.RoomsForNewMuseum> rooms= new ArrayList<DownloadableMuseum.RoomsForNewMuseum>();
    int currentRoom=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);//hide status bar
        setContentView(R.layout.activity_download_other_museums);

        // ------------------ Code in order to hide the navigation bar -------------------- //
        menu.hideNavBar(this.getWindow());

        TextView tv = (TextView) findViewById(R.id.download_switch_to_mbp_text);
        if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
            tv.setVisibility(View.VISIBLE);
        else
            tv.setVisibility(View.INVISIBLE);


        ImageView bt = (ImageView) findViewById(R.id.download_other_museums_back_button);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SeeAllMuseums();//Find out all available Museums to download
    }

    public void SeeAllMuseums() //Contact with server using php, method POST and receive all museums
    {
        currentActivity = this;
        Thread thread = new Thread(new Runnable() {//using thread to handle connections
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
                    ShowErrors(getBaseContext().getString(R.string.error_no_internet_connection_large));
                } catch (Exception e)     //general exception
                {
                    e.printStackTrace();
                    ShowErrors(getBaseContext().getString(R.string.error_try_later_large));
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

                ListView lv = (ListView) findViewById(R.id.download_other_museums_list_view);
                ArrayList<String> values = new ArrayList<String>();
                StringTokenizer st = new StringTokenizer(res, "|");      //m_id1,museum_name1|m_id2,museum_name2|..
                String str = "";

                while (st.hasMoreElements())                            //separate
                {
                    str = (String) st.nextElement();
                    String[] parts = str.split(",");
                    values.add(parts[1]);
                    //  Log.w("Warn", "Added to list:  0:" + parts[0] + " 1: " + parts[1]);
                }

                final CustomAdapter adapter = new CustomAdapter(currentActivity, values);
                lv.setAdapter(adapter);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {   //User selects one of these museums
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Log.w("Warn", "clicked at: " + (position + 1)); // 200: OK, The request was fulfilled.
                        DownloadMuseum(position + 1);//in Museums table id column starts from 1
                    }
                });
            }
        });
    }

    public void DownloadMuseum(int museumId)      //gets data from server and splits them
    {
        try {
            String s = new DownloadMuseumTask().execute(museumId+"").get();  //DownloadMuseumTask works with String
            Log.w("Warn", "TASK: "+s);
            //split by && to receive all rows: number_of_rooms*12
            //for each row: split by --- to separate data from tables   1)Museums,2)Rooms,3)Questions - Answers
            // 1)Museums    1
            // 2)Rooms      number_of_rooms
            // 3)Questions  number_of_rooms*3
            // 4)Answers    Questions*4*number_of_rooms

            StringTokenizer stRows = new StringTokenizer(s.replaceAll("<br>",""),"&&"); //remove <br>

            for(int i=0;i<(stRows.countTokens())/12 ;i++)
                rooms.add(new DownloadableMuseum.RoomsForNewMuseum());
            Log.w("Warn", "Total rows: " + stRows.countTokens() +" Added "+rooms.size() +" rooms");

            int row=0, currentRoom=-1;
            int answerIndex=1; //which answer we are currently reading, from 1 to 4
            while(stRows.hasMoreTokens())
            {
                currentRoom=row/12+1;
                String line = stRows.nextToken();
                StringTokenizer stInLine = new StringTokenizer(line,"---"); //each line holds a table row
                String str =stInLine.nextToken();
                Log.w("Warn", "Line: " + str);
                SeparateMuseum(str);
                str=stInLine.nextToken();
                Log.w("Warn", "Line: " + str);
                SeparateRooms(str, currentRoom);
                str= stInLine.nextToken();
                Log.w("Warn", "Line: " + str);
                SeparateQuestionsAndAnswers(str, answerIndex, currentRoom);

                answerIndex++;
                if(answerIndex>4)
                    answerIndex=1;

                //Log.w("Warn","Row= "+row +" Room: "+(row/12+1));
                row++;

            }
            myNewMuseum.RoomsList=rooms;

            DownloadMuseumImages(myNewMuseum);
            SaveMuseumLocally(myNewMuseum);


            //Verify museum changing
            new AlertDialog.Builder(this)
                    .setTitle(R.string.new_museum)
                    .setMessage(R.string.download_change_museum_confirm)
                    .setNegativeButton(R.string.confirm_exit_cancel, null)
                    .setPositiveButton(R.string.confirm_exit_οκ, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            //TODO  change data...
                            SharedPreferences settings = getApplicationContext().getSharedPreferences(MainActivity.WORKING_ON_EXTERNAL_MUSEUM_PREF_NAME, 0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putBoolean(MainActivity.WORKING_ON_EXTERNAL_MUSEUM_VAR_KEY,true);
                            // Apply the edits!
                            editor.apply();

                            //Restart
                            Intent refresh = new Intent(getBaseContext(), MainActivity.class);
                            finish();
                            startActivity(refresh);

                        }
                    }).create().show();

        } catch (Exception e)     //general exception
        {
            e.printStackTrace();
            ShowErrors(getBaseContext().getString(R.string.error_try_later_large));
        }

    }

    public void SeparateMuseum(String str)  //saves general data about Museum Downloaded
    {
        StringTokenizer stMuseum= new StringTokenizer(str,"|");
        //Log.w("Warn","Museum: "+stMuseum.countTokens());
        while(stMuseum.hasMoreTokens()) {
            String pairs[] = stMuseum.nextToken().split(":");
            //Log.w("Warn","0:"+pairs[0] +" 1:"+pairs[1]);
            switch (pairs[0]){
                case "username":
                    myNewMuseum.username=pairs[1];
                    break;
                case "museum_name":
                    myNewMuseum.museum_name=pairs[1];
                    break;
                case "museum_name_gr":
                    myNewMuseum.museum_name_gr=pairs[1];
                    break;
                case "logo":
                    myNewMuseum.logo=pairs[1];
                    break;
                case "floor_plan":
                    myNewMuseum.floor_plan=pairs[1];
                    break;
                case "number_of_rooms":
                    myNewMuseum.number_of_rooms=Integer.parseInt(pairs[1]);
                    break;
                default:
                    break;
            }
        }
    }

    public void SeparateRooms(String str, int currentRoom)   //saves data for each Museum Room
    {

        StringTokenizer stRoom= new StringTokenizer(str,"|");
        //Log.w("Warn","Room: "+stRoom.countTokens());
        while(stRoom.hasMoreTokens()) {
            String pairs[] = stRoom.nextToken().split(":");
//            Log.w("Warn","0:"+pairs[0] +" 1:"+pairs[1]);

            switch (pairs[0]){
                case "m_id":
                    myNewMuseum.m_id=Integer.parseInt( pairs[1]);
                    break;
                case "r_id":
                    rooms.get(currentRoom-1).r_id=Integer.parseInt(pairs[1]);
                    break;
                case "e_id":
                    rooms.get(currentRoom-1).e_id=Integer.parseInt( pairs[1]);
                    break;
                case "e2_id":
                    rooms.get(currentRoom-1).e2_id=Integer.parseInt( pairs[1]);
                    break;
                case "e3_id":
                    rooms.get(currentRoom-1).e3_id=Integer.parseInt( pairs[1]);
                    break;
                case "room_name":
                    rooms.get(currentRoom-1).room_name= pairs[1];
                    break;
                case "room_name_gr":
                    rooms.get(currentRoom-1).room_name_gr= pairs[1];
                    break;
                case "hint1":
                    rooms.get(currentRoom-1).hint1= pairs[1];
                    break;
                case "hint1_gr":
                    rooms.get(currentRoom-1).hint1_gr= pairs[1];
                    break;
                case "hint2":
                    rooms.get(currentRoom-1).hint2= pairs[1];
                    break;
                case "hint2_gr":
                    rooms.get(currentRoom-1).hint2_gr= pairs[1];
                    break;
                case "hintImg1":
                    rooms.get(currentRoom-1).hintImg1 = pairs[1];
                    break;
                case "hintImg2":
                    rooms.get(currentRoom-1).hintImg2= pairs[1];
                    break;
                default:
                    break;

            }
        }
    }

    public void SeparateQuestionsAndAnswers(String str, int answerIndex, int currentRoom)
    {
        StringTokenizer stQuestionsAndAnswers= new StringTokenizer(str,"|");
        Log.w("Warn","QuestionsAndAnswers: "+stQuestionsAndAnswers.countTokens());
        int currentQuestionIndex=-1;
        int currentAnswerID=-1;
        String currentAnswerText="",currentAnswerText_gr="";

        while(stQuestionsAndAnswers.hasMoreTokens()) {
            String pairs[] = stQuestionsAndAnswers.nextToken().split(":");
            Log.w("Warn","0:"+pairs[0] +" 1:"+pairs[1]);

            switch (pairs[0]){
                case "e_id":
                    Log.w("Warn","PAIR[1]:"+pairs[1] +" ID1):"+rooms.get(currentRoom-1).e_id+" 2)"+rooms.get(currentRoom-1).e2_id +" 3)"+rooms.get(currentRoom-1).e3_id);
                    if(Integer.parseInt(pairs[1])==rooms.get(currentRoom-1).e_id) //first save question id
                        currentQuestionIndex = 1;
                    else if(Integer.parseInt(pairs[1])==rooms.get(currentRoom-1).e2_id)
                        currentQuestionIndex = 2;
                    else if(Integer.parseInt(pairs[1])==rooms.get(currentRoom-1).e3_id)
                        currentQuestionIndex = 3;

                    break;

                case "a_id":                                 //then save answer id
                    currentAnswerID=Integer.parseInt(pairs[1]);

                case "e_text":
                    if(currentQuestionIndex==1)
                        rooms.get(currentRoom-1).e_text = pairs[1];
                    else if(currentQuestionIndex==2)
                        rooms.get(currentRoom-1).e2_text=pairs[1];
                    else if(currentQuestionIndex==3)
                        rooms.get(currentRoom-1).e3_text=pairs[1];
                    break;

                case "e_text_gr":
                    if(currentQuestionIndex==1)
                        rooms.get(currentRoom-1).e_text_gr = pairs[1];
                    else if(currentQuestionIndex==2)
                        rooms.get(currentRoom-1).e2_text_gr=pairs[1];
                    else if(currentQuestionIndex==3)
                        rooms.get(currentRoom-1).e3_text_gr=pairs[1];
                    break;
                case "a_text":                              // save answers text
                    currentAnswerText=pairs[1];
                    break;
                case "a_text_gr":
                    currentAnswerText_gr=pairs[1];
                    break;
                case "correct":             //after all data are collected we save them
                    switch (currentQuestionIndex){      //for each question - 3 questions
                        case 1:
                            if(answerIndex==1) {        //for each answer   - 4 answers
                                rooms.get(currentRoom-1).a1_1_id = currentAnswerID ;
                                rooms.get(currentRoom-1).a1_1_text=currentAnswerText;
                                rooms.get(currentRoom-1).a1_1_text_gr=currentAnswerText_gr;
                                if(pairs[1].equals("T")){
                                    rooms.get(currentRoom-1).correct1_id=currentAnswerID;
                                    rooms.get(currentRoom-1).correct1_text=currentAnswerText;
                                    rooms.get(currentRoom-1).correct1_text_gr=currentAnswerText_gr;
                                }
                            }
                            else if(answerIndex==2) {
                                rooms.get(currentRoom-1).a1_2_id = currentAnswerID ;
                                rooms.get(currentRoom-1).a1_2_text=currentAnswerText;
                                rooms.get(currentRoom-1).a1_2_text_gr=currentAnswerText_gr;
                                if(pairs[1].equals("T")){
                                    rooms.get(currentRoom-1).correct1_id=currentAnswerID;
                                    rooms.get(currentRoom-1).correct1_text=currentAnswerText;
                                    rooms.get(currentRoom-1).correct1_text_gr=currentAnswerText_gr;
                                }
                            }
                            else if(answerIndex==3) {
                                rooms.get(currentRoom-1).a1_3_id = currentAnswerID ;
                                rooms.get(currentRoom-1).a1_3_text=currentAnswerText;
                                rooms.get(currentRoom-1).a1_3_text_gr=currentAnswerText_gr;
                                if(pairs[1].equals("T")){
                                    rooms.get(currentRoom-1).correct1_id=currentAnswerID;
                                    rooms.get(currentRoom-1).correct1_text=currentAnswerText;
                                    rooms.get(currentRoom-1).correct1_text_gr=currentAnswerText_gr;
                                }
                            }
                            else if(answerIndex==4) {
                                rooms.get(currentRoom-1).a1_4_id = currentAnswerID ;
                                rooms.get(currentRoom-1).a1_4_text=currentAnswerText;
                                rooms.get(currentRoom-1).a1_4_text_gr=currentAnswerText_gr;
                                if(pairs[1].equals("T")){
                                    rooms.get(currentRoom-1).correct1_id=currentAnswerID;
                                    rooms.get(currentRoom-1).correct1_text=currentAnswerText;
                                    rooms.get(currentRoom-1).correct1_text_gr=currentAnswerText_gr;
                                }
                            }
                            break;
                        case 2:
                            if(answerIndex==1) {
                                rooms.get(currentRoom-1).a2_1_id = currentAnswerID ;
                                rooms.get(currentRoom-1).a2_1_text=currentAnswerText;
                                rooms.get(currentRoom-1).a2_1_text_gr=currentAnswerText_gr;
                                if(pairs[1].equals("T")){
                                    rooms.get(currentRoom-1).correct2_id=currentAnswerID;
                                    rooms.get(currentRoom-1).correct2_text=currentAnswerText;
                                    rooms.get(currentRoom-1).correct2_text_gr=currentAnswerText_gr;
                                }
                            }
                            else if(answerIndex==2) {
                                rooms.get(currentRoom-1).a2_2_id = currentAnswerID ;
                                rooms.get(currentRoom-1).a2_2_text=currentAnswerText;
                                rooms.get(currentRoom-1).a2_2_text_gr=currentAnswerText_gr;
                                if(pairs[1].equals("T")){
                                    rooms.get(currentRoom-1).correct2_id=currentAnswerID;
                                    rooms.get(currentRoom-1).correct2_text=currentAnswerText;
                                    rooms.get(currentRoom-1).correct2_text_gr=currentAnswerText_gr;
                                }
                            }
                            else if(answerIndex==3) {
                                rooms.get(currentRoom-1).a2_3_id = currentAnswerID ;
                                rooms.get(currentRoom-1).a2_3_text=currentAnswerText;
                                rooms.get(currentRoom-1).a2_3_text_gr=currentAnswerText_gr;
                                if(pairs[1].equals("T")){
                                    rooms.get(currentRoom-1).correct2_id=currentAnswerID;
                                    rooms.get(currentRoom-1).correct2_text=currentAnswerText;
                                    rooms.get(currentRoom-1).correct2_text_gr=currentAnswerText_gr;
                                }
                            }
                            else if(answerIndex==4) {
                                rooms.get(currentRoom-1).a2_4_id = currentAnswerID ;
                                rooms.get(currentRoom-1).a2_4_text=currentAnswerText;
                                rooms.get(currentRoom-1).a2_4_text_gr=currentAnswerText_gr;
                                if(pairs[1].equals("T")){
                                    rooms.get(currentRoom-1).correct2_id=currentAnswerID;
                                    rooms.get(currentRoom-1).correct2_text=currentAnswerText;
                                    rooms.get(currentRoom-1).correct2_text_gr=currentAnswerText_gr;
                                }
                            }
                            break;
                        case 3:
                            if(answerIndex==1) {
                                rooms.get(currentRoom-1).a3_1_id = currentAnswerID ;
                                rooms.get(currentRoom-1).a3_1_text=currentAnswerText;
                                rooms.get(currentRoom-1).a3_1_text_gr=currentAnswerText_gr;
                                if(pairs[1].equals("T")){
                                    rooms.get(currentRoom-1).correct3_id=currentAnswerID;
                                    rooms.get(currentRoom-1).correct3_text=currentAnswerText;
                                    rooms.get(currentRoom-1).correct3_text_gr=currentAnswerText_gr;
                                }
                            }
                            else if(answerIndex==2) {
                                rooms.get(currentRoom-1).a3_2_id = currentAnswerID ;
                                rooms.get(currentRoom-1).a3_2_text=currentAnswerText;
                                rooms.get(currentRoom-1).a3_2_text_gr=currentAnswerText_gr;
                                if(pairs[1].equals("T")){
                                    rooms.get(currentRoom-1).correct3_id=currentAnswerID;
                                    rooms.get(currentRoom-1).correct3_text=currentAnswerText;
                                    rooms.get(currentRoom-1).correct3_text_gr=currentAnswerText_gr;
                                }
                            }
                            else if(answerIndex==3) {
                                rooms.get(currentRoom-1).a3_3_id = currentAnswerID ;
                                rooms.get(currentRoom-1).a3_3_text=currentAnswerText;
                                rooms.get(currentRoom-1).a3_3_text_gr=currentAnswerText_gr;
                                if(pairs[1].equals("T")){
                                    rooms.get(currentRoom-1).correct3_id=currentAnswerID;
                                    rooms.get(currentRoom-1).correct3_text=currentAnswerText;
                                    rooms.get(currentRoom-1).correct3_text_gr=currentAnswerText_gr;
                                }
                            }
                            else if(answerIndex==4) {
                                rooms.get(currentRoom-1).a3_4_id = currentAnswerID ;
                                rooms.get(currentRoom-1).a3_4_text=currentAnswerText;
                                rooms.get(currentRoom-1).a3_4_text_gr=currentAnswerText_gr;
                                if(pairs[1].equals("T")){
                                    rooms.get(currentRoom-1).correct3_id=currentAnswerID;
                                    rooms.get(currentRoom-1).correct3_text=currentAnswerText;
                                    rooms.get(currentRoom-1).correct3_text_gr=currentAnswerText_gr;
                                }
                            }
                            break;
                    }
                    break;
                default:
                    break;

            }
        }
    }

    public void DownloadMuseumImages(DownloadableMuseum m) //Download images logo, exhibits etc
    {
        try {
            m.SaveLogoAsSerializable(new DownloadImageTask().execute("http://83.212.117.226/uploads/" + myNewMuseum.logo).get());
            m.SaveFloorPlanAsSerializable(new DownloadImageTask().execute("http://83.212.117.226/uploads/" + myNewMuseum.floor_plan).get());

            for (DownloadableMuseum.RoomsForNewMuseum r : m.RoomsList) {
                r.SaveHintImage1AsSerializable(new DownloadImageTask().execute("http://83.212.117.226/uploads/" + r.hintImg1).get());
                r.SaveHintImage2AsSerializable( new DownloadImageTask().execute("http://83.212.117.226/uploads/" + r.hintImg2).get());
            }
        } catch (Exception e) {
            Log.w("Warn","Error on downloading images");
            e.printStackTrace();
        }
    }

    public void SaveMuseumLocally(DownloadableMuseum museum)    //use Outputstream to output our Object to internal memory: Convert to string and save in preference
    {
        // SharedPreferences settings = getApplicationContext().getSharedPreferences(LOCALLY_SAVED_DATA_PREFERENCE_NAME, 0);
        SharedPreferences mPrefs = getApplicationContext().getSharedPreferences(MainActivity.WORKING_ON_EXTERNAL_MUSEUM_PREF_NAME,0);
        SharedPreferences.Editor ed = mPrefs.edit();
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutput;
        try {
            objectOutput = new ObjectOutputStream(arrayOutputStream);
            objectOutput.writeObject(museum);
            byte[] data = arrayOutputStream.toByteArray();

            objectOutput.close();
            arrayOutputStream.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Base64OutputStream b64 = new Base64OutputStream(out, Base64.DEFAULT);
            b64.write(data);
            b64.flush();
            b64.close();
            out.close();

            ed.putString(DownloadableMuseum.LOCALLY_SAVED_MUSEUM_PREFERENCE_KEY, new String(out.toByteArray()));

            ed.commit();
        } catch (IOException e) {
            Log.w("Warn","Error on saving Museum Object");
            e.printStackTrace();
        }
    }

    public void OnClickSwitchBackToMBP(View iv)     //return playing with museum ob byzantine culture
    {
        SharedPreferences settings = getApplicationContext().getSharedPreferences(MainActivity.WORKING_ON_EXTERNAL_MUSEUM_PREF_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(MainActivity.WORKING_ON_EXTERNAL_MUSEUM_VAR_KEY,false);   //save that we are not using external museum
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

    private class DownloadMuseumTask extends AsyncTask<String, Void, String> { //<Params, Progress, Result>
        String response = "";
        public DownloadMuseumTask() {
        }

        protected String doInBackground(String... museumId)//talks to server downloading data for museum from database
        {
            try {
                URL url;

                //UBUNTU LTS Server on okeanos.grnet.gr
                url = new URL("http://83.212.117.226/DownloadMuseum.php?museum_id="+museumId[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                int responseCode = conn.getResponseCode();// 200: OK, The request was fulfilled.
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                }

            } catch (ConnectException ce) //connection exception
            {
                //ce.printStackTrace();
                ShowErrors(getBaseContext().getString(R.string.error_no_internet_connection_large));
            } catch (Exception e)     //general exception
            {
                e.printStackTrace();
                ShowErrors(getBaseContext().getString(R.string.error_try_later_large));
            }
            return response;
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {   //<Params, Progress, Result>
        public DownloadImageTask() {
        }
        protected Bitmap doInBackground(String... urls) {    //Undefined number of arguments of type string
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

    }

}




