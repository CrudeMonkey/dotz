package com.enno.dotz.client.editor;

import com.enno.dotz.client.Cell.Machine;
import com.enno.dotz.client.Cell.Machine.MachineTrigger;
import com.enno.dotz.client.Cell.Machine.MachineType;
import com.enno.dotz.client.ui.MXButtonsPanel;
import com.enno.dotz.client.ui.MXForm;
import com.enno.dotz.client.ui.MXSelectItem;
import com.enno.dotz.client.ui.MXTextInput;
import com.enno.dotz.client.ui.MXVBox;
import com.enno.dotz.client.ui.MXWindow;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.fields.SpinnerItem;

public abstract class EditMachinePropDialog extends MXWindow
{
    private static EditMachinePropDialog s_instance;
    
    private Machine m_machine;
    
    private SpinnerItem m_machineEvery;
    private SpinnerItem m_machineHowMany;
    private MXSelectItem m_machineTrigger;
    private MXTextInput m_coinFreq;

    private MXSelectItem m_machineType;

    public EditMachinePropDialog(Machine machine)
    {
        m_machine = machine;
        
        setTitle("Machine Properties");
        
        setIsModal(false);
        setShowModalMask(false);
        
        addItem(createPane(machine));
        
        setWidth(300);
        setHeight(200 + (machine.isCoinMachine() ? 20 : 0));
        
        setCanDragResize(true);
        setCanDragReposition(true);
        
        setLeft(575);
        setTop(383);
        
        show();
        
        s_instance = this;
    }

    @Override
    public void closeWindow()
    {
        super.closeWindow();
        s_instance = null;
    }
    
    private Canvas createPane(Machine machine)
    {
        MXVBox pane = new MXVBox();
        pane.setPadding(5);
        pane.setMembersMargin(10);
        
        MXForm form = new MXForm();
        
        m_machineType = new MXSelectItem();
        m_machineType.setTitle("Machine Type");
        m_machineType.setWidth(85);
        m_machineType.setValueMap(MachineType.getValueMap());
        m_machineType.setValue(m_machine.getMachineType().name);
        m_machineType.setCanEdit(false);
        
        m_machineEvery = new SpinnerItem();
        m_machineEvery.setWidth(85);
        m_machineEvery.setTitle("Machine Every");
        m_machineEvery.setMin(1);
        m_machineEvery.setStep(1);
        m_machineEvery.setValue(m_machine.getEvery());
        
        m_machineHowMany = new SpinnerItem();
        m_machineHowMany.setWidth(85);
        m_machineHowMany.setTitle("Machine Items");
        m_machineHowMany.setMin(1);
        m_machineHowMany.setStep(1);
        m_machineHowMany.setValue(m_machine.getHowMany());
        
        m_machineTrigger = new MXSelectItem();
        m_machineTrigger.setTitle("Machine Trigger");
        m_machineTrigger.setWidth(85);
        m_machineTrigger.setValueMap(MachineTrigger.getValueMap());
        m_machineTrigger.setValue(m_machine.getTrigger().name);

        if (machine.isCoinMachine())
        {
            m_coinFreq = new MXTextInput();
            m_coinFreq.setWidth(150);
            m_coinFreq.setTitle("Coin Freq (for 1,5,10)");
            m_coinFreq.setValue(m_machine.getCoinFrequencies());
            
            form.setFields(m_machineType, m_machineEvery, m_machineHowMany, m_machineTrigger, m_coinFreq);
        }
        else
        {
            form.setFields(m_machineType, m_machineEvery, m_machineHowMany, m_machineTrigger);
        }
        
        pane.addMember(form);
        
        MXButtonsPanel buttons = new MXButtonsPanel();
        buttons.add("Save", new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                save();
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
    
    public int getMachineEvery()
    {
        return Integer.parseInt(m_machineEvery.getValueAsString());
    }
    
    public int getMachineHowMany()
    {
        return Integer.parseInt(m_machineHowMany.getValueAsString());
    }
    
//    public String getMachineType()
//    {
//        return m_machineType.getValueAsString();
//    }
    
    public String getMachineTrigger()
    {
        return m_machineTrigger.getValueAsString();
    }
    
    protected void save()
    {
        String coinFreq = null;
        if (m_machine.isCoinMachine())
        {
            String s = m_coinFreq.getValueAsString();
            try
            {
                Machine.parseCoinFrequencies(s);
            }
            catch (Exception e)
            {
                SC.warn("Invalid Coin Frequencies - " + e.getMessage());
                return;
            }
            
            coinFreq = s;
        }
        
        Machine m = new Machine(m_machine.getMachineType().name, m_machine.getLaunchItem().copy(), getMachineEvery(), getMachineHowMany(), getMachineTrigger(), coinFreq);
        
        save(m);
        closeWindow();
    }
    
    protected abstract void save(Machine machine);
    
    public static void closeDialog()
    {
        if (s_instance != null)
            s_instance.closeWindow();
    }
}
