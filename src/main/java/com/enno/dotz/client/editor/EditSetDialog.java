package com.enno.dotz.client.editor;

import java.util.ArrayList;
import java.util.List;

import com.ait.tooling.nativetools.client.NArray;
import com.ait.tooling.nativetools.client.NObject;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.io.ClientRequest;
import com.enno.dotz.client.io.MAsyncCallback;
import com.enno.dotz.client.io.ServiceCallback;
import com.enno.dotz.client.ui.MXButtonsPanel;
import com.enno.dotz.client.ui.MXForm;
import com.enno.dotz.client.ui.MXListGrid;
import com.enno.dotz.client.ui.MXListGridField;
import com.enno.dotz.client.ui.MXRecordList;
import com.enno.dotz.client.ui.MXTextInput;
import com.enno.dotz.client.ui.MXWindow;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.DoubleClickEvent;
import com.smartgwt.client.widgets.events.DoubleClickHandler;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

public class EditSetDialog extends MXWindow
{
    private NObject m_set;
    private MXListGrid m_grid;
    private StaticTextItem m_id;
    private MXTextInput m_name;
    private MXTextInput m_creator;
    
    private boolean m_changed;
    
    private PreviewLevelPanel m_previewPanel;
    private VLayout m_previewContainer;
    
    public EditSetDialog(boolean isNew, NObject set)
    {
        setTitle("Edit Level Set");
        
        if (isNew)
        {
            set = new NObject();
            set.put("levels", new NArray());
        }
        m_set = set;
        
        addItem(createPane());
        
        m_name.setValue(m_set.getAsString("name"));
        m_creator.setValue(m_set.getAsString("creator"));

        if (isNew)
            m_id.setValue("(new)");
        else
            m_id.setValue("" + m_set.getAsInteger("id"));
        
        m_grid.setData(MXRecordList.toRecordArray(m_set.getAsArray("levels")));
        
        setWidth(1200);
        setHeight(600);
        
        centerInPage();
        show();
    }

    private Canvas createPane()
    {
        HLayout pane = new HLayout();
        pane.setMembersMargin(10);
        pane.addMember(createLeftPane());
        
        m_previewContainer = new VLayout();
        m_previewContainer.setAlign(Alignment.CENTER);
        m_previewContainer.setDefaultLayoutAlign(Alignment.CENTER);
        pane.addMember(m_previewContainer);
        
        return pane;
    }    
    
