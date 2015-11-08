package com.enno.dotz.client.ui;

public class MXToolTip
{
    private final String m_text;
    private int m_toolTipWidth = -1;
    
    public MXToolTip(String text)
    {
        m_text = text;
    }
    
    public MXToolTip(String text, int toolTipWidth)
    {
        m_text = text;
        m_toolTipWidth = toolTipWidth;
    }
    
    public final String getText()
    {
        return m_text;
    }

    public int getToolTipWidth()
    {
        return m_toolTipWidth;
    }

    public void setToolTipWidth(int toolTipWidth)
    {
        m_toolTipWidth = toolTipWidth;
    }
}
