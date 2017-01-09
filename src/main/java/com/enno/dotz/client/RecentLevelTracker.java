package com.enno.dotz.client;

import java.util.ArrayList;
import java.util.List;

import com.ait.tooling.nativetools.client.NArray;
import com.enno.dotz.client.io.ClientRequest;

public class RecentLevelTracker
{
    private List<Integer> m_recent = new ArrayList<Integer>();
    private int m_maxSize = 20;
    
    public void testing(int id)
    {
        int i = m_recent.indexOf(id);
        if (i == 0)
            return; // no change
        
        if (i != -1)
            m_recent.remove(i);
        m_recent.add(0, id);
        
        while (m_recent.size() > m_maxSize)
            m_recent.remove(m_maxSize);
       
        save();
    }

    protected void save()
    {
        ClientRequest.saveRecentLevels(m_recent, null);
    }

    public void setRecentLevels(NArray levels)
    {
        m_recent.clear();
        for (int i = 0, n = levels.size(); i < n; i++)
        {
            m_recent.add(levels.getAsObject(i).getAsInteger("id"));
        }
    }
}
