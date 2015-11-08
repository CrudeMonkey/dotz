package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Star;
import com.ait.lienzo.shared.core.types.ColorName;

public class Fire extends Item
{
    public Fire()
    {        
    }
    
    @Override
    public IPrimitive<?> createShape(double size)
    {
        double sz = size / 4;
        Star star = new Star(11, 0.7 * sz, sz);
        star.setStrokeColor(ColorName.RED);
        star.setStrokeWidth(1);
        star.setFillColor(ColorName.YELLOW);
        return star;
    }
    
    protected Item doCopy()
    {
        return new Fire();
    }

    public boolean canExplodeNextTo()
    {
        return true;
    }
    
    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        ctx.score.explodedFire();
        return ExplodeAction.REMOVE; // remove fire
    }

    @Override
    public void addShapeToLayer(Layer layer)
    {
        super.addShapeToLayer(layer);
    }

    @Override
    public void removeShapeFromLayer(Layer layer)
    {
        super.removeShapeFromLayer(layer);
    }
    
    @Override
    public void animate(long t, double cursorX, double cursorY)
    {
        long speed = 4000;
        double rot = (t % speed) * Math.PI * 2 / speed;
        shape.setRotation(rot);
    }
}
