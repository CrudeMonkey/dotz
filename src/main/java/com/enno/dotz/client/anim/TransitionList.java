package com.enno.dotz.client.anim;

import java.util.logging.Logger;

import com.ait.lienzo.client.core.animation.AbstractAnimation;
import com.ait.lienzo.client.core.animation.IAnimationCallback;
import com.ait.lienzo.client.core.animation.TimedAnimation;
import com.ait.lienzo.client.core.shape.Layer;
import com.enno.dotz.client.util.CallbackChain.Callback;
import com.enno.dotz.client.util.Debug;
import com.enno.dotz.client.util.ParallelAnimation;

public class TransitionList extends Callback
{
    private static Logger logger = Logger.getLogger(TransitionList.class.getName());
    
    protected ParallelAnimation m_list;
    private double m_duration;
    private String m_name;
    
    public TransitionList(String name, Layer layer, double duration)
    {
        m_name = name;
        m_duration = duration;
        
        m_list = new ParallelAnimation(layer) {
            @Override
            public void done()
            {
                TransitionList.this.done();
                doNext(); // invoke next Callback in the CallbackChain
            }
        };
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
