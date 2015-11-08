package com.enno.dotz.server.commands

import org.springframework.stereotype.Service

import com.ait.tooling.server.core.json.JSONObject
import com.ait.tooling.server.rpc.IJSONRequestContext

@Service
class LoadLevelSetCommand extends DotzCommand
{
    @Override
    public JSONObject exec(IJSONRequestContext context, JSONObject params) throws Exception
    {
        return db.loadSet(params.id)
    }    
}
