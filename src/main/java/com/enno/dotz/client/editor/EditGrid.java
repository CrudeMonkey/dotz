package com.enno.dotz.client.editor;

import com.enno.dotz.client.Cell;
import com.enno.dotz.client.Cell.ItemCell;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.DotzGridPanel;
import com.enno.dotz.client.GridState;
import com.enno.dotz.client.item.RandomItem;

public class EditGrid extends DotzGridPanel
{
    public EditGrid(boolean isNew, Context ctx)
    {
        super(ctx, null);
        
        if (isNew)
        {
            initState();
        }
        
        init(false);
        initItemGraphics();
        
        m_floorLayer.setVisible(false);
        
        setBorders();
        
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
}