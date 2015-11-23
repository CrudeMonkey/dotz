package com.enno.dotz.client;

import com.ait.lienzo.client.core.animation.LayerRedrawManager;
import com.ait.lienzo.client.core.event.NodeMouseDownHandler;
import com.ait.lienzo.client.core.event.NodeMouseMoveHandler;
import com.ait.lienzo.client.core.event.NodeMouseOutHandler;
import com.ait.lienzo.client.core.event.NodeMouseUpHandler;
import com.ait.lienzo.client.core.shape.Layer;
import com.enno.dotz.client.DragConnectMode.ColorBombMode;
import com.enno.dotz.client.DragConnectMode.DropMode;
import com.enno.dotz.client.DragConnectMode.ExplodyMode;
import com.enno.dotz.client.DragConnectMode.IcePickMode;
import com.enno.dotz.client.DragConnectMode.KeyMode;
import com.enno.dotz.client.DragConnectMode.ReshuffleMode;
import com.enno.dotz.client.DragConnectMode.SpecialMode;
import com.enno.dotz.client.DragConnectMode.TurnMode;
import com.enno.dotz.client.DragConnectMode.WildCardMode;
import com.enno.dotz.client.SoundManager.Sound;
import com.enno.dotz.client.item.ColorBomb;
import com.enno.dotz.client.item.Drop;
import com.enno.dotz.client.item.Explody;
import com.enno.dotz.client.item.IcePick;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.Key;
import com.enno.dotz.client.item.Mirror;
import com.enno.dotz.client.item.Rocket;
import com.enno.dotz.client.item.Turner;
import com.enno.dotz.client.item.Wild;
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
    protected SpecialMode m_specialMode;
    
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
        
        ctx.boostPanel.setActive(true);
    }
    
    public void stop()
    {
        ctx.boostPanel.setActive(false);
        
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
            ((Mirror) cell.item).rotate(1);
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

    protected boolean startSpecialMode(Cell cell, int x, int y)
    {
        if (cell.isLocked() || cell.item == null)
            return false;
        
        if (cell.item instanceof Turner)
        {
            m_specialMode = new TurnMode(cell, x, y, ctx, this);
            return true;
        }
        else if (cell.item instanceof Drop)
        {
            m_specialMode = new DropMode(cell, x, y, ctx, this);
            return true;
        }
        else if (cell.item instanceof Key)
        {
            m_specialMode = new KeyMode(cell, x, y, ctx, this);
            return true;
        }
        else if (cell.item instanceof IcePick)
        {
            m_specialMode = new IcePickMode(cell, x, y, ctx, this);
            return true;
        }
        else if (cell.item instanceof ColorBomb && this instanceof DragConnectMode)
        {
            m_specialMode = new ColorBombMode(cell, x, y, ctx, this);
            return true;
        }
        
        return false;
    }
    
    public void startBoostMode(Item item, Runnable removeItem)
    {
        if (m_specialMode != null)
        {
            m_specialMode.cancel();
        }
        
        if (item instanceof Turner)
        {
            m_specialMode = new TurnMode(item, ctx, this, removeItem);
        }
        else if (item instanceof Drop)
        {
            m_specialMode = new DropMode(item, ctx, this, removeItem);
        }
        else if (item instanceof ColorBomb)
        {
            m_specialMode = new ColorBombMode(item, ctx, this, removeItem);
        }
        else if (item instanceof Wild)
        {
            m_specialMode = new WildCardMode(item, ctx, this, removeItem);
        }
        else if (item instanceof Explody)
        {
            m_specialMode = new ExplodyMode(item, ctx, this, removeItem);
        }
        else if (item instanceof YinYang)
        {
            m_specialMode = new ReshuffleMode(item, ctx, this, removeItem);
        }
        else if (item instanceof Key)
        {
            m_specialMode = new KeyMode(item, ctx, this, removeItem);
        }
        else if (item instanceof IcePick)
        {
            m_specialMode = new IcePickMode(item, ctx, this, removeItem);
        }
    }
    
    public void cancelSpecialMode()
    {
        m_specialMode = null;
    }

    public void cancelBoostMode()
    {
        if (m_specialMode != null)
        {
            m_specialMode.cancel();
            m_specialMode = null;
        }
    }
}
