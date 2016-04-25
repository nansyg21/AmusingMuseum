package com.Anaptixis.AmusingMuseum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Random;

/***
 * Hangman Activity
 * There is a list of words from a specific inscription of the museum
 * Every time the app starts a random word of the list is chosen
 * The player has to guess the word before the whole building on the right falls into ruins
 * The player must press the button with the letter he wants to appear in the word
 * If the letter is correct then the letter appears as many times as the word contains it
 * Else an arch is demolished
 * If the player finds the word before the whole building is demolished he wins
 * If he makes more than 5 mistaken guesses and the building is demolished he loses.
 */

public class Hangman extends Activity {

    private String choosenWord; // A word randomly chosen of the available
    private int numberOfLetters; // Number of letters of the chosen word
    private Random random; // Random number generator
    private int randomNum; // Random number in order to choose the word
    private int mistakes; // Number of mistaken guesses
    char[] lettersFound; // An array with the letters the player has already found in the word
    char[] wordArray; // A char array to split the string of the choosen word
    private View[] lettersView; // List with textViews one for each letter of the word dynamically generated
    private ImageView currentArch; // The instance of the current image of arches, change on every mistake
    private int correctLetters; // Number of correct letters found
    private int currentApiVersion; // The api version of android
    private int screenWidth;

    PauseMenuButton pauseBt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        random = new Random(System.currentTimeMillis());

        // populate words array from the available words in string.xml from list words
        String[] words = getResources().getStringArray(R.array.words);
        // pick a random word and convert it in char array
        randomNum = random.nextInt(words.length);
        choosenWord = words[randomNum];
        wordArray = choosenWord.toCharArray();

        // calculate the length of the choosen word
        numberOfLetters = choosenWord.length();

        // initialize found letters with "_" and create an array with equal views
        lettersFound = new char[numberOfLetters];
        for (int j = 0; j < numberOfLetters; j++) {
            lettersFound[j] = '_';
        }

        lettersView=new View[numberOfLetters];

        // initialize mistakes and correct letters to zero
        mistakes = 0;
        correctLetters=0;

        // Dynamicaly create as many views as the letters of the word with the use of inflater
        setContentView(R.layout.activity_hangman);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TableRow letterTableRow = (TableRow) findViewById(R.id.letterTableRow0);
        for (int letters = 0; letters < numberOfLetters; letters++) {
            TextView newTextView = (TextView) inflater.inflate(R.layout.hangman_textview, null);
            newTextView.setText(R.string.underscore);
            newTextView.setTextColor(getResources().getColor(R.color.gold));
            letterTableRow.addView(newTextView);
            lettersView[letters]=newTextView;
            lettersView[letters].setId(letters); //set id in order to use the same view later
                                                 // because it is dynamicly created we can set as id only a number - here the id is
                                                 // the counter of the for loop

        }

        // Has to be after the setContentView, otherway it crashes
        currentArch = (ImageView) findViewById(R.id.archImageView); // get the image view instance
        currentArch.setImageResource(R.drawable.hangman_wrong_0); // initialize the image to the first with no mistakes
        buttonListeners(); // apply button listeners for letter buttons


        // ------------------ Code in order to hide the navigation bar -------------------- //
        menu.hideNavBar(this.getWindow());

