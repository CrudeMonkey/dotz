package com.enno.dotz.client.io;

import com.ait.tooling.gwtdata.client.rpc.JSONCommandCallback;
import com.enno.dotz.client.ui.ErrorWindow;
import com.enno.dotz.client.ui.ServiceErrorException;

public abstract class ServiceCallback extends JSONCommandCallback
{
    public void onFailure(Throwable e)
    {
        if (e instanceof ServiceErrorException)
        {
            new ErrorWindow((ServiceErrorException) e);
        }
        else
        {
            new ErrorWindow(e);
        }
    }
}
