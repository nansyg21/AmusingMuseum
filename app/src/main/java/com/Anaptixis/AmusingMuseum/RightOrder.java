package com.Anaptixis.AmusingMuseum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;


public class RightOrder extends Activity {



    TextView timer; //Show the remaining time
    TextView[] names; //List of TextViews
    ImageView[] images; //List of ImageViews
    String[] nameStrings; //Correct sequence of names
    ImageView[] imageAnswers; //Correct sequence of images


    CharSequence helpText; //Text for the first selected textView
    TextView helpTextView; //The first selected textView
    int helpId; //Id for the first selected textView
    boolean first; //True if it the first selected textView, false if it is the second

    Drawable img; //Image of the first selected imageView
    ImageView helpImageView; //The first selected imageView
    int imageHelpId;//Id for the fist selected imageView
    boolean imageFirst; //True if it the first selected imageView, false if it is the second

    CountDownTimer countDownTimer; //Timer
    long seconds;
    long minutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_right_order);

        // ------------------ Code in order to hide the navigation bar -------------------- //
        menu.hideNavBar(this.getWindow());

        //------------------ Initializations ---------------------------//
        timer=(TextView) findViewById(R.id.timerTextView);

        //----------------- Timer class -----------------------------//
        countDownTimer=new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                seconds=(millisUntilFinished / 1000)%60;
                minutes=(millisUntilFinished/1000*60)%60;


                if(seconds > 9)
                    timer.setText(+minutes+" : " + seconds);
                else
                    timer.setText(+minutes+" : 0" + seconds);
            }

            public void onFinish() {
                timer.setText("done!");
            }
        }.start();

        first=true;
        imageFirst=true;

        // Define correct sequence of images
        imageAnswers=new ImageView[4];
        imageAnswers[0]=new ImageView(this);
        imageAnswers[0].setImageResource(R.drawable.pottery_small_1);
        imageAnswers[1]=new ImageView(this);
        imageAnswers[1].setImageResource(R.drawable.pottery_small_2);
        imageAnswers[2]=new ImageView(this);
        imageAnswers[2].setImageResource(R.drawable.pottery_small_3);
        imageAnswers[3]=new ImageView(this);
        imageAnswers[3].setImageResource(R.drawable.pottery_small_4);

        // Get the correct sequence of names
        nameStrings=getResources().getStringArray(R.array.right_order_names);

        // Initialize name textviews and imageviews
        names=new TextView[4];
        names[0]=(TextView)findViewById(R.id.textView1);
        names[1]=(TextView)findViewById(R.id.textView2);
        names[2]=(TextView)findViewById(R.id.textView3);
        names[3]=(TextView)findViewById(R.id.textView4);

        images=new ImageView[4];
        images[0]=(ImageView) findViewById(R.id.imageView1);
        images[1]=(ImageView) findViewById(R.id.imageView2);
        images[2]=(ImageView) findViewById(R.id.imageView3);
        images[3]=(ImageView) findViewById(R.id.imageView4);

        shuffleNames();

        setListeners();

        //Start help screen
        Intent itn= new Intent(getApplicationContext(), HelpDialogActivity.class);
        itn.putExtra("appNum", 8);
        startActivity(itn);

    }

    // Set listeners to textViews and imageViews
    // On click change background images and check if swap is nessesary
    private void setListeners()
    {
        for(int i=0;i<images.length;i++) {

            images[i].setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        setBorderImage((ImageView) v);
                        changeImages((ImageView) v);
                    }

                    return true;
                }
            });

            names[i].setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        setBorderTextView((TextView) v);
                        changeNames((TextView) v);
                    }
                        return true;
                }
            });

        }
    }

    // Swap textViews
    // If it is the first of the two touched keep it in mind and do nothing
    // If it is the second swap with the first touched then check if all are in correct sequence
    // If the sequence is right disable them and check if images are in order, if so finish the game
    public void changeNames(TextView txtV)
    {
        if(first)
        {
            helpText=txtV.getText();
            helpId=txtV.getId();
            first=false;
        }
        else
        {
            //swap
            first=true;
            helpTextView=(TextView)findViewById(helpId);
            helpTextView.setText(txtV.getText());
            txtV.setText(helpText);
            helpTextView.setBackgroundColor(getResources().getColor(R.color.neutral_beize));
            txtV.setBackgroundColor(getResources().getColor(R.color.neutral_beize));

            //check if done
            if(checkNames())
            {
                for (int i=0;i<names.length;i++)
                {
                    names[i].setEnabled(false);
                    names[i].setBackgroundColor(getResources().getColor(R.color.gold));
                }

                if(checkImages())
                {
                    CalculateScoreAndExit();
                }


            }

        }

    }

    // Swap imageViews
    // If it is the first of the two touched keep it in mind and do nothing
    // If it is the second swap with the first touched then check if all are in correct sequence
    // If the sequence is right disable them and check if names are in order, if so finish the game
    public void changeImages(ImageView imgV)
    {
        if(imageFirst)
        {
            img=imgV.getDrawable();
            imageHelpId=imgV.getId();
            imageFirst=false;
        }
        else
        {
            //swap
            imageFirst=true;
            helpImageView=(ImageView)findViewById(imageHelpId);
            helpImageView.setImageDrawable(imgV.getDrawable());
            imgV.setImageDrawable(img);
            helpImageView.setBackgroundColor(getResources().getColor(R.color.neutral_beize));
            imgV.setBackgroundColor(getResources().getColor(R.color.neutral_beize));

            //check if done
            if(checkImages())
            {
                for (int i=0;i<images.length;i++)
                {
                    images[i].setEnabled(false);
                    images[i].setBackgroundColor(getResources().getColor(R.color.gold));
                }

                if (checkNames())
                {
                    CalculateScoreAndExit();
                }


            }

        }

    }

    private void CalculateScoreAndExit()
    {
        SoundHandler.PlaySound(SoundHandler.correct_sound_id);
        Score.currentRiddleScore=(int) (10 + (minutes*60 + seconds)) ;
        Intent itn= new Intent(getApplicationContext(), Score.class);
        itn.putExtra("nextStage", 8);
        startActivity(itn);


        QrCodeScanner.questionMode=true;
        finish();
    }

    //Check if the names are in correct order
    private boolean checkNames()
    {
        TextView txtV=(TextView) findViewById(R.id.textView1);
        if(!txtV.getText().equals(nameStrings[0]))
            return false;

        txtV=(TextView) findViewById(R.id.textView2);
        if(!txtV.getText().equals(nameStrings[1]))
            return false;

        txtV=(TextView) findViewById(R.id.textView3);
        if(!txtV.getText().equals(nameStrings[2]))
            return false;

        txtV=(TextView) findViewById(R.id.textView4);
        if(!txtV.getText().equals(nameStrings[3]))
            return false;

        return  true;
    }

    public void pauseButtonOnClick(View v)
    {
        Intent itn;
        itn = new Intent(getApplicationContext(), PauseMenuActivity.class);
        startActivity(itn);
    }
    //Check if the images are in correct order
    private boolean checkImages()
    {
        ImageView imgV=(ImageView) findViewById(R.id.imageView1);

        if(!imgV.getDrawable().getConstantState().equals(imageAnswers[0].getDrawable().getConstantState()))
            return false;

        imgV=(ImageView) findViewById(R.id.imageView2);
        if(!imgV.getDrawable().getConstantState().equals(imageAnswers[1].getDrawable().getConstantState()))
            return false;

        imgV=(ImageView) findViewById(R.id.imageView3);
        if(!imgV.getDrawable().getConstantState().equals(imageAnswers[2].getDrawable().getConstantState()))
            return false;

        imgV=(ImageView) findViewById(R.id.imageView4);
        if(!imgV.getDrawable().getConstantState().equals(imageAnswers[3].getDrawable().getConstantState()))
            return false;

        return true;
    }

    // Set border color to the image
    public void setBorderImage(ImageView imgV)
    {
        ImageView img=(ImageView)imgV;
        img.setPadding(10,10,10,10);
        img.setBackgroundColor(getResources().getColor(R.color.royal_crimson));
    }

    // Set border color to the textviews
    public void setBorderTextView(TextView txtV)
    {
        TextView txt=(TextView)txtV;
        txt.setPadding(10,10,10,10);
        txt.setBackgroundColor(getResources().getColor(R.color.gold));
    }

    // Reset the flags to hide the navigation bar
    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        menu.hideNavBar(this.getWindow());
    }

    //Suffle names and images
    private void shuffleNames()
    {
        ArrayList<Integer> ints=new ArrayList<Integer>();
        ArrayList<Integer> icons=new ArrayList<Integer>();

        for (int i=0;i<4;i++)
        {
            ints.add(i);
            icons.add(i);
        }
        Collections.shuffle(ints);
        Collections.shuffle(icons);

        for(int i=0;i<4;i++)
        {
            setNames(ints.get(i), i);
            setImages(icons.get(i),i);
        }
    }

    // Set suffled names to the textviews
    private void setNames(int num,int ind)
    {
        names[ind].setText(nameStrings[num]);
    }

    //Set shuffled images to the imageviews
    private void setImages(int num,int ind)
    {
        switch (num)
        {
            case 0: images[ind].setImageResource(R.drawable.pottery_small_1);
                break;
            case 1: images[ind].setImageResource(R.drawable.pottery_small_2);
                break;
            case 2: images[ind].setImageResource(R.drawable.pottery_small_3);
                break;
            default: images[ind].setImageResource(R.drawable.pottery_small_4);
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
                        Intent itn= new Intent(getApplicationContext(), menu.class); //go to menu screen with proper flag set
                        itn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        itn.putExtra("leaving", true);
                        startActivity(itn);
                    }
                }).create().show();
    }
}
