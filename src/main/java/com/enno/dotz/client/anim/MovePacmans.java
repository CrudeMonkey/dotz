package com.enno.dotz.client.anim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.enno.dotz.client.Cell;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.Direction;
import com.enno.dotz.client.GridState;
import com.enno.dotz.client.SoundManager.Sound;
import com.enno.dotz.client.UserAction;
import com.enno.dotz.client.anim.Transition.DropTransition;
import com.enno.dotz.client.anim.Transition.MovePacmanTransition;
import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.item.Pacman;
import com.enno.dotz.client.item.Spider;

public class MovePacmans
{
    private Context ctx;
    private Config cfg;
    private GridState state;
    private int direction;
    private Collection<Cell> m_verboten;

    public MovePacmans(TransitionList list, Collection<Cell> verboten, Context ctx, UserAction action)
    {
        this.ctx = ctx;
        this.cfg = ctx.cfg;
        this.state = ctx.state;
        
        m_verboten = verboten;
        direction = action.direction; // last action by the user
        
        addTransitions(list, verboten);
    }
    
    private void addTransitions(TransitionList list, Collection<Cell> verboten)
    {
        List<Cell> potentialNeighbors = new ArrayList<Cell>();
        int nr = cfg.numRows;
        int nc = cfg.numColumns;
        
        int dir1 = 0, dir2 = 0;
        if (direction != 0)
        {
            if ((direction & Direction.NORTH) != 0)
                dir1 = Direction.NORTH;
            if ((direction & Direction.SOUTH) != 0)
                dir1 = Direction.SOUTH;
            
            if (dir1 == 0)
            {
                if ((direction & Direction.EAST) != 0)
                    dir1 = Direction.EAST;
                if ((direction & Direction.WEST) != 0)
                    dir1 = Direction.WEST;
            }
            else
            {
                if ((direction & Direction.EAST) != 0)
                    dir2 = Direction.EAST;
                if ((direction & Direction.WEST) != 0)
                    dir2 = Direction.WEST;
            }
        }
        
        boolean first = true;
        for (int row = 0; row < cfg.numRows; row++)
        {
            for (int col = 0; col < cfg.numColumns; col++)
            {
                final Cell src = state.cell(col, row);
                Cell target = null;
                if (!src.isLocked() && src.item instanceof Pacman)
                {
                    final Pacman pacman = (Pacman) src.item;
                    
                    if (dir1 != 0)
                        target = possibleTarget(col, row, dir1);
                    if (target != null && dir2 != 0)
                        target = possibleTarget(col, row, dir2);
                    
                    if (target == null)
                    {
                        potentialNeighbors.clear();
                        if (col > 0)
                            maybeAdd(potentialNeighbors, state.cell(col - 1, row));
                        if (col < nc - 1)
                            maybeAdd(potentialNeighbors, state.cell(col + 1, row));
                        if (row > 0)
                            maybeAdd(potentialNeighbors, state.cell(col, row - 1));
                        if (row < nr - 1)
                            maybeAdd(potentialNeighbors, state.cell(col, row + 1));
                        
                        
                        int n = potentialNeighbors.size();
                        if (n > 0)
                        {
                            if (n > 1)
                            {
                                // Try not to go back to where you just came from
                                int lastDirection = pacman.getDirection();
                                if (lastDirection != Direction.NONE)
                                {
                                    Cell previous = getPreviousCell(src, lastDirection);
                                    if (previous != null && potentialNeighbors.contains(previous))
                                    {
                                        potentialNeighbors.remove(previous);
                                        n--;
                                    }
                                }
                            }
                            
                            n = potentialNeighbors.size();
                            target = potentialNeighbors.get(n == 1 ? 0 : ctx.beastRandom.nextInt(n));
                        }
                    }
                    
                    if (target != null)
                    {
                        verboten.add(src);
                        verboten.add(target);

                        final Cell target_ = target;
                        final boolean first_ = first;
                        first = false;
                        
                        final int pdx = target.col - col;
                        final int pdy = target.row - row;
                        pacman.setDirection(Direction.fromVector(pdx, pdy));
                        
                        if (target.item instanceof Animal || target.item instanceof Spider)
                        {
                            list.add(new MovePacmanTransition(state.x(col), state.y(row), state.x(target.col), state.y(target.row), pacman)
                            {
                                public void afterStart()
                                {
                                    if (first_)
                                        Sound.PACMAN_CHOMP.play(true);
                                    
                                    pacman.removeShapeFromLayer(ctx.dotLayer);
                                    pacman.addShapeToLayer(ctx.nukeLayer);
                                }
                                
                                public void afterEnd()
                                {
                                    if (target_.item instanceof Animal)
                                    {
                                        ctx.score.explodedAnimal(((Animal)target_.item).getColor());
                                    }
                                    else    // Spider
                                    {
                                        ctx.score.explodedSpider();
                                    }
                                    pacman.removeShapeFromLayer(ctx.nukeLayer);
                                    pacman.addShapeToLayer(ctx.dotLayer);
                                    
                                    target_.item.removeShapeFromLayer(ctx.dotLayer);
                                    
                                    src.item = null;
                                    target_.item = pacman;
                                    
                                    if (first_)
                                        Sound.PACMAN_CHOMP.pause();
                                    
                                    Sound.PACMAN_EATS_GHOST.play();
                                }
                            });
                        }
                        else
                        {
                            // Swap pacman with target dot
                            list.add(new MovePacmanTransition(state.x(col), state.y(row), state.x(target.col), state.y(target.row), pacman)
                            {
                                public void afterStart()
                                {
                                    if (first_)
                                        Sound.PACMAN_CHOMP.play(true);
                                    
                                    pacman.removeShapeFromLayer(ctx.dotLayer);
                                    pacman.addShapeToLayer(ctx.nukeLayer);
                                }
                                
                                public void afterEnd()
                                {
                                    pacman.removeShapeFromLayer(ctx.nukeLayer);
                                    pacman.addShapeToLayer(ctx.dotLayer);
                                    
                                    src.item = target_.item;
                                    target_.item = pacman;
                                    
                                    if (first_)
                                        Sound.PACMAN_CHOMP.pause();
                                }
                            });
                            if (target.item != null)
                                list.add(new DropTransition(state.x(target.col), state.y(target.row), state.x(col), state.y(row), target.item));
                        }
                    }
                }
            }
        }
    }

