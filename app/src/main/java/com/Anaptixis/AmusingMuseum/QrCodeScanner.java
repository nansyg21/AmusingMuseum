package com.Anaptixis.AmusingMuseum;

/**
 * Created by theo on 19/8/2015.
 *
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class QrCodeScanner extends Activity {

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    public static int hintCounter=11;//counter for finding the proper hint in list
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

    TextView textViewIncorrectObjectView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the main content layout of the Activity
        setContentView(R.layout.activity_android_qr_code_example);

        if(savedInstanceState!=null)
        {
            Log.e("THEO","savedInstanceState not Null   ");
            hintCounter=savedInstanceState.getInt("hintCounter");
            appToStart=savedInstanceState.getInt("appToStart");
            questionMode=savedInstanceState.getBoolean("questionMode");
        }

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

        //The game is finished
        if(appToStart ==  11 && !questionMode)
        {
            itn = new Intent(getApplicationContext(), UploadScoreActivity.class);
            startActivityForResult(itn, 1);
        }

        //If user typed a pass...
      /*  if(numCodeCheck)
            validateNumCode();*/

        //These are important for the Exhibit information view

        monumentInformations=getResources().getStringArray(R.array.exhibits_information);
        if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
        {
            monumentInformations=MainActivity.GetAllHintsAsList();
        }

        Log.d("test",monumentInformations[0]);
    }

    //product qr code mode
    // when qr code button is hit if it is question mode show questions, else open the riddle according to the room number
    public void scanQR(View v) {
        try {
            //Increase the counter for the next Hint
            hintCounter++;

            if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM && hintCounter==(MainActivity.EXTERNAL_MUSEUM.number_of_rooms*2))
            {       //hints= rooms visited*2    Rooms=dynamic value     No rooms left, go to Upload Score Activity
                itn=new Intent(getApplicationContext(),UploadScoreActivity.class);
                startActivityForResult(itn,1);
            }
            //No need to check hangman will send message to stop after next quiz game
        /*    if(hintCounter>20)     //hints= rooms visited*2    Rooms=6     No rooms left, go to Upload Score Activity
            {
                itn=new Intent(getApplicationContext(),UploadScoreActivity.class);
                startActivityForResult(itn,1);
            }*/
            if(questionMode)
            {
                itn=new Intent(getApplicationContext(),QuizGameActivity.class);
                // pass the number of the next activity to the quiz so it can pass it back to the qr code activity
                // when the quiz is done and the riddle must start
                itn.putExtra(nextApp, appToStart);
                //Stage Increased
             //
             //   StartGame.startingStage++;

                startActivityForResult(itn,1);


            }
            else
            {
                if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)//if working on external museum the mini game is chosen by the tour creator
                {                                           //and loaded dynamically
                    OpenProperMiniGame(appToStart);
                }
                else {
                    switch (appToStart) {
                        case 1:
                            //itn = new Intent(getApplicationContext(), SequentialCoins.class);
                            itn = new Intent(getApplicationContext(), PuzzleActivity.class);
                            startActivityForResult(itn, 1);
                            break;
                        case 2:
                            itn = new Intent(getApplicationContext(), ExhibitsFall.class);
                            startActivityForResult(itn, 1);
                            break;
                        case 3:
                            itn = new Intent(getApplicationContext(), ClickMe.class);
                            startActivityForResult(itn, 1);
                            break;
                        case 4:
                            itn = new Intent(getApplicationContext(), MatchingStamps.class);
                            startActivityForResult(itn, 1);
                            break;
                        case 5:
                            itn = new Intent(getApplicationContext(), SequentialCoins.class);
                            startActivityForResult(itn, 1);
                            break;
                        case 6:
                            itn = new Intent(getApplicationContext(), ChurchMap.class);
                            startActivityForResult(itn, 1);
                            break;
                        case 7:
                            itn = new Intent(getApplicationContext(), RightOrder.class);
                            startActivityForResult(itn, 1);
                            break;
                        case 8:
                            itn = new Intent(getApplicationContext(), MemoryGame.class);
                            startActivityForResult(itn, 1);
                            break;
                        case 9:
                            itn = new Intent(getApplicationContext(), connect_wires.class);
                            startActivityForResult(itn, 1);
                            break;
                        case 10:
                            itn = new Intent(getApplicationContext(), Hangman.class);
                            startActivityForResult(itn, 1);
                            break;

                        default:
                            finish();
                    }
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

    private void OpenProperMiniGame(int mini_game_index) //Depending on the value received from server load the correct mini game
    {
        Class classToLoad ;
        switch (MainActivity.EXTERNAL_MUSEUM.RoomsList.get(mini_game_index-1).MiniGame)
        {
            case "churchmap":
                classToLoad=ChurchMap.class;
                break;
            case "clickme":
                classToLoad=ClickMe.class;
                break;
            case "hangman":
                classToLoad=Hangman.class;
                break;
            case "matchingcoins":
                classToLoad=MatchingStamps.class;
                break;
            case "puzzle":
                classToLoad=PuzzleActivity.class;
                break;
            case "rightorder":
                classToLoad=RightOrder.class;
                break;
            case "memorygame":
                classToLoad=MemoryGame.class;
                break;
            case "exhibitsfall":
                classToLoad=ExhibitsFall.class;
                break;
            default:
                classToLoad=ChurchMap.class;    //if something goes wrong we need to load something: load ChurchMap
        }
        itn = new Intent(getApplicationContext(), classToLoad);
        startActivityForResult(itn, 1);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.e("THEO","savedInstanceState Called");
        outState.putInt("hintCounter",hintCounter);
        outState.putInt("appToStart",appToStart);
        outState.putBoolean("questionMode",questionMode);
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
                    anfe.printStackTrace();
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
                    Log.d("code",monumentCodes[hintCounter]);
                    //The second parameter is not going to be used, dummy..
                    buildExhibitInformationView(true,true);

                }else {
                    //GO to information screen...
                    buildExhibitInformationView(false,false);
                    Log.d("code", monumentCodes[hintCounter]);
                    ///show a toast... for wrong...
                  //  Toast toast = Toast.makeText(this,getResources().getString(R.string.wrong_code), Toast.LENGTH_LONG);
                  //  toast.show();


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
                //Get editText ref
                numCodeTxt= (EditText) findViewById(R.id.numCode);
                //Find the textViewIncorrectpassword
                //textViewIncorrectObjectView = (TextView) findViewById(R.id.textViewIncorrectObjectCode);
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
            //The second parameter is not going to be used, dummy..
            buildExhibitInformationView(true,true);
            Log.d("code", monumentCodes[hintCounter]);
        }
        //IF code is incorrect , display the information about the current exhibit and a toast with proper message
        else
        {
            //Info window
            buildExhibitInformationView(false,true);
            Log.d("code", monumentCodes[hintCounter]);

            ///show a toast... for wrong...
            //Toast toast = Toast.makeText(this,getResources().getString(R.string.wrong_code), Toast.LENGTH_LONG);
            //toast.show();
        }
    }
    //BUILD and show the information screen
    private void buildExhibitInformationView(final boolean theAnswerWasRight, final boolean backToPasswordInsertion) {

        //set Content view
        setContentView(R.layout.exhibit_information);

        //Get References to components

        imgvExhibit = (ImageView) findViewById(R.id.imageview_exhibit_info);
        txtViewExhibit= (TextView)  findViewById(R.id.textViewExhibitInfo);
        ImageView bt=(ImageView) findViewById(R.id.backButtonExhibitInfo);

        ImageView titleImageView=(ImageView)findViewById(R.id.titleImageViewExInfo);

        //If the answer was incorrect display the message
        if(!theAnswerWasRight) {
            textViewIncorrectObjectView = (TextView) findViewById(R.id.textViewIncorrectObjectCode);
            if (textViewIncorrectObjectView != null)
                textViewIncorrectObjectView.setVisibility(View.VISIBLE);
        }

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int screenHeight = displaymetrics.heightPixels;
        int screenWidth = displaymetrics.widthPixels;

        if(menu.lang.equals("uk")) {
            titleImageView.setImageResource(R.drawable.info_title_icon);
        }
        else
        {
            titleImageView.setImageResource(R.drawable.info_title_icon_el);
        }
        titleImageView.getLayoutParams().height=screenHeight/4;

        //Set text and image
        txtViewExhibit.setText(monumentInformations[hintCounter]);
        switch (hintCounter)
        {
            case 0:
                imgvExhibit.setImageResource(R.drawable.monument1a);
                if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)         //working on external museum case
                {
                    imgvExhibit.setImageBitmap(Bitmap.createScaledBitmap(MainActivity.EXTERNAL_MUSEUM.FromByteArrayToBitmap
                            (MainActivity.EXTERNAL_MUSEUM.RoomsList.get(0).hintImgBmp1.imageByteArray),500,600,true));
                }
                break;
            case 1:
                imgvExhibit.setImageResource(R.drawable.monument1b);
                if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
                {
                    imgvExhibit.setImageBitmap(Bitmap.createScaledBitmap(MainActivity.EXTERNAL_MUSEUM.FromByteArrayToBitmap
                            (MainActivity.EXTERNAL_MUSEUM.RoomsList.get(0).hintImgBmp2.imageByteArray),500,600,true));
                }
                break;
            case 2:
                imgvExhibit.setImageResource(R.drawable.monument2a);
                if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
                {
                    imgvExhibit.setImageBitmap(Bitmap.createScaledBitmap(MainActivity.EXTERNAL_MUSEUM.FromByteArrayToBitmap
                            (MainActivity.EXTERNAL_MUSEUM.RoomsList.get(1).hintImgBmp1.imageByteArray),500,600,true));
                }
                break;
            case 3:
                imgvExhibit.setImageResource(R.drawable.monument2b);
                if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
                {
                    imgvExhibit.setImageBitmap(Bitmap.createScaledBitmap(MainActivity.EXTERNAL_MUSEUM.FromByteArrayToBitmap
                            (MainActivity.EXTERNAL_MUSEUM.RoomsList.get(1).hintImgBmp2.imageByteArray),500,600,true));
                }
                break;
            case 4:
                imgvExhibit.setImageResource(R.drawable.monument3a);
                if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
                {
                    imgvExhibit.setImageBitmap(Bitmap.createScaledBitmap(MainActivity.EXTERNAL_MUSEUM.FromByteArrayToBitmap
                            (MainActivity.EXTERNAL_MUSEUM.RoomsList.get(2).hintImgBmp1.imageByteArray),500,600,true));
                }
                break;
            case 5:
                imgvExhibit.setImageResource(R.drawable.monument3b);
                if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
                {
                    imgvExhibit.setImageBitmap(Bitmap.createScaledBitmap(MainActivity.EXTERNAL_MUSEUM.FromByteArrayToBitmap
                            (MainActivity.EXTERNAL_MUSEUM.RoomsList.get(2).hintImgBmp2.imageByteArray),500,600,true));
                }
                break;
            case 6:
                imgvExhibit.setImageResource(R.drawable.monument4a);
                if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
                {
                    imgvExhibit.setImageBitmap(Bitmap.createScaledBitmap(MainActivity.EXTERNAL_MUSEUM.FromByteArrayToBitmap
                            (MainActivity.EXTERNAL_MUSEUM.RoomsList.get(3).hintImgBmp1.imageByteArray),500,600,true));
                }
                break;
            case 7:
                imgvExhibit.setImageResource(R.drawable.monument4b);
                if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
                {
                    imgvExhibit.setImageBitmap(Bitmap.createScaledBitmap(MainActivity.EXTERNAL_MUSEUM.FromByteArrayToBitmap
                            (MainActivity.EXTERNAL_MUSEUM.RoomsList.get(3).hintImgBmp2.imageByteArray),500,600,true));
                }
                break;
            case 8:
                imgvExhibit.setImageResource(R.drawable.monument5a);
                if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
                {
                    imgvExhibit.setImageBitmap(Bitmap.createScaledBitmap(MainActivity.EXTERNAL_MUSEUM.FromByteArrayToBitmap
                            (MainActivity.EXTERNAL_MUSEUM.RoomsList.get(4).hintImgBmp1.imageByteArray),500,600,true));
                }
                break;
            case 9:
                imgvExhibit.setImageResource(R.drawable.monument5b);
                if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
                {
                    imgvExhibit.setImageBitmap(Bitmap.createScaledBitmap(MainActivity.EXTERNAL_MUSEUM.FromByteArrayToBitmap
                            (MainActivity.EXTERNAL_MUSEUM.RoomsList.get(4).hintImgBmp2.imageByteArray),500,600,true));
                }
                break;
            case 10:
                imgvExhibit.setImageResource(R.drawable.monument6a);
                if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
                {
                    imgvExhibit.setImageBitmap(Bitmap.createScaledBitmap(MainActivity.EXTERNAL_MUSEUM.FromByteArrayToBitmap
                            (MainActivity.EXTERNAL_MUSEUM.RoomsList.get(5).hintImgBmp1.imageByteArray),500,600,true));
                }
                break;
            case 11:
                imgvExhibit.setImageResource(R.drawable.monument6b);
                if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
                {
                    imgvExhibit.setImageBitmap(Bitmap.createScaledBitmap(MainActivity.EXTERNAL_MUSEUM.FromByteArrayToBitmap
                            (MainActivity.EXTERNAL_MUSEUM.RoomsList.get(5).hintImgBmp2.imageByteArray),500,600,true));
                }
                break;
            case 12:
                imgvExhibit.setImageResource(R.drawable.monument7a);
                if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
                {
                    imgvExhibit.setImageBitmap(Bitmap.createScaledBitmap(MainActivity.EXTERNAL_MUSEUM.FromByteArrayToBitmap
                            (MainActivity.EXTERNAL_MUSEUM.RoomsList.get(6).hintImgBmp2.imageByteArray),500,600,true));
                }
                break;
            case 13:
                imgvExhibit.setImageResource(R.drawable.monument7b);
                if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
                {
                    imgvExhibit.setImageBitmap(Bitmap.createScaledBitmap(MainActivity.EXTERNAL_MUSEUM.FromByteArrayToBitmap
                            (MainActivity.EXTERNAL_MUSEUM.RoomsList.get(6).hintImgBmp2.imageByteArray),500,600,true));
                }
                break;
            case 14:
                imgvExhibit.setImageResource(R.drawable.monument8a);
                if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
                {
                    imgvExhibit.setImageBitmap(Bitmap.createScaledBitmap(MainActivity.EXTERNAL_MUSEUM.FromByteArrayToBitmap
                            (MainActivity.EXTERNAL_MUSEUM.RoomsList.get(7).hintImgBmp2.imageByteArray),500,600,true));
                }
                break;
            case 15:
                imgvExhibit.setImageResource(R.drawable.monument8b);
                if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
                {
                    imgvExhibit.setImageBitmap(Bitmap.createScaledBitmap(MainActivity.EXTERNAL_MUSEUM.FromByteArrayToBitmap
                            (MainActivity.EXTERNAL_MUSEUM.RoomsList.get(7).hintImgBmp2.imageByteArray),500,600,true));
                }
                break;
            case 16:
                imgvExhibit.setImageResource(R.drawable.monument9a);
                if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
                {
                    imgvExhibit.setImageBitmap(Bitmap.createScaledBitmap(MainActivity.EXTERNAL_MUSEUM.FromByteArrayToBitmap
                            (MainActivity.EXTERNAL_MUSEUM.RoomsList.get(8).hintImgBmp2.imageByteArray),500,600,true));
                }
                break;
            case 17:
                imgvExhibit.setImageResource(R.drawable.monument9b);
                if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
                {
                    imgvExhibit.setImageBitmap(Bitmap.createScaledBitmap(MainActivity.EXTERNAL_MUSEUM.FromByteArrayToBitmap
                            (MainActivity.EXTERNAL_MUSEUM.RoomsList.get(8).hintImgBmp2.imageByteArray),500,600,true));
                }
                break;
            case 18:
                imgvExhibit.setImageResource(R.drawable.monument10a);
                if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
                {
                    imgvExhibit.setImageBitmap(Bitmap.createScaledBitmap(MainActivity.EXTERNAL_MUSEUM.FromByteArrayToBitmap
                            (MainActivity.EXTERNAL_MUSEUM.RoomsList.get(9).hintImgBmp2.imageByteArray),500,600,true));
                }
                break;
            case 19:
                imgvExhibit.setImageResource(R.drawable.monument10b);
                if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
                {
                    imgvExhibit.setImageBitmap(Bitmap.createScaledBitmap(MainActivity.EXTERNAL_MUSEUM.FromByteArrayToBitmap
                            (MainActivity.EXTERNAL_MUSEUM.RoomsList.get(9).hintImgBmp2.imageByteArray),500,600,true));
                }
                break;
            case 20:
                imgvExhibit.setImageResource(R.drawable.monument11a);
                if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
                {
                    imgvExhibit.setImageBitmap(Bitmap.createScaledBitmap(MainActivity.EXTERNAL_MUSEUM.FromByteArrayToBitmap
                            (MainActivity.EXTERNAL_MUSEUM.RoomsList.get(10).hintImgBmp2.imageByteArray),500,600,true));
                }
                break;

        }
        //ImageView listener!!!
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(theAnswerWasRight){
                    scanQR(null);
                }else {
                    //Find the textViewIncorrectpassword

                    //Check where was the incorrect password came from
                    if(backToPasswordInsertion)
                        setContentView(R.layout.activity_code);
                    else {
                        setContentView(R.layout.activity_android_qr_code_example);
                        textViewHint = (TextView) findViewById(R.id.textViewHints);
                        textViewHint.setText(hints[hintCounter]);
                    }
                }
            }
        });

        menu.hideNavBar(this.getWindow());
    }

    //Remember to hide everything when Activity Resumes...
    @Override
    protected void onResume() {
        super.onResume();
        menu.hideNavBar(this.getWindow());
        //Update the language
        getApplicationContext().getResources().updateConfiguration(menu.setLocale(), null);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        menu.hideNavBar(this.getWindow());
        //Update the language
        getApplicationContext().getResources().updateConfiguration(menu.setLocale(), null);
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


