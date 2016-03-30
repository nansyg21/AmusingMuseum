package com.asus_s550cb.theo.museum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;


public class MatchingStamps extends Activity {

    PauseMenuButton pauseBt;

    int screenWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);//hide status bar
        setContentView(new MatchingStampsScreen(this));

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenWidth = displaymetrics.widthPixels;
        pauseBt=new PauseMenuButton(screenWidth,this);

        // ------------------ Code in order to hide the navigation bar -------------------- //
        menu.hideNavBar(this.getWindow());

        //Start help screen
        Intent itn= new Intent(getApplicationContext(), HelpDialogActivity.class);
        itn.putExtra("appNum",5);
        startActivity(itn);
    }

    // Reset the flags to hide the navigation bar
    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        menu.hideNavBar(this.getWindow());
    }



    //This is actually drawing on screen the game : Matching Stamps
    //A couple of pictures appear for each stamp. The player matches the stamps together
    public class MatchingStampsScreen extends View implements Runnable
    {
        int ScreenWidth,ScreenHeight, hits,PlayerTouchX, PlayerTouchY, mPosX, mPosY,frameWidth,frameHeight, imgWidth,imgHeight; ;
        Paint backgroundPaint, linePaint;
        Bitmap frameImg;
        ArrayList<Bitmap> stampsList = new ArrayList<Bitmap>();//1 matches with 2, 3 with 4 etc
        Rect frame1Rect,frame2Rect;
        ArrayList<Rect> stampsRectList = new ArrayList<Rect>();
        Random rand = new Random();

        boolean movingSomething=false;
        Rect currentMovingStamp;
        int currentMovingStampId, leftStamp, rightStamp;

        boolean wave2=false;    //true once wave 1 is done

        private int mActivePointerId = -1;      //-1 instead of INVALID_POINTER_ID

        public MatchingStampsScreen(Context context)
        {
            super(context);


            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            ScreenHeight = displaymetrics.heightPixels;
            ScreenWidth = displaymetrics.widthPixels;

            //for background color
            backgroundPaint = new Paint();
            backgroundPaint.setStyle(Paint.Style.FILL);
            backgroundPaint.setColor(Color.parseColor("#8F0014"));//background: bussini

            //Paint for line
            linePaint = new Paint();
            linePaint.setStyle(Paint.Style.FILL);
            linePaint.setColor(Color.parseColor("#ff0000"));//background: dark blue royal

            //frame image
            frameWidth=ScreenWidth/5;
            frameHeight=frameWidth;
            frameImg = BitmapFactory.decodeResource(getResources(), R.drawable.match_frame);
            frame1Rect= new Rect(ScreenWidth/2-frameWidth,ScreenHeight/2-frameHeight/2,
                    ScreenWidth/2-frameWidth+frameWidth,ScreenHeight/2-frameHeight/2+frameHeight);


            frame2Rect= new Rect(ScreenWidth/2,frame1Rect.top, ScreenWidth/2+frameWidth,frame1Rect.bottom);

            hits=0;
            //stamp images
            imgWidth=ScreenWidth/6;
            imgHeight=imgWidth;

            InitializeStampImages("1"); //call wave 1

            leftStamp =-1;
            rightStamp =-1;

        }

        public void InitializeStampImages(String wave) {

            stampsList.clear();
            if(wave.equals("1"))
            {
                stampsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_1));
                stampsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_2));
                stampsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_3));
                stampsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_4));
                stampsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_5));
                stampsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_6));
                stampsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_7));
                stampsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_8));
            }
            else if (wave.equals("2"))
            {
                stampsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_9));
                stampsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_10));
                stampsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_11));
                stampsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_12));
                stampsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_13));
                stampsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_14));
                stampsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_15));
                stampsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.mc_16));
            }
            else
                Log.w("Warn","Wrong wave called.");
            InitializeStampRects();

        }

        public void InitializeStampRects() {
            stampsRectList.clear();
            int x,y;

            for(int i=0;i< stampsList.size()/2;i++)
            {
                //left side
                x = rand.nextInt(ScreenWidth / 2 - imgWidth);
                y = rand.nextInt(ScreenHeight - imgHeight);
                stampsRectList.add(new Rect(x, y, x + imgWidth, y + imgHeight));

                //right side
                x = rand.nextInt(ScreenWidth / 2 - imgWidth) + ScreenWidth / 2;
                y = rand.nextInt(ScreenHeight - imgHeight);
                stampsRectList.add(new Rect(x, y, x + imgWidth, y + imgHeight));
            }


        }


        public void DroppedPiece() //Player was dragging a piece, check if its near the original position
        {

            if(currentMovingStampId %2==0)
            {//frame 1 - left side
                if (currentMovingStamp.intersect(new Rect(frame1Rect.left, frame1Rect.top,   //empty frame 1:drop piece
                        frame1Rect.right - 50, frame1Rect.bottom - 50)) && leftStamp == -1) {
                    currentMovingStamp.left = frame1Rect.left; //assign by value
                    currentMovingStamp.top = frame1Rect.top;
                    currentMovingStamp.right = frame1Rect.right;
                    currentMovingStamp.bottom = frame1Rect.bottom;

                    leftStamp = currentMovingStampId;
                } else if (currentMovingStamp.intersect(new Rect(frame1Rect.left, frame1Rect.top,//moved the piece that already is in frame:drop the same piece again
                        frame1Rect.right - 50, frame1Rect.bottom - 50)) && leftStamp == currentMovingStampId) {
                    currentMovingStamp.left = frame1Rect.left; //assign by value
                    currentMovingStamp.top = frame1Rect.top;
                    currentMovingStamp.right = frame1Rect.right;
                    currentMovingStamp.bottom = frame1Rect.bottom;

                    leftStamp = currentMovingStampId;
                } else if (currentMovingStamp.intersect(new Rect(frame1Rect.left, frame1Rect.top,   //tried to drop piece in frame 1 but there is another piece: find random position and drop it
                        frame1Rect.right - 50, frame1Rect.bottom - 50)) && leftStamp != -1 && leftStamp != currentMovingStampId) {
                    int x = rand.nextInt(ScreenWidth / 2 - imgWidth);
                    int y = rand.nextInt(ScreenHeight - imgHeight);
                    stampsRectList.set(currentMovingStampId, new Rect(x, y, x + imgWidth, y + imgHeight));
                } else if (!currentMovingStamp.intersect(new Rect(frame1Rect.left, frame1Rect.top,   //Dropped piece in open space
                        frame1Rect.right - 50, frame1Rect.bottom - 50))) {
                    if (currentMovingStampId == leftStamp)//Dropped piece that was in frame
                        leftStamp = -1;
                    currentMovingStampId = -1;         //update value

                }

            }
            else if(currentMovingStampId %2==1)
            {//frame 2 = right side
                if (currentMovingStamp.intersect(new Rect(frame2Rect.left, frame2Rect.top,//empty frame 2:drop piece
                        frame2Rect.right , frame2Rect.bottom )) && rightStamp == -1) {
                    currentMovingStamp.left = frame2Rect.left; //assign by value
                    currentMovingStamp.top = frame2Rect.top;
                    currentMovingStamp.right = frame2Rect.right;
                    currentMovingStamp.bottom = frame2Rect.bottom;

                    rightStamp = currentMovingStampId;
                } else if (currentMovingStamp.intersect(new Rect(frame2Rect.left, frame2Rect.top,//moved the piece that already is in frame 2:drop the same piece again
                        frame2Rect.right , frame2Rect.bottom )) && rightStamp == currentMovingStampId) {
                    currentMovingStamp.left = frame2Rect.left; //assign by value
                    currentMovingStamp.top = frame2Rect.top;
                    currentMovingStamp.right = frame2Rect.right;
                    currentMovingStamp.bottom = frame2Rect.bottom;

                    rightStamp = currentMovingStampId;
                } else if (currentMovingStamp.intersect(new Rect(frame2Rect.left, frame2Rect.top, //tried to drop piece in frame 2 but there is another piece: find random position and drop it
                        frame2Rect.right , frame2Rect.bottom )) && rightStamp != -1 && rightStamp != currentMovingStampId) {
                    //right side
                    int x = rand.nextInt(ScreenWidth / 2 - imgWidth) + ScreenWidth / 2;
                    int y = rand.nextInt(ScreenHeight - imgHeight);
                    stampsRectList.set(currentMovingStampId, new Rect(x, y, x + imgWidth, y + imgHeight));
                } else if (!currentMovingStamp.intersect(new Rect(frame2Rect.left, frame2Rect.top, //Dropped piece in open space
                        frame2Rect.right , frame2Rect.bottom ))) {
                    if (currentMovingStampId == rightStamp)//Dropped piece that was in frame
                        rightStamp = -1;
                    currentMovingStampId = -1;         //update value
                }
            }

            Log.w("Warn","Left:"+ leftStamp +" right:"+ rightStamp);
            CheckMatch();

        }

        public void CheckMatch()
        {
            if(leftStamp +1== rightStamp)   //0 matches with 1 , 2 with 3 etc..
            {
                SoundHandler.PlaySound(SoundHandler.correct_sound_id4);
                stampsList.remove(rightStamp);
                stampsList.remove(leftStamp);
                stampsRectList.remove(rightStamp);
                stampsRectList.remove(leftStamp);

                if(stampsList.size()==0)
                {
                    InitializeStampImages("2");  //call wave 2
                    if(wave2)
                    {
                        //Calculate Save and Show Score
                        if(hits<=16)
                            Score.setRiddleScore(70) ;
                        else if(hits<=32)
                            Score.setRiddleScore(35) ;
                        else if(hits<=40)
                            Score.setRiddleScore(20) ;
                        else
                            Score.setRiddleScore(0) ;
                        Intent itn= new Intent(getApplicationContext(), Score.class);
                        itn.putExtra("nextStage", 5);
                        startActivity(itn);

                        QrCodeScanner.questionMode=true;
                        finish();
                    }
                    else
                        wave2=true;
                }

                Log.w("Warn", "still " + stampsList.size() + " stamps");
                leftStamp =-1;
                rightStamp =-1;
                currentMovingStamp =null;
                currentMovingStampId =-1;
            }



        }

        public boolean CheckCollision() //finds the piece the user selected: returns true
        {
            for(int i=0;i< stampsRectList.size();i++)
                if( stampsRectList.get(i).contains(PlayerTouchX, PlayerTouchY))//cant move correctly places pieces
                {
                    currentMovingStamp = stampsRectList.get(i);
                    currentMovingStampId =i;

                    return true;
                }
            return false;
        }


        @Override
        public void run()
        {   // Update state of what we draw

            // onDraw(Canvas) will be called
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            super.onDraw(canvas);

            //Draw color on background
            canvas.drawPaint(backgroundPaint);

            //draw line in the middle
            canvas.drawLine(ScreenWidth / 2, 0, ScreenWidth / 2, ScreenHeight, linePaint);

            //draw 2 frames
            canvas.drawBitmap(frameImg,null,frame1Rect,null);
            canvas.drawBitmap(frameImg,null,frame2Rect,null);

            // Draw stamps
            for(int i=0;i< stampsList.size();i++)
                canvas.drawBitmap(stampsList.get(i), null, stampsRectList.get(i), null);

            pauseBt.getPauseMenuButton().draw(canvas);

            // Invalidate view at about 60fps
            postDelayed(this, 16);
        }


        @Override
        public boolean onTouchEvent(MotionEvent ev)
        {
            switch (ev.getAction())
            {
                case MotionEvent.ACTION_DOWN:       //New touch started
                {
                    //Check if pause button is hit
                        float touchX = ev.getX();
                        float touchY = ev.getY();

                        if(pauseBt.getRect().contains((int)touchX,(int)touchY)) {
                            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                                Intent itn;
                                itn = new Intent(getApplicationContext(), PauseMenuActivity.class);
                                startActivity(itn);
                            }
                        }

                    final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                    final float x = MotionEventCompat.getX(ev, pointerIndex);
                    final float y = MotionEventCompat.getY(ev, pointerIndex);

                    // Remember where we started (for dragging)
                    PlayerTouchX = (int) x;
                    PlayerTouchY = (int) y;

                    // Save the ID of this pointer (for dragging)
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0);

                    if(!movingSomething && CheckCollision())    //CheckCollision:true:selected piece  false: selected empty
                        movingSomething = true;

                    break;
                }

                case MotionEvent.ACTION_MOVE:       //Finger is moving
                {
                    if(movingSomething)
                    {
                        //  Log.w("Warn","Dragging...");
                        // Find the index of the active pointer and fetch its position
                        final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);

                        final float x = MotionEventCompat.getX(ev, pointerIndex);
                        final float y = MotionEventCompat.getY(ev, pointerIndex);

                        // Calculate the distance moved
                        final float dx = x - PlayerTouchX;
                        final float dy = y - PlayerTouchY;

                        mPosX += dx;
                        mPosY += dy;

                        invalidate();

                        // Remember this touch position for the next move event
                        PlayerTouchX = (int) x;
                        PlayerTouchY = (int) y;

                        if(currentMovingStamp !=null && PlayerTouchX+imgWidth<ScreenWidth && PlayerTouchY+imgHeight<ScreenHeight)
                            if(currentMovingStampId %2==0 && PlayerTouchX+imgWidth<ScreenWidth/2) //cant move a left piece from the right side
                            {
                                currentMovingStamp.left = PlayerTouchX;
                                currentMovingStamp.top = PlayerTouchY;
                                currentMovingStamp.right = PlayerTouchX + imgWidth;
                                currentMovingStamp.bottom = PlayerTouchY + imgHeight;
                            }
                            else if(currentMovingStampId %2==1 && PlayerTouchX>ScreenWidth/2) //cant move a right piece from the left side
                            {
                                currentMovingStamp.left = PlayerTouchX;
                                currentMovingStamp.top = PlayerTouchY;
                                currentMovingStamp.right = PlayerTouchX + imgWidth;
                                currentMovingStamp.bottom = PlayerTouchY + imgHeight;
                            }
                      //  else
                         //   Log.w("Warn","currentMovingPiece IS NULL");
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:     //Finger left screen
                {
                    if(movingSomething)
                    {
                        DroppedPiece();
                        movingSomething = false;
                        hits++;
                        Log.w("Warn","Hits: "+hits);
                    }
                    break;
                }
                case MotionEvent.ACTION_CANCEL: //Current event has been canceled, something else took control of the touch event
                {
                    movingSomething=false;
                    break;
                }
                default:            //whatever happens
                {
                    movingSomething=false;
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