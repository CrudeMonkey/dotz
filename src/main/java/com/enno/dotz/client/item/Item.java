package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Layer;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Context;

public abstract class Item
{
    public enum ExplodeAction
    {
        REMOVE,
        EXPLODY,
        NONE
    };
    
    private static int s_counter = 0;
    
    public Context ctx;
    public Config cfg;
    public Integer id;      // unique number for each Item - still used in AnimList

    public IPrimitive<?> shape;

    protected Item()
    {
        id = s_counter++;
    }
    
    // only used by ScorePanel and Item
    public void setContext(Context ctx)
    {
        this.ctx = ctx;
        this.cfg = ctx.cfg;
    }
    
    public abstract IPrimitive<?> createShape(double size);
    
    public void init(Context ctx)
    {
        setContext(ctx);
        shape = createShape(cfg.size);
    }
    
    public Integer getColor()
    {
        return null;
    }

    public boolean canDropFromBottom()
    {
        return false;
    }
    
    public boolean canConnect()
    {
        return false;
    }

    public boolean canExplodeNextTo()
    {
        return false;
    }
    
    public boolean canGrowFire()
    {
        return false;
    }

    public boolean canBeEaten()
    {
        return true;
    }
    
    public boolean canChangeColor()
    {
        return false;
    }

    public boolean stopsLaser()
    {
        return false;
    }
    
    /** Used by layout editor to rotate Mirror and Laser */
    public boolean canRotate()
    {
        return false;
    }
    
    /** Used by layout editor to rotate Mirror and Laser */
    public void rotate()
    {
    }
    
    public void moveShape(double x, double y)
    {
        shape.setX(x);
        shape.setY(y);
    }
    
    public void animate(long t, double cursorX, double cursorY)
    {        
    }
    
    protected abstract Item doCopy();
    
    public Item copy()
    {
        Item c = doCopy();
        c.id = id;
        return c;
    }

    public void dropFromBottom()
    {        
    }
    
    /**
     *
     * @param color
     * @param chainSize
     * @return Whether the item should be removed.
     */
    public abstract ExplodeAction explode(Integer color, int chainSize);

    public void addShapeToLayer(Layer layer)
    {
        layer.add(shape);
    }

    public void removeShapeFromLayer(Layer layer)
    {
        layer.remove(shape);
    }
}
