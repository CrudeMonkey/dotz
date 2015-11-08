package com.enno.dotz.client.box;

import com.ait.lienzo.client.core.event.NodeMouseClickEvent;
import com.ait.lienzo.client.core.event.NodeMouseClickHandler;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.smartgwt.client.util.BooleanCallback;

public class BoxDialog extends BoxVLayout
{
    private BoxVLayout m_content;
    private BoxButtonPanel m_buttonPanel;

    public BoxDialog(String title)
    {
        setSpacing(0);
        
        BoxDefaults b = BoxDefaults.INSTANCE;
     
        if (title != null)
        {
            BoxVLayout header = new BoxVLayout();
            header.setWidth(Extent.FULL);
            header.setPadding(5, b.padding, 5, b.padding);
            
            Rectangle bg = new Rectangle(0, 0);
            bg.setFillColor(b.headerBackground);
            bg.setStrokeColor(b.borderColor);
            bg.setCornerRadius(b.cornerRadius);
            bg.setStrokeWidth(2);
            header.setBackground(bg);
            
            Text text = new Text(title);
            text.setTextAlign(TextAlign.LEFT);
            text.setTextBaseLine(TextBaseLine.MIDDLE);
            text.setFontSize(b.fontSize);
            text.setFillColor(b.headerTextColor);
            text.setFontStyle("bold");
            
            TextBox titleText = new TextBox(text);
            
            header.addMember(titleText);
            
            addMember(header);
        }
        
        m_content = new BoxVLayout();
        m_content.setWidth(Extent.FULL);
        m_content.setSpacing(15);
        
        Rectangle bg = new Rectangle(0, 0);
        bg.setFillColor(ColorName.WHITE);
        bg.setStrokeColor(BoxDefaults.INSTANCE.borderColor);
        bg.setCornerRadius(BoxDefaults.INSTANCE.cornerRadius);
        bg.setStrokeWidth(2);
        m_content.setBackground(bg);
        
        addMember(m_content);
    }
    
    public void setInnerContent(Box box)
    {
        m_content.addMember(box);
    }
    
    public void setInnerText(String... lines)
    {
        BoxDefaults b = BoxDefaults.INSTANCE;
        
        BoxVLayout innerText = new BoxVLayout();
        innerText.setSpacing(5);
        innerText.setPadding(0);
        for (int i = 0; i < lines.length; i++)
        {
            Text t = new Text(lines[i]);
            t.setFontSize(b.fontSize);
            t.setFillColor(b.textColor);
            t.setTextAlign(TextAlign.CENTER);
            t.setTextBaseLine(TextBaseLine.TOP); //TODO center
            
            TextBox tb = new TextBox(t);
            tb.setWidth(Extent.FULL);
            innerText.addMember(tb);
        }
        setInnerContent(innerText);
    }
    
    public void addButton(String text, NodeMouseClickHandler onClick)
    {
        if (m_buttonPanel == null)
        {
            m_buttonPanel = new BoxButtonPanel();
            m_content.addMember(m_buttonPanel);
        }
        
        m_buttonPanel.addButton(text, onClick);
    }

    public static BoxDialog say(String title, String... lines)
    {
        BoxDialog d = new BoxDialog(title);
        d.setInnerText(lines);
        return d;
    }
    
    public static BoxDialog ask(String title, final BooleanCallback cb, String... lines)
    {
        BoxDialog d = new BoxDialog(title);
        d.setInnerText(lines);
        
        d.addButton("OK", new NodeMouseClickHandler()
        {            
            @Override
            public void onNodeMouseClick(NodeMouseClickEvent event)
            {
                cb.execute(Boolean.TRUE);
            }
        });
        d.addButton("Cancel", new NodeMouseClickHandler()
        {            
            @Override
            public void onNodeMouseClick(NodeMouseClickEvent event)
            {
                cb.execute(Boolean.FALSE);
            }
        });
        
        return d;
    }
}
