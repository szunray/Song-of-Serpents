package com.example.mamajama.androidgame;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

/**
 * Created by Savaque on 12/16/2017.
 */

public class GameUI {
    public int[] carToIso(int cartX, int cartY) {
        int isoX = cartX - cartY;
        int isoY = (cartX + cartY) / 2;
        int[] ans = {isoX, isoY};
        return ans;
    }
    public float left, top, right, bottom;
    public RectF section;
    public boolean attackUI=false;
    Pawn attacker;
    Pawn target;
// GameUI needs to update regularly to stay in top left corner.
    public GameUI(int cameraX, int cameraY){
        int isoCam[]=carToIso(cameraX,cameraY);
        left = -isoCam[0];
        top= -isoCam[1];
        right = -isoCam[0]+400;
        bottom= -isoCam[1]+200;

        section = new RectF(left,top,right,bottom);
    }
    public GameUI(Pawn target1,Pawn attacker1){
        int [] isoPos=carToIso((int)target1.getX(),(int)target1.getY());
        target=target1;
        attacker=attacker1;
        attackUI=true;
        left = isoPos[0];
        right = isoPos[0]+200;
        top = isoPos[1];
        bottom= isoPos[1]+100;

        section = new RectF(left,top,right,bottom);
    }

    public void update(int cameraX, int cameraY){
        if(!attackUI) {
            int isoCam[] = carToIso(cameraX, cameraY);
            left = -isoCam[0];
            top = -isoCam[1];
            right = -isoCam[0] + 400;
            bottom = -isoCam[1] + 200;

            section = new RectF(left, top, right, bottom);
        }
    }

    public void draw(Canvas canvas, Paint paint){
        if(section!=null)
        canvas.drawRect(section,paint);

    }

    public boolean isInsideOf(float x, float y){
        int isoClick[]=carToIso((int)x,(int)y);
        x=isoClick[0];
        y=isoClick[1];
        if (x > left && x < right){
            Log.d("Inside", "Left is "+ left+ "Right is "+right+ " and x was "+x);
            if (y>top && y<bottom) {
                Log.d("Inside", "top is "+ top+ "bottom is "+bottom+ " and y was "+y);
                Log.d("Inside", "Returning True");
                if (attackUI){
                    top=left=right=bottom=0;
                    section=null;
                    attacker.attack(target);
                }
                return true;
            }
        }
        Log.d("Inside", "Left is "+ left+ "Right is "+right+ " and x was "+x);
        Log.d("Inside", "top is "+ top+ "bottom is "+bottom+ " and y was "+y);
        Log.d("Inside","Returning false");
        return false;
    }
}

