package com.enno.dotz.client;

public enum DropDirection
{
    LEFT,
    RIGHT,
    DOWN,
    TELEPORT,
    NOT_ALLOWED;

    /**
     * @return  Next direction that dot comes from when next to Slide.
     */
    public DropDirection next()
    {
        switch (this)
        {
            // RIGHT, DOWN, LEFT
            case DOWN: return LEFT;
            case RIGHT: return DOWN;
            default: return RIGHT;
        }
    }
}