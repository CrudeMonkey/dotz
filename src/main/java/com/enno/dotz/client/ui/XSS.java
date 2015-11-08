package com.enno.dotz.client.ui;

import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.HoverCustomizer;

public class XSS
{
    public static String clean(String s)
    {
        return s;
    }

    public static CellFormatter wrap(CellFormatter fmt)
    {
        return fmt;
    }

    public static HoverCustomizer wrap(HoverCustomizer customizer)
    {
        return customizer;
    }
}
