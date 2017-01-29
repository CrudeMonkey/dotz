package com.enno.dotz.client;

import java.util.ArrayList;
import java.util.List;

import com.ait.lienzo.client.core.animation.AnimationCallback;
import com.ait.lienzo.client.core.animation.IAnimation;
import com.ait.lienzo.client.core.animation.IAnimationHandle;
import com.ait.lienzo.client.core.animation.IndefiniteAnimation;
import com.ait.lienzo.client.core.shape.Arrow;
import com.ait.lienzo.client.core.shape.FastLayer;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.shared.core.types.ArrowType;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.ait.tooling.nativetools.client.NObject;
import com.enno.dotz.client.anim.Pt;
import com.enno.dotz.client.anim.Pt.PtList;
import com.enno.dotz.client.editor.LevelParser;
import com.enno.dotz.client.item.Item;

public class PlaybackLayer extends FastLayer
{
    private IndefiniteAnimation m_animation;

    List<IPrimitive<?>> m_list = new ArrayList<IPrimitive<?>>();

    private long m_startTime;
    private int m_blinkTime;
    
    private Context m_ctx;
    
    public PlaybackLayer(Context ctx)
    {
        m_ctx = ctx;
    }
    
    public void setAction(NObject row)
    {
        removeAll();
        m_list.clear();
        
        m_blinkTime = 300;
        
        String action = Recorder.getAction(row);
        PtList list = Recorder.parsePtList(row);
        
        if (list.size() > 1)
        {
            for (int i = 0; i < list.size() - 1; i++)
            {
                Pt a = list.get(i);
                Pt b = list.get(i + 1);
                
//                Line line = new Line(x(a.col), y(a.row), x(b.col), y(b.row));
//                line.setStrokeColor(ColorName.BLACK);
//                line.setDashArray(2);
                
                Arrow arrow = new Arrow(new Point2D(x(a.col), y(a.row)), 
                        new Point2D(x(b.col), y(b.row)),
                        3, 15, 30, 60, ArrowType.AT_END);
                arrow.setFillColor(ColorName.BLACK);
                
                add(arrow);
                m_list.add(arrow);
            }
        }
        else if (action.equals("boost"))
        {
            m_blinkTime = 500;
                    
            Item item = LevelParser.parseItem(row.getAsObject("item"));
            
            double dx, dy;
            double sz = m_ctx.state.size();
            
            Group g = new Group();
            if (list.size() == 1)
            {
                Pt a = list.get(0);

                Rectangle r = new Rectangle(sz, sz);
                r.setX(x(a.col) - sz / 2);
                r.setY(y(a.row) - sz / 2);            
                r.setStrokeWidth(3);
                r.setDashArray(3);
                r.setStrokeColor(ColorName.BLACK);
                g.add(r);
                
                dx = a.col < 2 ? m_ctx.state.x(a.col + 1) : m_ctx.state.x(a.col - 2);
                dy = a.row < 2 ? m_ctx.state.y(a.row + 1) : m_ctx.state.x(a.row - 2);
                dx -= sz / 2;
                dy -= sz / 2;
            }
            else
            {
                dx = m_ctx.state.x(m_ctx.state.numColumns / 2 - 2);
                dy = m_ctx.state.x(m_ctx.state.numRows / 2 - 2);
            }
            
            Rectangle frame = new Rectangle(sz * 2, sz * 2);
            frame.setFillColor(ColorName.WHITE);
            frame.setStrokeColor(ColorName.BLACK);
            frame.setX(dx);
            frame.setY(dy);
            g.add(frame);
            
            Text text = new Text("BOOST");
            text.setFillColor(ColorName.BLACK);
            text.setFontSize(12);
            text.setFontStyle("bold");
            text.setTextBaseLine(TextBaseLine.MIDDLE);
            text.setTextAlign(TextAlign.CENTER);
            text.setX(dx + sz);
            text.setY(dy + sz * 0.4);
            g.add(text);
            
            IPrimitive<?> shape = item.createShape(sz);
            shape.setX(dx + sz);
            shape.setY(dy + sz * 1);
            g.add(shape);
            
            add(g);
            m_list.add(g);
        }
        else    // click or special with one point
        {
            Pt a = list.get(0);

            double sz = m_ctx.state.size();

            Rectangle r = new Rectangle(sz, sz);
            r.setX(x(a.col) - sz / 2);
            r.setY(y(a.row) - sz / 2);            
            r.setStrokeWidth(3);
            r.setDashArray(3);
            r.setStrokeColor(ColorName.BLACK);
            
            add(r);
            m_list.add(r);
        }
    }
    
    private double x(int col)
    {
        return m_ctx.state.x(col);
    }

    private double y(int row)
    {
        return m_ctx.state.y(row);
    }

    public void start()
    {
        setVisible(true);
        
        m_startTime = System.currentTimeMillis(); 
        
        m_animation = new IndefiniteAnimation(new AnimationCallback() {            
            @Override
            public void onFrame(IAnimation animation, IAnimationHandle handle)
            {
                int n = m_list.size();
                long t = (System.currentTimeMillis() - m_startTime) / m_blinkTime;
                t = t % (n + 1);
                
                for (int i = 0; i < n; i++)
                {
                    m_list.get(i).setVisible(i < t);
                }
                redraw();
            }
        });
        m_animation.run();
    }
    
    public void stop()
    {
        setVisible(false);
        
        if (m_animation != null)
        {
            m_animation.stop();
            m_animation = null;
        }
    }
}
