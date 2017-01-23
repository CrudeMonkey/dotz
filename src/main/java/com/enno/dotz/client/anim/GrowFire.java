package com.enno.dotz.client.anim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.enno.dotz.client.Cell;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.GridState;
import com.enno.dotz.client.anim.Transition.GrowFireTransition;
import com.enno.dotz.client.item.Fire;

public class GrowFire
{
    private Context ctx;
    private Config cfg;
    private GridState state;
    
    private List<Cell> m_from = new ArrayList<Cell>();
    private List<Cell> m_to = new ArrayList<Cell>();
    
    private Collection<Cell> m_verboten; // don't grow fire where the animals were or are going
    
    public GrowFire(TransitionList list, Collection<Cell> verboten, Context ctx)
    {
        this.ctx = ctx;
        this.cfg = ctx.cfg;
        this.state = ctx.state;
        
        m_verboten = verboten;
        Collection<Cell> cells = getFireCells();
        
        int nr = cfg.numRows;
        int nc = cfg.numColumns;
        
        for (Cell c : cells)
        {
            int col = c.col;
            int row = c.row;
            if (col > 0)
                maybeAdd(c, state.cell(col - 1, row));
            if (col < nc - 1)
                maybeAdd(c, state.cell(col + 1, row));
            if (row > 0)
                maybeAdd(c, state.cell(col, row - 1));
            if (row < nr - 1)
                maybeAdd(c, state.cell(col, row + 1));
        }
        
        for (int i = 0; i < ctx.generator.fireGrowthRate; i++)
        {
            if (!canGrow())
                break;
            
            int index = ctx.beastRandom.nextInt(m_from.size());
            list.add(getTransition(index));
            
            m_from.remove(index);
            m_to.remove(index);
        }
    }
    
    private List<Cell> getFireCells()
    {
        List<Cell> cells = new ArrayList<Cell>();
    
        for (int row = 0; row < cfg.numRows; row++)
        {
            for (int col = 0; col < cfg.numColumns; col++)
            {
                Cell c = state.cell(col, row);
                if (!c.isLocked() && c.item != null && c.item instanceof Fire && !m_verboten.contains(c))
                {
                    // don't want to grow fire from where animal is moving!
                    cells.add(c);
                }
            }
        }
        return cells;
    }
    
    public boolean canGrow()
    {
        return m_from.size() > 0;
    }
    
    protected GrowFireTransition getTransition(int index)
    {
        Cell from = m_from.get(index);
        final Cell to = m_to.get(index);
        final Fire newFire = new Fire(((Fire) from.item).isStuck());
        newFire.init(ctx);
        
        return new GrowFireTransition(state.x(from.col), state.y(from.row), state.x(to.col), state.y(to.row), newFire)
        {
            public void afterStart()
            {
                newFire.addShapeToLayer(ctx.nukeLayer);
            }
            
            public void afterEnd()
            {
                ctx.score.ate(to.item);
                
                newFire.removeShapeFromLayer(ctx.nukeLayer);
                newFire.addShapeToLayer(ctx.dotLayer);
                
                to.item.removeShapeFromLayer(ctx.dotLayer);
                to.item = newFire;
                
                ctx.score.generatedFire();
            }
        };
    }
    
    protected void maybeAdd(Cell fire, Cell c)
    {
        if (c.canGrowFire() && !m_verboten.contains(c)) // don't want to grow fire where animal is moving!
        {
            m_from.add(fire);
            m_to.add(c);
            m_verboten.add(c); // can't have multiple fires growing into the same cell
        }
    }
}