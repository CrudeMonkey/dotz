
package com.enno.dotz.client.ui;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VisibilityMode;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickEvent;
import com.smartgwt.client.widgets.events.CloseClickHandler;

public class ErrorWindow extends Window
{
    public ErrorWindow(ServiceErrorException e)
    {
        this("A Fatal error occured. Expand the section below for details.", e.getMessage());
    }
    
    public ErrorWindow(Throwable e)
    {
        this(e.getMessage());
    }
    
    public ErrorWindow(String message)
    {
        super();

        setWidth(500);

        setHeight(350);

        setTitle("Error");

        setShowMinimizeButton(false);

        setDismissOnEscape(false);

        setIsModal(true);

        setShowModalMask(false);

        setShowShadow(true);

        setShadowSoftness(3);

        setShadowOffset(3);

        centerInPage();

        MXHBox hbar = new MXHBox();

        hbar.setHeight(25);

        hbar.setWidth100();

        hbar.setMembersMargin(5);

        hbar.setAlign(Alignment.RIGHT);

        final MXButton canc = new MXButton("Dismiss");

        addCloseClickHandler(new CloseClickHandler()
        {
            @Override
            public void onCloseClick(CloseClickEvent event)
            {
                destroy();
            }
        });
        canc.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                destroy();
            }
        });
        hbar.addMember(canc);

        MXVBox main = new MXVBox();

        main.setMargin(5);

        main.setMembersMargin(5);

        MXLabel text = new MXLabel("A Fatal error occured.  See below for description");

        text.setHeight(40);

        final MXHBox error = new MXHBox();

        error.setMargin(5);

        final MXLabel label = new MXLabel();

        label.setWidth("100%");

        label.setHeight("20px");

        error.addMember(label);

        error.setPadding(10);

        label.setContents(message);

        label.setIcon("exclamation.png");

        error.setBackgroundColor("pink");

        error.setBorder("1px solid red");

        main.addMember(text);

        main.addMember(error);

        main.addMember(hbar);

        addItem(main);
        
        show();
    }
    
    public ErrorWindow(String message, String exception)
    {
        super();

        setWidth(500);
        
        setHeight(150);
        
        setMinHeight(150);
        
        setAutoHeight();
        
        setAutoSize(true);

        setOverflow(Overflow.VISIBLE);

        setTitle("Error");

        setShowMinimizeButton(false);

        setDismissOnEscape(false);

        setIsModal(true);

        setShowModalMask(false);

        setShowShadow(true);

        setShadowSoftness(3);

        setShadowOffset(3);

        centerInPage();
        
        MXHBox hbar = new MXHBox();

        hbar.setHeight(25);

        hbar.setWidth100();

        hbar.setMembersMargin(5);

        hbar.setAlign(Alignment.RIGHT);

        final MXButton canc = new MXButton("Dismiss");

        addCloseClickHandler(new CloseClickHandler()
        {
            @Override
            public void onCloseClick(CloseClickEvent event)
            {
                destroy();
            }
        });
        canc.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                destroy();
            }
        });
        hbar.addMember(canc);
        
        MXLabel text = new MXLabel(message);

        text.setHeight(40);
        
        MXVBox msgVB = new MXVBox();
        
        msgVB.setWidth100();
        
        msgVB.setHeight(35);
        
        msgVB.setPadding(5);
        
        msgVB.addMember(text);
        
        addItem(msgVB);
        
        MXVBox stackVB = new MXVBox();
        
        stackVB.setWidth100();
        
        stackVB.setHeight100();
        
        stackVB.setPadding(5);
        
        stackVB.setMembersMargin(5);
        
        MXAccordion stack = new MXAccordion();

        stack.setWidth100();

        stack.setHeight100();

        stack.setAnimateSections(false);

        stack.setVisibilityMode(VisibilityMode.MUTEX);

        stack.setOverflow(Overflow.VISIBLE);
        
        MXVBox main = new MXVBox();
        
        main.setPadding(5);

        final MXHBox error = new MXHBox();

        final MXLabel label = new MXLabel();

        label.setWidth("100%");

        label.setHeight("20px");

        error.addMember(label);

        error.setPadding(10);

        label.setContents(exception);

        label.setIcon("exclamation.png");

        error.setBackgroundColor("pink");

        error.setBorder("1px solid red");

        main.addMember(error);
        
        MXAccordionSection acc = new MXAccordionSection();

        acc.setCanCollapse(true);

        acc.setTitle("&nbsp;&nbsp;Error Details");
        
        acc.setExpanded(false);
        
        acc.addItem(main);
        
        stack.addSection(acc);
        
        stackVB.addMember(stack);
        
        stackVB.addMember(hbar);

        addItem(stackVB);
        
        show();
    }
}