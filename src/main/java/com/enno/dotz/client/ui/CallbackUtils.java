package com.enno.dotz.client.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class CallbackUtils
{
    /**
     * Wraps the callback such that a MXWaitingWindow will be shown with 
     * the text "Loading data...". 
     * 
     * The window will be destroyed when a response is received 
     * (whether it's successful or not.)
     * 
     * @param <T>
     * @param cb
     * @return
     */
    public static <T> AsyncCallback<T> waiting(final AsyncCallback<T> cb)
    {
        return waiting("Loading data...", cb);
    }
    
    /**
     * Wraps the callback such that a MXWaitingWindow will be shown with 
     * the specified message, e.g. "Loading data...". 
     * 
     * The window will be destroyed when a response is received 
     * (whether it's successful or not.)
     * 
     * @param <T>
     * @param cb
     * @return
     */
    public static <T> AsyncCallback<T> waiting(String message, final AsyncCallback<T> cb)
    {
        final MXWaitingWindow waiting = new MXWaitingWindow(message);
        waiting.show();
        
        return new AsyncCallback<T>() {

            @Override
            public void onSuccess(T result)
            {
                waiting.destroy();
                cb.onSuccess(result);
            }
            
            public void onFailure(Throwable caught)
            {
                waiting.destroy();
                cb.onFailure(caught);
            }
        };
    }
}
