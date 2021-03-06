package com.Anaptixis.AmusingMuseum;


import android.annotation.SuppressLint;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.StringTokenizer;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by nasia on 27/04/2016
 */
public class FinalScreen extends FragmentActivity {

    ImageView shareTxtView;
    ImageView shareBtView;
    ImageView downloadTxtView;
    ImageView downloadBtView;
    ImageView exitView;

    ImageView totalScoreView;
    ImageView downloadSolution;
    ImageView shareView;
    ImageView exitViewTmp;
    ImageView congratsView;

    // share button
    private ShareButton shareButton;
    //image
    private Bitmap image;
    //counter
    private int counter = 0;

    CallbackManager callbackManager;
    ShareDialog shareDialog;    //a popup window with facebook share features

    //Fields to load saved data
    SharedPreferences sharedPreferences;
    static SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        setContentView(R.layout.activity_final_screen);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);//hide status bar
///////////////////////
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        Resources res = getApplicationContext().getResources();
        int id = R.drawable.phototoupload;
        Bitmap bitmap = BitmapFactory.decodeResource(res, id);
        // Uri photoPath = Uri.parse("android.resource://com.thesstrip.anaptixis.facebooktest/" + R.drawable.phototoupload);
        // Bitmap bitmap = BitmapFactory.decodeResource();

        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(bitmap)
                .build();

        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();

      /* ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse("https://developers.facebook.com"))
                .build();*/

        ShareButton shareButton = (ShareButton)findViewById(R.id.share_btn);
        shareButton.setShareContent(content);

    /*    final ShareButton shareButton = (ShareButton)findViewById(R.id.share_btn);
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        shareButton.setOnClickListener(new View.OnClickListener() { //on click, open up the share window
            @Override
            public void onClick(View v) {
                ShareLinkContent linkContent = new ShareLinkContent.Builder()
                        .setContentTitle("Amusing Museum")
                        .setContentUrl(Uri.parse("http://www.mbp.gr/"))
                        .setContentDescription("I just finished the game with score: "+Score.TotalScore)
                        .build();

                //SHARING MODES:
                // FFED:    1)Works through Google Chrome, 2)old design 3)need to log in
                // NATIVE:  1)Posts using the facebook app, no need for extra login
                // WEB: Similar to FEED but with different UI (i think..)
                //THE BEST ONE: AUTOMATIC:(Closes Immediately..)  1)uses only facebook app 2) no need to re-login 3)shares like any other share on facebook
                shareDialog.show(linkContent, ShareDialog.Mode.WEB );

            }
        });*/

        // ------------------ Code in order to hide the navigation bar -------------------- //

        totalScoreView=(ImageView)findViewById(R.id.totalScore);
        downloadSolution=(ImageView)findViewById(R.id.downloadTextImg);
        shareView=(ImageView)findViewById(R.id.shareTextImg);
        exitViewTmp=(ImageView)findViewById(R.id.exitTextImg);
        congratsView=(ImageView)findViewById(R.id.congratsTxt);


        if(menu.lang.equals("uk")) {
            totalScoreView.setImageResource(R.drawable.total_score_en);
            downloadSolution.setImageResource(R.drawable.solutions_en);
            shareView.setImageResource(R.drawable.share_en);
            exitViewTmp.setImageResource(R.drawable.exit_en);
            congratsView.setImageResource(R.drawable.congrats_msg_en);
        }
        else
        {
            totalScoreView.setImageResource(R.drawable.total_score_greek);
            downloadSolution.setImageResource(R.drawable.download_solution_greek);
            shareView.setImageResource(R.drawable.share_greek);
            exitViewTmp.setImageResource(R.drawable.exit_greek);
            congratsView.setImageResource(R.drawable.congrats_msg_greek);
        }

        menu.hideNavBar(this.getWindow());

        TextView scoreTxtView = (TextView) findViewById(R.id.scoreInfoTxt);
        scoreTxtView.setText(Score.TotalScore + "");

        //The game is finished there is no save.
        //This is the only place that the field is actually is set to false
        sharedPreferences=getSharedPreferences(menu.STORAGE_FILE, Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        editor.putBoolean("savedGameAvailiable",false);
        editor.commit();
    }

    public void closeApp(View v)
    {
        finish();
        Intent itn= new Intent(getApplicationContext(), menu.class); //go to menu screen with proper flag set
        itn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        itn.putExtra("leaving", true);
        startActivity(itn);


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

    public void share(View v)
    {
     /*   Log.w("Warn","SHARE PRESSED!!!!");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile("android.resource://" + "com.Anaptixis.AmusingMuseum" + "/drawable/" + "logo", options);

        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(bitmap)
                .build();
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();

        ShareButton shareButton = (ShareButton)findViewById(R.id.share_btn);
        shareButton.setShareContent(content);*/


    }

    //Download pdf solutions file
    public void downloadFile (View v) {
        Toast.makeText(getApplicationContext(), "Downloading started...", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, DownloadQRCodeService.class);
        i.putExtra("DOWNLOAD","solutions");
        this.startService(i);

     /*   Toast.makeText(getApplicationContext(), "Downloading started...", Toast.LENGTH_SHORT).show();

        //Download with download manager
        DownloadManager downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        //parse the uri
        Uri Download_Uri = Uri.parse("https://docs.google.com/document/d/1Q-ksv5YH0omtMbbcqmN2K5R8mvTQzveIIq-i4LR2lIU/export?format=pdf");
        //Create a download request
        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
        //Set Notification description , store location and filename
        request.setDescription("Anaptixis File").setTitle("Amusing_Museum_Solutions.pdf");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Amusing_Museum_Solutions.pdf");
        //Keep the notification into the status bar
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //start the downloading
        downloadManager.enqueue(request);*/


        //If I want to do anything after downloaded the file
        //just remove the comments here
       /* BroadcastReceiver onComplete=new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {

                //Open the download folder with this code

                Intent i = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
                startActivity(i);


            }
        };
        //register the receiver...
       registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));*/

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
