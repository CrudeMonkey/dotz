package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Slice;
import com.ait.lienzo.shared.core.types.ColorName;

public class YinYang extends Item
{

    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group g = new Group();
        
        double sz = size * 0.6;
        double r = sz / 2;
        double r2 = r / 2;
        double r3 = r2 / 4;
        
        Circle bg = new Circle(r * 1.08);
        bg.setFillColor(ColorName.BLACK);
        g.add(bg);
        
        Slice left = new Slice(r, Math.PI / 2, Math.PI * 1.5, false);
        left.setFillColor(ColorName.BLACK);
        g.add(left);
        
        Slice right = new Slice(r, Math.PI / 2, Math.PI * 1.5, true);
        right.setFillColor(ColorName.WHITE);
        g.add(right);
        
        Circle c = new Circle(r2);
        c.setY(-r2);
        c.setFillColor(ColorName.BLACK);
        g.add(c);
        
        c = new Circle(r2);
        c.setY(r2);
        c.setFillColor(ColorName.WHITE);
        g.add(c);
        
        c = new Circle(r3);
        c.setY(-r2);
        c.setFillColor(ColorName.WHITE);
        g.add(c);
        
        c = new Circle(r3);
        c.setY(r2);
        c.setFillColor(ColorName.BLACK);
        g.add(c);
        
        return g;
    }
    
    @Override
    public boolean canDropFromBottom()
    {
        return true;
    }

    @Override
    protected Item doCopy()
    {
        return new YinYang();
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        // TODO Auto-generated method stub
        return ExplodeAction.REMOVE;
    }
}
