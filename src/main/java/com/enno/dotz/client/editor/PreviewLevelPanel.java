package com.enno.dotz.client.editor;

import com.ait.lienzo.client.core.shape.Arrow;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.shared.core.types.ArrowType;
import com.ait.lienzo.shared.core.types.ColorName;
import com.enno.dotz.client.BoostPanel;
import com.enno.dotz.client.Cell;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.DotzGridPanel;
import com.enno.dotz.client.DotzGridPanel.EndOfLevel;
import com.enno.dotz.client.GridState;
import com.enno.dotz.client.MainPanel.GridContainer;
import com.enno.dotz.client.ScorePanel;
import com.enno.dotz.client.editor.TeleportConnections.Link;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.layout.VLayout;

public class PreviewLevelPanel extends VLayout
{
    private DotzGridPanel m_grid;
    private Context ctx;
    private ScorePanel m_score;
    private BoostPanel m_boostPanel;    
    private Layer m_connectionLayer;

    public PreviewLevelPanel(Config level)
    {
        setPadding(10);
        setAlign(Alignment.CENTER);
        setDefaultLayoutAlign(Alignment.CENTER);
        setWidth((Integer) null);
        setHeight((Integer) null);
        
        Context ctx = new Context();
        ctx.cfg = level;
        level.size = 40;
        
        this.ctx = ctx;
        
        EndOfLevel endOfLevel = null;
        m_grid = new DotzGridPanel(ctx, endOfLevel);

        m_score = new ScorePanel(ctx);
        ctx.scorePanel = m_score;
        addMember(m_score);
        
        GridContainer g = new GridContainer();            
        g.addMember(m_grid, ctx.cfg);
        addMember(g);
        
        m_boostPanel = new BoostPanel(ctx);
        ctx.boostPanel = m_boostPanel;
        addMember(m_boostPanel);        
        
        m_grid.init(false);
        initItemGraphics();

        m_connectionLayer = new Layer();
        m_connectionLayer.setListening(false);
        m_grid.add(m_connectionLayer);
        initTeleportInfo();
        
        m_grid.draw();        
        
        m_score.setGoals(ctx.cfg.goals);        
    }
    
    protected void initItemGraphics()
    {
        int nc = ctx.cfg.numColumns;
        int nr = ctx.cfg.numRows;
        
        GridState state = ctx.state;
        for (int row = 0; row < nr; row++)
        {
            for (int col = 0; col < nc; col++)
            {
                Cell cell = state.cell(col, row);
                if (cell.item != null)
                {
                    cell.item.moveShape(state.x(col), state.y(row));
                    cell.item.addShapeToLayer(ctx.dotLayer);
                }
            }
        }
    }
    
    protected void initTeleportInfo()
    {
        GridState state = ctx.state;
        TeleportConnections connections = new TeleportConnections(state);
        
        for (Link link : connections.getLinks())
        {
            createLinkLine(state, link);
        }
    }
    
    protected void createLinkLine(GridState state, Link link)
    {
        Arrow line = new Arrow(new Point2D(state.x(link.src.col), state.y(link.src.row)), 
                new Point2D(state.x(link.target.col), state.y(link.target.row)),
                3, 15, 30, 60, ArrowType.AT_END);
        line.setFillColor(ColorName.BLUE);
        
        m_connectionLayer.add(line);
        
        link.line = line;
    }
}
