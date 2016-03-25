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
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


/**
 * Created by theo on 9/7/2015.
 */
public class StartGame extends Activity {

    StartGameOurview v;
    int height,width,newInt;
    public static int startingStage=1;
    String nextApp;
    String [] rooms;

    PauseMenuButton pauseBt;
    float textViewX,textViewY;//the position of the textview

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Hide everything...:)
        menu.hideNavBar(this.getWindow());

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        height = displaymetrics.heightPixels;
        width = displaymetrics.widthPixels;

        QuizGameActivity.firstQuiz=true;

        pauseBt=new PauseMenuButton(width,this);
        rooms=getResources().getStringArray(R.array.rooms_startgame);
        if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
        {
            rooms=MainActivity.GetAllRoomNamesAsList();
        }

        nextApp="nextApp";
        PauseMenuActivity.pause=false;

        v = new StartGameOurview(this);
        setContentView(v);
    }


    // Check if pause button is pressed then start pause menu activity
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        if(pauseBt.getRect().contains((int)touchX,(int)touchY))
        {
            if(event.getAction()==MotionEvent.ACTION_DOWN) {
                PauseMenuActivity.pause = true;
                Intent itn;
                itn = new Intent(getApplicationContext(), PauseMenuActivity.class);
                startActivity(itn);
            }

        }

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        v.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        menu.hideNavBar(this.getWindow());//hide everything on Resume
        try {

            //If resume from pause make pause false and revert startingStage to previous condition
            if(PauseMenuActivity.pause==true)
            {
                PauseMenuActivity.pause=false;
                //Stage increases only if player plays the quiz,
                //New increment location into QrCodeScanner!
                //startingStage--;
            }
            v.resume();


        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //StageCounter
        //startingStage++;


    }

    @Override
    protected void onStop() {
        super.onStop();

       // startingStage--;
        Log.d("stopped", "Start Game stopped");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public class StartGameOurview extends SurfaceView implements Runnable {

        Thread t;
        SurfaceHolder holder;
        boolean isItok = false;
        Bitmap ppenguin, background;
        Sprite sprite;
        Paint paint;//Text style in TextView

        //CONSTRUCTOR
        public StartGameOurview(Context context) {
            super(context);
            holder = getHolder();
            ppenguin = BitmapFactory.decodeResource(getResources(), R.drawable.penguinsheet);
            background = BitmapFactory.decodeResource(getResources(), R.drawable.museum_map);
            background = Bitmap.createScaledBitmap(background, width, (int) Math.ceil(height * 0.95), false);

            paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize((float) Math.ceil(height * 0.045));
            paint.setTextAlign(Paint.Align.CENTER);

            textViewX = (float) Math.ceil(width * 0.8);
            textViewY = (float) Math.ceil(height * 0.3);

        }

        @Override
        public void run() {

            //Only when not paused
            if (PauseMenuActivity.pause == false) {
                sprite = new Sprite(StartGameOurview.this, ppenguin, width, height, startingStage);
                while (isItok) {

                    //perform drawing
                    if (!holder.getSurface().isValid()) {
                        continue;
                    }

                    Canvas c = holder.lockCanvas();
                    draw(c);
                    holder.unlockCanvasAndPost(c);

                }

            }

        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);

            //Only when not paused
            if (PauseMenuActivity.pause == false) {
                canvas.drawColor(Color.parseColor("#0B0075"));
                canvas.drawBitmap(background, 0, 0, null);
                //Symbol \n cannot be realized from android , too bad

                canvas.drawText(getResources().getString(R.string.room_word) + " " + startingStage + " :", textViewX, textViewY - 30, paint);
                canvas.drawText(setTextTitle()[0], textViewX, textViewY + 100, paint);
                canvas.drawText(setTextTitle()[1], textViewX, textViewY + 200, paint);
                canvas.drawText(setTextTitle()[2], textViewX, textViewY + 300, paint);

                newInt = sprite.onDraw(canvas);
                pauseBt.getPauseMenuButton().draw(canvas);


                // When character is in a room, start the qr scanner activity and pass the next riddle number to it
                if (newInt != 0) {
                    Intent itns = new Intent(getApplicationContext(), QrCodeScanner.class);
                    itns.putExtra(nextApp, newInt);
                    startActivity(itns);
                }
            }

        }

        public void pause() {
            isItok = false;
            while (true) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
        }

        //EDW KSEKINAEI NEO THREAD KATHE FORA
        public void resume() throws InterruptedException {
            isItok = true;
            t = new Thread(this);
            //This sleep is needed beacause switch activity takes time
            Thread.sleep(1000);
            t.start();
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

    public String [] setTextTitle(){
        if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
        {
            return new String[]{ rooms[startingStage-1],"",""};
        }
        else {
            if (startingStage == 1) {
                return new String[]{rooms[0], "", ""};
            } else if (startingStage == 2) {
                return new String[]{rooms[1], rooms[2], ""};
            } else if (startingStage == 3) {
                return new String[]{rooms[3], rooms[4], ""};
            } else if (startingStage == 4) {
                return new String[]{rooms[5], rooms[6], rooms[7]};
            } else if (startingStage == 5) {
                return new String[]{rooms[8], rooms[9], ""};
            } else if (startingStage == 6) {
                return new String[]{rooms[10], "", ""};
            } else if (startingStage == 7) {
                return new String[]{rooms[11], "", ""};
            } else if (startingStage == 8) {
                return new String[]{rooms[12], "", ""};
            } else if (startingStage == 9) {
                return new String[]{rooms[13], "", ""};
            } else if (startingStage == 10) {
                return new String[]{rooms[14], rooms[15], rooms[16]};
            } else if (startingStage == 11) {
                return new String[]{rooms[17],  rooms[18], ""};
            }
        }
        return null;
    }
}