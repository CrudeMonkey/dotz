package com.enno.dotz.client.util;

import java.util.ArrayList;
import java.util.List;

import com.ait.lienzo.client.core.animation.IAnimation;
import com.ait.lienzo.client.core.animation.IAnimationCallback;
import com.ait.lienzo.client.core.animation.IAnimationHandle;
import com.ait.lienzo.client.core.animation.LayerRedrawManager;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Node;

public class SerialAnimation extends ArrayList<IAnimationCallback> implements IAnimationCallback
{
    protected Layer m_layer;
    
    protected List<Long> m_durations = new ArrayList<Long>();

    private long m_totalDuration;

    private long m_startTime;

    private int m_currIndex;

    private long[] m_endTimes;

    private long[] m_startTimes;
     
    public SerialAnimation(Layer layer)
    {
        m_layer = layer;
    }
    
    public void addAnimation(long duration, IAnimationCallback cb)
    {
        add(cb);
        m_durations.add(duration);
        m_totalDuration += duration;
    }
    
    public long getDuration()
    {
        return m_totalDuration;
    }
    
    @Override
    public void onStart(IAnimation animation, IAnimationHandle handle)
    {
        m_startTimes = new long[size()];
        m_endTimes = new long[size()];
        
        long t = 0;
        for (int i = 0; i < size(); i++)
        {
            long dur = m_durations.get(i);
            m_startTimes[i] = t;
            m_endTimes[i] = m_startTimes[i] + dur;
            t += dur;
        }
        
        m_startTime = System.currentTimeMillis();
        m_currIndex = 0;
        get(0).onStart(anim(0), handle);
        redraw();
    }

    @Override
    public void onFrame(IAnimation animation, IAnimationHandle handle)
    {
        long dt = System.currentTimeMillis() - m_startTime;
        
        int i = getIndex(dt);
        
        
        if (i == -1)
        {
            return; //TODO close/start all in between? - can this happen?
        }
        
        while (m_currIndex < i)
        {
            get(m_currIndex).onClose(anim(1), handle);
            m_currIndex++;
            if (m_currIndex == size())
            {
                m_currIndex = -1;
                break;
            }
            get(m_currIndex).onStart(anim(0), handle);
        }
        
        if (m_currIndex != -1)
        {
            double perc = ((double) (dt - m_startTimes[m_currIndex])) / m_durations.get(m_currIndex);
            get(m_currIndex).onFrame(anim(perc), handle);
        }
        
        redraw();
    }

    @Override
    public void onClose(IAnimation animation, IAnimationHandle handle)
    {
        if (m_currIndex != -1)
        {
            // TODO close prev
            get(m_currIndex).onClose(anim(1), handle);
        }
        
        redraw();
        done();
    }
    
    public void addDelay(long duration)
    {
        addAnimation(duration, new IAnimationCallback() {
            @Override
            public void onStart(IAnimation animation, IAnimationHandle handle)
            {
            }

            @Override
            public void onFrame(IAnimation animation, IAnimationHandle handle)
            {
            }

            @Override
            public void onClose(IAnimation animation, IAnimationHandle handle)
            {
            }
        });
    }
    
    public static SerialAnimation delay(long delay, long duration, IAnimationCallback cb, Layer layer)
    {
        SerialAnimation serial = new SerialAnimation(layer);
        serial.addDelay(delay);
        serial.addAnimation(duration, cb);
        return serial;
    }
    
    private int getIndex(long dt)
    {
        if (m_currIndex == -1)  //TODO
            return -1;

        if (dt >= m_startTimes[m_currIndex] && dt < m_endTimes[m_currIndex])
            return m_currIndex;
        
        int i = m_currIndex;
        while (dt >= m_endTimes[i])
        {
            if (i == size() - 1)
                return -1;  // over time
            
            i++;
        }
        return i;
    }
    
    // Just a wrapper to pass 'perc' to the IAnimation members
    private static class Anim implements IAnimation
    {
        private double perc;

        @Override
        public double getPercent()
        {
            return perc;
        }

        @Override
        public double getDuration()
        {
            return 0;
        }

        @Override
        public IAnimation doStart()
        {
            return null;
        }

        @Override
        public IAnimation doFrame()
        {
            return null;
        }

        @Override
        public IAnimation doClose()
        {
            return null;
        }

        @Override
        public Node<?> getNode()
        {
            return null;
        }

        @Override
        public IAnimation setNode(Node<?> node)
        {
            return null;
        }        
    }
    private Anim m_anim = new Anim();
    
    private IAnimation anim(double perc)
    {
        m_anim.perc = perc;
        return m_anim;
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
