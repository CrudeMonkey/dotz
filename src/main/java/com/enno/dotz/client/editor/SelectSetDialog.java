package com.enno.dotz.client.editor;

import com.ait.tooling.nativetools.client.NArray;
import com.enno.dotz.client.io.ClientRequest;
import com.enno.dotz.client.io.MAsyncCallback;
import com.enno.dotz.client.ui.MXButtonsPanel;
import com.enno.dotz.client.ui.MXListGrid;
import com.enno.dotz.client.ui.MXListGridField;
import com.enno.dotz.client.ui.MXRecordList;
import com.enno.dotz.client.ui.MXWindow;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.DoubleClickEvent;
import com.smartgwt.client.widgets.events.DoubleClickHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.VLayout;

public abstract class SelectSetDialog extends MXWindow
{
    private MXListGrid m_grid;

    public SelectSetDialog()
    {
        setTitle("Select Level Set");
        
        addItem(createPane());
                
        setWidth(500);
        setHeight(500);
        
        centerInPage();
        
        ClientRequest.getSetList(new MAsyncCallback<NArray>() 
        {
            @Override
            public void onSuccess(NArray sets)
            {
                setSets(sets);
                show();
            }
        });
    }

    private Canvas createPane()
    {
        VLayout pane = new VLayout();
        pane.setMargin(10);
        pane.setMembersMargin(10);
        
        m_grid = new MXListGrid();
        m_grid.setSelectionType(SelectionStyle.SINGLE);
        
        MXListGridField id = new MXListGridField("id", "ID", ListGridFieldType.TEXT, 40);
        MXListGridField name = new MXListGridField("name", "Name", ListGridFieldType.TEXT);
        MXListGridField creator = new MXListGridField("creator", "Creator", ListGridFieldType.TEXT, 100);
        
        m_grid.setFields(id, name, creator);
        
        m_grid.addDoubleClickHandler(new DoubleClickHandler()
        {            
            @Override
            public void onDoubleClick(DoubleClickEvent event)
            {
                ListGridRecord rec = m_grid.getSelectedRecord();
                if (rec == null)                
                    return;                
                
                closeWindow();
                selected(rec.getAttributeAsInt("id"));
            }
        });
        
        pane.addMember(m_grid);
        
        MXButtonsPanel buttons = new MXButtonsPanel();
        buttons.add("OK", new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                ListGridRecord rec = m_grid.getSelectedRecord();
                if (rec == null)
                {
                    SC.warn("Select a set first");
                    return;
                }
                
                closeWindow();
                selected(rec.getAttributeAsInt("id"));
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
        
        pane.addMember(buttons);;
        
        return pane;
    }
    
    protected void setSets(NArray sets)
    {
        m_grid.setData(MXRecordList.toRecordArray(sets));
    }
    
    public abstract void selected(int setId);

}
