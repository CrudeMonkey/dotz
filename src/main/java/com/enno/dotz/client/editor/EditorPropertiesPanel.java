package com.enno.dotz.client.editor;

import java.util.LinkedHashMap;

import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.ui.MXForm;
import com.enno.dotz.client.ui.MXSelectItem;
import com.smartgwt.client.widgets.form.fields.SpinnerItem;
import com.smartgwt.client.widgets.layout.VLayout;

public class EditorPropertiesPanel extends VLayout
{
    private SpinnerItem m_iceStrength;
    private SpinnerItem m_animalStrength;
    private SpinnerItem m_knightStrength;
    private SpinnerItem m_doorStrength;
    private SpinnerItem m_clockStrength;
    private MXSelectItem m_animalType;
    private MXSelectItem m_doorRotation;
    private SpinnerItem m_bombStrength;

    public EditorPropertiesPanel()
    {
        setMargin(10);
        
        m_iceStrength = new SpinnerItem();
        m_iceStrength.setWidth(70);
        m_iceStrength.setTitle("Ice Strength");
        m_iceStrength.setMin(0);
        m_iceStrength.setStep(1);
        m_iceStrength.setValue(3);
        
        m_animalStrength = new SpinnerItem();
        m_animalStrength.setWidth(70);
        m_animalStrength.setTitle("Animal Strength");
        m_animalStrength.setMin(1);
        m_animalStrength.setStep(1);
        m_animalStrength.setValue(20);
        
        m_knightStrength = new SpinnerItem();
        m_knightStrength.setWidth(70);
        m_knightStrength.setTitle("Knight Strength");
        m_knightStrength.setMin(1);
        m_knightStrength.setStep(1);
        m_knightStrength.setValue(3);
        
        m_clockStrength = new SpinnerItem();
        m_clockStrength.setWidth(70);
        m_clockStrength.setTitle("Clock Strength");
        m_clockStrength.setMin(1);
        m_clockStrength.setStep(1);
        m_clockStrength.setValue(10);
        
        m_animalType = new MXSelectItem();
        m_animalType.setTitle("Animal Type");
        m_animalType.setValueMap(Animal.Type.getValueMap());
        m_animalType.setValue(Animal.Type.DEFAULT.getName());
        m_animalType.setWidth(70);
        
        m_doorStrength = new SpinnerItem();
        m_doorStrength.setWidth(70);
        m_doorStrength.setTitle("Door Strength");
        m_doorStrength.setMin(1);
        m_doorStrength.setStep(1);
        m_doorStrength.setValue(1);
        
        LinkedHashMap<String,String> rotMap = new LinkedHashMap<String,String>();
        rotMap.put("0", "None");
        rotMap.put("1", "Clockwise");
        rotMap.put("-1", "CCW");
        m_doorRotation = new MXSelectItem();
        m_doorRotation.setTitle("Door Rotation");
        m_doorRotation.setValueMap(rotMap);
        m_doorRotation.setValue("0");
        m_doorRotation.setWidth(70);
        
        m_bombStrength = new SpinnerItem();
        m_bombStrength.setWidth(70);
        m_bombStrength.setTitle("Bomb Strength");
        m_bombStrength.setMin(0);
        m_bombStrength.setStep(1);
        m_bombStrength.setValue(9);

        MXForm form = new MXForm();
        form.setNumCols(4);
        form.setColWidths(100, 50, 120, 50);
        
        form.setFields(m_iceStrength, m_animalStrength, 
                m_doorStrength, m_animalType, 
                m_doorRotation, m_knightStrength, 
                m_clockStrength, m_bombStrength);
        
        addMember(form);
    }
    
    public int getIceStrength()
    {
        return Integer.parseInt(m_iceStrength.getValueAsString());
    }
    
    public int getAnimalStrength()
    {
        return Integer.parseInt(m_animalStrength.getValueAsString());
    }

    public int getDoorStrength()
    {
        return Integer.parseInt(m_doorStrength.getValueAsString());
    }

    public int getBombStrength()
    {
        return Integer.parseInt(m_bombStrength.getValueAsString());
    }

    public Animal.Type getAnimalType()
    {
        return Animal.Type.fromName(m_animalType.getValueAsString());
    }

    public int getKnightStrength()
    {
        return Integer.parseInt(m_knightStrength.getValueAsString());
    }

    public int getClockStrength()
    {
        return Integer.parseInt(m_clockStrength.getValueAsString());
    }

    public int getDoorRotation()
    {
        return Integer.parseInt(m_doorRotation.getValueAsString());
    }
}
