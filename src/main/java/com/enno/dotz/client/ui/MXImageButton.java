
package com.enno.dotz.client.ui;

import com.smartgwt.client.widgets.ImgButton;

public class MXImageButton extends ImgButton
{
    /**
     * @param tooltip   Tooltip and ALT-text for accessibility compliance, see RWEB-1803.
     *                  E.g. "Refresh" or "Move to selected list"
     * @param image
     * @param wide
     * @param high
     */
    public MXImageButton(String tooltip, String image, int wide, int high)
    {
        setSrc(image);
        
        setWidth(wide);
        
        setHeight(high);
        
        setShowDown(false);
        
        setShowRollOver(false);
        
        setTooltip(tooltip);
    }
    
    /**
     * @param tooltip   Tooltip and ALT-text for accessibility compliance, see RWEB-1803.
     *                  E.g. "Refresh" or "Move to selected list"
     * @param image
     * @param wide
     * @param high
     * @param showDown
     * @param showRollOver
     */
    public MXImageButton(String tooltip, String image, int wide, int high, boolean showDown, boolean showRollOver)
    {
        setSrc(image);
        
        setWidth(wide);
        
        setHeight(high);
        
        setShowDown(showDown);
        
        setShowRollOver(showRollOver);
        
        setTooltip(tooltip);

    }
    
    /**
     * @param tooltip   Tooltip and ALT-text for accessibility compliance, see RWEB-1803.
     *                  E.g. "Refresh" or "Move to selected list"
     * @param image
     * @param wide
     * @param high
     * @param showDown
     * @param showRollOver
     */
    public MXImageButton(String image, int wide, int high, boolean showDown, boolean showRollOver)
    {
        setSrc(image);
        
        setWidth(wide);
        
        setHeight(high);
        
        setShowDown(showDown);
        
        setShowRollOver(showRollOver);
    }
    
    @Override
    public void setTooltip(String tooltip)
    {
    	super.setTooltip(tooltip);
        
        setAltText(tooltip);
    }
}
