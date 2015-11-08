
package com.enno.dotz.client.ui;

import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickEvent;
import com.smartgwt.client.widgets.events.CloseClickHandler;

public class MXWindow extends Window
{
    public MXWindow()
    {
        this(true);
    }

    public MXWindow(boolean setDefaultProperties)
    {
        setKeepInParentRect(true);

        if (setDefaultProperties)
        {
            setDismissOnEscape(true);
            
            setIsModal(true);
            setShowModalMask(true);
            setShowShadow(true);
            setShadowSoftness(3);
            setShadowOffset(3);

            setShowMinimizeButton(false);
            setShowMaximizeButton(false);
        
            addCloseClickHandler(new CloseClickHandler()
            {
                @Override
                public void onCloseClick(CloseClickEvent event)
                {
                    closeWindow();
                }
            });
        }
    }

    /**
     * Positions the window in a staggered fashion. 
     * When n = 0, it's just below/inside the 2 top-level tabs.
     * When n = 1, it's just below/inside the window with n = 0, etc...
     * @param n
     */
    public void setStaggerLocation(int n)
    {
        setLeft(22 + 11 * n);
        setTop(68 + 28 * n);
    }
    
    public void closeWindow()
    {
        hide();        
        destroy();
    }
    
    public ClickHandler createCancelButtonHandler()
    {
        return new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                closeWindow();
            }
        };
    }
}
