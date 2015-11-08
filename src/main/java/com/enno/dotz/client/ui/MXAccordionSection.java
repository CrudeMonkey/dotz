
package com.enno.dotz.client.ui;

import com.smartgwt.client.widgets.layout.SectionStackSection;

public class MXAccordionSection extends SectionStackSection
{
    public MXAccordionSection()
    {
        super();
    }

    public MXAccordionSection(String title)
    {
        super();

        setTitle(title);
    }

    @Override
    public void setTitle(String title)
    {
        super.setTitle("<b>" + XSS.clean(title) + "</b>");
    }
}
