package com.Anaptixis.AmusingMuseum;


import android.graphics.Bitmap;
import android.graphics.Rect;

public class PuzzlePiece {

    Bitmap img;
    public Rect rect;
    public Rect originalRec;
    public boolean IsSelected = false;
    public int ID;
    public boolean placedCorrectly=false;

    public PuzzlePiece(Bitmap i, Rect rec, Rect original , int id)
    {
        img = i;
        rect = rec;
        originalRec = original;
        ID=id;
    }

}
