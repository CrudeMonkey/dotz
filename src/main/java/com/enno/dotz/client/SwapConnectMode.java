package com.enno.dotz.client;

import java.util.ArrayList;
import java.util.List;

import com.ait.lienzo.client.core.animation.LayerRedrawManager;
import com.ait.lienzo.client.core.event.NodeMouseDownEvent;
import com.ait.lienzo.client.core.event.NodeMouseDownHandler;
import com.ait.lienzo.client.core.event.NodeMouseMoveEvent;
import com.ait.lienzo.client.core.event.NodeMouseMoveHandler;
import com.ait.lienzo.client.core.event.NodeMouseOutEvent;
import com.ait.lienzo.client.core.event.NodeMouseOutHandler;
import com.ait.lienzo.client.core.event.NodeMouseUpEvent;
import com.ait.lienzo.client.core.event.NodeMouseUpHandler;
import com.ait.lienzo.client.core.shape.Layer;
import com.enno.dotz.client.SoundManager.Sound;
import com.enno.dotz.client.anim.Pt;
import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.item.Dot;
import com.enno.dotz.client.item.DotBomb;
import com.enno.dotz.client.item.Knight;
import com.enno.dotz.client.item.Mirror;
import com.enno.dotz.client.item.Rocket;
import com.enno.dotz.client.item.YinYang;
import com.enno.dotz.client.util.Debug;
import com.google.gwt.event.shared.HandlerRegistration;

public class SwapConnectMode extends ConnectMode
{
    private boolean m_swapping;
    private Cell m_start;
    private double m_margin;
    
    public SwapConnectMode(final Context ctx, Layer layer)
    {
        super(ctx, layer);
                
        //TODO replace with GWT handlers! - don't need Node detection here!
        m_lineMoveHandler = new NodeMouseMoveHandler() {

            @Override
            public void onNodeMouseMove(NodeMouseMoveEvent event)
            {
                if (!m_swapping)
                    return;
                
                int eventX = event.getX();
                int eventY = event.getY();
                int col = m_state.col(eventX);
                int row = m_state.row(eventY);
                if (!m_state.isValidCell(col, row))
                    return;
                
                mouseMove(eventX, eventY, col, row);
            }
        };
        
        m_mouseUpHandler = new NodeMouseUpHandler()
        {                
            @Override
            public void onNodeMouseUp(NodeMouseUpEvent event)
            {
                mouseUp();
            }
        };
        
        m_mouseDownHandler = new NodeMouseDownHandler()
        {
            @Override
            public void onNodeMouseDown(NodeMouseDownEvent event)
            {
                int eventX = event.getX();
                int eventY = event.getY();
                int col = m_state.col(eventX);
                int row = m_state.row(eventY);
                if (!m_state.isValidCell(col, row))
                    return;
                
                Cell cell = m_state.cell(col, row);                
                
                mouseDown(eventX, eventY, cell);
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
                
                if (m_swapping)
                {
                    Sound.MISS.play();
                    mouseUp();
                }
            }
        };
    }
    
    protected void endSwap()
    {
        m_swapping = false;
        
        m_lineMoveReg.removeHandler();
        m_mouseUpReg.removeHandler();        
    }

    @Override
    protected void mouseDown(Cell c)
    {
        mouseDown((int) m_state.x(c.col), (int) m_state.y(c.row), c);
    }
    
    protected void mouseDown(int eventX, int eventY, Cell cell)
    {
        if (m_specialMode != null)
        {
            m_specialMode.click(cell);
            return;
        }
        
        if (isTriggeredBySingleClick(cell))
            return;
        
        if (startSpecialMode(cell, eventX, eventY))
            return;
        
        if (!GetSwapMatches.isSwapStart(cell))
//                if (!(GetSwapMatches.isSwapStart(cell) || cell.item instanceof Knight))
        
//                if (cell.item == null || !cell.item.canSwap())
            return;
        
        m_swapping = true;
        m_start = cell;
                        
        m_lineMoveReg = m_layer.addNodeMouseMoveHandler(m_lineMoveHandler);
        m_mouseUpReg = m_layer.addNodeMouseUpHandler(m_mouseUpHandler);
    }

    @Override
    protected void mouseMove(Cell c)
    {
        mouseMove((int) m_state.x(c.col), (int) m_state.y(c.row), c.col, c.row);
    }
    
    protected void mouseMove(int eventX, int eventY, int col, int row)
    {
        double dx = Math.abs(m_state.x(col) - eventX);
        double dy = Math.abs(m_state.y(row) - eventY);
        if (dx < m_margin && dy < m_margin)
            return;
        
        if (m_start.item instanceof Knight)
        {
            int dcol = Math.abs(m_start.col - col);
            int drow = Math.abs(m_start.row - row);
            if (!(dcol == 2 && drow == 1 || dcol == 1 && drow == 2))
                return;
        }
        else
        {                
            if (m_start.col == col)
            {
                if (m_start.row != row - 1 && m_start.row != row + 1)
                    return;
            }
            else if (m_start.row == row)
            {
                if (m_start.col != col - 1 && m_start.col != col + 1)
                    return;
            }
            else
                return;
        }
        
        GetSwapMatches matches = new GetSwapMatches(ctx);
        Cell end = m_state.cell(col, row);
        if (!matches.canSwap(m_start, end))
            return;
        
        mouseUp();
        
        stop(); // stop listening to user input
        
        ctx.lastMove = end.pt();
        if (ctx.isRecording())
            ctx.recorder.swap(m_start, end);
        
        UserAction action = new UserAction();
        action.direction = Direction.fromXY(m_start.col, m_start.row, col, row);
        
        m_state.processSwapChain(action, matches, new Runnable() {
            @Override
            public void run()
            {
                start(); // next move
            }
        });
    }

    @Override
    protected void mouseUp()
    {
        endSwap();
    }
}