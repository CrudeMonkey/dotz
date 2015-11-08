package com.enno.dotz.client;

import java.util.Random;

import com.enno.dotz.client.anim.Pt;

public class Direction
{
    public static int rotate(int direction, boolean clockwise)
    {
        if (clockwise) // clockwise
        {
            switch (direction)
            {
                case Direction.NORTH: return Direction.EAST;
                case Direction.EAST: return Direction.SOUTH;
                case Direction.SOUTH: return Direction.WEST;
                case Direction.WEST: return Direction.NORTH;
            }
        }
        else // CCW
        {
            switch (direction)
            {
                case Direction.NORTH: return Direction.WEST;
                case Direction.EAST: return Direction.NORTH;
                case Direction.SOUTH: return Direction.EAST;
                case Direction.WEST: return Direction.SOUTH;
            }
        }
        return direction; // should never happen
    }

    public static final int NONE = 0;
    public static final int EAST = 1;
    public static final int WEST = 2;
    public static final int NORTH = 4;
    public static final int SOUTH = 8;
    
    public static int randomDirection(Random random)
    {
        return 1 << random.nextInt(4);
    }

    public static Pt vector(int direction)
    {
        switch (direction)
        {
            case NORTH: return new Pt(0, -1);
            case EAST: return new Pt(1, 0);
            case SOUTH: return new Pt(0, 1);
            case WEST: return new Pt(-1, 0);
        }
        return null;
    }

    public static int opposite(int direction)
    {
        switch (direction)
        {
            case NORTH: return SOUTH;
            case EAST: return WEST;
            case SOUTH: return NORTH;
            case WEST: return EAST;
        }
        return Direction.NONE; // should never happen
    }

    /**
     * 
     * @param from
     * @param to
     * 
     * @return -1 for left turn, 0 for straight, 1 for right turn
     */
    public static int turn(int from, int to)
    {
        if (from == to)
            return 0;
        
        switch (from)
        {
            case NORTH: return to == EAST ? 1 : -1;
            case EAST: return to == SOUTH ? 1 : -1;
            case SOUTH: return to == WEST ? 1 : -1;
            case WEST: return to == NORTH ? 1 : -1;
        }
        return 0; // should never happen
    }
}
