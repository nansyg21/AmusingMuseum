package com.Anaptixis.AmusingMuseum;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

/**
 * Created by Nasia on 6/9/2015.
 */
public class PauseMenuButton {

    Rect menuRect;
    Drawable menuBt;
    int width;

    //Create a button for menu within a rectangle
    public PauseMenuButton(int width,Activity activity)
    {
        menuRect=new Rect((width-(width/15)),0,width,width/15);
        menuBt= ContextCompat.getDrawable(activity,R.drawable.menu_icon);
        menuBt.setBounds(menuRect);

    }

    // Get drawable to add it to the draw method
    public Drawable getPauseMenuButton()
    {
        return menuBt;
    }

    // Get rectangle to check the touch on screen
    public Rect getRect()
    {
        return menuRect;
    }
}
