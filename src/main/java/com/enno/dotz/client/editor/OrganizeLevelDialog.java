package com.enno.dotz.client.editor;

import com.ait.tooling.nativetools.client.NArray;
import com.ait.tooling.nativetools.client.NObject;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.io.ClientRequest;
import com.enno.dotz.client.io.MAsyncCallback;
import com.enno.dotz.client.io.ServiceCallback;
import com.enno.dotz.client.ui.MXButtonsPanel;
import com.enno.dotz.client.ui.MXCheckBox;
import com.enno.dotz.client.ui.MXForm;
import com.enno.dotz.client.ui.MXTree;
import com.enno.dotz.client.ui.MXTreeGrid;
import com.enno.dotz.client.ui.MXWindow;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.util.ValueCallback;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.DropEvent;
import com.smartgwt.client.widgets.events.DropHandler;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeGridField;
import com.smartgwt.client.widgets.tree.TreeNode;

public class OrganizeLevelDialog extends MXWindow
{
    private MXTreeGrid m_tree;
    private int m_primary = 10000;
    private MXCheckBox m_preview;
    
    private VLayout m_previewContainer;
    private PreviewLevelPanel m_previewPanel;
    
    public OrganizeLevelDialog()
    {
        setTitle("Organize Levels");
        
        addItem(createPane());
        
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
        
        m_tree = createTreeGrid();
        pane.addMember(m_tree);
        
        MXButtonsPanel buttons = new MXButtonsPanel();
        buttons.add("Add Folder", new ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                SC.askforValue("New Folder", "Folder Name", new ValueCallback()
                {                    
                    @Override
                    public void execute(String value)
                    {
                        if (value != null && value.length() > 0)
                        {
                            createFolder(value);
                        }
                    }
                });
            }
        });
        buttons.add("Delete Level", new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                final TreeNode rec = m_tree.getSelectedRecord();
                if (rec == null)
                {
                    SC.warn("Select a level first.");
                    return;
                }
                
                int levelId = rec.getAttributeAsInt("id");
                ClientRequest.deleteLevel(levelId, new ServiceCallback() {
                    @Override
                    public void onSuccess(NObject result)
                    {
                        if (result.getAsBoolean("success"))
                            m_tree.removeData(rec);
                    }
                });
            }
        });
        buttons.add("Save", new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                save();
            }
        });
        
        buttons.add("Close", createCancelButtonHandler());
        
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
        tree.setCanReparentNodes(true);
        
        tree.addDropHandler(new DropHandler()
        {
            public void onDrop(DropEvent event)
            {
                clearSelections();
            }
        });
        
        TreeGridField name = new TreeGridField("name", "Name");
        
        TreeGridField id = new TreeGridField("id", "ID");
        id.setWidth(50);

        TreeGridField creator = new TreeGridField("creator", "Creator");
        creator.setWidth(100);

        tree.setFields(name, id, creator);
        
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

    protected void clearSelections()
    {
        m_tree.deselectAllRecords();
    }
    
    protected void createFolder(String folderName)
    {
        String id = "" + m_primary++;
        
        NObject folder = new NObject();
        folder.put("name", folderName);
        folder.put("primary", id);
        folder.put("parent", "0");
        folder.put("is_folder", true);
        
        TreeNode pa = new TreeNode(folder.getJSO());
        m_tree.addData(pa);
    }
    
    protected void setLevels(NArray levels)
    {
        MXTree tree = new MXTree("name", "primary", "parent", levels);
        tree.setPathDelim("/");
        tree.setIsFolderProperty("is_folder");
        
        m_tree.setData(tree);
    }
    
    protected void save()
    {
        Tree tree = m_tree.getTree();
        NArray list = new NArray();
        TreeNode[] nodes = tree.getAllNodes();
        for (TreeNode rec : nodes)
        {
            if (rec.getAttributeAsBoolean("is_folder"))
                continue;
            
            NObject level = new NObject();
            level.put("id", rec.getAttributeAsInt("id"));
            String parent = tree.getParentPath(rec);
            level.put("folder", parent);
            list.push(level);
        }
        
        ClientRequest.organizeLevels(list, new ServiceCallback()
        {            
            @Override
            public void onSuccess(NObject result)
            {
                // TODO Auto-generated method stub
                
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
}
