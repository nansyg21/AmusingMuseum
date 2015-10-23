package com.asus_s550cb.theo.museum;

/**
 * Created by theo on 19/8/2015.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
    String[] monumentInformations;

    TextView textViewHint;//In this textview the hints will be displayed!

    ImageView imgvExhibit ;//Exhibit image
    TextView txtViewExhibit;//Exhibit information text


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

        //These are important for the Exhibit information view

        monumentInformations=getResources().getStringArray(R.array.exhibits_information);


        Log.d("test",monumentInformations[0]);
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
                    buildExhibitInformationView();
                    scanQR(null);
                }else {
                    //GO to information screen...
                    buildExhibitInformationView();

                    ///show a toast... for wrong...
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
                buildExhibitInformationView();
                scanQR(null);
            }
            //IF code is incorrect , display the information about the current exhibit and a toast with proper message
            else
            {
                //Info window
                buildExhibitInformationView();

                ///show a toast... for wrong...
                Toast toast = Toast.makeText(this,getResources().getString(R.string.wrong_code), Toast.LENGTH_LONG);
                toast.show();

                menu.hideNavBar(this.getWindow());
            }
        }
    //BUILD and show the information screen
    private void buildExhibitInformationView() {

        //set Content view
        setContentView(R.layout.exhibit_information);

        //Get References to components
        imgvExhibit = (ImageView) findViewById(R.id.imageview_exhibit_info);
        txtViewExhibit= (TextView)  findViewById(R.id.textViewExhibitInfo);
        ImageView bt=(ImageView) findViewById(R.id.backButtonExhibitInfo);

        //Set text and image
        txtViewExhibit.setText(monumentInformations[hintCounter]);
        switch (hintCounter)
        {
            case 0:
                imgvExhibit.setImageResource(R.drawable.monument1a);
                break;
            case 1:
                imgvExhibit.setImageResource(R.drawable.monument1b);
                break;
            case 2:
                imgvExhibit.setImageResource(R.drawable.monument2a);
                break;
            case 3:
                imgvExhibit.setImageResource(R.drawable.monument2b);
                break;
            case 4:
                imgvExhibit.setImageResource(R.drawable.monument3a);
                break;
            case 5:
                imgvExhibit.setImageResource(R.drawable.monument3b);
                break;
            case 6:
                imgvExhibit.setImageResource(R.drawable.monument4a);
                break;
            case 7:
                imgvExhibit.setImageResource(R.drawable.monument4b);
                break;
            case 8:
                imgvExhibit.setImageResource(R.drawable.monument5a);
                break;
            case 9:
                imgvExhibit.setImageResource(R.drawable.monument5a);
                break;
            case 10:
                imgvExhibit.setImageResource(R.drawable.monument6a);
                break;
            case 11:
                imgvExhibit.setImageResource(R.drawable.monument6b);
                break;
        }
        //ImageView listener!!!
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_android_qr_code_example);
                textViewHint = (TextView) findViewById(R.id.textViewHints);
                textViewHint.setText(hints[hintCounter]);
            }
        });


    }

    //Remember to hide everything when Activity Resumes...
    @Override
    protected void onResume() {
        super.onResume();
        menu.hideNavBar(this.getWindow());
    }

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


