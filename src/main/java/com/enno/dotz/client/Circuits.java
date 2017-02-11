package com.enno.dotz.client;

import java.util.ArrayList;
import java.util.List;

import com.enno.dotz.client.Cell.CircuitCell;
import com.enno.dotz.client.Cell.CircuitCell.OnOff;
import com.enno.dotz.client.Circuits.Circuit;

public class Circuits extends ArrayList<Circuit>
{
    private GridState m_state;
    
    public void makeConnections(GridState state)
    {
        m_state = state;
        
        for (int row = 0; row < m_state.numRows; row++)
        {
            for (int col = 0; col < m_state.numColumns; col++)
            {
                Cell cell = state.cell(col, row);
                if (cell instanceof CircuitCell)
                {
                    CircuitCell c = (CircuitCell) cell;
                    Circuit g = row > 0 ? circuit(col, row - 1) : null;
                    if (g != null)
                    {
                        g.add(c);
                        Circuit g2 = col > 0 ? circuit(col - 1, row) : null;
                        if (g2 != null)
                            merge(g, g2);
                    }
                    else
                    {
                        g = col > 0 ? circuit(col - 1, row) : null;
                        if (g != null)
                        {
                            g.add(c);
                        }
                        else
                        {
                            g = new Circuit();
                            add(g);
                            g.add(c);
                        }
                    }
                }
            }
        }
    }
    
    public void checkConnections(List<Circuit> explodedCircuits)
    {
        for (Circuit g : this)
        {
            if (!g.done)
            {
                if (g.checkConnections())
                {
                    explodedCircuits.add(g);
                    g.done = true;
                }
            }
        }
    }
    
    public void copyState(UndoState undoState)
    {
        int n = size();
        boolean[] b = new boolean[n];
        for (int i = 0; i < n; i++)
        {
            b[i] = get(i).done;
        }
        undoState.circuitsDone = b;
    }
    
    public void restoreState(UndoState undoState)
    {
        int n = size();
        boolean[] b = undoState.circuitsDone;
        for (int i = 0; i < n; i++)
        {
            get(i).done = b[i];
        }
    }
    
    protected void merge(Circuit a, Circuit b)
    {
        if (a != b)
        {
            remove(b);
            a.addAll(b);
        }
    }
    
    protected Circuit circuit(int col, int row)
    {
        Cell c = m_state.cell(col,  row);
        if (c instanceof CircuitCell)
        {
            for (Circuit g : this)
            {
                if (g.contains((CircuitCell) c))
                    return g;
            }
        }
        return null;
    }
    
    public static class Circuit extends ArrayList<CircuitCell>
    {
        public boolean done;
        
        public boolean checkConnections()
        {
            for (CircuitCell c : this)
            {
                if (c.state == OnOff.ON)
                    return false;
            }
            
            for (CircuitCell c : this)
            {
                c.setDone();
            }
            return true; // blow it up
        }
    }
}
