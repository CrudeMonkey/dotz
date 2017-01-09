package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Line;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.IColor;
import com.enno.dotz.client.Config;

public class Striped extends Item
{
    public int color;
    public boolean vertical;
    public boolean armed;
    private boolean m_wide;
    private boolean m_bothWays;
    
    public Striped(int color, boolean vertical)
    {
        this.color = color;
        this.vertical = vertical;
    }
    
    public void setWide(boolean wide)
    {
        m_wide = wide;
    }
    
    public boolean isWide()
    {
        return m_wide;
    }
    
    public void setBothWays(boolean bothWays)
    {
        m_bothWays = bothWays;
    }
    
    public boolean isBothWays()
    {
        return m_bothWays;
    }

    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group g = new Group();
        
        if (isStuck())
            g.add(createStuckShape(size));
        
        double s = size / 4;
        Circle c = new Circle(size / 4);
        IColor fillColor = cfg == null ? Config.COLORS[0] : cfg.drawColor(color);      // cfg is null in ModePalette  
        c.setFillColor(fillColor);
        g.add(c);
        
        double w = 3;
        double dx = w * 2;
        for (double x = -s; x < s; x += dx)
        {
            double a = Math.asin(x / s);
            double y = Math.cos(a) * s;
            Line line = new Line(x, y, x, -y);
            line.setStrokeColor(ColorName.WHITE);
            line.setStrokeWidth(w);
            g.add(line);
        }
        
        c = new Circle(size / 4);
        c.setStrokeColor(fillColor);
        g.add(c);
        
        if (!vertical)
            g.setRotationDegrees(90);
        
        return g;
    }
    
    public boolean isVertical()
    {
        return vertical;
    }
    
    @Override
    public Integer getColor()
    {
        return color;
    }
    
    @Override
    public boolean canConnect()
    {
        return true;
    }

    @Override
    public boolean canGrowFire()
    {
        return true;
    }

    @Override
    public boolean canChangeColor()
    {
        return true;
    }

    @Override
    public boolean canReshuffle()
    {
        return true;
    }
    
    @Override
    public boolean canRotate()
    {
        return true;
    }

    @Override
    public void rotate(int n) // ignore n
    {
        vertical = !vertical;
        shape.setRotationDegrees(vertical ? 0 : 90);
    }
    
    @Override
    public boolean isArmed()
    {
        return armed;
    }
    
    @Override
    protected Item doCopy()
    {
        return new Striped(color, vertical);
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        // ignore passed in color
        //ctx.score.explodedDot(this.color);
        armed = true;
        
        return ExplodeAction.NONE; // REMOVE; // remove dot
    }
}
