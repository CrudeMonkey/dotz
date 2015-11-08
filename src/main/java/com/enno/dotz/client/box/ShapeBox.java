package com.enno.dotz.client.box;

import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.shared.core.types.TextAlign;

public class ShapeBox<T extends IPrimitive<?>> implements Box
{
    protected Double m_width;

    protected Double m_height;
    
    protected TextAlign m_align = TextAlign.CENTER;
    protected HorAlign  m_halign = HorAlign.MIDDLE;
    
    protected T m_shape;
    protected Extent m_pref;
    
    public ShapeBox(T shape)
    {
        m_shape = shape;
        m_shape.setListening(false);
    }
    
    @Override
    public Extent getPreferred(LayoutContext ctx)
    {
        if (m_pref != null)
            return m_pref;
        
        return m_pref = new Extent(m_width, m_height);
    }

    @Override
    public void setRect(Extent r, LayoutContext ctx)
    {
        double dw = r.w - m_pref.w;
        double dh = r.h - m_pref.h;
        
        double w = m_pref.w;
        if (dw == 0)
        {
            m_shape.setX(r.x);
        }
        else if (Extent.isFill(m_width))
        {
            w = r.w;
            m_shape.setX(r.x);
            //TODO set shape width?
        }
        else
        {
            switch (m_align)
            {
                case LEFT: m_shape.setX(r.x); break;
                case RIGHT: m_shape.setX(r.x + dw); break;
                default: m_shape.setX(r.x + dw / 2); break;
            }
        }
        
        double h = m_pref.h;
        if (dh == 0)
        {
            m_shape.setY(r.y);
        }
        else if (Extent.isFill(m_height))
        {
            h = r.h;
            m_shape.setY(r.y);
            //TODO set shape height?
        }
        else
        {
            switch (m_halign)
            {
                case TOP: m_shape.setY(r.y); break;
                case BOTTOM: m_shape.setY(r.y + dh); break;
                default: m_shape.setY(r.y + dh / 2); break;
            }
        }
    }

    @Override
    public TextAlign getAlign()
    {
        return m_align;
    }

    @Override
    public HorAlign getHalign()
    {
        return m_halign;
    }

    public Double getWidth()
    {
        return m_width;
    }

    public void setWidth(Double width)
    {
        m_width = width;
    }

    public Double getHeight()
    {
        return m_height;
    }

    public void setHeight(Double height)
    {
        m_height = height;
    }

    public void setAlign(TextAlign align)
    {
        m_align = align;
    }

    public void setHalign(HorAlign halign)
    {
        m_halign = halign;
    }
    
    @Override
    public IPrimitive<?> getShape()
    {
        return m_shape;
    }
}