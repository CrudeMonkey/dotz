
package com.enno.dotz.client.ui;

import com.smartgwt.client.widgets.form.fields.SelectItem;

public class MXSelectItem extends SelectItem
{
    public MXSelectItem()
    {
        super();

        doInit();
    }

    public MXSelectItem(String name)
    {
        super(name);

        doInit();
    }

    public MXSelectItem(String name, String title)
    {
        super(name, title);

        doInit();
    }

    private final void doInit()
    {
//        setCellStyle("MXSelectItemCellStyle"); // fix style of SelectItem with setMultipleAppearance(MultipleAppearance.GRID)

//        setTextBoxStyle("MXSelectItemTextBoxStyle"); // same - needed for IE9
    }
}