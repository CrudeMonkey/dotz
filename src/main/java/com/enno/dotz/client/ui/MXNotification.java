package com.enno.dotz.client.ui;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.VLayout;

public class MXNotification extends Window
{
    private MXButtonsPanel m_buttons;

    protected MXNotification(String title, String message)
    {
        setTitle(title);
        
        setKeepInParentRect(true);
        setCanDragReposition(true);
        
        setIsModal(true);
        setShowModalMask(true);
        setShowShadow(true);
        setShadowSoftness(3);
        setShadowOffset(3);

        setShowMinimizeButton(false);
        setShowMaximizeButton(false);
        setShowCloseButton(false);
        
        VLayout pane = new VLayout();
        pane.setMargin(10);
        pane.setMembersMargin(10);
        
        MXLabel text = new MXLabel(message);
        text.setValign(VerticalAlignment.CENTER);
        text.setAlign(Alignment.CENTER);
        text.setHeight("100%");
        pane.addMember(text);
        
        m_buttons = new MXButtonsPanel();
        pane.addMember(m_buttons);
        
        addItem(pane);
    }
    
    protected void addOkCancel(final BooleanCallback cb)
    {
        addOk(cb);
        addCancel(cb);
    }
    
    protected void addOk(final BooleanCallback cb)
    {
        m_buttons.add("OK", new ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                closeWindow();
                if (cb != null)
                    cb.execute(Boolean.TRUE);
            }
        });
    }
    
    protected void addYesNoCancel(final BooleanCallback cb)
    {
        m_buttons.add("Yes", new ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                closeWindow();
                cb.execute(Boolean.TRUE);
            }
        });
        m_buttons.add("No", new ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                closeWindow();
                cb.execute(Boolean.FALSE);
            }
        });
        m_buttons.add("Cancel", new ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                closeWindow();
                cb.execute(null);
            }
        });
    }
    
    protected void addCancel(final BooleanCallback cb)
    {
        m_buttons.add("Cancel", new ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                closeWindow();
                cb.execute(Boolean.FALSE);
            }
        });
    }

    public void closeWindow()
    {
        hide();        
        destroy();
    }

    public static void say(String title, String message, int width, int height, int left, int top, BooleanCallback cb)
    {
        MXNotification w = new MXNotification(title, message);
        w.addOk(cb);
        w.setProps(width, height, left, top);        
        w.show();
    }

    public static void ask(String title, String message, int width, int height, int left, int top, BooleanCallback cb)
    {
        MXNotification w = new MXNotification(title, message);
        w.addOkCancel(cb);
        w.setProps(width, height, left, top);        
        w.show();
    }

    public static void askYesNoCancel(String title, String message, int width, int height, int left, int top, BooleanCallback cb)
    {
        MXNotification w = new MXNotification(title, message);
        w.addYesNoCancel(cb);
        w.setProps(width, height, left, top);        
        w.show();
    }

    private void setProps(int width, int height, int left, int top)
    {
        if (width > 0)
            setWidth(width);
        if (height > 0)
            setHeight(height);
    
        if (left > 0 || top > 0)
        {
            if (left > 0)
                setLeft(left);
            if (top > 0)
                setTop(top);
        }
        else
        {
            centerInPage();
        }
    }
}
