package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.animation.IAnimation;
import com.ait.lienzo.client.core.animation.IAnimationCallback;
import com.ait.lienzo.client.core.animation.IAnimationHandle;
import com.ait.lienzo.client.core.animation.LayerRedrawManager;
import com.ait.lienzo.client.core.animation.TimedAnimation;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Picture;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.GridState;
import com.enno.dotz.client.util.Debug;

public class Bird extends Group
{
    private static final long CELL_TIME = 300;
    
    private Picture[] m_pictures = new Picture[5];
    private int m_index = 0;
    
    public Bird()
    {
        setScale(0.5);
        
        for (int i = 0; i < m_pictures.length; i++)
        {
            m_pictures[i] = new Picture("images/bird" + i + "t.png");
            add(m_pictures[i]);
            if (i != 0)
                m_pictures[i].setVisible(false);
        }
    }
    
    public void animate(double t)
    {
        int i = ((int) (t * 0.02)) % m_pictures.length;
        if (i != m_index)
        {
            m_pictures[m_index].setVisible(false);
            m_pictures[i].setVisible(true);
            m_index = i;
        }
    }
    
    public void animate(int col, int row, Context ctx, Runnable whenDone)
    {
        GridState state = ctx.state;
        double x1 = state.x(col);
        double y1 = state.y(row) - 50;
        int n = ctx.cfg.numColumns - col;
        animate(x1, y1, x1 + n * ctx.cfg.size, y1, n * CELL_TIME, ctx.laserLayer, null);
    }
    
    public void animate(final double x1, final double y1, final double x2, final double y2, final long duration, final Layer layer, final Runnable whenDone)
    {
        setX(x1);
        setY(y1);
        layer.add(this);
        final long startTime = System.currentTimeMillis();
        
        IAnimationCallback callback = new IAnimationCallback() {
            @Override
            public void onStart(IAnimation animation, IAnimationHandle handle)
            {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onFrame(IAnimation animation, IAnimationHandle handle)
            {
                long t = System.currentTimeMillis();
                double exp = t - startTime;
                double pct = exp / duration;
                setX(x1 + pct * (x2 - x1));
                setY(y1 + pct * (y2 - y1));
                animate(exp);
                redraw();
            }

            @Override
            public void onClose(IAnimation animation, IAnimationHandle handle)
            {
                layer.remove(Bird.this);
                redraw();
                if (whenDone != null)
                    whenDone.run();
            }
            
            protected void redraw()
            {
                LayerRedrawManager.get().schedule(layer);
            }
        };
        new TimedAnimation(duration, callback).run();
    }
}
