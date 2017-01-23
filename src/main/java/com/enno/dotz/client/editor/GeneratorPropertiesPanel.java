package com.enno.dotz.client.editor;

import java.util.LinkedHashMap;
import java.util.Random;

import com.enno.dotz.client.Config;
import com.enno.dotz.client.Generator;
import com.enno.dotz.client.Rewards.RewardEditor;
import com.enno.dotz.client.editor.EditLevelDialog.ChangeListener;
import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.ui.MXAccordion;
import com.enno.dotz.client.ui.MXCheckBox;
import com.enno.dotz.client.ui.MXComboBoxItem;
import com.enno.dotz.client.ui.MXForm;
import com.enno.dotz.client.ui.MXSelectItem;
import com.enno.dotz.client.ui.MXTextInput;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.form.fields.ButtonItem;
import com.smartgwt.client.widgets.form.fields.SliderItem;
import com.smartgwt.client.widgets.form.fields.SpinnerItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
import com.smartgwt.client.widgets.layout.VLayout;

public abstract class GeneratorPropertiesPanel extends VLayout
{
    private static final String DOTS_MODE = "Dots Mode";
    private static final String WORD_MODE = "Word Mode";
    private static final String SWAP_MODE = "Swap Mode";
    private static final String DOMINO_MODE = "Domino Mode";
    private static final String CLICK_MODE = "Click Mode";
        
    private ChangeListener m_changeListener;

    private MXTextInput          m_seed;
    private SpinnerItem          m_animalStrength;
    private MXCheckBox           m_randomSeed;
    private MXCheckBox           m_initialDotsOnly;
    private MXCheckBox           m_rollMode;
    private MXCheckBox           m_diagonalMode;
    private MXCheckBox           m_slipperyAnchors;
    private SpinnerItem          m_fireGrowthRate;
    private MXSelectItem         m_animalType;
    private MXSelectItem         m_animalAction;
    private SpinnerItem          m_maxAnchors;
    private SpinnerItem          m_maxDomino;
    private SpinnerItem          m_knightStrength;
    private SpinnerItem          m_bombStrength;
    private SpinnerItem          m_blockerStrength;
    private SpinnerItem          m_clockStrength;
    private ButtonItem           m_genSeed;
    private MXComboBoxItem       m_mode;
    private BoostPropertiesPanel m_boostProps;
    private MXComboBoxItem       m_minChainLength;
    private MXTextInput          m_rewardStrategies;
    private RadiusCombo          m_icePickRadius;
    private RadiusCombo          m_dropRadius;
    private ButtonItem           m_editRewards;
    private MXCheckBox           m_removeLetters;
    private MXCheckBox           m_findWords;
    private SpinnerItem          m_maxWordLength;
    private SpinnerItem          m_chestStrength;
    private SliderItem           m_radioActivePct;
    private SpinnerItem          m_eggsNeeded;
    private SliderItem           m_spiderGrowth;

