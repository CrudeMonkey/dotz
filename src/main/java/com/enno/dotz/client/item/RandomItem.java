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
    private boolean m_radioActive;

    public RandomItem()
    {
    }
    
    public RandomItem(boolean stuck, boolean radioActive)
    {
        m_stuck = stuck;
        m_radioActive = radioActive;
    }

    @Override
    public String getType()
    {
        return "random";
    }
    
    @Override
    public boolean isRadioActive()
    {
        return m_radioActive;
    }
    
    public void setRadioActive(boolean radioActive)
    {
        m_radioActive = radioActive;
    }
    
    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group g = new Group();
        
        //boolean isEditing = ctx == null ? true : ctx.isEditing;
        
        if (isStuck())
            g.add(createStuckShape(size));
        
        Circle bg = new Circle(size / 4);
        bg.setFillColor(ColorName.WHITE);
        bg.setStrokeWidth(2);
        bg.setStrokeColor(ColorName.BLACK);
        
        if (m_radioActive)
        {
            bg.setDashArray(2);
        }
        
        g.add(bg);
        
//        Circle c = new Circle(size / 4 - 1); // minus half the strokeWidth
//        c.setStrokeColor(ColorName.BLACK);
//        c.setStrokeWidth(2);
//        g.add(c);
        
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
        return new RandomItem(m_stuck, m_radioActive);
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        // not applicable
        return ExplodeAction.NONE;
    }
}
