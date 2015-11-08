
package com.enno.dotz.client.ui;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.HoverCustomizer;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.SortNormalizer;

public class MXListGridField extends ListGridField
{
    static final SortNormalizer DATE_TIME_SORT_NORMALIZER = new SortNormalizer()
    {            
        @Override
        public Object normalize(ListGridRecord record, String fieldName)
        {
            // "04/12/2013 12:34:56"
            String s = record.getAttribute(fieldName);
            if (s == null || s.length() != 19)
                return s;
            
            // year + month/day + time
            return s.substring(6, 10) + s.substring(0, 5) + s.substring(11);
        }
    };
    
    static final SortNormalizer DATE_SORT_NORMALIZER =new SortNormalizer()
    {            
        @Override
        public Object normalize(ListGridRecord record, String fieldName)
        {
            // "04/12/2013"
            String s = record.getAttribute(fieldName);
            if (s == null || s.length() != 10)
                return s;
            
            // year + month/day
            return s.substring(6, 10) + s.substring(0, 5);
        }
    };
    
    private boolean m_hasCellFormatter;

    public MXListGridField(String name)
    {
        super(name);
        
        setEscapeHTML(true);
    }
    
    public MXListGridField(String name, String title)
    {
        super(name, title);
        
        setEscapeHTML(true); // prevent XSS
    }

    public MXListGridField(String name, String title, ListGridFieldType type)
    {
        super(name, title);

        setType(type);
    }
    
    public MXListGridField(String name, String title, ListGridFieldType type, Alignment align)
    {
        super(name, title);

        setType(type);
        
        setAlign(align);
    }
    
    public MXListGridField(String name, String title, ListGridFieldType type, Alignment align, int width)
    {
        super(name, title);

        setType(type);
        
        setAlign(align);
        
        setWidth(width);
    }
    
    public MXListGridField(String name, String title, ListGridFieldType type, int width)
    {
        super(name, title);

        setType(type);
        
        setWidth(width);
    }
    
    public MXListGridField(String name, String title, ListGridFieldType type, boolean isEditable)
    {
        super(name, title);

        setType(type);
        
        setCanEdit(isEditable);
    }
    
    public MXListGridField(String name, String title, ListGridFieldType type, boolean isEditable, int width)
    {
        super(name, title);

        setType(type);
        
        setCanEdit(isEditable);
        
        setWidth(width);
    }
    
    public MXListGridField(String name, String title, ListGridFieldType type, boolean isEditable, boolean isFilterable)
    {
        super(name, title);

        setType(type);
        
        setCanEdit(isEditable);
        
        setCanFilter(isFilterable);
    }

    public MXListGridField(String name, String title, ListGridFieldType type, CellFormatter formatter)
    {
        super(name, title);

        setType(type);

        setCellFormatter(formatter);
    }
    
    public MXListGridField(String name, String title, ListGridFieldType type, CellFormatter formatter, int width)
    {
        super(name, title);

        setType(type);

        setCellFormatter(formatter);
        
        setWidth(width);
    }
    
    public MXListGridField(String name, String title, ListGridFieldType type, CellFormatter formatter, Alignment align)
    {
        super(name, title);

        setType(type);

        setCellFormatter(formatter);
        
        setAlign(align);
    }
    
    public MXListGridField(String name, String title, ListGridFieldType type, CellFormatter formatter, Alignment align, int width)
    {
        super(name, title);

        setType(type);

        setCellFormatter(formatter);
        
        setAlign(align);
        
        setWidth(width);
    }
    
    public MXListGridField(String name, String title, ListGridFieldType type, CellFormatter formatter, boolean isEditable)
    {
        super(name, title);

        setType(type);

        setCellFormatter(formatter);
        
        setCanEdit(isEditable);
    }
    
    public MXListGridField(String name, String title, ListGridFieldType type, CellFormatter formatter, boolean isEditable, int width)
    {
        this(name, title, type, formatter, isEditable);
        
        setWidth(width);
    }
    
    public MXListGridField(String name, String title, ListGridFieldType type, CellFormatter formatter, boolean isEditable, boolean isFilterable)
    {
        super(name, title);

        setType(type);

        setCellFormatter(formatter);
        
        setCanEdit(isEditable);
        
        setCanFilter(isFilterable);
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
        setEscapeHTML(false);
        
        m_hasCellFormatter = true;
        super.setCellFormatter(XSS.wrap(fmt)); // prevent XSS
    }
    
    @Override
    public void setType(ListGridFieldType type)
    {
        super.setType(type);
        
        if (!m_hasCellFormatter && !type.equals(ListGridFieldType.ICON) && !type.equals(ListGridFieldType.IMAGE))
            setEscapeHTML(true); // prevent XSS
        else
            setEscapeHTML(false);
    }

    public MXListGridField sortByDate()
    {
        setSortNormalizer(DATE_SORT_NORMALIZER);
        return this;
    }

    public MXListGridField sortByDateTime()
    {
        setSortNormalizer(DATE_TIME_SORT_NORMALIZER);
        return this;
    }
}
