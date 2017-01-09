package com.enno.dotz.server.commands

import java.text.NumberFormat
import java.text.SimpleDateFormat

import org.apache.log4j.Logger

import com.ait.tooling.server.core.json.JSONObject
import com.ait.tooling.server.core.json.parser.JSONParser

class LevelDB
{
    private static Logger s_log = Logger.getLogger(LevelDB)
    
    public static LevelDB INSTANCE = new LevelDB()

    private String m_dataDirectory
    
    private String m_levelDirectory
    private String m_deletedLevelDirectory
    private String m_deletedSetDirectory
    private String m_setDirectory
    private String m_statsDirectory
    
    private LevelDB()
    {
        // Specify in CATALINA_OPTS in catalina.bat (for Tomcat)
        m_dataDirectory = System.getProperty("dotz.data.dir", "c:/workspace/dotz/data")

        s_log.info("dotz.data.dir=$m_dataDirectory (can be set via system property)")
        
        m_levelDirectory        = "$m_dataDirectory/levels"
        m_deletedLevelDirectory = "$m_dataDirectory/deleted"
        m_deletedSetDirectory   = "$m_dataDirectory/deletedSets"
        m_setDirectory          = "$m_dataDirectory/sets"
        m_statsDirectory        = "$m_dataDirectory/stats"
    }
    
