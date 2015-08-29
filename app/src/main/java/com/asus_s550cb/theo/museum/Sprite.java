package com.asus_s550cb.theo.museum;

/**
 * Created by theo on 3/8/2015.
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.widget.Toast;

import java.util.logging.XMLFormatter;

import static android.support.v4.app.ActivityCompat.startActivity;

/**
 * Created by theo on 9/7/2015.
 */

public class Sprite {

    //ARRAY int for which includes the critical pixel for navigation
    //{ x %, y %, direction}
    public static final float[] coordinates = {41,78,1,
                                             41,61,2,
                                             47,61,1,
                                             43,46,1,
                                             43,40,2,
                                             47,40,2,/*room 1*/
                                             44,21,3,
                                             35,21,1,
                                             35,16,1/*room 2*/};


    int i;//Step Counter for navigation

    int x,y,newInt,stage;//x,y coordinates , newInt Flag for new Activity (when new room is reached)
    int penguinStep;//Number of pixels for the animation step
    int xSpeed,ySpeed;
    int height,width;
    Bitmap b;
    StartGame.Ourview ov;
    int currentFrame=0;
    int direction;
    int widthScreen,heightScreen;
    boolean thereYet=false;



    public Sprite(StartGame.Ourview ourview, Bitmap peng,int widthScreen,int heightScreen,int stage) {
        b=peng;
        ov=ourview;
        this.stage=stage;
        this.widthScreen=widthScreen;
        this.heightScreen=heightScreen;
        height=b.getHeight()/4; //Because my sheet has 4 rows
        width=b.getWidth()/3;     //Because my sheet has 3 columns
        penguinStep=widthScreen/215;

        //direction
        // 0=down
        // 1=up
        // 2=right
        // 3=left
        initializeSpriteStartingPosition();

    }

    public int onDraw(Canvas canvas) {
        newInt= update();
        int srcY = direction * height;
        int srcX = currentFrame * width;
        Rect src = new Rect(srcX,srcY,srcX+width,srcY+height);
        Rect dst = new Rect(x,y,x+width,y+height);
        canvas.drawBitmap(b, src, dst, null);

        return newInt;
    }

