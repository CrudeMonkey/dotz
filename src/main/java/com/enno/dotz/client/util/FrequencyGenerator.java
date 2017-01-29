package com.enno.dotz.client.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FrequencyGenerator<T>
{
    public static class Frequency<T>
    {
        private double m_frequency;
        private T m_object;
        private double m_maxFrequency;
        
        public Frequency(double freq, T obj)
        {
            m_frequency = freq;
            m_object = obj;
        }

        public double getFrequency()
        {
            return m_frequency;
        }

        public void setMaxFrequency(double max)
        {
            m_maxFrequency = max;
        }
        
        public double getMaxFrequency()
        {
            return m_maxFrequency;
        }

        public T getObject()
        {
            return m_object;
        }
    }
    
    private List<Frequency<T>> m_list = new ArrayList<Frequency<T>>();
    private boolean m_initialized;
    private double m_total;
    
    public FrequencyGenerator<T> add(double freq, T obj)
    {
        m_list.add(new Frequency<T>(freq, obj));
        m_initialized = false;
        return this;
    }
    
    public T next(Random rnd)
    {
        if (!m_initialized)
        {
            m_total = 0;
            for (Frequency<T> f : m_list)
            {
                m_total += f.getFrequency();
                f.setMaxFrequency(m_total);
            }
            
            m_initialized = true;
        }
        
        double d = rnd.nextDouble() * m_total;
        for (Frequency<T> f : m_list)
        {
            if (d < f.getMaxFrequency())
                return f.getObject();
        }
        return m_list.get(0).getObject(); // should never happen
    }
}
