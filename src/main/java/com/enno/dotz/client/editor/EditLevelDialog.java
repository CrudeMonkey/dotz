package com.enno.dotz.client.editor;

import com.ait.tooling.nativetools.client.NObject;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Recorder;
import com.enno.dotz.client.SelectPlaybackDialog;
import com.enno.dotz.client.ShowScoresDialog;
import com.enno.dotz.client.io.ClientRequest;
import com.enno.dotz.client.io.MAsyncCallback;
import com.enno.dotz.client.io.ServiceCallback;
import com.enno.dotz.client.ui.MXButtonsPanel;
import com.enno.dotz.client.ui.MXNotification;
import com.enno.dotz.client.ui.MXWindow;
import com.enno.dotz.client.ui.UTabSet;
import com.enno.dotz.client.util.Debug;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.layout.VLayout;

public abstract class EditLevelDialog extends MXWindow
{
    private UTabSet m_tabs;
    private EditGeneratorTab m_generator;
    private EditLayoutTab m_layout;
    private EditGoalsTab m_goals;

    private MXButtonsPanel m_buttons;
    
    private Config m_level;
    private GeneratorPropertiesPanel m_props;
    private ChangeListener m_changeListener;

    private boolean m_changed = false;
    
    public EditLevelDialog(boolean isNew, Config level)
    {
        setTitle("Level Editor");
        setIsModal(false);
        
        m_level = level;
        
        addItem(createPane(isNew, level));
        
        setWidth(1060);
        setHeight(900);
        
        setTop(30);
        
        setCanDragResize(true);
        setCanDragReposition(true);
        
        show();
    }
    
    private Canvas createPane(boolean isNew, Config level)
    {
        VLayout pane = new VLayout();
        pane.setMargin(10);
        pane.setMembersMargin(10);
        
        m_tabs = new UTabSet();
        pane.addMember(m_tabs);        
        
        m_changeListener = new ChangeListener();
        
        m_layout = new EditLayoutTab(isNew, level, m_changeListener)
        {
            @Override
            protected boolean isLetterMode()
            {
                return m_props.isLetterMode();
            }
            
            @Override
            protected boolean isDominoMode()
            {
                return m_props.isDominoMode();
            }
        };
        
        m_tabs.addTab("Layout", m_layout);  
        
        m_generator = new EditGeneratorTab(isNew, level, m_changeListener);
        m_tabs.addTab("Generator Frequencies", m_generator);                
        
        m_props = new GeneratorPropertiesPanel(level, m_changeListener)
        {
            @Override
            protected void setLetterMode(boolean isLetterMode)
            {
                m_layout.setLetterMode(isLetterMode);
            }
        };
        m_tabs.addTab("Generator Settings", m_props); 
        
        m_goals = new EditGoalsTab(level, m_changeListener);
        m_tabs.addTab("Goals", m_goals);
        
        
        m_buttons = new MXButtonsPanel();
        m_buttons.add("Test", new ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                testLevel(null);
            }
        });
        m_buttons.add("Playback", new ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                playback();
            }
        });
        m_buttons.add("Save", new ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                saveLevel(null);
            }
        });
        m_buttons.add("Scores", new ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                ShowScoresDialog.showScores(m_level.id);
            }
        });
