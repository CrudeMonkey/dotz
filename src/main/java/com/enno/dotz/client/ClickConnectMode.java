package com.enno.dotz.client;

import com.ait.lienzo.client.core.event.NodeMouseDownEvent;
import com.ait.lienzo.client.core.event.NodeMouseDownHandler;
import com.ait.lienzo.client.core.event.NodeMouseOutEvent;
import com.ait.lienzo.client.core.event.NodeMouseOutHandler;
import com.ait.lienzo.client.core.shape.Layer;
import com.enno.dotz.client.DragConnectMode.ColorBombMode;
import com.enno.dotz.client.SoundManager.Sound;
import com.enno.dotz.client.item.Blaster;
import com.enno.dotz.client.item.Dot;
import com.enno.dotz.client.item.DotBomb;
import com.enno.dotz.client.item.Item;

public class ClickConnectMode extends ConnectMode
{
    private Cell m_start;
    private double m_margin;
    
    public ClickConnectMode(final Context ctx, Layer layer)
    {
        super(ctx, layer);
        
        //TODO replace with GWT handlers! - don't need Node detection here!
//        m_lineMoveHandler = new NodeMouseMoveHandler() {
//
//            @Override
//            public void onNodeMouseMove(NodeMouseMoveEvent event)
//            {
//                 
//                int col = m_state.col(event.getX());
//                int row = m_state.row(event.getY());
//                if (!m_state.isValidCell(col, row))
//                    return;
//                
//                double dx = Math.abs(m_state.x(col) - event.getX());
//                double dy = Math.abs(m_state.y(row) - event.getY());
//                if (dx < m_margin && dy < m_margin)
//                    return;
//                
//                if (m_start.item instanceof Knight)
//                {
//                    int dcol = Math.abs(m_start.col - col);
//                    int drow = Math.abs(m_start.row - row);
//                    if (!(dcol == 2 && drow == 1 || dcol == 1 && drow == 2))
//                        return;
//                }
//                else
//                {                
//                    if (m_start.col == col)
//                    {
//                        if (m_start.row != row - 1 && m_start.row != row + 1)
//                            return;
//                    }
//                    else if (m_start.row == row)
//                    {
//                        if (m_start.col != col - 1 && m_start.col != col + 1)
//                            return;
//                    }
//                    else
//                        return;
//                }
//                
//                GetSwapMatches matches = new GetSwapMatches(ctx);
//                Cell end = m_state.cell(col, row);
//                if (!matches.canSwap(m_start, end))
//                    return;
//                
//                endSwap();
//                
//                stop(); // stop listening to user input
//                
//                ctx.lastMove = new Pt(end.col, end.row);
//                
//                m_state.processSwapChain(matches, new Runnable() {
//                    public void run()
//                    {
//                        start(); // next move
//                    }
//                });
//            }
//        };
        
//        m_mouseUpHandler = new NodeMouseUpHandler()
//        {                
//            @Override
//            public void onNodeMouseUp(NodeMouseUpEvent event)
//            {
//                endSwap();
//            }
//        };
        
        m_mouseDownHandler = new NodeMouseDownHandler()
        {
            @Override
            public void onNodeMouseDown(NodeMouseDownEvent event)
            {
                int col = m_state.col(event.getX());
                int row = m_state.row(event.getY());
                if (!m_state.isValidCell(col, row))
                {
                    Sound.MISS.play();
                    return;
                }
                
                Cell cell = m_state.cell(col, row);                
                
                if (m_specialMode != null)
                {
                    m_specialMode.click(cell);
                    return;
                }
                
                //TODO knight?
                
                if (isTriggeredBySingleClick2(cell))
                    return;
                
                if (isTriggeredMerge(cell))
                    return;
                
                if (igniteBlaster(cell) || igniteBomb(cell))
                    return;
                
                if (startSpecialMode(cell, event.getX(), event.getY()))
                {
                    if (m_specialMode instanceof ColorBombMode)
                    {
                        Cell c = m_state.getRandomDotCell();
                        //TODO animate
                        m_specialMode.done(c, false);
                    }
                    return;
                }
                
                CellList matches = clickCell(cell);
                if (matches == null)
                {
                    Sound.MISS.play();
                    return;
                }
                
                stop();
                
                UserAction action = new UserAction(matches);
                m_state.processChain(action, new Runnable()
                {
                    public void run()
                    {
                        start(); // next move
                    }
                });

                
//                m_start = cell;
//                                
//                m_lineMoveReg = m_layer.addNodeMouseMoveHandler(m_lineMoveHandler);
//                m_mouseUpReg = m_layer.addNodeMouseUpHandler(m_mouseUpHandler);
            }                
        };
        
        m_mouseOutHandler = new NodeMouseOutHandler() {
            @Override
            public void onNodeMouseOut(NodeMouseOutEvent event)
            {
                if (m_specialMode != null)
                {
                    m_specialMode.mouseOut();
                    return;
                }
                
//                if (m_swapping)
//                {
//                    Sound.MISS.play();
//                    endSwap();
//                }
            }
        };
    }
//    
//    protected void endSwap()
//    {
//
//        
//        m_lineMoveReg.removeHandler();
//        m_mouseUpReg.removeHandler();        
//    }
    
    protected CellList clickCell(Cell cell)
    {
        Item item = cell.item;
        if (item == null || cell.isLockedDoor())
            return null;
        
        if (!(item instanceof Dot || item instanceof DotBomb))
            return null;
        
        CellList list = new CellList();
        list.add(cell);
        int color = item.getColor();
        
        addNeighbors(cell.col, cell.row, color, list);
        if (list.size() < ctx.generator.minChainLength)
            return null;
        
        return list;
    }
    
    private void addNeighbors(int col, int row, int color, CellList list)
    {
        Cell cell = m_state.cell(col, row);
        
        addNeighbor(cell, col - 1, row, color, list);
        addNeighbor(cell, col + 1, row, color, list);
        addNeighbor(cell, col, row - 1, color, list);
        addNeighbor(cell, col, row + 1, color, list);
        
        if (ctx.generator.diagonalMode)
        {
            addNeighbor(cell, col - 1, row - 1, color, list);
            addNeighbor(cell, col + 1, row - 1, color, list);
            addNeighbor(cell, col - 1, row + 1, color, list);
            addNeighbor(cell, col + 1, row + 1, color, list);
        }
    }

    private void addNeighbor(Cell prev, int col, int row, int color, CellList list)
    {
        if (!m_state.isValidCell(col, row) || list.didCell(col, row))
            return;
        
        Cell cell = m_state.cell(col, row);
        if (!cell.canConnect(color, false))
            return;
        
        if (cell.isLockedCage() && prev.isLockedCage())
            return; // can't connect 2 locked cages
        
        list.add(cell);
        addNeighbors(cell.col, cell.row, color, list);
    }
}