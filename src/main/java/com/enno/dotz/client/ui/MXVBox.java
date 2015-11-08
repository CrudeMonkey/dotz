
package com.enno.dotz.client.ui;

import com.smartgwt.client.widgets.layout.VLayout;

public class MXVBox extends VLayout
{
    public MXVBox()
    {
        super();

        setWidth100();

        setResizeBarClass("MXResizeBar");
    }
    
    public MXVBox(int membersMargin)
    {
        super(membersMargin);

        setWidth100();

        setResizeBarClass("MXResizeBar");
    }
}
