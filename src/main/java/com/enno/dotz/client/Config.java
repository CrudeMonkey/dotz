package com.enno.dotz.client;

import com.ait.lienzo.shared.core.types.Color;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.IColor;

public class Config
{
    public static final int UNDEFINED_ID = -1;
    
    public static int WILD_ID = 100;
    public static int ANCHOR_ID = 101;
    public static int FIRE_ID = 102;
    
    public static IColor[] COLORS = {
            Color.fromColorString("#768BBD"), // light blue
            Color.fromColorString("#7CC196"), // green,
            Color.fromColorString("#D05C6C"), // red,
            Color.fromColorString("#945B7D"), // purple
            Color.fromColorString("#F7CD6E"), // yellow
            //Color.fromColorString("#FFA500"), // orange
            Color.fromColorString("#FF8C00"), // orange
    };
    public static int MAX_COLORS = COLORS.length;
    
    public int id = UNDEFINED_ID;
    public String name = "?";
    public String creator = "";
    public String folder = "";
    public String description = "";
    public Long lastModified;
    
    public int numColumns = 8;
    public int numRows = 8;
    public int size = 50;               // size of grid cell (iPhone 320x480 - size=40)

    public IColor[] colors = COLORS; //TODO define colors

    public double dropDuration = 120;               // animation speed of dropping dot
    public double nukeDuration = 250;
    public double explosionDuration = 250;
    public double growFireDuration = 500;           // grow fire & move animals
    public double lazySusanTurnDuration = growFireDuration / 2;
        
    public Generator generator;
    
    public Goal goals;
    
    public GridState grid;
    
    public Config()
    {
        
    }
    
    public void init()
    {
        
    }
    
    public IColor drawColor(Integer color)
    {
        return colors[color];
    }
    
    public IColor connectColor(Integer color)
    {
        if (isWild(color))
            return ColorName.BLACK;
        
        return drawColor(color);
    }

    public boolean isWild(Integer color)
    {
        return color != null && color == WILD_ID;
    }
}
