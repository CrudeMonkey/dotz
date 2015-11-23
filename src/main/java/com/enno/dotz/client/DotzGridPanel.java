package com.enno.dotz.client;

import java.util.logging.Logger;

import com.ait.lienzo.client.core.animation.AnimationCallback;
import com.ait.lienzo.client.core.animation.IAnimation;
import com.ait.lienzo.client.core.animation.IAnimationHandle;
import com.ait.lienzo.client.core.animation.IndefiniteAnimation;
import com.ait.lienzo.client.core.animation.LayerRedrawManager;
import com.ait.lienzo.client.core.shape.FastLayer;
import com.ait.lienzo.client.core.shape.GridLayer;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Line;
import com.ait.lienzo.client.widget.LienzoPanel;
import com.ait.lienzo.shared.core.types.ColorName;
import com.enno.dotz.client.item.Animal.EyeTracker;

public class DotzGridPanel extends LienzoPanel
{
    private static Logger logger = Logger.getLogger(DotzGridPanel.class.getName());
    
    private EndOfLevel m_endOfLevel;

    protected Context ctx;
    protected Config cfg;
    protected GridState m_state;
    
    private Layer m_backgroundLayer;
    private Layer m_iceLayer;
    private Layer m_connectLayer;
    private Layer m_dotLayer;
    private Layer m_doorLayer;
    private Layer m_nukeLayer; // nuke explosions and moving fire

    private ConnectMode m_connectMode;

    private IAnimationHandle m_animation;

    public DotzGridPanel(Context ctx, EndOfLevel endOfLevel)
    {
        super(ctx.cfg.numColumns * ctx.cfg.size, (ctx.cfg.numRows + 1) * ctx.cfg.size);
        
        this.ctx = ctx;
        ctx.gridPanel = this;
        m_endOfLevel = endOfLevel;
    }    

    public void cancelLevel()
    {
        m_endOfLevel.cancel();
    }

    public void init(boolean replaceRandom)
    {
        this.cfg = ctx.cfg;
        ctx.state = ctx.cfg.grid.copy();
        m_state = ctx.state;
        
        setBackgroundColor(ColorName.WHITE);
        
        Line gridLine = new Line();
        gridLine.setStrokeColor(ColorName.DARKGRAY);
        
        add(new GridLayer(cfg.size, gridLine));
        
        m_iceLayer = createLayer();
        ctx.iceLayer = m_iceLayer;
        add(m_iceLayer);

        m_backgroundLayer = createLayer();
        add(m_backgroundLayer);
        ctx.backgroundLayer = m_backgroundLayer;
        
        m_connectLayer = new Layer(); // only this layer responds to events (for performance)
        add(m_connectLayer);
        ctx.connectLayer = m_connectLayer;
        
        m_dotLayer = createLayer();
        add(m_dotLayer);
        ctx.dotLayer = m_dotLayer;
        
        m_doorLayer = createLayer();
        add(m_doorLayer);
        ctx.doorLayer = m_doorLayer; //TODO remove member variables - not used
        
        m_nukeLayer = createLayer();
        ctx.nukeLayer = m_nukeLayer;
        m_nukeLayer.setVisible(false);
        add(m_nukeLayer);
        
        ctx.laserLayer = createLayer();
        add(ctx.laserLayer);
        
        //p("building");
            
        ctx.init();
        m_state.init(ctx, replaceRandom);
        
        ctx.score.initGrid(ctx.state);        
    }
    
    private Layer createLayer()
    {
//        Layer layer = new Layer();
//        layer.setListening(false); // don't listen to events (for performance)
//        return layer;
        return new FastLayer();
    }

    public void play(final Runnable startTimer)
    {
        m_state.endOfLevel = m_endOfLevel;
        
        m_connectMode = ctx.generator.swapMode ? new SwapConnectMode(ctx, m_connectLayer) : new DragConnectMode(ctx, m_connectLayer);        
        ctx.boostPanel.setConnectMode(m_connectMode);
        
        startItemAnimation();

        m_state.doInitialTransitions(new Runnable() {
            public void run()
            {
              //Debug.p("start connect mode");
                m_connectMode.start();  
                startTimer.run();
            }
        });
    }
    
    public void pause()
    {
        m_connectLayer.setListening(false);
        ctx.statsPanel.pauseTimer();
    }
    
    public void unpause()
    {
        m_connectLayer.setListening(true);
        ctx.statsPanel.unpauseTimer();
    }

    public void kill()
    {
        ctx.killed = true;
        m_animation.stop();
    }
    
    protected void startItemAnimation()
    {
        final EyeTracker eyeTracker = new EyeTracker(m_connectLayer);
        eyeTracker.start();
        
        m_animation = new IndefiniteAnimation(new AnimationCallback() {            
            @Override
            public void onFrame(IAnimation animation, IAnimationHandle handle)
            {
                long t = System.currentTimeMillis();
                double cursorX = eyeTracker.getX();
                double cursorY = eyeTracker.getY();
                for (int row = 0, nr = m_state.numRows; row < nr; row++)
                {
                    for (int col = 0, nc = m_state.numColumns; col < nc; col++)
                    {
                        m_state.cell(col, row).animate(t, cursorX, cursorY);                        
                    }
                }
                if (m_state.hasConveyors() || m_state.getCircuitCount() > 0)
                    LayerRedrawManager.get().schedule(ctx.backgroundLayer);
                    
                LayerRedrawManager.get().schedule(m_dotLayer);
                LayerRedrawManager.get().schedule(ctx.laserLayer);
            }
        }).run();
    }
    
    public static interface EndOfLevel
    {
        public void goalReached(int time, int score, int moves, int levelId);
        public void failed(String reason);
        public void cancel();
        public void retry();
        public void skip();
    }
}
