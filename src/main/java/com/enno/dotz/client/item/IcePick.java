package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Line;
import com.ait.lienzo.client.core.shape.SVGPath;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.LineCap;

public class IcePick extends Item
{
    private int m_radius;
    
    public IcePick()
    {
        this(3, false);
    }
    
    public IcePick(int radius, boolean stuck)
    {
        m_radius = radius;
        m_stuck = stuck;
    }

    @Override
    public String getType()
    {
        return "pick";
    }
    
    public void setRadius(int radius)
    {
        m_radius = radius;
    }
    
    @Override
    public IPrimitive<?> createShape(double sz)
    {
        Group g = new Group();
        double scale = sz / 65;
        g.setScale(scale);
        
        Line line = new Line(3, -15, -10, 16);
        line.setStrokeWidth(8);
        line.setStrokeColor(ColorName.BROWN);
        line.setLineCap(LineCap.ROUND);
        g.add(line);
        
        SVGPath p = new SVGPath("M -10,-20 a 40,40 45 0,1 30,25 a 50,50 45 0,0 -32,-20 z");
        p.setStrokeColor(ColorName.BLACK);
        p.setFillColor(ColorName.GREY);
        p.setStrokeWidth(1 / scale);
        g.add(p);
        
        if (isStuck())
        {
            Group shape = new Group();
            shape.add(createStuckShape(sz));
            shape.add(g);
            return shape;
        }
        return g;
    }

    @Override
    public boolean canGrowFire()
    {
        return true;
    }

    @Override
    public boolean canDropFromBottom()
    {
        return true;
    }
    
    @Override
    protected Item doCopy()
    {
        return new IcePick(m_radius, m_stuck);
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        return ExplodeAction.REMOVE;
    }
    
    public int getRadius()
    {
        return m_radius;
    }
    
    public int getStrength()
    {
        return 1;
    }
}
