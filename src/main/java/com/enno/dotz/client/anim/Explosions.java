package com.enno.dotz.client.anim;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.enno.dotz.client.Cell;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.GridState;
import com.enno.dotz.client.anim.Transition.ExplosionTransition;
import com.enno.dotz.client.item.Explody;

public class Explosions
{
    TransitionList explosions;
    Set<Cell> exploding = new HashSet<Cell>();
    GridState state;
    Context ctx;
    
    public Explosions(TransitionList explosions, List<Cell> cells, Context ctx)
    {
        this.ctx = ctx;
        this.explosions = explosions;
        this.state = ctx.state;
        exploding.addAll(cells);
        
        int nc = state.numColumns;
        int nr = state.numRows;
        for (Cell c : cells)
        {
            int col = c.col;
            int row = c.row;
            maybeAdd(c, col - 1, row);
            maybeAdd(c, col + 1, row);
            maybeAdd(c, col, row - 1);
            maybeAdd(c, col, row + 1);
        }
        for (Cell c : cells)
        {
            int col = c.col;
            int row = c.row;
            maybeAdd(c, col - 1, row - 1);
            maybeAdd(c, col - 1, row + 1);
            maybeAdd(c, col + 1, row - 1);
            maybeAdd(c, col + 1, row + 1);
        }
        
        for (Cell c : cells)
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