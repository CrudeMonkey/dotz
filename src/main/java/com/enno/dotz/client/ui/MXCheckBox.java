
package com.enno.dotz.client.ui;

import com.smartgwt.client.widgets.form.fields.CheckboxItem;

public class MXCheckBox extends CheckboxItem
{
    public MXCheckBox()
    {
        super();
    }

    public MXCheckBox(boolean value)
    {
        super();

        setValue(value);
    }

    public MXCheckBox(String name)
    {
        super(name);
    }

    public boolean isChecked()
    {
        Object value = getValue();

        if (null == value)
        {
            return false;
        }
        return "true".equalsIgnoreCase(value.toString());
    }
}
