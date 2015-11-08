package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.shared.core.types.ColorName;
import com.enno.dotz.client.Config;

public class Wild extends Item
{
    public Wild()
    {        
    }
    
    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group g = new Group();
        
        Circle bg = new Circle(size / 4);
        bg.setFillColor(ColorName.WHITE);
        g.add(bg);
        
        Circle c = new Circle(size / 4 - 1); // minus half the strokeWidth
        c.setStrokeColor(ColorName.BLACK);
        c.setStrokeWidth(2);
        g.add(c);
        
        return g;
    }

    @Override
    public Integer getColor()
    {
        return Config.WILD_ID; //TODO is this even used?
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
    protected Item doCopy()
    {
        return new Wild();
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        if (color == null)
            ctx.score.explodedWildcard();
        else            
            ctx.score.explodedDot(color);
        
        return ExplodeAction.REMOVE; // remove wildcard
    }
}
