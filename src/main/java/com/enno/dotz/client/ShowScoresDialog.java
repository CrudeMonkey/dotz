package com.enno.dotz.client;

import com.ait.tooling.nativetools.client.NArray;
import com.ait.tooling.nativetools.client.NObject;
import com.enno.dotz.client.io.ClientRequest;
import com.enno.dotz.client.io.ServiceCallback;
import com.enno.dotz.client.ui.MXListGrid;
import com.enno.dotz.client.ui.MXListGridField;
import com.enno.dotz.client.ui.MXRecordList;
import com.enno.dotz.client.ui.MXWindow;
import com.enno.dotz.client.ui.UTabSet;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.VLayout;

public class ShowScoresDialog extends MXWindow
{
    public ShowScoresDialog(NObject stats)
    {
        setTitle("Scores");
        
        addItem(createPane(stats));
        
        setWidth(600);
        setHeight(400);
        
        centerInPage();
        show();
    }

    private Canvas createPane(NObject stats)
    {
        VLayout pane = new VLayout();
        pane.setPadding(10);
        
        UTabSet tabs = new UTabSet();
        
        tabs.addTab("Best Scores", createTab(stats.getAsArray("score"), "Score"));
        tabs.addTab("Least Moves", createTab(stats.getAsArray("moves"), "Moves"));
        tabs.addTab("Best Time", createTab(stats.getAsArray("time"), "Time"));
        
        pane.addMember(tabs);
        
        return pane;
    }

    private Canvas createTab(NArray data, String title)
    {
        VLayout pane = new VLayout();
        pane.setPadding(10);
        
        MXListGrid grid = new MXListGrid();
        
        MXListGridField stat = new MXListGridField("stat", title, ListGridFieldType.TEXT, Alignment.RIGHT, 100);
        MXListGridField user = new MXListGridField("user", "User", ListGridFieldType.TEXT, Alignment.CENTER, 200);
        MXListGridField time = new MXListGridField("time", "When", ListGridFieldType.TEXT, 150);
        MXListGridField expired = new MXListGridField("expired", "Expired", ListGridFieldType.BOOLEAN, Alignment.CENTER, 50);
        expired.setCellFormatter(new CellFormatter()
        {            
            @Override
            public String format(Object value, ListGridRecord record, int rowNum, int colNum)
            {
                return Boolean.TRUE.equals(value) ? "X" : "";
            }
        });
        grid.setFields(stat, user, time, expired);
        
        grid.setData(MXRecordList.toRecordArray(data));
        
        pane.addMember(grid);
        
        return pane;
    }
    
    public static void showScores(int levelId)
    {
        if (levelId == Config.UNDEFINED_ID)
        {
            SC.say("This level was never saved, nor played.");
            return;
        }
        
        ClientRequest.getScores(levelId, new ServiceCallback()
        {
            @Override
            public void onSuccess(NObject result)
            {
                new ShowScoresDialog(result);
            }
        });
    }
}
