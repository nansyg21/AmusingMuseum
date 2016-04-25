package com.Anaptixis.AmusingMuseum;

/*Created by nansyg21*/

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class connect_wires extends Activity {

    private static final boolean AUTO_HIDE = true;
    private int rotationDegrees[][]; // array to keep track of the rotation of each piece - used to calculate next rotation
    //Levels setup
    private int level1[]={2,0,0,0,0,2,0,0,0,0,3,1,1,5,0,0,0,0,2,0,0,0,0,2,0};
    private int level2[]={2,0,4,1,5,2,0,2,0,2,2,0,2,0,2,2,0,2,0,2,3,1,6,0,2};
    private int level3[]={2,0,4,1,5,2,0,2,0,2,2,0,2,0,2,3,1,7,1,6,0,0,2,0,0};
    private int level4[]={2,4,5,0,0,3,7,6,4,1,0,2,0,2,0,4,7,1,7,5,3,6,0,3,6};

    private final int TILENUM=5; // How many pieces of wires we have horizontally or vertically
    // 0 --> empty tile
    // 1 --> horizontal tile
    // 2 --> vertical tile
    // 3 --> right-up corner tile
    // 4 --> right-down corner tile
    // 5 --> left-down corner tile
    // 6 --> left-up corner tile
    // 7 --> cross tile
    private ImageView[][] wires; //Array for the imageviews of the wires
    private int tilesTypes[][]; //Array to know what kind of wire we have in order to check the solution with the rotation degree table values
    //Grid cords for the wire clicked
    private int currentX;
    private int currentY;

    //Use to update clicks numbers
    private TextView clicks;
    private String clicksString;
    private int numOfClicks;

    private int currentLevel=1; // The current level
    private ImageView mainImage; // The imageView with the picture of the Saint



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_connect_wires);

        menu.hideNavBar(this.getWindow());
        numOfClicks=0;
        clicksString=getResources().getString(R.string.num_clicks);
        clicks=(TextView)findViewById(R.id.numOfClicks);
        clicks.setText(clicksString+" "+numOfClicks);

        rotationDegrees=new int[TILENUM][TILENUM];
        tilesTypes=new int[TILENUM][TILENUM];
        wires=new ImageView[TILENUM][TILENUM];
        mainImage=(ImageView)findViewById(R.id.imageToLight);
        getImageViews(); // Set proper images

        resetRotation(); // Set all rotation to zero
        assignImage(currentLevel); // Set image of Saint according to level
        setTableau(currentLevel);

        Intent itn= new Intent(getApplicationContext(), HelpDialogActivity.class);
        itn.putExtra("appNum", 10);
        startActivity(itn);

        QrCodeScanner.questionMode=true;
    }

    // Reset the flags to hide the navigation bar
    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);

        //  PauseMenuActivity.pause=false;
        menu.hideNavBar(this.getWindow());

    }

    // Call pause menu
    public void menuBtClick(View v)
    {
        Intent itn;
        itn = new Intent(getApplicationContext(), PauseMenuActivity.class);
        startActivity(itn);
    }

    protected void onPause()
    {
        super.onPause();

    }

    @Override
    public void onBackPressed() {

        new android.app.AlertDialog.Builder(this)
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

    // Get all imageView instances and store them to an array so we can refer to them
    public void getImageViews()
    {
        wires[0][0]=(ImageView)findViewById(R.id.wire0);
        wires[0][1]=(ImageView)findViewById(R.id.wire1);
        wires[0][2]=(ImageView)findViewById(R.id.wire2);
        wires[0][3]=(ImageView)findViewById(R.id.wire3);
        wires[0][4]=(ImageView)findViewById(R.id.wire4);
        wires[1][0]=(ImageView)findViewById(R.id.wire5);
        wires[1][1]=(ImageView)findViewById(R.id.wire6);
        wires[1][2]=(ImageView)findViewById(R.id.wire7);
        wires[1][3]=(ImageView)findViewById(R.id.wire8);
        wires[1][4]=(ImageView)findViewById(R.id.wire9);
        wires[2][0]=(ImageView)findViewById(R.id.wire10);
        wires[2][1]=(ImageView)findViewById(R.id.wire11);
        wires[2][2]=(ImageView)findViewById(R.id.wire12);
        wires[2][3]=(ImageView)findViewById(R.id.wire13);
        wires[2][4]=(ImageView)findViewById(R.id.wire14);
        wires[3][0]=(ImageView)findViewById(R.id.wire15);
        wires[3][1]=(ImageView)findViewById(R.id.wire16);
        wires[3][2]=(ImageView)findViewById(R.id.wire17);
        wires[3][3]=(ImageView)findViewById(R.id.wire18);
        wires[3][4]=(ImageView)findViewById(R.id.wire19);
        wires[4][0]=(ImageView)findViewById(R.id.wire20);
        wires[4][1]=(ImageView)findViewById(R.id.wire21);
        wires[4][2]=(ImageView)findViewById(R.id.wire22);
        wires[4][3]=(ImageView)findViewById(R.id.wire23);
        wires[4][4]=(ImageView)findViewById(R.id.wire24);
    }

    // Set all rotation degrees to zero
    public void resetRotation()
    {
        for(int i=0;i<TILENUM;i++)
        {
            for(int j=0;j<TILENUM;j++)
            {
                rotationDegrees[i][j]=0;
            }
        }
    }


    // Set the level
    public void setTableau(int level)
    {
        int lvl=level;
        int levelToLoad[];

        // Get the level array to load according to the level number
        if(lvl==1)
        {
            levelToLoad=level1;
        }
        else if(lvl==2)
        {
            levelToLoad=level2;
        }
        else if(lvl==3)
        {
            levelToLoad=level3;
        }
        else
        {
            levelToLoad=level4;
        }
        //Convert from 1D array to 2D with accordance with the wire imageView array
        int counter=0;
        for(int i=0;i<TILENUM;i++)
        {
            for(int j=0;j<TILENUM;j++)
            {
                tilesTypes[i][j]=levelToLoad[counter];

                // Check the type of the wire in each cell and rotate randomly
                if(levelToLoad[counter]==1)
                {
                    wires[i][j].setImageResource(R.drawable.wire_horizontal);
                    lineRotation(wires[i][j],i,j);
                }
                else if (levelToLoad[counter]==2)
                {
                    wires[i][j].setImageResource(R.drawable.wire_vertical);
                    lineRotation(wires[i][j],i,j);
                }
                else if (levelToLoad[counter]==3)
                {
                    wires[i][j].setImageResource(R.drawable.wire_right_up);
                    ancleRotation(wires[i][j],i,j);
                }
                else if (levelToLoad[counter]==4)
                {
                    wires[i][j].setImageResource(R.drawable.wire_right_down);
                    ancleRotation(wires[i][j], i, j);
                }
                else if (levelToLoad[counter]==5)
                {
                    wires[i][j].setImageResource(R.drawable.wire_left_down);
                    ancleRotation(wires[i][j], i, j);
                }
                else if (levelToLoad[counter]==6)
                {
                    wires[i][j].setImageResource(R.drawable.wire_left_up);
                    ancleRotation(wires[i][j], i, j);
                }
                else if (levelToLoad[counter]==7)
                {
                    wires[i][j].setImageResource(R.drawable.wire_cross);
                }
                else
                {
                    wires[i][j].setImageResource(R.drawable.plain);
                }
                //tilesTypes[i][j]=levelToLoad[counter];
                counter++;
            }
        }
    }

    // For horizontal or vertical wires 50% possibility to rotate 90 degrees
    public void lineRotation(ImageView imgview, int i, int j)
    {
        Random r=new Random(System.currentTimeMillis());
        int n=r.nextInt(10);
        Log.d("NUMBER", "Number "+n);
        if(n<5)
        {
            final RotateAnimation rotateAnim = new RotateAnimation(0.0f, 90,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);

            rotateAnim.setDuration(0);
            rotateAnim.setFillAfter(true);
            imgview.startAnimation(rotateAnim);

            rotationDegrees[i][j]=90;
        }
    }

    //For angles 25% possibility to rotate 90,180 or 270 degrees
    public void ancleRotation(ImageView imgview, int i, int j)
    {
        Random r=new Random(System.currentTimeMillis());
        int n=r.nextInt(40);
        Log.d("NUMBER", "Number "+n);
        if(n<10)
        {
            final RotateAnimation rotateAnim = new RotateAnimation(0.0f, 90,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);

            rotateAnim.setDuration(0);
            rotateAnim.setFillAfter(true);
            imgview.startAnimation(rotateAnim);

            rotationDegrees[i][j]=90;

        }
        else if(n<20)
        {
            final RotateAnimation rotateAnim = new RotateAnimation(0.0f, 180,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);

            rotateAnim.setDuration(0);
            rotateAnim.setFillAfter(true);
            imgview.startAnimation(rotateAnim);

            rotationDegrees[i][j]=180;

        }
        if(n<30)
        {
            final RotateAnimation rotateAnim = new RotateAnimation(0.0f, 270,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);

            rotateAnim.setDuration(0);
            rotateAnim.setFillAfter(true);
            imgview.startAnimation(rotateAnim);

            rotationDegrees[i][j]=270;

        }

    }

    // Check solution
    // Lines are correct if they have 0 or 180 degrees rotation
    // Angles are only correct with 0 rotation
    // Cross is always correct
    public boolean checkSolution()
    {
        for(int i=0;i<TILENUM;i++)
        {
            for(int j=0;j<TILENUM;j++)
            {
                if((tilesTypes[i][j]==1)||(tilesTypes[i][j]==2))
                {
                    if((rotationDegrees[i][j]!=0)&&(rotationDegrees[i][j]!=180))
                    {
                        Log.d("ROTATION", "Rotation degree "+ rotationDegrees[i][j]+" at "+i+" "+j);
                        return false;
                    }
                }
                else if((tilesTypes[i][j]==3)||(tilesTypes[i][j]==4)||(tilesTypes[i][j]==5)||(tilesTypes[i][j]==6))
                {
                    if(rotationDegrees[i][j]!=0)
                    {
                        Log.d("ROTATION", "Rotation degree "+ rotationDegrees[i][j]+" at "+i+" "+j);
                        return false;
                    }
                }
                else
                {
                    continue;
                }
            }
        }
        // If current level is 4 aka last, wait for a sec and then finish
        // Else take us to the next level
        if(currentLevel==4)
        {
            assignImage(currentLevel + 1);

            Handler handler = new Handler();

            handler.postDelayed(new Runnable() {
                public void run() {

                    if(numOfClicks<45)
                        Score.currentRiddleScore= 70 ;
                    else  if(numOfClicks<55)
                        Score.currentRiddleScore= 50 ;
                    else
                        Score.currentRiddleScore= 30 ;
                    Intent itn= new Intent(getApplicationContext(), Score.class);
                    itn.putExtra("nextStage", 10);
                    startActivity(itn);
                    finish();
                }
            }, 1000);
        }
        else
        {
            currentLevel++;
            resetRotation();
            assignImage(currentLevel);
            setTableau(currentLevel);
        }

        Log.d("OK","It is done");
        return true;
    }

    // Set the image of the Saint according to the level number
    public void assignImage(int level)
    {
        if(level==1)
        {
            mainImage.setImageResource(R.drawable.wires_lumocity_0);
        }
        else if(level==2)
        {
            mainImage.setImageResource(R.drawable.wires_lumocity_1);
        }
        else if(level==3)
        {
            mainImage.setImageResource(R.drawable.wires_lumocity_2);
        }
        else if(level==4)
        {
            mainImage.setImageResource(R.drawable.wires_lumocity_3);
        }
        else
        {
            mainImage.setImageResource(R.drawable.wires_lumocity_4);
        }

    }

    //On touch of a wire rotate it
    public void onTouch(View v)
    {
        ImageView imgview=(ImageView)v;

        checkWhichWire(v);

        if(tilesTypes[currentX][currentY]>0)
        {
            numOfClicks++;
            clicks.setText(clicksString+" "+numOfClicks);
        }


        float startRotation=(float)rotationDegrees[currentX][currentY]; // get the last position

        float endRotation;

        endRotation=startRotation+90; //End rotation after rotating for 90 degrees from the last position


        // Practice rotation
        final RotateAnimation rotateAnim = new RotateAnimation(startRotation, endRotation,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);

        rotateAnim.setDuration(0); //Rotate immediately
        rotateAnim.setFillAfter(true);
        imgview.startAnimation(rotateAnim); //Update imageview with the new rotated image

        if(endRotation>=360)
        {
            endRotation=0; // If a full circle was made set 360 degrees to 0 for correct calculations
        }

        rotationDegrees[currentX][currentY]=(int)endRotation; //Update rotation table

        checkSolution();

    }

    //Check witch wire was touched and get X and Y of the grid
    public void checkWhichWire(View v)
    {
        ImageView imgv=(ImageView)v;
        switch (v.getId()) {

            case R.id.wire0:
                currentX=0;
                currentY=0;
                break;
            case R.id.wire1:
                currentX=0;
                currentY=1;
                break;
            case R.id.wire2:
                currentX=0;
                currentY=2;
                break;
            case R.id.wire3:
                currentX=0;
                currentY=3;
                break;
            case R.id.wire4:
                currentX=0;
                currentY=4;
                break;
            case R.id.wire5:
                currentX=1;
                currentY=0;
                break;
            case R.id.wire6:
                currentX=1;
                currentY=1;
                break;
            case R.id.wire7:
                currentX=1;
                currentY=2;
                break;
            case R.id.wire8:
                currentX=1;
                currentY=3;
                break;
            case R.id.wire9:
                currentX=1;
                currentY=4;
                break;
            case R.id.wire10:
                currentX=2;
                currentY=0;
                break;
            case R.id.wire11:
                currentX=2;
                currentY=1;
                break;
            case R.id.wire12:
                currentX=2;
                currentY=2;
                break;
            case R.id.wire13:
                currentX=2;
                currentY=3;
                break;
            case R.id.wire14:
                currentX=2;
                currentY=4;
                break;
            case R.id.wire15:
                currentX=3;
                currentY=0;
                break;
            case R.id.wire16:
                currentX=3;
                currentY=1;
                break;
            case R.id.wire17:
                currentX=3;
                currentY=2;
                break;
            case R.id.wire18:
                currentX=3;
                currentY=3;
                break;
            case R.id.wire19:
                currentX=3;
                currentY=4;
                break;
            case R.id.wire20:
                currentX=4;
                currentY=0;
                break;
            case R.id.wire21:
                currentX=4;
                currentY=1;
                break;
            case R.id.wire22:
                currentX=4;
                currentY=2;
                break;
            case R.id.wire23:
                currentX=4;
                currentY=3;
                break;
            case R.id.wire24:
                currentX=4;
                currentY=4;
                break;

        }
    }


}
