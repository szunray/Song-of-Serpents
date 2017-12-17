package com.example.mamajama.androidgame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import android.graphics.RectF;
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

    //Camera offsets are set right before the Pawn is Drawn
    public static int CAMERA_X = 0;
    public static int CAMERA_Y = 0;

    //These are Camera Panning variables
    //need to stay global
    public boolean isPanning = false;
    float firstX =0, firstY =0;


    GameView gameView;
    GameUI basicUI;

    // Should make player a class in the future
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
//--------------------------------------------------------------------------------------------------
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
        List<Pawn> playerPawns = new ArrayList<Pawn>();
        List<Pawn> directorPawns= new ArrayList<Pawn>();
        Pawn activePawn;
        Pawn Lamia;


        //Lamia is not moving at the start
        boolean isMoving = false;
        boolean playerTurn=true;

        boolean cLocked=false;

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
            //Bug: If enemy pawn doesnt move, you can select it!
            Lamia = new Pawn(context.getApplicationContext(), "lamiawalk", 400, 400);
            activePawn = new Pawn(context.getApplicationContext(), "lamiawalk");
            Pawn enemyPawn = new Pawn(context.getApplicationContext(),"lamiawalk",0,0);
            enemyPawn.isAlly=false;

            playerPawns.add(activePawn);
            playerPawns.add(Lamia);


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

                // Delete pass
                int i=directorPawns.size();
                for (int x=0;x<i;x++){
                    if (directorPawns.get(x).hp==0){
                        directorPawns.remove(directorPawns.get(x));
                        x--;
                        i--;
                    }
                }
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

                    //First checks to see if the click was inside of any UI elements
                    //Theres only one now, and orange square.
                    int[] isoCam = carToIso(CAMERA_X, CAMERA_Y);

                    // Also: the grid extends beyond visible boundaries.
                    if (firstX<(-CAMERA_Y+400) && firstY<(-CAMERA_Y+200)){
                        Log.d("Inside", "Click intercepted");
                        nextPawn();
                        break;

                    }
                    int[] cartesianClick = isoToCar((int) motionEvent.getX() - isoCam[0], (int) (motionEvent.getY() - isoCam[1]));

                    cartesianClick[0] = Math.round((cartesianClick[0]) / TILE_WIDTH) * TILE_WIDTH;
                    cartesianClick[1] = Math.round((cartesianClick[1]) / TILE_HEIGHT) * TILE_HEIGHT;


                    int fingerRow = cartesianClick[0] / TILE_WIDTH;
                    int fingerColumn = cartesianClick[1] / TILE_HEIGHT;
                    int numberOfColumns = SCREEN_WIDTH / TILE_WIDTH;
                    int numberOfRows = SCREEN_HEIGHT / TILE_HEIGHT;


                    if(activePawn.isMoving==false) {
                        int positionInArray = (fingerColumn * numberOfColumns) + fingerRow;
                        if (positionInArray >= 0 && positionInArray < grid.length) {
                            if (grid[positionInArray].isOccupied) {

                                if(grid[positionInArray].type==4){
                                    grid[positionInArray].pawn.kill();
                                    grid[positionInArray].Vacate();
                                    activePawn.hasMoved=true;
                                }
                                else if (grid[positionInArray].pawn.isAlly&&grid[positionInArray].pawn.hasMoved==false){
                                    activePawn = grid[positionInArray].pawn;
                                    isPanning = false;
                                }

                            }

                            if (grid[positionInArray].type == 2) {
                                activePawn.setDestination(cartesianClick[0], cartesianClick[1]);

                            }


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

//--------------------------------------------------------------------------------------------------
        public void update(long time) {
            for (int x = 0; x < grid.length; x++) {
                grid[x].reset();
            }

            grid[17].setType(3);

            // ensure the paws are occupying tiles
            for (Pawn temp : playerPawns) {
                int currentX = (int) temp.pawnXPosition / 200;
                int currentY = (int) temp.pawnYPosition / 200;
                int numberOfColumns = SCREEN_WIDTH / TILE_WIDTH;
                int positionInArray = (currentY * numberOfColumns) + currentX;
                grid[positionInArray].setIsOccupied(temp);
            }
            for (Pawn temp : directorPawns) {
                int currentX = (int) temp.pawnXPosition / 200;
                int currentY = (int) temp.pawnYPosition / 200;
                int numberOfColumns = SCREEN_WIDTH / TILE_WIDTH;
                int positionInArray = (currentY * numberOfColumns) + currentX;

                if(temp.hp>0)
                grid[positionInArray].setIsOccupied(temp);
                else
                    grid[positionInArray].Vacate();
            }


            //Keep track of who moved
            //TODO: Make player unable to move moved pieces.
            if (activePawn.hasMoved){
                nextPawn();
            }

            //After player pieces have moved, their turn is over
            //Spawn enemy pawn, have all enemy pawns chase.
            if (playerTurn==false){
                Pawn enemyPawn = new Pawn(getContext().getApplicationContext(),"lamiawalk",0,0);

                enemyPawn.isAlly=false;
                directorPawns.add(enemyPawn);

                for(Pawn enemy:directorPawns) {
                    if(enemy.hp>0){
                        autoChase(enemy);
                    }
                }

            }

            for (int x = 0; x < grid.length; x++) {
                double Distance = Math.sqrt(Math.pow((activePawn.pawnXPosition - grid[x].posX), 2) + Math.pow((activePawn.pawnYPosition - grid[x].posY), 2));
                if (Distance < activePawn.pawnMoveSpeed) {
                    grid[x].setType(2);
                    if (grid[x].isOccupied){
                        grid[x].setType(1);
                        if(grid[x].pawn.isAlly==false){
                            grid[x].setType(4);
                        }
                    }
                }

            }

            // Only the active pawn moves as you can see here.
            // This will need to be changed later, to accomodate Idle animations.
            activePawn.move();
            if (activePawn.isMoving) {
                activePawn.animate(time);
            }
            for (Pawn pawn:directorPawns){
                pawn.move();
                if (pawn.isMoving){
                    pawn.animate(time);
                    break;
                }
            }


        }

        public void nextPawn(){
            int loopCount=0;
            while(loopCount<playerPawns.size()) {
                int playerIndex = playerPawns.indexOf(activePawn);
                if (playerIndex < playerPawns.size() - 1)
                    playerIndex += 1;
                else
                    playerIndex = 0;
                if(playerPawns.get(playerIndex).hasMoved==false) {
                    activePawn = playerPawns.get(playerIndex);
                    break;
                }
                loopCount++;
                if (loopCount>=playerPawns.size())
                    playerTurn=false;
            }

        }
//--------------------------------------------------------------------------------------------------

        public void draw() {

            //Ensure the drawing surface exists

            if (ourHolder.getSurface().isValid()) {
                // Lock the canvas ready to draw
                // Make the drawing surface our canvas object
                //if(!cLocked)
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
                    grid[positionInArray].setType(2);


                } catch (IndexOutOfBoundsException e) {
                    System.err.println("IndexOutOfBoundsException: " + e.getMessage());
                }
                if (isPanning == false) {
                    CAMERA_Y = (int) -activePawn.getY() + 300;
                    CAMERA_X = (int) -activePawn.getX() + 900;
                }
                int[] Cam = carToIso(CAMERA_X, CAMERA_Y);
                canvas.translate(Cam[0], Cam[1]);

                //Draw the grid isometrically
                for (int x = 0; x < grid.length; x++) {
                    float xpos = ((x % w) * TILE_WIDTH);
                    float ypos = (x / w) * TILE_HEIGHT;
                    int[] isoTile = carToIso(grid[x].posX, grid[x].posY);
                    canvas.drawBitmap(grid[x].bitmap, isoTile[0], isoTile[1], paint);
                    canvas.drawText("Tile#" + x, isoTile[0], isoTile[1], paint);

                }

                // Display the current fps on the screen
                canvas.drawText("FPS:" + fps, 20, 40, paint);


                //instead of drawing pawns based on the grid data, just go thru these lists.
                for (Pawn pawn: playerPawns){
                    int[] isoPawn = carToIso((int) pawn.getX(), (int) pawn.getY());
                    canvas.drawBitmap(pawn.animation[pawn.currentFrame],isoPawn[0],isoPawn[1]-100,paint);
                }


                for (Pawn pawn: directorPawns){
                    if(pawn.hp>0) {
                        int[] isoPawn = carToIso((int) pawn.getX(), (int) pawn.getY());
                        canvas.drawBitmap(pawn.animation[pawn.currentFrame], isoPawn[0], isoPawn[1] - 100, paint);
                    }
                }

                //So UI is going to be objects that get drawn last.
                //They're based on the isometric camera location, so they should remain "static"
                //over the grid. Eventually, we'll add them to an arrayList to keep em straight.
                //canvas.drawRect(-Cam[0],-Cam[1],400-Cam[0],200-Cam[1],paint);

                basicUI=new GameUI(Cam[0],Cam[1]);
                basicUI.draw(canvas,paint);


                int pawnIndex=0;
                //Bitmap bitmapIsis= BitmapFactory.decodeResource(this.getResources(),R.drawable.isis);
                for(Pawn temp: playerPawns){
                    if (temp.hasMoved)
                    {
                        ColorMatrix cm = new ColorMatrix();
                        cm.setSaturation(0);
                        ColorMatrixColorFilter f = new ColorMatrixColorFilter (cm);
                        paint.setColorFilter(f);

                    }
                    else{
                        paint.setColorFilter(null);
                    }
                    RectF wherePortrait=new RectF(-Cam[0]+200*pawnIndex,-Cam[1],-Cam[0]+200+200*pawnIndex,-Cam[1]+200);
                    //canvas.drawBitmap(temp.portrait,-Cam[0]+200*pawnIndex,-Cam[1],paint);
                    canvas.drawBitmap(temp.Idle,temp.frames[temp.currentFrame],wherePortrait,paint);

                    pawnIndex+=1;
                }
                pawnIndex=0;
                paint.setColorFilter(null);
                //canvas.drawBitmap(activePawn.portrait,-Cam[0],-Cam[1],paint);
                //protoUI

                //if (cLocked)
                ourHolder.unlockCanvasAndPost(canvas);
            }


        }

        public void autoChase(Pawn pawn){
            List<Tile> viable = new ArrayList<Tile>();
            Pawn target=null;
            //find target
            for (int x = 0; x < grid.length; x++) {
                if (grid[x].isOccupied)
                    if(grid[x].pawn.isAlly){
                    target = grid[x].pawn;
                    break;
                    }
            }

            //find viable targets to move to
            for (int x = 0; x < grid.length; x++) {
                double Distance = Math.sqrt(Math.pow((pawn.pawnXPosition - grid[x].posX), 2) + Math.pow((pawn.pawnYPosition - grid[x].posY), 2));
                if (Distance < pawn.pawnMoveSpeed&&grid[x].isOccupied==false) {
                    viable.add(grid[x]);
                }
            }

            //find which viable tile is closest to target
            if (target==null){
                return;
            }
            else{
                Tile targetTile=viable.get(0);
                double shortestDistance=Math.sqrt(Math.pow((target.pawnXPosition - viable.get(0).posX), 2) + Math.pow((target.pawnYPosition - viable.get(0).posY), 2));
                for (int x = 0; x < viable.size(); x++) {
                    double Distance = Math.sqrt(Math.pow((target.pawnXPosition - viable.get(x).posX), 2) + Math.pow((target.pawnYPosition - viable.get(x).posY), 2));
                    if (Distance<shortestDistance){
                        targetTile=viable.get(x);
                        shortestDistance=Distance;
                    }
                }

                //move to tile
                pawn.setDestination(targetTile.posX,targetTile.posY);
                pawn.move();
                playerTurn=true;
                resetTurn();
            }


        }

        public void resetTurn(){
            for (Pawn temp:playerPawns){
                temp.hasMoved=false;
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
