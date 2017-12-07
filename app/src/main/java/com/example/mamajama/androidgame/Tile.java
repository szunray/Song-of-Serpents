package com.example.mamajama.androidgame;

import android.graphics.Bitmap;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;

/**
 * Created by Savaque on 12/2/2017.
 */

public class Tile {

    int posX, posY, centX, centY, type;
    Bitmap tileType[];
    Bitmap bitmap;
   /* Boolean isOccupied=false;
    Pawn pawn;*/


    public Tile(Context context, int Xpos, int Ypos){
        posX=Xpos;
        posY=Ypos;
        type=1;
        tileType=new Bitmap[2];
        Resources resources = context.getResources();
        String nameOfImage = "grasscenterblock";
        int resId = context.getResources().getIdentifier(nameOfImage, "drawable", context.getPackageName());
        tileType[0]=BitmapFactory.decodeResource(resources,resId);
        nameOfImage = "goldblock";
        resId = context.getResources().getIdentifier(nameOfImage, "drawable", context.getPackageName());
        tileType[1]=BitmapFactory.decodeResource(resources,resId);
        for(int x=0; x<5; x++){

            if (type==1){
                bitmap=tileType[0];
            }
            else if (type==2){
                bitmap=tileType[1];
            }
        }
    }
    // Theres probably a way better way to do all of this.
    public void setType(int Type){
        type=Type;
        bitmap=tileType[type];

    }

    public void reset(){
        type=0;
        bitmap=tileType[type];

    }
    /*
    public void setIsOccupied(Pawn thing){
        pawn=thing;
        isOccupied=true;
    }
    public void Vacate(){
        pawn=null;
        isOccupied=false;
    }*/



}