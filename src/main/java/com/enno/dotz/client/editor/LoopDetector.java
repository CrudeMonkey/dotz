package com.enno.dotz.client.editor;

import java.util.List;

import com.enno.dotz.client.Cell;
import com.enno.dotz.client.Cell.Hole;
import com.enno.dotz.client.Cell.Machine;
import com.enno.dotz.client.Cell.Rock;
import com.enno.dotz.client.Cell.Slide;
import com.enno.dotz.client.Cell.Teleport;
import com.enno.dotz.client.GridState;
import com.enno.dotz.client.anim.Pt;
import com.enno.dotz.client.anim.Pt.PtList;
import com.enno.dotz.client.util.Debug;

public class LoopDetector
{
    public static class LoopException extends Exception
    {
        public PtList list;

        public LoopException(PtList list)
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
    
    private PtList list = new PtList();
    
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
        for (int row = 0; row < nr; row++)
        {
            for (int col = 0; col < nc; col++)
            {
                chain(col, row, false);
            }
        }
    }
    
    private void chain(int col, int row, boolean fromTeleportTarget) throws LoopException
    {
        if (isDeadEnd(col, row, fromTeleportTarget))
            return;
        
        if (list.contains(col, row))
            throw new LoopException(list);
        
        list.add(new Pt(col, row));
        
        if (valid(col - 1, row))
        {
            Cell left = state.cell(col - 1,  row);
            if (left instanceof Slide && !((Slide) left).isToLeft())
                chain(col - 1, row - 1, false);
        }
        
        if (valid(col + 1, row))
        {
            Cell right = state.cell(col + 1,  row);
            if (right instanceof Slide && ((Slide) right).isToLeft())
                chain(col + 1, row - 1, false);
        }
        
        Cell c = state.cell(col, row);
        if (c.isTeleportTarget())
        {
            Teleport t = (Teleport) c;
            chain(t.getOtherCol(), t.getOtherRow(), true);            
        }
        else
        {
            chain(col, row - 1, false);
        }
        list.pop();
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
            return true;
        }
        return false;
    }
    
    protected boolean valid(int col, int row)
    {
        return col >= 0 && col < nc && row >= 0 && row < nr;
    }
}
