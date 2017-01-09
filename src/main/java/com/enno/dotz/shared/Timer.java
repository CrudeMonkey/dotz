package com.enno.dotz.shared;

public class Timer
{
    private String m_name;
    private long m_start;
    
    public Timer()
    {
        this(null);
    }
    
    public Timer(String name)
    {
        m_name = name;
        start();
    }
    
    public void start()
    {
        m_start = System.currentTimeMillis();
    }
    
    public long elapsed()
    {
        return System.currentTimeMillis() - m_start;
    }
    
    public String toString()
    {
        long t = elapsed();
        StringBuilder b = new StringBuilder();
        if (m_name != null)
            b.append(m_name).append(": ");
        
        long ms = t % 1000;
        t /= 1000;
        long sec = t % 60;
        t /= 60;
        long min = t;
        
        if (min > 0)
        {
            b.append(min).append(':');
            if (sec < 10)
                b.append('0');
            b.append(sec);
            b.append(" min");
        }
        else
        {
            if (sec > 0)
            {
                b.append(sec);
                if (ms < 100)
                    b.append('0');
                if (ms < 10)
                    b.append('0');
                b.append(ms);
                b.append(" sec");
            }
            else
            {
                b.append(ms).append(" ms");
            }
        }
        
        
        return b.toString();
    }
}
