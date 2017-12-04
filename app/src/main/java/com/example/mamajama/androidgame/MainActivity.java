package com.example.mamajama.androidgame;

import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.os.Bundle;


public class MainActivity extends Activity {

    //Setting some constants we'll need for the map
    public static int TILE_WIDTH = 200;
    public static int TILE_HEIGHT = 200;
    public static int SCREEN_WIDTH = 1200;
    public static int SCREEN_HEIGHT = 1600;


    GameView gameView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize gameView and set it as the view
        gameView = new GameView(this);
        setContentView(gameView);


    }

    class GameView extends SurfaceView implements Runnable {

        // Our Thread
        Thread gameThread = null;

        //Will be used with Paint and Canvas
        SurfaceHolder ourHolder;

        //Is the game playing?
        volatile boolean playing;

        //Canvas and Paint objects
        Canvas canvas;
        Paint paint;

        //Tracks the game's Framerate
        long fps;

        //Used to help calculate FPS
        long thisTimeFrame;

        //Our bitmap
        Bitmap bitmapLamia;

        //TESTING Pawn
        //Trying to instance a new pawn:
        Pawn Lamia;

        //Lamia is not moving at the start
        boolean isMoving = false;

        //Creating game grid


        int w = (SCREEN_WIDTH/TILE_WIDTH);
        int h = (SCREEN_HEIGHT/TILE_HEIGHT);

        int map[] = new int[(w*h)];
        Tile grid[]= new Tile[(w*h)];









        public GameView(Context context) {


            //Have SurfaceView set up our object
            super(context);

            //Initialize Holder and Paint objects
            ourHolder = getHolder();
            paint = new Paint();

            //instantiating Pawn
           Lamia = new Pawn(context, "lamiawalk");

           //Instancing grid
            for(int x=0; x<grid.length; x++){
                int xpos= (x%w)*TILE_WIDTH;
                int ypos= (x/w)*TILE_HEIGHT;
                grid[x]=new Tile(context,xpos,ypos);
            }

    }


        @Override
        public void run() {
            while (playing) {

                //capture the current time in milliseconds
                long startFrameTime = System.currentTimeMillis();

                //Then update
                update(startFrameTime);

                //Then draw
                draw();

                //Lastly calculate fps
                long timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if (timeThisFrame > 0) {
                    fps = 1000 / timeThisFrame;
                }


            }
        }

        // The SurfaceView class implements onTouchListener
        // So we can override this method and detect screen touches.
        @Override
        public boolean onTouchEvent(MotionEvent motionEvent){

            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

                // Player has touched the screen
                case MotionEvent.ACTION_DOWN:

                    // Set isMoving so the Lamia is moved in the update method
                    isMoving = true;

                    break;

                // Player has removed finger from screen
                case MotionEvent.ACTION_UP:
                    // Here I'm taking the location of the event to send to pawn
                    float x = Math.round(motionEvent.getX()/TILE_WIDTH)*TILE_WIDTH;
                    float y = Math.round(motionEvent.getY()/TILE_HEIGHT)*TILE_HEIGHT;
                    Lamia.setDestination(x,y);

                    // Set isMoving so the Lamia does not move
                    isMoving = false;
                    break;
            }
            return true;


        }


        public void update(long time) {

            //If Lamia is moving, then move her to the right
            Lamia.move();
            if (Lamia.isMoving) {
                //lamiaXPosition = lamiaXPosition + (walkSpeedPerSecond / fps);
                Lamia.animate(time);
            }


        }

        public void draw() {

            //Ensure the drawing surface exists
            if (ourHolder.getSurface().isValid()) {
                // Lock the canvas ready to draw
                // Make the drawing surface our canvas object
                canvas = ourHolder.lockCanvas();

                // Draw the background color
                canvas.drawColor(Color.argb(255, 26, 128, 182));

                // Choose the brush color for drawing
                paint.setColor(Color.argb(255, 249, 129, 0));

                // Make the text a bit bigger
                paint.setTextSize(45);

                for(int x=0;x<grid.length;x++){
                    grid[x].reset();
                }

                int currentX=(int) Lamia.pawnXPosition/200;
                int currentY=(int) Lamia.pawnYPosition/200;
                int numberOfColumns= SCREEN_WIDTH/TILE_WIDTH;
                int positionInArray= (currentY*numberOfColumns)+currentX;
                System.out.print("currently at block "+positionInArray);
                grid[positionInArray].setType(1);
                for(int x=0;x<grid.length;x++){
                    double Distance = Math.sqrt(Math.pow((Lamia.pawnXPosition-grid[x].posX),2)+Math.pow((Lamia.pawnYPosition-grid[x].posY),2));
                    if (Distance < Lamia.pawnMoveSpeed){
                        grid[x].setType(1);
                    }
                }


                for(int x=0; x<map.length; x++){
                    float xpos= (x%w)*TILE_WIDTH;
                    float ypos= (x/w)*TILE_HEIGHT;
                    Bitmap tile = BitmapFactory.decodeResource(this.getResources(),R.drawable.grasscenterblock);
                    canvas.drawBitmap(grid[x].bitmap,grid[x].posX,grid[x].posY,paint);
                }
//------------------------------------------------------------------------
                //Fiddling with isometry
                //Drawing lines to represent top down grid
                //to see how they fit over current map
                int countW=SCREEN_WIDTH/TILE_WIDTH;
                for (int i=0; i<=countW;i++){
                    //canvas.drawLine(0, 0, 200, 200, paint);
                    canvas.drawLine((float)(i*TILE_WIDTH),0,(float)(i*TILE_WIDTH),(float)(SCREEN_WIDTH),paint);
                }
                // The following code snippet produces an isometric grid
            /*
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.GREEN);// Needs to be changes to work with Canvas
                int width = getWidth();
                int height = getHeight();
                int sizeW = 50;
                int sizeH = 50;
                int countW = width / sizeW;
                int countH = height / sizeH;

                for (int i = 0; i <= countW + countH; i++) {
                    g.drawLine(0, i * sizeH, i * sizeH, 0);
                    g.drawLine(width - i * sizeW, 0, width, i * sizeW);
                }*/
//--------------------------------------------------------------------------------------


                // Display the current fps on the screen
                canvas.drawText("FPS:" + fps, 20, 40, paint);




                //draw the lamia at the proper position
                canvas.drawBitmap(Lamia.animation[Lamia.currentFrame], Lamia.getX(), Lamia.getY(), paint);



                // Draw everything to the screen
                // and unlock the drawing surface
                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        // shutdown our thread.
        public void pause() {
            playing = false;


            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error:", "joining thread");
            }

        }

        // start our thread.
        public void resume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    // This method executes when the player starts the game
    @Override
    protected void onResume() {
        super.onResume();

        // Tell the gameView resume method to execute
        gameView.resume();
    }

    // This method executes when the player quits the game
    @Override
    protected void onPause() {
        super.onPause();

        // Tell the gameView pause method to execute
        gameView.pause();
    }
}