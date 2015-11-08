package com.enno.dotz.server.commands

import java.util.Collection;
import java.util.Set;

import com.ait.tooling.server.core.json.JSONArray
import com.ait.tooling.server.core.json.JSONObject
import com.ait.tooling.server.core.json.parser.JSONParser
import com.ait.tooling.server.core.json.JSONUtils

class PrettyPrinter
{
    private StringBuilder b = new StringBuilder()
    private int level
    
    private Set<String> dont = [] as Set
    
    public PrettyPrinter(Collection dont)
    {
        this.dont = dont as Set
    }
    
    public String toString(JSONObject p)
    {
        level = 0
        add(false, p, true, true)
        return b.toString()
    }
    
    protected void add(boolean afterKey, JSONObject p, boolean expand, boolean expandChildren)
    {
        if (expand)
        {
            if (!afterKey)
                indent()
            b << "{"
        
            level++
            p.eachWithIndex{ k, v, i -> 
                b << "\n"
                indent()
                b << "\"$k\": "
                add(true, v, expandChildren, expandChildren && !dont.contains(k))
                if (i != p.size() - 1)
                    b << ","
            }
            level--
            
            if (p.size() > 0)
                b << "\n"
            
            indent()
            b << "}"
        }
        else
        {
            b << p.toJSONString()
        }
    }
    
    protected void add(boolean afterKey, JSONArray p, boolean expand, boolean expandChildren)
    {
        if (expand)
        {
            if (!afterKey)
                indent()
            b << "["
        
            level++
            p.eachWithIndex{ v, i -> 
                b << "\n"
                indent()
                add(false, v, expandChildren, expandChildren)
                if (i != p.size() - 1)
                    b << ","
            }
            level--
            
            if (p)
                b << "\n"

            indent()
            b << "]"
        }
        else
        {
            b << p.toJSONString()
        }
    }
    
    protected void add(boolean afterKey, def p, boolean expand, boolean expandChildren)
    {
        b << JSONUtils.toJSONString(p, true)
    }
    
    protected void indent()
    {
        level.times{ b.append("  ") }
    }
    
    public static void main(String[] args)
    {
        def stream = new FileInputStream("c:/workspace/dotz/data/levels/0.json")
        String s = stream.getText()
        Object json = new JSONParser().parse(s)
        
        PrettyPrinter pr = new PrettyPrinter(['teleporters', 'conveyors', 'lazySusans', 'doors', 'animals', 'freq'])
        
        println pr.toString(json)
    }
}
