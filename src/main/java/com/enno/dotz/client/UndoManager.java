package com.enno.dotz.client;

import java.util.ArrayList;
import java.util.List;

public abstract class UndoManager
{
    private List<UndoState> m_list = new ArrayList<UndoState>();
    private int m_curr = -1;
    
    public abstract void push();
    
    public void push(UndoState state)
    {
        for (int i = m_list.size() - 1; i > m_curr; i--)
            m_list.remove(i);
        
        m_list.add(state);
        m_curr++;
    }

    public boolean canUndo()
    {
        return m_curr > 0;
    }
    
    public boolean canRedo()
    {
        return m_curr < m_list.size() - 1;
    }
    
    public UndoState undo()
    {
        m_curr--;
        return m_list.get(m_curr);
    }
    
    public UndoState redo()
    {
        m_curr++;
        return m_list.get(m_curr);
    }

    public UndoState peek()
    {
        return m_list.get(m_curr);
    }
}
