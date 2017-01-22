package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Ellipse;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Line;
import com.ait.lienzo.client.core.shape.MultiPath;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.shared.core.types.Color;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;

public class Chest extends Item
{
    protected Item m_item;
    private int m_strength;
    private Text m_text;
    private Circle m_strengthCircle;
    
    public Chest(Item item, int strength)
    {
        m_strength = strength;
        m_item = item;
    }
    
    public Chest(Item item, int strength, boolean stuck)
    {
        this(item, strength);
        setStuck(stuck);
    }
    
    @Override
    public boolean canReshuffle()
    {
        return false;
    }

    @Override
    public boolean canExplodeNextTo()
    {
        return true;
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
    public boolean canSwap()
    {
        return false;
    }
    
    @Override
    public boolean canDrop()
    {
        return false;
    }

    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group g = new Group();
        
        if (isStuck())
            g.add(createStuckShape(size));
        
        double w = size * 0.7;
        double h = size * 0.35;
        
        double x = -w * 0.5;
        double y = -h * 0.25;
        
        Rectangle re = new Rectangle(w, h);
        re.setStrokeColor(ColorName.BLACK);
        re.setFillColor(Color.fromColorString("#A95500"));
        re.setX(x);
        re.setY(y);
        g.add(re);
        
        MultiPath path = new MultiPath();
        path.M(x, y);
        path.A(w * 0.5, w * 0.2, 0, 0, 1, x + w, y);
        path.Z();
        path.setStrokeColor(ColorName.BLACK);
        path.setFillColor(Color.fromColorString("#A95500"));
        g.add(path);
        
        double w2 = h * 0.6;
        re = new Rectangle(w2, w2);
        re.setFillColor(Color.fromColorString("#EBB30A"));
        re.setX(-w2 * 0.5);
        re.setY(y + h/2 - w2/2);
        g.add(re);
        
        Ellipse el = new Ellipse(w2 * 0.3, w2 * 0.8);
        el.setFillColor(ColorName.BLACK);
        el.setY(y + h/2);
        g.add(el);
        
        double dx = w * 0.08;
        double dy = h * 0.18;
        Line line = new Line(x + dx, y - dy, x + w - dx, y - dy);
        line.setStrokeColor(ColorName.BLACK);
        g.add(line);
        
        double r = size * 0.16;
        double p = size * 0.3 - r / 3;
        
        m_strengthCircle = new Circle(r);
        m_strengthCircle.setFillColor(ColorName.WHITE);
        m_strengthCircle.setStrokeColor(ColorName.BLACK);
        m_strengthCircle.setX(-p);
        m_strengthCircle.setY(p);
        g.add(m_strengthCircle);
        
        m_text = new Text("" + m_strength);
        m_text.setFontSize(9);
        m_text.setFontStyle("bold");
        m_text.setTextAlign(TextAlign.CENTER);
        m_text.setTextBaseLine(TextBaseLine.MIDDLE);
        m_text.setFillColor(ColorName.BLACK);
        m_text.setX(-p);
        m_text.setY(p);
        g.add(m_text);
        
        if (m_strength < 2)
        {
            m_text.setVisible(false);
            m_strengthCircle.setVisible(false);
        }
        
        return g;
    }
    
    @Override
    public void incrementStrength(int ds)
    {
        if (ds == -1 && m_strength <= 1)
            return;
        
        setStrength(m_strength + ds);
    }
    
    @Override
    protected Item doCopy()
    {
        return new Chest(m_item == null ? null : m_item.copy(), m_strength, m_stuck);
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        if (m_strength <= 1)
        {
            //TODO ctx.score.openedChest();
            return ExplodeAction.OPEN; // open Chest
        }
        
        setStrength(m_strength - 1);
        
        return ExplodeAction.NONE;
    }
    
    public Item getItem()
    {
        return m_item;
    }
    
    public void setItem(Item item)
    {
        m_item = item;
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
            
            m_strengthCircle.setVisible(m_strength > 1);
        }
    }
}