    private int update() {

        //Road to ROOM 1
        if(stage==1){
           if(goRoom_1())//if room1 reached... then return 1
               return 1;}


        //Road to ROOM 2
       else  if (stage==2){
               if(goRoom_2())
                 return 2;}

        //Road to ROOM 3
       else  if (stage==3){
                   if(goRoom_3())
                       return 3;}

        //Road to ROOM 4
        else  if (stage==4){
            if(goRoom_4())
                return 4;}

        //Road to ROOM 5
        else  if (stage==5){
            if(goRoom_5())
                return 5;}

        //Road to ROOM 6
        else  if (stage==6){
            if(goRoom_6())
                return 6;}

        try {
            Thread.sleep(75);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        currentFrame = ++currentFrame % 3;
        x+=xSpeed;
        y+=ySpeed;

        return 0;
    }

    public void initializeSpriteStartingPosition(){
        //Starting Point
        if(stage==1){
            x= (int) Math.ceil(widthScreen*0.40);
            y= (int) Math.ceil(heightScreen*0.78);
            direction=1;
            xSpeed=0;
            ySpeed=-penguinStep;
        }if(stage==2){
            x= (int) Math.ceil(widthScreen*0.48);
            y= (int) Math.ceil(heightScreen*0.175);
            direction=3;
            xSpeed=-penguinStep;
            ySpeed=0;
        }
        else if(stage==3){
            x= (int) Math.ceil(widthScreen*0.34);
            y= (int) Math.ceil(heightScreen*0.03);
            direction=0;
            xSpeed=0;
            ySpeed=penguinStep;
        }
        else if(stage==4){
            x= (int) Math.ceil(widthScreen*0.255);
            y= (int) Math.ceil(heightScreen*0.25);
            direction=1;//up
            xSpeed=0;
            ySpeed=-penguinStep;
        }
        else if(stage==5){
            x= (int) Math.ceil(widthScreen*0.255);
            y= (int) Math.ceil(heightScreen*0.25);
            direction=1;
            xSpeed=0;
            ySpeed=-penguinStep;
        }
        else if(stage==6){
            x= (int) Math.ceil(widthScreen*0.255);
            y= (int) Math.ceil(heightScreen*0.25);
            direction=1;
            xSpeed=0;
            ySpeed=-penguinStep;
        }
        else if(stage==7){
            x= (int) Math.ceil(widthScreen*0.255);
            y= (int) Math.ceil(heightScreen*0.25);
            direction=1;
            xSpeed=0;
            ySpeed=-penguinStep;
        }
        else if(stage==8){
            x= (int) Math.ceil(widthScreen*0.255);
            y= (int) Math.ceil(heightScreen*0.25);
            direction=1;
            xSpeed=0;
            ySpeed=-penguinStep;
        }
    }

    public boolean goRoom_1(){

        if ( i == 0 && y < (int) Math.ceil(heightScreen * 0.56) ) {
            //Go to next Coordinate
            xSpeed = penguinStep;
            ySpeed = 0;
            direction = 2;// right
            i++;
        }
        else if (i == 1 && x > (int) Math.ceil(widthScreen * 0.46)) {
            //Go to next Coordinate
            xSpeed = 0;
            ySpeed = -penguinStep;
            direction = 1;//up
            i++;
        } else if (i == 2 && y < (int) Math.ceil(heightScreen * 0.415)) {
            //Go to next Coordinate
            xSpeed = -penguinStep;
            ySpeed = 0;
            direction = 3;//left
            i++;
        } else if (i == 3 && x < (int) Math.ceil(widthScreen * 0.43)) {
            //Go to next Coordinate
            xSpeed = 0;
            ySpeed = -penguinStep;
            direction = 1;//up
            i++;
        } else if (i == 4 && y < (int) Math.ceil(heightScreen * 0.37)) {
            //Go to next Coordinate
            xSpeed = penguinStep;
            ySpeed = 0;
            direction = 2;//right
            i++;
        } else if (i == 5 && x < (int) Math.ceil(widthScreen * 0.47)) {
            i=0;
            setThereYet(true);
            return true;
        }

        //is Room 1 reached???
        return false;
    }
    public boolean goRoom_2() {
        setThereYet(false);

        if (x < (int) Math.ceil(widthScreen * 0.345) && i == 0) {
            //Go to next Coordinate
            xSpeed = 0;
            ySpeed = -penguinStep;
            direction = 1;// up
            i++;
        }
        else if (i==1 && y < (int) Math.ceil(heightScreen * 0.12)){
            i=0;
            setThereYet(true);
            return true;
        }
        //is Room 2 reached??
        return false;
    }
    public boolean goRoom_3() {

        setThereYet(false);
        if (y > (int) Math.ceil(heightScreen * 0.16) && i == 0) {
            //Go to next Coordinate
            xSpeed = -penguinStep;
            ySpeed = 0;
            direction = 3;// left
            i++;
        } else if (i == 1 && x < (int) Math.ceil(widthScreen * 0.255)) {
            //Go to next Coordinate
            xSpeed = 0;
            ySpeed = penguinStep;
            direction = 0;//down
            i++;
        }else if (i==2 && y > (int) Math.ceil(heightScreen * 0.2)){
            i=0;
            setThereYet(true);
            return true;
        }
        //is Room 3 reached??
        return false;
    }
    public boolean goRoom_4() {


        if (y < (int) Math.ceil(heightScreen * 0.17) && i == 0) {
            //Go to next Coordinate
            xSpeed = -penguinStep;
            ySpeed = 0;
            direction = 3;// left
            i++;
        } else if (i == 1 && x < (int) Math.ceil(widthScreen * 0.185)) {
            //Go to next Coordinate
            xSpeed = 0;
            ySpeed = penguinStep;
            direction = 0;//down
            i++;
        }else if (i == 2 && y > (int) Math.ceil(heightScreen * 0.53)) {
            //Go to next Coordinate
            xSpeed = -penguinStep;
            ySpeed = 0;
            direction = 3;//left
            i++;
        }else if (i == 3 && x < (int) Math.ceil(widthScreen * 0.15)) {
            //Room 3 reached
            i=0;
            return true;
        }
        //is Room 3 reached??
        return false;
    }
    public boolean goRoom_5() {


        if (y > (int) Math.ceil(heightScreen * 0.20) && i == 0) {
            //Go to next Coordinate
            xSpeed = -penguinStep;
            ySpeed = 0;
            direction = 3;// left
            i++;
        } else if (i == 1 && x < (int) Math.ceil(widthScreen * 0.2)) {
            //Go to next Coordinate
            xSpeed = 0;
            ySpeed = penguinStep;
            direction = 0;//down
            i++;
        }
        //is Room 4 reached??
        return false;
    }
    public boolean goRoom_6() {


        if (y > (int) Math.ceil(heightScreen * 0.20) && i == 0) {
            //Go to next Coordinate
            xSpeed = -penguinStep;
            ySpeed = 0;
            direction = 3;// left
            i++;
        } else if (i == 1 && x < (int) Math.ceil(widthScreen * 0.2)) {
            //Go to next Coordinate
            xSpeed = 0;
            ySpeed = penguinStep;
            direction = 0;//down
            i++;
        }
        //is Room 5 reached??
        return false;
    }
    public boolean goRoom_7() {


        if (y > (int) Math.ceil(heightScreen * 0.20) && i == 0) {
            //Go to next Coordinate
            xSpeed = -penguinStep;
            ySpeed = 0;
            direction = 3;// left
            i++;
        } else if (i == 1 && x < (int) Math.ceil(widthScreen * 0.2)) {
            //Go to next Coordinate
            xSpeed = 0;
            ySpeed = penguinStep;
            direction = 0;//down
            i++;
        }
        //is Room 6 reached??
        return false;
    }


public void setThereYet(boolean thereYet)
{
    this.thereYet=thereYet;
}

public boolean getThereYet()
{
    return thereYet;
}





}

