package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Slice;
import com.ait.lienzo.shared.core.types.ColorName;
import com.enno.dotz.client.Config;

public class ColorBomb extends Item
{   
    public ColorBomb()
    {
    }

    @Override
    public Integer getColor()
    {
        return Config.WILD_ID;
    }
    
    @Override
    public boolean canConnect()
    {
        return false;
    }

    @Override
    public boolean canGrowFire()
    {
        return true; //TODO or not?
    }

    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group g = new Group();
        
        int n = Config.MAX_COLORS;
        
        double da = Math.PI * 2 / n;
        for (int i = 0; i < n; i++)
        {
            Slice slice = new Slice(size / 4, i * da, (i + 1) * da, false);
            slice.setFillColor(Config.COLORS[i]);
            slice.setStrokeColor(ColorName.BLACK);
            g.add(slice);
        }
          
        return g;
    }

    @Override
    protected Item doCopy()
    {
        return new ColorBomb();
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        return ExplodeAction.REMOVE; //TODO
    }
}
