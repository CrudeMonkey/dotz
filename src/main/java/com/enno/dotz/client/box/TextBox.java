package com.enno.dotz.client.box;

import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.client.core.types.TextMetrics;
import com.ait.lienzo.shared.core.types.TextAlign;

public class TextBox extends ShapeBox<Text>
{
    public TextBox(Text shape)
    {
        super(shape);
        
        m_align = shape.getTextAlign();
        if (m_align == null)
            m_align = TextAlign.LEFT;
        
        if (shape.getTextBaseLine() != null)
        {
            switch (shape.getTextBaseLine())
            {
                case TOP: m_halign = HorAlign.TOP; break;
                case BOTTOM: m_halign = HorAlign.BOTTOM; break;
                default: m_halign = HorAlign.MIDDLE; break;
            }
        }
    }

    @Override
    public Extent getPreferred(LayoutContext ctx)
    {
        if (m_pref != null)
            return m_pref;
        
        TextMetrics tm = m_shape.measure(ctx.layer.getContext());
        
        return m_pref = new Extent(tm.getWidth(), tm.getHeight());
    }
    
    @Override
    public void setRect(Extent r, LayoutContext ctx)
    {
        double dw = r.w - m_pref.w;
        double dh = r.h - m_pref.h;
        
        double w = m_pref.w;
        if (Extent.isFill(m_width))
            w = r.w;
        
//        if (dw == 0)
//        {
//            m_shape.setX(r.x);
//        }
//        else
//        {
            switch (m_align)
            {
                case LEFT: m_shape.setX(r.x); break;
                case RIGHT: m_shape.setX(r.x + w); break;
                default: m_shape.setX(r.x + w / 2); break;
            }
//        }
        
        double h = m_pref.h;
        if (Extent.isFill(m_height))
           h = r.h;
        
//        if (dh == 0)
//        {
//            m_shape.setY(r.y);
//        }
//        else
//        {
            switch (m_halign)
            {
                case TOP: m_shape.setY(r.y); break;
                case BOTTOM: m_shape.setY(r.y + h); break;
                default: m_shape.setY(r.y + h / 2); break;
            }
//        }
    }
}