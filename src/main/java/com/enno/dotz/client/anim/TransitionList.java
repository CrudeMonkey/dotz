package com.enno.dotz.client.anim;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.ait.lienzo.client.core.animation.AbstractAnimation;
import com.ait.lienzo.client.core.animation.IAnimation;
import com.ait.lienzo.client.core.animation.IAnimationCallback;
import com.ait.lienzo.client.core.animation.IAnimationHandle;
import com.ait.lienzo.client.core.animation.LayerRedrawManager;
import com.ait.lienzo.client.core.animation.TimedAnimation;
import com.ait.lienzo.client.core.shape.Layer;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.util.CallbackChain.Callback;
import com.enno.dotz.client.util.ParallelAnimation;

public class TransitionList extends Callback
{
    private static Logger logger = Logger.getLogger(TransitionList.class.getName());
    
    protected ParallelAnimation m_list;
    private double m_duration;
    private String m_name;

    private List<Layer> m_otherLayers = new ArrayList<Layer>();
    
    public TransitionList(String name, Layer layer, double duration)
    {
        m_name = name;
        m_duration = duration;
        
        m_list = new ParallelAnimation(layer) {
            @Override
            public void onStart(IAnimation animation, IAnimationHandle handle)
            {
                TransitionList.this.onStart();
                super.onStart(animation, handle);
                redrawOtherLayers();
            }
            
            @Override
            public void done()
            {
                drawOtherLayers();  // prevent stutter
                TransitionList.this.done();
                doNext(); // invoke next Callback in the CallbackChain
            }
        };
    }
    
    public static class NukeTransitionList extends TransitionList
    {
        private Context m_ctx;

        public NukeTransitionList(String name, Context ctx, double duration)
        {
            super(name, ctx.nukeLayer, duration);
            
            m_ctx = ctx;
            
            addOtherLayers(ctx.dotLayer);
        }
        
        @Override
        public void onStart()
        {
            m_ctx.nukeLayer.setVisible(true);
        }

        @Override
        public void done()
        {
            m_ctx.nukeLayer.setVisible(false);
        }
    }
    
    public TransitionList addOtherLayers(Layer... layers)
    {
        for (Layer layer : layers)
        {
            if (!m_otherLayers.contains(layer))
                m_otherLayers.add(layer);
        }
        return this;
    }
    
    public void redrawOtherLayers()
    {
        if (m_otherLayers != null)
        {
            for (Layer layer : m_otherLayers)
                LayerRedrawManager.get().schedule(layer);
        }
    }
    
    public void drawOtherLayers()
    {
        if (m_otherLayers != null)
        {
            for (Layer layer : m_otherLayers)
                layer.draw();
        }
    }
    
    public boolean condition()
    {
        return m_list.size() > 0;
    }
    
    public double getDuration()
    {
        return m_duration;
    }
    
    public void add(IAnimationCallback trans)
    {
        m_list.add(trans);
    }
    
    public ParallelAnimation getTransitions()
    {
        return m_list;
    }
    
    public boolean isEmpty()
    {
        return m_list.isEmpty();
    }
    
    public void onStart()
    {
    }
    
    public void done()
    {        
    }
    
    @Override
    public void run()
    {
        //Debug.p("run tlist " + m_index + ":" + m_name);
        AbstractAnimation anim = new TimedAnimation(m_duration, m_list);
        anim.run();
    }
    
    public static void p(String s)
    {
        logger.info(s);
    }
}
