package com.enno.dotz.client;

import com.ait.tooling.nativetools.client.NArray;
import com.enno.dotz.client.io.ClientRequest;
import com.enno.dotz.client.io.MAsyncCallback;
import com.enno.dotz.client.ui.MXButtonsPanel;
import com.enno.dotz.client.ui.MXListGrid;
import com.enno.dotz.client.ui.MXListGridField;
import com.enno.dotz.client.ui.MXVBox;
import com.enno.dotz.client.ui.MXWindow;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.CellDoubleClickEvent;
import com.smartgwt.client.widgets.grid.events.CellDoubleClickHandler;

public class SelectPlaybackDialog extends MXWindow
{
    private Context m_ctx;
    
    private MXListGrid m_grid;

    private AsyncCallback<Recorder> m_callback;
    
    public SelectPlaybackDialog(NArray list, AsyncCallback<Recorder> callback)
    {
        m_callback = callback;
        
        setTitle("Playback List");
        
        addItem(createPane(list));
        setWidth(580);
        setHeight(300);
        
        setCanDragResize(true);
        setCanDragReposition(true);
        
        centerInPage();
        
        show();
    }
    
    public static void showDialog(final AsyncCallback<Recorder> callback)
    {
        ClientRequest.loadPlaybackList(new MAsyncCallback<NArray>()
        {
            @Override
            public void onSuccess(NArray list)
            {
                new SelectPlaybackDialog(list, callback);
            }
        });
    }
    
    private Canvas createPane(NArray list)
    {
        MXVBox pane = new MXVBox();
        pane.setPadding(5);
        pane.setMembersMargin(10);
        
        m_grid = new MXListGrid();
        m_grid.setSelectionType(SelectionStyle.SINGLE);
        
        MXListGridField level = new MXListGridField("level", "Level", ListGridFieldType.INTEGER, 60);
        MXListGridField levelName = new MXListGridField("levelName", "Name", ListGridFieldType.TEXT, 120);
        MXListGridField seed = new MXListGridField("seed", "Seed", ListGridFieldType.TEXT, 95);
        MXListGridField id = new MXListGridField("id", "ID", ListGridFieldType.TEXT, 95);
        MXListGridField date = new MXListGridField("date", "Date", ListGridFieldType.TEXT);
        m_grid.setFields(level, levelName, date, seed, id);
                
        pane.addMember(m_grid);
        
        m_grid.setData(list);
        
        m_grid.addCellDoubleClickHandler(new CellDoubleClickHandler()
        {
            @Override
            public void onCellDoubleClick(CellDoubleClickEvent event)
            {
                playback(event.getRecord().getAttribute("id"));
            }
        });
        
        MXButtonsPanel buttons = new MXButtonsPanel();
        buttons.add("Playback", new ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                ListGridRecord rec = m_grid.getSelectedRecord();
                if (rec == null)
                {
                    return;
                }
                
                playback(rec.getAttribute("id"));
            }
        });
        buttons.add("Close", new ClickHandler()
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
    
    protected void playback(String id)
    {
        ClientRequest.getPlaybackRows(id, new MAsyncCallback<NArray>()
        {
            @Override
            public void onSuccess(NArray rows)
            {
                closeWindow();
                Recorder recorder = new Recorder(rows);
                m_callback.onSuccess(recorder);
            }
        });
    }
}
