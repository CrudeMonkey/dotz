package com.enno.dotz.client.editor;

import com.ait.tooling.nativetools.client.NArray;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.ShowScoresDialog;
import com.enno.dotz.client.io.ClientRequest;
import com.enno.dotz.client.io.MAsyncCallback;
import com.enno.dotz.client.ui.MXButtonsPanel;
import com.enno.dotz.client.ui.MXCheckBox;
import com.enno.dotz.client.ui.MXForm;
import com.enno.dotz.client.ui.MXTree;
import com.enno.dotz.client.ui.MXTreeGrid;
import com.enno.dotz.client.ui.MXWindow;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.grid.events.RecordDoubleClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordDoubleClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tree.TreeGridField;

public abstract class SelectLevelDialog extends MXWindow
{
    private MXTreeGrid m_tree;
    private boolean m_closeOnSelect;

    private VLayout m_previewContainer;
    private PreviewLevelPanel m_previewPanel;
    private MXCheckBox m_preview;
    private boolean m_multiple;

    public SelectLevelDialog(boolean closeOnSelect)
    {
        this(closeOnSelect, false);
    }
    
    public SelectLevelDialog(boolean closeOnSelect, boolean multiple)
    {
        setTitle("Select Level");
        
        m_closeOnSelect = closeOnSelect;
        m_multiple = multiple;
        
        addItem(createPane(false));
        
        setWidth(1000);
        setHeight(550);
        
        centerInPage();
        
        ClientRequest.getLevelList(true, new MAsyncCallback<NArray>() 
        {
            @Override
            public void onSuccess(NArray levels)
            {
                setLevels(levels);
                show();
            }
        });
    }
    
    public SelectLevelDialog(NArray levels)
    {
        setTitle("Select Level");
        
        m_closeOnSelect = true;
        m_multiple = false;
        
        addItem(createPane(true));
        
        setWidth(1000);
        setHeight(550);
        
        centerInPage();
        
        setLevels(levels);
        show();
    }

    private Canvas createPane(boolean addNewButton)
    {
        HLayout pane = new HLayout();
        pane.setMembersMargin(10);
        pane.addMember(createLeftPane(addNewButton));
        
        m_previewContainer = new VLayout();
        m_previewContainer.setAlign(Alignment.CENTER);
        m_previewContainer.setDefaultLayoutAlign(Alignment.CENTER);
        pane.addMember(m_previewContainer);
        
        return pane;
    }    
    
    private Canvas createLeftPane(boolean addNewButton)
    {
        VLayout pane = new VLayout();
        pane.setMargin(10);
        pane.setMembersMargin(10);
        
        m_tree = createTreeGrid();
        pane.addMember(m_tree);
        
        MXButtonsPanel buttons = new MXButtonsPanel();
        buttons.add("OK", new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                if (m_multiple)
                {
                    ListGridRecord[] recs = m_tree.getSelectedRecords();
                    if (recs == null || recs.length == 0)
                    {
                        SC.warn("Select one or more levels first");
                        return;
                    }
                    if (m_closeOnSelect)
                        closeWindow();
                    
                    selectedRecords(recs);
                }
                else
                {
                    Record rec  = m_tree.getSelectedRecord();
                    if (rec == null)
                    {
                        SC.warn("Select a level first");
                        return;
                    }
                    
                    if (m_closeOnSelect)
                        closeWindow();
                    
                    selected(rec);
                }
            }
        });
        
        if (addNewButton)
        {
            buttons.add("New", new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    closeWindow();
                    
                    createNewLevel();
                }
            });
        }
        
        buttons.add("Cancel", createCancelButtonHandler());
        
        MXForm form = new MXForm();
        m_preview = new MXCheckBox();
        m_preview.setTitle("Preview");
        m_preview.setValue(true);
        form.setFields(m_preview);
        
        m_preview.addChangedHandler(new ChangedHandler()
        {            
            @Override
            public void onChanged(ChangedEvent event)
            {
                if (m_preview.isChecked())
                {
                    Record rec  = m_tree.getSelectedRecord();
                    if (rec != null)
                    {
                        previewLevel(rec.getAttributeAsInt("id"));
                    }
                }
            }
        });
        buttons.addMember(form);
        
        pane.addMember(buttons);
        
        return pane;
    }
    
    protected void previewLevel(Integer levelId)
    {
        if (levelId == null)
            return;
        
        ClientRequest.loadLevel(levelId, new MAsyncCallback<Config>() {
            @Override
            public void onSuccess(Config level)
            {
                if (m_previewPanel != null)
                {
                    m_previewContainer.removeMember(m_previewPanel);
                    m_previewPanel = null;
                }
                m_previewPanel = new PreviewLevelPanel(level);
                m_previewContainer.addMember(m_previewPanel);
            }            
        });
    }

    private MXTreeGrid createTreeGrid()
    {
        MXTreeGrid tree = new MXTreeGrid();
                
        tree.setHeight100();
        tree.setWidth100();
        tree.setExpandOnClick(false);
        tree.setShowHeader(true);
        tree.setSelectionType(SelectionStyle.MULTIPLE);
        tree.setSelectionAppearance(SelectionAppearance.ROW_STYLE);
        tree.setOnFolderOpenClose(false);
        tree.setOpenTree(false);
        
        //tree.setShowSelectedStyle(false);
//        m_firm_reports_tree.setOpenRoot(false);
//
//        m_firm_reports_tree.setOnFolderOpenClose(false);

        TreeGridField name = new TreeGridField("name", "Name");
        
        TreeGridField id = new TreeGridField("id", "ID");
        id.setWidth(60);
        
        TreeGridField creator = new TreeGridField("creator", "Creator");
        creator.setWidth(100);

        TreeGridField score = new TreeGridField("score", "Score");
        score.setType(ListGridFieldType.ICON);
        score.setWidth(50);
        score.setCellIcon("clock.gif");
        score.addRecordClickHandler(new RecordClickHandler()
        {
            @Override
            public void onRecordClick(RecordClickEvent event)
            {
                int id = event.getRecord().getAttributeAsInt("id");
                ShowScoresDialog.showScores(id);
            }
        });
        
        tree.setFields(name, id, creator, score);

        tree.addRecordDoubleClickHandler(new RecordDoubleClickHandler()
        {
            @Override
            public void onRecordDoubleClick(final RecordDoubleClickEvent event)
            {
                // Have to defer it to prevent a weird error
                Scheduler.get().scheduleDeferred(new ScheduledCommand()
                {
                    @Override
                    public void execute()
                    {
                        Record record = event.getRecord();
                        
                        if (m_closeOnSelect)
                            closeWindow();
                        
                        selected(record);
                    }
                });
            }
        });
        
        tree.addRecordClickHandler(new RecordClickHandler()
        {            
            @Override
            public void onRecordClick(RecordClickEvent event)
            {
                if (m_preview.isChecked())
                {
                    Record rec  = m_tree.getSelectedRecord();
                    if (rec != null)
                    {
                        previewLevel(rec.getAttributeAsInt("id"));
                    }
                }
            }
        });
        
        return tree;
    }
    
    protected void setLevels(NArray levels)
    {
        m_tree.setData(new MXTree("name", "primary", "parent", levels));        
    }
    
    public void selected(Record rec)
    {
        selected(rec.getAttributeAsInt("id"));
    }
    
    public void selectedRecords(ListGridRecord[] recs)
    {
    }
    
    public void selected(int levelId)
    {        
    }
    
    public void createNewLevel()
    {
    }
}
