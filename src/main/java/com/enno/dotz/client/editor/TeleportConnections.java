package com.enno.dotz.client.editor;

import java.util.ArrayList;
import java.util.List;

import com.ait.lienzo.client.core.shape.IPrimitive;
import com.enno.dotz.client.Cell;
import com.enno.dotz.client.Cell.Teleport;
import com.enno.dotz.client.GridState;

public class TeleportConnections
{
    public static class Link
    {
        public Teleport src;
        public Teleport target;
        
        public IPrimitive<?> line;
        
        public Link(Teleport src, Teleport target)
        {
            this.src = src;
            this.target = target;
        }
    }
    
    private List<Teleport> m_sources = new ArrayList<Teleport>();
    private List<Teleport> m_targets = new ArrayList<Teleport>();
    private List<TeleportConnections.Link> m_links = new ArrayList<TeleportConnections.Link>();
    
    public TeleportConnections(GridState state)
    {
        for (int row = 0; row < state.numRows; row++)
        {
            for (int col = 0; col < state.numColumns; col++)
            {
                Cell cell = state.cell(col,  row);
                if (cell.isTeleportSource())
                    m_sources.add((Teleport) cell);
                else if (cell.isTeleportTarget())
                    m_targets.add((Teleport) cell);
            }
        }
        
        for (int i = m_sources.size() - 1; i >= 0; i--)
        {
            Teleport src = m_sources.get(i);
            if (state.isValidCell(src.getOtherCol(), src.getOtherRow()))
            {
                Cell cell = state.cell(src.getOtherCol(), src.getOtherRow());
                if (cell.isTeleportTarget())
                {
                    Teleport target = (Teleport) cell;
                    m_sources.remove(src);
                    m_targets.remove(target);
                    m_links.add(new Link(src, target));
                    
                    // Make sure link direction is bidirectional
                    target.setOther(src.col, src.row);
                }
            }
        }
        
        for (Teleport src : m_sources)
            src.setDisconnected();
        
        for (Teleport target : m_targets)
            target.setDisconnected();            
    }
    
    public boolean isAllConnected()
    {
        return m_sources.size() == 0 && m_targets.size() == 0;
    }
    
    public List<TeleportConnections.Link> getLinks()
    {
        return m_links;
    }

    public TeleportConnections.Link getSourceLink(Teleport src)
    {
        for (TeleportConnections.Link link : m_links)
        {
            if (link.src == src)
                return link;
        }
        return null;
    }

    public TeleportConnections.Link getTargetLink(Teleport target)
    {
        for (TeleportConnections.Link link : m_links)
        {
            if (link.target == target)
                return link;
        }
        return null;
    }

    public TeleportConnections.Link link(Teleport src, Teleport target)
    {
        TeleportConnections.Link srcLink = getSourceLink(src);
        if (srcLink != null)
        {
            m_targets.add(srcLink.target);
            srcLink.target.setDisconnected();
            m_links.remove(srcLink);
        }
        else
            m_sources.remove(src);
        
        TeleportConnections.Link targetLink = getTargetLink(target);
        if (targetLink != null)
        {
            m_sources.add(targetLink.src);
            targetLink.src.setDisconnected();
            m_links.remove(targetLink);
        }
        else
            m_targets.remove(target);
        
        TeleportConnections.Link link = new Link(src, target);
        src.setOther(target.col, target.row);
        target.setOther(src.col, src.row);
        
        m_links.add(link);
        return link;
    }
}