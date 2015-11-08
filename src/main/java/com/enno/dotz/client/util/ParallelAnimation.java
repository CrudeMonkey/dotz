package com.enno.dotz.client.util;

import java.util.ArrayList;

import com.ait.lienzo.client.core.animation.IAnimation;
import com.ait.lienzo.client.core.animation.IAnimationCallback;
import com.ait.lienzo.client.core.animation.IAnimationHandle;
import com.ait.lienzo.client.core.animation.LayerRedrawManager;
import com.ait.lienzo.client.core.shape.Layer;

public class ParallelAnimation extends ArrayList<IAnimationCallback> implements IAnimationCallback
{
    protected Layer m_layer;

    public ParallelAnimation(Layer layer)
    {
        m_layer = layer;
    }
    
    @Override
    public void onStart(IAnimation animation, IAnimationHandle handle)
    {
        for (IAnimationCallback t : this)
        {
            t.onStart(animation, handle);
        }
        redraw();
    }

    @Override
    public void onFrame(IAnimation animation, IAnimationHandle handle)
    {
        for (IAnimationCallback t : this)
        {
            t.onFrame(animation, handle);
        }
        redraw();
    }

    @Override
    public void onClose(IAnimation animation, IAnimationHandle handle)
    {
        for (IAnimationCallback t : this)
        {
            t.onClose(animation, handle);
        }
        redraw();
        done();
    }
    
    public void redraw()
    {
        if (m_layer != null)
            LayerRedrawManager.get().schedule(m_layer);
    }
    
    public void done()
    {
        
    }
}
