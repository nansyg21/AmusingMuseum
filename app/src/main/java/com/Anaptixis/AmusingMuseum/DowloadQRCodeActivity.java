package com.Anaptixis.AmusingMuseum;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


public class DowloadQRCodeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_download_qrcode);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);//hide status bar


    }

    public void downloadQR(View v) {
        Toast.makeText(getApplicationContext(), "Downloading started...", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, DownloadQRCodeService.class);
        i.putExtra("DOWNLOAD","qrcodes");
        this.startService(i);
    }


    private class MyBReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getApplicationContext(), "Downloading completed", Toast.LENGTH_SHORT).show();
        }
    }
}