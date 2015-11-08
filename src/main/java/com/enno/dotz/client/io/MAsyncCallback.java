package com.enno.dotz.client.io;


import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class MAsyncCallback<T> implements AsyncCallback<T>
{
    @Override
    public void onFailure(Throwable caught)
    {
    }
}
