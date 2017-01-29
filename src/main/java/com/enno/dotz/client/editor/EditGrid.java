package com.enno.dotz.client.editor;

import com.ait.lienzo.client.core.shape.FastLayer;
import com.ait.lienzo.client.core.shape.Line;
import com.ait.lienzo.shared.core.types.ColorName;
import com.enno.dotz.client.Cell;
import com.enno.dotz.client.Cell.ItemCell;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.DotzGridPanel;
import com.enno.dotz.client.GridState;
import com.enno.dotz.client.anim.Pt;
import com.enno.dotz.client.anim.Pt.PtList;
import com.enno.dotz.client.item.RandomItem;

public class EditGrid extends DotzGridPanel
{
    private FastLayer m_loopLayer;

    public EditGrid(boolean isNew, Context ctx)
    {
        super(ctx, null);
        
        if (isNew)
        {
            initState();
        }
        
        init(false);
        initItemGraphics();
        
        setBorders();
        
        m_loopLayer = new FastLayer();
        add(m_loopLayer);
        
        draw();
    }
    
    protected void initState()
    {
        int nc = ctx.cfg.numColumns;
        int nr = ctx.cfg.numRows;
        
        GridState state = new GridState(nc, nr);
        for (int row = 0; row < nr; row++)
        {
            for (int col = 0; col < nc; col++)
            {
                ItemCell cell = new ItemCell();
                cell.item = new RandomItem();
                state.setCell(col, row, cell);
            }
        }
        ctx.cfg.grid = state;
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

    public void prepareSave(Config level)
    {
        // TODO Auto-generated method stub
        
    }
    
    public void showLoop(PtList list)
    {
        m_loopLayer.removeAll();
        
        GridState state = ctx.state;
        for (int i = 0, n = list.size(); i < n; i++)
        {
            Pt p = list.get(i);
            Pt q = list.get(i == n - 1 ? 0 : i + 1);
            double x1 = state.x(p.col);
            double y1 = state.y(p.row);
            double x2 = state.x(q.col);
            double y2 = state.y(q.row);
            
            Line line = new Line(x1, y1, x2, y2);
            line.setStrokeColor(ColorName.BLACK);
            line.setStrokeWidth(2);
            m_loopLayer.add(line);
        }
        m_loopLayer.setVisible(true);
        m_loopLayer.draw();
    }
    
    public void showLoopLayer(boolean show)
    {
        m_loopLayer.setVisible(show);
    }
}