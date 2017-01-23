package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Triangle;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.IColor;

/**
 * Similar to an Anchor, it can drop from the bottom, but it can't explode.
 * 
 * @author Enno
 *
 */
public class Diamond extends Item
{
    private static final String[] COLORS = {
            "#C3ECFB", 
            "#9AE0FA", 
            "#E3F6FB", 
            "#9AE0FA", 
            "#54CDFB", 
            "#E8F5F9", 
            "#C3ECFB", 
            "#00AEF0", 
    };
    private static final IColor STROKE_COLOR = ColorName.BLACK;

    public Diamond()
    {
    }
    
    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group group = new Group();
        
        double w = size * 0.6;
        double h = size * 0.6;
        double h1 = h / 3;
        double h2 = h - h1;
        double w1 = w * 0.3;
        double w2 = w - w1 * 2;

        double xtip = w / 2;

        double dx = -xtip;
        double dy = h1 - h/2;
        
        addTriangle(0, group, dx, dy, 0, 0, w1, 0, w1/2, -h1);
        addTriangle(1, group, dx, dy, w1, 0, w1/2, -h1, w1 + w2/2, -h1);
        addTriangle(2, group, dx, dy, w1, 0, w1 + w2, 0, xtip, -h1);
        addTriangle(3, group, dx, dy, w1 + w2/2, -h1, w1 + w2, 0, 1.5 * w1 + w2, -h1);
        addTriangle(4, group, dx, dy, w1 + w2, 0, 1.5 * w1 + w2, -h1, w, 0);
        addTriangle(5, group, dx, dy, 0, 0, w1, 0, xtip, h2);
        addTriangle(6, group, dx, dy, w1, 0, w1 + w2, 0, xtip, h2);
        addTriangle(7, group, dx, dy, w1 + w2, 0, w, 0, xtip, h2);
        
        
        return group;
    }
    
    private static void addTriangle(int color, Group g, double dx, double dy, double x1, double y1, double x2, double y2, double x3, double y3)
    {
        Triangle t = new Triangle(new Point2D(dx + x1, dy + y1), new Point2D(dx + x2, dy + y2), new Point2D(dx + x3, dy + y3));
        t.setStrokeColor(STROKE_COLOR);
        t.setFillColor(COLORS[color]);
        g.add(t);
    }
    
    @Override
    public boolean canDropFromBottom()
    {
        return true;
    }
    
    @Override
    public boolean canGrowFire()
    {
        return false;
    }
    
    @Override
    public boolean canBeEaten()
    {
        return false;
    }
    
    @Override
    public boolean canBeReplaced()
    {
        return false;
    }
    
    @Override
    protected Item doCopy()
    {
        return new Diamond();
    }

    @Override
    public void dropFromBottom()
    {
        ctx.score.droppedDiamond();
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        return ExplodeAction.NONE;
    }
}
