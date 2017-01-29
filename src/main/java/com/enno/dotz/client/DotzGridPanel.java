package com.enno.dotz.client;

import java.util.logging.Logger;

import com.ait.lienzo.client.core.animation.AnimationCallback;
import com.ait.lienzo.client.core.animation.IAnimation;
import com.ait.lienzo.client.core.animation.IAnimationHandle;
import com.ait.lienzo.client.core.animation.IndefiniteAnimation;
import com.ait.lienzo.client.core.animation.LayerRedrawManager;
import com.ait.lienzo.client.core.image.ImageShapeLoadedHandler;
import com.ait.lienzo.client.core.shape.FastLayer;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Line;
import com.ait.lienzo.client.core.shape.Picture;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.widget.LienzoPanel;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.tooling.nativetools.client.NObject;
import com.enno.dotz.client.Cell.Hole;
import com.enno.dotz.client.Cell.Rock;
import com.enno.dotz.client.item.Animal.EyeTracker;

public class DotzGridPanel extends LienzoPanel
{
    private static Logger logger = Logger.getLogger(DotzGridPanel.class.getName());
    
    private EndOfLevel m_endOfLevel;

    protected Context ctx;
    protected Config cfg;
    protected GridState m_state;
    
    protected FloorLayer m_floorLayer;
    private FastLayer m_iceLayer;
    private FastLayer m_backgroundLayer;
    private Layer m_connectLayer;
    private FastLayer m_borderLayer;
    private FastLayer m_dotLayer;
    private FastLayer m_doorLayer;
    private FastLayer m_nukeLayer; // nuke explosions and moving fire

    private ConnectMode m_connectMode;

    private IAnimationHandle m_animation;

    private PlaybackLayer m_playbackLayer;

    public DotzGridPanel(Context ctx, EndOfLevel endOfLevel)
    {
        super(ctx.gridWidth, ctx.gridHeight);
        
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
        ctx.init();
        
        this.cfg = ctx.cfg;
        m_state = ctx.state;
        

        //setBackgroundColor(ColorName.WHITE);
        
//        Line gridLine = new Line();
//        gridLine.setStrokeColor(ColorName.DARKGRAY);
        
        m_floorLayer = new FloorLayer(ctx);
        add(m_floorLayer);
        
        //add(new GridLayer(cfg.size, gridLine));
        
        m_iceLayer = createLayer();
        ctx.iceLayer = m_iceLayer;
        add(m_iceLayer);

        m_backgroundLayer = createLayer();
        add(m_backgroundLayer);
        ctx.backgroundLayer = m_backgroundLayer;
        
        m_connectLayer = new Layer(); // only this layer responds to events (for performance)
        add(m_connectLayer);
        ctx.connectLayer = m_connectLayer;
        
        m_borderLayer = createLayer();
        add(m_borderLayer);
        
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
            
        m_state.init(ctx, replaceRandom);
        
        m_floorLayer.init(ctx);
        setBorders();
        
        m_playbackLayer = new PlaybackLayer(ctx);
        m_playbackLayer.setVisible(false);
        ctx.playbackLayer = m_playbackLayer;
        add(m_playbackLayer);
        
        ctx.score.initGrid(ctx.state);        
    }
    
    public void setBorders()
    {
        new BorderFinder(m_state).find(m_borderLayer);
    }

    private FastLayer createLayer()
    {
//        Layer layer = new Layer();
//        layer.setListening(false); // don't listen to events (for performance)
//        return layer;
        return new FastLayer();
    }

