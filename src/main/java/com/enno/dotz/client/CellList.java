package com.enno.dotz.client;

import java.util.ArrayList;

import com.enno.dotz.client.Cell.Bubble;
import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.item.Chest;
import com.enno.dotz.client.item.Egg;
import com.enno.dotz.client.item.Wild;
import com.enno.dotz.client.util.Debug;

public class CellList extends ArrayList<Cell>
{
    public boolean previousCell(int col, int row)
    {
        int n = size();
        if (n < 2)
            return false;
        
        Cell prevCell = get(n - 2);
        return prevCell.col == col && prevCell.row == row;
    }
    
    public boolean didCell(int col, int row)
    {
        for (Cell c : this)
        {
            if (c.col == col && c.row == row)
                return true;
        }
        return false;
    }

    public Boolean isCracked()
    {
        for (Cell cell : this)
        {
            if (cell.item instanceof Egg)
                return ((Egg) cell.item).isCracked();
        }
        return null;
    }
    
    public boolean isOnlyWildCards()
    {
        for (Cell cell : this)
        {
            if (!(cell.item instanceof Wild))
                return false;
        }
        return true;
    }
    
    public boolean hasAnimals()
    {
        for (Cell cell : this)
        {
            if (cell.item instanceof Animal)
                return true;
        }
        return false;
    }
    
    public boolean hasDiagonal()
    {
        int n = size();
        for (int i = 1; i < n; i++)
        {
            Cell a = get(i-1);
            Cell b = get(i);
            if (isDiagonal(a.col, a.row, b.col, b.row))
                return true;
        }
        return false;
    }
    
    public boolean isDiagonal(int cola, int rowa, int colb, int rowb)
    {
        return cola != colb && rowa != rowb;
    }
    
    public void dumpChain()
    {
        StringBuilder b = new StringBuilder();
        for (Cell c : this)
        {
            b.append(c.col).append(",").append(c.row).append(" ");
        }
        Debug.p(b.toString());
    }

    public int getColor()
    {
        for (Cell c : this)
        {
            if (c.item == null)
                continue;
            
            int col = c.item.getColor();
            if (col != Config.WILD_ID)
                return col;
            
        }
        return Config.WILD_ID;
    }

    public boolean isOneColor()
    {
        int col = Config.WILD_ID;
        for (Cell c : this)
        {
            if (c.item.getColor() == Config.WILD_ID)
                continue;
            
            if (col == Config.WILD_ID)
            {
                col = c.item.getColor();
            }
            else
            {
                if (col != c.item.getColor())
                    return false;
            }
        }
        return true;
    }

    public boolean containsBubble()
    {
        for (Cell c : this)
        {
            if (c instanceof Bubble && !((Bubble) c).isPopped() && !(c.item instanceof Chest))
                return true;
        }
        return false;
    }

    public boolean containsCell(int col, int row)
    {
        for (Cell c : this)
        {
            if (c.col == col && c.row == row)
                return true;
        }
        return false;
    }
}