    public JSONObject loadLevel(int id)
    {
        String resource = m_levelDirectory + "/" + id + ".json"
        JSONObject json = loadResource(resource)
        if (json.lastModified == null)
        {
            json.lastModified = Long.toString(new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").parse("2015/10/15 12:00:00").time)
        }
        return json
    }
    
    public boolean deleteLevel(int id)
    {
        String resource = m_levelDirectory + "/" + id + ".json"
        File file = new File(resource)
        
        new File(m_deletedLevelDirectory).mkdirs()
        
        return file.renameTo(new File(m_deletedLevelDirectory, file.getName()))
    }
    
    public boolean deleteSet(int setId)
    {
        String resource = m_setDirectory + "/" + setId + ".json"
        File file = new File(resource)
        
        new File(m_deletedSetDirectory).mkdirs()
        
        return file.renameTo(new File(m_deletedSetDirectory, file.getName()))
    }

    public JSONObject loadSet(int setId)
    {
        String resource = m_setDirectory + "/" + setId + ".json"
        JSONObject set = loadResource(resource)
        
        List levels = []
        set.levels.each{ id ->
            try
            {
                def level = loadLevel(id)            
                levels << [id: id, name: level.name, creator: level.creator]
            }
            catch (Exception e)
            {
                s_log.error("skipping level $id", e)
            }
        }
        set.levels = levels
        
        return set;
    }    
    
    public Map saveLevel(JSONObject level)
    {
        if (!level.containsKey('id'))
            level.id = newLevelID
            
        level.lastModified = Long.toString(new Date().time)
        
        String resource = m_levelDirectory + "/" + level.id + ".json"
        
        String json = new PrettyPrinter(['teleporters', 'conveyors', 'lazySusans', 'doors', 'animals', 
            'freq', 'lasers', 'mirrors', 'knights', 'dots', 'bombs', 'rockets', 'turners', 'eggs',
            'dominoes', 'drops', 'dotBombs', 'blasters', 'blockers']).toString(level)
        //String json = level.toJSONString()
        
        File file = new File(resource)
        file.delete()
        file << json
        
        return [id: level.id, lastModified: level.lastModified]
    }
    
    public Map saveScore(Map params)
    {
        String when = Long.toString(new Date().time)        
        long lastModified = Long.parseLong(loadLevel(params.levelId).lastModified)        
        String file = normalize(params.user)
        
        return [
            moves: updateScore("moves", false, when, lastModified, params.moves, file, params.user, params.levelId),
            score: updateScore("score", true, when, lastModified, params.score, file, params.user, params.levelId),
            time: updateScore("time", false, when, lastModified, params.time, file, params.user, params.levelId)
        ]
    }
    
    protected boolean updateScore(String cat, boolean max, String when, long lastModified, int stat, String fileName, String user, int levelId)
    {
        String resource = m_statsDirectory + "/$levelId/$cat/${fileName}.json"
        
        JSONObject old = null;
        try
        {
            old = loadResource(resource)
        }
        catch (Exception e)
        {
            //TODO log error if it exists
        }
        
        if (old != null)
        {
            if (Long.parseLong(old.when) > lastModified
             && ((max ? (old.stat >= stat) : (old.stat <= stat))))
                return false                            
        }
                
        JSONObject rec = new JSONObject([
            user: user,
            when: when,
            stat: stat
        ])
        
        String json = new PrettyPrinter([]).toString(rec)
        
        File file = new File(resource)
        file.parentFile.mkdirs()
        file.delete()
        file << json
        
        return true
    }
    
    public Map getScores(int levelId, int maxRecords)
    {
        NumberFormat fmt = NumberFormat.getNumberInstance()
        def formatNumber = { n ->
            fmt.format(n)        
        }
        
        long lastModified = Long.parseLong(loadLevel(levelId).lastModified)
        
        return [
            moves: loadStats("moves", false, levelId, lastModified, maxRecords, { formatNumber(it) }),
            score: loadStats("score", true, levelId, lastModified, maxRecords, { formatNumber(it) }),
            time: loadStats("time", false, levelId, lastModified, maxRecords, { formatTime(it) })
        ]        
    }
    
    private static String formatTime(int time)
    {
        int hour = time / 3600;
        int min = (time - (hour * 3600)) / 60;
        int sec = time % 60;
        
        StringBuilder b = new StringBuilder();
        if (hour > 0)
        {
            b.append(hour);
            b.append(min < 10 ? ":0" : ":");
            b.append(min);
            b.append(sec < 10 ? ":0" : ":");
            b.append(sec);
        }
        else
        {
            b.append(min);
            b.append(sec < 10 ? ":0" : ":");
            b.append(sec);
        }
        return b.toString();
    }
    
    protected List loadStats(String cat, boolean max, int levelId, long lastModified, int maxRecords, Closure formatStat)
    {
        File dir = new File(m_statsDirectory + "/$levelId/$cat")
        List list = []
        
        if (!dir.isDirectory())
            return list
            
        dir.eachFile() { file ->
            try
            {
                def rec = loadResource(file.absolutePath)
                list << rec
            }
            catch (Exception e)
            {
                s_log.error("can't load stat file ${file.absolutePath}", e)
            }
        }
        if (max)
            list.sort{ a, b -> b.stat <=> a.stat }
        else
            list.sort{ a, b -> a.stat <=> b.stat }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat('MM/dd/yyyy hh:mm:ss')
        
        list.each{ Map s -> 
            s.stat = formatStat(s.stat)
            long when = Long.parseLong(s.when)
            s.expired = when < lastModified
            s.time = dateFormat.format(new Date(when))
        }
                
        return list
    }
    
    protected static String normalize(String s)
    {
        s.replaceAll(/[^a-zA-Z0-9_]/) {'_'}
    }
    
    public int saveSet(JSONObject set)
    {
        if (!set.containsKey('id'))
            set.id = newSetID
        
        List levels = set.levels
        if (levels)
        {
            if (levels[0] instanceof Map)
            {
                set.levels = levels.collect{ it.id }
            }
        }
            
        String resource = m_setDirectory + "/" + set.id + ".json"        
        
        String json = new PrettyPrinter([]).toString(set)        
        // String json = set.toJSONString()
        
        File file = new File(resource)
        file.delete()
        file << json
        
        return set.id
    }

    public List getLevelList()
    {
        List levels = []
        eachLevel{ JSONObject level ->
            levels << [id: level.id, creator: level.creator, name: level.name, folder: level.folder ?: ""]
        }
        
        levels.sort{ a, b -> a.name <=> b.name }
        
        return levels
    }
    
    public List getSetList()
    {
        List sets = []
        eachSet{ JSONObject set ->
            sets << [id: set.id, creator: set.creator, name: set.name]
        }
        
        sets.sort{ a, b -> a.name <=> b.name }
        
        return sets
    }
    
    public void organizeLevels(List levels)
    {
        levels.each{ Map level ->
            String folder = level.folder
            if (folder.startsWith('/'))
                folder = folder.substring(1)     // remove leading slash
            
            JSONObject lev = loadLevel(level.id)
            if (lev.folder != folder)
            {
                lev.folder = folder
                saveLevel(lev)
            }
        }
    }
    
    public void saveRecentLevels(List<Integer> levelIds)
    {
        File f = new File("$m_dataDirectory/recentLevels.txt")
        f.delete()
        f << levelIds.join(",")
    }
    
    public List loadRecentLevels()
    {
        File f = new File("$m_dataDirectory/recentLevels.txt")
        if (!f.isFile())
            return []
            
        String[] levelIds = f.text.split(",")
        List list = []
        levelIds.each{ String levelId ->
            try
            {
                int id = levelId as int
                Map level = loadLevel(id)
                list << level
            }
            catch (e)
            {
                // skip - level could've been deleted
            }
        }
        return list
    }
    
    protected JSONObject loadResource(String resource)
    {
        try
        {
            def stream = new FileInputStream(resource)
            String s = stream.getText()
            Object json = new JSONParser().parse(s)
            return json
        }
        catch (Exception e)
        {
            throw new DBException("can't parse $resource", e)
        }
    }
    
    protected int getNewLevelID()
    {
        int max = 0
        eachLevelFile{ File file ->
            int index = (file.name - ".json") as int
            if (index > max)
                max = index
        }
        return max + 1
    }
    
    protected int getNewSetID()
    {
        int max = 0
        eachSetFile{ File file ->
            int index = (file.name - ".json") as int
            if (index > max)
                max = index
        }
        return max + 1
    }

    protected void eachLevelFile(Closure cl)
    {
        new File(m_levelDirectory).eachFile{ File file -> cl(file) }
    }
    
    protected void eachLevel(Closure cl)
    {
        new File(m_levelDirectory).eachFile{ File file -> 
            try
            {
                JSONObject level = loadResource(file.absolutePath)
                cl(level)
            }
            catch (Exception e)
            {
                s_log.error("skipping $file", e)
            }
        }
    }
    
    protected void eachSetFile(Closure cl)
    {
        new File(m_setDirectory).eachFile{ File file -> cl(file) }
    }
    
    protected void eachSet(Closure cl)
    {
        new File(m_setDirectory).eachFile{ File file -> 
            try
            {
                JSONObject level = loadResource(file.absolutePath)
                cl(level)
            }
            catch (Exception e)
            {
                s_log.error("skipping $file", e)
            }
        }
    }
    
    protected String prettyPrintLevel(JSONObject p)
    {
        return new PrettyPrinter().toString(p)
    }
}
