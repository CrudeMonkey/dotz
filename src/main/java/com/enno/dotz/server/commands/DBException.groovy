package com.enno.dotz.server.commands

class DBException extends Exception
{
    DBException(String msg)
    {
        super(msg)
    }
    
    DBException(String msg, Throwable th)
    {
        super(msg, th)
    }
}
