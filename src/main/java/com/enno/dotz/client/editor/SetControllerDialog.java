package com.enno.dotz.client.editor;

import java.util.ArrayList;
import java.util.List;

import com.ait.tooling.nativetools.client.NObject;
import com.enno.dotz.client.Controller;
import com.enno.dotz.client.Controller.Controllable;
import com.enno.dotz.client.editor.EditLevelDialog.ChangeListener;
import com.enno.dotz.client.ui.MXButtonsPanel;
import com.enno.dotz.client.ui.MXForm;
import com.enno.dotz.client.ui.MXListGrid;
import com.enno.dotz.client.ui.MXListGridField;
import com.enno.dotz.client.ui.MXTextInput;
import com.enno.dotz.client.ui.MXVBox;
import com.enno.dotz.client.ui.MXWindow;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.CellClickEvent;
import com.smartgwt.client.widgets.grid.events.CellClickHandler;
import com.smartgwt.client.widgets.grid.events.CellDoubleClickEvent;
import com.smartgwt.client.widgets.grid.events.CellDoubleClickHandler;

public class SetControllerDialog extends MXWindow
{
    private static final  SetControllerDialog INSTANCE = new SetControllerDialog();
    private static boolean s_open;

    private MXTextInput m_init;
    private MXTextInput m_repeat;
    private Controllable m_controllable;
    private MXListGrid m_grid;
    private ChangeListener m_changeListener;
    
    protected SetControllerDialog()
    {
        setTitle("Set Sequence");

        setIsModal(false);
        setShowModalMask(false);
        
        setWidth(300);
        setHeight(325);
        
        addItem(createPane());
        
        setCanDragResize(true);
        setCanDragReposition(true);
        
        setLeft(575);
        setTop(383);
        
//        centerInPage();
        //show();
    }
    
    protected Canvas createPane()
    {
        MXVBox pane = new MXVBox();
        pane.setPadding(5);
        pane.setMembersMargin(10);
        
        m_grid = createGrid();
        pane.addMember(m_grid);
        
        MXForm form = new MXForm();
        
        m_init = new MXTextInput();
        m_init.setTitle("Initial Sequence");

        m_repeat = new MXTextInput();
        m_repeat.setTitle("Repeating Sequence");

        form.setFields(m_init, m_repeat);
        pane.addMember(form);
        
        MXButtonsPanel buttons = new MXButtonsPanel();
        buttons.add("Apply", new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                apply();
            }
        });
        buttons.add("Cancel", new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                hide();
            }
        });
        pane.addMember(buttons);
        
        return pane;
    }
    
    protected MXListGrid createGrid()
    {
        MXListGrid grid = new MXListGrid();
        
        MXListGridField seq = new MXListGridField("seq", "Sequence", ListGridFieldType.TEXT);
        grid.setFields(seq);
        
        grid.addCellClickHandler(new CellClickHandler()
        {
            @Override
            public void onCellClick(CellClickEvent event)
            {
                String seq = event.getRecord().getAttribute("seq");
                setSequence(seq);
            }
        });
        
        grid.addCellDoubleClickHandler(new CellDoubleClickHandler()
        {
            @Override
            public void onCellDoubleClick(CellDoubleClickEvent event)
            {
                String seq = event.getRecord().getAttribute("seq");
                setSequence(seq);
                apply();
            }
        });
        
        return grid;
    }
    
    protected void apply()
    {
        String init = m_init.getValueAsString();
        if (init == null)
            init = "";
        init = init.replace("\\s+", "");
        
        String rep = m_repeat.getValueAsString();
        if (rep == null)
            rep = "";
        rep = rep.replace("\\s+", "");
        if (rep.length() == 0)
        {
            SC.warn("Repeating Sequence can't be empty");
            return;
        }
        
        String seq = rep;
        if (init.length() > 0)
            seq = init + ",+" + rep;
        
        try
        {
            new Controller(seq);
        }
        catch (Exception e)
        {
            SC.warn("Invalid expression");
            return;
        }
        
        addSequenceToGrid(seq);
        
        m_controllable.setSequence(seq);
        m_changeListener.changed();
    }

    private void addSequenceToGrid(String seq)
    {
        for (ListGridRecord rec : m_grid.getRecords())
        {
            if (seq.equals(rec.getAttribute("seq")))
                return; // already exists
        }

        List<ListGridRecord> list = new ArrayList<ListGridRecord>();
        
        NObject newRec = new NObject("seq", seq);
        list.add(new ListGridRecord(newRec.getJSO()));
        
        for (ListGridRecord rec : m_grid.getRecords())
        {
            list.add(rec);
        }
        m_grid.setData(list.toArray(new ListGridRecord[list.size()]));        
    }

    private void setSequence(String seq)
    {
        Controller c = new Controller(seq);
        m_init.setValue(c.getInitialSequence());
        m_repeat.setValue(c.getRepeatingSequence());
        
        addSequenceToGrid(seq);
    }

    private void setControllableItem(Controllable c, ChangeListener changeListener)
    {
        m_controllable = c;
        m_changeListener = changeListener;
        
        setSequence(c.getSequence());
    }

    public static void setControllable(Controllable c, ChangeListener changeListener)
    {
        INSTANCE.setControllableItem(c, changeListener);
        s_open = true;
        INSTANCE.show();
    }

    public static void closeDialog()
    {
        if (s_open)
        {
            s_open = false;
            INSTANCE.hide();
        }
    }
}
