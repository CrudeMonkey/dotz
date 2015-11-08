package com.enno.dotz.client.box;

import com.ait.lienzo.client.core.event.NodeMouseClickHandler;
import com.ait.lienzo.shared.core.types.TextAlign;

public class BoxButtonPanel extends BoxVLayout
{
    private BoxHLayout m_inner;

    public BoxButtonPanel()
    {
        m_inner = new BoxHLayout();
        m_inner.setAlign(TextAlign.CENTER);
        m_inner.setPadding(0);
        m_inner.setSpacing(15);
        addMember(m_inner);
        
        setPadding(0);
        setWidth(Extent.FULL);
    }
    
    public void addButton(String text, NodeMouseClickHandler onClick)
    {
        BoxButton button = new BoxButton(text);
        button.addNodeMouseClickHandler(onClick);
        m_inner.addMember(button);
    }
}
