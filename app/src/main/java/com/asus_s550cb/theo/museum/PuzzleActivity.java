package com.asus_s550cb.theo.museum;

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
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;


//this is for application stuff
public class PuzzleActivity extends Activity   {

    PauseMenuButton pauseBt;
    PauseMenuButton skipBt; //temporary   //TODO: Delete this button when no necessary


    int screenWidth;
    int currentApiVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new PuzzleScreen(this));


        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenWidth = displaymetrics.widthPixels;
        pauseBt=new PauseMenuButton(screenWidth,this);
        skipBt=new PauseMenuButton(screenWidth/2,this);


        // ------------------ Code in order to hide the navigation bar -------------------- //
        menu.hideNavBar(this.getWindow());

        //Start help screen
        Intent itn= new Intent(getApplicationContext(), HelpDialogActivity.class);
        itn.putExtra("appNum", 2);
        startActivity(itn);


    }

    //This is actually drawing on screen puzzle stuff : 4x4 puzzle
    public class PuzzleScreen extends View implements Runnable {

        ArrayList<PuzzlePiece> PuzzlePieces = new ArrayList<PuzzlePiece>();
        PuzzlePiece currentMovingPiece;


        /**
         * #EFCF9C oudetero mpez
         #0B0075 blue royal
         #8F0014 bussini
         #E9D308 gold
         *
         * Rect:(Left, Right, Top, Bottom) |3rd and 4rth arg: Not width and height|  left < right and top < bottom  *
         */
        public Random rand= new Random();
        MediaPlayer puzzle_completed_sound ;
        int ScreenWidth,ScreenHeight;
        int imgWidth , imgHeight;
        int PlayerTouchX, PlayerTouchY;
        int mPosX, mPosY;   //coordinates in the middle on dragging
        Bitmap frame;
        Rect frameRect;
        int startingX,startingY;//where frame starts
        int extraSpace;  //piece matches with puzzle curves

        boolean movingSomething=false;

        //Drag puzzle piece: The ‘active pointer’ is the one currently moving our object.
        private int mActivePointerId = -1;      //-1 instead of INVALID_POINTER_ID


        public PuzzleScreen(Context context)
        {
            super(context);

            puzzle_completed_sound= MediaPlayer.create(this.getContext(), R.raw.puzzle_completed);

            //get screen size
            Point size = new Point();
            getWindowManager().getDefaultDisplay().getSize(size);
            ScreenWidth=size.x;
            ScreenHeight=size.y;

            startingX=ScreenWidth/2;
            startingY=ScreenHeight/12;

            imgWidth=ScreenWidth/8;
            imgHeight=imgWidth;

            extraSpace=imgWidth/3;

            InitializePuzzlePieces();

            frame = BitmapFactory.decodeResource(getResources(), R.drawable.frame1);
            frameRect =  new Rect(startingX, startingY, startingX+4*imgWidth, startingY+4*imgHeight); //static numbers
        }

        public int getRandX()
        {
            return  rand.nextInt(startingX-imgWidth)+10; //from 10 to where frame starts
        }

        public int getRandY()
        {
            return  rand.nextInt(startingY+ 2*imgWidth)+startingY; //from 10 to where frame starts
        }

        public Rect getRandomRect()
        {
            int  x=getRandX();
            int y=getRandY();
            return    new Rect(x, y, x + imgWidth, y + imgHeight);
        }

        public void InitializePuzzlePieces()
        {
            //original pieces positions: where they should be placed are specific and cannot be changes easily

            PuzzlePieces.add(new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p1),
                    getRandomRect(),
                    new Rect(startingX, startingY, startingX + imgWidth, startingY + imgHeight + extraSpace),
                    0));

            PuzzlePieces.add(new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p2),
                    getRandomRect(),
                    new Rect(startingX + imgWidth - extraSpace, startingY, startingX + imgWidth + imgWidth, startingY + imgHeight),
                    1));

            PuzzlePieces.add(new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p3),
                    getRandomRect(),
                    new Rect(startingX + 2 * imgWidth - extraSpace, startingY, startingX + 2 * imgWidth + imgWidth, startingY + imgHeight + extraSpace),
                    2));

            PuzzlePieces.add( new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p4),
                    getRandomRect(),
                    new Rect(startingX+3*imgWidth-extraSpace, startingY, startingX+3*imgWidth + imgWidth, startingY + imgHeight),
                    3));

            PuzzlePieces.add( new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p5),
                    getRandomRect(),
                    new Rect(startingX, startingY+imgHeight, startingX+imgWidth, startingY + imgHeight+imgHeight+extraSpace),
                    4));

            PuzzlePieces.add(new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p6),
                    getRandomRect(),
                    new Rect(startingX + imgWidth - extraSpace, startingY + imgHeight - extraSpace, startingX + imgWidth + imgWidth + extraSpace, startingY + imgHeight + imgHeight),
                    5));

            PuzzlePieces.add( new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p7),
                    getRandomRect(),
                    new Rect(startingX+2*imgWidth, startingY+imgHeight, startingX+2*imgWidth+ imgWidth, startingY +imgHeight+ imgHeight+extraSpace),
                    6));

            PuzzlePieces.add( new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p8),
                    getRandomRect(),
                    new Rect(startingX+3*imgWidth-extraSpace, startingY+imgHeight-extraSpace, startingX+3*imgWidth+ imgWidth, startingY +imgHeight+ imgHeight),
                    7));

            PuzzlePieces.add( new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p9),
                    getRandomRect(),
                    new Rect(startingX, startingY+2*imgHeight, startingX+imgWidth, startingY +2*imgHeight+ imgHeight),
                    9));

            PuzzlePieces.add( new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p10),
                    getRandomRect(),
                    new Rect(startingX+imgWidth-extraSpace, startingY+2*imgHeight-extraSpace, startingX+imgWidth+imgWidth, startingY +2*imgHeight+ imgHeight+extraSpace),
                    10));

            PuzzlePieces.add( new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p11),
                    getRandomRect(),
                    new Rect(startingX+2*imgWidth-extraSpace, startingY+2*imgHeight, startingX+2*imgWidth+imgWidth, startingY +2*imgHeight+ imgHeight+extraSpace),
                    11));

            PuzzlePieces.add(new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p12),
                    getRandomRect(),
                    new Rect(startingX + 3 * imgWidth - extraSpace, startingY + 2 * imgHeight - extraSpace, startingX + 3 * imgWidth + imgWidth, startingY + 2 * imgHeight + imgHeight),
                    12));

            PuzzlePieces.add(new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p13),
                    getRandomRect(),
                    new Rect(startingX, startingY + 3 * imgHeight - extraSpace, startingX + imgWidth, startingY + 3 * imgHeight + imgHeight),
                    13));

            PuzzlePieces.add(new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p14),
                    getRandomRect(),
                    new Rect(startingX + imgWidth - extraSpace, startingY + 3 * imgHeight, startingX + imgWidth + imgWidth, startingY + 3 * imgHeight + imgHeight),
                    14));

            PuzzlePieces.add( new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p15),
                    getRandomRect(),
                    new Rect(startingX+2*imgWidth-extraSpace, startingY+3*imgHeight, startingX+2*imgWidth+imgWidth, startingY +3*imgHeight+ imgHeight),
                    15));

            PuzzlePieces.add(new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p16),
                    getRandomRect(),
                    new Rect(startingX + 3 * imgWidth - extraSpace, startingY + 3 * imgHeight - extraSpace, startingX + 3 * imgWidth + imgWidth, startingY + 3 * imgHeight + imgHeight),
                    16));

        }

        public void CheckFinishedPuzzle()
        {
            for(PuzzlePiece p : PuzzlePieces)
                if(!p.placedCorrectly)
                    return;

            //all pieces are correctly placed
            frame = BitmapFactory.decodeResource(getResources(), R.drawable.frame2);
            puzzle_completed_sound.start();


            //Save and Show Score
            Score.setRiddleScore(70) ;//full score
            Intent itn= new Intent(getApplicationContext(), Score.class);
            itn.putExtra("nextStage", 2);
            startActivity(itn);

            QrCodeScanner.questionMode=true;
            finish();


        }

        public void DroppedPiece() //Player was dragging a piece, check if its near the original position
        {
            if( currentMovingPiece.rect.contains(new Rect(currentMovingPiece.originalRec.left + 2*imgWidth / 5, currentMovingPiece.originalRec.top + 3*imgHeight / 5,
                    currentMovingPiece.originalRec.right - 2*imgWidth / 5, currentMovingPiece.originalRec.bottom - 2*imgHeight / 5)))
            {
                currentMovingPiece.rect.left = currentMovingPiece.originalRec.left; //assign by value
                currentMovingPiece.rect.top = currentMovingPiece.originalRec.top;
                currentMovingPiece.rect.right = currentMovingPiece.originalRec.right;
                currentMovingPiece.rect.bottom = currentMovingPiece.originalRec.bottom;
                currentMovingPiece.placedCorrectly=true;

                SoundHandler.PlaySound(SoundHandler.correct_sound_id3);
                CheckFinishedPuzzle();

            }
        }


        public boolean CheckCollision() //finds the piece the user selected: returns true
        {
            for(PuzzlePiece p : PuzzlePieces)
                if( p.rect.contains(PlayerTouchX,PlayerTouchY) && !p.placedCorrectly)//cant move correctly places pieces
                {
                    currentMovingPiece=p;
                    return true;
                }
            return false;
        }

        @Override
        public void run() {   // Update state of what we draw

            // Request a redraw of this view
            // onDraw(Canvas) will be called
            invalidate();
        }


        @Override
        protected void onDraw(Canvas canvas)
        {
            super.onDraw(canvas);

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#FFC5AA22"));//background: dark gold
            canvas.drawPaint(paint);

            pauseBt.getPauseMenuButton().draw(canvas);
            skipBt.getPauseMenuButton().draw(canvas);

            // Draw frame
            canvas.drawBitmap(frame, null, frameRect, null);

            // Draw puzzle pieces
            for(PuzzlePiece p : PuzzlePieces)
                canvas.drawBitmap(p.img, null, p.rect, null);

            /** //Execute to see the area the dropped piece must cover to be accepted as correct
             if(currentMovingPiece!=null) {
             Paint tmpPaint = new Paint();
             tmpPaint.setColor(Color.BLACK);

             canvas.drawRect(new Rect(currentMovingPiece.originalRec.left + 2*imgWidth / 5, currentMovingPiece.originalRec.top + 3*imgHeight / 5,
             currentMovingPiece.originalRec.right - 2*imgWidth / 5, currentMovingPiece.originalRec.bottom - 2*imgHeight / 5), tmpPaint);
             }
             **/
            // Invalidate view at about 60fps
            postDelayed(this, 16);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {

            switch (ev.getAction())
            {
                case MotionEvent.ACTION_DOWN:       //New touch started
                {

                    // Check if pause button is pressed
                    float touchX = ev.getX();
                    float touchY = ev.getY();

                    if(pauseBt.getRect().contains((int)touchX,(int)touchY))
                    {
                        if(ev.getAction()==MotionEvent.ACTION_DOWN) {
                            Intent itn;
                            itn = new Intent(getApplicationContext(), PauseMenuActivity.class);
                            startActivity(itn);
                        }

                    }

                    if(skipBt.getRect().contains((int)touchX,(int)touchY))
                    {
                        if(ev.getAction()==MotionEvent.ACTION_DOWN) {
                            //all pieces are correctly placed
                            frame = BitmapFactory.decodeResource(getResources(), R.drawable.frame2);
                            puzzle_completed_sound.start();


                            //Save and Show Score
                            Score.setRiddleScore(70) ;//full score
                            Intent itn= new Intent(getApplicationContext(), Score.class);
                            itn.putExtra("nextStage", 2);
                            startActivity(itn);

                            QrCodeScanner.questionMode=true;



                            finish();


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
                    {
                        movingSomething = true;
                    }
                    break;
                }

                case MotionEvent.ACTION_MOVE:       //Finger is moving
                {
                    if(movingSomething)
                    {
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

                        if(currentMovingPiece!=null)
                        {
                            currentMovingPiece.rect.left = PlayerTouchX;
                            currentMovingPiece.rect.top = PlayerTouchY;
                            currentMovingPiece.rect.right = PlayerTouchX + imgWidth;
                            currentMovingPiece.rect.bottom = PlayerTouchY + imgHeight;
                        }
                        // else
                        //    Log.w("Warn","currentMovingPiece IS NULL");
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:     //Finger left screen
                {
                    if(movingSomething)
                    {
                        DroppedPiece();

                        movingSomething = false;
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
    //Remember to hide everything when Activity Resumes...
    @Override
    protected void onResume() {
        super.onResume();
        menu.hideNavBar(this.getWindow());
    }
}



