package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Polygon;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.core.shape.Triangle;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.shared.core.types.ColorName;

/**
 * Inspired by the Blaster in Toy Blast.
 * 
 * @author Enno
 */
public class Blaster extends Item
{
    private boolean m_vertical;
    private boolean m_armed;
    
    private boolean m_bothWays;
    private boolean m_wide;
    
    public Blaster(boolean vertical, boolean stuck)
    {
        m_vertical = vertical;
        m_stuck = stuck;
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
    
    public void setVertical(boolean vertical)
    {
        m_vertical = vertical;
    }
    
    public boolean isVertical()
    {
        return m_vertical;
    }
    
    @Override
    public boolean isArmed()
    {
        return m_armed;
    }
    
    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group group = new Group();
        
        if (isStuck())
            group.add(createStuckShape(size));
                
//        double w = size * 0.3;
//        double h = size * 0.15;
//        Rectangle r = new Rectangle(w, h);
//        r.setStrokeColor(ColorName.BLACK);
//        r.setFillColor(ColorName.LIGHTGRAY);
//        r.setX(-w/2);
//        r.setY(-h/2);
//        g.add(r);
//        
//        double a = size * 0.20; // half the height of the triangle
//        double b = size * 0.25;  // width of triangle
//        double f = b * 0.1;     // corner radius
//        
//        Triangle t = new Triangle(new Point2D(-b - w/2, 0), new Point2D(-w/2, a), new Point2D(-w/2, -a), f);
//        t.setStrokeColor(ColorName.BLACK);
//        t.setFillColor(ColorName.LIGHTGRAY);
//        g.add(t);
//        
//        t = new Triangle(new Point2D(b + w/2, 0), new Point2D(w/2, a), new Point2D(w/2, -a), f);
//        t.setStrokeColor(ColorName.BLACK);
//        t.setFillColor(ColorName.LIGHTGRAY);
//        g.add(t);
        
        double sz = size * 0.8;
        
        double rw = sz * 0.5;
        double rh = sz * 0.3;
        
        double dw1 = 0.65 * rw;
        double dw2 = rw - dw1;
        Rectangle rect = new Rectangle(rw, rh);
        rect.setX(-dw1);
        rect.setY(-rh / 2);
        rect.setFillColor(ColorName.CORNFLOWERBLUE);
        rect.setStrokeWidth(1);
        rect.setStrokeColor(ColorName.CRIMSON);
        group.add(rect);
        
        double skew = sz * 0.2;
        double bw = skew/2;
        double bx = -dw1; //-(dw1 + bw) /2;
        Polygon band = new Polygon(bx, rh/2, skew + bx, -rh/2, 
                skew + bx + bw, -rh/2, bx + bw, rh/2);
        band.setFillColor(ColorName.YELLOW);
        band.setStrokeWidth(1);
        band.setStrokeColor(ColorName.CRIMSON);
        group.add(band);
        
        bx += skew+bw/2;
        band = new Polygon(bx, rh/2, skew + bx, -rh/2, 
                skew + bx + bw, -rh/2, bx + bw, rh/2);
        band.setFillColor(ColorName.YELLOW);
        band.setStrokeWidth(1);
        band.setStrokeColor(ColorName.CRIMSON);
        group.add(band);
        
        double tw = sz * 0.4;
        double th = rh;
        Triangle tri = new Triangle(new Point2D(dw2, -th), new Point2D(dw2 + tw, 0), new Point2D(dw2, th));
        tri.setFillColor(ColorName.CRIMSON);
        group.add(tri);

        tri = new Triangle(new Point2D(-dw2, -th), new Point2D(-dw2 - tw, 0), new Point2D(-dw2, th));
        tri.setFillColor(ColorName.CRIMSON);
        group.add(tri);

        updateRotation(group);
        
        return group;
    }
    
    private void updateRotation(IPrimitive<?> group)
    {
        group.setRotation(m_vertical ? Math.PI / 2 : 0);
    }

    @Override
    public boolean canRotate()
    {
        return true;
    }
    
    @Override
    public void rotate(int n)
    {
        m_vertical = !m_vertical;
        
        updateRotation(shape);
    }
    
    protected Item doCopy()
    {
        return new Blaster(m_vertical, m_stuck);
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        arm();
        
        return ExplodeAction.NONE;
    }

    public void arm()
    {
        m_armed = true;
    }
}
