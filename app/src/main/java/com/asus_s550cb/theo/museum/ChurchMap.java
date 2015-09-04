package com.asus_s550cb.theo.museum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChurchMap extends Activity {

    private int currentApiVersion;
    private ImageView mapImageView; // Greece map
    private ImageView monumentImageView; // Current monument image
    private TextView monumentNameTextView; // Current monument name

    // For random choice
    private Random random;
    private int num;

    // Monuments already used in this session
    private List<String> usedMonuments;

    // List of monuments names
    private String[] monumentNameArray;

    private Rect[] monumentsRects; // List of all monuments rectangles
    private Rect touchRect; // Create rectangle on touch --> helps collision
    private Rect selectedRect; // Current monument's rectangle

    //Screen dimensions
    private int screenWidth;
    private int screenHeight;

    private List<Point> correctPoints; //List of correct points -- green circles
    private List<Point> wrongPoints; // List of wrong points -- red circles

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_church_map);


             /*   .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });*/
        // Create the AlertDialog object and return it

   //     builder.create();

        // Get screen dimensions -- pixels
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenHeight = displaymetrics.heightPixels;
        screenWidth = displaymetrics.widthPixels;

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

        // Initalizations
        usedMonuments=new ArrayList<String>();

        correctPoints=new ArrayList<Point>();
        wrongPoints=new ArrayList<Point>();

        monumentNameArray=getResources().getStringArray(R.array.monument_names);

        random=new Random(System.currentTimeMillis());

        mapImageView=(ImageView) findViewById(R.id.greeceMap);
        monumentImageView=(ImageView) findViewById(R.id.church_ImageView);
        monumentNameTextView=(TextView) findViewById(R.id.nameTextView);

        monumentsRects=new Rect[monumentNameArray.length];
        touchRect=new Rect(0,0,0,0);
        selectedRect=new Rect(0,0,0,0);

        // When the user touches the image for the first time with each touch down create a circle where touched and check location
        mapImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    createBitMap(event.getX(), event.getY());
                return true;
            }
        });

        // Create all monuments' rectangles
        populateMonumentRectangles();

        //Choose a monument to start
        setMonument();

        //Start help screen
        Intent itn= new Intent(getApplicationContext(), HelpDialogActivity.class);
        itn.putExtra("appNum",2);
        startActivity(itn);

    }

    // Reset the flags to hide the navigation bar
    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus)
        {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    // Set the question for each monument
    // Select a random monument that has not been used in this session
    private void setMonument()
    {
       num=random.nextInt(monumentNameArray.length);
       String chosenMonumentName=monumentNameArray[num];

        while (usedMonuments.contains(chosenMonumentName))
        {
            num=random.nextInt(monumentNameArray.length);
            chosenMonumentName=monumentNameArray[num];
        }

            usedMonuments.add(chosenMonumentName); // Add it to the used list
            monumentNameTextView.setText(monumentNameArray[num]); // Update the monument name
            imageLoading(num); // Load the correct image
            selectRect(num); // Create the correct rectangle to check collision

    }

    // Load the correct image according to the random number
    private void imageLoading(int number)
    {
        switch (number){
            case 0: monumentImageView.setImageResource(R.drawable.monument_aggelokastro);
                break;
            case 1: monumentImageView.setImageResource(R.drawable.monument_amfipoli);
                break;
            case 2: monumentImageView.setImageResource(R.drawable.monument_servia);
                break;
            case 3: monumentImageView.setImageResource(R.drawable.monument_filippoi);
                break;
            case 4: monumentImageView.setImageResource(R.drawable.monument_moglena);
                break;
            case 5: monumentImageView.setImageResource(R.drawable.monument_serres);
                break;
            case 6: monumentImageView.setImageResource(R.drawable.monument_platamonas);
                break;
            case 7: monumentImageView.setImageResource(R.drawable.monument_anaktoroupoli);
                break;
            case 8: monumentImageView.setImageResource(R.drawable.monument_thessaloniki);
                break;
            case 9: monumentImageView.setImageResource(R.drawable.monument_anastasioupoli);
                break;
            case 10: monumentImageView.setImageResource(R.drawable.monument_gynaikokastro);
                break;
            case 11: monumentImageView.setImageResource(R.drawable.monument_koumoutzina);
                break;
            case 12: monumentImageView.setImageResource(R.drawable.monument_sidirokastro);
                break;
            case 13: monumentImageView.setImageResource(R.drawable.monument_gratini);
                break;
            case 14: monumentImageView.setImageResource(R.drawable.monument_rentina);
                break;
            case 15: monumentImageView.setImageResource(R.drawable.monument_didimoteixo);
                break;
            case 16: monumentImageView.setImageResource(R.drawable.monument_pithio);
                break;
            default: break;
        }
    }

    // Method to draw the circle where the player touches the screen
    private void createBitMap(float pointX, float pointY) {

        ImageView imageView = (ImageView) findViewById(R.id.greeceMap);
        // Creates bitmap, width and height in accordance with the image view in order to have a smooth motion of the circle
        Bitmap bitMap = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888);
        bitMap = bitMap.copy(bitMap.getConfig(), true);     //lets bmp to be mutable
        Canvas canvas = new Canvas(bitMap);                 //draw a canvas in defined bmp

        Paint paint = new Paint();//define paint and paint color

        // Create the rectangle around the circle
        touchRect=new Rect((int)pointX-15,(int) pointY-15, (int)pointX+15, (int)pointY+15);

        // If the rectangle collides with the monument's location rectangle add it to the correct answers list
        // Else add it to the wrong answers list
        // Use Points to avoid create a Circle class, Points is a built in Java class
        if(touchRect.intersect(selectedRect)) {
            paint.setColor(Color.GREEN);
            correctPoints.add(new Point((int)pointX,(int)pointY));
        }
        else {
            paint.setColor(Color.RED);
            wrongPoints.add(new Point((int)pointX,(int)pointY));
        }
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);                           //smooth edges

        imageView.setImageBitmap(bitMap);
        // Set the map on the background and draw the circle above
        imageView.setBackgroundResource(R.drawable.church_map);
        // Use cords of touch to draw the circle
        canvas.drawCircle(pointX, pointY, 30, paint);

        // If the lists are not empty, draw all the red circles, then all the green
        if(!wrongPoints.isEmpty()) {
            paint.setColor(Color.RED);
            for (Point p : wrongPoints) {
                canvas.drawCircle(p.x,p.y,30,paint);
            }
        }

        if(!correctPoints.isEmpty()) {
            paint.setColor(Color.GREEN);
            for (Point p : correctPoints) {
                canvas.drawCircle(p.x,p.y,30,paint);
            }
        }

        // Update the view
        imageView.invalidate();

        // As long as there are more monuments to show built the next question, else finish the game
        if(usedMonuments.size()<monumentNameArray.length) {
            setMonument();
        }
        else
        {

            //Show Score
            Intent itn= new Intent(getApplicationContext(), Score.class);
            itn.putExtra("appNum",100);
            startActivity(itn);

            QrCodeScanner.questionMode=true;
            finish();
            //TODO: calculate score
        }


    }

    // Create monuments' rectangles approximately
    private void populateMonumentRectangles()
    {
        int widthFragment=(2*(screenWidth/3))/10; // take 2/3 of screen width because the map uses the 2/3 or table layout
        int heightFragment=screenHeight/10;

        //Aggelokastro
        monumentsRects[0]=new Rect(0,8*heightFragment,widthFragment/2,(8*heightFragment)+(heightFragment/2));
        //Amfipoli
        monumentsRects[1]=new Rect((6*widthFragment)+(widthFragment/2),(3*heightFragment)+(heightFragment/2),7*widthFragment,(4*heightFragment));
        //Servia
        monumentsRects[2]=new Rect(3*widthFragment,6*heightFragment,(3*widthFragment)+(widthFragment/2),(6*heightFragment)+(heightFragment/2));
        //Filippoi
        monumentsRects[3]=new Rect(7*widthFragment,(2*heightFragment),(7*widthFragment)+(widthFragment/2),(2*heightFragment)+(heightFragment/2));
        //Moglena
        monumentsRects[4]=new Rect(3*widthFragment,(3*heightFragment)+(heightFragment/2),(3*widthFragment)+(widthFragment/2),4*heightFragment);
        //Serres
        monumentsRects[5]=new Rect((6*widthFragment)+(widthFragment/2),3*(heightFragment/2),7*widthFragment,2*heightFragment);
        //Platamonas
        monumentsRects[6]=new Rect((4*widthFragment)+(widthFragment/2),6*heightFragment,5*widthFragment,(6*heightFragment)+(heightFragment/2));
        //Anaktoroupoli
        monumentsRects[7]=new Rect(7*widthFragment,3*heightFragment,(7*widthFragment)+(widthFragment/2),(3*heightFragment)+(heightFragment/2));
        //Thessaloniki
        monumentsRects[8]=new Rect((4*widthFragment)+(widthFragment/2),4*heightFragment,5*widthFragment,(4*heightFragment)+(heightFragment/2));
        //Anastasioupoli
        monumentsRects[9]=new Rect((7*widthFragment)+(widthFragment/2),3*(heightFragment/2),8*widthFragment,2*heightFragment);
        //Gynaikokastro
        monumentsRects[10]=new Rect(5*widthFragment,3*heightFragment,(5*widthFragment)+(widthFragment/2),(3*heightFragment)+(heightFragment/2));
        //Koumoutzina
        monumentsRects[11]=new Rect(8*widthFragment,2*heightFragment,(8*widthFragment)+(widthFragment/2),(2*heightFragment)+(heightFragment/2));
        //Sidirokastro
        monumentsRects[12]=new Rect(5*widthFragment,3*(heightFragment/2),(5*widthFragment)+(widthFragment/2),2*heightFragment);
        //Gratini
        monumentsRects[13]=new Rect((8*widthFragment)+(widthFragment/2),3*(heightFragment/2),9*widthFragment,2*heightFragment);
        //Rentina
        monumentsRects[14]=new Rect((5*widthFragment)+(widthFragment/2),(4*heightFragment)+(heightFragment/2),6*widthFragment,5*heightFragment);
        //Didimoteixo
        monumentsRects[15]=new Rect((9*widthFragment)+(widthFragment/2),3*(heightFragment/2),11*widthFragment,2*heightFragment);
        //Pithio
        monumentsRects[16]=new Rect((9*widthFragment)+(widthFragment/2),heightFragment,11*widthFragment,3*(heightFragment/2));
}

    // Select the rectangle to interact
    private void selectRect(int number)
    {
        selectedRect=monumentsRects[number];
    }

}
