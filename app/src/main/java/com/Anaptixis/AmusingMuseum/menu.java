package com.Anaptixis.AmusingMuseum;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Locale;



public class menu extends Activity {
    public static int mainPid;
    public static String lang="el";
    public static String STORAGE_FILE="DATA";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        //hide nav & stat bars
        hideNavBar(this.getWindow());
        mainPid=android.os.Process.myPid();



        //hide icon: Upload score from local data if local data don't exist
        SharedPreferences settings = getApplicationContext().getSharedPreferences(UploadScoreActivity.LOCALLY_SAVED_DATA_PREFERENCE_NAME, 0);
        String name = settings.getString(UploadScoreActivity.LOCALLY_SAVED_NAME, null);
        int score = settings.getInt(UploadScoreActivity.LOCALLY_SAVED_SCORE, -1);
        String date = settings.getString(UploadScoreActivity.LOCALLY_SAVED_DATE, null);
        String museum = settings.getString(UploadScoreActivity.LOCALLY_SAVED_MUSEUM, null);

        if(name==null || score==-1 || date==null || museum==null)   //no data found: do nothing
        {
            ImageView localDataImgView = (ImageView) findViewById(R.id.menu_upload_locally_saved_data);
            ((ViewManager)localDataImgView.getParent()).removeView(localDataImgView);
            localDataImgView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void ButtonOnClick(View v){

        switch (v.getId()) {

            case R.id.btnMenuExit:
                finish();
                System.exit(0);
                break;
            case R.id.btnMenuNewGame:


                //Check if saved data
                if(checkSavedDataExistance()) {
                    showDialog(this,"Saved Game found!","Wish to continue last game or start a brand new one?", "New Game","Resume Last").show();
                }
                else {
                    initializeSavedData();
                    startActivity(new Intent(getApplicationContext(), Game.class));
                }


                break;
            case R.id.btnMenuHelp:

                /***Put into comments the commands that use shared prefs in your minigame to get access from here****/

                 startActivity(new Intent(getApplicationContext(),HelpscreenSliderActivity.class));
               // startActivity(new Intent(getApplicationContext(),ChurchMap.class));
                //startActivity(new Intent(getApplicationContext(),QuizGameActivity.class));
              //  startActivity(new Intent(getApplicationContext(),UploadScoreActivity.class));
               //  Intent itn = new Intent(getApplicationContext(), SequentialCoins.class);
               // Intent itn = new Intent(getApplicationContext(), ChurchMap.class);
               // Intent itn = new Intent(getApplicationContext(), ClickMe.class);
               // Intent itn = new Intent(getApplicationContext(), Hangman.class);
               //  Intent itn = new Intent(getApplicationContext(), MatchingStamps.class);
               // Intent itn = new Intent(getApplicationContext(), PuzzleActivity.class);
             //    Intent itn = new Intent(this, RightOrder.class);
               //  startActivity(itn);
                //  Intent itn = new Intent(getApplicationContext(), MemoryGame.class);
               //  Intent itn = new Intent(getApplicationContext(), ExhibitsFall.class);
              //  Intent itn = new Intent(getApplicationContext(), connect_wires.class);
              //  startActivityForResult(itn, 1);
                break;
            case R.id.btnMenuAbout:
                startActivity(new Intent(getApplicationContext(),CreditsActivity.class));
                break;

        }
    }

    public void ConnectToServerView(View v)
    {
        ImageView imgv=(ImageView) v;
        switch(imgv.getId()) {
            case R.id.menu_upload_locally_saved_data:
                startActivity(new Intent(getApplicationContext(), UploadScoreFromLocalDataActivity.class));
                break;
            case R.id.menu_download_other_museums:
                startActivity(new Intent(getApplicationContext(), DownloadOtherMuseumsActivity.class));
                break;
        }
    }
    public void GoToDownloads(View v)
    {
        startActivity(new Intent(getApplicationContext(),DowloadQRCodeActivity.class));
       // startActivity(new Intent(getApplicationContext(),Statistics.class));
     //   ImageView imgv=(ImageView) v;

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
                    }
                }).create().show();
    }


    //Remember to hide everything when Activity Resumes...
    @Override
    protected void onResume() {
        super.onResume();
        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            boolean leaving = extras.getBoolean("leaving");
            if(leaving)
                android.os.Process.killProcess(android.os.Process.myPid()); //will kill all the activities started in this process

        }
        hideNavBar(this.getWindow());

        //Update the language
        getApplicationContext().getResources().updateConfiguration( setLocale(), null);
    }

    public void changeLanguage(View v)
    {
        ImageView imgv=(ImageView) v;

        switch (imgv.getId()) {
            case R.id.ukflag:
                Locale locale = new Locale("en_US");
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                lang="uk";
                getApplicationContext().getResources().updateConfiguration(config, null);
                /**/

                Intent refresh = new Intent(this, menu.class);
                finish();
                startActivity(refresh);
                //Store language selection
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString("LANGUAGE","uk");

                break;
            case R.id.elflag:
                locale = new Locale("el");
                Locale.setDefault(locale);
                config = new Configuration();
                config.locale = locale;
                lang="el";
                getApplicationContext().getResources().updateConfiguration(config, null);
                refresh = new Intent(this, menu.class);
                finish();
                startActivity(refresh);
                //Store language selection
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString("LANGUAGE", "el");

                break;
        }
    }

    //HIDE the status an the navigation bars
    public static void hideNavBar(Window w) {

        if (Build.VERSION.SDK_INT >= 19)
        {
            View v = w.getDecorView();
            v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        else if(Build.VERSION.SDK_INT >16)  //hide the status bar only between JELLY_BEAN and KITKAT and na bar:Low Profile
        {
            int uiOptions =( View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LOW_PROFILE);
            //SYSTEM_UI_FLAG_FULLSCREEN         removes status bar
            //And SYSTEM_UI_FLAG_LOW_PROFILE    sets navigation bar to dots
            w.getDecorView().setSystemUiVisibility(uiOptions);

        }
        else if (Build.VERSION.SDK_INT <= 16)    // If the Android version is lower than Jellybean, hide the status bar.
        {
            w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

        }

    }

    //Restore the selected language
    public static Configuration setLocale()
    {
        Locale locale;
        if(lang=="uk") {
             locale = new Locale("en_US");
        }
        else
        {
             locale = new Locale("el");
        }
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;

        return config;
    }

    //Check if there is saved game Data
    private boolean checkSavedDataExistance(){
        SharedPreferences sharedPreferences=getSharedPreferences(this.STORAGE_FILE, Context.MODE_PRIVATE);

        return sharedPreferences.getBoolean("savedGameAvailiable",false);
    }

    //Reset SharedPreferences for a brand new game()
    private void initializeSavedData(){
        SharedPreferences sharedPreferences=getSharedPreferences(this.STORAGE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();

        //General Data
        editor.putBoolean("savedGameAvailiable",false);
        //roomNumber is availiable via Math.floor(hintCOunter/2) + 1

        //editor.putInt("roomNumber",0);
        editor.putInt("totalScore",0);

        //Check if crash happened during quiz or minigame or somewhere else
        editor.putBoolean("questionMode",true);

        //Saved Data about quiz
        editor.putInt("hintCounter",0);


        //Commit the initialized data
        editor.commit();
    }

    //Load sharedPreferences data, this method has to be called after the checkSavedDataExistance.
    private void loadData(){

        SharedPreferences sharedPreferences=getSharedPreferences(this.STORAGE_FILE, Context.MODE_PRIVATE);

        //A saved game needs only the following values!
        Score.TotalScore=sharedPreferences.getInt("totalScore",0);
        QrCodeScanner.hintCounter=sharedPreferences.getInt("hintCounter",0);
        QrCodeScanner.questionMode=sharedPreferences.getBoolean("questionMode",true);

       // QuizGameActivity.questionCountPublic=sharedPreferences.getInt("questionCountPublic",0);
        //Fix counter, in case which game exited/crashed during quiz
    //    int room = (int)(Math.floor(QrCodeScanner.hintCounter/2))+1 ;

    //    QuizGameActivity.questionCountPublic=(room-1)*3;
      /*  if( room*3 > QuizGameActivity.questionCountPublic*3 )
        {
            if(QuizGameActivity.questionCountPublic%3==0)
                QuizGameActivity.questionCountPublic+=3;
            else if(QuizGameActivity.questionCountPublic%3==1)
                QuizGameActivity.questionCountPublic += 2;
            else if(QuizGameActivity.questionCountPublic%3==2)
                QuizGameActivity.questionCountPublic+=1;
        }*/

     //   QuizGameActivity.questionRightAnsPublic=sharedPreferences.getInt("questionRightAnsPublic",0);

     //   Log.w("THEO", QrCodeScanner.hintCounter+" hintC ");
     //   Log.w("THEO ", Score.TotalScore+" score");


        Intent itns = new Intent(getApplicationContext(), QrCodeScanner.class);
        //This is how I calculate the room number
        itns.putExtra("nextApp",(int) (Math.floor(QrCodeScanner.hintCounter/2 + 1)));
        startActivity(itns);
        finish();

    }

    //Create a dialog for saved game if exists
    private AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {

                try {
                    initializeSavedData();
                    startActivity(new Intent(getApplicationContext(), Game.class));
                } catch (ActivityNotFoundException anfe) {
                    anfe.printStackTrace();
                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    loadData();
                } catch (ActivityNotFoundException anfe) {
                    anfe.printStackTrace();
                }

            }
        });
        return downloadDialog.show();
    }
}
