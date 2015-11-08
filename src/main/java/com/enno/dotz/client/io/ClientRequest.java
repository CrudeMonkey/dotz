
package com.enno.dotz.client.io;

import com.ait.tooling.gwtdata.client.rpc.JSONCommandCallback;
import com.ait.tooling.gwtdata.client.rpc.JSONCommandRequest;
import com.ait.tooling.nativetools.client.NArray;
import com.ait.tooling.nativetools.client.NObject;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.editor.LevelParser;
import com.enno.dotz.client.ui.CallbackUtils;
import com.enno.dotz.client.ui.ServiceErrorException;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ClientRequest
{
    private static JSONCommandRequest s_req = new JSONCommandRequest("JSONCommand.rpc", false);
    
    public static void loadLevel(int levelId, final AsyncCallback<Config> callback)
    {
        NObject req = new NObject("id", levelId);
        request("LoadLevelCommand", req, new ServiceCallback() {
            @Override
            public void onSuccess(NObject level)
            {
                Config cfg = new LevelParser().parse(level);
                callback.onSuccess(cfg);
            }
        });
    }
    
    public static void loadLevelSet(int setId, final ServiceCallback callback)
    {
        NObject req = new NObject("id", setId);
        request("LoadLevelSetCommand", req, callback);
    }    

    public static void saveLevel(Config level, ServiceCallback callback)
    {
        NObject req = new NObject("level", LevelParser.toJson(level));
        request("SaveLevelCommand", req, CallbackUtils.waiting("Saving Level...", callback));
    }

    public static void saveSet(NObject set, ServiceCallback callback)
    {
        NObject req = new NObject("set", set);
        request("SaveSetCommand", req, CallbackUtils.waiting("Saving Set...", callback));
    }

    public static void organizeLevels(NArray levels, ServiceCallback callback)
    {
        NObject req = new NObject("levels", levels);
        request("OrganizeLevelsCommand", req, CallbackUtils.waiting("Saving Levels...", callback));
    }

    public static void deleteLevel(int levelId, ServiceCallback callback)
    {
        NObject req = new NObject("id", levelId);
        request("DeleteLevelCommand", req, CallbackUtils.waiting("Deleting Level...", callback));
    }

    public static void getLevelList(boolean tree, final MAsyncCallback<NArray> callback)
    {
        NObject req = new NObject();
        if (tree)
            req.put("type", "tree");
        
        request("GetLevelsCommand", req, CallbackUtils.waiting("Loading Levels...", new ServiceCallback() {

            @Override
            public void onSuccess(NObject result)
            {
                NArray levels = result.getAsArray("levels");
                callback.onSuccess(levels);
            }
        }));
    }

    public static void getSetList(final MAsyncCallback<NArray> callback)
    {
        NObject req = new NObject();
        request("GetSetsCommand", req, CallbackUtils.waiting("Loading Sets...", new ServiceCallback() {

            @Override
            public void onSuccess(NObject result)
            {
                NArray levels = result.getAsArray("sets");
                callback.onSuccess(levels);
            }
        }));
    }
    
    public static void getWordList(final MAsyncCallback<String> callback)
    {
        NObject req = new NObject();
        request("GetWordListCommand", req, CallbackUtils.waiting("Loading Word List...", new ServiceCallback() {

            @Override
            public void onSuccess(NObject result)
            {
                String list = result.getAsString("list");
                callback.onSuccess(list);
            }
        }));
    }

    public static void saveScore(int time, int score, int moves, int levelId, String userName, final ServiceCallback callback)
    {
        NObject req = new NObject();
        req.put("time", time);
        req.put("score", score);
        req.put("moves", moves);
        req.put("levelId", levelId);
        req.put("user", userName);
        
        request("SaveScoreCommand", req, CallbackUtils.waiting("Saving Score...", new ServiceCallback() {
            @Override
            public void onSuccess(NObject result)
            {
                callback.onSuccess(result);
            }
        }));
    }

    public static void getScores(int levelId, final ServiceCallback callback)
    {
        NObject req = new NObject();
        req.put("id", levelId);
        
        request("GetScoresCommand", req, CallbackUtils.waiting("Getting Scores...", new ServiceCallback() {
            @Override
            public void onSuccess(NObject result)
            {
                callback.onSuccess(result);
            }
        }));
    }
    
    protected static void request(String command, NObject request, final AsyncCallback<NObject> callback)
    {        
        s_req.call(command, request, new JSONCommandCallback() {
            @Override
            public void onSuccess(NObject result)
            {
                if (result.isDefined("error"))
                {
                    callback.onFailure(new ServiceErrorException(result.getAsString("error")));
                }
                else
                {
                    callback.onSuccess(result);
                }
            }
            
            @Override
            public void onFailure(Throwable t)
            {
                callback.onFailure(t);
            }
        });
    }

}
