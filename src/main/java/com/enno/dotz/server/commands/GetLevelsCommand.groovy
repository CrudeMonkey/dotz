package com.enno.dotz.server.commands

import java.util.List
import java.util.Map

import org.springframework.stereotype.Service

import com.ait.tooling.server.core.json.JSONObject
import com.ait.tooling.server.rpc.IJSONRequestContext

@Service
class GetLevelsCommand extends DotzCommand
{
    @Override
    public Map exec(IJSONRequestContext context, JSONObject params) throws Exception
    {
        List list = db.levelList
        
        if (params.type == 'tree')
        {
            list = makeTree(list)
        }
        return [levels: list]
    }
    
    protected List makeTree(List list)
    {
         int id = 0
         Map root = [dirs: [:] as TreeMap, levels: [], primary: "0"]
         
         def makeDir
         makeDir = { Map dir, Map level, String name ->
             if (!name)
             {
                 level.primary = "" + ++id
                 level.parent = dir.primary
                 dir.levels << level
             }
             else
             {
                 int slash = name.indexOf("/")
                 String rest = "";                 
                 if (slash != -1)
                 {
                     rest = name.substring(slash + 1)
                     name = name.substring(0, slash)
                 }
                 
                 Map subdir = dir.dirs[name]
                 if (subdir == null)
                 {
                     subdir = [name: name, dirs: [:] as TreeMap, levels: [],
                         primary: "" + ++id, parent: dir.primary]
                     dir.dirs[name] = subdir
                 }
                 
                 makeDir(subdir, level, rest)                 
             }
         }
         
         list.each{ level ->
             makeDir(root, level, level.folder)
         }
         
         List newList = []
         def addDir
         addDir = { Map dir ->
             if (dir.primary != "0") // don't include root
                 newList << dir
                 
             dir.dirs.each{ k, v ->
                 addDir(v)
             }
             dir.levels.each{ level -> newList << level }
             
             dir.remove('dirs')
             dir.remove('levels')
         }
         addDir(root)
         
         return newList
    }  
}
