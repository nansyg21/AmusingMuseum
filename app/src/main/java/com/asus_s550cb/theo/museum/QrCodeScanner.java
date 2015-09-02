package com.asus_s550cb.theo.museum;

/**
 * Created by theo on 19/8/2015.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class QrCodeScanner extends Activity {

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    int appToStart; // The number of the next activity to start
    public static boolean questionMode=true; // If this is true then Quiz will come up, else a riddle
    String nextApp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the main content layout of the Activity
        setContentView(R.layout.activity_android_qr_code_example);

        //Hide all..
        hideNavBar();

        // Handle the incoming variables
        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            appToStart = extras.getInt("nextApp");
        }

        nextApp="nextApp";
    }

    //product qr code mode
    // when qr code button is hit if it is question mode show questions, else open the riddle according to the room number
    public void scanQR(View v) {
        try {
            Intent itn;

            if(questionMode)
            {
                itn=new Intent(getApplicationContext(),QuizGameActivity.class);
                // pass the number of the next activity to the quiz so it can pass it back to the qr code activity
                // when the quiz is done and the riddle must start
                itn.putExtra(nextApp, appToStart);
                startActivityForResult(itn,1);
            }
            else
            {
                switch (appToStart) {
                    case 1:
                        itn = new Intent(getApplicationContext(), ChurchMap.class);
                        startActivityForResult(itn, 1);
                        break;
                    case 2:
                        itn = new Intent(getApplicationContext(), ClickMe.class);
                        startActivityForResult(itn, 1);
                        break;
                    case 3:
                        itn = new Intent(getApplicationContext(), Hangman.class);
                        startActivityForResult(itn, 1);
                        break;
                    case 4:
                        itn = new Intent(getApplicationContext(), MatchingCoins.class);
                        startActivityForResult(itn, 1);
                        break;
                    case 5:
                        itn = new Intent(getApplicationContext(), PuzzleActivity.class);
                        startActivityForResult(itn, 1);
                        break;
                    case 6:
                        itn = new Intent(getApplicationContext(), RightOrder.class);
                        startActivityForResult(itn, 1);
                        break;
          /*      case 7:
                    itn = new Intent(getApplicationContext(), RightOrder.class);
                    startActivityForResult(itn, 1);
                    break;
                case 8:
                    itn = new Intent(getApplicationContext(), RightOrder.class);
                    startActivityForResult(itn, 1);
                    break;
                case 9:
                    itn = new Intent(getApplicationContext(), RightOrder.class);
                    startActivityForResult(itn, 1);
                    break;
                case 10:
                    itn = new Intent(getApplicationContext(), RightOrder.class);
                    startActivityForResult(itn, 1);
                    break;
                case 11:
                    itn = new Intent(getApplicationContext(), RightOrder.class);
                    startActivityForResult(itn, 1);
                    break;*/
                    default:
                        finish();
                }
            }



            finish();
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
         //   Intent intent = new Intent(ACTION_SCAN);
         //   intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
         //   startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            showDialog(QrCodeScanner.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    //alert dialog for downloadDialog
    private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    act.startActivity(intent);
                } catch (ActivityNotFoundException anfe) {

                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }

    //on ActivityResult method
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }
    //HIDE the status an the navigation bars
    public void hideNavBar() {
        if (Build.VERSION.SDK_INT >= 19) {
            View v = getWindow().getDecorView();
            v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

    }
}


