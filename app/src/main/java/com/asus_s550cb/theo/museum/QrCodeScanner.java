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
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class QrCodeScanner extends Activity {

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    public static int hintCounter;//counter for finding the proper hint in list
    int appToStart; // The number of the next activity to start
    public static boolean questionMode=true; // If this is true then Quiz will come up, else a riddle

    String nextApp;

    public Intent itn;

    String[] advices;
    String[] hints;
    String[] monumentCodes;

    TextView textViewHint;//In this textview the hints will be displayed!

    EditText numCodeTxt;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the main content layout of the Activity
        setContentView(R.layout.activity_android_qr_code_example);

        //Find the textView and the Hints List
        textViewHint = (TextView) findViewById(R.id.textViewHints);
        hints= getResources().getStringArray(R.array.hints);
        monumentCodes=getResources().getStringArray(R.array.monument_codes);
        textViewHint.setText(hints[hintCounter]);

        //Hide all..
        menu.hideNavBar(this.getWindow());

        advices=getResources().getStringArray(R.array.advices);

        // Handle the incoming variables
        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            appToStart = extras.getInt("nextApp");
        }

        nextApp="nextApp";

        //If user typed a pass...
      /*  if(numCodeCheck)
            validateNumCode();*/

    }

    //product qr code mode
    // when qr code button is hit if it is question mode show questions, else open the riddle according to the room number
    public void scanQR(View v) {
        try {
            //Increase the counter for the next Hint
            hintCounter++;

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
             /*   case 7:
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
                //String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                //Check if the qrcode is correct...
                if (contents.equals(monumentCodes[hintCounter]+"\n") || contents.equals(monumentCodes[hintCounter]) ) {
                    scanQR(null);
                }else {
                    Toast toast = Toast.makeText(this,getResources().getString(R.string.wrong_code), Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }
    }

    public void buttonOnclick(View v) {
        switch (v.getId()) {
            case R.id.button_qr_code:
                try {
                    // start the scanning activity from the com.google.zxing.client.android.SCAN intent
                    Intent intent = new Intent(ACTION_SCAN);
                    intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                    startActivityForResult(intent, 0);
                } catch (ActivityNotFoundException anfe) {
                    //on catch, show the download dialog
                    showDialog(QrCodeScanner.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
                }
                break;
            case R.id.button_num_code:
              /*  startActivity(new Intent(getApplicationContext(),CodeActivity.class));
                itn = new Intent(getApplicationContext(),CodeActivity.class);
                itn.putExtra("nextApp",appToStart);
                startActivityForResult(itn,0);*/
              //  finish();
                setContentView(R.layout.activity_code);
                menu.hideNavBar(this.getWindow());
                break;
            case R.id.button_num_code_Ok:
                   numCodeTxt= (EditText) findViewById(R.id.numCode);
                   validateNumCode();
                break;
            case R.id.button_num_code_Back:
                setContentView(R.layout.activity_android_qr_code_example);
                textViewHint = (TextView) findViewById(R.id.textViewHints);
                textViewHint.setText(hints[hintCounter]);
                menu.hideNavBar(this.getWindow());
                break;
        }
    }
    //check the code that the user typed
    public void validateNumCode(){
           // numCodeCheck=false;
            if(monumentCodes[hintCounter].equals(numCodeTxt.getText().toString())){
                scanQR(null);
            } else  {
                setContentView(R.layout.activity_android_qr_code_example);
                textViewHint = (TextView) findViewById(R.id.textViewHints);
                textViewHint.setText(hints[hintCounter]);
                Toast toast = Toast.makeText(this,getResources().getString(R.string.wrong_code), Toast.LENGTH_LONG);
                toast.show();
                menu.hideNavBar(this.getWindow());
            }
        }

    //Remember to hide everything when Activity Resumes...
    @Override
    protected void onResume() {
        super.onResume();
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
                        System.exit(0);
                    }
                }).create().show();
    }
}


