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
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;


//this is for application stuff
public class PuzzleActivity extends Activity   {

    PauseMenuButton pauseBt;

    int screenWidth;
    int currentApiVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new PuzzleScreen(this));
        menu.hideNavBar(this.getWindow());

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenWidth = displaymetrics.widthPixels;
        pauseBt=new PauseMenuButton(screenWidth,this);

        // ------------------ Code in order to hide the navigation bar -------------------- //
        // The navigation bar is hiden and comes up only if the user swipes down the status bar
        currentApiVersion = Build.VERSION.SDK_INT; //get the current api

        // Initialize flags for full screen and hide navitation bar, immersive approach
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // This work only for android 4.4+
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT)
        {

            getWindow().getDecorView().setSystemUiVisibility(flags);

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            final View decorView = getWindow().getDecorView();
            decorView
                    .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
                    {
                        @Override
                        public void onSystemUiVisibilityChange(int visibility)
                        {
                            if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                            {
                                decorView.setSystemUiVisibility(flags);
                            }
                        }
                    });
        }

        //Start help screen
        Intent itn= new Intent(getApplicationContext(), HelpDialogActivity.class);
        itn.putExtra("appNum", 6);
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
        int imgWidth = 100, imgHeight = 100;
        int PlayerTouchX, PlayerTouchY;
        int mPosX, mPosY;   //coordinates in the middle on dragging
        Bitmap frame;
        Rect frameRect;
        int startingX=380,startingY=10;//where frame starts
        int extraSpace=30;  //piece matches with puzzle curves

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
            return  rand.nextInt(ScreenHeight- imgWidth)+startingY; //from 10 to where frame starts
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

            PuzzlePieces.add( new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p3),
                    getRandomRect(),
                    new Rect(startingX+2*imgWidth-extraSpace, startingY, startingX+2*imgWidth + imgWidth, startingY + imgHeight+extraSpace),
                    2));

            PuzzlePieces.add( new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p4),
                    getRandomRect(),
                    new Rect(startingX+3*imgWidth-extraSpace, startingY, startingX+3*imgWidth + imgWidth, startingY + imgHeight),
                    3));

            PuzzlePieces.add( new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p5),
                    getRandomRect(),
                    new Rect(startingX, startingY+imgHeight, startingX+imgWidth, startingY + imgHeight+imgHeight+extraSpace),
                    4));

            PuzzlePieces.add( new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p6),
                    getRandomRect(),
                    new Rect(startingX+imgWidth-extraSpace, startingY+imgHeight-extraSpace, startingX+imgWidth+ imgWidth+extraSpace, startingY +imgHeight+ imgHeight),
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

            PuzzlePieces.add( new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p12),
                    getRandomRect(),
                    new Rect(startingX+3*imgWidth-extraSpace, startingY+2*imgHeight-extraSpace, startingX+3*imgWidth+imgWidth, startingY +2*imgHeight+ imgHeight),
                    12));

            PuzzlePieces.add( new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p13),
                    getRandomRect(),
                    new Rect(startingX, startingY+3*imgHeight-extraSpace, startingX+imgWidth, startingY +3*imgHeight+ imgHeight),
                    13));

            PuzzlePieces.add( new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p14),
                    getRandomRect(),
                    new Rect(startingX+imgWidth-extraSpace, startingY+3*imgHeight, startingX+imgWidth+imgWidth, startingY +3*imgHeight+ imgHeight),
                    14));

            PuzzlePieces.add( new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p15),
                    getRandomRect(),
                    new Rect(startingX+2*imgWidth-extraSpace, startingY+3*imgHeight, startingX+2*imgWidth+imgWidth, startingY +3*imgHeight+ imgHeight),
                    15));

            PuzzlePieces.add( new PuzzlePiece(BitmapFactory.decodeResource(getResources(), R.drawable.p16),
                    getRandomRect(),
                    new Rect(startingX+3*imgWidth-extraSpace, startingY+3*imgHeight-extraSpace, startingX+3*imgWidth+imgWidth, startingY +3*imgHeight+ imgHeight),
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
            startActivity(itn);

            QrCodeScanner.questionMode=true;
            finish();

        }

        public void DroppedPiece() //Player was dragging a piece, check if its near the original position
        {

            Log.w("Warn","DROPPED PIECE");
            if( currentMovingPiece.rect.intersect(new Rect( currentMovingPiece.originalRec.left,currentMovingPiece.originalRec.top,
                    currentMovingPiece.originalRec.right-50,currentMovingPiece.originalRec.bottom-50)))
            {
                Log.w("Warn", "SUCCESSFULLY");
                currentMovingPiece.rect.left = currentMovingPiece.originalRec.left; //assign by value
                currentMovingPiece.rect.top = currentMovingPiece.originalRec.top;
                currentMovingPiece.rect.right = currentMovingPiece.originalRec.right;
                currentMovingPiece.rect.bottom = currentMovingPiece.originalRec.bottom;
                currentMovingPiece.placedCorrectly=true;

                CheckFinishedPuzzle();

            }
        }


        public boolean CheckCollision() //finds the piece the user selected: returns true
        {
           for(PuzzlePiece p : PuzzlePieces)
               if( p.rect.contains(PlayerTouchX,PlayerTouchY) && !p.placedCorrectly)//cant move correctly places pieces
               {
                   Log.w("Warn","COLLISION");
                   currentMovingPiece=p;
                   return true;
               }
            return false;
        }

        @Override
        public void run() {   // Update state of what we draw
            // Log.w("Warn","Run Method Called");


            // Request a redraw of this view
            // onDraw(Canvas) will be called
            invalidate();
        }


        @Override
        protected void onDraw(Canvas canvas)
        {
            //  Log.w("Warn","Draw Method Called");

            super.onDraw(canvas);

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#FFC5AA22"));//background: dark gold
            canvas.drawPaint(paint);

            pauseBt.getPauseMenuButton().draw(canvas);

            // Draw frame
            canvas.drawBitmap(frame, null, frameRect, null);

            // Draw puzzle pieces
            for(PuzzlePiece p : PuzzlePieces)
                canvas.drawBitmap(p.img, null, p.rect, null);

            // Invalidate view at about 60fps
            postDelayed(this, 16);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {

            //Log.w("Warn","Action:"+ev.getAction()+" Touched: "+ PlayerTouchX +"x"+ PlayerTouchY);
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
                        Log.w("Warn", "Started moving");
                        movingSomething = true;
                    }
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

                        if(currentMovingPiece!=null)
                        {
                            currentMovingPiece.rect.left = PlayerTouchX;
                            currentMovingPiece.rect.top = PlayerTouchY;
                            currentMovingPiece.rect.right = PlayerTouchX + imgWidth;
                            currentMovingPiece.rect.bottom = PlayerTouchY + imgHeight;
                        }
                        else
                            Log.w("Warn","currentMovingPiece IS NULL");
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:     //Finger left screen
                {
                    Log.w("Warn","ACTION UP");
                    if(movingSomething)
                    {
                        DroppedPiece();

                        movingSomething = false;
                    }

                    break;
                }
                case MotionEvent.ACTION_CANCEL: //Current event has been canceled, something else took control of the touch event
                {
                    Log.w("Warn","ACTION CANCEL");
                    movingSomething=false;
                    break;
                }
                default:            //whatever happens
                {
                    Log.w("Warn","default");
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
                        System.exit(0);
                    }
                }).create().show();
    }
}



