package com.enno.dotz.client;

import com.ait.tooling.nativetools.client.NObject;

public class LetterMultiplier
{
    public static final int WORD_MULTIPLIER = 0;
    public static final int LETTER_MULTIPLIER = 1;
    
    private int m_type;
    private int m_multiplier;
    
    public LetterMultiplier(int multiplier, int type)
    {
        m_multiplier = multiplier;
        m_type = type;
    }

    public int getType()
    {
        return m_type;
    }
    
    public int getMultiplier()
    {
        return m_multiplier;
    }

    public boolean isWordMultiplier()
    {
        return m_type == WORD_MULTIPLIER;
    }

    public static LetterMultiplier fromJson(NObject obj)
    {
        if (obj == null)
            return null;
        
        return new LetterMultiplier(obj.getAsInteger("n"), obj.getAsInteger("type"));
    }
    
    public NObject toJson()
    {
        NObject obj = new NObject();
        obj.put("n", m_multiplier);
        obj.put("type", m_type);
        return obj;
    }

    public LetterMultiplier copy()
    {
       return new LetterMultiplier(m_multiplier, m_type);
    }
}