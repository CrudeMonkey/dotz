
package com.enno.dotz.client.ui;

import com.ait.tooling.nativetools.client.NArray;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridRecord;

public class MXListGrid extends ListGrid
{
    private String m_gridSettingsId;

    public MXListGrid()
    {
        super();

        setCanAutoFitFields(false);

        // rlb - this is a nifty setting you can enable that prevents that annoying white chunking that 
        // happens when you scroll a grid that contains more rows than visible, but in hosted mode, 
        // setting thisfor grids with even 300 rows of data hangs the browser...smaller datasets it's fine, 
        // but I'm leaving this in here in case anyone else finds this annoying and is looking for a solution
        // and maybe a future release of SmartGWT will optimize this...
        //
        // added to set optionally if running in compiled mode

        if (GWT.isScript())
        {
            setShowAllRecords(true);
        }
        
        //setBaseStyle("MXGrid");
    }
    
    public String getGridSettingsId()
    {
        return m_gridSettingsId;
    }

    /**
     * If this method is used, we will add an extra menu item to the right-click menu of each column 
     * called "Save Column Settings" that will save the viewState
     * (see {@link #getViewState()}) in the database under the specified gridSettingsId.
     * You can load this state with {@link #loadGridSettings(AsyncCallback, String...)} and then set it by calling {@link #setViewState(String)} e.g.
     * <pre>
     * final MXListGrid grid = new MXListGrid();
     * grid.setGridSettingsId("MY_GRID");
     * MXListGrid.loadSettings(new MAsyncCallback<NObject>() {
     *   public void onSuccess(NObject result) {
     *       String state = result.getAsString("MY_GRID");
     *       grid.setViewState(state);
     *       // etc...
     *   }
     * }, "MY_GRID");
     * </pre>
     * 
     * Make sure to copy the state with {@link #getViewState()} and {@link #setViewState(String)} when destroying and re-creating the grid.
     * 
     * @param gridSettingsId
     */
    public void setGridSettingsId(String gridSettingsId)
    {
        m_gridSettingsId = gridSettingsId;
    }

//    @Override  
//    protected MenuItem[] getHeaderContextMenuItems(final Integer fieldNum)
//    {
//        MenuItem[] items = super.getHeaderContextMenuItems(fieldNum);
//        
//        items = addMenuItems(items, fieldNum);
//        
//        return items;
//    }
    
//    protected MenuItem[] addMenuItems(MenuItem[] items, final Integer fieldNum)
//    {
//        if (m_gridSettingsId == null)
//            return items;
//        
//        MenuItem customItem = new MenuItem("Save Column Settings", "save.png");
//        
//        customItem.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler()
//        {
//            @Override
//            public void onClick(MenuItemClickEvent event)
//            {
//                saveGridColumns();
//            }
//        });
//        
//        MenuItem[] newItems = new MenuItem[items.length + 1];
//        int count = 0;
//        for (int i = 0; i < items.length; i++)
//        {
//            MenuItem item = items[i];
//            
//            newItems[count++] = item;
//            if ("Columns".equals(item.getTitle()))
//            {
//                newItems[count++] = customItem;
//            }
//        }
//        
//        return newItems;
//    }    

//    protected void saveGridColumns()
//    {
//        String state = getViewState();
//        
//        NObject req = new NObject();
//        req.put("userId", ClientCache.getInstance().getUserId());
//        req.put("gridId", m_gridSettingsId);
//        req.put("settings", state);
//        
//        JSONServiceRequest.result("SetGridSettingsService", req, CallbackUtils.waiting("Saving grid settings...", new JSONAsyncCallback()
//        {
//            @Override
//            public void onSuccess(NObject result)
//            {
//            }
//            
//            @Override
//            public void onFailure(Throwable e)
//            {
//                SC.warn("Saving grid settings failed");
//            }
//        }));
//    }
//
//    /**
//     * Loads the grid settings for the specified gridSettingIds and invokes the callback.
//     * The callback will be called with an empty NObject if the DB call fails, because you probably still want to continue.
//     * The NObject passed to onSuccess is basically a Map, mapping each gridSettingId to the state string that can be passed to {@link MXListGrid#setViewState(String)}
//     * (or null.)
//     * 
//     * @param callback
//     * @param gridSettingIds
//     */
//    public static void loadGridSettings(final AsyncCallback<NObject> callback, String... gridSettingIds)
//    {
//        NArray gridIds = new NArray();
//        for (String id : gridSettingIds)
//        {
//            gridIds.push(id);
//        }
//        
//        NObject req = new NObject();
//        req.put("userId", ClientCache.getInstance().getUserId());
//        req.put("gridIds", gridIds);
//        
//        JSONServiceRequest.result("GetGridSettingsService", req, CallbackUtils.waiting("Loading grid settings...", new JSONAsyncCallback()
//        {
//            @Override
//            public void onSuccess(NObject result)
//            {
//                NObject data = result.getAsNObject("data");
//                callback.onSuccess(data);
//            }
//            
//            @Override
//            public void onFailure(Throwable e)
//            {
//                SC.say("Loading of grid settings failed");
//                callback.onSuccess(new NObject());
//            }
//        }));
//    }
    
    public boolean isValid()
    {
        boolean allgood = true;

        for (int i = 0; i < getRecords().length; i++)
        {
            if (validateRow(i) == false)
            {
                allgood = false;
                break;
            }
        }
             
        return allgood;
    }
    
    public void setData(NArray a)
    {
        setData(MXRecordList.toRecordArray(a));
    }

    public void removeAllRecords()
    {
        for (ListGridRecord rec : getRecords())
            removeData(rec);
    }
}
