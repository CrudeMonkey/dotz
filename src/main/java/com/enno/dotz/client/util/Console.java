package com.enno.dotz.client.util;

//package com.emitrom.pilot.util.client.core;

public class Console {
    /**
     * Log a message to the browser's console window.
     * 
     * @param message
     */
    public static native void log(String message)
    /*-{
        if ($wnd.console) {
            $wnd.console.log(message);
        }
    }-*/;
}
