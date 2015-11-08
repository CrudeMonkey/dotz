package com.enno.dotz.client.ui;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.tab.TabSet;

public class UTabSet extends TabSet
{
    public UTab addTab(String title, Canvas component)
    {
        UTab tab = new UTab(title);
        tab.setPane(component);
        addTab(tab);
        return tab;
    }  
}