        //Start help screen
        Intent itn= new Intent(getApplicationContext(), HelpDialogActivity.class);
        itn.putExtra("appNum",11);
        startActivity(itn);
    }

    public void pauseButtonOnClick(View v)
    {
        Intent itn;
        itn = new Intent(getApplicationContext(), PauseMenuActivity.class);
        startActivity(itn);
    }

    // Redefine the flags for hidden navigation bar every time the window has focus, otherwise with the first touch on the screen
    // the navigation bar appears and won't hide again
    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        menu.hideNavBar(this.getWindow());
    }

    // Check if the letter of pressed button exists in the word
    private void checkLetter(Button bt)
    {
        String letter=bt.getText().toString(); // get the button letter
        // make the button disable so the user can't hit it again and change it's color
        bt.setEnabled(false);
        bt.setClickable(false);
        bt.setBackgroundColor(getResources().getColor(R.color.disable_button));
        char letterChar=letter.charAt(0); // convert string to char
        boolean found=false; // initialize found as false, it becomes true when the letter is found in the word

        // Check if the letter exists in the word, if so then change the text of the specific textView from _ to the letter
        for(int i=0;i<numberOfLetters;i++)
        {
            if(wordArray[i]==letterChar)
            {
                found=true;
                correctLetters++;
                TextView tv = (TextView) findViewById(lettersView[i].getId());
                tv.setText(""+letterChar);
                tv.invalidate();
            }
        }

        // If the letter does not exists, increase mistakes and update the image of the the arch image view, then check if
        // the game is over
        if(!found)
        {
            SoundHandler.PlaySound(SoundHandler.wrong_sound_id2);
            mistakes++;
            if(mistakes==1)
            {
                currentArch.setImageResource(R.drawable.hangman_wrong_1);
            }
            else if (mistakes==2)
            {
                currentArch.setImageResource(R.drawable.hangman_wrong_2);
            }
            else if (mistakes==3)
            {
                currentArch.setImageResource(R.drawable.hangman_wrong_3);
            }
            else if (mistakes==4)
            {
                currentArch.setImageResource(R.drawable.hangman_wrong_4);
            }
            else if(mistakes>4)
            {
                //Lost - Save and Show Score
                for(int i=0;i<numberOfLetters;i++)  //show letters
                {
                    TextView tv = (TextView) findViewById(lettersView[i].getId());
                    tv.setText(""+wordArray[i]);
                    tv.invalidate();
                }
                currentArch.setImageBitmap(null);
                CountDownTimer countDownTimer = new CountDownTimer(3000,1000) { //after 3 seconds quit
                    public void onTick(long t)
                    {
                    }

                    public void onFinish() {
                        SoundHandler.PlaySound(SoundHandler.wrong_sound_id4);
                        Score.setRiddleScore(correctLetters*2) ;
                        Intent itn= new Intent(getApplicationContext(), Score.class);
                        itn.putExtra("nextStage", 11);
                        startActivity(itn);
                        QrCodeScanner.questionMode=true;
                        finish();
                    }}.start();
            }
        }

        checkIfDone();
    }

    // Checks if the player has found all the letters
    private void checkIfDone()
    {
        if(correctLetters==numberOfLetters)
        {
            //Win - Save and Show Score
            SoundHandler.PlaySound(SoundHandler.correct_sound_id);
            Score.setRiddleScore(70 - mistakes * 2) ;
            Intent itn= new Intent(getApplicationContext(), Score.class);
            itn.putExtra("nextStage", 11);
            startActivity(itn);

            QrCodeScanner.questionMode=true;
            finish();
        }
    }

    // Apply button listeners to each letter button, when touched call checkLetter method with arg the button pressed
    private void buttonListeners()
    {
        Button alpha=(Button)findViewById(R.id.alphaButton);
        alpha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLetter((Button) v);
            }
        });

        Button beta=(Button)findViewById(R.id.betaButton);
        beta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLetter((Button) v);
            }
        });

        Button gama=(Button)findViewById(R.id.gamaButton);
        gama.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLetter((Button) v);
            }
        });

        Button delta=(Button)findViewById(R.id.deltaButton);
        delta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLetter((Button) v);
            }
        });

        Button epsilon=(Button) findViewById(R.id.epsilonButton);
        epsilon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLetter((Button) v);
            }
        });

        Button zeta=(Button)findViewById(R.id.zetaButton);
        zeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLetter((Button) v);
            }
        });

        Button eta=(Button)findViewById(R.id.etaButton);
        eta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLetter((Button) v);
            }
        });

        Button theta=(Button)findViewById(R.id.thetaButton);
        theta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLetter((Button) v);
            }
        });

        Button giota=(Button)findViewById(R.id.giotaButton);
        giota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLetter((Button) v);
            }
        });

        Button kapa=(Button)findViewById(R.id.kapaButton);
        kapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLetter((Button) v);
            }
        });

        Button lamda=(Button)findViewById(R.id.lamdaButton);
        lamda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLetter((Button) v);
            }
        });

        Button mi=(Button)findViewById(R.id.miButton);
        mi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLetter((Button) v);
            }
        });

        Button ni=(Button)findViewById(R.id.niButton);
        ni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLetter((Button) v);
            }
        });

        Button ksi=(Button)findViewById(R.id.ksiButton);
        ksi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLetter((Button) v);
            }
        });

        Button omikron=(Button)findViewById(R.id.omikronButton);
        omikron.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLetter((Button) v);
            }
        });

        Button pi=(Button)findViewById(R.id.piButton);
        pi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLetter((Button) v);
            }
        });

        Button ro=(Button)findViewById(R.id.roButton);
        ro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLetter((Button) v);
            }
        });

        Button sigma=(Button)findViewById(R.id.sigmaButton);
        sigma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLetter((Button) v);
            }
        });

        Button taf=(Button)findViewById(R.id.tafButton);
        taf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLetter((Button) v);
            }
        });

        Button ypsilon=(Button)findViewById(R.id.ypsilonButton);
        ypsilon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLetter((Button) v);
            }
        });

        Button fi=(Button)findViewById(R.id.fiButton);
        fi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLetter((Button) v);
            }
        });

        Button xi=(Button)findViewById(R.id.xiButton);
        xi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLetter((Button) v);
            }
        });

        Button psi=(Button)findViewById(R.id.psinButton);
        psi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLetter((Button) v);
            }
        });

        Button omega=(Button)findViewById(R.id.omegaButton);
        omega.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLetter((Button) v);
            }
        });
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
