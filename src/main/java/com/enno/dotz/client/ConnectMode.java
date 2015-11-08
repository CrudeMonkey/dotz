package com.enno.dotz.client;

import com.ait.lienzo.client.core.animation.LayerRedrawManager;
import com.ait.lienzo.client.core.event.NodeMouseDownHandler;
import com.ait.lienzo.client.core.event.NodeMouseMoveHandler;
import com.ait.lienzo.client.core.event.NodeMouseOutHandler;
import com.ait.lienzo.client.core.event.NodeMouseUpHandler;
import com.ait.lienzo.client.core.shape.Layer;
import com.enno.dotz.client.SoundManager.Sound;
import com.enno.dotz.client.item.Mirror;
import com.enno.dotz.client.item.Rocket;
import com.enno.dotz.client.item.YinYang;
import com.google.gwt.event.shared.HandlerRegistration;

public abstract class ConnectMode
{
    protected Layer m_layer;
    protected GridState m_state;
    
    protected NodeMouseDownHandler m_mouseDownHandler;
    protected NodeMouseMoveHandler m_lineMoveHandler;
    protected NodeMouseUpHandler   m_mouseUpHandler;
    protected NodeMouseOutHandler m_mouseOutHandler;
    
    protected HandlerRegistration m_mouseDownReg;
    protected HandlerRegistration m_lineMoveReg;
    protected HandlerRegistration m_mouseUpReg;
    protected HandlerRegistration m_mouseOutReg;

    protected Context ctx;
    protected Config cfg;
    
    protected ConnectMode(Context ctx, Layer layer)
    {
        this.ctx = ctx;
        this.cfg = ctx.cfg;
        m_layer = layer;
        m_state = ctx.state;
    }
    
    /**
     * Start listening to user input (mouse down)
     */
    public void start()
    {
        m_mouseDownReg = m_layer.addNodeMouseDownHandler(m_mouseDownHandler);
        m_mouseOutReg = m_layer.addNodeMouseOutHandler(m_mouseOutHandler);
    }
    
    public void stop()
    {
        m_mouseDownReg.removeHandler();
        m_mouseOutReg.removeHandler();
    }
    
    protected void redraw()
    {
        LayerRedrawManager.get().schedule(m_layer);
    }
    
    protected boolean reshuffle(Cell cell)
    {
        if (!cell.isLocked() && cell.item instanceof YinYang)
        {
            stop();

            Sound.RESHUFFLE.play();
            
            ctx.dotLayer.remove(cell.item.shape);
            cell.item = null;
            
            ctx.state.reshuffle();
                        
            m_state.processChain(new Runnable() {
                public void run()
                {
                    start(); // next move
                }
            });
            
            return true;
        }
        return false;
    }
    
    protected boolean flipMirror(Cell cell)
    {
        if (!cell.isLocked() && cell.item instanceof Mirror)
        {
            ctx.state.activateLasers(false);
            ((Mirror) cell.item).rotate();
            Sound.FLIP_MIRROR.play();
            ctx.dotLayer.draw();
            ctx.state.activateLasers(true);
            
            stop();
            
            m_state.processChain(new Runnable() {
                public void run()
                {
                    start(); // next move
                }
            });
            
            return true;
        }
        return false;
    }
    
    protected boolean fireRocket(Cell cell)
    {
        if (!cell.isLocked() && cell.item instanceof Rocket)
        {
            stop();
            m_state.fireRocket(cell, new Runnable() {
                public void run()
                {
                    start(); // next move
                }
            });
            return true;
        }
        return false;
    }    

}
