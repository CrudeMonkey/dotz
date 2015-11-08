package com.enno.dotz.server.commands

import java.util.Map;

import org.springframework.stereotype.Service

import com.ait.tooling.server.core.json.JSONArray
import com.ait.tooling.server.core.json.JSONObject
import com.ait.tooling.server.rpc.IJSONRequestContext

@Service
class OrganizeLevelsCommand extends DotzCommand
{
    @Override
    public Map exec(IJSONRequestContext context, JSONObject params) throws Exception
    {
        JSONArray levels = params.levels
        db.organizeLevels(levels)
        return [success: true]
    }    
}