//        m_buttons.add("New Level", new ClickHandler()
//        {            
//            @Override
//            public void onClick(ClickEvent event)
//            {
//                //TODO check for changes
//                closeWindow();
//                createNewLevel();
//            }
//        });
        m_buttons.add("Random", new ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                m_layout.randomLevel();
            }
        });
        m_buttons.add("Equalize Dot Freq", new ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                m_generator.equalizeDotFreq();
            }
        });
        m_buttons.add("Delete Level", new ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                if (m_level.id == Config.UNDEFINED_ID)
                {
                    closeWindow();
                    return;
                }
                
                ClientRequest.deleteLevel(m_level.id, new ServiceCallback() {
                    @Override
                    public void onSuccess(NObject result)
                    {
                        closeWindow();
                    }
                });
            }
        });
        m_buttons.add("Close", new ClickHandler()
        {            
            @Override
            public void onClick(ClickEvent event)
            {
                askToSave(new Runnable() { 
                    @Override
                    public void run() {} // do nothing - just ask 'Do you want to save' if necessary
                });
            }
        });
        pane.addMember(m_buttons);
        
        return pane;
    }
    
    @Override
    public void closeWindow()
    {
        //TODO check if anything changed
        SetControllerDialog.closeDialog();
        
        exitEditMode();
        super.closeWindow();
    }
    
    protected void playback()
    {
        SelectPlaybackDialog.showDialog(new MAsyncCallback<Recorder>()
        {
            @Override
            public void onSuccess(final Recorder recorder)
            {
                testLevel(recorder);
            }
        },
        new MAsyncCallback<Integer>()
        {
            @Override
            public void onSuccess(final Integer levelId)
            {
                SC.warn("Not implemented from here");
            }
        });
    }
    
    protected void showEditor()
    {
        setVisible(true);
    }
    
    protected void testLevel(Recorder recorder)
    {
        m_level.grid = m_layout.getGridState();
        
        if (!validate(false))            
        {
            return;
        }
        
        m_layout.prepareSave(m_level);
        m_generator.prepareSave(m_level);
        m_goals.prepareSave(m_level);
        m_props.prepareSave(m_level);
        
        setVisible(false);
        testLevel(m_level, recorder);
    }
    
    protected boolean validate(boolean save)
    {
        if (!m_layout.validate(save) || !m_generator.validate() || !m_props.validate() || !m_goals.validate(m_generator))            
        {
            return false;
        }
        
        //TODO check if goals are attainable, e.g. is dot/anchor/animal spawned
        
        return true;
    }
    
    protected void saveLevel(final Runnable afterSave)
    {
        if (!validate(true))            
        {
            return;
        }
        
        m_level.grid = m_layout.getGridState();
        
        m_layout.prepareSave(m_level);
        m_generator.prepareSave(m_level);
        m_goals.prepareSave(m_level);
        m_props.prepareSave(m_level);
        
        ClientRequest.saveLevel(m_level, new ServiceCallback() 
        {
            @Override
            public void onSuccess(NObject result)
            {
                Debug.p("result: " + result);
                int id = result.getAsInteger("id");
                String lastModified = result.getAsString("lastModified");
                m_level.id = id;
                m_level.lastModified = Long.parseLong(lastModified);

                m_layout.setLevelID(id);
                
                m_changed = false;
                setTitle("Level Editor");
                
                if (afterSave != null)
                    afterSave.run();
            }
        });
    }

    public abstract void exitEditMode();

    public abstract void createNewLevel();

    public abstract void testLevel(Config level, Recorder recorder);

    public void askToSave(final Runnable action)
    {
        if (!m_changed)
        {
            closeWindow();
            action.run();
            return;
        }
        
        showEditor();
        MXNotification.askYesNoCancel("Save Level?", "Do you want to save the current level?", 300, 100, 100, 100, new BooleanCallback()
        {            
            @Override
            public void execute(Boolean value)
            {
                if (value == null)
                    return;
                
                if (value)
                {
                    saveLevel(new Runnable() {

                        @Override
                        public void run()
                        {
                            closeWindow();
                            action.run();
                        }
                    });
                }
                else
                {
                    closeWindow();
                    action.run();
                }
            }
        });
    }
    
    public class ChangeListener implements ChangedHandler
    {
        public void changed()
        {
            if (!m_changed)
                setTitle("* Level Editor");
            
            m_changed = true;
            Debug.p("changed");
        }

        @Override
        public void onChanged(ChangedEvent event)
        {
            changed();
        }
    }
}
