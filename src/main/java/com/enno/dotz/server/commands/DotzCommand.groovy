package com.enno.dotz.server.commands

import java.util.Map

import org.apache.log4j.Logger

import com.ait.tooling.server.core.json.JSONObject
import com.ait.tooling.server.rpc.IJSONRequestContext
import com.ait.tooling.server.rpc.JSONCommandSupport

abstract class DotzCommand extends JSONCommandSupport
{
    private Logger m_log
        
    protected DotzCommand()
    {
        m_log = Logger.getLogger(getClass())
    }
    
    public LevelDB getDb()
    {
        return LevelDB.INSTANCE
    }
    
    public JSONObject execute(IJSONRequestContext context, JSONObject params) throws Exception
    {
        try
        {
            Map map = exec(context, params)
            if (map instanceof JSONObject)
                return map
            else
                return new JSONObject(map)
        }
        catch (DBException e)
        {
            m_log.error("DB error", e);
            
            String msg = e.message
            if (e.cause != null)
                msg += ": ${e.cause.message}"
                
            return error(msg)
        }
        catch (Exception e)
        {
            m_log.error("Unknown error", e)
            return error(e.class.name + ": " + e.message)
        }
    }
    
    public abstract Map exec(IJSONRequestContext context, JSONObject params) throws Exception;    
    
    protected JSONObject error(String msg)
    {
        new JSONObject('error', msg)
    }
}
