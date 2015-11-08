
package com.enno.dotz.client.ui;

import com.smartgwt.client.widgets.IButton;

public class MXButton extends IButton
{
    public MXButton()
    {
        super();

        setAutoFit(true);
    }

    public MXButton(String label)
    {
        super(label);

        setAutoFit(true);
    }

    public MXButton(String label, String icon)
    {
        super(label);

        setAutoFit(true);

        setIcon(icon);
    }

    public MXButton(String label, MXToolTip tooltip)
    {
        super(label);

        setAutoFit(true);

        applyTooltip(tooltip);
    }

    public MXButton(String label, String icon, MXToolTip tooltip)
    {
        super(label);

        setAutoFit(true);

        setIcon(icon);

        applyTooltip(tooltip);
    }

    private void applyTooltip(MXToolTip tooltip)
    {
        setTooltip(tooltip.getText());

        int w = tooltip.getToolTipWidth();
        if (w > 0)
            setHoverWidth(w);
    }

    @Override
    public void setTitle(String title)
    {
        super.setTitle("<b>&nbsp;&nbsp;" + XSS.clean(title) + "&nbsp;&nbsp;</b>");
    }
}
