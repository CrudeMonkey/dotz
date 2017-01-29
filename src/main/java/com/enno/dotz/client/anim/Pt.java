package com.enno.dotz.client.anim;

import java.util.ArrayList;

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
    
    public static class PtList extends ArrayList<Pt>
    {
        public boolean contains(int col, int row)
        {
            for (Pt p : this)
            {
                if (p.col == col && p.row == row)
                    return true;
            }
            return false;
        }

        public Pt pop()
        {
            int n = size();
            if (n == 0)
                return null;
            
            return remove(n - 1);
        }
    }
}