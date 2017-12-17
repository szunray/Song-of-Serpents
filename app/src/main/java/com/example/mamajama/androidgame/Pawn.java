package com.example.mamajama.androidgame;
import android.graphics.Bitmap;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by MamaJama on 12/1/2017.
 */

public class Pawn {
    // an array of images comprising the frames of an animation
    //There will eventually be 5 for every pawn
    //one for facing, away, to the left, to the right, and an idle animation
    Bitmap[] animation = new Bitmap[5];
    Bitmap portrait;
    Bitmap Idle;

    //Use frametimer and fps to determine if an animation should update.
    //increase frameTimer for slower animations theoretically
    //Note: I'm getting like, 9fps.
    private long frameTimer;
    private long fps;

    //Moving some attributes to the pawn itself
    float pawnMoveSpeed=400;
    float pawnXPosition=200;
    float pawnYPosition=200;

    float destination[] = {200,200};

    int hp=100;

    boolean isAlly=true;
    boolean isMoving=false;
    boolean hasMoved=false;

    public Rect frame = new Rect(0, 0 , 200, 200);
    public Rect frames[] = new Rect[7];


    //current frame is the index of the image in our animation array to use
    int currentFrame ;
    private int frameCount = animation.length;

    public Pawn(Context context, String animationPrefix){


        for(int x=0; x<5; x++){
            Resources resources = context.getResources();
            String nameOfImage = animationPrefix+"_"+(x+1);
            int resId = context.getResources().getIdentifier(nameOfImage, "drawable", context.getPackageName());
            animation[x] = BitmapFactory.decodeResource(resources,resId);




        }

        Resources resources = context.getResources();
        String nameOfImage = "medusa";
        int resId = context.getResources().getIdentifier(nameOfImage, "drawable", context.getPackageName());
        Idle = BitmapFactory.decodeResource(resources,resId);
        //Idle=Bitmap.createScaledBitmap(Idle,200*frames.length,200,false);

        try {
            InputStream targetStream = context.getAssets().open("medusa.xml");
            getFrames(targetStream);
        }catch(Exception r){
            Log.d("XMLREAD","File not found");
        }

        portrait= Bitmap.createScaledBitmap(animation[0],200,200,false);
        currentFrame=0;
        frameTimer=10;
        fps=10;

    }
    public Pawn(Context context, String animationPrefix,float x, float y){


        for(int i=0; i<5; i++){
            Resources resources = context.getResources();
            String nameOfImage = animationPrefix+"_"+(i+1);
            int resId = context.getResources().getIdentifier(nameOfImage, "drawable", context.getPackageName());
            animation[i] = BitmapFactory.decodeResource(resources,resId);




        }

        Resources resources = context.getResources();
        String nameOfImage = "medusa";
        int resId = context.getResources().getIdentifier(nameOfImage, "drawable", context.getPackageName());
        Idle = BitmapFactory.decodeResource(resources,resId);
        //Idle=Bitmap.createScaledBitmap(Idle,200*frames.length,200,false);
        try {
            InputStream targetStream = context.getAssets().open("medusa.xml");
            getFrames(targetStream);
        }catch(Exception r){
            Log.d("XMLREAD","File not found");
        }

        portrait= Bitmap.createScaledBitmap(animation[0],200,200,false);
        pawnXPosition=x;
        pawnYPosition=y;
        setDestination(x,y);
        pawnMoveSpeed=400;
        currentFrame=0;
        frameTimer=10;
        fps=10;

    }


    public void animate(long gameTime){
        if(gameTime>(frameTimer+fps)){
            currentFrame += 1;
            if (currentFrame>=frameCount)
                currentFrame = 0;
        }

    }

    public void getFrames(InputStream in)throws XmlPullParserException, IOException {

        float x = 0,y =0,w=0, h= 0;
       

         XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
         XmlPullParser myparser = xmlFactoryObject.newPullParser();

         myparser.setInput(in,null);

         int event = myparser.getEventType();
         int iterator=0;
         while (event != XmlPullParser.END_DOCUMENT){
             String name = myparser.getName();
             switch (event){

                 case XmlPullParser.START_TAG:
                 break;

                 case XmlPullParser.END_TAG:
                     if (name.equals("sprite")){
                         x = Float.parseFloat(myparser.getAttributeValue(null,"x"));
                         y = Float.parseFloat(myparser.getAttributeValue(null,"y"));
                         w = Float.parseFloat(myparser.getAttributeValue(null,"w"));
                         h = Float.parseFloat(myparser.getAttributeValue(null,"h"));
                         int scaleW=(int)Math.ceil(200/w);
                         Log.d("ScaleW", "ScaleW is "+scaleW);
                         int scaleH=(int)Math.ceil(200/h);
                         Log.d("ScaleW", "ScaleH is "+scaleH);

                         frames[iterator]=new Rect((int)x*scaleW,(int)y*scaleH,(int)x*scaleW+(int)(w*scaleW),(int)y*scaleH+(int)(h*scaleH));
                         iterator ++;

                        Log.d("XMLREAD","Event is"+x +" Iterator is" + iterator);

                     }
                     break;

             }
             event=myparser.next();
             //Log.d("XMLREAD","Event is" + event + "Iterator is "+ iterator);



         }
    }
// The start of the move function

    public void setDestination(float Xposition, float Yposition){
        destination[0]=Xposition;
        destination[1]=Yposition;

    }
    public void move(){

        if (destination[0]==pawnXPosition&&destination[1]==pawnYPosition&&isMoving==false){
            isMoving=false;
            return;
        }
        if (destination[0]>pawnXPosition){
            isMoving=true;
            pawnXPosition = pawnXPosition + (pawnMoveSpeed/2 / fps);
        }
        else if (destination[0]<pawnXPosition){
            isMoving=true;
            pawnXPosition = pawnXPosition - (pawnMoveSpeed/2 / fps);
        }
        if (destination[1]>pawnYPosition){
            isMoving=true;
            pawnYPosition=pawnYPosition+(pawnMoveSpeed/2/fps);
        }
        else if(destination[1]<pawnYPosition){
            isMoving=true;
            pawnYPosition=pawnYPosition-(pawnMoveSpeed/2/fps);
        }
        if (destination[0]==pawnXPosition&&destination[1]==pawnYPosition&&isMoving){
            isMoving=false;
            hasMoved=true;
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

    public void kill(){
        Log.d("KILL", "Pawn is dead, long live the pawn!");
        hp=0;
    }

}