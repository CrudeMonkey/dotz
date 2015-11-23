package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Line;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.IColor;
import com.ait.lienzo.shared.core.types.LineCap;

public class Key extends Item
{
    public Key()
    {        
    }
    
    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group g = new Group();
        
        double sz = size * 0.9;
        
        double x1 = sz * 0.1;
        double x2 = sz * 0.25;
        
        double x3 = -sz * 0.09;
        double x4 = sz * 0.35;
        
        double y1 = sz * 0.15;
        double w1 = sz * 0.1;
        IColor color1 = ColorName.GOLDENROD;
        
        Line line = new Line(x1, 0, x1, y1);
        line.setStrokeWidth(w1);
        line.setStrokeColor(color1);
        line.setLineCap(LineCap.ROUND);        
        g.add(line);
        
        line = new Line(x2, 0, x2, y1 * 1.2);
        line.setStrokeWidth(w1);
        line.setStrokeColor(color1);
        line.setLineCap(LineCap.ROUND);        
        g.add(line);
        
        line = new Line(x3, 0, x4, 0);
        line.setStrokeWidth(w1);
        line.setStrokeColor(color1);
        line.setLineCap(LineCap.ROUND);        
        g.add(line);
        
        double r = sz * 0.15;
        Circle c = new Circle(r);
        c.setStrokeWidth(w1);
        c.setStrokeColor(color1);
        c.setX(sz * -0.225);
        g.add(c);
        
        g.setRotation(Math.PI / 4);
        
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
        return new Key();
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        return ExplodeAction.REMOVE;
    }
}
