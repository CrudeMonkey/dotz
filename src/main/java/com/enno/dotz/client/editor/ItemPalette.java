package com.enno.dotz.client.editor;

import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Layer;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.Direction;
import com.enno.dotz.client.item.Anchor;
import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.item.Blaster;
import com.enno.dotz.client.item.Blocker;
import com.enno.dotz.client.item.Bomb;
import com.enno.dotz.client.item.Clock;
import com.enno.dotz.client.item.Cog;
import com.enno.dotz.client.item.Coin;
import com.enno.dotz.client.item.ColorBomb;
import com.enno.dotz.client.item.Diamond;
import com.enno.dotz.client.item.Domino;
import com.enno.dotz.client.item.Dot;
import com.enno.dotz.client.item.Drop;
import com.enno.dotz.client.item.Egg;
import com.enno.dotz.client.item.Fire;
import com.enno.dotz.client.item.IcePick;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.Key;
import com.enno.dotz.client.item.Knight;
import com.enno.dotz.client.item.Laser;
import com.enno.dotz.client.item.LazySusan;
import com.enno.dotz.client.item.Mirror;
import com.enno.dotz.client.item.RandomItem;
import com.enno.dotz.client.item.Rocket;
import com.enno.dotz.client.item.Spider;
import com.enno.dotz.client.item.Striped;
import com.enno.dotz.client.item.Turner;
import com.enno.dotz.client.item.Wild;
import com.enno.dotz.client.item.YinYang;

public class ItemPalette extends Palette<Object>
{
    public ItemPalette()
    {
        super(40, ItemButton.SIZE, 7, 6);

        ctx.dotLayer = new Layer();
        add(ctx.dotLayer);                
        
        addButton(new RandomItem(), 0, 0);
        addButton(new Wild(false), 1, 0);
        addButton(new Fire(false), 2, 0);
        addButton(new Anchor(false), 3, 0);
        addButton(new Knight(1, false), 4, 0);
        addButton(new Rocket(Direction.EAST, false), 5, 0);
        addButton(new Diamond(), 6, 0);
        
        for (int i = 0; i < Config.MAX_COLORS; i++)
        {
            addButton(new Dot(i), i, 1);
            addButton(new Animal(i, 0, Animal.Type.DEFAULT, false), i, 2);
        }        
        addButton(new Animal(Animal.BLACK_ANIMAL,0, Animal.Type.DEFAULT, false), Config.MAX_COLORS, 2);
        
        addButton(new LazySusan(0, 0, true), 0, 3);
        addButton(new LazySusan(0, 0, false), 1, 3);
        addButton(new Clock(11, false), 2, 3);
        addButton(new Laser(Direction.EAST, false), 3, 3);
        addButton(new Mirror(false, false), 4, 3);        
        addButton(new YinYang(false), 5, 3);
        addButton(new Coin(), 6, 3);

        addButton(new Egg(), 0, 4);
        addButton(new Domino(), 1, 4);
        addButton(new Turner(3, false), 2, 4);
        addButton(new Drop(), 3, 4);
        addButton(new ColorBomb(false), 4, 4);
        addButton(new Key(false), 5, 4);
        addButton(new Spider(), 6, 4);
        
        addButton(new IcePick(), 0, 5);
        addButton(new Striped(0, false), 1, 5);
        addButton(new Blocker(1, false, false), 2, 5);
        addButton(new Blocker(1, false, true), 3, 5);
        addButton(new Cog(), 4, 5);
        addButton(new Blaster(true, false), 5, 5);
        addButton(new Bomb(), 6, 5);
    }
    
    private void addButton(Object cell, int col, int row)
    {
        int W = ItemButton.SIZE;
        double x = (col + 0.5) * W;
        double y = (row + 0.5) * W;
        
        int numCols = 1;
        int numRows = 1;
        if (cell instanceof LazySusan)
        {            
            double sz = ctx.cfg.size;
            LazySusan susan = (LazySusan) cell;
            IPrimitive<?> shape = susan.createShape(sz);
            shape.setX(x);
            shape.setY(y);
            ctx.dotLayer.add(shape);
        }
        else if (cell instanceof Item)
        {
            Item item = (Item) cell;
            item.setContext(ctx);
            IPrimitive<?> shape = item.createShape(ctx.cfg.size);
            shape.setX(x);
            shape.setY(y);
            ctx.dotLayer.add(shape);
        }
        
        ItemButton b = new ItemButton(col, row, numCols, numRows, cell, ctx);
        b.setX((col + 0.5) * W);
        b.setY((row + 0.5) * W);
        m_list.add(b);
    }
    
    public static class ItemButton extends PaletteButton<Object>
    {
        public static final int SIZE = 40 + 10;                

        public ItemButton(int col, int row, int numCols, int numRows, Object cell, Context ctx)
        {
            super(col, row, numCols, numRows, SIZE, cell, ctx, true);
        }
    }
}
