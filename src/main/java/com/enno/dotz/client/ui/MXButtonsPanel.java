package com.enno.dotz.client.ui;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.events.ClickHandler;

public class MXButtonsPanel extends MXHBox
{
    private Integer m_buttonWidth;

    public MXButtonsPanel(Integer buttonWidth)
    {
        m_buttonWidth = buttonWidth;
        
        setMargin(5);
        setMembersMargin(25);
        setHeight(20);
        setAlign(Alignment.CENTER);
    }
    
    public MXButtonsPanel()
    {
        this(null);
    }
    
    public void setButtonWidth(int buttonWidth)
    {
        m_buttonWidth = buttonWidth;
    }

    public MXButton add(String title, ClickHandler handler)
    {
        return add(title, null, handler);
    }
    
    public MXButton add(String title, String icon, ClickHandler handler)
    {
        MXButton button = new MXButton(title);
        button.addClickHandler(handler);
        if (m_buttonWidth != null)
        {
            button.setWidth(m_buttonWidth);
            button.setAutoFit(false);
        }
        
        if (icon != null)
            button.setIcon(icon);
        
        addMember(button);
        return button;
    }
}
