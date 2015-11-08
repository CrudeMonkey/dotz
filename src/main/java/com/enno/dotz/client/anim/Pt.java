package com.enno.dotz.client.anim;

import com.enno.dotz.client.DropDirection;

public class Pt
{
    public int col, row;
    public DropDirection dir;
    
    public Pt(int col, int row)
    {
        this.col = col;
        this.row = row;
    }
    
    public String toString()
    {
        return col + "," + row;
    }
    
    @Override
    public boolean equals(Object pt)
    {
        if (pt instanceof Pt)
        {
            Pt p = (Pt) pt;
            return p.col == col && p.row == row;
        }
        return false;   
    }
    
    @Override
    public int hashCode()
    {
        return col * 100 + row;
    }
}