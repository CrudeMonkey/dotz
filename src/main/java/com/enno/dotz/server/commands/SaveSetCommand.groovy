package com.enno.dotz.server.commands

import java.util.Map

import org.springframework.stereotype.Service

import com.ait.tooling.server.core.json.JSONObject
import com.ait.tooling.server.rpc.IJSONRequestContext

@Service
class SaveSetCommand extends DotzCommand
{
    @Override
    public Map exec(IJSONRequestContext context, JSONObject params) throws Exception
    {
        JSONObject set = params.set
        int id = db.saveSet(set)
        return [id: id]
    }    
}
