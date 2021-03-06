package com.enno.dotz.client.anim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.enno.dotz.client.Cell;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.Direction;
import com.enno.dotz.client.GridState;
import com.enno.dotz.client.anim.Transition.DropTransition;
import com.enno.dotz.client.anim.Transition.MoveAnimalTransition;
import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.item.Animal.Action;
import com.enno.dotz.client.item.Domino;
import com.enno.dotz.client.item.Dot;
import com.enno.dotz.client.item.DotBomb;

public class MoveAnimals
{
    private Context ctx;
    private Config cfg;
    private GridState state;
    
    private Set<Cell> m_animalTargets = new HashSet<Cell>();
    private Collection<Cell> m_verboten;
    
    public MoveAnimals(TransitionList list, Collection<Cell> verboten, Context ctx)
    {
        this.ctx = ctx;
        this.cfg = ctx.cfg;
        this.state = ctx.state;
        m_verboten = verboten;
        
        addTransitions(list, verboten);
    }
    
    private void addTransitions(TransitionList list, Collection<Cell> verboten)
    {            
        List<Cell> potentialNeighbors = new ArrayList<Cell>();
        int nr = cfg.numRows;
        int nc = cfg.numColumns;
        Random rnd = ctx.generator.getRandom();
        
        for (int row = 0; row < nr; row++)
        {
            for (int col = 0; col < nc; col++)
            {
                final Cell src = state.cell(col, row);
                if (!src.isLocked() && src.item instanceof Animal && !m_verboten.contains(src))
                {
                    final Animal animal = (Animal) src.item;
                    if (animal.isStunned())
                        continue;
                    
                    Action action = animal.getAction();
                    
                    potentialNeighbors.clear();
                    if (row < nr - 1)
                        maybeAdd(potentialNeighbors, state.cell(col, row + 1), action);
                    if (row > 0)
                        maybeAdd(potentialNeighbors, state.cell(col, row - 1), action);
                    if (col > 0)
                        maybeAdd(potentialNeighbors, state.cell(col - 1, row), action);
                    if (col < nc - 1)
                        maybeAdd(potentialNeighbors, state.cell(col + 1, row), action);
                    
                    int n = potentialNeighbors.size();
                    if (n > 0)
                    {
                        if (n > 1)
                        {
                            Pt lastMove = ctx.lastMove;
                            if (animal.getAnimalType() == Animal.Type.FOLLOW && lastMove != null)
                            {
                                potentialNeighbors = findClosestMove(src, potentialNeighbors, lastMove);
                            }
                            else if (animal.getAnimalType() == Animal.Type.SCARED && lastMove != null)
                            {
                                potentialNeighbors = findFurthestMove(src, potentialNeighbors, lastMove);
                            }
                            else
                            {
                                // Try not to go back to where you just came from
                                int lastDirection = animal.lastDirection;
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
                        }
                        
                        n = potentialNeighbors.size();
                        final Cell target = potentialNeighbors.get(n == 1 ? 0 : rnd.nextInt(n));
                        animal.lastDirection = getDirection(src, target);
                        m_animalTargets.add(target);
                        
                        verboten.add(src);
                        verboten.add(target);
                        
                        if (action == Action.SWAP) //ctx.generator.swapMode)
                        {
                            // Swap animal with target dot
                            list.add(new MoveAnimalTransition(state.x(col), state.y(row), state.x(target.col), state.y(target.row), animal)
                            {
                                public void afterStart()
                                {
                                    animal.removeShapeFromLayer(ctx.dotLayer);
                                    animal.addShapeToLayer(ctx.nukeLayer);
                                }
                                
                                public void afterEnd()
                                {
                                    animal.removeShapeFromLayer(ctx.nukeLayer);
                                    animal.addShapeToLayer(ctx.dotLayer);
                                    
                                    src.item = target.item;
                                    target.item = animal;
                                }
                            });
                            list.add(new DropTransition(state.x(target.col), state.y(target.row), state.x(col), state.y(row), target.item));
                        }
                        else if (action == Action.BOMBIFY)
                        {
                            // Swap animal with target dot
                            list.add(new MoveAnimalTransition(state.x(col), state.y(row), state.x(target.col), state.y(target.row), animal)
                            {
                                public void afterStart()
                                {
                                    animal.removeShapeFromLayer(ctx.dotLayer);
                                    animal.addShapeToLayer(ctx.nukeLayer);
                                }
                                
                                public void afterEnd()
                                {
                                    animal.removeShapeFromLayer(ctx.nukeLayer);
                                    animal.addShapeToLayer(ctx.dotLayer);
                                    
                                    if (target.item instanceof Dot)
                                    {
                                        DotBomb bomb = new DotBomb((Dot) target.item, 10, false); //TODO strength
                                        bomb.init(ctx);
                                        bomb.moveShape(state.x(src.col), state.y(src.row));
                                        
                                        target.item.removeShapeFromLayer(ctx.dotLayer);
                                        bomb.addShapeToLayer(ctx.dotLayer);
                                        target.item = bomb;
                                        // no need to track Dot or DotBomb count
                                    }
                                    
                                    src.item = target.item;
                                    target.item = animal;
                                }
                            });
                            list.add(new DropTransition(state.x(target.col), state.y(target.row), state.x(col), state.y(row), target.item));
                        }
                        else if (ctx.generator.dominoMode)
                        {
                            int pip = animal.getStrength() % (ctx.generator.maxDomino + 1);
                            boolean vertical = col == target.col; 
                            final Domino newDot = new Domino(pip, pip, vertical, false);
                            
                            newDot.init(ctx);
                            newDot.moveShape(state.x(col), state.y(row));
                            
                            list.add(new MoveAnimalTransition(state.x(col), state.y(row), state.x(target.col), state.y(target.row), animal)
                            {
                                public void afterStart()
                                {
                                    newDot.addShapeToLayer(ctx.dotLayer);
                                    
                                    animal.removeShapeFromLayer(ctx.dotLayer);
                                    animal.addShapeToLayer(ctx.nukeLayer);
                                }
                                
                                public void afterEnd()
                                {
                                    if (target.item != null)
                                    {
                                        target.item.removeShapeFromLayer(ctx.dotLayer);
                                        ctx.score.ate(target.item);
                                    }
    
                                    src.item = newDot;
                                    target.item = animal;
                                    
                                    animal.removeShapeFromLayer(ctx.nukeLayer);
                                    animal.addShapeToLayer(ctx.dotLayer);
                                }
                            });
                        }
                        else // Animal.Action.DEFAULT
                        {
                            int color = animal.isBlack() ? ctx.generator.getNextDotColor() : animal.getColor();
                            
                            final Dot newDot = new Dot(color);
                            if (ctx.generator.generateLetters)
                                newDot.setLetter(ctx.generator.nextLetter());
                            
                            newDot.init(ctx);
                            newDot.moveShape(state.x(col), state.y(row));
                            
                            list.add(new MoveAnimalTransition(state.x(col), state.y(row), state.x(target.col), state.y(target.row), animal)
                            {
                                public void afterStart()
                                {
                                    newDot.addShapeToLayer(ctx.dotLayer);
                                    
                                    animal.removeShapeFromLayer(ctx.dotLayer);
                                    animal.addShapeToLayer(ctx.nukeLayer);
                                }
                                
                                public void afterEnd()
                                {
                                    if (target.item != null)
                                    {
                                        target.item.removeShapeFromLayer(ctx.dotLayer);
                                        ctx.score.ate(target.item);
                                    }
    
                                    src.item = newDot;
                                    target.item = animal;
                                    
                                    animal.addShapeToLayer(ctx.dotLayer);
                                    animal.removeShapeFromLayer(ctx.nukeLayer);
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    private List<Cell> findClosestMove(Cell src, List<Cell> potentialNeighbors, Pt lastMove)
    {
        int n = potentialNeighbors.size();
        List<Cell> newList = new ArrayList<Cell>();
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < n; i++)
        {
            Cell c = potentialNeighbors.get(i);
            int d = Math.abs(lastMove.col - c.col) + Math.abs(lastMove.row - c.row);    // taxicab distance
            if (d < min)
            {
                min = d;
                newList.clear();
                newList.add(c);
            }
            else if (d == min)
            {
                newList.add(c);
            }
        }
        return newList;
    }

    private List<Cell> findFurthestMove(Cell src, List<Cell> potentialNeighbors, Pt lastMove)
    {
        int n = potentialNeighbors.size();
        List<Cell> newList = new ArrayList<Cell>();        
        int max = 0;
        for (int i = 0; i < n; i++)
        {
            Cell c = potentialNeighbors.get(i);            
            int d = Math.abs(lastMove.col - c.col) + Math.abs(lastMove.row - c.row);    // taxicab distance
            if (d > max)
            {
                max = d;
                newList.clear();
                newList.add(c);
            }
            else if (d == max)
            {
                newList.add(c);
            }
        }
        return newList;
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

    private void maybeAdd(List<Cell> potentialNeighbors, Cell cell, Action action)
    {
        if (m_animalTargets.contains(cell) || m_verboten.contains(cell))
            return; // another animal is already moving here
        
        if (cell.isLocked() || !cell.canContainItems())
            return; // can't move into locked Door
        
        if (cell.item == null)
            return; // can't move into empty cell
        
        if (action == Animal.Action.DEFAULT && !cell.item.canBeEaten())
            return; 
        
        if ((action == Animal.Action.SWAP || action == Animal.Action.BOMBIFY) && !cell.item.canSwap())
            return;
        
        potentialNeighbors.add(cell);
    }
}