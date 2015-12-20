package com.asus_s550cb.theo.museum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by panos on 18/12/2015.
 */
public class ExhibitsFall extends Activity {

    int currentApiVersion;
    PauseMenuButton pauseBt;

    int screenWidth, screenHeight;
    private int mActivePointerId = -1;      //-1 instead of INVALID_POINTER_ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);//hide status bar
        setContentView(new ExhibitsFallScreen(this));

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenWidth = displaymetrics.widthPixels;
        screenHeight = displaymetrics.heightPixels;
        pauseBt = new PauseMenuButton(screenWidth, this);

        // ------------------ Code in order to hide the navigation bar -------------------- //
        menu.hideNavBar(this.getWindow());

        //Start help screen
        //Intent itn = new Intent(getApplicationContext(), HelpDialogActivity.class);
        //  itn.putExtra("appNum", 9);  //TODO: WRITE MINI GAME DESCRIPTION
        //  startActivity(itn);

    }

    // Reset the flags to hide the navigation bar
    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        menu.hideNavBar(this.getWindow());
    }

    //This is actually drawing on screen the game
    public class ExhibitsFallScreen extends View implements Runnable {

        int PlayerTouchX = 10, PlayerTouchY = 10;
        MediaPlayer memory_game_pair_sound;
        Paint backgroundPaint, txtPaint;
        Bitmap baseImg;
        Rect baseRect;
        int imgWidth, imgHeight, xSpeed, ySpeed, baseImgWidth, baseImgHeight, baseImgXSpeed, wrongObjectsCaught, correctObjectsCaught;
        int currentRound, totalRounds;
        Random rand = new Random();
        Boolean movinBaseImg,infoState, fallingWrongObjects, fallingCorrectObject;

        // long noSelectTimer, elapsedNoSelectTime, limitNoSelectTime; //timer to freeze game between 2 selections
        //   long showAllImagesTimer, elapsedShowAllImagesTime, limitShowAllImagesTime; //timer for showing images on start
        long delayTimer, elapsedDelayTimer;

        ArrayList<FallingExhibit> allExhibitsList = new ArrayList<FallingExhibit>();    //all FallingExhibit objects
        ArrayList<FallingExhibit> activeExhibits = new ArrayList<FallingExhibit>();     //the enabled ones
        ArrayList<Rect> activeExhibitsRects = new ArrayList<Rect>();     //and their rects

        int chosenObjectIndex;//the object the user must catch
        String chosenObjectName;
        Button invBtn;          //invisible button: triggered on exit to finish mini game

        public ExhibitsFallScreen(Context context) {
            super(context);
            // memory_game_pair_sound = MediaPlayer.create(this.getContext(), something..);

            //for background color
            backgroundPaint = new Paint();
            backgroundPaint.setStyle(Paint.Style.FILL);
            backgroundPaint.setColor(getResources().getColor(R.color.accent_material_dark));

            //for text color
            txtPaint = new Paint();
            txtPaint.setStyle(Paint.Style.FILL);
            txtPaint.setColor(Color.BLACK);
            txtPaint.setTextSize(getResources().getDimension(R.dimen.click_me_text_size));


            DisplayMetrics displaymetrics = new DisplayMetrics();   //for some reason we have to re-read screen resolution
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            screenWidth = displaymetrics.widthPixels;
            screenHeight = displaymetrics.heightPixels;

            imgWidth = 100;
            imgHeight = 100;
            baseImgWidth=screenWidth/5;
            baseImgHeight=screenHeight/5;
            xSpeed = 0;
            ySpeed = 5;

            baseImg =BitmapFactory.decodeResource(getResources(), R.drawable.ed_base);
            baseRect= new Rect(10, screenHeight-baseImgHeight, 10+ baseImgWidth, screenHeight );
            baseImgXSpeed=20;
            movinBaseImg=false;
            infoState=false;
            chosenObjectIndex=-1;
            wrongObjectsCaught=0;
            correctObjectsCaught=0;
            currentRound=0;
            totalRounds =5;

            // imgWidth = screenWidth / (N+1); //split by 5:  4 for each image 1 for free space

            invBtn = new Button(getContext());
            invBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.w("Warn", "Clicked button");
                    LeaveExhibitsFall();
                }
            });

            AddAllExhibits();
            AddAChosenExhibit();
            InformAboutCorrectObject();
            AddRandomlyFallingExhibits();
            delayTimer=System.currentTimeMillis();

            Log.w("Warn","SCORE 1 IS: "+(Math.max(20, (int) Math.ceil(70- (totalRounds-1)*(70/totalRounds)))));
            Log.w("Warn","SCORE 2 IS: "+(Math.max(20, (int) Math.ceil(70- (totalRounds-2)*(70/totalRounds)))));
            Log.w("Warn","SCORE 3 IS: "+(Math.max(20, (int) Math.ceil(70- (totalRounds-3)*(70/totalRounds)))));
            Log.w("Warn","SCORE 4 IS: "+(Math.max(20, (int) Math.ceil(70- (totalRounds-4)*(70/totalRounds)))));
            Log.w("Warn","SCORE 5 IS: "+(Math.max(20, (int) Math.ceil(70- (totalRounds-5)*(70/totalRounds)))));



        }

        public void AddAllExhibits() {
            allExhibitsList.add(new FallingExhibit(getResources().getString(R.string.exhibitsFall1_name), BitmapFactory.decodeResource(getResources(), R.drawable.ed1), getResources().getString(R.string.exhibitsFall1),GetRandDelay()));
            allExhibitsList.add(new FallingExhibit(getResources().getString(R.string.exhibitsFall2_name), BitmapFactory.decodeResource(getResources(), R.drawable.ed2), getResources().getString(R.string.exhibitsFall2),GetRandDelay()));
            allExhibitsList.add(new FallingExhibit(getResources().getString(R.string.exhibitsFall3_name), BitmapFactory.decodeResource(getResources(), R.drawable.ed3), getResources().getString(R.string.exhibitsFall3),GetRandDelay()));
            allExhibitsList.add(new FallingExhibit(getResources().getString(R.string.exhibitsFall4_name), BitmapFactory.decodeResource(getResources(), R.drawable.ed4), getResources().getString(R.string.exhibitsFall4), GetRandDelay()));
            allExhibitsList.add(new FallingExhibit(getResources().getString(R.string.exhibitsFall5_name), BitmapFactory.decodeResource(getResources(), R.drawable.ed5), getResources().getString(R.string.exhibitsFall5), GetRandDelay()));
            allExhibitsList.add(new FallingExhibit(getResources().getString(R.string.exhibitsFall6_name), BitmapFactory.decodeResource(getResources(), R.drawable.ed6), getResources().getString(R.string.exhibitsFall6),GetRandDelay()));
            allExhibitsList.add(new FallingExhibit(getResources().getString(R.string.exhibitsFall7_name), BitmapFactory.decodeResource(getResources(), R.drawable.ed7), getResources().getString(R.string.exhibitsFall7),GetRandDelay()));
            allExhibitsList.add(new FallingExhibit(getResources().getString(R.string.exhibitsFall8_name), BitmapFactory.decodeResource(getResources(), R.drawable.ed8), getResources().getString(R.string.exhibitsFall8),GetRandDelay()));
            allExhibitsList.add(new FallingExhibit(getResources().getString(R.string.exhibitsFall9_name), BitmapFactory.decodeResource(getResources(), R.drawable.ed9), getResources().getString(R.string.exhibitsFall9),GetRandDelay()));
            allExhibitsList.add(new FallingExhibit(getResources().getString(R.string.exhibitsFall10_name), BitmapFactory.decodeResource(getResources(), R.drawable.ed10), getResources().getString(R.string.exhibitsFall10),GetRandDelay()));
            allExhibitsList.add(new FallingExhibit(getResources().getString(R.string.exhibitsFall11_name), BitmapFactory.decodeResource(getResources(), R.drawable.ed11), getResources().getString(R.string.exhibitsFall11),GetRandDelay()));
            allExhibitsList.add(new FallingExhibit(getResources().getString(R.string.exhibitsFall12_name), BitmapFactory.decodeResource(getResources(), R.drawable.ed12), getResources().getString(R.string.exhibitsFall12), GetRandDelay()));
            allExhibitsList.add(new FallingExhibit(getResources().getString(R.string.exhibitsFall13_name), BitmapFactory.decodeResource(getResources(), R.drawable.ed13), getResources().getString(R.string.exhibitsFall13), GetRandDelay()));
            allExhibitsList.add(new FallingExhibit(getResources().getString(R.string.exhibitsFall14_name), BitmapFactory.decodeResource(getResources(), R.drawable.ed14), getResources().getString(R.string.exhibitsFall14),GetRandDelay()));
            allExhibitsList.add(new FallingExhibit(getResources().getString(R.string.exhibitsFall15_name), BitmapFactory.decodeResource(getResources(), R.drawable.ed15), getResources().getString(R.string.exhibitsFall15),GetRandDelay()));
        }

        public void AddAChosenExhibit()
        {
            chosenObjectIndex= rand.nextInt(allExhibitsList.size() - 1);
            chosenObjectName= allExhibitsList.get(chosenObjectIndex).name;
            Log.w("Warn", "Chosen is: " + chosenObjectIndex);
        }

        public void InformAboutCorrectObject()     //Show a pop up toast informing the player about the correct object
        {
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(this.getContext(),getResources().getString(R.string.looking_for)+allExhibitsList.get(chosenObjectIndex).name, duration);
            toast.show();
        }

        public void AddRandomlyFallingExhibits() {
            int n=rand.nextInt(5)+1;                            //n exhibits from 1 to 5
            int k;                                              //k is the current exhibit to get randomly

            for (int i = 0; i <n ; i++) {
                k= rand.nextInt(allExhibitsList.size() - 1);
                while(k== chosenObjectIndex)             //we cant have the chosen object twice
                    k= rand.nextInt(allExhibitsList.size() - 1);

                activeExhibits.add(allExhibitsList.get(k));
                activeExhibitsRects.add(GetRandomRect());
            }
            fallingWrongObjects =true;
            fallingCorrectObject =false;
            Log.w("Warn", "Added " + activeExhibits.size() + " active exhibits ");
        }

        public Rect GetRandomRect() //returns a random rectangle starting from above the screen
        {
            int x = rand.nextInt(screenWidth - imgWidth);
            return new Rect(x, -imgHeight, x + imgWidth, 0);
        }

        public int GetRandDelay()   //returns a random delay in milliseconds
        {
            int x=rand.nextInt(6000)+2000; //objects start falling from 2 to 8 sec
           // Log.w("Warn", "Random time= " + x);
            return x;
        }

        public void ShowInfoForExhibit(String title, FallingExhibit de)
        {
            final Dialog dialog = new Dialog(this.getContext());
            dialog.setContentView(R.layout.exhibit_fall_info);              //set view
            dialog.setTitle(title);                                         //set title

            TextView text = (TextView) dialog.findViewById(R.id.text);      //set info text
            text.setText(de.description);
            ImageView image = (ImageView) dialog.findViewById(R.id.image);  //set image
            image.setImageBitmap(de.image);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    infoState=false;
                    currentRound++;
                    if((currentRound)<= totalRounds) {
                        Log.w("Warn", "Round: "+currentRound);
                        AddAChosenExhibit();                                    //restart game
                        InformAboutCorrectObject();
                        AddRandomlyFallingExhibits();
                        delayTimer = System.currentTimeMillis();
                    }
                    else
                        LeaveExhibitsFall();
                    //Log.w("Warn", "STOPPED INFO ");
                }
            });
            dialog.show();
        }

        public void LeaveExhibitsFall() {

            //Save and Show Score

            Log.w("Warn", "FINAL SCORE: "+(Math.max(20, (int) Math.ceil(70- (totalRounds-correctObjectsCaught)*(70/totalRounds))-5*wrongObjectsCaught)));
            Score.currentRiddleScore =Math.max(20, (int) Math.ceil(70- (totalRounds-correctObjectsCaught)*(70/totalRounds))-5*wrongObjectsCaught);//score starts from 20
            Intent itn = new Intent(getApplicationContext(), Score.class);
            startActivity(itn);

            QrCodeScanner.questionMode = true;
            finish();
        }


        @Override
        public void run() {
            elapsedDelayTimer=System.currentTimeMillis() - delayTimer;
            if(!infoState) {
                FallingExhibit de;
                Rect r;
                for (int i = 0; i < activeExhibitsRects.size(); i++) {
                    de = activeExhibits.get(i);
                    if (!de.isFalling && elapsedDelayTimer > de.delay) {
                        de.isFalling = true;
                    } else if (de.isFalling) {
                        r = activeExhibitsRects.get(i);
                        r.top += ySpeed;     //move vertically
                        r.bottom += ySpeed;

                        r.left += xSpeed;     //move horizontally
                        r.right += xSpeed;

                        //user caught an object
                        if (r.intersect(baseRect) && r.left > baseRect.left && r.left + imgWidth < baseRect.right && r.bottom + 5 > baseRect.top) //if falling object intersects with base object and it is inside: it remove it
                        {
                            // Log.w("Warn", "r.width: " + r.width() + " r.height: " + r.height() + "CheckFall:  r.left: " + r.left + " baseRect.left: " + baseRect.left + " r.right: " + r.right + " baseRect.right: " + baseRect.right);

                            if(activeExhibits.get(i).name.equals(chosenObjectName))    //caught correct object  -  show info
                            {
                                infoState = true;
                                Log.w("Warn", "CAUGHT CORRECT");
                                correctObjectsCaught++;
                                ShowInfoForExhibit(getResources().getString(R.string.correct), allExhibitsList.get(chosenObjectIndex));
                            }
                            else {                                                      //caught wrong object
                                //play wrong sound...
                                wrongObjectsCaught++;
                                Log.w("Warn", "CAUGHT WRONG OBJECT");
                                activeExhibitsRects.remove(i);
                                activeExhibits.remove(i);
                            }


                        } else if (r.top > screenHeight) {  //object fell down
                            if(activeExhibits.get(i).name.equals(chosenObjectName))  //correct object fell down - show info
                            {
                                infoState = true;
                                ShowInfoForExhibit(getResources().getString(R.string.wrong), allExhibitsList.get(chosenObjectIndex));
                            }
                            else {                                                  //wrong object fell down - show info
                                activeExhibitsRects.remove(i);
                                activeExhibits.remove(i);
                                if (activeExhibits.size() == 0)                     //no more wrong objects, drop the correct one
                                {
                                    fallingWrongObjects = false;
                                    fallingCorrectObject = true;
                                    activeExhibits.add(allExhibitsList.get(chosenObjectIndex));
                                    activeExhibitsRects.add(GetRandomRect());
                                    activeExhibits.get(0).delay=GetRandDelay();
                                }
                            }
                        }
                    }
                }
            }

            // onDraw(Canvas) will be called
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            //Draw color on background
            canvas.drawPaint(backgroundPaint);

            pauseBt.getPauseMenuButton().draw(canvas);

            for (int i = 0; i <activeExhibits.size(); i++)//draw back side or normal side in all images
            {
                canvas.drawBitmap(activeExhibits.get(i).image, null, activeExhibitsRects.get(i), null);
            }

            //draw base image
            canvas.drawBitmap(baseImg, null, baseRect, null);

            // Invalidate view at about 60fps
            postDelayed(this, 16);
        }


        @Override
        public boolean onTouchEvent(MotionEvent ev) {

            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:       //New touch started
                {
                    float touchX = ev.getX();
                    float touchY = ev.getY();

                    if (pauseBt.getRect().contains((int) touchX, (int) touchY)) {
                        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                            Intent itn;
                            itn = new Intent(getApplicationContext(), PauseMenuActivity.class);
                            startActivity(itn);
                        }
                    }


                    final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                    final float x = MotionEventCompat.getX(ev, pointerIndex);
                    final float y = MotionEventCompat.getY(ev, pointerIndex);

                    PlayerTouchX = (int) x;
                    PlayerTouchY = (int) y;
                    // Save the ID of this pointer (for dragging)
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0);

                     if(!infoState)
                        if(baseRect.contains(PlayerTouchX,PlayerTouchY)) {
                            movinBaseImg=true;
                        //Log.w("Warn", "Started moving");
                    }

                    break;
                }
                case MotionEvent.ACTION_MOVE:       //Finger is moving
                {
                    // Find the index of the active pointer and fetch its position
                    final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);

                    final float x = MotionEventCompat.getX(ev, pointerIndex);
                    final float y = MotionEventCompat.getY(ev, pointerIndex);

                    // Calculate the distance moved
                    final float dx = x - PlayerTouchX;
                    final float dy = y - PlayerTouchY;

                   // invalidate();     //We redraw after run() methods ends, no need to redraw here again

                    // Remember this touch position for the next move event
                    PlayerTouchX = (int) x;
                    PlayerTouchY = (int) y;

                    //move base image
                    if(movinBaseImg) {
                        baseRect.left = PlayerTouchX - baseImgWidth / 2;
                        baseRect.right = PlayerTouchX + baseImgWidth / 2;
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:     //Finger left screen
                {
                    movinBaseImg=false;
                    break;
                }
            }

            return true;
        }
    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_exit_small)
                .setMessage(R.string.confirm_exit_large)
                .setNegativeButton(R.string.confirm_exit_cancel, null)
                .setPositiveButton(R.string.confirm_exit_οκ, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                        Intent itn = new Intent(getApplicationContext(), menu.class); //go to menu screen with proper flag set
                        itn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        itn.putExtra("leaving", true);
                        startActivity(itn);
                    }
                }).create().show();
    }

    public class FallingExhibit     //on object used in mini game ExhibitsFall to hold data for falling exhibits
    {
        Bitmap image;
        Rect rect;
        String description; //information about the specific object
        public int delay;          //delay before it appears on screen
        public Boolean isFalling;
        public String name;

        public FallingExhibit(String _name, Bitmap _image, String _description, int _delay) {
            name=_name;
            image = _image;
            description = _description;
            delay = _delay;
            isFalling = false;
        }
    }
}



