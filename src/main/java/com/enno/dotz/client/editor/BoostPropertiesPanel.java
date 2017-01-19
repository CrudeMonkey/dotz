package com.enno.dotz.client.editor;

import com.enno.dotz.client.Boosts;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.editor.EditLevelDialog.ChangeListener;
import com.enno.dotz.client.ui.MXForm;
import com.smartgwt.client.widgets.form.fields.SpinnerItem;
import com.smartgwt.client.widgets.layout.VLayout;

public class BoostPropertiesPanel extends VLayout
{
    private SpinnerItem m_turners;
    private SpinnerItem m_drops;
    private SpinnerItem m_picks;
    private SpinnerItem m_colorBombs;
    private SpinnerItem m_wildCards;
    private SpinnerItem m_explodies;
    private SpinnerItem m_reshuffles;
    private SpinnerItem m_keys;
    
    public BoostPropertiesPanel(Config level, ChangeListener changeListener)
    {
        setMargin(10);
        
        MXForm form = new MXForm();
        form.setNumCols(2);
        
        m_turners = new SpinnerItem();
        m_turners.setTitle("Turners");
        m_turners.setValue(0);
        m_turners.setMin(0);
        m_turners.setStep(1);
        m_turners.setWidth(70);
        m_turners.addChangedHandler(changeListener);
        
        m_drops = new SpinnerItem();
        m_drops.setTitle("Drops");
        m_drops.setValue(0);
        m_drops.setMin(0);
        m_drops.setStep(1);
        m_drops.setWidth(70);
        m_drops.addChangedHandler(changeListener);
        
        m_picks = new SpinnerItem();
        m_picks.setTitle("Ice Picks");
        m_picks.setValue(0);
        m_picks.setMin(0);
        m_picks.setStep(1);
        m_picks.setWidth(70);
        m_picks.addChangedHandler(changeListener);
        
        m_colorBombs = new SpinnerItem();
        m_colorBombs.setTitle("Color Bombs");
        m_colorBombs.setValue(0);
        m_colorBombs.setMin(0);
        m_colorBombs.setStep(1);
        m_colorBombs.setWidth(70);
        m_colorBombs.addChangedHandler(changeListener);
        
        m_wildCards = new SpinnerItem();
        m_wildCards.setTitle("Wild Cards");
        m_wildCards.setValue(0);
        m_wildCards.setMin(0);
        m_wildCards.setStep(1);
        m_wildCards.setWidth(70);
        m_wildCards.addChangedHandler(changeListener);
        
        m_explodies = new SpinnerItem();
        m_explodies.setTitle("Explodies");
        m_explodies.setValue(0);
        m_explodies.setMin(0);
        m_explodies.setStep(1);
        m_explodies.setWidth(70);
        m_explodies.addChangedHandler(changeListener);
        
        m_reshuffles = new SpinnerItem();
        m_reshuffles.setTitle("Reshuffles");
        m_reshuffles.setValue(0);
        m_reshuffles.setMin(0);
        m_reshuffles.setStep(1);
        m_reshuffles.setWidth(70);
        m_reshuffles.addChangedHandler(changeListener);
        
        m_keys = new SpinnerItem();
        m_keys.setTitle("Keys");
        m_keys.setValue(0);
        m_keys.setMin(0);
        m_keys.setStep(1);
        m_keys.setWidth(70);
        m_keys.addChangedHandler(changeListener);
        
        form.setFields(m_turners, m_drops, m_picks, m_colorBombs, m_wildCards, m_explodies, m_reshuffles, m_keys);
        
        Boosts b = level.boosts;
        m_turners.setValue(b.turners);
        m_drops.setValue(b.drops);
        m_picks.setValue(b.picks);
        m_colorBombs.setValue(b.colorBombs);
        m_wildCards.setValue(b.wildCards);
        m_explodies.setValue(b.explodies);
        m_reshuffles.setValue(b.reshuffles);
        m_keys.setValue(b.keys);
        
        addMember(form);
    }
    
    public boolean validate()
    {
        return true;
    }
    
    public void prepareSave(Config level)
    {
        if (level.boosts == null)
            level.boosts = new Boosts();
        
        level.boosts.turners = Integer.parseInt(m_turners.getValueAsString());
        level.boosts.drops = Integer.parseInt(m_drops.getValueAsString());
        level.boosts.picks = Integer.parseInt(m_picks.getValueAsString());
        level.boosts.colorBombs = Integer.parseInt(m_colorBombs.getValueAsString());
        level.boosts.wildCards = Integer.parseInt(m_wildCards.getValueAsString());
        level.boosts.explodies = Integer.parseInt(m_explodies.getValueAsString());
        level.boosts.reshuffles = Integer.parseInt(m_reshuffles.getValueAsString());
        level.boosts.keys = Integer.parseInt(m_keys.getValueAsString());
    }
}
