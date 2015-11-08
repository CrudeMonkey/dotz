
package com.enno.dotz.client.ui;

import com.smartgwt.client.widgets.Label;

public class MXLabel extends Label
{
    public MXLabel()
    {
        super();
    }

    public MXLabel(String label)
    {
        super(label); // calls setContents() which sanitizes
    }
    
    public MXLabel(String label, boolean sanitize)
    {
        super();
        
        if (sanitize)
            setContents(label);
        else
            setSafeContents(label);
    }
    
    @Override
    public void setContents(String contents)
    {
        super.setContents(XSS.clean(contents));
    }

    /** 
     * Only use this if you know for sure that the supplied String contains
     * absolutely no dangerous HTML!
     * 
     * @param sanitizedContents
     */
    public void setSafeContents(String sanitizedContents)
    {
        super.setContents(sanitizedContents);
    }
}
