package com.enno.dotz.client.box;

import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.IColor;

public class BoxDefaults
{
    public static final BoxDefaults INSTANCE = new BoxDefaults();
    
    int fontSize = 10;
    IColor textColor = ColorName.BLACK;
    
    IColor borderColor = ColorName.DARKSLATEBLUE;
    IColor buttonColor = ColorName.LIGHTSTEELBLUE;
    IColor buttonActiveColor = ColorName.LIGHTBLUE;
    
    IColor headerBackground = ColorName.DARKSLATEBLUE;
    IColor headerTextColor = ColorName.WHITE;
        
    int cornerRadius = 6;
    
    int padding = 10;
    int spacing = 10;
}
