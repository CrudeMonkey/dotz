
package com.enno.dotz.client.ui;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.Window;

public class MXWaitingWindow extends Window
{
    public MXWaitingWindow(String title)
    {
        super();

        setTitle(title);
        
        setShowShadow(true);

        setShadowSoftness(3);

        setShadowOffset(3);

        setShowMinimizeButton(false);

        setShowCloseButton(false);

        setDismissOnEscape(false);

        setIsModal(true);

        setShowModalMask(false);

        centerInPage();
        
        bringToFront();

        setAutoSize(true);

        MXVBox area = new MXVBox();

        area.setAlign(Alignment.CENTER);

        area.setMargin(5);

        area.addMember(new MXImage("animated progress bar image", "animated_progress_bar.gif", 196, 20));

        addItem(area);
    }
}
