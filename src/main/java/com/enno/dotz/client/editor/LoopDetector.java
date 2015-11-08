package com.enno.dotz.client.editor;

import java.util.ArrayList;
import java.util.List;

import com.enno.dotz.client.Cell;
import com.enno.dotz.client.Cell.Hole;
import com.enno.dotz.client.Cell.Rock;
import com.enno.dotz.client.Cell.Slide;
import com.enno.dotz.client.Cell.Teleport;
import com.enno.dotz.client.GridState;
import com.enno.dotz.client.anim.Pt;
import com.enno.dotz.client.util.Debug;

public class LoopDetector
{
    public static class LoopException extends Exception
    {
        public List<Pt> list;

        public LoopException(List<Pt> list)
        {
            super("Loop");
            this.list = list;
        }

        public String loopToString()
        {
            StringBuilder b = new StringBuilder();
            for (Pt p : list)
            {
                b.append(p).append(" ");
            }
            return b.toString();
        }
    }
    
    private GridState state;
    private int nc, nr;
    
    private List<Pt> list = new ArrayList<Pt>();
    
    public LoopDetector(GridState state)
    {
        this.state = state;
        nc = state.numColumns;
        nr = state.numRows;
    }
    
    public boolean isValid()
    {
        try
        {
            validate();
            return true;
        }
        catch (LoopException e)
        {
            Debug.p("Found loop: " + e.loopToString());
            return false;
        }
    }

    public void validate() throws LoopException
    {
        COL: for (int col = 0; col < nc; col++)
        {
            int row = nr - 1;
            while (isHole(col, row))
                row--;
            
            if (isDeadEnd(col, row, false))
                continue COL;
            
            chain(col, row, false);
        }
    }
    
    private void chain(int col, int row, boolean fromTeleportTarget) throws LoopException
    {
        if (isDeadEnd(col, row, fromTeleportTarget))
            return;
        
        if (valid(col - 1, row))
        {
            Cell left = state.cell(col - 1,  row);
            if (left instanceof Slide && !((Slide) left).isToLeft())
                split().chain(col - 1, row - 1, false);
        }
        
        if (valid(col + 1, row))
        {
            Cell right = state.cell(col + 1,  row);
            if (right instanceof Slide && ((Slide) right).isToLeft())
                split().chain(col + 1, row - 1, false);
        }
        
        Cell c = state.cell(col, row);
        if (c.isTeleportTarget())
        {
            Teleport t = (Teleport) c;
            push(col, row);
            chain(t.getOtherCol(), t.getOtherRow(), true);            
        }
        else
        {
            push(col, row);
            chain(col, row - 1, false);
        }
    }
    
    private LoopDetector split()
    {
        LoopDetector d = new LoopDetector(state);
        d.list.addAll(list);
        return d;
    }

    protected void push(int col, int row) throws LoopException
    {
        Pt pt = new Pt(col, row);
        if (list.contains(pt))
        {
            list.add(pt);
            throw new LoopException(list);
        }
        
        list.add(pt);
    }

    protected boolean isDeadEnd(int col, int row, boolean fromTeleportTarget) throws LoopException
    {
        if (!valid(col, row))
            return true;
        
        Cell c = state.cell(col,  row);
        if (c instanceof Slide || c instanceof Rock)
            return true;
        
        if (!fromTeleportTarget && c.isTeleportSource())
        {
            LoopDetector d = new LoopDetector(state);            
            d.chain(col, row, true);
            return true;
        }
        return false;
    }
    
    protected boolean valid(int col, int row)
    {
        return col >= 0 && col < nc && row >= 0 && row < nr;
    }
    
    protected boolean isHole(int col, int row)
    {
        if (!valid(col, row))
            return false;
        
        return state.cell(col,  row) instanceof Hole;
    }
}
