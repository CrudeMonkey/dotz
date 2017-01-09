package com.enno.dotz.client.editor;

import com.ait.tooling.nativetools.client.NArray;
import com.ait.tooling.nativetools.client.NObject;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.ShowScoresDialog;
import com.enno.dotz.client.UserSettings;
import com.enno.dotz.client.io.ClientRequest;
import com.enno.dotz.client.io.MAsyncCallback;
import com.enno.dotz.client.io.ServiceCallback;
import com.enno.dotz.client.ui.MXButtonsPanel;
import com.enno.dotz.client.ui.MXCheckBox;
import com.enno.dotz.client.ui.MXComboBoxItem;
import com.enno.dotz.client.ui.MXForm;
import com.enno.dotz.client.ui.MXListGrid;
import com.enno.dotz.client.ui.MXListGridField;
import com.enno.dotz.client.ui.MXRecordList;
import com.enno.dotz.client.ui.MXSimpleDS;
import com.enno.dotz.client.ui.MXTextInput;
import com.enno.dotz.client.ui.MXTree;
import com.enno.dotz.client.ui.MXTreeGrid;
import com.enno.dotz.client.ui.MXTreeGridField;
import com.enno.dotz.client.ui.MXVBox;
import com.enno.dotz.client.ui.MXWindow;
import com.enno.dotz.client.util.Debug;
import com.google.gwt.thirdparty.guava.common.collect.Sets;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.DragDataAction;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.util.ValueCallback;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.DropEvent;
import com.smartgwt.client.widgets.events.DropHandler;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.grid.events.RecordDropEvent;
import com.smartgwt.client.widgets.grid.events.RecordDropHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeGridField;
import com.smartgwt.client.widgets.tree.TreeNode;

public class OrganizeLevelDialog2 extends MXWindow
{
    private MXTreeGrid m_tree;
    private int m_primary = 10000;
    private MXCheckBox m_preview;
    
    private VLayout m_previewContainer;
    private PreviewLevelPanel m_previewPanel;
    private Integer m_lastPreviewLevel;

    private SetPane m_setPane;
    
    public OrganizeLevelDialog2()
    {
        setTitle("Organize Levels");
        
        addItem(createPane());
        
        setWidth(1300);
        setHeight(575);
        
        setCanDragResize(true);
        setCanDragReposition(true);
        
        setTop(50);
        setLeft(5);
        
        ClientRequest.getLevelList(true, new MAsyncCallback<NArray>() 
        {
            @Override
            public void onSuccess(NArray levels)
            {
                setLevels(levels);
                
                ClientRequest.getSetList(new MAsyncCallback<NArray>() 
                {
                    @Override
                    public void onSuccess(NArray sets)
                    {
                        m_setPane.setSets(sets);
                        show();
                    }
                });
            }
        });
    }
    
    private Canvas createPane()
    {
        HLayout pane = new HLayout();
        pane.setMembersMargin(10);
        pane.addMember(createLeftPane());
        
//        pane.setDefaultResizeBars(LayoutResizeBarPolicy.MIDDLE);
        
        m_setPane = createSetPane();
        pane.addMember(m_setPane);
        
        m_previewContainer = new VLayout();
        m_previewContainer.setAlign(Alignment.CENTER);
        m_previewContainer.setDefaultLayoutAlign(Alignment.CENTER);
        pane.addMember(m_previewContainer);
        
        return pane;
    }
    
