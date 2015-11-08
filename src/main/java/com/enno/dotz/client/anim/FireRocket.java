package com.enno.dotz.client.anim;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ait.lienzo.client.core.animation.AbstractAnimation;
import com.ait.lienzo.client.core.animation.TimedAnimation;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.types.Point2D;
import com.enno.dotz.client.Cell;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.Direction;
import com.enno.dotz.client.GridState;
import com.enno.dotz.client.SoundManager.Sound;
import com.enno.dotz.client.anim.Transition.DropTransition;
import com.enno.dotz.client.item.Explody;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.Rocket;
import com.enno.dotz.client.util.CallbackChain;
import com.enno.dotz.client.util.CallbackChain.Callback;
import com.enno.dotz.client.util.ParallelAnimation;

public class FireRocket
{
    private CallbackChain m_chain;
    private Set<Pt> m_rocketLocations = new HashSet<Pt>();
    private Context ctx;
    private GridState m_state;
    
    public FireRocket(Cell cell, final Runnable nextMoveCallback, Context ctx)
    {
        this.ctx = ctx;
        m_state = ctx.state;
        
        m_chain = new CallbackChain()
        {
            public void done()
            {
                m_state.processChain(nextMoveCallback);
            }
        };
        
        doRocket(cell, 0);
        
        m_chain.run();
    }
    
    public void doRocket(Cell cell, int n)
    {
        final Rocket rocket = (Rocket) cell.item;
        
        int direction = rocket.getDirection();
        Pt d = Direction.vector(direction);
     
        List<Cell> path = new ArrayList<Cell>();
        path.add(cell);
        
        int col = cell.col;
        int row = cell.row;
        m_rocketLocations.add(new Pt(col, row));
        
        cell.item = null;
        
        Point2D ig = rocket.getIgnitionPoint();
        double x = m_state.x(col) + ig.getX();
        double y = m_state.y(row) + ig.getY();
        
        for (int i = 0; i < 3; i++)
            animateIgnition(n++, x, y, i == 0);
        
        boolean first = true;
        while (true)
        {
            int ncol = col + d.col;
            int nrow = row + d.row;
            if (!m_state.isValidCell(ncol, nrow))
            {
                // Reached border
                double s2 = ctx.cfg.size / 2;
                animate(n, col, row, m_state.x(col) + d.col * s2, m_state.y(row) + d.row * s2, rocket, first, true);
                break;
            }
            
            if (m_state.cell(ncol, nrow).item instanceof Rocket)
            {
                doRocket(m_state.cell(ncol, nrow), n);
            }
            
            animate(n, col, row, m_state.x(ncol), m_state.y(nrow), rocket, first, false);
            first = false;
            
            row = nrow;
            col = ncol;
            n++;
        }
    }
    
    protected void animate(int n, final int col, final int row, final double x2, final double y2, final Item item, final boolean first, final boolean last)
    {            
        getRocketCallback(n).animate(col, row, x2, y2, item, first, last);
    }
    
    protected RocketCallback getRocketCallback(int n)
    {
        List<Callback> cbs = m_chain.getCallbacks();
        RocketCallback cb = null;
        if (cbs.size() <= n)
        {
            cb = new RocketCallback(ctx.laserLayer);
            m_chain.add(cb);
        }
        else
        {
            cb = (RocketCallback) cbs.get(n);
        }
        return cb;
    }
    
    protected void animateIgnition(int n, double x, double y, boolean playSound)
    {
        getRocketCallback(n).animateIgnition(x, y, playSound);
    }
    
    public class RocketCallback extends Callback
    {
        private ParallelAnimation m_list;
        
        public RocketCallback(Layer layer)
        {
            m_list = new ParallelAnimation(layer)
            {
                @Override
                public void done()
                {
                    doNext();
                }  
            };
        }            

        public void animate(final int col, final int row, final double x2, final double y2, final Item item, final boolean first, final boolean last)
        {
            final double x1 = m_state.x(col);
            final double y1 = m_state.y(row);
            
            Transition trans = new DropTransition(x1, y1, x2, y2, item)
            {
                public void afterStart()
                {
                    Cell cell = m_state.cell(col, row);
                    if (first)
                    {
                        item.removeShapeFromLayer(ctx.dotLayer);
                        item.addShapeToLayer(ctx.laserLayer);
                        
                        Sound.START_ROCKET.play();
                        m_state.addExplody(cell);
                    }
                    
                    if (!first && !(cell.item instanceof Explody || m_rocketLocations.contains(new Pt(col, row))))
                    {
                        cell.explode(null, 1);
                        if (cell.item == null && cell.canBeFilled())
                            m_state.addExplody(cell);
                    }
                }
                
                public void afterEnd()
                {
                    if (last)
                    {
                        item.explode(null, 0);
                        item.removeShapeFromLayer(ctx.laserLayer);
                    }
                }
            };
            m_list.add(trans);
        }
        
        public void animateIgnition(double x, double y, boolean playSound)
        {                
            m_list.add(new Ignition(x, y, ctx, playSound));
        }
        
        public void run()
        {                
            AbstractAnimation anim = new TimedAnimation(300, m_list);
            anim.run();
        }
    }
}