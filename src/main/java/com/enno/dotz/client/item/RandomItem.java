package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.google.gwt.dom.client.Style.FontWeight;

public class RandomItem extends Item
{
    public RandomItem()
    {
    }
    
    public RandomItem(boolean stuck)
    {
        m_stuck = stuck;
    }
    
    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group g = new Group();
        
        if (isStuck())
            g.add(createStuckShape(size));
        
        Circle bg = new Circle(size / 4);
        bg.setFillColor(ColorName.WHITE);
        g.add(bg);
        
        Circle c = new Circle(size / 4 - 1); // minus half the strokeWidth
        c.setStrokeColor(ColorName.BLACK);
        c.setStrokeWidth(2);
        g.add(c);
        
        Text m_text = new Text("?");
        m_text.setFillColor(ColorName.BLACK);
        m_text.setFontSize(9);
        m_text.setFontStyle(FontWeight.BOLD.getCssName());
        m_text.setTextAlign(TextAlign.CENTER);
        m_text.setY(1);
        m_text.setTextBaseLine(TextBaseLine.MIDDLE); // y position is position of top of the text
        g.add(m_text);
        
        return g;
    }

    @Override
    protected Item doCopy()
    {
        return new RandomItem(m_stuck);
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        // not applicable
        return ExplodeAction.NONE;
    }
}
