package com.enno.dotz.client;

import com.ait.lienzo.client.core.animation.AnimationCallback;
import com.ait.lienzo.client.core.animation.IAnimation;
import com.ait.lienzo.client.core.animation.IAnimationHandle;
import com.ait.lienzo.client.core.animation.IndefiniteAnimation;
import com.ait.lienzo.client.core.animation.LayerRedrawManager;
import com.ait.lienzo.client.core.shape.FastLayer;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Picture;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.client.widget.LienzoPanel;
import com.ait.lienzo.shared.core.types.ColorName;
import com.enno.dotz.client.util.Font;

public class StatsPanel extends LienzoPanel
{
    private static Picture[] s_modeIcons = initModeIcons();
    
    private Context ctx;

    private Text m_score;
    private Text m_time;
    private Text m_moves;
    private Layer m_layer;
    
    private long m_startTime;
    private int m_lastTime;

    private IAnimationHandle m_animation;
    private int m_goalTime;
    private Runnable m_outOfTimeCallback;

    private boolean m_paused;

    private boolean m_bombWentOff;

    public StatsPanel(Context ctx)
    {
        super(ctx.gridWidth, 50);
        
        this.ctx = ctx;
        
        setBackgroundColor(ColorName.ALICEBLUE);
        
        m_layer = new FastLayer();
        add(m_layer);
        
        m_moves = new Text("Moves: 0");
        m_moves.setFillColor(ColorName.BLACK);
        m_moves.setFontSize(10);
        m_moves.setFontFamily(Font.SCORE);
//        m_moves.setFontStyle("bold");
        m_moves.setX(40);
        m_moves.setY(30);
        m_layer.add(m_moves);
        
        m_score = new Text("Score: 0");
        m_score.setFillColor(ColorName.BLACK);
        m_score.setFontSize(10);
        m_score.setFontFamily(Font.SCORE);
//        m_score.setFontStyle("bold");
        m_score.setX(170);
        m_score.setY(30);
        m_layer.add(m_score);
        
        m_time = new Text("Time: 0:00");
        m_time.setFillColor(ColorName.BLACK);
        m_time.setFontSize(10);
        m_time.setFontFamily(Font.SCORE);
//        m_time.setFontStyle("bold");
        m_time.setX(300);
        m_time.setY(30);
        m_layer.add(m_time);
        
        draw();
    }

    public void copyState(UndoState undoState)
    {
        undoState.elapsedTime = elapsedTime();
        undoState.bombWentOff = m_bombWentOff;
    }
    
    public void restoreState(UndoState undoState)
    {
        m_bombWentOff = undoState.bombWentOff;
        m_startTime = System.currentTimeMillis() - undoState.elapsedTime * 1000;
        m_lastTime = 0; // force redraw
        
        updateMoves();
        updateTime();
        updateScore();
        draw();
    }

    private static Picture[] initModeIcons()
    {
        return new Picture[] {
                new Picture("images/blue_dot.png"),
                new Picture("images/domino.png"),
                new Picture("images/blue_dot_T.png"),
                new Picture("images/refresh.png"),
                new Picture("images/diagonal_mode.png"),
                new Picture("images/click_mode.gif"),
        };
    }

    public void setGoals(Goal goal)
    {
        update();
        
        m_goalTime = ctx.cfg.goals.getTime();
        m_lastTime = -1;
        m_startTime = System.currentTimeMillis();
        updateTime();
        
        Picture pic = s_modeIcons[ctx.generator.getMode()];
        pic.setX(10);
        pic.setY(17);
        m_layer.add(pic);
    }

    public void update()
    {
        updateMoves();
        updateScore();
        
        LayerRedrawManager.get().schedule(m_layer);
    }
    
    public boolean isOutOfMoves()
    {
        int max = ctx.cfg.goals.getMaxMoves();
        if (max == 0)
            return false;
        
        return ctx.score.getMoves() >= max;
    }
    
    public boolean isOutOfTime()
    {
        if (m_goalTime == 0)
            return false; // no time limit
        
        return m_lastTime >= m_goalTime;
    }
    
    protected void updateScore()
    {
        m_score.setText("Score: " + ctx.score.getScore());
    }    
    
    protected void updateMoves()
    {
        int max = ctx.cfg.goals.getMaxMoves();
        int n = max == 0 ? ctx.score.getMoves() : max - ctx.score.getMoves();
        m_moves.setText("Moves: " + n);
//        BoundingBox box = m_moves.getBoundingBox();
//        m_moves.setX((47 - box.getWidth())/ 2);
    }

    public void startTimer(Runnable outOfTimeCallback)
    {
        m_outOfTimeCallback = outOfTimeCallback;
        m_startTime = System.currentTimeMillis();
        m_lastTime = 0;
        
        m_animation = new IndefiniteAnimation(new AnimationCallback() {            
            @Override
            public void onFrame(IAnimation animation, IAnimationHandle handle)
            {
                updateTime();                
            }
        }).run();
    }
    
    public int elapsedTime()
    {
        if (m_paused)
            return m_lastTime;
        
        return (int) (System.currentTimeMillis() - m_startTime) / 1000;
    }
    
    protected void updateTime()
    {
        int time = elapsedTime();
        if (time == m_lastTime)
            return;
        
        m_lastTime = time;
        
        if (m_goalTime != 0)
        {
            time = m_goalTime - time;
        }
        
        m_time.setText(formatTime("Time: ", time));
        
        if (m_goalTime != 0 && time <= 0)
        {
            killTimer();
            m_outOfTimeCallback.run();
            return;
        }
        
        LayerRedrawManager.get().schedule(m_layer);
    }

    protected String formatTime(String prefix, int time)
    {
        int hour = time / 3600;
        int min = (time - (hour * 3600)) / 60;
        int sec = time % 60;
        
        StringBuilder b = new StringBuilder(prefix);
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
    
    public void killTimer()
    {
        if (m_animation != null)
        {
            m_animation.stop();
            m_animation = null;
        }
    }

    public void pauseTimer()
    {
        m_paused = true;
    }

    public void unpauseTimer()
    {
        m_paused = false;
        m_startTime = System.currentTimeMillis() - m_lastTime * 1000; 
    }

    public boolean isBombWentOff()
    {
        return m_bombWentOff;
    }
    
    public void setBombWentOff()
    {
        m_bombWentOff = true;
    }
}
