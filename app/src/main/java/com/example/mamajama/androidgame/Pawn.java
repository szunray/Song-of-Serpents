package com.example.mamajama.androidgame;
import android.graphics.Bitmap;
/**
 * Created by MamaJama on 12/1/2017.
 */

public class Pawn {
    // an array of images comprising the frames of an animation
    private Bitmap[] animation;

    //Use frametimer and fps to determine if an animation should update.
    private long frameTimer;
    private long fps;

    //current frame is the index of the image in our animation array to use
    private int currentFrame;
    private int frameCount = animation.length;

    public void animate(long gameTime){
        if(gameTime>(frameTimer+fps)){
            currentFrame += 1;
            if (currentFrame>frameCount)
                currentFrame = 0;
        }

    }

    public Pawn(String animationPrefix, int frames, long fps, long timer){
        animation = new Bitmap [frames];

    }

}
