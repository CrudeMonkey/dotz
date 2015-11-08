package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Line;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.core.types.Point2DArray;
import com.ait.lienzo.shared.core.types.ColorName;

public class Clock extends Item
{
    private int m_strength;
    private Line m_time;
    private double m_handle;
    
    public Clock(int strength)
    {
        m_strength = strength;
    }
    
    public int getStrength()
    {
        return m_strength;
    }
    
    public void setStrength(int strength)
    {
        m_strength = strength;        
    }
    
    @Override
    public IPrimitive<?> createShape(double sz)
    {
        Group shape = new Group();
        
        double r = sz * 0.36;
        
        double w = r/2, h = w * 0.9;

        Rectangle top = new Rectangle(w, h);
        top.setX(-w / 2);
        top.setY(-r - h/2);
        top.setFillColor(ColorName.FIREBRICK);
        shape.add(top);

        Circle bomb = new Circle(r);
        bomb.setFillColor(ColorName.GOLD);
        bomb.setStrokeColor(ColorName.GOLDENROD);
        shape.add(bomb);        
        
        double clock_radius = r * 0.75;
        Circle clock = new Circle(clock_radius);
        clock.setFillColor(ColorName.WHITE);
        clock.setStrokeColor(ColorName.GOLDENROD);
        shape.add(clock);
        
        Circle tiny = new Circle(2);
        tiny.setFillColor(ColorName.BLACK);
        shape.add(tiny);
        
        double handle = clock_radius * 0.85;
        Line up = new Line(0, 0, 0, - handle);
        up.setStrokeColor(ColorName.BLACK);
        up.setStrokeWidth(1);
        shape.add(up);
        
        double a = ((m_strength + 3) % 12) * Math.PI / 6;
        m_handle = handle * 0.7;
        m_time = new Line(0, 0, Math.cos(a) * m_handle, - Math.sin(a) * m_handle);
        m_time.setStrokeColor(ColorName.BLACK);
        m_time.setStrokeWidth(1);
        shape.add(m_time);
        
        return shape;
    }

    @Override
    protected Item doCopy()
    {
        return new Clock(m_strength);
    }
        
    @Override
    public boolean canDropFromBottom()
    {
        return true;
    }
        
    @Override
    public void dropFromBottom()
    {
        ctx.score.droppedClock(m_strength);
    }
    
    public boolean tick()
    {
        if (m_strength > 0)
            m_strength--;
        
        if (m_strength > 0)
        {
            updateHandle();
            return false;
        }
        
        ctx.score.explodedClock();        
        return true; // explode
    }
    
    protected void updateHandle()
    {
        double a = ((m_strength + 3) % 12) * Math.PI / 6;
        
        Point2DArray pts = m_time.getPoints();
        Point2D pt = pts.get(1);
        pt.setX(Math.cos(a) * m_handle).setY(- Math.sin(a) * m_handle);
        m_time.setPoints(pts);
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        if (tick())
            return ExplodeAction.EXPLODY;
        
        return ExplodeAction.NONE;
    }
}
