package edu.rpi.cs.csci4963.su20.dzm.pacman;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFrame;

import edu.rpi.cs.csci4963.su20.dzm.pacman.game.Blinky;
import edu.rpi.cs.csci4963.su20.dzm.pacman.game.Clyde;
import edu.rpi.cs.csci4963.su20.dzm.pacman.game.Ghost;
import edu.rpi.cs.csci4963.su20.dzm.pacman.game.GhostMode;
import edu.rpi.cs.csci4963.su20.dzm.pacman.game.Inky;
import edu.rpi.cs.csci4963.su20.dzm.pacman.game.Pinky;
import edu.rpi.cs.csci4963.su20.dzm.pacman.game.Point;

public class Pacman {

    /**
     * The total score of all pellets and energizers
     */
    public static final int MAX_SCORE = 2600;
	
	private static final int POINT_SCORES = 10;
	private static final int FRUIT_SCORES = 30;
	private static final int ENERGIZER_SCORES = 50;
	private static final int ENERGIZER_LAST_TICKS = 3;
	private static final int MOVE_GAP = 5;
	private static int energizedCounter = 0;
	private static int scores = 0;
  	private static Tile[][] board;
  	private static Point direction;
   	private static Point location;
   	private static int tickCounter = 0;
   	private static boolean running;
	private static JFrame frame;
    private static GUI gui;

    private static Blinky blinky;
    private static Clyde clyde;
    private static Inky inky;
    private static Pinky pinky;
    private static Ghost[] ghosts;

    private static final int[] MODE_DURATIONS = {7*60, 20*60, 7*60, 20*60, 5*60, 20*60, 5*60};
    private static int curModeCount, curModeIndex;

    /**
     * Get blinky's location
     * @return the location of blinky
     */
    public static Point getBlinkyPos() {
        return blinky.getPosition();
    }

    /**
     * Get clyde's location
     * @return the location of clyde
     */
    public static Point getClydePos() {
        return clyde.getPosition();
    }

    /**
     * Get inky's location
     * @return the location of inky
     */
    public static Point getInkyPos() {
        return inky.getPosition();
    }

    /**
     * Get pinky's location
     * @return the location of pinky
     */
    public static Point getPinkyPos() {
        return pinky.getPosition();
    }
    
    /**
     * Sets a target tile in the board to a specified type.
     * Useful for pacman since every tile he leaves should become empty. Does nothing if target tile is out
     * of bounds.
     * @param row the row of the target tile
     * @param col the column of the target tile
     * @param newTile the new type for the target tile
     */
    private static void setBoardPos(int row, int col, Tile newTile) {
        if (row < 0 || row >= board.length || col < 0 || col >= board[0].length)
            return;
        
        board[row][col] = newTile;
    }

    /**
     * Gets the tile value for a target position in the board. Out of bounds accesses returns a wall.
     * @param row the row of the target tile
     * @param col the column of the target tile
     * @return the value of the tile at the specified location
     */
    public static Tile getBoardPos(int row, int col) {
        if (row < 0 || row >= board.length || col < 0 || col >= board[0].length)
            return Tile.WALL;
        
        return board[row][col];
    }

    /**
     * Get whether or not pacman can legally move into a tile, given that he can access that tile from his current location
     * @param row the row of the target tile
     * @param col the column of the target tile
     * @return true if the tile is not a wall or the ghost house, false otherwise
     */
    public static boolean isLegalPlayerMove(int row, int col) {
        if (row < 0 || row >= board.length || col < 0 || col >= board[0].length)
            return false;

        Tile t = board[row][col];
        return !(t == Tile.WALL || t == Tile.GHOST_HOUSE);
    }

    /**
     * Get whether or not a ghost can legally move into a tile, given that it can access that tile from its current location
     * @param row the row of the target tile
     * @param col the column of the target tile
     * @param leftHouse whether or not the ghost has already left the ghost house since spawning
     * @return true if the tile is a legal move, false otherwise
     */
    public static boolean isLegalGhostMove(int row, int col, boolean leftHouse) {
        if (row < 0 || row >= board.length || col < 0 || col >= board[0].length)
            return false;

        Tile t = board[row][col];
        return !(t == Tile.WALL || (t == Tile.GHOST_HOUSE && leftHouse));
    }

    /**
     * Get a copy of the player's current position
     * @return the current position of the player
     */
    public static Point getPlayerPos() {
    	return new Point(location.row, location.col);
    }

    /**
     * Get a copy of the current direction of the player
     * A point can also be used to represent a 2D vector
     * @return the player's current direction
     */
    public static Point getPlayerDir() {
    	return new Point(direction.row, direction.col);
    }

    /**
     * Get the player's current score
     * @return the current score of the player
     */
    public static int getPlayerScore() {
    	return scores;
    }

