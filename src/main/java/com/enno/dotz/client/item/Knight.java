package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.SVGPath;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.client.core.types.Transform;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.google.gwt.dom.client.Style.FontWeight;

public class Knight extends Item
{
    private static Transform s_trans = new Transform(0.866,0.5,-0.5,0.866,9.693,-5.173);
 
    private Text m_text;
    private int m_strength;
    
    public Knight(int strength, boolean stuck)
    {
        m_strength = strength;
        m_stuck = stuck;
    }
    
    public int getStrength()
    {
        return m_strength;
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
        
        m_strength += ds;
        
        if (m_text != null)
        {
            m_text.setVisible(m_strength > 1);
            m_text.setText("" + m_strength);
        }
    }
    
    public void setStrength(int strength)
    {
        m_strength = strength;        
    }
    
    @Override
    public IPrimitive<?> createShape(double sz)
    {
        Group shape = new Group();
        
        if (isStuck())
            shape.add(createStuckShape(sz));
        
        Group g = new Group();
        g.setX(-sz / 2);
        g.setY(-sz / 2);
        if (sz != 50)
            g.setScale(sz / 50);
        
        SVGPath p = new SVGPath("M 22,10 C 32.5,11 38.5,18 38,39 L 15,39 C 15,30 25,32.5 23,18");
        p.setFillColor(ColorName.BLACK);
        g.add(p);
        
        p = new SVGPath("M 24,18 C 24.38,20.91 18.45,25.37 16,27 C 13,29 13.18,31.34 11,31 C 9.958,30.06 12.41,27.96 11,28 C 10,28 11.19,29.23 10,30 C 9,30 5.997,31 6,26 C 6,24 12,14 12,14 C 12,14 13.89,12.1 14,10.5 C 13.27,9.506 13.5,8.5 13.5,7.5 C 14.5,6.5 16.5,10 16.5,10 L 18.5,10 C 18.5,10 19.28,8.008 21,7 C 22,7 22,10 22,10");
        p.setFillColor(ColorName.BLACK);
        g.add(p);
        
        p = new SVGPath("M 9.5 25.5 A 0.5 0.5 0 1 1 8.5,25.5 A 0.5 0.5 0 1 1 9.5 25.5 z");
        p.setFillColor(ColorName.WHITE);
        g.add(p);
        
        p = new SVGPath("M 15 15.5 A 0.5 1.5 0 1 1  14,15.5 A 0.5 1.5 0 1 1  15 15.5 z")
        {
            @Override
            protected Transform getPossibleNodeTransform()
            {
                return s_trans;
            }
        };
        p.setFillColor(ColorName.WHITE);
        g.add(p);
        
        p = new SVGPath("M 24.55,10.4 L 24.1,11.85 L 24.6,12 C 27.75,13 30.25,14.49 32.5,18.75 C 34.75,23.01 35.75,29.06 35.25,39 L 35.2,39.5 L 37.45,39.5 L 37.5,39 C 38,28.94 36.62,22.15 34.25,17.66 C 31.88,13.17 28.46,11.02 25.06,10.5 L 24.55,10.4 z ");
        p.setFillColor(ColorName.WHITE);
        g.add(p);
        
        shape.add(g);
        
        m_text = new Text("" + m_strength);
        m_text.setFillColor(ColorName.WHITE);
        m_text.setFontSize(7);
        m_text.setFontStyle(FontWeight.BOLD.getCssName());
        m_text.setX(-3);
        m_text.setY(5);
        m_text.setTextBaseLine(TextBaseLine.TOP); // y position is position of top of the text
        shape.add(m_text);
        
        if (m_strength <= 1)
            m_text.setVisible(false);
        
        return shape;
    }

    @Override
    protected Item doCopy()
    {
        return new Knight(m_strength, m_stuck);
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        // if chainSize == 1, it's caused by Explody
        if (chainSize > 1)
            chainSize--;
        
        m_strength -= chainSize;        
        if (m_strength <= 0)
        {
            ctx.score.explodedKnight();
            return ExplodeAction.EXPLODY; // turn into Explody
        }
        
        if (m_strength <= 1)
            m_text.setVisible(false);
        else
            m_text.setText("" + m_strength);
        
        return ExplodeAction.NONE; // don't remove knight
    }
}
