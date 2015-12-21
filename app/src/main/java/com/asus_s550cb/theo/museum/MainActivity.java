package com.asus_s550cb.theo.museum;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Base64InputStream;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;


public class MainActivity extends AppCompatActivity {

    public int height,width;
    public static String WORKING_ON_EXTERNAL_MUSEUM_PREF_NAME="WORKING_ON_EXTERNAL_MUSEUM_PREF_NAME";//working with: Museum of Byzantine Culture of Thessaloniki or not
    public static String WORKING_ON_EXTERNAL_MUSEUM_VAR_KEY="WORKING_ON_EXTERNAL_MUSEUM_VAR_KEY";
    public static boolean WORKING_ON_EXTERNAL_MUSEUM;
    public static DownloadableMuseum EXTERNAL_MUSEUM;
    public static Context my_context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        menu.hideNavBar(this.getWindow());
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        height = displaymetrics.heightPixels;
        width = displaymetrics.widthPixels;


        setContentView(new SampleView(this));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), menu.class));
                finish();
            }
        }, 1000);

    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    private class SampleView extends View {

        Bitmap logo= null;
        Bitmap museum_logo=null;

        // CONSTRUCTOR
        public SampleView(Context context) {
            super(context);
            setFocusable(true);

            my_context=getBaseContext();
            SoundHandler.InitiateSoundPool();   //create object to handle sounds
            SharedPreferences settings = getApplicationContext().getSharedPreferences(MainActivity.WORKING_ON_EXTERNAL_MUSEUM_PREF_NAME, 0);
            WORKING_ON_EXTERNAL_MUSEUM = settings.getBoolean(MainActivity.WORKING_ON_EXTERNAL_MUSEUM_VAR_KEY, false);//if nothing found: return false
            Log.w("Warn","1)SAMPLE VIEW WORKING_ON_EXTERNAL_MUSEUM: "+ WORKING_ON_EXTERNAL_MUSEUM);
            //MainActivity.WORKING_ON_EXTERNAL_MUSEUM=false;
            if(WORKING_ON_EXTERNAL_MUSEUM)
            {
                EXTERNAL_MUSEUM =RetrieveSavedMuseum();
                if(EXTERNAL_MUSEUM==null)
                    WORKING_ON_EXTERNAL_MUSEUM=false;//could not retrieve it, return back to default museum
            }
            Log.w("Warn","2)SAMPLE VIEW WORKING_ON_EXTERNAL_MUSEUM: "+ WORKING_ON_EXTERNAL_MUSEUM);
            if(WORKING_ON_EXTERNAL_MUSEUM)
            {
                museum_logo =  EXTERNAL_MUSEUM.FromByteArrayToBitmap(EXTERNAL_MUSEUM.logoBmp.imageByteArray);
                museum_logo = Bitmap.createScaledBitmap(museum_logo, height / 2, height / 2, true);
            }
            else {
                museum_logo = BitmapFactory.decodeResource(getResources(), R.drawable.museum_logo);
                museum_logo = Bitmap.createScaledBitmap(museum_logo, height / 2, height / 2, true);
            }
            logo= BitmapFactory.decodeResource(getResources(), R.drawable.logo);
            logo= Bitmap.createScaledBitmap(logo, width / 2, height / 4, true);
        }

        @Override
        protected void onDraw(final Canvas canvas) {

            //Set Background Color
            canvas.drawColor(getResources().getColor(R.color.royal_blue));
            //Draw the logo
            canvas.drawBitmap(logo, (width / 2) / 2, (height * 2 / 10), null);
            canvas.drawBitmap(museum_logo, (width / 2) - (height / 4), (height * 5 / 10), null);

        }
    }

    public DownloadableMuseum RetrieveSavedMuseum()
    {
        DownloadableMuseum m=null;
        try {
            // = getApplicationContext().getSharedPreferences(UploadScoreActivity.LOCALLY_SAVED_DATA_PREFERENCE_NAME, 0);

            SharedPreferences mPrefs = getApplicationContext().getSharedPreferences(WORKING_ON_EXTERNAL_MUSEUM_PREF_NAME,0);
            byte[] bytes = mPrefs.getString(DownloadableMuseum.LOCALLY_SAVED_MUSEUM_PREFERENCE_KEY, "{}").getBytes();
            if (bytes.length == 0) {
                Log.w("Warn", "Error: No Stored Museum Found");
            }
            ByteArrayInputStream byteArray = new ByteArrayInputStream(bytes);
            Base64InputStream base64InputStream = new Base64InputStream(byteArray, Base64.DEFAULT);

            ObjectInputStream in;
            in = new ObjectInputStream(base64InputStream);
            m = (DownloadableMuseum) in.readObject();

            //Log.w("Warn","RETRIEVED");
            return  m;


        }
        catch (Exception e) {
            Log.w("Warn","Error on Getting Museum Object");
            e.printStackTrace();
            return null;
        }
    }

    public static String GetAllRoomNames()
    {
        String result="";
        for(int i=0;i<EXTERNAL_MUSEUM.RoomsList.size();i++)
        {
            if(menu.lang.equals("uk"))
                result+=(i+1)+"."+EXTERNAL_MUSEUM.RoomsList.get(i).room_name+"\n";
            else
                result+=(i+1)+"."+EXTERNAL_MUSEUM.RoomsList.get(i).room_name_gr+"\n";

        }
        return  result;
    }

    public static String[] GetAllRoomNamesAsList()
    {
        String[] result = new String[EXTERNAL_MUSEUM.RoomsList.size()];
        for(int i=0;i<EXTERNAL_MUSEUM.RoomsList.size();i++)
        {
            if(menu.lang.equals("uk"))
                result[i] = EXTERNAL_MUSEUM.RoomsList.get(i).room_name;
            else
                result[i]=  EXTERNAL_MUSEUM.RoomsList.get(i).room_name_gr;
        }
        return  result;
    }

    public static String[] GetAllHintsAsList()
    {
        String[] result = new String[EXTERNAL_MUSEUM.RoomsList.size()*2];
        int i=0;
        for(DownloadableMuseum.RoomsForNewMuseum r : EXTERNAL_MUSEUM.RoomsList)
        {
            if(menu.lang.equals("uk")) {
                result[i] = r.hint1;
                result[i+1] =r.hint2;
            }
            else {
                result[i] =r.hint1_gr;
                result[i + 1] =r.hint2_gr;
            }
            i+=2;
        }
        return  result;
    }

    public static String[] GetAllQuestions()
    {
        String[] result= new String[EXTERNAL_MUSEUM.RoomsList.size() *3];   //In each room: 3 questions
        int i=0;
        for(DownloadableMuseum.RoomsForNewMuseum room : EXTERNAL_MUSEUM.RoomsList)
        {
            if(menu.lang.equals("uk")) {
                result[i] =room.e_text;
                result[i+1] =room.e2_text;
                result[i+2] =room.e3_text;
            }
            else {
                result[i] =room.e_text_gr;
                result[i+1] =room.e2_text_gr;
                result[i+2] =room.e3_text_gr;
                Log.w("Warn", "QUESTIONS: 1)" +room.e_text_gr +" 2)"+ room.e2_text_gr +" 3)"+room.e3_text_gr);
            }
            i+=3;

        }
        return  result;
    }

    public static String[] GetAllAnswers()
    {
        String[] result= new String[EXTERNAL_MUSEUM.RoomsList.size() *12];   //In each room: 12 answers
        int i=0;
        for(DownloadableMuseum.RoomsForNewMuseum room : EXTERNAL_MUSEUM.RoomsList)
        {
            if(menu.lang.equals("uk")) {
                result[i] =room.a1_1_text;
                result[i+1] =room.a1_2_text;
                result[i+2] =room.a1_3_text;
                result[i+3] =room.a1_4_text;

                result[i+4] =room.a2_1_text;
                result[i+5] =room.a2_2_text;
                result[i+6] =room.a2_3_text;
                result[i+7] =room.a2_4_text;

                result[i+8] =room.a3_1_text;
                result[i+9] =room.a3_2_text;
                result[i+10] =room.a3_3_text;
                result[i+11] =room.a3_4_text;

            }
            else {
                result[i] =room.a1_1_text_gr;
                result[i+1] =room.a1_2_text_gr;
                result[i+2] =room.a1_3_text_gr;
                result[i+3] =room.a1_4_text_gr;

                result[i+4] =room.a2_1_text_gr;
                result[i+5] =room.a2_2_text_gr;
                result[i+6] =room.a2_3_text_gr;
                result[i+7] =room.a2_4_text_gr;

                result[i+8] =room.a3_1_text_gr;
                result[i+9] =room.a3_2_text_gr;
                result[i+10] =room.a3_3_text_gr;
                result[i+11] =room.a3_4_text_gr;
            }
            i+=12;

        }
        return  result;
    }

    public static String[] GetAllRightAnswers()
    {
        String[] result= new String[EXTERNAL_MUSEUM.RoomsList.size() *3];   //In each room: 3 correct answers
        int i=0;
        for(DownloadableMuseum.RoomsForNewMuseum room : EXTERNAL_MUSEUM.RoomsList)
        {
            if(menu.lang.equals("uk")) {
                result[i] =room.correct1_text;
                result[i+1] =room.correct2_text;
                result[i+2] =room.correct3_text;
            }
            else {
                result[i] =room.correct1_text_gr;
                result[i+1] =room.correct2_text_gr;
                result[i+2] =room.correct3_text_gr;
            }
            i+=3;

        }
        return  result;
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
