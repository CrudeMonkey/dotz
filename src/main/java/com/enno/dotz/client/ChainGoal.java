package com.enno.dotz.client;

import com.ait.tooling.nativetools.client.NArray;
import com.ait.tooling.nativetools.client.NObject;
import com.enno.dotz.client.Rewards.RewardStrategy;
import com.enno.dotz.client.ui.MXButtonsPanel;
import com.enno.dotz.client.ui.MXListGrid;
import com.enno.dotz.client.ui.MXListGridField;
import com.enno.dotz.client.ui.MXVBox;
import com.enno.dotz.client.ui.MXWindow;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridEditEvent;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;

public class ChainGoal
{
    private NArray m_list;
    
    public ChainGoal(NObject obj)
    {
        m_list = obj.getAsArray("list");
    }
    
    public ChainGoal(String chains)
    {
        this(parseChains(chains));
    }
    
    private static NObject parseChains(String chains)
    {
        NArray a = new NArray();
        if (chains != null && chains.length() > 0)
        {
            for (String s : chains.split(","))
            {
                NObject obj = new NObject();
                String[] vals = s.split("=");   // color=len
                obj.put("color", Integer.parseInt(vals[0]));
                obj.put("len", Integer.parseInt(vals[1]));            
                a.push(obj);
            }
        }
        return new NObject("list", a);
    }
    
    public String toString()
    {
        StringBuilder b = new StringBuilder();
        for (int i = 0, n = m_list.size(); i < n; i++)
        {
            if (i > 0)
                b.append(",");
            
            NObject obj = m_list.getAsObject(i);
            b.append(obj.getAsInteger("color")).append("=").append(obj.getAsInteger("len"));
        }
        return b.toString();
    }

    public NObject toNObject()
    {
        NObject obj = new NObject();
        obj.put("list",  m_list);
        
        return obj;
    }

    public int[] getColors()
    {
        int n = m_list.size();
        int[] colors = new int[n];
        for (int i = 0; i < n; i++)
            colors[i] = getInt(m_list.getAsObject(i), "color");
        
        return colors;
    }

    protected Integer getInt(NObject row, String prop)
    {
        return row.getAsInteger(prop);
    }

    public int[] getChainLengths()
    {
        int n = m_list.size();
        int[] chainLengths = new int[n];
        for (int i = 0; i < n; i++)
            chainLengths[i] = getInt(m_list.getAsObject(i), "len");
        
        return chainLengths;
    }

    public NArray copyList()
    {
        try
        {
            return m_list.deep();
        }
        catch (Exception e)
        {
            return new NArray();
        }
    }

    public abstract static class EditChainGoalDialog extends MXWindow
    {
        private MXListGrid m_grid;
        
        public EditChainGoalDialog(String chains)
        {
            this(new ChainGoal(chains));
        }
        
        public EditChainGoalDialog(ChainGoal goal)
        {
            setTitle("Edit Chain Goal");
            
            addItem(createPane());
            
            setCanDragResize(true);
            setCanDragReposition(true);
            
            setWidth(325);
            setHeight(190);
            centerInPage();
            
            setGoal(goal);
            
            show();
        }
        
        public abstract void saveGoal(ChainGoal goal);

        private Canvas createPane()
        {
            MXVBox pane = new MXVBox();
            pane.setPadding(5);
            pane.setMembersMargin(10);
            
            m_grid = createGrid();
            pane.addMember(m_grid);
            
            MXButtonsPanel buttons = new MXButtonsPanel();
            buttons.add("Add", new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    Record rec = new Record();
                    m_grid.addData(rec);
                }
            });
            buttons.add("Save", new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    ChainGoal goal = getGoal();
                    if (goal == null)
                        return;
                    
                    closeWindow();
                    saveGoal(goal);
                    
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
        
        protected MXListGrid createGrid()
        {
            MXListGrid grid = new MXListGrid();
            grid.setCanEdit(true);
            grid.setEditEvent(ListGridEditEvent.CLICK);
            MXListGridField len = new MXListGridField("len", "Chain Length", ListGridFieldType.INTEGER, 120);
            MXListGridField color = new MXListGridField("color", "Color", ListGridFieldType.INTEGER, 135);
            MXListGridField del = new MXListGridField("del", " ", ListGridFieldType.ICON);
            del.setIcon("delete2.png");
            
            len.setCellAlign(Alignment.CENTER);
            color.setCellAlign(Alignment.CENTER);
            
            del.addRecordClickHandler(new RecordClickHandler()
            {
                @Override
                public void onRecordClick(RecordClickEvent event)
                {
                    m_grid.removeData(event.getRecord());
                }
            });

            
//            grid.setEditorCustomizer(new ListGridEditorCustomizer() {  
//                public FormItem getEditor(ListGridEditorContext context) {  
//                    ListGridField field = context.getEditField();  
//                    if (field.getName().equals("type")) {  
//                        ListGridRecord record = context.getEditedRecord();  
//                        char ch = record.getAttribute("type").charAt(0);
//                        MXSelectItem sel = new MXSelectItem();
//                        sel.setValueMap(RewardStrategy.getValueMap());
//                        sel.setValue("" + ch);
//                        return sel;
//                    }  
//                    return context.getDefaultProperties();  
//                }  
//            });  
            
            grid.setFields(del, color, len);
            
            return grid;
        }
        
        public void setGoal(ChainGoal goal)
        {
            NArray data = goal.copyList();
            m_grid.setData(data);
        }
        
        public ChainGoal getGoal()
        {
            NArray list = new NArray();
            
            for (ListGridRecord rec : m_grid.getRecords())
            {
                Integer color = rec.getAttributeAsInt("color");
                Integer len = rec.getAttributeAsInt("len");
                if (color == null || len == null)
                    continue;
                
                NObject row = new NObject();
                
                row.put("color", color);
                row.put("len", len);
                list.push(row);
            }
            return new ChainGoal(new NObject("list", list));
        }
    }
}
