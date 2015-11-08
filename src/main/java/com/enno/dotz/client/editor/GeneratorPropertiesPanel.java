package com.enno.dotz.client.editor;

import java.util.LinkedHashMap;
import java.util.Random;

import com.enno.dotz.client.Config;
import com.enno.dotz.client.Generator;
import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.ui.MXCheckBox;
import com.enno.dotz.client.ui.MXComboBoxItem;
import com.enno.dotz.client.ui.MXForm;
import com.enno.dotz.client.ui.MXSelectItem;
import com.enno.dotz.client.ui.MXTextInput;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.form.fields.ButtonItem;
import com.smartgwt.client.widgets.form.fields.SpinnerItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
import com.smartgwt.client.widgets.layout.VLayout;

public abstract class GeneratorPropertiesPanel extends VLayout
{
    private static final String DOTS_MODE = "Dots Mode";
    private static final String LETTER_MODE = "Letter Mode";
    private static final String SWAP_MODE = "Swap Mode";
    
    private MXTextInput m_seed;
    private SpinnerItem m_animalStrength;
    private MXCheckBox m_randomSeed;
    private MXCheckBox m_initialDotsOnly;
    private MXCheckBox m_rollMode;
    private SpinnerItem m_fireGrowthRate;
    private MXSelectItem m_animalType;
    private SpinnerItem m_maxAnchors;
    private SpinnerItem m_knightStrength;
    private SpinnerItem m_bombStrength;
    private SpinnerItem m_clockStrength;
    private ButtonItem m_genSeed;
    private MXComboBoxItem m_mode;

