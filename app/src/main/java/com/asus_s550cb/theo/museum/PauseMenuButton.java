package com.asus_s550cb.theo.museum;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

/**
 * Created by Nasia on 6/9/2015.
 */
public class PauseMenuButton {

    Rect menuRect;
    Drawable menuBt;
    int width;

    public PauseMenuButton(int width,Activity activity)
    {
        menuRect=new Rect((width-(width/15)),0,width,width/15);
        menuBt= ContextCompat.getDrawable(activity,R.drawable.menu_icon);
        menuBt.setBounds(menuRect);

    }

    public Drawable getPauseMenuButton()
    {
        return menuBt;
    }

    public Rect getRect()
    {
        return menuRect;
    }




}
