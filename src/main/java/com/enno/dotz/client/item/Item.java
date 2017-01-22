package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Line;
import com.ait.lienzo.shared.core.types.ColorName;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Context;

public abstract class Item
{
    public enum ExplodeAction
    {
        REMOVE,
        EXPLODY,
        OPEN,   // Chest
        NONE
    };
    
    private static int s_counter = 0;
    
    public Context ctx;
    public Config cfg;
    public Integer id;      // unique number for each Item - still used in AnimList

    public IPrimitive<?> shape;

    protected boolean m_stuck;
    
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
    
    public static Group createStuckShape(double sz)
    {
        Group g = new Group();
        
        double s = sz / 2;
        Line l = new Line(-s, -s, s, s);
        l.setStrokeWidth(2);
        l.setStrokeColor(ColorName.DARKGRAY);
        l.setDashArray(2, 2);
        g.add(l);
        
        l = new Line(-s, s, s, -s);
        l.setStrokeWidth(2);
        l.setStrokeColor(ColorName.DARKGRAY);
        l.setDashArray(2, 2);
        g.add(l);
        
        return g;
    }
    
    public boolean isStuck()
    {
        return m_stuck;
    }
    
    public void setStuck(boolean stuck)
    {
        m_stuck = stuck;
    }
    
    public boolean canBeStuck()
    {
        return true;
    }
    
    public boolean isRadioActive()
    {
        return false;
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
    
    public boolean canReshuffle()
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
    
    public boolean canDrop()
    {
        return !isStuck();
    }

    public boolean canChangeColor()
    {
        return false;
    }

    public boolean canSwap()
    {
        return true;
    }
    
    public boolean stopsLaser()
    {
        return false;
    }

    public boolean isArmed()
    {
        return false; // overriden by Striped with armed=true, and Blaster when triggered
    }
    
    // overriden by Striped and Blaster
    public boolean isVertical()
    {
        return false;
    }
    
    /** Used by layout editor to rotate Mirror and Laser */
    public boolean canRotate()
    {
        return false;
    }

    /** Used by layout editor to rotate Mirror and Laser */
    public void rotate(int n)
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
     * @return  Whether +/- keys can modify the strength in the Level Editor.
     */
    public boolean canIncrementStrength()
    {
        return false;
    }

    /**
     * Override for items that can increment/decrement strength in the layout editor (with +/-)
     * 
     * @param ds    {1, -1}
     */
    public void incrementStrength(int ds)
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
