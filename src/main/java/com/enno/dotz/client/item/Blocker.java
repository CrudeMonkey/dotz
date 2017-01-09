package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Ellipse;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.google.gwt.dom.client.Style.FontWeight;

/**
 * Inspired by the Slime Guard in Jelly Splash.
 * They act like a Door, so you must explode things next to it, but they can drop like regular items.
 * 
 * @author Enno
 */
public class Blocker extends Item
{
    private int m_strength;
    private Text m_text;
    
    public Blocker(int strength, boolean stuck)
    {
        m_strength = strength;
        m_stuck = stuck;
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
    public boolean canIncrementStrength()
    {
        return true;
    }
    
    @Override
    public void incrementStrength(int ds)
    {
        if (m_strength <= 1 && ds == -1)
            return;
        
        setStrength(m_strength + ds);
    }

    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group g = new Group();
        
        if (isStuck())
            g.add(createStuckShape(size));
                
        double r = size * 0.9;
        Ellipse c = new Ellipse(r, r * 0.5);
        c.setFillColor(ColorName.PURPLE);        
        g.add(c);
        
        m_text = new Text("" + m_strength);
        m_text.setFillColor(ColorName.WHITE);
        m_text.setFontSize(7);
        m_text.setFontStyle(FontWeight.BOLD.getCssName());
        m_text.setTextAlign(TextAlign.CENTER);
        //m_text.setY(-3);
        m_text.setTextBaseLine(TextBaseLine.MIDDLE); // y position is position of top of the text
        g.add(m_text);
        
        m_text.setVisible(m_strength > 1);
        
        return g;
    }
    
    @Override
    protected Item doCopy()
    {
        return new Blocker(m_strength, m_stuck);
    }

    @Override
    public boolean canExplodeNextTo()
    {
        return true;
    }
    
    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        if (m_strength <= 1)
        {
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
