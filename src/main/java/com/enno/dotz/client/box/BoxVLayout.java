package com.enno.dotz.client.box;

public class BoxVLayout extends BoxContainer
{
    @Override
    public Extent getPreferred(LayoutContext ctx)
    {
        if (m_pref != null)
            return m_pref;
        
        init();
        
        m_neededWidth = 0;
        m_neededHeight = 0;
        
        boolean first = true;
        for (Box t : m_kids)
        {
            Extent r = t.getPreferred(ctx);
            
            if (r.w > m_neededWidth)
                m_neededWidth = r.w;
            
            if (first)
                first = false;
            else
                m_neededHeight += m_spacing;

            m_neededHeight += r.h;
        }
        
        m_neededWidth += m_leftPadding + m_rightPadding;
        m_neededHeight += m_topPadding + m_bottomPadding;
        
        return m_pref = new Extent(m_width != null && m_width > 0 ? m_width : m_neededWidth, 
                                m_height != null && m_height > 0 ? m_height : m_neededHeight);
    }

    @Override
    public void setRect(Extent r, LayoutContext ctx)
    {
        double dw = r.w - m_pref.w;
        double dh = r.h - m_pref.h;
        
        double w = m_pref.w;
        if (dw == 0)
        {
            setX(r.x);
        }
        else if (Extent.isFill(m_width))
        {
            w = r.w;
            setX(r.x);
        }
        else
        {
            switch (m_align)
            {
                case LEFT: setX(r.x); break;
                case RIGHT: setX(r.x + dw); break;
                default: setX(r.x + dw / 2); break;
            }
        }
        
        double h = m_pref.h;
        if (dh == 0)
        {
            setY(r.y);
        }
        else if (Extent.isFill(m_height))
        {
            h = r.h;
            setY(r.y);
        }
        else
        {
            switch (m_halign)
            {
                case TOP: setY(r.y); break;
                case BOTTOM: setY(r.y + dh); break;
                default: setY(r.y + dh / 2); break;
            }
        }            
        
        if (m_background != null)
        {
            m_background.setWidth(w);
            m_background.setHeight(h);
        }
        
        w -= m_leftPadding + m_rightPadding;
        
        double x = m_leftPadding;
        double y = m_topPadding;
        Extent ar = new Extent(x, y, w, 0d);
        
        for (Box t : m_kids)
        {
            Extent kr = t.getPreferred(ctx);
            ar.y = y;                       
            ar.h = kr.h;
            t.setRect(ar, ctx);
                            
            y += m_spacing + kr.h;
        }
    }
}