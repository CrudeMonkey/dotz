package com.enno.dotz.client.item;

import java.util.ArrayList;
import java.util.List;

import com.ait.lienzo.client.core.shape.Ellipse;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Line;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.shared.core.types.Color;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.util.Font;

public class Coin extends Item
{
    public static final String DEFAULT_COIN_FREQ = "10,3,1";                // 1,5,10
    public static final int[]  COIN_DENOMINATION = { 1, 5, 10 };
    public static final int    NUM_COINS         = COIN_DENOMINATION.length;
    
    private int m_amount;

    public Coin()
    {
        this(1, false);   
    }
    
    public Coin(int amount, boolean stuck)
    {
        m_amount = amount;
        m_stuck = stuck;
    }

    @Override
    public String getType()
    {
        return "coin";
    }
    
    public int getAmount()
    {
        return m_amount;
    }

    public void setAmount(int amount)
    {
        m_amount = amount;
    }
    
    static String[][] COLORS = {
            {// back oval  main fill  main edge  inner1     inner2     line       text
                "#846C0E", "#E9D98C", "#715A0E", "#E1C837", "#C4AE36", "#C4AE36", "#000000"        // $1
            },
            {
                "#5A5A5A", "#E3E3E3", "#676767", "#CBCBCB", "#929292", "#929292", "#000000"        // $5
            },
            {
                "#846C0E", "#FEEA4D", "#715A0E", "#E1C837", "#C4AE36", "#C4AE36", "#553D00"        // $10
            },
    };
    
    @Override
    public IPrimitive<?> createShape(double sz)
    {
        double r = sz * 0.6;
        double r2 = r * 0.95;
        
        Group g = new Group();
        
        if (isStuck())
            g.add(createStuckShape(sz));
        
        double dx = r * 0.1;
        
        int index = m_amount == 1 ? 0 : (m_amount == 5 ? 1 : 2);
        
        Ellipse c = new Ellipse(r2, r);
        c.setFillColor(Color.fromColorString(COLORS[index][0])); //#8D7728"));
        //c.setStrokeColor(Color.fromColorString("#715A0E")); //#E1C837"));
        c.setX(dx);
        //c.setY(-dx);
        g.add(c);

        c = new Ellipse(r2, r);
        c.setFillColor(Color.fromColorString(COLORS[index][1]));
        c.setStrokeColor(Color.fromColorString(COLORS[index][2])); //#E1C837"));        
        g.add(c);
        
        c = new Ellipse(r2-2, r-2);
        c.setStrokeColor(Color.fromColorString(COLORS[index][3]));
        c.setStrokeWidth(2);
        g.add(c);

        c = new Ellipse(r2-3, r-3);
        c.setStrokeColor(Color.fromColorString(COLORS[index][4]));
        c.setStrokeWidth(1);
        g.add(c);

        int n = 14;
        double da = Math.PI / n;
        double a = -Math.PI / 2;
        for (int i = 0; i < n; i++, a += da)
        {
            double x = r2 * 0.5 * Math.cos(a);
            double y = r * 0.5 * Math.sin(a);
            
            Line line = new Line(x, y, x + dx, y);
            line.setStrokeColor(Color.fromColorString(COLORS[index][5]));
            g.add(line);
        }

        Text text = new Text("$" + m_amount);
        
        if (sz < Config.DEFAULT_CELL_SIZE)
            text.setFontSize(10);
        else
            text.setFontSize(12);
        
        text.setFontFamily(Font.COIN);
        //text.setFontStyle("bold");
        text.setTextAlign(TextAlign.CENTER);
        text.setTextBaseLine(TextBaseLine.MIDDLE);
        text.setFillColor(Color.fromColorString(COLORS[index][6]));        
        g.add(text);
        
        return g;
    }

    @Override
    protected Item doCopy()
    {
        return new Coin(m_amount, m_stuck);
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        return ExplodeAction.REMOVE;
    }
    
    public static List<Coin> getCoins(int amount)
    {
        List<Coin> list = new ArrayList<Coin>();
        int i = COIN_DENOMINATION.length - 1;
        while (amount > 0)
        {
            int denom = COIN_DENOMINATION[i];
            if (amount >= denom)
            {
                list.add(new Coin(denom, false));
                amount -= denom;
            }
            else i--;            
        }
        return list;
    }
}