    private Canvas createLeftPane()
    {
        VLayout pane = new VLayout();
        pane.setMargin(10);
        pane.setMembersMargin(10);
        
        MXForm form = new MXForm();
        
        m_id = new StaticTextItem();
        m_id.setTitle("ID");
        m_id.setWidth(50);
        m_id.setCanEdit(false);

        m_name = new MXTextInput();
        m_name.setWidth(400);
        m_name.setTitle("Name");
        
        m_creator = new MXTextInput();
        m_creator.setWidth(200);
        m_creator.setTitle("Creator");
        
        form.setFields(m_id, m_name, m_creator);
        pane.addMember(form);
        
        m_grid = new MXListGrid();
        m_grid.setSelectionType(SelectionStyle.SINGLE);
        m_grid.setCanReorderRecords(true);
        
        MXListGridField id = new MXListGridField("id", "ID", ListGridFieldType.TEXT, 40);
        MXListGridField name = new MXListGridField("name", "Name", ListGridFieldType.TEXT);
        MXListGridField creator = new MXListGridField("creator", "Creator", ListGridFieldType.TEXT, 100);
        
        m_grid.setFields(id, name, creator);
        
        pane.addMember(m_grid);        
        
        MXButtonsPanel buttons = new MXButtonsPanel();
        MXButtonsPanel buttons2 = new MXButtonsPanel();
        buttons.add("Add Level", new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                new SelectLevelDialog(false, true) {
                    @Override
                    public void selectedRecords(ListGridRecord[] recs)
                    {
                        for (int i = 0; i < recs.length; i++)
                        {
                            m_grid.addData(recs[i]);
                        }
                        m_changed = true;
                    }
                };
            }
        });
        buttons.add("Remove Level", new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                ListGridRecord rec = m_grid.getSelectedRecord();
                if (rec == null)
                {
                    SC.warn("Select a level first");
                    return;
                }
                m_grid.removeData(rec);
                m_changed = true;
            }
        });
        buttons.add("Move Up", new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                moveLevel(-1);
            }
        });
        buttons.add("Move Down", new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                moveLevel(1);
            }
        });
        buttons2.add("Edit Level", new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                ListGridRecord rec = m_grid.getSelectedRecord();
                if (rec == null)
                {
                    SC.warn("Select a level first");
                    return;
                }
                
                int levelId = rec.getAttributeAsInt("id");
                
                closeWindow();
                editLevel(levelId);
            }
        });
        buttons2.add("Play Level", new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                ListGridRecord rec = m_grid.getSelectedRecord();
                if (rec == null)
                {
                    SC.warn("Select a level first");
                    return;
                }
                
                int levelId = rec.getAttributeAsInt("id");
                
                closeWindow();
                playLevel(levelId);
            }
        });
        buttons2.add("Play From Level", new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                ListGridRecord rec = m_grid.getSelectedRecord();
                if (rec == null)
                {
                    SC.warn("Select a level first");
                    return;
                }
                
                if (!m_set.isInteger("id"))
                {
                    SC.warn("Save the set first.");
                    return;
                }
                
                int index = m_grid.getRecordIndex(rec);
                int setId = m_set.getAsInteger("id");
                
                closeWindow();
                playSet(setId, index);
            }
        });
        buttons2.add("Preview", new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                ListGridRecord rec = m_grid.getSelectedRecord();
                if (rec == null)
                {
                    SC.warn("Select a level first");
                    return;
                }
                previewLevel(rec.getAttributeAsInt("id"));
            }
        });
        buttons.add("Save", new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                saveSet();
                m_changed = false;
            }
        });
        buttons.add("Close", createCancelButtonHandler());
        
        pane.addMember(buttons);
        pane.addMember(buttons2);
        
        return pane;
    }
    
    @Override
    public void closeWindow()
    {
        if (!m_changed)
        {
            super.closeWindow();
            return;
        }
        
        SC.ask("You will lose your changes.<br>Are you sure you don't want to save them?", new BooleanCallback()
        {            
            @Override
            public void execute(Boolean ok)
            {
                if (Boolean.TRUE.equals(ok))
                    EditSetDialog.super.closeWindow();
            }
        });
    }

    protected void previewLevel(Integer levelId)
    {
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
    
    protected void saveSet()
    {
        String name = m_name.getValueAsString();
        if (name == null || name.length() == 0)
        {
            SC.warn("Must enter a level set Name");
            return;
        }
        
        m_set.put("name", name);
        m_set.put("creator", m_creator.getValueAsString());
        
        NArray levels = new NArray();
        ListGridRecord[] records = m_grid.getRecords();
        for (int i = 0; i < records.length; i++)
        {
            levels.push(records[i].getAttributeAsInt("id"));
        }
        
        m_set.put("levels", levels);
        ClientRequest.saveSet(m_set, new ServiceCallback() {
            @Override
            public void onSuccess(NObject result)
            {
                int id = result.getAsInteger("id");
                m_set.put("id", id);
                m_id.setValue("" + id);
            }
        });
    }
    
    protected void moveLevel(int d)
    {
        ListGridRecord rec = m_grid.getSelectedRecord();
        if (rec == null)
        {
            SC.warn("Select a level first");
            return;
        }
        
        int p = m_grid.getRecordIndex(rec);
        int q = p + d;
        ListGridRecord[] records = m_grid.getRecords();
        if (q < 0 || q >= records.length)
        {
            return;
        }
        
        List<ListGridRecord> list = new ArrayList<ListGridRecord>();
        for (int i = 0; i < records.length; i++)
        {
            list.add(records[i]);
        }
        list.remove(p);
        list.add(q, rec);

        m_grid.setData(list.toArray(new ListGridRecord[list.size()]));        
        m_grid.selectRecord(q);
        
        m_changed = true;
    }
    
    public void playLevel(int levelId)
    {
        // override
    }
    
    public void editLevel(int levelId)
    {
        // override
    }
    
    public void playSet(int setId, int firstLevel)
    {
        // override
    }
}
