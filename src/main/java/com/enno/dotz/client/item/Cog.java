package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.shared.core.types.ColorName;
import com.enno.dotz.client.util.Gear;

public class Cog extends Item
{
    public Cog()
    {
        
    }
    
    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group group = new Group();
        
        if (isStuck())
            group.add(createStuckShape(size));
        
        double sz = size / 4;
        
//        Gear p = new Gear(8, sz * 0.75, sz, 0.8, 0.3);
        Gear p = new Gear(8, sz * 0.75, sz, 0.6, 0.3);
        
        p.setFillColor(ColorName.LIGHTBLUE);
        p.setStrokeColor(ColorName.DARKBLUE);
        p.setStrokeWidth(1);
        group.add(p);

        double r = sz * 0.3;
        Circle c = new Circle(r);
        c.setFillColor(ColorName.WHITE);
        c.setStrokeColor(ColorName.DARKBLUE);
        group.add(c);
        
        return group;
    }
    
//    @Override
//    public boolean canDropFromBottom()
//    {
//        return true;
//    }
//    
//    @Override
//    public boolean canRotate()
//    {
//        return true;
//    }
//
//    @Override
//    public boolean canBeEaten()
//    {
//        return false;
//    }
//    
//    @Override
//    public void rotate(int n) // ignore n
//    {
//        m_flipped = !m_flipped;
//        shape.setRotation(m_flipped ? Math.PI / 2 : 0);
//    }
    
    @Override
    protected Item doCopy()
    {
        return new Cog();
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
        //ctx.score.explodedMirror();
        return ExplodeAction.NONE;
    }

}
