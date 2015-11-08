package com.enno.dotz.client;

import java.util.ArrayList;

import com.enno.dotz.client.Cell.ConveyorCell;
import com.enno.dotz.client.Conveyors.Conveyor;
import com.enno.dotz.client.anim.Pt;
import com.enno.dotz.client.anim.Transition.DropTransition;
import com.enno.dotz.client.anim.Transition.RotateTransition;
import com.enno.dotz.client.anim.TransitionList;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.TeleportClipBox;

public class Conveyors extends ArrayList<Conveyor>
{
    private GridState m_state;
    
    public static class ConveyorException extends Exception
    {
        public ConveyorException(String msg)
        {
            super(msg);
        }
    }
    
    public static class Conveyor extends ArrayList<ConveyorCell>
    {
        public void addTransitions(TransitionList list, final GridState state, final Context ctx)
        {
            final int n = size();
            final Item[] items = new Item[n];
            for (int i = 0; i < n; i++)
            {
                items[i] = get(i).item;
            }
            
            boolean first = true;
            for (int i = 0; i < n; i++)
            {
                if (items[i] == null)
                    continue;
                                
                ConveyorCell src = get(i);
                int to = (i + 1) % n;
                final ConveyorCell target = get(to);
                
                // Move all items in the first transition
                // We can't move them each in their own transition, because some source cells may be empty and won't have a transition,
                // and it would leave extra shapes behind.
                final Runnable moveItems = first ? new Runnable() {
                    @Override
                    public void run()
                    {
                        for (int j = 0; j < n; j++)
                        {
                            int to = (j + 1) % n;
                            get(to).item = items[j];                                
                        }
                    }
                } : null;
                first = false;                
                
                if (Math.abs(src.col - target.col) <= 1 && Math.abs(src.row - target.row) <= 1)
                {
                    list.add(new RotateTransition(state.x(src.col), state.y(src.row), state.x(target.col), state.y(target.row), items[i]) {
                        @Override
                        public void afterEnd()
                        {
                            if (moveItems != null)
                                moveItems.run();
                        }
                    });
                }
                else // wrapping around
                {
                    Pt v = Direction.vector(src.getExitDirection());
                    
                    final Item origItem = items[i];
                    final Item clone = origItem.copy();
                    clone.init(ctx);
                    
                    final TeleportClipBox fromBox = new TeleportClipBox(origItem.shape, src, ctx);
                    final TeleportClipBox toBox = new TeleportClipBox(clone.shape, target, ctx);

                    list.add(new RotateTransition(state.x(src.col), state.y(src.row), state.x(src.col + v.col), state.y(src.row + v.row), origItem));
                                        
                    list.add(new RotateTransition(state.x(target.col - v.col), state.y(target.row - v.row), state.x(target.col), state.y(target.row), clone) {
                        public void afterStart()
                        {
                            clone.addShapeToLayer(ctx.dotLayer);
                            fromBox.init();
                            toBox.init();
                        }
                        
                        public void afterEnd()
                        {
                            origItem.moveShape(state.x(target.col), state.y(target.row));
                            fromBox.done();
                            toBox.done();
                            clone.removeShapeFromLayer(ctx.dotLayer);
                            
                            if (moveItems != null)
                                moveItems.run();
                        }
                    });
                }
            }
        }
    }
    
    protected Conveyor getConveyor(ConveyorCell c)
    {
        for (Conveyor g : this)
        {
            if (g.contains(c))
                return g;
        }
        return null;
    }
    
    public Conveyors(GridState state) throws ConveyorException
    {
        makeConnections(state);
    }
    
    private void makeConnections(GridState state) throws ConveyorException
    {
        m_state = state;
        
        for (int row = 0; row < m_state.numRows; row++)
        {
            for (int col = 0; col < m_state.numColumns; col++)
            {
                Cell cell = state.cell(col, row);
                if (cell instanceof ConveyorCell)
                {
                    ConveyorCell cc = (ConveyorCell) cell;
                    Conveyor c = getConveyor(cc);
                    if (c != null)
                        continue;
                    
                    c = new Conveyor();
                    c.add(cc);
                    add(c);
                    
                    int dir = 0;
                    while (true)
                    {
                        dir = cc.getExitDirection();
                        
                        Cell newCell = nextCell(cc, dir);
                        if (!(newCell instanceof ConveyorCell))
                            throw new ConveyorException("Conveyor at " + cc.col + "," + cc.row + " doesn't connect to a Conveyor");
                        
                        ConveyorCell newCc = (ConveyorCell) newCell;
                        if (newCc.getDirection() != dir)
                            throw new ConveyorException("Conveyor at " + cc.col + "," + cc.row + " doesn't match Conveyor at " + newCc.col + "," + newCc.row);
                        
                        Conveyor newCon = getConveyor(newCc);
                        if (newCon == null)
                        {
                            c.add(newCc);
                            cc = newCc;
                        }
                        else if (newCon != c)
                        {
                            throw new ConveyorException("Conveyor at " + cc.col + "," + cc.row + " connects to another conveyor belt");                            
                        }
                        else if (newCc == c.get(0))
                        {
                            // Finished loop
                            break;
                        }
                        else
                        {
                            // shouldn't happen
                            throw new ConveyorException("Conveyor at " + cc.col + "," + cc.row + " is weird");
                        }
                    }
                }
            }
        }
    }
    
    protected Cell nextCell(Cell c, int dir)
    {
        Pt v = Direction.vector(dir);
        int col = (c.col + v.col + m_state.numColumns) % m_state.numColumns;
        int row = (c.row + v.row + m_state.numRows) % m_state.numRows;
        return m_state.cell(col,  row);
    }
}
