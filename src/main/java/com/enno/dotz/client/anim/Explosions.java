package com.enno.dotz.client.anim;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.enno.dotz.client.Cell;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.GridState;
import com.enno.dotz.client.anim.Transition.BlastTransition;
import com.enno.dotz.client.anim.Transition.ExplosionTransition;
import com.enno.dotz.client.item.Explody;
import com.enno.dotz.client.item.Striped;

public class Explosions
{
    TransitionList explosions;
    Set<Cell> exploding = new HashSet<Cell>();
    GridState state;
    Context ctx;
    
    public Explosions(TransitionList explosions, List<Cell> explodies, List<Cell> armed, Context ctx)
    {
        this.ctx = ctx;
        this.explosions = explosions;
        this.state = ctx.state;
        exploding.addAll(explodies);
        exploding.addAll(armed);
        
        int nr = ctx.state.numRows;
        int nc = ctx.state.numColumns;
        for (Cell c : armed)
        {
            final List<Cell> blasted = new ArrayList<Cell>();
            boolean vertical = ((Striped) c.item).vertical;
            if (vertical)
            {
                for (int row = 0; row < nr; row++)
                {
                    if (row != c.row)
                        addBlasted(blasted, c.col, row);
                }
            }
            else
            {
                for (int col = 0; col < nc; col++)
                {
                    if (col != c.col)
                        addBlasted(blasted, col, c.row);
                }
            }
            
            double x = state.x(c.col);
            double y = state.y(c.row);
            double x2 = x;
            double y2 = y;
            if (vertical)
                y2 += state.numRows * ctx.cfg.size;
            else
                x2 += state.numColumns * ctx.cfg.size;
            
            explosions.add(new BlastTransition(x, y, x2, y2, ctx) {
                public void afterEnd()
                {
                    super.afterEnd();
                    for (Cell to : blasted)
                        to.zap();           
                }
            });
        }
        
        for (Cell c : explodies)
        {
            int col = c.col;
            int row = c.row;
            maybeAdd(c, col - 1, row);
            maybeAdd(c, col + 1, row);
            maybeAdd(c, col, row - 1);
            maybeAdd(c, col, row + 1);
        }
        for (Cell c : explodies)
        {
            int col = c.col;
            int row = c.row;
            maybeAdd(c, col - 1, row - 1);
            maybeAdd(c, col - 1, row + 1);
            maybeAdd(c, col + 1, row - 1);
            maybeAdd(c, col + 1, row + 1);
        }
        
        for (Cell c : explodies)
        {
            if (((Explody) c.item).getRadius() == 2)
            {
                int col = c.col;
                int row = c.row;
                maybeAdd(c, col - 2, row - 2);
                maybeAdd(c, col - 2, row - 1);
                maybeAdd(c, col - 2, row);
                maybeAdd(c, col - 2, row + 1);
                maybeAdd(c, col - 2, row + 2);
                maybeAdd(c, col + 2, row - 2);
                maybeAdd(c, col + 2, row - 1);
                maybeAdd(c, col + 2, row);
                maybeAdd(c, col + 2, row + 1);
                maybeAdd(c, col + 2, row + 2);
                
                maybeAdd(c, col - 1, row - 2);
                maybeAdd(c, col,     row - 2);
                maybeAdd(c, col + 1, row - 2);
                maybeAdd(c, col - 1, row + 2);
                maybeAdd(c, col,     row + 2);
                maybeAdd(c, col + 1, row + 2);
            }
        }
    }
    
    private void addBlasted(List<Cell> blasted, int col, int row)
    {
        final Cell to = state.cell(col, row);
        if (!exploding.contains(to) && to.canBeNuked())
        {
            exploding.add(to);
            blasted.add(to);
        }
    }

    private void maybeAdd(Cell from, int col, int row)
    {
        if (!state.isValidCell(col, row))
            return;
        
        final Cell to = state.cell(col, row);
        if (!exploding.contains(to) && to.canBeNuked())
        {
            exploding.add(to);
            explosions.add(new ExplosionTransition(state.x(from.col), state.y(from.row), state.x(to.col), state.y(to.row), ctx) {
                public void afterEnd()
                {
                    super.afterEnd();
                    to.zap();                        
                }
            });
        }
    }        
}