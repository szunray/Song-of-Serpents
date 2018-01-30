package com.example.mamajama.androidgame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;


public class MainActivity extends Activity {

    //Setting some constants we'll need for the map
    public static int TILE_WIDTH = 200;
    public static int TILE_HEIGHT = 200;
    public static int SCREEN_WIDTH = 1200;
    public static int SCREEN_HEIGHT = 1600;

    //Camera offsets are set right before the Pawn is Drawn
    public static int CAMERA_X = 0;
    public static int CAMERA_Y = 0;
    public static int CAMERA_TIME=0;

    //These are Camera Panning variables
    //need to stay global
    public boolean isPanning = false;
    float firstX =0, firstY =0;
    public int numMoved=0;


    GameView gameView;


    public boolean someoneIsMoving=false;

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
        List<GameUI>UIObjects = new ArrayList<GameUI>();

        Pawn activePawn;
        Pawn Lamia;


        //Lamia is not moving at the start
        boolean isMoving = false;
        boolean playerTurn=true;



        //Creating game grid


        int w = (SCREEN_WIDTH / TILE_WIDTH);
        int h = (SCREEN_HEIGHT / TILE_HEIGHT);
        int userPermissions=0;

        int map[] = new int[(w * h)];
        Tile grid[] = new Tile[(w * h)];
        //Tile grid[]=new Tile[20];
    String template = "5 1 1 1 1 1  1 1 1 1 1 4 1 1 1 1 1 1 1 1 4 1 1 4 1 5 4";

    public boolean checkNetworkConnection() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean isConnected = false;
        if (networkInfo != null && (isConnected = networkInfo.isConnected())) {



        } else {
            // show "Not Connected"
            Log.d("SONG", "No connection");
        }