    public GeneratorPropertiesPanel(Config level, final ChangeListener changeListener)
    {
        m_changeListener = changeListener;
        
        setMargin(10);
        setMembersMargin(10);
        
        MXForm form = new MXForm();
        form.setNumCols(8);
        
        m_randomSeed = new MXCheckBox();
        m_randomSeed.setTitle("Random Seed");
        m_randomSeed.setLabelAsTitle(true);
        //m_check.setShowLabel(false);
        //m_randomSeed.setVAlign(VerticalAlignment.CENTER);
        m_randomSeed.setValue(true);
        m_randomSeed.addChangedHandler(changeListener);
        
        m_initialDotsOnly = new MXCheckBox();
        m_initialDotsOnly.setTitle("Initial Dots Only");
        m_initialDotsOnly.setLabelAsTitle(true);
        m_initialDotsOnly.setValue(true);
        m_initialDotsOnly.addChangedHandler(changeListener);
        
        m_rollMode = new MXCheckBox();
        m_rollMode.setTitle("Roll Mode");
        m_rollMode.setLabelAsTitle(true);
        m_rollMode.setValue(false);
        m_rollMode.addChangedHandler(changeListener);
        
        m_diagonalMode = new MXCheckBox();
        m_diagonalMode.setTitle("Diagonal Mode");
        m_diagonalMode.setLabelAsTitle(true);
        m_diagonalMode.setValue(false);
        m_diagonalMode.addChangedHandler(changeListener);
        
        m_slipperyAnchors = new MXCheckBox();
        m_slipperyAnchors.setTitle("Slippery Anchors");
        m_slipperyAnchors.setLabelAsTitle(true);
        m_slipperyAnchors.setValue(false);
        m_slipperyAnchors.setPrompt("Anchors/Diamonds slip through (in roll mode), similar to Jelly Splash diamonds");
        m_slipperyAnchors.addChangedHandler(changeListener);
        
        LinkedHashMap<String,String> modeMap = new LinkedHashMap<String,String>();
        modeMap.put(DOTS_MODE, DOTS_MODE);
        modeMap.put(WORD_MODE, WORD_MODE);
        modeMap.put(SWAP_MODE, SWAP_MODE);
        modeMap.put(DOMINO_MODE, DOMINO_MODE);
        modeMap.put(CLICK_MODE, CLICK_MODE);
        
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
        m_mode.addChangedHandler(changeListener);
        
        LinkedHashMap<String,String> lenMap = new LinkedHashMap<String,String>();
        lenMap.put("2", "2");
        lenMap.put("3", "3");
        lenMap.put("4", "4");
        lenMap.put("5", "5");
        
        m_minChainLength = new MXComboBoxItem();
        m_minChainLength.setTitle("Min Chain Length");
        m_minChainLength.setValueMap(lenMap);
        m_minChainLength.setValue("2");
        m_minChainLength.addChangedHandler(changeListener);
        
        m_seed = new MXTextInput();
        m_seed.setTitle("Seed");  
        m_seed.setValue("123");
        m_seed.addChangedHandler(changeListener);
        
        m_genSeed = new ButtonItem();
        m_genSeed.setTitle("Gen");
        m_genSeed.setStartRow(false);
        m_genSeed.setEndRow(false);
        m_genSeed.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                m_seed.setValue("" + Math.abs(new Random().nextInt()));
                changeListener.changed();
            }
        });

        m_editRewards = new ButtonItem();
        m_editRewards.setTitle("Edit");
        m_editRewards.setStartRow(false);
        m_editRewards.setEndRow(false);
        m_editRewards.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                editRewards();
            }
        });
        
        m_animalStrength = new SpinnerItem();
        m_animalStrength.setTitle("Animal Strength");
        m_animalStrength.setMin(1);
        m_animalStrength.setStep(1);
        m_animalStrength.setValue(10);
        m_animalStrength.setWidth(70);
        m_animalStrength.addChangedHandler(changeListener);
        
        m_animalType = new MXSelectItem();
        m_animalType.setTitle("Animal Type");
        m_animalType.setValueMap(Animal.Type.getValueMap());
        m_animalType.setValue(Animal.Type.DEFAULT.getName());
        m_animalType.setWidth(70);
        m_animalType.addChangedHandler(changeListener);
        
        m_animalAction = new MXSelectItem();
        m_animalAction.setTitle("Animal Action");
        m_animalAction.setValueMap(Animal.Action.getValueMap());
        m_animalAction.setValue(Animal.Action.DEFAULT.getName());
        m_animalAction.setWidth(70);
        m_animalAction.addChangedHandler(changeListener);
        
        m_fireGrowthRate = new SpinnerItem();
        m_fireGrowthRate.setTitle("Fire Growth Rate");
        m_fireGrowthRate.setValue(1);
        m_fireGrowthRate.setMin(1);
        m_fireGrowthRate.setStep(1);
        m_fireGrowthRate.setWidth(70);
        m_fireGrowthRate.addChangedHandler(changeListener);
        
        m_maxAnchors = new SpinnerItem();
        m_maxAnchors.setTitle("Max Anchors In Level");
        m_maxAnchors.setValue(3);
        m_maxAnchors.setMin(1);
        m_maxAnchors.setStep(1);
        m_maxAnchors.setWidth(70);
        m_maxAnchors.setPrompt("The generator won't generate more Anchors/Diamonds if the grid already contains this many.");
        m_maxAnchors.addChangedHandler(changeListener);
        
        m_maxDomino = new SpinnerItem();
        m_maxDomino.setTitle("Max Domino");
        m_maxDomino.setValue(6);
        m_maxDomino.setMin(1);
        m_maxDomino.setMax(9);
        m_maxDomino.setStep(1);
        m_maxDomino.setWidth(70);
        m_maxDomino.addChangedHandler(changeListener);
        
        m_knightStrength = new SpinnerItem();
        m_knightStrength.setTitle("Knight Strength");
        m_knightStrength.setMin(1);
        m_knightStrength.setStep(1);
        m_knightStrength.setValue(3);
        m_knightStrength.setWidth(70);
        m_knightStrength.addChangedHandler(changeListener);
        
        m_clockStrength = new SpinnerItem();
        m_clockStrength.setTitle("Clock Strength");
        m_clockStrength.setMin(1);
        m_clockStrength.setStep(1);
        m_clockStrength.setValue(10);
        m_clockStrength.setWidth(70);
        m_clockStrength.addChangedHandler(changeListener);
        
        m_bombStrength = new SpinnerItem();
        m_bombStrength.setTitle("Bomb Strength");
        m_bombStrength.setMin(1);
        m_bombStrength.setStep(1);
        m_bombStrength.setValue(9);
        m_bombStrength.setWidth(70);
        m_bombStrength.addChangedHandler(changeListener);
        
        m_blockerStrength = new SpinnerItem();
        m_blockerStrength.setTitle("Blocker Strength");
        m_blockerStrength.setMin(1);
        m_blockerStrength.setStep(1);
        m_blockerStrength.setValue(1);
        m_blockerStrength.setWidth(70);
        m_blockerStrength.addChangedHandler(changeListener);
        
        m_chestStrength = new SpinnerItem();
        m_chestStrength.setTitle("Chest Strength");
        m_chestStrength.setMin(1);
        m_chestStrength.setStep(1);
        m_chestStrength.setValue(1);
        m_chestStrength.setWidth(70);
        m_chestStrength.addChangedHandler(changeListener);
        
        m_rewardStrategies = new MXTextInput();
        m_rewardStrategies.setTitle("Rewards");
        m_rewardStrategies.setPrompt("E.g. 's4,s8' means: upgrade a Dot to Striped when chain length >= 4 and >= 8");
        m_rewardStrategies.addChangedHandler(changeListener);
        
        m_icePickRadius = new RadiusCombo("Ice Pick Radius");
        m_icePickRadius.addChangedHandler(changeListener);
        
        m_dropRadius = new RadiusCombo("Drop Radius");
        m_dropRadius.addChangedHandler(changeListener);
        
        m_removeLetters = new MXCheckBox();
        m_removeLetters.setTitle("Remove Letters");
        m_removeLetters.setLabelAsTitle(true);
        m_removeLetters.setValue(false);
        m_removeLetters.setPrompt("Whether to remove letters after chain is made in Word Mode");
        m_removeLetters.addChangedHandler(changeListener);
        
        m_findWords = new MXCheckBox();
        m_findWords.setTitle("Find Words");
        m_findWords.setLabelAsTitle(true);
        m_findWords.setValue(false);
        m_findWords.setPrompt("Whether user should find specific (generated) words in Word Mode");
        m_findWords.addChangedHandler(changeListener);
        
        m_maxWordLength = new SpinnerItem();
        m_maxWordLength.setTitle("Max Word Length");
        m_maxWordLength.setMin(3);
        m_maxWordLength.setStep(1);
        m_maxWordLength.setValue(6);
        m_maxWordLength.setWidth(70);
        m_maxWordLength.setPrompt("When finding specific (generated) words in Word Mode");
        m_maxWordLength.addChangedHandler(changeListener);
        
        m_radioActivePct = new SliderItem();
        m_radioActivePct.setTitle("Radio Active Pct");
        m_radioActivePct.setHeight(50);
        m_radioActivePct.setVertical(false);
        m_radioActivePct.setValue(0.0);
        m_radioActivePct.setMinValue(0.0);
        m_radioActivePct.setMaxValue(100.0);
        m_radioActivePct.setNumValues(1000);
        m_radioActivePct.setRoundPrecision(1);
        m_radioActivePct.setRoundValues(false);
        m_radioActivePct.setPrompt("Percentage of radio active Dots and DotBombs");
        m_radioActivePct.addChangedHandler(changeListener);
        
        m_spiderGrowth = new SliderItem();
        m_spiderGrowth.setHeight(50);
        m_spiderGrowth.setTitle("Spider Growth Pct");
        m_spiderGrowth.setVertical(false);
        m_spiderGrowth.setValue(10);
        m_spiderGrowth.setMinValue(0.0);
        m_spiderGrowth.setMaxValue(100.0);
        m_spiderGrowth.setNumValues(1000);
        m_spiderGrowth.setRoundPrecision(1);
        m_spiderGrowth.setRoundValues(false);
        m_spiderGrowth.setPrompt("Growth Rate of Spiders");
        m_spiderGrowth.addChangedHandler(changeListener);
        
        m_eggsNeeded = new SpinnerItem();
        m_eggsNeeded.setTitle("Eggs/Bird Ratio");
        m_eggsNeeded.setMin(2);
        m_eggsNeeded.setValue(3);
        m_eggsNeeded.setStep(1);
        m_eggsNeeded.setWidth(70);
        m_eggsNeeded.setPrompt("Number of eggs per cracked egg, and cracked eggs per bird");
        m_eggsNeeded.addChangedHandler(changeListener);
        
        m_randomSeed.setColSpan(2);
        m_rollMode.setColSpan(2);
        m_mode.setColSpan(2);
        m_diagonalMode.setColSpan(2);
        m_minChainLength.setColSpan(2);
        m_removeLetters.setColSpan(2);
        m_radioActivePct.setColSpan(2);
        m_spiderGrowth.setColSpan(2);
        
        form.setFields(
                m_mode, m_chestStrength, m_knightStrength,
                m_rollMode, m_animalStrength, m_fireGrowthRate, 
                m_diagonalMode, m_animalType, m_clockStrength,
                m_randomSeed, m_animalAction, m_maxDomino, 
                m_seed, m_genSeed, m_slipperyAnchors, m_bombStrength,
                m_minChainLength, m_maxAnchors, m_blockerStrength,
                m_rewardStrategies, m_editRewards, m_icePickRadius, m_dropRadius,
                m_removeLetters, m_findWords, m_maxWordLength,
                m_radioActivePct, m_spiderGrowth, m_initialDotsOnly, 
                m_eggsNeeded);
        
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
        
        Generator g = level.generator;
        m_fireGrowthRate.setValue(g.fireGrowthRate);
        m_knightStrength.setValue(g.knightStrength);
        m_clockStrength.setValue(g.clockStrength);
        m_animalStrength.setValue(g.animalStrength);
        m_bombStrength.setValue(g.bombStrength);
        m_chestStrength.setValue(g.chestStrength);
        m_blockerStrength.setValue(g.blockerStrength);
        m_animalType.setValue(g.animalType.getName());
        m_animalAction.setValue(g.animalAction.getName());
        m_maxAnchors.setValue(g.maxAnchors);
        m_maxDomino.setValue(g.maxDomino);
        m_initialDotsOnly.setValue(g.initialDotsOnly);
        m_rollMode.setValue(g.rollMode);
        m_diagonalMode.setValue(g.diagonalMode);
        m_slipperyAnchors.setValue(g.slipperyAnchors);
        m_minChainLength.setValue("" + g.minChainLength);
        m_rewardStrategies.setValue(g.rewardStrategies);
        m_icePickRadius.setRadius(g.icePickRadius); 
        m_dropRadius.setRadius(g.dropRadius);
        m_radioActivePct.setValue(g.radioActivePct);
        m_spiderGrowth.setValue(g.spiderGrowth);
        m_eggsNeeded.setValue(g.eggsNeeded);
        
        m_removeLetters.setValue(g.removeLetters);
        m_findWords.setValue(g.findWords);
        m_maxWordLength.setValue(g.maxWordLength);
        
        m_mode.setValue(g.generateLetters ? WORD_MODE : (g.swapMode ? SWAP_MODE : (g.dominoMode ? DOMINO_MODE : (g.clickMode ? CLICK_MODE : DOTS_MODE))));
        
        if (g.getSeed() != Generator.RANDOM_SEED)
        {
            m_randomSeed.setValue(false);
            m_seed.setValue("" + g.getSeed());
            m_seed.setDisabled(false);
            m_genSeed.setDisabled(false);
        }
        
        m_boostProps = new BoostPropertiesPanel(level, changeListener);
        MXAccordion acc = MXAccordion.createAccordion("Boosts", m_boostProps);
        addMember(acc);
    }

    public boolean validate()
    {
        if (!m_boostProps.validate())
            return false;
        
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
        
        if (isLetterMode())
        {
            if (m_findWords.isChecked() && m_removeLetters.isChecked())
            {
                SC.warn("When findWords is selected, removeLetters can't be selected");
                return false;
            }
            //TODO goal.words must be > 0
        }
        
        return true;
    }

    public void prepareSave(Config level)
    {
        m_boostProps.prepareSave(level);
        
        Generator g = level.generator;
        g.setSeed(m_randomSeed.isChecked() ? Generator.RANDOM_SEED : Long.parseLong(m_seed.getValueAsString()));
        g.fireGrowthRate = Integer.parseInt(m_fireGrowthRate.getValueAsString());
        g.maxAnchors = Integer.parseInt(m_maxAnchors.getValueAsString());
        g.maxDomino = Integer.parseInt(m_maxDomino.getValueAsString());
        g.animalStrength = Integer.parseInt(m_animalStrength.getValueAsString());
        g.bombStrength = Integer.parseInt(m_bombStrength.getValueAsString());
        g.chestStrength = Integer.parseInt(m_chestStrength.getValueAsString());
        g.knightStrength = Integer.parseInt(m_knightStrength.getValueAsString());
        g.blockerStrength = Integer.parseInt(m_blockerStrength.getValueAsString());
        g.clockStrength = Integer.parseInt(m_clockStrength.getValueAsString());
        g.animalType = Animal.Type.fromName(m_animalType.getValueAsString());
        g.animalAction = Animal.Action.fromName(m_animalAction.getValueAsString());
        g.initialDotsOnly = m_initialDotsOnly.isChecked();
        g.eggsNeeded = Integer.parseInt(m_eggsNeeded.getValueAsString());
        
        g.generateLetters = isLetterMode();
        g.swapMode = isSwapMode();
        g.dominoMode = isDominoMode();
        g.clickMode = isClickMode();
        
        g.rollMode = m_rollMode.isChecked();
        g.diagonalMode = m_diagonalMode.isChecked();
        g.slipperyAnchors = m_slipperyAnchors.isChecked();
        g.minChainLength = Integer.parseInt(m_minChainLength.getValueAsString());
        g.icePickRadius = m_icePickRadius.getRadius();
        g.dropRadius = m_dropRadius.getRadius();
        g.findWords = m_findWords.isChecked();
        g.removeLetters = m_removeLetters.isChecked();
        g.maxWordLength = Integer.parseInt(m_maxWordLength.getValueAsString());
        g.radioActivePct = m_radioActivePct.getValueAsFloat().doubleValue();
        g.spiderGrowth = m_spiderGrowth.getValueAsFloat().doubleValue();
        
        g.rewardStrategies = m_rewardStrategies.getValueAsString();
        if (g.rewardStrategies == null)
            g.rewardStrategies = "";
    }

    protected void editRewards()
    {
        String rewards = m_rewardStrategies.getValueAsString();
        new RewardEditor(rewards) {
            @Override
            public void saveRewards(String rewards)
            {
                m_rewardStrategies.setValue(rewards);
                m_changeListener.changed();
            }
        };
    }
    
    public boolean isLetterMode()
    {
        return WORD_MODE.equals(m_mode.getValueAsString());
    }

    public boolean isDominoMode()
    {
        return DOMINO_MODE.equals(m_mode.getValueAsString());
    }
    
    public boolean isSwapMode()
    {
        return SWAP_MODE.equals(m_mode.getValueAsString());
    }
    
    public boolean isClickMode()
    {
        return CLICK_MODE.equals(m_mode.getValueAsString());
    }
    
    protected abstract void setLetterMode(boolean isLetterMode);
}
