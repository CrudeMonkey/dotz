package com.enno.dotz.client.ui;

import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.HoverCustomizer;
import com.smartgwt.client.widgets.tree.TreeGridField;

public class MXTreeGridField extends TreeGridField
{
    public MXTreeGridField(String name, String title)
    {
        super(name, title);
        
        setEscapeHTML(true); // prevent XSS
    }
    
    public MXTreeGridField(String name, String title, ListGridFieldType type)
    {
        this(name, title, type, false);
    }

    public MXTreeGridField(String name, String title, ListGridFieldType type, int width)
    {
        this(name, title, type, false);

        setWidth(width);
    }

    public MXTreeGridField(String name, String title, ListGridFieldType type, boolean hidden)
    {
        super(name, title);
        
        setType(type);
        
        setHidden(hidden);
        
        setCanHide(false == hidden);
    }

    public MXTreeGridField(String name, String title, ListGridFieldType type, CellFormatter formatter, int width)
    {
        this(name, title, type, formatter);
        
        setWidth(width);
    }
    
    public MXTreeGridField(String name, String title, ListGridFieldType type, CellFormatter formatter)
    {
        super(name, title);
        
        setType(type);
        
        setCellFormatter(formatter);
    }

    @Override
    public void setHoverCustomizer(HoverCustomizer customizer)
    {
        setShowHover(true);

        super.setHoverCustomizer(XSS.wrap(customizer)); // prevent XSS
    }
    
    @Override
    public void setCellFormatter(CellFormatter fmt)
    {
        super.setCellFormatter(XSS.wrap(fmt)); // prevent XSS
    }    

    public MXTreeGridField sortByDate()
    {
        setSortNormalizer(MXListGridField.DATE_SORT_NORMALIZER);
        return this;
    }

    public MXTreeGridField sortByDateTime()
    {
        setSortNormalizer(MXListGridField.DATE_TIME_SORT_NORMALIZER);
        return this;
    }
}
