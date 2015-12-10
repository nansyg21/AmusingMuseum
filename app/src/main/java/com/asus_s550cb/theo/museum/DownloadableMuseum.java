package com.asus_s550cb.theo.museum;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by panos on 8/11/2015.
 */
public class DownloadableMuseum implements java.io.Serializable {       //HOLDS DOWNLOADED DATA

    public static String LOCALLY_SAVED_MUSEUM_PREFERENCE_KEY = "LOCALLY_SAVED_MUSEUM_KEY"; //static: one museum at a time -- use these to save data locally

    public   String username ;
    public   String museum_name;
    public   String museum_name_gr;

    public   String logo ;
    public   BitmapDataObject logoBmp ;
    public   String floor_plan ;
    public   BitmapDataObject floor_planBmp ;
    public   int number_of_rooms ;
    public   int m_id ;

    public ArrayList<RoomsForNewMuseum> RoomsList;

    public DownloadableMuseum() {
        logoBmp = new BitmapDataObject();
        floor_planBmp= new BitmapDataObject();
    }

    public Bitmap FromByteArrayToBitmap(byte[] array)
    {
        return BitmapFactory.decodeByteArray(array, 0, array.length);
    }

    public void SaveLogoAsSerializable(Bitmap b)    //From Bitmap -> Stream -> byte[]
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, stream);
        logoBmp.imageByteArray = stream.toByteArray();
    }

    public void SaveFloorPlanAsSerializable(Bitmap b)    //From Bitmap -> Stream -> byte[]
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, stream);
        floor_planBmp.imageByteArray = stream.toByteArray();
    }

    public static class RoomsForNewMuseum implements java.io.Serializable {//HOLDS DATA EACH ROOM HAS
        String MiniGame;         //mini game to load in each room: info coming from server
        int r_id;
        String room_name;
        String room_name_gr;
        int e_id;
        String e_text;
        String e_text_gr;
        int a1_1_id;
        String a1_1_text;
        String a1_1_text_gr;
        int a1_2_id;
        String a1_2_text;
        String a1_2_text_gr;
        int a1_3_id;
        String a1_3_text;
        String a1_3_text_gr;
        int a1_4_id;
        String a1_4_text;
        String a1_4_text_gr;


        int e2_id;
        String e2_text;
        String e2_text_gr;
        int a2_1_id;
        String a2_1_text;
        String a2_1_text_gr;
        int a2_2_id;
        String a2_2_text;
        String a2_2_text_gr;
        int a2_3_id;
        String a2_3_text;
        String a2_3_text_gr;
        int a2_4_id;
        String a2_4_text;
        String a2_4_text_gr;

        int e3_id;
        String e3_text;
        String e3_text_gr;
        int a3_1_id;
        String a3_1_text;
        String a3_1_text_gr;
        int a3_2_id;
        String a3_2_text;
        String a3_2_text_gr;
        int a3_3_id;
        String a3_3_text;
        String a3_3_text_gr;
        int a3_4_id;
        String a3_4_text;
        String a3_4_text_gr;

        int correct1_id;
        String correct1_text;
        String correct1_text_gr;
        int correct2_id;
        String correct2_text;
        String correct2_text_gr;
        int correct3_id;
        String correct3_text;
        String correct3_text_gr;

        String hint1;
        String hint1_gr;
        String hint2;
        String hint2_gr;
        String hintImg1;
        String hintImg2;

        BitmapDataObject hintImgBmp1= new BitmapDataObject();
        BitmapDataObject hintImgBmp2= new BitmapDataObject();

        public RoomsForNewMuseum(){
        }

        public void SaveHintImage1AsSerializable(Bitmap b)    //From Bitmap -> Stream -> byte[]
        {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.PNG, 100, stream);
            hintImgBmp1.imageByteArray = stream.toByteArray();
        }

        public void SaveHintImage2AsSerializable(Bitmap b)    //From Bitmap -> Stream -> byte[]
        {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.PNG, 100, stream);
            hintImgBmp2.imageByteArray = stream.toByteArray();
        }
    }

    protected static class BitmapDataObject implements java.io.Serializable {//Problem: Cant Serialize Bitmap so we use byte[]
        public byte[] imageByteArray;   //Use BitmapFactory.decodeByteArray(ByteArray, 0, ByteArray.length) to get Bitmap
        public BitmapDataObject(){

        }
    }
}