package com.enno.dotz.client.editor;

import com.enno.dotz.client.Config;
import com.enno.dotz.client.ui.MXCheckBox;
import com.enno.dotz.client.ui.MXForm;
import com.enno.dotz.client.ui.MXTextArea;
import com.enno.dotz.client.ui.MXTextInput;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.layout.HLayout;

public class LevelPropPanel extends HLayout
{
    private MXTextInput m_name;
    private MXTextInput m_creator;
    private MXTextInput m_folder;
    private StaticTextItem m_id;
    private MXTextArea m_description;
    private MXCheckBox m_showLoop;

    public LevelPropPanel()
    {
        setHeight(30);
        
        MXForm form = new MXForm();
        form.setNumCols(10);
        form.setColWidths(100, 250, 60, 100, 60, 140, 30, 30, 30, 20);
        form.setWidth(770);
        form.setHeight(30);
        
        m_id = new StaticTextItem();
        m_id.setTitle("ID");
        m_id.setWidth("100%");

        m_name = new MXTextInput();
        m_name.setWidth("100%");
        m_name.setTitle("Name");
        m_name.setValue("?");
        
        m_creator = new MXTextInput();
        m_creator.setWidth("100%");
        m_creator.setTitle("Creator");
        
        m_folder = new MXTextInput();
        m_folder.setWidth("100%");
        m_folder.setTitle("Folder");
        
        m_description = new MXTextArea();
        m_description.setWidth("100%");
        m_description.setTitle("Description");
        m_description.setHeight(48);
       
        m_showLoop = new MXCheckBox();
        m_showLoop.setTitle("Loop");
        m_showLoop.addChangedHandler(new ChangedHandler()
        {
            @Override
            public void onChanged(ChangedEvent event)
            {
                showLoopLayer(m_showLoop.isChecked());
            }
        });
        
        form.setFields(m_name, m_creator, m_folder, m_id, 
                m_description, m_showLoop);
        m_description.setColSpan(7);
        
        addMember(form);
    }

    public void showLoopLayer(boolean show)
    {
        // override
    }
    
    public void setLevel(Config level)
    {
        m_name.setValue(level.id == Config.UNDEFINED_ID ? "?" : level.name);
        m_id.setValue(level.id == Config.UNDEFINED_ID ? "(new)" : "" + level.id);
        m_creator.setValue(level.creator);
        m_folder.setValue(level.folder);
        m_description.setValue(fromHtml(level.description));
    }

    public boolean validate(boolean save)
    {
        String name = m_name.getValueAsString();
        if (name == null || name.length() == 0 || (save && name.equals("?")))
        {
            SC.warn("Level Name must be specified.");
            return false;
        }
        return true;
    }

    public void prepareSave(Config level)
    {
        level.name = m_name.getValueAsString();
        level.creator = m_creator.getValueAsString();
        level.description = toHtml(m_description.getValueAsString());
        
        level.folder = m_folder.getValueAsString();
        if (level.folder == null)
            level.folder = "";
    }

    public void setLevelID(int id)
    {
        m_id.setValue("" + id);
    }
    
    protected static String toHtml(String d)
    {
        if (d == null)
            return "";
        
        return d.replaceAll("(\\r|\\r\\n|\\n)", "<br>");
    }
    
    protected static String fromHtml(String d)
    {
        if (d == null)
            return "";
        
        return d.replaceAll("\\<br\\>", "\n");
    }
}
