
package com.enno.dotz.client.ui;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.HandlerRegistration;
import com.smartgwt.client.types.TextMatchStyle;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;

public class MXComboBoxItem extends ComboBoxItem
{
    private HandlerRegistration m_clickHandler;

    public MXComboBoxItem()
    {
        super();

        doInit();
    }

    public MXComboBoxItem(String name)
    {
        super(name);

        doInit();
    }

    private final void doInit()
    {
        //setTextBoxStyle("MXComboBoxItemDean");

        //setPickListBaseStyle("MXComboBoxItem");

        setTextMatchStyle(TextMatchStyle.SUBSTRING);

        /*if (MXBrowser.isIE())
        {
            ListGrid grid = new ListGrid();

            grid.setCellFormatter(new SafeCellFormatter()
            {
                @Override
                public String format(Object value, ListGridRecord record, int rowNum, int colNum)
                {
                    return "<span class='MXComboBoxItem'>" + XSS.htmlEscape(value.toString()) + "</span>";
                }
            });
            setPickListProperties(grid);
        }*/
        setWrapTitle(false);

        m_clickHandler = addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                Scheduler.get().scheduleDeferred(new ScheduledCommand()
                {
                    @Override
                    public void execute()
                    {
                        if (getValue() != null)
                        {
                            setSelectionRange(0, getDisplayValue().length());
                        }
                    }
                });
            }
        });
        setAddUnknownValues(false);
    }

    public HandlerRegistration getClickHandler()
    {
        return m_clickHandler;
    }
}
