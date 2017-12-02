package com.example.mamajama.androidgame;
import android.graphics.Bitmap;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;

/**
 * Created by MamaJama on 12/1/2017.
 */

public class Pawn {
    // an array of images comprising the frames of an animation
    //There will eventually be 5 for every pawn
    //one for facing, away, to the left, to the right, and an idle animation
     Bitmap[] animation = new Bitmap[5];

    //Use frametimer and fps to determine if an animation should update.
    //increase frameTimer for slower animations theoretically
    //Note: I'm getting like, 9fps.
    private long frameTimer;
    private long fps;

    //Moving some attributes to the pawn itself
    float pawnMoveSpeed=150;
    float pawnXPosition=10;
    float pawnYPosition=200;


    //current frame is the index of the image in our animation array to use
     int currentFrame ;
    private int frameCount = animation.length;


    public void animate(long gameTime){
        if(gameTime>(frameTimer+fps)){
            currentFrame += 1;
            if (currentFrame>=frameCount)
                currentFrame = 0;
        }

    }
// The start of the move function
    public void move(float Xposition, float Yposition){
        if (Xposition>pawnXPosition){
           pawnXPosition = pawnXPosition + (pawnMoveSpeed / fps);
        }
        else if (Xposition<pawnXPosition){
            pawnXPosition = pawnXPosition - (pawnMoveSpeed / fps);
        }

    }

    public float getX(){
        return pawnXPosition;
    }
    public float getY(){
        return pawnYPosition;
    }

    //pawn constructor.
    //I'm sure this could be done better
    public Pawn(Context context, String animationPrefix){

        for(int x=0; x<5; x++){
            Resources resources = context.getResources();
            String nameOfImage = animationPrefix+"_"+(x+1);
            int resId = context.getResources().getIdentifier(nameOfImage, "drawable", context.getPackageName());
            animation[x] = BitmapFactory.decodeResource(resources,resId);
            //animation[x] = BitmapFactory.decodeResource(context.getResources(), R.drawable.lamia_02);


        }

        currentFrame=0;
        frameTimer=10;
        fps=9;

    }

}
