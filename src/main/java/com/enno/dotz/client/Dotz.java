
package com.enno.dotz.client;

import com.google.gwt.core.client.EntryPoint;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Dotz implements EntryPoint
{
    /**
     * This is the entry point method.
     */
    public void onModuleLoad()
    {
        MainPanel mainPanel = new MainPanel();
        mainPanel.draw();
        //RootPanel.get().add(mainPanel); <-- this does not work!
        mainPanel.init();
    }
}
