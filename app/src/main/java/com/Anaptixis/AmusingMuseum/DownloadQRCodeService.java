package com.Anaptixis.AmusingMuseum;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class DownloadQRCodeService extends IntentService {

    public DownloadQRCodeService() {
        super("DownloadQRCodeService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Download with download manager
        DownloadManager downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        //parse the uri
        Uri Download_Uri;
        if(intent.getStringExtra("DOWNLOAD").equals("qrcodes"))
        {
            Download_Uri = Uri.parse("https://drive.google.com/uc?export=download&id=0BykWjgDJ7yWNb1djdjBMd3NTa3c");
            //Create a download request
            DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
            //Set Notification description , store location and filename
            request.setDescription("Anaptixis File").setTitle("Amusing_Museum_Exhibits_QRCodes.pdf");
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Amusing_Museum_Exhibits_QRCodes.pdf");
            //Keep the notification into the status bar
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            //start the downloading
            downloadManager.enqueue(request);
        }
        else
        {
            Download_Uri = Uri.parse("https://docs.google.com/document/d/1Q-ksv5YH0omtMbbcqmN2K5R8mvTQzveIIq-i4LR2lIU/export?format=pdf");
            //Create a download request
            DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
            //Set Notification description , store location and filename
            request.setDescription("Anaptixis File").setTitle("Amusing_Museum_Solutions.pdf");
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Amusing_Museum_Solutions.pdf");
            //Keep the notification into the status bar
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            //start the downloading
            downloadManager.enqueue(request);

        }


        Intent i=new Intent("BROADCAST").putExtra("STATUS","OK");
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);

    }
}
