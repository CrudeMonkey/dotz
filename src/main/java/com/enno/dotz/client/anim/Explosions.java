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
import com.enno.dotz.client.item.Blaster;
import com.enno.dotz.client.item.Bomb;
import com.enno.dotz.client.item.Explody;
import com.enno.dotz.client.item.Striped;
import com.enno.dotz.client.util.Debug;

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
            boolean vertical = c.item.isVertical(); // Blaster or Striped
            boolean bothWays = false;
            boolean isWide = false;
            
            if (c.item instanceof Blaster)
            {
                ctx.score.usedBlaster();
                
                Blaster blaster = (Blaster) c.item;
                bothWays = blaster.isBothWays();
                isWide = blaster.isWide();
            }
            else if (c.item instanceof Striped)
            {
                Striped blaster = (Striped) c.item;
                bothWays = blaster.isBothWays();
                isWide = blaster.isWide();
            }
            
            double x = state.x(c.col);
            double y = state.y(c.row);

            final boolean doingHorizontal = !vertical || bothWays;
            if (vertical || bothWays)
            {
                for (int row = 0; row < nr; row++)
                {
                    if (row != c.row)
                        addBlasted(blasted, c.col, row);
                    
                    if (isWide)
                    {
                        if (c.col > 0)
                            addBlasted(blasted, c.col - 1, row);
                        if (c.col < ctx.cfg.numColumns - 1)
                            addBlasted(blasted, c.col + 1, row);
                    }
                }
                
                double y2 = y + state.numRows * ctx.cfg.size;
                explosions.add(new BlastTransition(x, y, x, y2, isWide, ctx) {
                    public void afterEnd()
                    {
                        super.afterEnd();
                        
                        if (!doingHorizontal)   // don't zap them twice if we're blasting both directions
                        {
                            for (Cell to : blasted)
                                to.zap();
                        }
                    }
                });
            }
            if (doingHorizontal)
            {
                for (int col = 0; col < nc; col++)
                {
                    if (col != c.col)
                        addBlasted(blasted, col, c.row);

                    if (isWide)
                    {
                        if (c.row > 0)
                            addBlasted(blasted, col, c.row - 1);
                        if (c.row < ctx.cfg.numRows - 1)
                            addBlasted(blasted, col, c.row + 1);
                    }
                }
                
                double x2 = x + state.numColumns * ctx.cfg.size;
                explosions.add(new BlastTransition(x, y, x2, y, isWide, ctx) {
                    public void afterEnd()
                    {
                        super.afterEnd();
                        for (Cell to : blasted)
                            to.zap();           
                    }
                });
            }
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
            int r = 1;
            if (c.item instanceof Explody)
                r = ((Explody) c.item).getRadius();
            else if (c.item instanceof Bomb)
            {
                ctx.score.usedBomb();
                r = ((Bomb) c.item).getRadius();
            }
            
            if (r > 1)
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