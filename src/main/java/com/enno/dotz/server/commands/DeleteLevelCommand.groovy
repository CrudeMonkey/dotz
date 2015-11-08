package com.enno.dotz.server.commands

import java.util.Map

import org.springframework.stereotype.Service

import com.ait.tooling.server.core.json.JSONObject
import com.ait.tooling.server.rpc.IJSONRequestContext

@Service
class DeleteLevelCommand extends DotzCommand
{
    @Override
    public Map exec(IJSONRequestContext context, JSONObject params) throws Exception
    {        
        return [success: db.deleteLevel(params.id)]
    }    
}
