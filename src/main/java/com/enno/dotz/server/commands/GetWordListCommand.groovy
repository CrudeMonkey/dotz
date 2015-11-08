package com.enno.dotz.server.commands

import java.util.Map

import org.springframework.stereotype.Service

import com.ait.tooling.server.core.json.JSONObject
import com.ait.tooling.server.rpc.IJSONRequestContext

@Service
class GetWordListCommand extends DotzCommand
{
    @Override
    public Map exec(IJSONRequestContext context, JSONObject params) throws Exception
    {
        String list = getClass().getResource('/enable1.txt').text
        return [list: list]
    }    
}
