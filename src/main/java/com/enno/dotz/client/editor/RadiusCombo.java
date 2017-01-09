package com.enno.dotz.client.editor;

import java.util.LinkedHashMap;

import com.enno.dotz.client.ui.MXSelectItem;

public class RadiusCombo extends MXSelectItem
{
    public RadiusCombo(String title)
    {
        setTitle(title);
        setWidth(70);
        
        LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
        map.put("1", "1x1");
        map.put("3", "3x3");
        setValueMap(map);
        
        setRadius(3);
    }
    
    public void setRadius(int radius)
    {
        setValue("" + radius);
    }
    
    public Integer getRadius()
    {
        String val = getValueAsString();
        if (val == null)
            return null;
        
        return new Integer(val);
    }
}
