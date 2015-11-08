package com.enno.dotz.client.ui;

/**
 * Wraps the "error" field of a JSON service response, so it can be displayed appropriately in the ErrorWindow.
 * 
 * @author ederksen
 */
@SuppressWarnings("serial")
public class ServiceErrorException extends Exception
{
    public ServiceErrorException(String error)
    {
        super(error);
    }
}
