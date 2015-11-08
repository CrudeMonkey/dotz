package com.enno.dotz.client.ui;

import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VisibilityMode;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.layout.SectionStack;

public class MXAccordion extends SectionStack
{
    public MXAccordion()
    {
        super();
    }

    /**
     * Creates an Accordion with one section that wraps the specified component 
     * and has the specified label.
     * 
     * @param label
     * @param component     Main component shown in the accordion area
     * @param controls      Optional components (e.g. buttons) that are included in the header (on the right side.)
     * @return MXAccordion
     */
    public static MXAccordion createAccordion(String label, Canvas component, Canvas... controls)
    {
        MXAccordion acc = new MXAccordion();
        acc.setWidth100();
        acc.setHeight100();
        acc.setOverflow(Overflow.VISIBLE);
        acc.setVisibilityMode(VisibilityMode.MUTEX);
    
        MXAccordionSection section = new MXAccordionSection("&nbsp;&nbsp;" + label);
        section.setCanCollapse(false);
        if (controls != null)
            section.setControls(controls);
    
        section.addItem(component);
        acc.addSection(section);
    
        return acc;
    }
}