    private SetPane createSetPane()
    {
        return new SetPane() {
            @Override
            public void previewLevel(int levelId)
            {
                OrganizeLevelDialog2.this.previewLevel(levelId);
            }

            @Override
            public void editLevel(int levelId)
            {
                closeWindow();
                OrganizeLevelDialog2.this.editLevel(levelId);
            }

            @Override
            public void playLevel(int levelId)
            {
                closeWindow();
                OrganizeLevelDialog2.this.playLevel(levelId);
            }

            @Override
            public void playSet(int setId, int firstLevel)
            {
                closeWindow();
                OrganizeLevelDialog2.this.playSet(setId, firstLevel);
            }
        };
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
//        buttons.add("Delete Level", new ClickHandler()
//        {
//            @Override
//            public void onClick(ClickEvent event)
//            {
//                final TreeNode rec = m_tree.getSelectedRecord();
//                if (rec == null)
//                {
//                    SC.warn("Select a level first.");
//                    return;
//                }
//                
//                int levelId = rec.getAttributeAsInt("id");
//                ClientRequest.deleteLevel(levelId, new ServiceCallback() {
//                    @Override
//                    public void onSuccess(NObject result)
//                    {
//                        if (result.getAsBoolean("success"))
//                            m_tree.removeData(rec);
//                    }
//                });
//            }
//        });
        
        buttons.add("Save", new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                save();
            }
        });
        
        buttons.add("Close", createCancelButtonHandler());
        
        MXButtonsPanel buttons2 = new MXButtonsPanel();
        buttons2.add("Play Level", new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                ListGridRecord rec = m_tree.getSelectedRecord();
                if (rec == null || rec.getAttributeAsBoolean("is_folder"))
                {
                    SC.warn("Select a level first");
                    return;
                }
                
                int levelId = rec.getAttributeAsInt("id");
                
                closeWindow();
                playLevel(levelId);
            }
        });
        buttons2.add("Edit Level", new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                ListGridRecord rec = m_tree.getSelectedRecord();
                if (rec == null || rec.getAttributeAsBoolean("is_folder"))
                {
                    SC.warn("Select a level first");
                    return;
                }
                
                int levelId = rec.getAttributeAsInt("id");
                
                closeWindow();
                editLevel(levelId);
            }
        });

        
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
                Record rec  = m_tree.getSelectedRecord();
                if (rec != null)
                {
                    previewLevel(rec.getAttributeAsInt("id"));
                }
            }
        });
        buttons.addMember(form);
        
        pane.addMember(buttons);
        pane.addMember(buttons2);
        
        return pane;
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
        tree.setSortFoldersBeforeLeaves(true);
        
        tree.setCanDragRecordsOut(true);
        tree.setDragDataAction(DragDataAction.COPY);  
        
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

        TreeGridField del = new MXTreeGridField("del", " ", ListGridFieldType.ICON);
        del.setIcon("delete2.png");
        
        del.addRecordClickHandler(new RecordClickHandler()
        {
            @Override
            public void onRecordClick(final RecordClickEvent event)
            {
                SC.ask("Are you sure you want to delete this level?", new BooleanCallback() {
                    @Override
                    public void execute(Boolean value)
                    {
                        final ListGridRecord rec = event.getRecord();
                        if (rec.getAttributeAsBoolean("is_folder"))
                        {
                            SC.warn("Can't delete directories");
                            return;
                        }
                        
                        if (Boolean.TRUE.equals(value))
                        {
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
                    }
                });
            }
        });
        
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
        
        tree.setFields(name, id, creator, score, del);
        
        tree.addRecordClickHandler(new RecordClickHandler()
        {            
            @Override
            public void onRecordClick(RecordClickEvent event)
            {
                Record rec  = event.getRecord();
                if (rec != null)
                {
                    previewLevel(rec.getAttributeAsInt("id"));
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
        String parent = "0";
        Record rec  = m_tree.getSelectedRecord();
        if (rec != null)
        {
            if (rec.getAttributeAsBoolean("is_folder"))
            {
                parent = rec.getAttribute("primary");
            }
        }
        String id = "" + m_primary++;
        
        NObject folder = new NObject();
        folder.put("name", folderName);
        folder.put("primary", id);
        folder.put("parent", parent);
        folder.put("is_folder", true);
        
        TreeNode node = new TreeNode(folder.getJSO());
        if (parent.equals("0"))
        {
            m_tree.addData(node);
        }
        else
        {
            TreeNode pa = m_tree.getTree().find("primary", parent);
            m_tree.getTree().add(node, pa);
        }
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
                // do nothing
            }
        });
    }

    protected void previewLevel(final Integer levelId)
    {
        if (!m_preview.isChecked())
        {
            return;
        }
        
        if (levelId == null)
            return;
        
        if (levelId.equals(m_lastPreviewLevel))
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
                m_lastPreviewLevel = levelId;
            }            
        });
    }
    
    public abstract static class SetPane extends MXVBox
    {
        private MXComboBoxItem m_selectSet;
        private StaticTextItem m_id;
        private MXTextInput m_name;
        private MXTextInput m_creator;
        private MXListGrid m_grid;

        private boolean m_changed;
        private NObject m_set = new NObject();
        private NArray m_sets;

        public SetPane()
        {
            setPadding(5);
            setMembersMargin(5);
            
            setWidth(320);
            
            MXForm form = new MXForm();
            form.setNumCols(4);
            form.setColWidths(45, "*", 45, 50);
            
            m_selectSet = new MXComboBoxItem();
            m_selectSet.setTitle("Set");
            
            MXListGridField id = new MXListGridField("id", "ID", ListGridFieldType.TEXT, 40);
            MXListGridField name = new MXListGridField("name", "Name", ListGridFieldType.TEXT);
            MXListGridField creator = new MXListGridField("creator", "Creator", ListGridFieldType.TEXT, 100);
            
            m_selectSet.setDisplayField("name");
            m_selectSet.setValueField("id");
            m_selectSet.setPickListFields(id, name, creator);
            m_selectSet.setPickListWidth(300);
            
            m_selectSet.addChangedHandler(new ChangedHandler()
            {
                @Override
                public void onChanged(ChangedEvent event)
                {
                    Integer id = (Integer) event.getValue();
                    selectSet(id);
                }
            });
            
            m_id = new StaticTextItem();
            m_id.setTitle("ID");
            m_id.setWidth(50);
            m_id.setCanEdit(false);

            m_name = new MXTextInput();
            m_name.setWidth("*");
            m_name.setTitle("Name");
            
            m_creator = new MXTextInput();
            m_creator.setWidth("*");
            m_creator.setTitle("Creator");
            
            form.setFields(m_selectSet, m_id, m_name, m_creator);
            
            addMember(form);
         
            m_grid = createGrid();
            addMember(m_grid);
            
            MXButtonsPanel buttons = new MXButtonsPanel();
            MXButtonsPanel buttons2 = new MXButtonsPanel();
            
            buttons.add("New", new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    checkChanged(new Runnable() {
                        @Override
                        public void run()
                        {
                            createNewSet();
                        }
                    });
                }
            });
            
            buttons.add("Play Set", new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    if (!m_set.isInteger("id"))
                    {
                        SC.warn("Save the set first.");
                        return;
                    }
                    
                    checkChanged(new Runnable() {
                        @Override
                        public void run()
                        {
                            int setId = m_set.getAsInteger("id");
                            
                            playSet(setId, 0);
                        }
                    });                    
                }
            });
            
            buttons.add("Delete Set", new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    SC.ask("Are you sure you want to delete this set?", new BooleanCallback() {
                        @Override
                        public void execute(Boolean value)
                        {
                            if (Boolean.TRUE.equals(value))
                            {
                                if (!m_set.isInteger("id"))     // hasn't been saved yet
                                {
                                    createNewSet();     // clear the form
                                    return;
                                }
                                
                                final int setId = m_set.getAsInteger("id");
                                ClientRequest.deleteSet(setId, new ServiceCallback() {
                                    @Override
                                    public void onSuccess(NObject result)
                                    {
                                        removeSetFromDropdown(setId);
                                        createNewSet();     // clear the form
                                    }
                                });
                                    
                            }
                        }
                    });
                }
            });
            
            buttons.add("Save", new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    saveSet(null);
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
                    playLevel(levelId);
                }
            });

            buttons2.add("Edit Level", new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    final ListGridRecord rec = m_grid.getSelectedRecord();
                    if (rec == null)
                    {
                        SC.warn("Select a level first");
                        return;
                    }
                    
                    checkChanged(new Runnable() {
                        @Override
                        public void run()
                        {
                            int levelId = rec.getAttributeAsInt("id");
                            editLevel(levelId);
                        }
                    });
                }
            });

            buttons2.add("Play From Level", new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    final ListGridRecord rec = m_grid.getSelectedRecord();
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
                    
                    checkChanged(new Runnable() {
                        @Override
                        public void run()
                        {
                            int index = m_grid.getRecordIndex(rec);
                            int setId = m_set.getAsInteger("id");
                            
                            playSet(setId, index);
                        }
                    });                    
                }
            });
            addMember(buttons);
            addMember(buttons2);
        }
        
        public abstract void previewLevel(int levelId);
        public abstract void editLevel(int levelId);
        public abstract void playLevel(int levelId);
        public abstract void playSet(int setId, int firstLevel);
        
        protected MXListGrid createGrid()
        {
            MXListGrid grid = new MXListGrid();
            
            MXListGridField id = new MXListGridField("id", "ID", ListGridFieldType.TEXT, 40);
            MXListGridField name = new MXListGridField("name", "Name", ListGridFieldType.TEXT);
            MXListGridField creator = new MXListGridField("creator", "Creator", ListGridFieldType.TEXT, 80);
            MXListGridField del = new MXListGridField("del", " ", ListGridFieldType.ICON);
            del.setIcon("delete2.png");
            
            del.addRecordClickHandler(new RecordClickHandler()
            {
                @Override
                public void onRecordClick(RecordClickEvent event)
                {
                    m_grid.removeData(event.getRecord());
                    m_changed = true;
                }
            });
            name.setWidth("*");
            
            grid.setFields(id, name, creator, del);
            
            grid.setCanAcceptDroppedRecords(true);  
            grid.setCanReorderRecords(true);  
            
//            m_grid.addDoubleClickHandler(new DoubleClickHandler()
//            {            
//                @Override
//                public void onDoubleClick(DoubleClickEvent event)
//                {
//                    ListGridRecord rec = m_grid.getSelectedRecord();
//                    if (rec == null)                
//                        return;                
//                    
//                    closeWindow();
//                    selected(rec.getAttributeAsInt("id"));
//                }
//            });
            
            grid.addRecordDropHandler(new RecordDropHandler()
            {                
                @Override
                public void onRecordDrop(RecordDropEvent event)
                {
                    Debug.p("dropped");
                    m_changed = true;
                }
            });
            
            grid.addRecordClickHandler(new RecordClickHandler()
            {            
                @Override
                public void onRecordClick(RecordClickEvent event)
                {
                    Record rec  = event.getRecord();
                    if (rec != null)
                    {
                        previewLevel(rec.getAttributeAsInt("id"));
                    }
                }
            });
            
            return grid;
        }
        
        protected void checkChanged(final Runnable callback)
        {
            if (m_changed)
            {
                SC.ask("Save changes?", new BooleanCallback() {
                    @Override
                    public void execute(Boolean value)
                    {
                        if (Boolean.TRUE.equals(value))
                        {
                            saveSet(callback);
                        }
                        else if (Boolean.FALSE.equals(value))
                        {
                            callback.run();
                        }
                    }
                });
            }
            else
                callback.run();
        }
        
        protected void saveSet(final Runnable callback)
        {
            String name = m_name.getValueAsString();
            if (name == null || name.length() == 0)
            {
                SC.warn("Must enter a Set Name");
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
                    
                    //TODO add new set to dropdown
                    
                    try
                    {
                        m_sets.push(m_set.deep());
                        setSets(m_sets);
                        m_selectSet.setValue(id);
                    }
                    catch (Exception e)
                    {
                        // shouldn't happen
                    }
                    
                    m_changed = false;
                    if (callback != null)
                        callback.run();
                }
            });
        }
        
        protected void selectSet(Integer setId)
        {
            ClientRequest.loadLevelSet(setId, new ServiceCallback() {
                @Override
                public void onSuccess(NObject set)
                {
                    m_set = set;
                    
                    m_name.setValue(m_set.getAsString("name"));
                    m_creator.setValue(m_set.getAsString("creator"));
                    m_id.setValue("" + m_set.getAsInteger("id"));
                    
                    m_grid.setData(MXRecordList.toRecordArray(m_set.getAsArray("levels")));
                    
                    m_changed = false;
                }            
            });
        }

        public void setSets(NArray sets)
        {
            m_sets = sets;
            m_selectSet.setOptionDataSource(new MXSimpleDS(sets));
        }

        protected void createNewSet()
        {
            m_selectSet.setValue((String) null);
            m_id.setValue("");
            m_name.setValue("");
            m_creator.setValue(UserSettings.INSTANCE.userName);
            
            m_grid.setData(MXRecordList.toRecordArray(new NArray()));
            
            m_set = new NObject();
            m_changed = false;
        }
        
        protected void removeSetFromDropdown(int setId)
        {
            NArray newSets = new NArray();
            for (int i = 0, n = m_sets.size(); i < n; i++)
            {
                NObject set = m_sets.getAsObject(i);
                if (set.getAsInteger("id") != setId)
                    newSets.push(set);
            }
            setSets(newSets);
        }
    }
}
