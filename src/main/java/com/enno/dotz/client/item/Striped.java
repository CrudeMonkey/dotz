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
    
    public Striped(int color, boolean vertical)
    {
        this.color = color;
        this.vertical = vertical;
    }
    
    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group g = new Group();
        
        double s = size / 4;
        Circle c = new Circle(size / 4);
        IColor fillColor = cfg == null ? Config.COLORS[0] : cfg.drawColor(color);      // cfg is null in ModePalette  
        c.setFillColor(fillColor);
        g.add(c);
        
        double w = 3;
        double dx = w * 2;
        if (vertical)
        {
            for (double x = -s; x < s; x += dx)
            {
                double a = Math.asin(x / s);
                double y = Math.cos(a) * s;
                Line line = new Line(x, y, x, -y);
                line.setStrokeColor(ColorName.WHITE);
                line.setStrokeWidth(w);
                g.add(line);
            }
        }
        else
        {
            for (double y = 2-s; y < s; y += dx)
            {
                double a = Math.asin(y / s);
                double x = Math.cos(a) * s;
                Line line = new Line(x, y, -x, y);
                line.setStrokeColor(ColorName.WHITE);
                line.setStrokeWidth(w);
                g.add(line);
            }
        }
        
        c = new Circle(size / 4);
        c.setStrokeColor(fillColor);
        g.add(c);
        
        return g;
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