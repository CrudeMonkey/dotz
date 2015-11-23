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
import com.enno.dotz.client.GridState.GetSwapMatches;
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
                
                int col = m_state.col(event.getX());
                int row = m_state.row(event.getY());
                if (!m_state.isValidCell(col, row))
                    return;
                
                double dx = Math.abs(m_state.x(col) - event.getX());
                double dy = Math.abs(m_state.y(row) - event.getY());
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
                
                endSwap();
                
                stop(); // stop listening to user input
                
                ctx.lastMove = new Pt(end.col, end.row);
                
                m_state.processSwapChain(matches, new Runnable() {
                    public void run()
                    {
                        start(); // next move
                    }
                });
            }
        };
        
        m_mouseUpHandler = new NodeMouseUpHandler()
        {                
            @Override
            public void onNodeMouseUp(NodeMouseUpEvent event)
            {
                endSwap();
            }
        };
        
        m_mouseDownHandler = new NodeMouseDownHandler()
        {
            @Override
            public void onNodeMouseDown(NodeMouseDownEvent event)
            {
                int col = m_state.col(event.getX());
                int row = m_state.row(event.getY());
                if (!m_state.isValidCell(col, row))
                    return;
                
                Cell cell = m_state.cell(col, row);                
                
                if (m_specialMode != null)
                {
                    m_specialMode.click(cell);
                    return;
                }
                
                if (flipMirror(cell) || fireRocket(cell) || reshuffle(cell))
                    return;
                
                if (startSpecialMode(cell, event.getX(), event.getY()))
                    return;
                
                if (!(GetSwapMatches.isSwapStart(cell) || cell.item instanceof Knight))
                    return;
                
                m_swapping = true;
                m_start = cell;
                                
                m_lineMoveReg = m_layer.addNodeMouseMoveHandler(m_lineMoveHandler);
                m_mouseUpReg = m_layer.addNodeMouseUpHandler(m_mouseUpHandler);
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
                    endSwap();
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
}