    public void play(final Runnable startTimer)
    {
        m_state.endOfLevel = m_endOfLevel;
        
        m_connectMode = ctx.generator.swapMode ? new SwapConnectMode(ctx, m_connectLayer) : 
            (ctx.generator.clickMode ? new ClickConnectMode(ctx, m_connectLayer) : new DragConnectMode(ctx, m_connectLayer));        
        ctx.boostPanel.setConnectMode(m_connectMode);
        
        SoundManager.startLoop();
        
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
    
    public void playback(NObject row)
    {
        m_connectMode.playback(row);
    }
    
    public void pause()
    {
        SoundManager.pauseLoop();
        
        m_connectLayer.setListening(false);
        ctx.statsPanel.pauseTimer();
    }
    
    public void unpause()
    {
        SoundManager.startLoop();
        
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
                LayerRedrawManager.get().schedule(ctx.doorLayer); // TODO optimize - only needed if we have blinking Doors/Cages
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
        public void editLevel();
    }
    
    public static class FloorLayer extends FastLayer
    {
//        private static final String ROCK_IMAGE = "images/Mountain Rock-053.jpg";
        private static final String ROCK_IMAGE = "images/Mountain Rock-057.jpg";
        private static final String WOOD_IMAGE = "images/Background-Image-8HE.jpg";
//        private static final String STAR_IMAGE = "images/milky-way-923801_1280.jpg";
        private static final String STAR_IMAGE = "images/starry-sky-1654074_1280.jpg";
        
        private boolean m_gridLines;

        public FloorLayer(Context ctx)
        {
            m_gridLines = true;
        }
        
        public void init(Context ctx)
        {
            if (ctx.isEditing)
                initEditing(ctx);
            else
                initNotEditing(ctx);
        }
        
        public void initNotEditing(Context ctx)
        {
            removeAll();
            
            Picture p = new Picture(STAR_IMAGE, false);
            add(p);
            p.getImageProxy().setImageShapeLoadedHandler(new ImageShapeLoadedHandler<Picture>()
            {                
                @Override
                public void onImageShapeLoaded(Picture image)
                {
                    LayerRedrawManager.get().schedule(FloorLayer.this);
                }
            });
            
            GridState state = ctx.state;
            int sz = (int) state.size();
            
            int rockScale = 5;
            
            for (int col = 0; col < state.numColumns; col++)
            {
                for (int row = 0; row < state.numRows; row++)
                {
                    double x = state.x(col) - sz/2;
                    double y = state.y(row) - sz/2;
                    
                    Cell c = state.cell(col,  row);
                    if (c instanceof Hole)
                    {
                        
                    }
                    else if (c instanceof Rock)
                    {
                        p = new Picture(ROCK_IMAGE, (int) x * rockScale, (int) y * rockScale, rockScale * sz, rockScale * sz, sz, sz, false);
                        p.setX(x);
                        p.setY(y);
                        add(p);
                        
                        p.getImageProxy().setImageShapeLoadedHandler(new ImageShapeLoadedHandler<Picture>()
                        {                
                            @Override
                            public void onImageShapeLoaded(Picture image)
                            {
                                LayerRedrawManager.get().schedule(FloorLayer.this);
                            }
                        });
                    }
                    else
                    {
                        Rectangle r = new Rectangle(sz, sz);
                        r.setX(x);
                        r.setY(y);
                        
                        r.setFillColor(ColorName.WHITE);
                        add(r);
                        
                        Line line = new Line(x, y, x + sz, y);
                        line.setStrokeColor(ColorName.DARKGRAY);
                        add(line);
                        
                        line = new Line(x, y, x, y + sz);
                        line.setStrokeColor(ColorName.DARKGRAY);
                        add(line);
                    }
                }
            }
        }

        public void initEditing(Context ctx)
        {
            removeAll();
            
            GridState state = ctx.state;
            int sz = (int) state.size();
                        
            for (int col = 0; col < state.numColumns; col++)
            {
                for (int row = 0; row < state.numRows; row++)
                {
                    double x = state.x(col) - sz/2;
                    double y = state.y(row) - sz/2;
                    
                    Rectangle r = new Rectangle(sz, sz);
                    r.setX(x);
                    r.setY(y);
                    
                    r.setFillColor(ColorName.WHITE);
                    add(r);
                    
                    Line line = new Line(x, y, x + sz, y);
                    line.setStrokeColor(ColorName.DARKGRAY);
                    add(line);
                    
                    line = new Line(x, y, x, y + sz);
                    line.setStrokeColor(ColorName.DARKGRAY);
                    add(line);
                }
            }
        }
    }
}

