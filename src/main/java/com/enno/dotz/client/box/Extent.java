package com.enno.dotz.client.box;

public class Extent
{
    public static final Double FULL = new Double(-100);
    
    Double x, y, w, h;
    
    public Extent(Double w, Double h)
    {
        this.w = w;
        this.h = h;
    }

    public Extent(Double x, Double y, Double w, Double h)
    {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public static boolean isFill(Double w)
    {
        return w != null && w <= 0;
    }
}