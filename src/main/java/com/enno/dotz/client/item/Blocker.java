package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Ellipse;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.shared.core.types.Color;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.enno.dotz.client.Config;
import com.google.gwt.dom.client.Style.FontWeight;

/**
 * Inspired by the Slime Guard in Jelly Splash.
 * They act like a Door, so you must explode things next to it, but they can drop like regular items. These are green.
 * 
 * If zapOnly=true, then you can't explode things next to it but you have to zap them (these are red.)
 * 
 * @author Enno
 */
public class Blocker extends Item
{
    private int m_strength;
    private Text m_text;
    private boolean m_zapOnly;
    
    public Blocker(int strength, boolean stuck, boolean zapOnly)
    {
        m_strength = strength;
        m_stuck = stuck;
        m_zapOnly = zapOnly;
    }
    
    public int getStrength()
    {
        return m_strength;
    }
    
    public void setStrength(int strength)
    {
        m_strength = strength;
        
        if (m_text != null)
        {
            m_text.setVisible(m_strength > 1);
            m_text.setText("" + m_strength);
        }
    }
    
    @Override
    public boolean stopsLaser()
    {
        return true;
    }
    
    public boolean isZapOnly()
    {
        return m_zapOnly;
    }

    @Override
    public boolean canIncrementStrength()
    {
        return true;
    }
    
    @Override
    public boolean canBeEaten()
    {
        return false;
    }
    
    @Override
    public boolean canGrowFire()
    {
        return false;
    }
    
    @Override
    public void incrementStrength(int ds)
    {
        if (m_strength <= 1 && ds == -1)
            return;
        
        setStrength(m_strength + ds);
    }

    @Override
    public IPrimitive<?> createShape(double sz)
    {
        Group g = new Group();
        
        if (isStuck())
            g.add(createStuckShape(sz));

        sz *= 0.9;
        
        Ellipse e = new Ellipse(sz * 0.8, sz * 0.75);   // cap
        e.setFillColor(m_zapOnly ? ColorName.RED : Color.fromColorString("#7BD359"));
        e.setStrokeColor(ColorName.BLACK);
        g.add(e);
        
        double y = sz * 0.25;   // face
        e = new Ellipse(sz * 0.45, sz * 0.35);
        e.setFillColor(Color.fromColorString("#C2B59B"));
        e.setStrokeColor(ColorName.BLACK);
        e.setY(y);
        g.add(e);
        
        double x = sz * 0.08;
        for (int i = 0; i < 2; i++) // eyes
        {
            e = new Ellipse(sz * 0.06, sz * 0.18);
            e.setFillColor(ColorName.BLACK);
            e.setY(y * 0.85);
            e.setX(i == 0 ? x : -x);
            g.add(e);
        }
        
        x = sz * 0.32;
        for (int i = 0; i < 2; i++) // white spots on the side
        {
            e = new Ellipse(sz * 0.085, sz * 0.3);
            e.setFillColor(ColorName.WHITE);
            e.setStrokeColor(ColorName.BLACK);
            e.setX(i == 0 ? x : -x);
            g.add(e);
        }
        
        Circle c = new Circle(sz * 0.16);   // circle with strength
        c.setFillColor(ColorName.WHITE);
        c.setStrokeColor(ColorName.BLACK);
        c.setY(sz * -0.14);
        g.add(c);
        
        m_text = new Text("" + m_strength);
        m_text.setFillColor(ColorName.BLACK);
        m_text.setFontStyle(FontWeight.BOLD.getCssName());
        m_text.setTextAlign(TextAlign.CENTER);
        m_text.setTextBaseLine(TextBaseLine.MIDDLE);
        
        if (sz < Config.DEFAULT_CELL_SIZE *0.9)  // inside Machine
        {
            m_text.setFontSize(6);
            m_text.setY(sz * -0.14 + 1);
        }
        else
        {
            m_text.setFontSize(8);
            m_text.setY(sz * -0.14);
        }
        g.add(m_text);
        
        m_text.setVisible(m_strength > 1);
        
        return g;
    }
    
    @Override
    protected Item doCopy()
    {
        return new Blocker(m_strength, m_stuck, m_zapOnly);
    }

    @Override
    public boolean canExplodeNextTo()
    {
        return !m_zapOnly;
    }
    
    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        if (m_strength <= 1)
        {
            if (m_zapOnly)
                ctx.score.explodedZapBlocker();
            else
                ctx.score.explodedBlocker();
            return ExplodeAction.REMOVE; // remove Blocker
        }
        
        setStrength(m_strength - 1);
        
        return ExplodeAction.NONE;
    }
    
//    @Override
//    public void animate(long t, double cursorX, double cursorY)
//    {
//        long speed = 4000;
//        double rot = (t % speed) * Math.PI * 2 / speed;
//        shape.setRotation(rot);
//    }
}
