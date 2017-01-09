package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.SVGPath;
import com.ait.lienzo.shared.core.types.ColorName;

public class Anchor extends Item
{
    public Anchor(boolean stuck)
    {
        m_stuck = stuck;
    }
    
    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group group = new Group();
        
        if (isStuck())
            group.add(createStuckShape(size));
        
        Circle outer = new Circle(size / 4 - 1);  // minus half the strokeWidth
//        outer.setStrokeColor(ColorName.BLACK);
//        outer.setStrokeWidth(2);
//        outer.setFillColor(ColorName.WHITE);
        outer.setFillColor(ColorName.BLACK);
        group.add(outer);

//        Circle inner = new Circle(size / 8);
//        inner.setFillColor(ColorName.BLACK);
//        group.add(inner);

        SVGPath p = new SVGPath("M15.25,10.5c-0.553,0-1,0.447-1,1c0,2.414-1.721,4.434-4,4.898V9h4c0.553,0,1-0.447,1-1" + 
                "s-0.447-1-1-1h-4V5.816c1.162-0.413,2-1.511,2-2.816c0-1.657-1.343-3-3-3s-3,1.343-3,3c0,1.305,0.838,2.403,2,2.816V7h-4" +
            "c-0.553,0-1,0.447-1,1s0.447,1,1,1h4v7.398c-2.279-0.465-4-2.484-4-4.898c0-0.553-0.447-1-1-1s-1,0.447-1,1c0,3.859,3.141,7,7,7" +
            "s7-3.141,7-7C16.25,10.947,15.803,10.5,15.25,10.5z M9.25,2c0.551,0,1,0.449,1,1s-0.449,1-1,1s-1-0.449-1-1S8.699,2,9.25,2z");
        p.setFillColor(ColorName.WHITE);
        p.setX(-7);
        p.setY(-8);
        p.setScale(0.8);
        group.add(p);
        
        return group;
    }
    
    @Override
    public boolean canDropFromBottom()
    {
        return true;
    }
    
    @Override
    public boolean canGrowFire()
    {
        return true;
    }
    
    @Override
    protected Item doCopy()
    {
        return new Anchor(m_stuck);
    }

    @Override
    public void dropFromBottom()
    {
        ctx.score.droppedAnchor();
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        // too bad - no points
        ctx.score.explodedAnchor();
        return ExplodeAction.REMOVE; // remove anchor
    }
}
