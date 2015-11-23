package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.MultiPath;
import com.ait.lienzo.shared.core.types.ColorName;

public class Mirror extends Item
{
    public boolean m_flipped; // false: \    true: /
            
    public Mirror(boolean flipped)
    {
        m_flipped = flipped;
    }
    
    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group group = new Group();
        
        double sz = size * 0.6;
        double sz2 = sz / 2;
        double r = size * 1; // greater r means flatter
        
        MultiPath p = new MultiPath();
        p.M(sz2, sz2);
        p.A(r, r, 135, 0, 0, -sz2, -sz2);
        p.A(r, r, 215, 0, 0, sz2, sz2);
        p.Z();
        
        p.setFillColor(ColorName.LIGHTBLUE);
        p.setStrokeColor(ColorName.DARKBLUE);
        p.setStrokeWidth(1);
        group.add(p);

        if (m_flipped)
            group.setRotation(Math.PI / 2);
        
        return group;
    }
    
    @Override
    public boolean canDropFromBottom()
    {
        return true;
    }
    
    @Override
    public boolean canRotate()
    {
        return true;
    }

    @Override
    public boolean canBeEaten()
    {
        return false;
    }
    
    @Override
    public void rotate(int n) // ignore n
    {
        m_flipped = !m_flipped;
        shape.setRotation(m_flipped ? Math.PI / 2 : 0);
    }
    
    @Override
    protected Item doCopy()
    {
        return new Mirror(m_flipped);
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
        ctx.score.explodedMirror();
        return ExplodeAction.REMOVE; // remove mirror
    }

    public boolean isFlipped()
    {
        return m_flipped;
    }

    public void setFlipped(boolean flipped)
    {
        m_flipped = flipped;
    }
}
