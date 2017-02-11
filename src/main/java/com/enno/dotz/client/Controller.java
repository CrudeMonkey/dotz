package com.enno.dotz.client;

public class Controller
{
    public static interface Controllable
    {
        String getSequence();
        void setSequence(String seq);
        boolean hasController();
        void tick();
    }
    
    public static final String ON_OFF = "0,1";
    
    private int m_time = 0;
    private int[] m_initial;
    private int[] m_repeat;
    
    /**
     * 
     * @param sequence "0,1,+1,1,0" means "0,1" initially, followed by "1,1,0" repeating. 
     *          If there is no "+", then the whole sequence is repeating.
     */
    public Controller(String sequence)
    {
        String[] s = sequence.split(",");
        int i = 0;
        int n = s.length;
        while (i < n && !s[i].startsWith("+"))
            i++;
        
        if (i == n)
        {
            m_initial = new int[0];
            m_repeat = new int[n];
            for (int j = 0; j < n; j++)
            {
                m_repeat[j] = Integer.parseInt(s[j]);
            }
        }
        else
        {
            m_initial = new int[i];
            m_repeat = new int[n - i];
            for (int j = 0; j < i; j++)
            {
                m_initial[j] = Integer.parseInt(s[j]);
            }
            for (int j = i; j < n; j++)
            {
                m_repeat[j-i] = j == i ? Integer.parseInt(s[j].substring(1)) : Integer.parseInt(s[j]);
            }
        }
    }
    
    public Controller(int[] initial, int[] repeat)
    {
        m_initial = initial;
        m_repeat = repeat;
    }
    
    public int tick()
    {
        int state = getState();
        m_time++;
        return state;
    }
    
    public int getTime()
    {
        return m_time;
    }
    
    public void setTime(int time)
    {
        m_time = time;
    }
    
    public int getState()
    {
        if (m_time < m_initial.length)
            return m_initial[m_time];
        
        return m_repeat[(m_time - m_initial.length) % m_repeat.length];
    }
    
    public Controller copy()
    {
        return new Controller(m_initial, m_repeat);
    }

    public String getInitialSequence()
    {
        StringBuilder b = new StringBuilder();
        for (int i = 0, n = m_initial.length; i < n; i++)
        {
            if (i > 0)
                b.append(',');
            b.append(m_initial[i]);
        }
        return b.toString();
    }
    
    public String getRepeatingSequence()
    {
        StringBuilder b = new StringBuilder();
        for (int i = 0, n = m_repeat.length; i < n; i++)
        {
            if (i > 0)
                b.append(',');
            b.append(m_repeat[i]);
        }
        
        return b.toString();
    }
    
    public String getSequence()
    {
        StringBuilder b = new StringBuilder();
        b.append(getInitialSequence());
        
        if (b.length() > 0)
            b.append(",+");
            
       b.append(getRepeatingSequence());
        
        return b.toString();
    }
}
