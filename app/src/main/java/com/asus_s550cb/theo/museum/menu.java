package com.asus_s550cb.theo.museum;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

        Button button=(Button) v;
        switch (v.getId()) {

            case R.id.btnMenuExit:
                finish();
                System.exit(0);
                break;
            case R.id.btnMenuNewGame:
                startActivity(new Intent(getApplicationContext(), Game.class));
                break;
            case R.id.btnMenuHelp:
                startActivity(new Intent(getApplicationContext(),HelpscreenSliderActivity.class));
                //startActivity(new Intent(getApplicationContext(),UploadScoreActivity.class));
               // Intent itn = new Intent(getApplicationContext(), ChurchMap.class);
               // Intent itn = new Intent(getApplicationContext(), ClickMe.class);
               // Intent itn = new Intent(getApplicationContext(), Hangman.class);
                // Intent itn = new Intent(getApplicationContext(), MatchingCoins.class);
               // Intent itn = new Intent(getApplicationContext(), PuzzleActivity.class);
                // Intent itn = new Intent(getApplicationContext(), RightOrder.class);
               // Intent itn = new Intent(getApplicationContext(), MemoryGame.class);
               //  Intent itn = new Intent(getApplicationContext(), ExhibitsFall.class);
                //startActivityForResult(itn, 1);
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

    public void GoToStats(View v)
    {
        startActivity(new Intent(getApplicationContext(),Statistics.class));
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
}
