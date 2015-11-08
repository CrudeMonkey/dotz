package com.enno.dotz.client.editor;

import com.enno.dotz.client.Config;
import com.enno.dotz.client.UserSettings;
import com.enno.dotz.client.ui.MXButtonsPanel;
import com.enno.dotz.client.ui.MXForm;
import com.enno.dotz.client.ui.MXTextInput;
import com.enno.dotz.client.ui.MXWindow;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.VLayout;

public abstract class NewLevelDialog extends MXWindow
{
    private MXTextInput m_cols;
    private MXTextInput m_rows;

    public NewLevelDialog()
    {
        setTitle("New Level");
        
        addItem(createPane());
        
        setWidth(220);
        setHeight(160);
        
        setLeft(50);
        setTop(50);
        
        setCanDragResize(true);
        
        show();
    }
    
    public abstract void editNewLevel(Config level);
    
    private Canvas createPane()
    {
        VLayout pane = new VLayout();
        pane.setMargin(10);
        pane.setMembersMargin(10);
        
        MXForm form = new MXForm();
        form.setColWidths(100, 100);
        
        m_cols = new MXTextInput();
        m_cols.setTitle("Columns");
        m_cols.setValue("8");
        m_cols.setWidth("100%");
        
        m_rows = new MXTextInput();
        m_rows.setTitle("Rows");
        m_rows.setValue("8");
        m_rows.setWidth("100%");
        
        form.setFields(m_cols, m_rows);
        pane.addMember(form);
        
        MXButtonsPanel buttons = new MXButtonsPanel();
        buttons.add("Continue", new ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                Config cfg = new Config();
                try
                {
                    cfg.numColumns = Integer.parseInt(m_cols.getValueAsString());
                }
                catch (Exception e)
                {
                    SC.warn("Invalid Columns value");
                    return;
                }
                try
                {
                    cfg.numRows = Integer.parseInt(m_rows.getValueAsString());
                }
                catch (Exception e)
                {
                    SC.warn("Invalid Rows value");
                    return;
                }
                
                cfg.creator = UserSettings.INSTANCE.userName;
                
                close();
                editNewLevel(cfg);
            }
        });
        pane.addMember(buttons);
        return pane;
    }
}
