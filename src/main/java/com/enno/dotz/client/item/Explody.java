package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.shared.core.types.ColorName;

public class Explody extends Item
{
    private int m_radius;
    
    public Explody()
    {
        m_radius = 1;
    }
    
    public Explody(int radius)
    {
        m_radius = radius;
    }

    @Override
    public String getType()
    {
        return "explody";
    }
    
    public int getRadius()
    {
        return m_radius;
    }
    
    @Override
    public IPrimitive<?> createShape(double size)
    {
        Circle c = new Circle(size / 4);
        c.setFillColor(ColorName.BLACK);  
        return c;
    }
    
    protected Item doCopy()
    {
        return new Explody(m_radius);
    }

    public boolean canExplodeNextTo()
    {
        return false;
    }
    
    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        return ExplodeAction.REMOVE;
    }
}
