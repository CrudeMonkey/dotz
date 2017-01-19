package com.enno.dotz.client.editor;

import com.enno.dotz.client.Config;
import com.enno.dotz.client.editor.EditLevelDialog.ChangeListener;
import com.smartgwt.client.widgets.layout.VLayout;

public class EditGeneratorTab extends VLayout
{
    private FrequencySliderGroup m_sliderGroup;

    public EditGeneratorTab(boolean isNew, Config level, ChangeListener changeListener)
    {
        m_sliderGroup = new FrequencySliderGroup(isNew, level, changeListener);
        
        addMember(m_sliderGroup);
    }

    public boolean validate()
    {
        return m_sliderGroup.validate();
    }

    public void prepareSave(Config level)
    {
        m_sliderGroup.prepareSave(level);
    }

    public void equalizeDotFreq()
    {
        m_sliderGroup.equalizeDotFreq();
    }
}