        return isConnected;
    }

        public GameView(Context context) {
            super(context);

            //Have SurfaceView set up our object


            //Initialize Holder and Paint objects
            ourHolder = getHolder();
            paint = new Paint();

            //instantiating Pawn
            //Bug: If enemy pawn doesnt move, you can select it!
            Lamia = new Pawn(context.getApplicationContext(), "lamiawalk", 400, 400);
            Lamia.playerPermissions=1;
            activePawn = new Pawn(context.getApplicationContext(), "lamiawalk");
            activePawn.playerPermissions=2;

            Pawn enemyPawn = new Pawn(context.getApplicationContext(),"lamiawalk",0,0);
            enemyPawn.isAlly=false;

            playerPawns.add(activePawn);
            playerPawns.add(Lamia);

            GameUI basicUI = new GameUI(CAMERA_X,CAMERA_Y);
            UIObjects.add(basicUI);

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
                i=playerPawns.size();
                for (int x=0;x<i;x++){
                    if (playerPawns.get(x).hp==0){
                        playerPawns.remove(playerPawns.get(x));
                        x--;
                        i--;
                        nextPawn();
                    }
                }
                i=UIObjects.size();
                for (int x=0;x<i;x++){
                    if (UIObjects.get(x).section==null){
                        UIObjects.remove(UIObjects.get(x));
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

                    int[] cartesianClick = isoToCar((int) motionEvent.getX() - isoCam[0], (int) (motionEvent.getY() - isoCam[1]));

                    //first check if the click is inside a UI element.
                    for(GameUI ui : UIObjects) {
                        if (ui.isInsideOf(cartesianClick[0], cartesianClick[1])&&!ui.attackUI) {
                            nextPawn();
                        }
                    }

                    //then check to see if it is inside of the grid.

                    cartesianClick[0] = Math.round((cartesianClick[0]) / TILE_WIDTH) * TILE_WIDTH;
                    cartesianClick[1] = Math.round((cartesianClick[1]) / TILE_HEIGHT) * TILE_HEIGHT;

                    if(cartesianClick[0]>SCREEN_WIDTH ||cartesianClick[0]<0)
                        break;
                    if(cartesianClick[1]>SCREEN_HEIGHT ||cartesianClick[1]<0)
                        break;



                    //Then check to see if it's on a grid element
                    if (cartesianClick[0]>0 && cartesianClick[0]<SCREEN_WIDTH){

                    }

                    int fingerRow = cartesianClick[0] / TILE_WIDTH;
                    int fingerColumn = cartesianClick[1] / TILE_HEIGHT;
                    int numberOfColumns = SCREEN_WIDTH / TILE_WIDTH;
                    int numberOfRows = SCREEN_HEIGHT / TILE_HEIGHT;


                    if(activePawn.isMoving==false&&!someoneIsMoving) {
                        int positionInArray = (fingerColumn * numberOfColumns) + fingerRow;
                        if (positionInArray >= 0 && positionInArray < grid.length) {
                            if (grid[positionInArray].isOccupied) {// all this validation may not be necessary.

                                if(grid[positionInArray].type==4){
                                    for(GameUI ui : UIObjects){
                                        if(ui.attackUI){
                                            ui.section=null;
                                        }
                                    }
                                    GameUI attackUI=new GameUI(grid[positionInArray].pawn,activePawn);
                                    UIObjects.add(attackUI);
                                    //activePawn.attack(grid[positionInArray].pawn);
                                    //grid[positionInArray].pawn.kill();
                                    //grid[positionInArray].Vacate();
                                    //activePawn.hasMoved=true;
                                }
                                else if (grid[positionInArray].pawn.isAlly&&grid[positionInArray].pawn.hasMoved==false){
                                    activePawn = grid[positionInArray].pawn;

                                        for(GameUI ui : UIObjects){
                                            if(ui.attackUI){
                                                ui.section=null;
                                            }
                                        }

                                    isPanning = false;
                                }

                            }

                            if (grid[positionInArray].type == 2) {
                                //Log.d("Destination","Position in area is "+positionInArray);
                                if(activePawn.playerPermissions==userPermissions) {
                                    HttpGetAsyncTask setDest= new HttpGetAsyncTask();
                                    HashMap<String,String> data = new HashMap<String, String>();
                                    data.put("Pawn",Integer.toString(userPermissions));
                                    data.put("X",Integer.toString(cartesianClick[0]));
                                    data.put("Y",Integer.toString(cartesianClick[1]));
                                    data.put("URL","http://192.168.0.110/songofservers/getupdates");
                                    setDest.execute(data);
                                    activePawn.setDestination(cartesianClick[0], cartesianClick[1]);
                                }

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
            //Beginning network stuffs
            boolean connection = checkNetworkConnection();
            HttpGetAsyncTask task = new HttpGetAsyncTask();
            String myUrl= "http://192.168.0.110/songofservers/getupdates";
            String startingState;
            String playerData;
            try {
                startingState = task.execute(myUrl).get();

                playerData=startingState.substring(startingState.indexOf("Player Data"),startingState.indexOf("Game"));
                Log.d("RETRIEVED", "Retrieved: "+ playerData);
                applyNetworkUpdate(playerPawns,playerData);
                startingState=startingState.substring(startingState.indexOf("Game"));
                Log.d("RETRIEVED", "Retrieved: "+ startingState);
            }
            catch(Exception e){
                startingState = "failed";
            }


            //Parsing the string it got from the server
            startingState=startingState.replaceAll("[^0-9]+"," ");
            Log.d("RETRIEVED", "s = "+startingState);

            Scanner templateReader=new Scanner(startingState);
            int gridIndex=0;
            while(templateReader.hasNextInt()){
                int type=templateReader.nextInt();
                Log.d("GRIDTYPE", "Type = "+type);
                grid[gridIndex].type=type;
                //Log.d("TEMPL",gridIndex + "Has type "+ type);
                gridIndex++;
            }

            //Find out which snake the player is controlling.
            //Default setting is "0" which is neither snake.
            Log.d("PLAYER", "User Permissions "+userPermissions);
            if (userPermissions<1) {
                try {
                    HttpGetAsyncTask getNumber = new HttpGetAsyncTask();
                    myUrl = "http://10.0.2.2/songofservers/getpermission";
                    String s = getNumber.execute(myUrl).get();
                    Log.d("PLAYER", "Player : "+s);

                    s = s.replaceAll("[^0-9]+"," ");
                    Scanner numberGetter=new Scanner(s);
                    userPermissions=numberGetter.nextInt();
                }
                catch(Exception e){
                    Log.d("PLAYER", "Player : FAILED");
                    e.printStackTrace();
                }
            }
            // ensure the paws are occupying tiles
            for (Pawn temp : playerPawns) {
                int currentX = (int) temp.pawnXPosition / 200;
                int currentY = (int) temp.pawnYPosition / 200;
                int numberOfColumns = SCREEN_WIDTH / TILE_WIDTH;
                int positionInArray = (currentY * numberOfColumns) + currentX;

                if(temp.hp>0)
                    grid[positionInArray].setIsOccupied(temp);
                else
                    grid[positionInArray].Vacate();
                //grid[positionInArray].setIsOccupied(temp);
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

            if (playerPawns.size()==0){
                return;
            }



            for (int x = 0; x < grid.length; x++) {
                double Distance = Math.sqrt(Math.pow((activePawn.pawnXPosition - grid[x].posX), 2) + Math.pow((activePawn.pawnYPosition - grid[x].posY), 2));
                if (Distance < activePawn.pawnMoveSpeed&&grid[x].type!=4) {
                    grid[x].setType(2);
                    if (grid[x].isOccupied){
                        grid[x].setType(1);
                        if(grid[x].pawn.isAlly==false){
                            grid[x].setType(4);
                        }

                    }

                }

            }





            //Keep track of who moved
            if (activePawn.hasMoved){
                nextPawn();
            }

            //After player pieces have moved, their turn is over
            //Spawn enemy pawn,

            // have all enemy pawns chase.
            if (playerTurn==false){

                for(int x=0;x<grid.length;x++)
                {
                    if (grid[x].type==5){
                        Log.d("SPAWN", "Spawning pawn");
                        Pawn enemyPawn = new Pawn(getContext().getApplicationContext(),"lamiawalk",grid[x].posX,grid[x].posY);
                        enemyPawn.isAlly=false;
                        //grid[x].setIsOccupied(enemyPawn);
                        directorPawns.add(enemyPawn);
                    }
                }
                /*Pawn enemyPawn = new Pawn(getContext().getApplicationContext(),"lamiawalk",0,0);
                enemyPawn.isAlly=false;
                directorPawns.add(enemyPawn);*/

                    for (Pawn pawn : directorPawns) {
                            autoChase(pawn);
                    }

                        resetTurn();
                        playerTurn = true;





            }




            //grid[4].type=4;

            // Only the active pawn moves as you can see here.
            // This will need to be changed later, to accomodate Idle animations.
            someoneIsMoving=false;
            activePawn.move();
          /*  if (activePawn.isMoving) {
                someoneIsMoving=true;
                activePawn.animate(time);
            }*/
            for (Pawn pawn:playerPawns){
                pawn.move();
                pawn.animate(time);
                if (pawn.isMoving||pawn.isAttacking){
                    someoneIsMoving=true;
                    break;
                }

            }
            for (Pawn pawn:directorPawns){
                pawn.move();
                if(pawn.knockedDown){
                    pawn.animate(time);
                }
                else if (pawn.isMoving||pawn.isAttacking){
                    someoneIsMoving=true;
                    pawn.animate(time);
                    break;
                }

            }






        }

        public void nextPawn(){
            int loopCount=0;
            int playerIndex = playerPawns.indexOf(activePawn);

            while(loopCount<playerPawns.size()) {


                if (playerIndex < playerPawns.size() - 1) {
                    playerIndex++;
                }
                else {
                    playerIndex = 0;
                }

                if(playerPawns.get(playerIndex).hasMoved==false) {
                    activePawn = playerPawns.get(playerIndex);


                        for(GameUI ui : UIObjects) {
                            if (ui.attackUI) {
                                ui.section = null;
                            }
                        }


                    return;
                }
                else
                loopCount++;

                if (loopCount>=playerPawns.size()) {
                    Log.d("PawnList", "size of player pawns is "+playerPawns.size());
                }
            }

            playerTurn = false;
            resetEnemy();

        }
//--------------------------------------------------------------------------------------------------

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

                while (playerPawns.size()<1){
                    canvas.drawText("You Lose", 20, 40, paint);
                    ourHolder.unlockCanvasAndPost(canvas);
                    return;
                }
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
                    //smooth panning
                    int diffX = (int)(-activePawn.getX() + 900)-CAMERA_X;
                    int diffY = (int)(-activePawn.getY() + 300)-CAMERA_Y;
                    Log.d("DIFF", "destY = " +(-activePawn.getY() + 900)+ "dest x");
                    Log.d("DIFF", "currentY = " + CAMERA_Y);
                    Log.d("DIFF", "Difference = " + diffY);

                    if(fps==0)
                        fps=1;
                    CAMERA_X+=(diffX/fps);
                    CAMERA_Y+=(diffY/fps);
                    CAMERA_TIME++;
                    Log.d("DIFF", "CamTick "+CAMERA_TIME);
                    // If it takes too long to center, snap to the other pawn
                    if(CAMERA_TIME>20){
                    CAMERA_Y = (int) -activePawn.getY() + 300;
                    CAMERA_X = (int) -activePawn.getX() + 900;
                    CAMERA_TIME=0;
                    Log.d("DIFF", "CamTick hit 20");
                    isPanning=true;}
                }
                int[] Cam = carToIso(CAMERA_X, CAMERA_Y);
                //----------------

                canvas.translate(Cam[0], Cam[1]);


                //canvas.translate(CAMERA_X,CAMERA_Y);
                // basicUI needs to be updated as the canvas translates.
                //To remain firmly in place.
                for (GameUI ui:UIObjects) {
                    ui.update(CAMERA_X, CAMERA_Y);
                }

                //Draw the grid isometrically
                for (int x = 0; x < grid.length; x++) {
                    if (grid[x].type==4){
                        int[] isoTile = carToIso(grid[x].posX, grid[x].posY);
                        canvas.drawText("Tile#" + x, isoTile[0], isoTile[1], paint);
                    }
                    else {
                        int[] isoTile = carToIso(grid[x].posX, grid[x].posY);
                        canvas.drawBitmap(grid[x].bitmap, isoTile[0], isoTile[1], paint);
                        canvas.drawText("Tile type" + grid[x].type, isoTile[0], isoTile[1], paint);
                    }

                }

                // Display the current fps on the screen
                canvas.drawText("FPS:" + fps, 20, 40, paint);


                //instead of drawing pawns based on the grid data, just go thru these lists.
                for (Pawn pawn: playerPawns){
                    int[] isoPawn = carToIso((int) pawn.getX(), (int) pawn.getY());
                    if (pawn.isAttacking){canvas.drawBitmap(pawn.spriteSheet,pawn.fullAnim[pawn.currentFrame+pawn.attackIndex],pawn.location,paint);}

                    else if(pawn.knockedDown){canvas.drawBitmap(pawn.spriteSheet,pawn.fullAnim[pawn.currentFrame+pawn.knockdownIndex],pawn.location,paint);}

                    else if (pawn.isMoving)
                    canvas.drawBitmap(pawn.spriteSheet,pawn.fullAnim[pawn.currentFrame+pawn.walkIndex],pawn.location,paint);
                    else
                        canvas.drawBitmap(pawn.spriteSheet,pawn.fullAnim[pawn.currentFrame],pawn.location,paint);
                }


                for (Pawn pawn: directorPawns){
                    if(pawn.hp>0) {
                        int[] isoPawn = carToIso((int) pawn.getX(), (int) pawn.getY());
                        if (pawn.isAttacking){canvas.drawBitmap(pawn.spriteSheet,pawn.fullAnim[pawn.currentFrame+pawn.attackIndex],pawn.location,paint);}
                        else if(pawn.knockedDown){canvas.drawBitmap(pawn.spriteSheet,pawn.fullAnim[pawn.currentFrame+pawn.knockdownIndex],pawn.location,paint);}

                        else {

                            canvas.drawBitmap(pawn.spriteSheet, pawn.fullAnim[pawn.currentFrame + pawn.walkIndex], pawn.location, paint);
                        }
                        //canvas.drawBitmap(pawn.animation[pawn.currentFrame], isoPawn[0], isoPawn[1] - 100, paint);
                    }
                }

                //So UI is going to be objects that get drawn last.
                //They're based on the isometric camera location, so they should remain "static"
                //over the grid. Eventually, we'll add them to an arrayList to keep em straight.
                for(GameUI ui : UIObjects) {
                    ui.draw(canvas, paint);
                }



                int pawnIndex=0;

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

                    canvas.drawBitmap(temp.spriteSheet,temp.fullAnim[1],wherePortrait,paint);

                    pawnIndex+=1;
                }

                paint.setColorFilter(null);


                ourHolder.unlockCanvasAndPost(canvas);
            }


        }

        public void autoChase(Pawn pawn){
            if (pawn.knockedDown){
                pawn.knockedDown=false;
                pawn.hasMoved=true;
                return;
            }
            if (playerPawns.size()==0)
                return;
            List<Tile> viable = new ArrayList<Tile>();
            Pawn target=null;
            Tile toVacate=null;
            //find target
            List<Pawn> possibleTargets = new ArrayList<Pawn>();
            for (int x = 0; x < grid.length; x++) {
                if (grid[x].isOccupied)
                    if(grid[x].pawn.isAlly){
                    possibleTargets.add(grid[x].pawn);
                    /*target = grid[x].pawn;
                    testVacate=grid[x];
                    break;*/
                    }
            }
            if(possibleTargets.size()==0)
                return;

            target = possibleTargets.get(0);
            double distanceToClosestTarget = getDistance(pawn,target);
            toVacate = grid[getPositionInArray(target)];


            for (Pawn possibleTarget:possibleTargets){
                if (getDistance(pawn,possibleTarget)<distanceToClosestTarget){
                    target=possibleTarget;
                    distanceToClosestTarget=getDistance(pawn,target);

                    int currentX = (int) target.pawnXPosition / 200;
                    int currentY = (int) target.pawnYPosition / 200;
                    int numberOfColumns = SCREEN_WIDTH / TILE_WIDTH;
                    int positionInArray = (currentY * numberOfColumns) + currentX;
                    toVacate = grid[positionInArray];
                }

            }
            if (target==null){
                return;
            }

            //find viable targets to move to
            for (int x = 0; x < grid.length; x++) {
                double Distance = Math.sqrt(Math.pow((pawn.pawnXPosition - grid[x].posX), 2) + Math.pow((pawn.pawnYPosition - grid[x].posY), 2));
                if (Distance < pawn.pawnMoveSpeed&&grid[x].isOccupied==false&&grid[x].type!=4) {
                    viable.add(grid[x]);
                }
            }

            //
            double inrange=Math.sqrt(Math.pow((target.pawnXPosition - pawn.pawnXPosition), 2) + Math.pow((target.pawnYPosition - pawn.pawnYPosition), 2));
            if (inrange<pawn.pawnMoveSpeed){
                pawn.attack(target);
                //Log.d("Kill confirmed","Pawn has killed a player piece");
                //testVacate.Vacate();
                if(target.hp<1)
                toVacate.Vacate();

                pawn.hasMoved=true;
                return;
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
                targetTile.setIsOccupied(pawn);
                pawn.move();
            }


        }

        public void resetTurn(){
            for (Pawn temp:playerPawns){
                temp.hasMoved=false;
                if (temp.knockedDown){
                    temp.knockedDown=false;
                    //temp.hasMoved=true;
                }
            }

        }
        public void resetEnemy(){
            for (Pawn temp: directorPawns){
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
        public double getDistance(Pawn X, Pawn Y){
            double distance = Math.sqrt(Math.pow((X.pawnXPosition - Y.pawnXPosition), 2) + Math.pow((X.pawnYPosition - Y.pawnYPosition), 2));
            return distance;
        }
        public int getPositionInArray(Pawn target){

            int currentX = (int) target.pawnXPosition / 200;
            int currentY = (int) target.pawnYPosition / 200;
            int numberOfColumns = SCREEN_WIDTH / TILE_WIDTH;
            int positionInArray = (currentY * numberOfColumns) + currentX;
          return positionInArray;
        }
        public void applyNetworkUpdate(List<Pawn> players, String playerState){
            String[] entries = playerState.split("Pawn");
            for(String entry:entries){
                Log.d("ANU",entry);
            }
            int count=playerState.split("Pawn").length;
            for (int x=1;x<count;x++){
                entries[x]=entries[x].replaceAll("[^0-9]+"," ");
                Scanner entryReader = new Scanner (entries[x]);
                int pawnIndex=entryReader.nextInt();
                int xDest = entryReader.nextInt();
                int yDest= entryReader.nextInt();
                players.get(pawnIndex).setDestination(xDest,yDest);
            }


        }
    }
}
