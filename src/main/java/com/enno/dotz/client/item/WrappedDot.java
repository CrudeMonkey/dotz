package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.IColor;
import com.enno.dotz.client.Config;

public class WrappedDot extends Item
{
    public int color;
    
    public WrappedDot(int color)
    {
        this.color = color;
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
    public IPrimitive<?> createShape(double size)
    {
        Group g = new Group();
        
        if (isStuck())
            g.add(createStuckShape(size));
        
        Circle c = new Circle(size / 4);
        IColor fillColor = cfg == null ? Config.COLORS[0] : cfg.drawColor(color);
        c.setFillColor(fillColor);       // cfg is null in ModePalette  
        g.add(c);
        
        Dot.addMark(color, g, size);
        
        double sz = size * 0.75;
        Rectangle r = new Rectangle(sz, sz);
        r.setX(-sz/2);
        r.setY(-sz/2);
        r.setFillColor(fillColor);
        r.setAlpha(0.5);
        r.setDashArray(1, 1);
        g.add(r);
        
        r = new Rectangle(sz, sz);
        r.setX(-sz/2);
        r.setY(-sz/2);
        r.setStrokeColor(ColorName.BLACK);
        r.setStrokeWidth(1);
        r.setDashArray(1, 1);
        g.add(r);
        
        return g;
    }

    @Override
    protected Item doCopy()
    {
        return new WrappedDot(color);
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        return ExplodeAction.EXPLODY; //TODO
    }
}
