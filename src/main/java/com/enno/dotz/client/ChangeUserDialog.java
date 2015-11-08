package com.enno.dotz.client;

import com.enno.dotz.client.ui.MXButtonsPanel;
import com.enno.dotz.client.ui.MXForm;
import com.enno.dotz.client.ui.MXTextInput;
import com.enno.dotz.client.ui.MXWindow;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.VLayout;

public class ChangeUserDialog extends MXWindow
{
    private MXTextInput m_user;

    public ChangeUserDialog()
    {
        setTitle("Change User");
        addItem(createPane());
        setWidth(300);
        setHeight(150);
        
        centerInPage();
        show();
    }

    private Canvas createPane()
    {
        VLayout pane = new VLayout();
        pane.setMembersMargin(10);
        pane.setPadding(10);
        
        MXForm form = new MXForm();
        
        m_user = new MXTextInput();
        m_user.setTitle("User Name:");
        form.setFields(m_user);
        pane.addMember(form);
        
        MXButtonsPanel buttons = new MXButtonsPanel();
        buttons.add("OK", new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                String user = m_user.getValueAsString();
                if (user == null || user.length() == 0)
                {
                    SC.say("Enter a name first");
                    return;
                }
                UserSettings.INSTANCE.userName = user;
                closeWindow();
            }
        });
        buttons.add("Cancel", new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                closeWindow();
            }
        });
        pane.addMember(buttons);
        
        return pane;
    }
}
