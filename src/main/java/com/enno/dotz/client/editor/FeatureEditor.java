package com.enno.dotz.client.editor;

import java.util.ArrayList;
import java.util.List;

import com.ait.tooling.nativetools.client.NArray;
import com.ait.tooling.nativetools.client.NObject;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.GridState;
import com.enno.dotz.client.editor.FeatureEditor.Stack.Frame;
import com.enno.dotz.client.editor.RandomGridGenerator.ExtendedProperties;
import com.enno.dotz.client.ui.MXButtonsPanel;
import com.enno.dotz.client.ui.MXCheckBox;
import com.enno.dotz.client.ui.MXForm;
import com.enno.dotz.client.ui.MXListGrid;
import com.enno.dotz.client.ui.MXListGridField;
import com.enno.dotz.client.ui.MXRecordList;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

public class FeatureEditor extends VLayout
{
    Stack stack = new Stack();
    
    private Context ctx;
    private EditLayoutTab tab;
    private RandomGridGenerator gen;

    private MXListGrid m_grid;
    private GridState m_lastState;

    private MXCheckBox m_x;
    private MXCheckBox m_y;
    private MXCheckBox m_replace;

    private ExtendedProperties m_randomProperties;
    
    public FeatureEditor(Context ctx, final EditLayoutTab tab)
    {
        this.ctx = ctx;
        this.tab = tab;
        this.gen = new RandomGridGenerator(ctx);
        
        m_randomProperties = new ExtendedProperties(tab.getEditorPropertiesPanel()) {
            @Override
            public boolean x_symmetry()
            {
                return m_x.isChecked();
            }

            @Override
            public boolean y_symmetry()
            {
                return m_y.isChecked();
            }

            @Override
            public boolean isReplace()
            {
                return m_replace.isChecked();
            }

            @Override
            public boolean isInteractive()
            {
                return true;
            }
        };
     
        setMembersMargin(10);
        setMargin(10);
        
        m_grid = new MXListGrid();
        m_grid.setWidth100();
        m_grid.setHeight(180);
        
        //MXListGridField id = new MXListGridField("id", "ID", ListGridFieldType.INTEGER, 40);
        MXListGridField name = new MXListGridField("name", "Stack Frames", ListGridFieldType.TEXT);
        m_grid.setFields(name);
        
        addMember(m_grid);
                
        MXButtonsPanel buttons = new MXButtonsPanel();
        buttons.add("Push", new ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                pushGrid();
            }
        });
        buttons.add("Pop", new ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                Frame f = stack.pop();
                if (f == null)
                    return;
                
                m_grid.getRecordList().removeAt(0);
                tab.replaceGrid(stack.currentState());
            }
        });
        buttons.add("Last", new ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                tab.replaceGrid(stack.currentState());
            }
        });
        buttons.add("Undo", new ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                if (m_lastState != null)
                    tab.replaceGrid(m_lastState);
            }
        });
        
        
        MXForm form = new MXForm();
        form.setNumCols(6);
        form.setColWidths(35, 0, 35, 0, 50, 0);
        form.setHeight(24);
        form.setWidth(130);
        form.setAlign(Alignment.CENTER);
        
        m_replace = new MXCheckBox();
        m_replace.setTitle("Replace");
        m_replace.setWidth(50);
        m_replace.setVAlign(VerticalAlignment.CENTER);
        
        m_x = new MXCheckBox();
        m_x.setTitle("X");
        m_x.setValue(true);
        m_x.setWidth(35);
        m_x.setVAlign(VerticalAlignment.CENTER);
        
        m_y = new MXCheckBox();
        m_y.setTitle("Y");
        m_y.setWidth(35);
        m_y.setVAlign(VerticalAlignment.CENTER);
        
        form.setFields(m_x, m_y, m_replace);
        
        addMember(buttons);
        
        HLayout formContainer = new HLayout();
        formContainer.setHeight(24);
        formContainer.setAlign(Alignment.CENTER);
        formContainer.addMember(form);
        addMember(formContainer);
        
        HLayout bottom = new HLayout();
        bottom.setMembersMargin(30);
        bottom.addMember(createGrid("Features", RandomGridGenerator.getLeftFeatures()));
        bottom.addMember(createGrid("Items", RandomGridGenerator.getRightFeatures()));
        addMember(bottom);
        
        setWidth(370);
    }    
    
    protected MXListGrid createGrid(String name, NArray features)
    {
        MXListGrid grid = new MXListGrid();
        //grid.setWidth(150);
        //MXListGridField fid = new MXListGridField("id", "ID", ListGridFieldType.INTEGER, 40);
        MXListGridField fname = new MXListGridField("name", name, ListGridFieldType.TEXT);
        grid.setFields(fname);
        
        grid.addRecordClickHandler(new RecordClickHandler()
        {
            @Override
            public void onRecordClick(RecordClickEvent event)
            {
                if (stack.list.size() == 0)
                {
                    pushGrid();
                }
                
                int fid = event.getRecord().getAttributeAsInt("id");
                gen.useGrid(stack.currentState().copy());
                gen.randomize(fid, m_randomProperties);
                
                m_lastState = tab.copyGrid();
                tab.replaceGrid(gen.getState());
            }
        });
        grid.setData(MXRecordList.toRecordArray(features));
        return grid;
    }
    
    private void pushGrid()
    {
        GridState curr = tab.copyGrid();
        stack.push(curr);
        tab.replaceGrid(curr);
                        
        RecordList records = m_grid.getRecordList();
        Record rec = new Record(stack.currentFrame().getRow().getJSO());
        records.addAt(rec, 0);
    }

    public static class Stack
    {
        List<Frame> list = new ArrayList<Frame>(); 
        
        public void push(GridState state)
        {
            Frame frame = new Frame();
            frame.state = state;
            frame.id = list.size();
            
            list.add(0, frame);
        }
        
        public Frame pop()
        {
            if (list.size() > 1)
            {
                return list.remove(0);
            }
            return null;
        }

        public GridState currentState()
        {
            return list.get(0).state;
        }
        
        public Frame currentFrame()
        {
            return list.get(0);
        }
        
        public static class Frame
        {
            GridState state;
            int id;
            
            public NObject getRow()
            {
                NObject obj = new NObject();
                obj.put("id", id);
                obj.put("name", "frame " + id);
                return obj;
            }
        }
    }
}
