package com.enno.dotz.client;

import java.util.ArrayList;
import java.util.List;

import com.ait.tooling.nativetools.client.NArray;
import com.ait.tooling.nativetools.client.NObject;
import com.enno.dotz.client.DotzGridPanel.EndOfLevel;
import com.enno.dotz.client.box.LienzoPopup;
import com.enno.dotz.client.editor.EditLevelDialog;
import com.enno.dotz.client.editor.EditSetDialog;
import com.enno.dotz.client.editor.NewLevelDialog;
import com.enno.dotz.client.editor.OrganizeLevelDialog;
import com.enno.dotz.client.editor.SelectLevelDialog;
import com.enno.dotz.client.editor.SelectSetDialog;
import com.enno.dotz.client.io.ClientRequest;
import com.enno.dotz.client.io.MAsyncCallback;
import com.enno.dotz.client.io.ServiceCallback;
import com.enno.dotz.client.ui.MXLabel;
import com.enno.dotz.client.ui.MXNotification;
import com.enno.dotz.client.util.Matrix;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.SelectionType;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuBar;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.MenuItemIfFunction;
import com.smartgwt.client.widgets.menu.events.ClickHandler;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;

public class MainPanel extends VLayout
{
    private MenuBar m_menubar;
    private VLayout m_gridContainer;
    private PlayLevelPanel m_playPanel;
    private EndOfLevel m_endOfLevel;
    private ModeManager m_modeManager;
    private EditLevelDialog m_editLevelDialog;

