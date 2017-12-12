package com.example.mamajama.androidgame;

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

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    //Setting some constants we'll need for the map
    public static int TILE_WIDTH = 200;
    public static int TILE_HEIGHT = 200;
    public static int SCREEN_WIDTH = 1200;
    public static int SCREEN_HEIGHT = 1600;
    public static int X_OFFSET = 0;
    //Camera offsets are set right before the Pawn is Drawn
    public static int CAMERA_X = 0;
    public static int CAMERA_Y = 0;
    public boolean isPanning = false;
    GameView gameView;
    private float firstX, firstY;

    //Cartesian to isometric:
    public int[] carToIso(int cartX, int cartY) {
        int isoX = cartX - cartY;
        int isoY = (cartX + cartY) / 2;
        int[] ans = {isoX, isoY};
        return ans;
    }

    //Isometric to Cartesian:
    public int[] isoToCar(int isoX, int isoY) {
        int cartX = (2 * isoY + isoX) / 2;
        int cartY = (2 * isoY - isoX) / 2;
        int ans[] = {cartX, cartY};
        return ans;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize gameView and set it as the view
        gameView = new GameView(this);
        setContentView(gameView);


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
        //Not sure how pawns will spawn in final game
        List<Pawn> allPawns = new ArrayList<Pawn>();
        Pawn activePawn;
        Pawn Lamia;
        Pawn Handoff;

        //Lamia is not moving at the start
        boolean isMoving = false;

        //Creating game grid


        int w = (SCREEN_WIDTH / TILE_WIDTH);
        int h = (SCREEN_HEIGHT / TILE_HEIGHT);

        int map[] = new int[(w * h)];
        Tile grid[] = new Tile[(w * h)];


        public GameView(Context context) {


            //Have SurfaceView set up our object
            super(context);

            //Initialize Holder and Paint objects
            ourHolder = getHolder();
            paint = new Paint();

            //instantiating Pawn
            Lamia = new Pawn(context.getApplicationContext(), "lamiawalk", 400, 400);
            activePawn = new Pawn(context.getApplicationContext(), "lamiawalk");
            Handoff = activePawn;
            allPawns.add(activePawn);
            allPawns.add(Lamia);

            //Instancing grid
            for (int x = 0; x < grid.length; x++) {
                int xpos = (x % w) * TILE_WIDTH;
                int ypos = (x / w) * TILE_HEIGHT;
                grid[x] = new Tile(context.getApplicationContext(), xpos, ypos);
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
        public boolean onTouchEvent(MotionEvent motionEvent) {

            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {


                // Player has removed finger from screen
                case MotionEvent.ACTION_DOWN:

                    firstX = motionEvent.getX();
                    firstY = motionEvent.getY();
                    //   break; // up to debate which is better
                    //case MotionEvent.ACTION_UP:

                    /*We find what the isometric camera offsets would be, subtract those values
                    * from the input (which was made on an isometric grid), and THEN find the cartesian
                    * coordinates of that input.*/
                    int[] isoCam = carToIso(CAMERA_X, CAMERA_Y);
                    int[] cartesianClick = isoToCar((int) motionEvent.getX() - isoCam[0], (int) (motionEvent.getY() - isoCam[1]));


                    cartesianClick[0] = Math.round((cartesianClick[0]) / TILE_WIDTH) * TILE_WIDTH;
                    cartesianClick[1] = Math.round((cartesianClick[1]) / TILE_HEIGHT) * TILE_HEIGHT;

                    int fingerRow = cartesianClick[0] / TILE_WIDTH;
                    int fingerColumn = cartesianClick[1] / TILE_HEIGHT;
                    int numberOfColumns = SCREEN_WIDTH / TILE_WIDTH;
                    int numberOfRows = SCREEN_HEIGHT / TILE_HEIGHT;

                    int positionInArray = (fingerColumn * numberOfColumns) + fingerRow;
                    if (positionInArray >= 0 && positionInArray < grid.length) {
                        if (grid[positionInArray].isOccupied) {
                            activePawn = grid[positionInArray].pawn;
                            isPanning = false;
                        }

                        if (grid[positionInArray].type == 1) {
                            activePawn.setDestination(cartesianClick[0], cartesianClick[1]);

                        }


                    }


                    break;
                case MotionEvent.ACTION_MOVE: {


                    // Find the index of the active pointer and fetch its position
                    isPanning = true;
                    final float activeX = motionEvent.getX();
                    final float activeY = motionEvent.getY();

                    // Calculate the distance moved

                    float dx = activeX - firstX;
                    float dy = activeY - firstY;
                    int[] distance = isoToCar((int) dx, (int) dy);
                    CAMERA_X += distance[0];
                    CAMERA_Y += distance[1];

                    invalidate();

                    // Remember this touch position for the next move event
                    firstX = activeX;
                    firstY = activeY;

                    break;
                }
            }
            return true;


        }


        public void update(long time) {
            for (int x = 0; x < grid.length; x++) {
                grid[x].reset();
            }

            for (Pawn temp : allPawns) {
                int currentX = (int) temp.pawnXPosition / 200;
                int currentY = (int) temp.pawnYPosition / 200;
                int numberOfColumns = SCREEN_WIDTH / TILE_WIDTH;
                int positionInArray = (currentY * numberOfColumns) + currentX;
                grid[positionInArray].setIsOccupied(temp);
            }

            for (int x = 0; x < grid.length; x++) {
                double Distance = Math.sqrt(Math.pow((activePawn.pawnXPosition - grid[x].posX), 2) + Math.pow((activePawn.pawnYPosition - grid[x].posY), 2));
                if (Distance < activePawn.pawnMoveSpeed) {
                    grid[x].setType(1);
                }
            }

            activePawn.move();
            if (activePawn.isMoving) {
                activePawn.animate(time);
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

                //would be nice to find a new background image
                Bitmap Backdrop = BitmapFactory.decodeResource(this.getResources(), R.drawable.cloud);
                canvas.drawBitmap(Backdrop, 0, 0, paint);

                // Make the text a bit bigger
                paint.setTextSize(45);


                //highlight the square beneath the active pawn
                int currentX = (int) activePawn.pawnXPosition / 200;
                int currentY = (int) activePawn.pawnYPosition / 200;
                int numberOfColumns = SCREEN_WIDTH / TILE_WIDTH;
                int positionInArray = (currentY * numberOfColumns) + currentX;
                System.out.print("currently at block " + positionInArray);
                try {
                    grid[positionInArray].setType(1);


                } catch (IndexOutOfBoundsException e) {
                    System.err.println("IndexOutOfBoundsException: " + e.getMessage());
                }
                if (isPanning == false) {
                    CAMERA_Y = (int) -activePawn.getY() + 600;
                    CAMERA_X = (int) -activePawn.getX() + 1000;
                }
                int[] Cam = carToIso(CAMERA_X, CAMERA_Y);
                canvas.translate(Cam[0], Cam[1]);

                for (int x = 0; x < grid.length; x++) {
                    float xpos = ((x % w) * TILE_WIDTH);
                    float ypos = (x / w) * TILE_HEIGHT;
                    int[] isoTile = carToIso(grid[x].posX + X_OFFSET, grid[x].posY);
                    canvas.drawBitmap(grid[x].bitmap, isoTile[0], isoTile[1], paint);
                    canvas.drawText("Tile#" + x, isoTile[0], isoTile[1], paint);
                    //canvas.drawBitmap(grid[x].bitmap,grid[x].posX,grid[x].posY,paint);
                }
//------------------------------------------------------------------------

//--------------------------------------------------------------------------------------


                // Display the current fps on the screen
                canvas.drawText("FPS:" + fps, 20, 40, paint);


                //Go through entire map. If Tile has a monster on it, draw it.
                for (Tile tile : grid) {
                    if (tile.isOccupied) {
                        int[] isoPawn = carToIso((int) tile.pawn.getX(), (int) tile.pawn.getY());
                        canvas.drawBitmap(tile.pawn.animation[tile.pawn.currentFrame], isoPawn[0], isoPawn[1] - 100, paint);
                    }

                }


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
}
