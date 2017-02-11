package com.enno.dotz.client;

import com.ait.tooling.nativetools.client.NArray;
import com.ait.tooling.nativetools.client.NObject;
import com.ait.tooling.nativetools.client.NUtils;
import com.enno.dotz.client.ui.MXButton;
import com.enno.dotz.client.ui.MXButtonsPanel;
import com.enno.dotz.client.ui.MXListGrid;
import com.enno.dotz.client.ui.MXListGridField;
import com.enno.dotz.client.ui.MXVBox;
import com.enno.dotz.client.ui.MXWindow;
import com.enno.dotz.client.util.Console;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickEvent;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;

public class PlaybackDialog extends MXWindow
{
    private Context m_ctx;
    
    private int m_row = 0;
    private MXListGrid m_grid;
    private int m_rowCount;

    private MXButton m_step;

    protected Integer m_stepTo;

    private long m_seed;

    private Runnable m_restartCallback;
    
    public PlaybackDialog(Recorder rec, Context ctx)
    {
        super(false);
        
        m_ctx = ctx;
        
        addCloseClickHandler(new CloseClickHandler()
        {
            @Override
            public void onCloseClick(CloseClickEvent event)
            {
                closeWindow();
            }
        });
        
        setTitle("Playback");
        
        addItem(createPane());
        setWidth(350);
        setHeight(800);
        
        setRows(rec.getRows());
        
        setTop(30);
        setLeft(500);
        
        setCanDragResize(true);
        setCanDragReposition(true);
        
        show();
    }
    
    private Canvas createPane()
    {
        MXVBox pane = new MXVBox();
        pane.setPadding(5);
        pane.setMembersMargin(10);
        
        m_grid = new MXListGrid();
        m_grid.setSelectionType(SelectionStyle.SINGLE);
        MXListGridField type = new MXListGridField("action", "Action", ListGridFieldType.TEXT);
        m_grid.setFields(type);
                
        pane.addMember(m_grid);
        
        MXButtonsPanel buttons = new MXButtonsPanel();
        m_step = buttons.add("Step", new ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                nextAction();
            }
        });
        m_step.setDisabled(true);
        
        buttons.add("Step All", new ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                m_stepTo = m_rowCount;
                nextAction();
            }
        });
        
        buttons.add("Step To", new ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                ListGridRecord rec = m_grid.getSelectedRecord();
                if (rec == null)
                {
                    SC.warn("Select a row first");
                    return;
                }
                
                int selectedRow = m_grid.getRecordIndex(rec);
                m_stepTo = selectedRow;
                nextAction();
            }
        });
        
        buttons.add("Restart", new ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                closeWindow();
                m_restartCallback.run();
            }
        });
        
        pane.addMember(buttons);
        
        return pane;
    }
    
    public void setRows(NArray rows)
    {
        m_rowCount = rows.size();
        
        NArray a = new NArray();
        for (int i = 0, n = m_rowCount; i < n; i++)
        {
            if (i == 0)
            {
                m_seed = Long.parseLong(rows.getAsObject(i).getAsString("seed"));
            }
            
            NObject row = new NObject("action", rows.getAsObject(i).toJSONString());
            a.push(row);
        }
        a.push(new NObject("action", "DONE"));
                
        m_grid.setData(a);
        m_grid.selectRecord(m_row);
    }

    public long getSeed()
    {
        return m_seed;
    }

    public void start() // start of ConnectMode
    {
        m_row++;
        if (m_row <= m_rowCount)
        {
            m_grid.deselectAllRecords();
            m_grid.selectRecord(m_row);
            
            if (m_row < m_rowCount)
            {
                m_ctx.playbackLayer.setAction(getSelectedRow());
                m_ctx.playbackLayer.start();
                
                m_step.setDisabled(false);
                
                if (m_stepTo != null && m_row < m_stepTo)
                    nextAction();
                
                return;
            }
        }
        
        m_step.setDisabled(true);
        m_ctx.playbackLayer.stop();
    }
    
    public void stop()
    {
        m_step.setDisabled(true);
        m_ctx.playbackLayer.stop();
    }
    
    protected void nextAction()
    {
        try
        {
            if (m_row >= m_rowCount)
                return;
            
            NObject row = getSelectedRow();            
            playback(row);
        }
        catch (Exception e)
        {
            Console.log(e.getMessage());
        }
    }

    protected NObject getSelectedRow()
    {
        if (m_row >= m_rowCount)
            return null;
        
        try
        {
            ListGridRecord rec = m_grid.getRecord(m_row);
            String s = rec.getAttributeAsString("action");
            
            NObject row = (NObject) NUtils.JSON.parse(s);
            return row;
        }
        catch (Exception e)
        {
            Console.log(e.getMessage());
            return null;
        }
    }
    
    public void playback(NObject row)
    {
        m_ctx.playPanel.playback(row);
    }

    public void playedBackUndoRedo()
    {
        start();
    }
    
    public void setRestartCallback(Runnable callback)
    {
        m_restartCallback = callback;
    }

    public void undo(boolean playingBack)
    {
        if (playingBack)
        {
            start();
            return;
        }
        
        if (m_row > 0)
        {
            m_row--;
            m_grid.deselectAllRecords();
            m_grid.selectRecord(m_row);
            m_ctx.playbackLayer.setAction(getSelectedRow());
            m_ctx.playbackLayer.start();
            m_step.setDisabled(false);
        }
    }

    public void redo(boolean playingBack)
    {
        if (playingBack)
        {
            start();
            return;
        }
        
        if (m_row < m_rowCount)
        {
            m_row++;
            m_grid.deselectAllRecords();
            m_grid.selectRecord(m_row);
            
            if (m_row < m_rowCount)
            {
                m_ctx.playbackLayer.setAction(getSelectedRow());
                m_ctx.playbackLayer.start();
                m_step.setDisabled(false);
                return;
            }
        }
        m_step.setDisabled(true);
    }
}
