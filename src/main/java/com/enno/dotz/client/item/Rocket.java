package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Polygon;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.core.shape.Triangle;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.shared.core.types.ColorName;
import com.enno.dotz.client.Direction;

public class Rocket extends Item
{
    public int m_direction;
    private Point2D m_ignitionPoint;
            
    public Rocket(int direction, boolean stuck)
    {
        m_direction = direction;
        m_stuck = stuck;
    }

    @Override
    public String getType()
    {
        return "rocket";
    }

    public void setDirection(int direction)
    {
        m_direction = direction;
    }
    
    public int getDirection()
    {
        return m_direction;
    }
    
    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group group = new Group();
        
        if (isStuck())
            group.add(createStuckShape(size));
        
        double sz = size * 0.8;
        
        double rw = sz * 0.5;
        double rh = sz * 0.3;
        
        double dw1 = 0.7 * rw;
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
//        tri.setStrokeWidth(1);
//        tri.setStrokeColor(ColorName.YELLOW);
        group.add(tri);
        
        double sw = sz * 0.2;
        double sh = rh / 3;
        Rectangle s = new Rectangle(sw, sh);
        s.setX(-dw1 - sw);
        s.setY(-rh /2);
        s.setFillColor(ColorName.BURLYWOOD);
        group.add(s);
        
        m_ignitionPoint = new Point2D(-dw1-sw/2, -sh/2);
        
//        MultiPath p = new MultiPath();
//        p.M(sz2, sz2);
//        p.A(r, r, 135, 0, 0, -sz2, -sz2);
//        p.A(r, r, 215, 0, 0, sz2, sz2);
//        p.Z();
//        
//        p.setFillColor(ColorName.LIGHTBLUE);
//        p.setStrokeColor(ColorName.DARKBLUE);
//        p.setStrokeWidth(1);
//        group.add(p);
//
        
        updateRotation(group);
        
        return group;
    }
    
    public Point2D getIgnitionPoint()
    {
        switch (m_direction)
        {
            case Direction.EAST: return m_ignitionPoint;
            case Direction.SOUTH: return new Point2D(-m_ignitionPoint.getY(), m_ignitionPoint.getX());
            case Direction.WEST: return new Point2D(-m_ignitionPoint.getX(), -m_ignitionPoint.getY());
            case Direction.NORTH: return new Point2D(m_ignitionPoint.getY(), -m_ignitionPoint.getX());
        }
        return null;
    }
    
    
    @Override
    public boolean canDropFromBottom()
    {
        return false;
    }
    
    @Override
    public boolean canRotate()
    {
        return true;
    }
    
    @Override
    public void rotate(int n)
    {
        for (int i = 0; i < n; i++)
            m_direction = Direction.rotate(m_direction, true);
        
        updateRotation(shape);
    }
    
    protected void updateRotation(IPrimitive<?> shape)
    {
        switch (m_direction)
        {
            case Direction.EAST: shape.setRotation(0); break;
            case Direction.SOUTH: shape.setRotation(Math.PI / 2); break;
            case Direction.WEST: shape.setRotation(Math.PI); break;
            case Direction.NORTH: shape.setRotation(-Math.PI / 2); break;
        }
    }

    @Override
    protected Item doCopy()
    {
        return new Rocket(m_direction, m_stuck);
    }

//    @Override
//    public void dropFromBottom()
//    {
//        ctx.score.droppedAnchor();
//    }
//
    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        // too bad - no points
        ctx.score.explodedRocket();
        return ExplodeAction.REMOVE; // remove rocket
    }

}
