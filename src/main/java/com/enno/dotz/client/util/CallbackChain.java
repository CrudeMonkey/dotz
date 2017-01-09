package com.enno.dotz.client.util;

import java.util.ArrayList;
import java.util.List;

public class CallbackChain implements Runnable
{
    protected List<Callback> m_list = new ArrayList<Callback>();
    protected Runnable m_doneCallback;
    
    public static abstract class Callback implements Runnable
    {
        private CallbackChain m_parent;
        protected int m_index;
        
        public abstract void run();
        
        public void doRun()
        {
            init();
            
            if (condition())
            {
                run();
                
                //TODO if synchronous doNext()
            }
            else
            {
                doNext();
            }
        }
        
        protected void init()
        {            
        }
        
        protected void doNext()
        {
            if (m_parent != null)
                m_parent.doNext(m_index + 1);
        }
        
        public boolean condition()
        {
            return true;
        }
        
        public void connectParent(CallbackChain parent, int index)
        {
            m_parent = parent;
            m_index = index;
        }
    }
    
    public CallbackChain()
    {        
    }
    
    public List<Callback> getCallbacks()
    {
        return m_list;
    }
    
    public CallbackChain(Runnable doneCallback)
    {        
        m_doneCallback = doneCallback;
    }
    
    public CallbackChain add(Callback cb)
    {
        cb.connectParent(this, m_list.size());
        m_list.add(cb);
        return this;
    }
    
    @Override 
    public void run()
    {
        doNext(0);
    }
    
    public void doNext(int i)
    {
        if (i < m_list.size())
            m_list.get(i).doRun();
        else
            done();
    }
    
    public void done()
    {
        if (m_doneCallback != null)
            m_doneCallback.run();
    }
    
    public void setDoneCallback(Runnable cb)
    {
        m_doneCallback = cb;
    }
    
    public Callback asCallback()
    {
        if (m_doneCallback != null)
            throw new RuntimeException("can't use CallbackChain as a callback this way");
        
        return new Callback() {
            @Override
            public void run()
            {
                m_doneCallback = new Runnable() {
                    @Override
                    public void run()
                    {
                        doNext();
                    }
                };
                CallbackChain.this.run();
            }
        };
    }
}
