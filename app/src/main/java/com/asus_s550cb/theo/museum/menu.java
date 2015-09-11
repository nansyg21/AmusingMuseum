package com.asus_s550cb.theo.museum;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Locale;


public class menu extends Activity {
    public static int mainPid;
    public static String lang="uk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        //hide nav & stat bars
        hideNavBar(this.getWindow());
        mainPid=android.os.Process.myPid();
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
                //finish();
                break;
            case R.id.btnMenuHelp:
                startActivity(new Intent(getApplicationContext(),HelpscreenSliderActivity.class));
                break;
            case R.id.btnMenuAbout:
                startActivity(new Intent(getApplicationContext(),CreditsActivity.class));
                break;
        }
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
                Intent refresh = new Intent(this, menu.class);
                finish();
                startActivity(refresh);

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
        else if (Build.VERSION.SDK_INT < 16)    // If the Android version is lower than Jellybean, hide the status bar.
        {
            w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);

        }

    }
//to commit

}
