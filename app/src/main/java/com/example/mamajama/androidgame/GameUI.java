package com.example.mamajama.androidgame;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

/**
 * Created by Savaque on 12/16/2017.
 */

public class GameUI {
    public float left, top, right, bottom;
    public RectF section;
// GameUI needs to update regularly to stay in top left corner.
    public GameUI(float cameraX, float cameraY){
        left = -cameraX;
        top= -cameraY;
        right = -cameraX+400;
        bottom= -cameraY+200;

        section = new RectF(left,top,right,bottom);
    }

    public void draw(Canvas canvas, Paint paint){
        canvas.drawRect(section,paint);
    }

    public boolean isInsideOf(float x, float y){
        if (x > right){

                Log.d("Inside","Returning True");
                return true;
        }
        Log.d("Inside","Returning false");
        return false;
    }
}

