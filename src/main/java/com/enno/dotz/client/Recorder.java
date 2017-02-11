package com.enno.dotz.client;

import com.ait.tooling.nativetools.client.NArray;
import com.ait.tooling.nativetools.client.NObject;
import com.enno.dotz.client.anim.Pt;
import com.enno.dotz.client.anim.Pt.PtList;
import com.enno.dotz.client.editor.LevelParser;
import com.enno.dotz.client.io.ClientRequest;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.util.Console;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class Recorder
{
    private Context ctx;    //TODO remove ctx

    private String m_id;
    private NArray m_rows = new NArray();
    
    public Recorder(Context ctx)
    {
        this.ctx = ctx;
    }
    
    public Recorder(NArray rows)
    {
        m_rows = rows;
    }

    public NArray getRows()
    {
        return m_rows;
    }

    public void level(int level_id, long seed, String levelName)
    {
        NObject row = new NObject("level", level_id);
        row.put("seed", "" + seed);
        row.put("levelName", levelName);
        add(row);
    }
    
    public int getLevelId()
    {
        NObject row = m_rows.getAsObject(0);
        return row.getAsInteger("level");
    }

    public long getSeed()
    {
        NObject row = m_rows.getAsObject(0);
        return Long.parseLong(row.getAsString("seed"));
    }

    public void undo()
    {
        NObject row = new NObject();
        row.put("type", "undo");
        add(row);
    }
    
    public void redo()
    {
        NObject row = new NObject();
        row.put("type", "redo");
        add(row);
    }
    
    public void click(Cell cell)
    {
        NObject row = new NObject();
        row.put("click", p(cell));
        add(row);
    }
    
    public void drag(CellList list)
    {
        NObject row = new NObject();
        row.put("drag", p(list));
        add(row);
    }
    
    public void swap(Cell c, Cell d)
    {
        NObject row = new NObject();
        row.put("swap", p(c, d));
        add(row);
    }
    
    public void special(Cell c, Cell d)
    {
        NObject row = new NObject();
        row.put("special", p(c, d));
        add(row);
    }
    
    public void boost(Cell c, Item boost)
    {
        NObject row = new NObject();
        row.put("boost", p(c));
        row.put("item", LevelParser.toJson(boost, true));
        add(row);
    }

    protected NArray p(Cell c)
    {
        NArray a = new NArray();
        if (c == null)
        {
            a.push(-1);
            a.push(-1);
        }
        else
        {
            a.push(c.col);
            a.push(c.row);
        }
        return a;
    }
    
    protected NArray p(Cell src, Cell target)
    {
        NArray a = new NArray();
        
        a.push(src.col);
        a.push(src.row);
        
        if (target == null)
        {
            a.push(-1);
            a.push(-1);
        }
        else
        {
            a.push(target.col);
            a.push(target.row);
        }
        return a;
    }
    
    protected NArray p(CellList list)
    {
        NArray a = new NArray();
        for (Cell c : list)
        {
            a.push(c.col);
            a.push(c.row);
        }
        return a;
    }
    
    protected NArray p(int col, int row)
    {
        NArray a = new NArray();
        a.push(col);
        a.push(row);
        return a;
    }
    
    protected void add(NObject row)
    {
        Console.log(row.toJSONString());
        m_rows.push(row);
        
        ClientRequest.record(row, m_id, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught)
            {
                // TODO Auto-generated method stub
            }

            @Override
            public void onSuccess(String id)
            {
                m_id = id;
            }
        });
    }

    public static PtList parsePtList(NObject row)
    {
        PtList list = new PtList();
        NArray p = row.getAsArray(getAction(row));
        if (p == null)
            return null;
        
        for (int i = 0, n = p.size(); i < n; i += 2)
        {
            if (p.getAsInteger(i) != -1)    // some specials have (-1,-1) as first point
                list.add(new Pt(p.getAsInteger(i), p.getAsInteger(i + 1)));
        }
        return list;
    }
    
    public static String getAction(NObject row)
    {
        if (row.isDefined("type"))
            return row.getAsString("type");
        
        if (row.isArray("drag"))
            return "drag";
        else if (row.isArray("swap"))
            return "swap";
        else if (row.isArray("click"))
            return "click";
        else if (row.isArray("special"))
            return "special";
        else if (row.isArray("boost"))
            return "boost";
        
        return null;
    }
}
