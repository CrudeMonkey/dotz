package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Arc;
import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Triangle;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.shared.core.types.ColorName;
import com.enno.dotz.client.Config;

public class Turner extends Item
{
    public int n; // 1, 2, 3
    private Group m_rotateShape;
    
    public Turner(int n, boolean stuck)
    {        
        this.n = n;
        m_stuck = stuck;
    }
    
    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group g = new Group();
        
        
        double r = size * 0.3;
        Circle bg = new Circle(r);
        bg.setFillColor(ColorName.LIGHTBLUE);
        bg.setStrokeColor(ColorName.DODGERBLUE);
        g.add(bg);
        
        double startAngle = n == 3 ? 0 : Math.PI;
        double endAngle = n == 2 ? 0 : -Math.PI / 2;
        boolean ccw = n == 3;
        Arc c = new Arc(r, startAngle, endAngle, ccw);
        c.setStrokeColor(ColorName.DARKBLUE);
        c.setStrokeWidth(4);
        g.add(c);
        
        double h = r * 0.4;
        double d = h / 2;
        
        Triangle t = n == 3 ?
                new Triangle(new Point2D(d, r), new Point2D(-d, r + h), new Point2D(-d, r - h)) :
                new Triangle(new Point2D(-d, r), new Point2D(d, r + h), new Point2D(d, r - h));
        t.setFillColor(ColorName.DARKBLUE);
        t.setRotation(n == 2 ? -Math.PI/2 : Math.PI);
        g.add(t);
        
        Circle center = new Circle(2);
        center.setFillColor(ColorName.DARKBLUE);
        g.add(center);
        
        m_rotateShape = g;
        
        if (isStuck())
        {
            Group g2 = new Group();
            g2.add(createStuckShape(size));
            g2.add(g);
            return g2;
        }

        return g;
    }

    @Override
    public Integer getColor()
    {
        return Config.WILD_ID; //TODO is this even used?
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
        return new Turner(n, m_stuck);
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        return ExplodeAction.REMOVE;
    }
    
    @Override
    public void animate(long t, double cursorX, double cursorY)
    {
        long speed = 4000;
        double rot = (t % speed) * Math.PI * 2 / speed;
        if (n == 3)
            rot = -rot;
        m_rotateShape.setRotation(rot);
    }
}
