package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.IColor;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.util.ColorUtil;

public class WrappedDot extends Item
{
    public int color;
    private Rectangle m_border;
    
    public WrappedDot(int color)
    {
        this.color = color;
    }

    @Override
    public String getType()
    {
        return "wrappedDot";
    }

    @Override
    public Integer getColor()
    {
        return color;
    }

    public void setColor(int color)
    {
        this.color = color;
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
    public boolean canChangeColor()
    {
        return true;
    }

    @Override
    public boolean canReshuffle()
    {
        return true;
    }
    
    @Override
    public boolean canBeLaunched()
    {
        return false;   // see MachineType.WRAP
    }
   
    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group g = new Group();
        
        if (isStuck())
            g.add(createStuckShape(size));
        
        Circle c = new Circle(size / 4);
        IColor fillColor = cfg == null ? Config.COLORS[0] : cfg.drawColor(color);
        c.setFillColor(fillColor);       // cfg is null in ModePalette  
        g.add(c);
        
        Dot.addMark(color, g, size);
        
        IColor dark = ColorUtil.darker(fillColor, 0.5);
        
        double cr = size * 0.1;
        double sz = size * 0.6; // 0.75;
        
        Rectangle r = new Rectangle(sz, sz);
        r.setX(-sz/2);
        r.setY(-sz/2);
        r.setFillColor(fillColor);
        r.setAlpha(0.5);
        r.setDashArray(2);
        r.setCornerRadius(cr);
        g.add(r);
        
        r = new Rectangle(sz, sz);
        r.setX(-sz/2);
        r.setY(-sz/2);
        r.setStrokeColor(dark);
        r.setStrokeWidth(2);
        r.setDashArray(2);
        r.setCornerRadius(cr);
        g.add(r);
        
        m_border = r;
        
        return g;
    }

    @Override
    public void animate(long t, double cursorX, double cursorY)
    {
        int n = (int) ((t / 200) % 2);
        m_border.setDashOffset(n);
        
        super.animate(t, cursorX, cursorY);
    }
    
    @Override
    protected Item doCopy()
    {
        return new WrappedDot(color);
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        ctx.score.usedWrappedDot();
        return ExplodeAction.EXPLODY;
    }
}
