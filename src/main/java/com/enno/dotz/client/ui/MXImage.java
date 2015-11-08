
package com.enno.dotz.client.ui;

import com.smartgwt.client.widgets.Img;

public class MXImage extends Img
{
    /**
     * @param altText   ALT-text for accessibility compliance, see RWEB-1803.
     *                  E.g. "Wells Fargo logo" or "warning icon".
     *                  Specify "" for separators and other decorative items.
     * @param source    Image URL
     */
    public MXImage(String altText, String source)
    {
        super(source);
        setAltText(altText);
    }
    
    /**
     * 
     * @param altText   ALT-text for accessibility compliance, see RWEB-1803.
     *                  E.g. "Wells Fargo logo" or "warning icon".
     *                  Specify "" for separators and other decorative items.
     * @param source    Image URL
     * @param wide      Image width
     * @param high      Image height
     */
    public MXImage(String altText, String source, int wide, int high)
    {
        super(source, wide, high);
        setAltText(altText);
    }
}