    public MainPanel()
    {
        Matrix.test();
        
        m_modeManager = new ModeManager();
        
        m_menubar = new MenuBar();
        m_menubar.setWidth100();
        m_menubar.setHeight("30px");
        
        // Level menu
        Menu levelMenu = new Menu();
        levelMenu.setTitle("Level");
        levelMenu.setWidth("100px");
        
        MenuItem playLevel = m_modeManager.createMenuItem(Mode.NONE, Mode.PLAY, Mode.EDIT);
        playLevel.setTitle("Play Level");
        playLevel.addClickHandler(new ClickHandler()
        {            
            @Override
            public void onClick(MenuItemClickEvent event)
            {
                new SelectLevelDialog(true) {
                    @Override
                    public void selected(int levelId)
                    {
                        playLevel(levelId);
                    }
                };
            }
        });
        
        MenuItem newLevel = m_modeManager.createMenuItem(Mode.NONE, Mode.PLAY, Mode.EDIT);
        newLevel.setTitle("New Level");
        newLevel.addClickHandler(new ClickHandler()
        {            
            @Override
            public void onClick(MenuItemClickEvent event)
            {
                createNewLevel();
            }
        });
        
        MenuItem editLevel = m_modeManager.createMenuItem(Mode.NONE, Mode.PLAY, Mode.EDIT);
        editLevel.setTitle("Edit Level");
        editLevel.addClickHandler(new ClickHandler()
        {            
            @Override
            public void onClick(MenuItemClickEvent event)
            {
                new SelectLevelDialog(true) {
                    @Override
                    public void selected(int levelId)
                    {
                        editLevel(levelId);
                    }
                };
            }
        });
        
        MenuItem copyLevel = m_modeManager.createMenuItem(Mode.NONE, Mode.PLAY, Mode.EDIT);
        copyLevel.setTitle("Copy Level");
        copyLevel.addClickHandler(new ClickHandler()
        {            
            @Override
            public void onClick(MenuItemClickEvent event)
            {
                copyLevel();
            }
        });
        
        MenuItem cancelLevel = m_modeManager.createMenuItem(Mode.TEST, Mode.PLAY);
        cancelLevel.setTitle("Cancel Level");
        cancelLevel.addClickHandler(new ClickHandler()
        {            
            @Override
            public void onClick(MenuItemClickEvent event)
            {
                cancelLevel();
            }
        });

        MenuItem retryLevel = m_modeManager.createMenuItem(Mode.TEST, Mode.PLAY);
        retryLevel.setTitle("Retry Level");
        retryLevel.addClickHandler(new ClickHandler()
        {            
            @Override
            public void onClick(MenuItemClickEvent event)
            {
                retryLevel();
            }
        });

        MenuItem organizeLevels = new MenuItem();
        organizeLevels.setTitle("Organize Levels");
        organizeLevels.addClickHandler(new ClickHandler()
        {            
            @Override
            public void onClick(MenuItemClickEvent event)
            {
                new OrganizeLevelDialog()
                {
                    @Override
                    public void playLevel(final int levelId)
                    {
                        m_modeManager.askSaveLevel(new Runnable()
                        {                                            
                            @Override
                            public void run()
                            {
                                MainPanel.this.playLevel(levelId);
                            }
                        });
                    }
                    
                    @Override
                    public void editLevel(final int levelId)
                    {
                        m_modeManager.askSaveLevel(new Runnable()
                        {                                            
                            @Override
                            public void run()
                            {
                                MainPanel.this.editLevel(levelId);
                            }
                        });
                    }
                };
            }
        });

        levelMenu.setItems(playLevel, newLevel, editLevel, copyLevel, cancelLevel, retryLevel, organizeLevels);
                
        // Set menu
        Menu setMenu = new Menu();
        setMenu.setTitle("Set");
        setMenu.setWidth("100px");
        
        MenuItem playSet = m_modeManager.createMenuItem(Mode.NONE, Mode.PLAY, Mode.EDIT);
        playSet.setTitle("Play Set");
        playSet.addClickHandler(new ClickHandler()
        {            
            @Override
            public void onClick(MenuItemClickEvent event)
            {
                new SelectSetDialog() {
                    @Override
                    public void selected(int setId)
                    {
                        ClientRequest.loadLevelSet(setId, new ServiceCallback() {
                            @Override
                            public void onSuccess(NObject set)
                            {
                                playSet(set, 0);
                            }            
                        });
                    }
                };
            }
        });
        
        MenuItem newSet = new MenuItem();
        newSet.setTitle("New Set");
        newSet.addClickHandler(new ClickHandler()
        {            
            @Override
            public void onClick(MenuItemClickEvent event)
            {
                new EditSetDialog(true, null)
                {                                                    
                    @Override
                    public void playLevel(final int levelId)
                    {
                        m_modeManager.askSaveLevel(new Runnable()
                        {                                            
                            @Override
                            public void run()
                            {
                                MainPanel.this.playLevel(levelId);
                            }
                        });
                        
                    }
                    @Override
                    public void editLevel(final int levelId)
                    {
                        m_modeManager.askSaveLevel(new Runnable()
                        {                                            
                            @Override
                            public void run()
                            {
                                MainPanel.this.editLevel(levelId);
                            }
                        });
                    }
                    @Override
                    public void playSet(final int setId, final int levelIndex)
                    {
                        m_modeManager.askSaveLevel(new Runnable()
                        {                                            
                            @Override
                            public void run()
                            {
                                MainPanel.this.playSet(setId, levelIndex);
                            }
                        });
                    }
                };
            }
        });
        
        MenuItem editSet = new MenuItem();
        editSet.setTitle("Edit Set");
        editSet.addClickHandler(new ClickHandler()
        {            
            @Override
            public void onClick(MenuItemClickEvent event)
            {
                new SelectSetDialog() {
                    @Override
                    public void selected(int setId)
                    {
                        ClientRequest.loadLevelSet(setId, new ServiceCallback() {
                            @Override
                            public void onSuccess(NObject set)
                            {
                                new EditSetDialog(false, set)
                                {     
                                    @Override
                                    public void playLevel(final int levelId)
                                    {
                                        m_modeManager.askSaveLevel(new Runnable()
                                        {                                            
                                            @Override
                                            public void run()
                                            {
                                                MainPanel.this.playLevel(levelId);
                                            }
                                        });
                                        
                                    }
                                    @Override
                                    public void editLevel(final int levelId)
                                    {
                                        m_modeManager.askSaveLevel(new Runnable()
                                        {                                            
                                            @Override
                                            public void run()
                                            {
                                                MainPanel.this.editLevel(levelId);
                                            }
                                        });
                                    }
                                    @Override
                                    public void playSet(final int setId, final int levelIndex)
                                    {
                                        m_modeManager.askSaveLevel(new Runnable()
                                        {                                            
                                            @Override
                                            public void run()
                                            {
                                                MainPanel.this.playSet(setId, levelIndex);
                                            }
                                        });
                                    }
                                };
                            }            
                        });
                    }
                };
            }
        });

        setMenu.setItems(playSet, newSet, editSet);
        
        // Settings menu
        Menu settingsMenu = new Menu();
        settingsMenu.setTitle("Settings");
        settingsMenu.setWidth("100px");
        
        MenuItem changeUser = new MenuItem();
        changeUser.setTitle("Change User");
        changeUser.addClickHandler(new ClickHandler()
        {            
            @Override
            public void onClick(MenuItemClickEvent event)
            {
                new ChangeUserDialog();
            }
        });
        settingsMenu.setItems(changeUser);
        
        m_menubar.setMenus(levelMenu, setMenu, settingsMenu);
        
        HLayout top = new HLayout();
        top.addMember(m_menubar);
        
        final MXLabel modeLabel = new MXLabel();
        modeLabel.setAlign(Alignment.CENTER);
        //top.addMember(modeLabel);
        m_modeManager.add(new ModeListener()
        {
            @Override
            public void setMode(Mode mode)
            {
                modeLabel.setContents(mode.toString());
            }
        });
        
        ToolStrip toolStrip = new ToolStrip();
        toolStrip.setAlign(Alignment.RIGHT);

        //toolStrip.addMember(modeLabel);
        
        final ToolStripButton pause = m_modeManager.createToolStripButton(Mode.TEST, Mode.PLAY);
        pause.setIcon("pause.gif");  
        pause.setPrompt("Pause Level");
        pause.setActionType(SelectionType.BUTTON);  
        pause.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                pause.setIcon("play.gif");
                pauseLevel(new Runnable() {
                    @Override
                    public void run()
                    {
                        pause.setIcon("pause.gif");
                    }
                });
            }
        });
        toolStrip.addButton(pause); 
        
        ToolStripButton cancel = m_modeManager.createToolStripButton(Mode.TEST, Mode.PLAY);
        cancel.setIcon("cancel.png");  
        cancel.setPrompt("Cancel Level");
        cancel.setActionType(SelectionType.BUTTON);  
        cancel.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                cancelLevel();
            }
        });
        toolStrip.addButton(cancel); 
        
        ToolStripButton retry = m_modeManager.createToolStripButton(Mode.TEST, Mode.PLAY);
        retry.setIcon("history.png");
        retry.setPrompt("Retry Level");
        retry.setActionType(SelectionType.BUTTON);  
        retry.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                retryLevel();
            }
        });
        toolStrip.addButton(retry);
        
        ToolStripButton skip = m_modeManager.createToolStripButton(Mode.PLAY);
        skip.setIcon("skip.gif");
        skip.setPrompt("Skip Level");
        skip.setActionType(SelectionType.BUTTON);  
        skip.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                skipLevel();
            }
        });
        toolStrip.addButton(skip);  
        
        HLayout stripContainer = new HLayout();
        stripContainer.setWidth("150px");
        stripContainer.setAlign(Alignment.RIGHT);
        stripContainer.addMember(toolStrip);
        
        top.addMember(stripContainer);
       
        addMember(top);
        
        m_gridContainer = new VLayout();
        addMember(m_gridContainer);
    }
    
    public void init()
    {
        //playLevel(0);
        
        Config level = new Config();
        level.numColumns = 8;
        level.numRows = 8;
        level.creator = UserSettings.INSTANCE.userName;
        
        showEditLevelDialog(true, level);
    }
    
    protected void cancelLevel()
    {
        if (m_endOfLevel != null)
            m_endOfLevel.cancel();
    }
    
    protected void retryLevel()
    {
        if (m_endOfLevel != null)
            m_endOfLevel.retry();
//        if (m_playPanel != null)
//            m_playPanel.testDialog();
    }
    
    protected void skipLevel()
    {
        if (m_endOfLevel != null)
            m_endOfLevel.skip();
    }

    protected void playSet(int setId, final int levelIndex)
    {
        ClientRequest.loadLevelSet(setId, new ServiceCallback() {
            @Override
            public void onSuccess(NObject set)
            {
                playSet(set, levelIndex);
            }            
        });
    }

    protected void playSet(NObject set, int levelIndex)
    {
        new SetPlayer(set, levelIndex).playNextLevel();
    }
    
    protected void playLevel(final int levelId)
    {
        playLevel(levelId, new EndOfLevel() {
            @Override
            public void goalReached(int time, int score, int moves, int levelId)
            {
                saveScore(time, score, moves, levelId, new Runnable() {
                    @Override
                    public void run()
                    {
                        say("Yay! You made it!", killCallback());
                    }
                });
            }

            @Override
            public void failed(String reason)
            {
                ask(reason + "<br>Try again?", new BooleanCallback() {
                    @Override
                    public void execute(Boolean ok)
                    {
                        killLevel();
                        if (ok)
                        {
                            playLevel(levelId); //TODO don't reload level
                        }
                    }
                });
            }

            @Override
            public void cancel()
            {
                ask("Cancel Level", "Are you sure?", new BooleanCallback() 
                {
                    @Override
                    public void execute(Boolean ok)
                    {
                        if (ok)
                        {
                            killLevel();
                        }
                    }
                });
            }
            
            @Override
            public void retry()
            {
                ask("Retry Level", "Are you sure?", new BooleanCallback() {
                    @Override
                    public void execute(Boolean ok)
                    {
                        if (ok)
                        {
                            killLevel();
                            playLevel(levelId); //TODO don't reload level
                        }
                    }
                });
            }
            
            @Override
            public void skip()
            {                
            }
        });
    }
    
    protected void playLevel(int levelId, final EndOfLevel endOfLevel)
    {
        ClientRequest.loadLevel(levelId, new MAsyncCallback<Config>() {
            @Override
            public void onSuccess(Config level)
            {
                playLevel(level, Mode.PLAY, endOfLevel);
            }            
        });
    }
    
    protected void playLevel(Config level, Mode mode, EndOfLevel endOfLevel)
    {
        Context ctx = new Context();
        ctx.cfg = level;
        
        killLevel();
        
        m_modeManager.setMode(mode);
        
        m_endOfLevel = endOfLevel;
        
        m_playPanel = new PlayLevelPanel(ctx, endOfLevel);
        m_gridContainer.addMember(m_playPanel);
        
        m_playPanel.play();
    }
    
    public static class GridContainer extends VLayout
    {
        public GridContainer()
        {
            setAlign(Alignment.CENTER);
            setDefaultLayoutAlign(Alignment.CENTER);
            setBackgroundColor("white");
        }
        
        public void addMember(Widget child, Config level)
        {
            addMember(child);
            
            if (level.numColumns < 8)
            {
                setWidth(8 * level.size);
            }
            else
            {
                setWidth(level.numColumns * level.size);
            }
            
            if (level.numRows < 8)
            {
                setHeight(8 * level.size);
            }
            else
            {
                setWidth(level.numRows * level.size);
            }
        }        
    }
    
    protected void killLevel()
    {
        if (m_playPanel != null)
        {
            m_playPanel.kill();
            m_gridContainer.removeMember(m_playPanel);
            m_playPanel = null;
        }
        m_modeManager.setMode(Mode.NONE);
    }
    
    protected void saveScore(int time, int score, int moves, int levelId, final Runnable whenDone)
    {
        if (levelId == Config.UNDEFINED_ID)
        {
            whenDone.run();
            return;
        }
        
        ClientRequest.saveScore(time, score, moves, levelId, UserSettings.INSTANCE.userName, new ServiceCallback() {
            @Override
            public void onSuccess(NObject result)
            {
                whenDone.run();
            }
        });
    }
    
    public class SetPlayer
    {
        private int m_index;
        private NObject m_set;
        
        public SetPlayer(NObject set, int levelIndex)
        {
            m_set = set;
            m_index = levelIndex;
        }
        
        public void playSameLevel()
        {
            m_index--;
            playNextLevel();
        }
        
        protected boolean isLastLevel()
        {
            return m_index == m_set.getAsArray("levels").size();
        }
        
        public void playNextLevel()
        {
            NArray levels = m_set.getAsArray("levels");
            if (levels.size() == 0)
            {
                SC.warn("This level set has no levels!");
                return;
            }
            
            if (m_index < levels.size())
            {
                int levelId;
                if (levels.isInteger(m_index))
                {
                    levelId = levels.getAsInteger(m_index);
                }
                else
                {
                    NObject level = levels.getAsObject(m_index);
                    levelId = level.getAsInteger("id");
                }
                m_index++;
                
                playLevel(levelId, new EndOfLevel() {
                    @Override
                    public void goalReached(int time, int score, int moves, int levelId)
                    {
                        saveScore(time, score, moves, levelId, new Runnable() {
                            @Override
                            public void run()
                            {
                                if (isLastLevel())
                                {
                                    say("You finished the last level of the set.<br>Congratulations!", killCallback());
                                    return;
                                }
                                
                                ask("You finished the level. Do you want to continue?", new BooleanCallback() 
                                {
                                    @Override
                                    public void execute(Boolean ok)
                                    {
                                        if (ok)
                                        {
                                            playNextLevel();
                                        }
                                        else
                                        {
                                            killLevel();
                                        }
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void failed(String reason)
                    {
                        ask(reason + "<br>Try same level again?", new BooleanCallback() 
                        {
                            @Override
                            public void execute(Boolean ok)
                            {
                                if (ok)
                                {
                                    playSameLevel();
                                }
                                else
                                {
                                    killLevel();
                                    //goto start screen
                                }
                            }
                        });
                    }

                    @Override
                    public void cancel()
                    {
                        ask("Cancel Level", "Are you sure?", new BooleanCallback() 
                        {
                            @Override
                            public void execute(Boolean ok)
                            {
                                if (ok)
                                {
                                    killLevel();
                                    //goto start screen
                                }
                            }
                        });
                    }
                    
                    @Override
                    public void retry()
                    {
                        ask("Retry Level", "Are you sure?", new BooleanCallback() 
                        {
                            @Override
                            public void execute(Boolean ok)
                            {
                                if (ok)
                                {
                                    playSameLevel();
                                }
                            }
                        });
                    }
                    
                    @Override
                    public void skip()
                    {
                        if (isLastLevel())
                        {
                            say("This is the last level.", null);
                            return;
                        }
                        
                        ask("Skip Level", "Are you sure?", new BooleanCallback() 
                        {
                            @Override
                            public void execute(Boolean ok)
                            {
                                if (ok)
                                {
                                    playNextLevel();
                                }
                            }
                        });
                    }
                });
            }
            else
            {
                say("Yay! You finished all the levels!", killCallback());
            }
        }
    }
    
    protected BooleanCallback killCallback()
    {
        return new BooleanCallback() {
            @Override
            public void execute(Boolean value)
            {
                killLevel();
            }
        };        
    }
    
    public void pauseLevel(final Runnable whenDone)
    {
        m_playPanel.pause(true);
        say("Paused", new BooleanCallback() {
            @Override
            public void execute(Boolean value)
            {
                m_playPanel.pause(false);
                whenDone.run();
            }
        });
    }
    
    public static class PlayLevelPanel extends VLayout
    {
        private Context ctx;
        private DotzGridPanel m_grid;
        private ScorePanel m_score;
        private StatsPanel m_statsPanel;
        private BoostPanel m_boostPanel;
        
        private boolean m_playing;
        private EndOfLevel m_endOfLevel;
        
        public PlayLevelPanel(Context ctx, EndOfLevel endOfLevel)
        {
            this.ctx = ctx;
            m_endOfLevel = endOfLevel;
            
            m_grid = new DotzGridPanel(ctx, endOfLevel);

            m_score = new ScorePanel(ctx);
            ctx.scorePanel = m_score;
            addMember(m_score);
            
            GridContainer g = new GridContainer();            
            g.addMember(m_grid, ctx.cfg);
            addMember(g);
            
            m_statsPanel = new StatsPanel(ctx);
            ctx.statsPanel = m_statsPanel;
            addMember(m_statsPanel);
            
            m_boostPanel = new BoostPanel(ctx);
            ctx.boostPanel = m_boostPanel;
            addMember(m_boostPanel);
        }
        
        public void cancelLevel()
        {
            if (m_playing)
                m_grid.cancelLevel();
        }
        
        public void pause(boolean paused)
        {
            if (m_playing)
            {
                if (paused)
                    m_grid.pause();
                else
                    m_grid.unpause();
            }
        }

        public void play()
        {
            m_playing = true;
            
            m_grid.init(true);
            
            m_score.setGoals(ctx.cfg.goals);
            m_statsPanel.setGoals(ctx.cfg.goals);
            
            Runnable startTimer = new Runnable() {
                @Override
                public void run()
                {
                    m_statsPanel.startTimer(new Runnable() {
                        public void run()
                        {
                            m_endOfLevel.failed("You're out of time.");
                        }
                    });
            }};
            
            m_grid.play(startTimer);
        }
        
        public void kill()
        {   
            if (m_playing)
            {
                m_grid.kill();
                m_statsPanel.killTimer();                
                m_playing = false;
            }
        }

        public DotzGridPanel getGridPanel()
        {
            return m_grid;
        }
    }

    public void say(String msg, BooleanCallback cb)
    {
        say(null, msg, cb);
    }
    
    public void say(String title, String msg, final BooleanCallback cb)
    {
        if (m_playPanel != null)
        {
            m_modeManager.setShowDialog(true);

            final DotzGridPanel panel = m_playPanel.getGridPanel();
            panel.pause();
            
            LienzoPopup.say(title, panel, new Runnable() {
                @Override
                public void run()
                {
                    m_modeManager.setShowDialog(false);

                    panel.unpause();
                    cb.execute(Boolean.TRUE);
                }
            }, msg);
        }
        else
        {
            int left = 50; // TODO center it
            int top = 250;
            MXNotification.say(title == null ? "Information" : title, msg, 300, 120, left, top, cb);
        }
    }
    
    public void ask(String msg, BooleanCallback cb)
    {
        ask("Question", msg, cb);
    }
    
    public void ask(String title, String msg, final BooleanCallback cb)
    {
        if (m_playPanel != null)
        {
            m_modeManager.setShowDialog(true);
            
            final DotzGridPanel panel = m_playPanel.getGridPanel();
            panel.pause();
            
            LienzoPopup.ask(title, panel, new BooleanCallback() {
                @Override
                public void execute(Boolean value)
                {
                    m_modeManager.setShowDialog(false);
                    panel.unpause();
                    cb.execute(value);
                }
            }, msg);
        }
        else
        {
            int left = 50; // TODO center it
            int top = 250;
            MXNotification.ask(title, msg, 300, 120, left, top, cb);
        }
    }
    
    private void createNewLevel()
    {
        new NewLevelDialog()
        {
            @Override
            public void editNewLevel(Config level)
            {
                showEditLevelDialog(true, level);
            }
        };
    }

    private void editLevel(int levelId)
    {
        ClientRequest.loadLevel(levelId, new MAsyncCallback<Config>() {
            
            @Override
            public void onSuccess(Config level)
            {
                showEditLevelDialog(false, level);
            }            
        });
    }
    
    private void showEditLevelDialog(boolean isNew, Config level)
    {
        killLevel();
        m_modeManager.setMode(Mode.EDIT);
        
        m_editLevelDialog = new EditLevelDialog(isNew, level)
        {
            @Override
            public void exitEditMode()
            {
                m_modeManager.setMode(Mode.NONE);
            }
            
            @Override
            public void createNewLevel()
            {
                MainPanel.this.createNewLevel();
            }
            
            @Override
            public void testLevel(final Config level)
            {
                playLevel(level, Mode.TEST, new EndOfLevel() {
                    @Override
                    public void goalReached(int time, int score, int moves, int levelId)
                    {
                        saveScore(time, score, moves, levelId, new Runnable() {
                            @Override
                            public void run()
                            {
                                say("Yay! You made it!",
                                        new BooleanCallback() {
                                            @Override
                                            public void execute(Boolean value)
                                            {
                                                killLevel();
                                                m_modeManager.setMode(Mode.EDIT);
                                                showEditor();
                                            }
                                });
                            }
                        });
                    }

                    @Override
                    public void failed(String reason)
                    {
                        say(reason, new BooleanCallback() {
                            @Override
                            public void execute(Boolean value)
                            {
                                killLevel();
                                m_modeManager.setMode(Mode.EDIT);
                                showEditor();
                            }
                        });
                    }

                    @Override
                    public void cancel()
                    {
                        killLevel();
                        m_modeManager.setMode(Mode.EDIT);
                        showEditor();
                    }
                    
                    @Override
                    public void retry()
                    {
                        killLevel();
                        testLevel(level);
                    }
                    
                    @Override
                    public void skip()
                    {                        
                    }
                });
            }
        };
    }
    
    private void copyLevel()
    {
        new SelectLevelDialog(true) {
            @Override
            public void selected(int levelId)
            {
                ClientRequest.loadLevel(levelId, new MAsyncCallback<Config>() {
                    @Override
                    public void onSuccess(Config level)
                    {
                        level.id = Config.UNDEFINED_ID;
                        level.name = "";
                        level.creator = "";
                        
                        showEditLevelDialog(false, level);
                    }            
                });
            }
        };
    }

    public enum Mode { NONE, PLAY, TEST, EDIT };
    public interface ModeListener
    {
        void setMode(Mode mode);
    };
    
    public class ModeManager
    {
        private List<ModeListener> m_listeners = new ArrayList<ModeListener>();
        
        private Mode m_mode = Mode.NONE;

        private boolean m_showingDialog;
        
        public void setMode(Mode mode)
        {
            m_mode = mode;
            for (ModeListener a : m_listeners)
            {
                a.setMode(mode);
            }
        }
        
        public void setShowDialog(boolean show)
        {
            m_showingDialog = show;
        }
        
        protected boolean showingDialog()
        {
            return m_showingDialog;
        }
        
        public MenuItem createMenuItem(final Mode... modes)
        {
            final MenuItem item = new MenuItem() {
                @Override
                public HandlerRegistration addClickHandler(final ClickHandler handler)
                {
                    return super.addClickHandler(new ClickHandler()
                    {                        
                        @Override
                        public void onClick(final MenuItemClickEvent event)
                        {
                            if (m_mode == Mode.EDIT)
                            {
                                m_editLevelDialog.askToSave(new Runnable()
                                {                                    
                                    @Override
                                    public void run()
                                    {
                                        handler.onClick(event);
                                    }
                                });
                            }
                            else
                            {
                                handler.onClick(event);
                            }
                        }
                    });
                }
            };
            MenuItemIfFunction function = new MenuItemIfFunction() {
                public boolean execute(Canvas target, Menu menu, MenuItem item) {
         
                    return isMode(modes) && !showingDialog();
                }
            };
            item.setEnableIfCondition(function);
            
            return item;
        }
        
        public void askSaveLevel(final Runnable callback)
        {
            if (m_mode == Mode.EDIT)
            {
                m_editLevelDialog.askToSave(new Runnable()
                {                                    
                    @Override
                    public void run()
                    {
                        callback.run();
                    }
                });
            }
            else
            {
                callback.run();
            }
        }

        public ToolStripButton createToolStripButton(final Mode... modes)
        {
            final ToolStripButton item = new ToolStripButton() {
                @Override
                public HandlerRegistration addClickHandler(final com.smartgwt.client.widgets.events.ClickHandler handler)
                {
                    return super.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event)
                        {
                            if (!showingDialog())
                                handler.onClick(event);
                        }
                    });
                }
            };
            add(new ModeListener() {
                @Override
                public void setMode(Mode mode)
                {
                    Scheduler.get().scheduleDeferred(new ScheduledCommand()
                    {
                        @Override
                        public void execute()
                        {
                            item.setDisabled(!isMode(modes));
                        }
                    });
                }
            });
            return item;
        }

        protected boolean isMode(Mode... modes)
        {
            for (Mode m : modes)
            {
                if (m == m_mode)
                    return true;
            }
            return false;
        }

        public void add(ModeListener listener)
        {
            m_listeners.add(listener);
        }
    }
}