    private void maybeAdd(List<Cell> potentialNeighbors, Cell cell)
    {
        if (possibleTargetCell(cell) != null)
            potentialNeighbors.add(cell);
    }

    private Cell possibleTarget(int col, int row, int direction)
    {
        Pt v = Direction.vector(direction);
        int ncol = col + v.col;
        int nrow = row + v.row;
        if (!state.isValidCell(ncol, nrow))
            return null;
        
        Cell c = state.cell(ncol, nrow);
        return possibleTargetCell(c);
    }
    
    private Cell possibleTargetCell(Cell c)
    { 
        if (c.isLocked() || !c.canContainItems() || m_verboten.contains(c))
            return null;
        
        if (c.item == null)
            return c;
        
        if (c.item instanceof Animal || c.item instanceof Spider)
            return c;
        
        if (!c.item.canSwap())
            return null;
        
        return c;
    }
    
    private Cell getPreviousCell(Cell src, int lastDirection)
    {
        switch (lastDirection)
        {
            case Direction.NORTH: return (src.row + 1 < cfg.numRows) ? state.cell(src.col, src.row + 1) : null;
            case Direction.SOUTH: return (src.row - 1 >= 0) ? state.cell(src.col, src.row - 1) : null;
            case Direction.WEST: return (src.col + 1 < cfg.numColumns) ? state.cell(src.col + 1, src.row) : null;
            case Direction.EAST: return (src.col - 1 >= 0) ? state.cell(src.col - 1, src.row) : null;
        }
        return null;
    }
    
    private int getDirection(Cell src, Cell target)
    {
        if (src.col + 1 == target.col)
            return Direction.EAST;
        if (src.col - 1 == target.col)
            return Direction.WEST;
        if (src.row + 1 == target.row)
            return Direction.SOUTH;
        if (src.row - 1 == target.row)
            return Direction.NORTH;
        
        return -1;
    }
}
