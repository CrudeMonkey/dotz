package com.enno.dotz.client.box;

import java.util.ArrayList;
import java.util.List;

import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.shared.core.types.TextAlign;

public abstract class BoxContainer extends Group implements Box
{
    protected List<Box> m_kids = new ArrayList<Box>();
    protected Double m_width;
    protected Double m_height;
    
    protected double m_neededWidth;
    protected double m_neededHeight;
    
    protected TextAlign m_align = TextAlign.CENTER;
    protected HorAlign m_halign = HorAlign.MIDDLE;
    
    protected double m_spacing = BoxDefaults.INSTANCE.spacing;
    protected double m_leftPadding = BoxDefaults.INSTANCE.padding;    
    protected double m_rightPadding = BoxDefaults.INSTANCE.padding;
    protected double m_topPadding = BoxDefaults.INSTANCE.padding;
    protected double m_bottomPadding = BoxDefaults.INSTANCE.padding;
    
    protected Rectangle m_background;
    
    protected Extent m_pref;
    
    protected boolean m_initialized;
    
    public void addMember(Box box)
    {
        m_kids.add(box);
    }
    
    protected void init()
    {
        if (m_initialized)
            return;            
        m_initialized = true;
        
        doInit();
    }
    
    protected void doInit()
    {
        if (m_background != null)
        {
            add(m_background);
        }
        
        for (Box b : m_kids)
        {
            add(b.getShape());
        }
    }
    
    public IPrimitive<?> getShape()
    {
        return this;
    }

    public TextAlign getAlign()
    {
        return m_align;
    }
    
    public HorAlign getHalign()
    {
        return m_halign;
    }
    
    public void setBackground(Rectangle bg)
    {
        m_background = bg;
    }

    public double getSpacing()
    {
        return m_spacing;
    }

    public void setSpacing(double spacing)
    {
        m_spacing = spacing;
    }

    public double getLeftPadding()
    {
        return m_leftPadding;
    }

    public void setLeftPadding(double leftPadding)
    {
        m_leftPadding = leftPadding;
    }

    public double getRightPadding()
    {
        return m_rightPadding;
    }

    public void setRightPadding(double rightPadding)
    {
        m_rightPadding = rightPadding;
    }

    public double getTopPadding()
    {
        return m_topPadding;
    }

    public void setTopPadding(double topPadding)
    {
        m_topPadding = topPadding;
    }

    public double getBottomPadding()
    {
        return m_bottomPadding;
    }

    public void setBottomPadding(double bottomPadding)
    {
        m_bottomPadding = bottomPadding;
    }

    public void setPadding(double padding)
    {
        setPadding(padding, padding, padding, padding);
    }
    
    public void setPadding(double top, double right, double bottom, double left)    
    {
        setTopPadding(top);
        setRightPadding(right);
        setBottomPadding(bottom);
        setLeftPadding(left);
    }
    
    public Rectangle getBackground()
    {
        return m_background;
    }

    public void setAlign(TextAlign align)
    {
        m_align = align;
    }

    public void setHalign(HorAlign halign)
    {
        m_halign = halign;
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
}