    /**
     * move the pacman to next tile and calculate the scores, if pacman dies, the pacman back to 0,0 on board
     * @param x row 
     * @param y col 
     * @param ghostPos The location of all ghosts.
     * @return zero and positive for the score gained on this move
     * 			-1 if the pacman is eaten by the ghost
     */
    private static int movePacman(int x, int y, ArrayList<Point> ghostPos) {
        //Allow wrapping around horizontally (go through tunnel)
        y = ((y % 28) + 28) % 28;
    	
    	if(!isLegalPlayerMove(x, y)) {
    		return 0;
    	}
    	if(tickCounter < MOVE_GAP) {
    		tickCounter ++;
    		return 0;
    	}
    	else {
    		tickCounter =0;
    	}
    	for(int i = 0; i < ghostPos.size();i++) {
    		Point tempGhostPos = ghostPos.get(i);   
//    		If pacman dies.
    		if((tempGhostPos.equals(location))&&(energizedCounter > 0)) {
				location = new Point(27,14);
    				return -1;
    		}
    	}
    	if(energizedCounter > 0) {
    		energizedCounter -= 1;
    	}
    	int gainedScore = 0;
    	Tile tempTile = board[x][y];
    	if(tempTile == Tile.ENERGIZER) {
    		gainedScore += ENERGIZER_SCORES;
    		energizedCounter = ENERGIZER_LAST_TICKS;
    	}
    	else if(tempTile == Tile.FRUIT) {
    		gainedScore += FRUIT_SCORES;
    	}
//    	both wall and ghost house are not allowed to enter
    	else if(tempTile == Tile.WALL||tempTile == Tile.GHOST_HOUSE) {
    		return gainedScore;
    	}
    	else if(tempTile == Tile.POINT) {
    		gainedScore += POINT_SCORES;
    	}
    	location = new Point(x,y);
    	scores += gainedScore;
    	return gainedScore;
    }
    
    /*
     * these method move pacmen by one tile one the board in one direction. And return the scores gained on this move. If -1
     * is returned, the pacman is eaten by ghost.
     * 
     * the method required input the location of the all ghost
     * 
     */
    
    /**
     * 
     * @param ghostPos location of all ghosts
     * @return new gained scores
     */
    public static int moveUp(ArrayList<Point> ghostPos) {
    	return movePacman(location.row-1, location.col, ghostPos);
    }
    /**
     * @param ghostPos location of all ghosts
     * @return new gained scores
     */
    public static int moveDown(ArrayList<Point> ghostPos) {
    	return movePacman(location.row+1, location.col, ghostPos);
    }
    /**
     * @param ghostPos location of all ghosts
     * @return new gained scores
     */
    public static int moveLeft(ArrayList<Point> ghostPos) {
    	return movePacman(location.row, location.col-1, ghostPos);
    }
    /**
     * @param ghostPos location of all ghosts
     * @return new gained scores
     */
    public static int moveRight(ArrayList<Point> ghostPos) {
    	return movePacman(location.row, location.col+1, ghostPos);
    }

    /**
     * Set the player to move in a specified direction.
     * The direction should be one of the constant points defined in the Point class
     * @param dir the new direction for the player
     */
    public static void setPlayerDirection(Point dir) {
        direction = dir;
    }

    private static void initBoard() {
        board = new Tile[36][28];
        
        try (Scanner scan = new Scanner(new FileInputStream(".." + File.separator + "res" + File.separator + "board.csv"))) {
            Tile[] values = Tile.values();

            for (int row = 0; row < 36; ++row) {
                String[] tiles = scan.nextLine().split(",", 0);

                for (int col = 0; col < 14; ++col) {
                    Tile tile = values[Integer.valueOf(tiles[col])];
                    board[row][col] = tile;
                    board[row][28-col-1] = tile;
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading board");
            e.printStackTrace();
        }
    }

    private static void initGame() {
        initBoard();

        blinky = new Blinky(new Point(14, 13));
        clyde = new Clyde(new Point(17, 15));
        inky = new Inky(new Point(17, 11), blinky);
        pinky = new Pinky(new Point(17, 13));

        ghosts = new Ghost[] {blinky, clyde, inky, pinky};

        location = new Point(27, 14);
        direction = Point.UP;

        curModeIndex = 0;
        curModeCount = 0;
    }

    private static void tick() {
        //Have ghosts change modes on a set timer
        if (curModeIndex < MODE_DURATIONS.length && MODE_DURATIONS[curModeIndex] <= ++curModeCount) {
            curModeCount = 0;
            GhostMode newMode = (++curModeIndex % 2 == 0) ? GhostMode.SCATTER : GhostMode.CHASE;

            for (Ghost g : ghosts)
                g.setMode(newMode);
        }

        //Move pacman
        ArrayList<Point> ghostPos = new ArrayList<Point>(4);
        for (Ghost g : ghosts)
            ghostPos.add(g.getPosition());

        if (direction == Point.UP)
            moveUp(ghostPos);
        else if (direction == Point.LEFT)
            moveLeft(ghostPos);
        else if (direction == Point.DOWN)
            moveDown(ghostPos);
        else
            moveRight(ghostPos);

        //Move ghosts
        for (Ghost g : ghosts) {
            if (g.getPosition().equals(location)) {
                //Kill pacman
            }
            g.tick();
            if (g.getPosition().equals(location)) {
                //Kill pacman
            }
        }

        //Redraw screen
        gui.repaint();
    }

    /**
     * Method to call in order to begin the game loop
     */
    public static void runGame() {
        int ticksPerSec = 60;
        long tickGap = 1000L / ticksPerSec; // time in milliseconds
        
        running = true;
        long lastTick = 0;
        while (running) {
            if (System.currentTimeMillis() - lastTick >= tickGap) {
                tick();
                lastTick = System.currentTimeMillis();
            }
        }
    }

    public static void main(String[] args) {
        initGame();

        frame = new JFrame();
        gui = new GUI();
        frame.add(gui);
        
        frame.setTitle("Pacman");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(560, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(false);
    }

}