    public GeneratorPropertiesPanel(boolean isNew, Config level)
    {
        setMargin(10);
        
        MXForm form = new MXForm();
        form.setNumCols(8);
        
        m_randomSeed = new MXCheckBox();
        m_randomSeed.setTitle("Random Seed");
        m_randomSeed.setLabelAsTitle(true);
        //m_check.setShowLabel(false);
        //m_randomSeed.setVAlign(VerticalAlignment.CENTER);
        m_randomSeed.setValue(true);
        
        m_initialDotsOnly = new MXCheckBox();
        m_initialDotsOnly.setTitle("Initial Dots Only");
        m_initialDotsOnly.setLabelAsTitle(true);
        m_initialDotsOnly.setValue(true);
 
        m_rollMode = new MXCheckBox();
        m_rollMode.setTitle("Roll Mode");
        m_rollMode.setLabelAsTitle(true);
        m_rollMode.setValue(false);
 
        LinkedHashMap<String,String> modeMap = new LinkedHashMap<String,String>();
        modeMap.put(DOTS_MODE, DOTS_MODE);
        modeMap.put(LETTER_MODE, LETTER_MODE);
        modeMap.put(SWAP_MODE, SWAP_MODE);
        
        m_mode = new MXComboBoxItem();
        m_mode.setTitle("Mode");
        m_mode.setValueMap(modeMap);
        m_mode.setValue(DOTS_MODE);
        m_mode.addChangedHandler(new ChangedHandler()
        {            
            @Override
            public void onChanged(ChangedEvent event)
            {
                setLetterMode(isLetterMode());
            }
        });
        
        m_seed = new MXTextInput();
        m_seed.setTitle("Seed");  
        m_seed.setValue("123");
        
        m_genSeed = new ButtonItem();
        m_genSeed.setTitle("Gen");
        m_genSeed.setStartRow(false);
        m_genSeed.setEndRow(false);
        m_genSeed.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                m_seed.setValue("" + Math.abs(new Random().nextLong() / 2));
            }
        });
        
        m_animalStrength = new SpinnerItem();
        m_animalStrength.setTitle("Animal Strength");
        m_animalStrength.setMin(1);
        m_animalStrength.setStep(1);
        m_animalStrength.setValue(10);
        m_animalStrength.setWidth(70);
        
        m_animalType = new MXSelectItem();
        m_animalType.setTitle("Animal Type");
        m_animalType.setValueMap(Animal.Type.getValueMap());
        m_animalType.setValue(Animal.Type.DEFAULT.getName());
        m_animalType.setWidth(70);
        
        m_fireGrowthRate = new SpinnerItem();
        m_fireGrowthRate.setTitle("Fire Growth Rate");
        m_fireGrowthRate.setValue(1);
        m_fireGrowthRate.setMin(1);
        m_fireGrowthRate.setStep(1);
        m_fireGrowthRate.setWidth(70);
        
        m_maxAnchors = new SpinnerItem();
        m_maxAnchors.setTitle("Max Anchors In Level");
        m_maxAnchors.setValue(3);
        m_maxAnchors.setMin(1);
        m_maxAnchors.setStep(1);
        m_maxAnchors.setWidth(70);
        
        m_knightStrength = new SpinnerItem();
        m_knightStrength.setTitle("Knight Strength");
        m_knightStrength.setMin(1);
        m_knightStrength.setStep(1);
        m_knightStrength.setValue(3);
        m_knightStrength.setWidth(70);
        
        m_clockStrength = new SpinnerItem();
        m_clockStrength.setTitle("Clock Strength");
        m_clockStrength.setMin(1);
        m_clockStrength.setStep(1);
        m_clockStrength.setValue(10);
        m_clockStrength.setWidth(70);
        
        m_bombStrength = new SpinnerItem();
        m_bombStrength.setTitle("Bomb Strength");
        m_bombStrength.setMin(1);
        m_bombStrength.setStep(1);
        m_bombStrength.setValue(9);
        m_bombStrength.setWidth(70);
        
        m_randomSeed.setColSpan(2);
        m_rollMode.setColSpan(2);
        m_mode.setColSpan(2);
        
        form.setFields(
                m_mode, m_animalStrength, m_knightStrength,
                m_randomSeed, m_animalType, m_fireGrowthRate, 
                m_seed, m_genSeed, m_maxAnchors, m_clockStrength,
                m_rollMode, m_initialDotsOnly, m_bombStrength);
        
        m_seed.setDisabled(true);
        m_genSeed.setDisabled(true);
        
        m_randomSeed.addChangedHandler(new ChangedHandler()
        {            
            @Override
            public void onChanged(ChangedEvent event)
            {
                m_seed.setDisabled(m_randomSeed.isChecked());
                m_genSeed.setDisabled(m_randomSeed.isChecked());
            }
        });
        
        addMember(form);
        
        if (!isNew)
        {
            Generator g = level.generator;
            m_fireGrowthRate.setValue(g.fireGrowthRate);
            m_knightStrength.setValue(g.knightStrength);
            m_clockStrength.setValue(g.clockStrength);
            m_animalStrength.setValue(g.animalStrength);
            m_bombStrength.setValue(g.bombStrength);
            m_animalType.setValue(g.animalType.getName());
            m_maxAnchors.setValue(g.maxAnchors);
            m_initialDotsOnly.setValue(g.initialDotsOnly);
            m_rollMode.setValue(g.rollMode);
                        
            m_mode.setValue(g.generateLetters ? LETTER_MODE : (g.swapMode ? SWAP_MODE : DOTS_MODE));
            
            if (g.getSeed() != Generator.RANDOM_SEED)
            {
                m_randomSeed.setValue(false);
                m_seed.setValue("" + g.getSeed());
                m_seed.setDisabled(false);
                m_genSeed.setDisabled(false);
            }
        }
    }

    public boolean validate()
    {
        if (!m_randomSeed.isChecked())
        {
            try
            {
                long seed = Long.parseLong(m_seed.getValueAsString());
                if (seed < 0)
                {
                    SC.warn("Invalid generator seed (must be positive)");
                    return false;
                }
            }
            catch (Exception e)
            {
                SC.warn("Invalid generator seed [" + m_seed.getValueAsString() + "]");
                return false;
            }
        }
        
        return true;
    }

    public void prepareSave(Config level)
    {
        Generator g = level.generator;
        g.setSeed(m_randomSeed.isChecked() ? Generator.RANDOM_SEED : Long.parseLong(m_seed.getValueAsString()));
        g.fireGrowthRate = Integer.parseInt(m_fireGrowthRate.getValueAsString());
        g.maxAnchors = Integer.parseInt(m_maxAnchors.getValueAsString());
        g.animalStrength = Integer.parseInt(m_animalStrength.getValueAsString());
        g.bombStrength = Integer.parseInt(m_bombStrength.getValueAsString());
        g.knightStrength = Integer.parseInt(m_knightStrength.getValueAsString());
        g.clockStrength = Integer.parseInt(m_clockStrength.getValueAsString());
        g.animalType = Animal.Type.fromName(m_animalType.getValueAsString());
        g.initialDotsOnly = m_initialDotsOnly.isChecked();
        g.generateLetters = isLetterMode();
        g.swapMode = SWAP_MODE.equals(m_mode.getValueAsString());
        g.rollMode = m_rollMode.isChecked();
    }

    public boolean isLetterMode()
    {
        return LETTER_MODE.equals(m_mode.getValueAsString());
    }
    
    protected abstract void setLetterMode(boolean isLetterMode);
}
