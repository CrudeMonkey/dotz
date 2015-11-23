package com.enno.dotz.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.ait.lienzo.shared.core.types.IColor;
import com.enno.dotz.client.anim.Pt;
import com.enno.dotz.client.item.Anchor;
import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.item.Clock;
import com.enno.dotz.client.item.Domino;
import com.enno.dotz.client.item.Dot;
import com.enno.dotz.client.item.DotBomb;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.Knight;
import com.enno.dotz.client.item.Laser;
import com.enno.dotz.client.item.Mirror;
import com.enno.dotz.client.item.Rocket;
import com.enno.dotz.client.item.Turner;
import com.enno.dotz.client.item.Wild;
import com.enno.dotz.client.util.FrequencyGenerator;

public class Generator
{
    public static final long RANDOM_SEED = -1;

    protected static FrequencyGenerator<String> s_letterGenerator = createLetterGenerator();
    
    protected List<Generator.ItemFrequency> m_list = new ArrayList<Generator.ItemFrequency>();
    protected long                          m_seed = RANDOM_SEED;

    protected double m_scale;
    protected double m_dotsOnlyScale;
    protected Random m_rnd;

    public int         fireGrowthRate = 1;                  // default: 1 fire per turn
    public int         animalStrength = 10;
    public Animal.Type animalType     = Animal.Type.DEFAULT;
    public int         maxAnchors     = 3;
    public int         knightStrength = 3;
    public int         bombStrength   = 9;
    public Integer     clockStrength  = 10;
    public boolean     initialDotsOnly = false;
    public boolean     generateLetters = false;
    public boolean     swapMode        = false;
    public boolean     rollMode        = false;
    public boolean     dominoMode      = false;
    public int         maxDomino       = 9;
    
    private int        m_excludeDotColor = -1; // when user makes a square, that color is not generated

    private int m_numDotColors;

    private DominoGenerator m_dominoGenerator;

    public Generator()
    {
    }    
    
    public void setSeed(long seed)
    {
        m_seed = seed;
        m_rnd = null;
    }
    
    protected Random getRandom()
    {
        if (m_rnd == null)
        {
            m_rnd = new Random(m_seed == RANDOM_SEED ? System.currentTimeMillis() : m_seed);
        }
        return m_rnd;
    }
    
    public long getSeed()
    {
        return m_seed;
    }
    
    public String nextLetter()
    {
        return nextLetter(m_rnd);
    }
    
    public static String nextLetter(Random rnd)
    {
        return s_letterGenerator.next(rnd);
    }
    
    public void excludeDotColor(int color)
    {
        m_excludeDotColor = color; 
    }
    
    public void dontExcludeDotColor()
    {
        m_excludeDotColor = -1;
    }
    
    public Generator copy()
    {
        Generator g = new Generator();
        g.setSeed(m_seed);
        g.animalStrength = animalStrength;
        g.animalType = animalType;
        g.fireGrowthRate = fireGrowthRate;
        g.maxAnchors = maxAnchors;
        g.knightStrength = knightStrength;
        g.clockStrength = clockStrength;
        g.bombStrength = bombStrength;
        g.generateLetters = generateLetters;
        g.swapMode = swapMode;
        g.rollMode = rollMode;
        g.dominoMode = dominoMode;
        g.maxDomino = maxDomino;
        
        for (ItemFrequency f : m_list)
        {
            g.add(f.copy());
        }
        return g;
    }
    
    public void add(ItemFrequency f)
    {
        m_list.add(f);
        init();
    }
    
    public List<ItemFrequency> getFrequencies()
    {
        return m_list;
    }
    
    protected void init()
    {
        m_scale = 0;
        m_dotsOnlyScale = 0;
        m_numDotColors = 0;
        for (ItemFrequency f : m_list)
        {
            m_scale += f.frequency;
            f.maxFrequency = m_scale;
            
            if (f.item instanceof Dot || f.item instanceof Wild)
            {
                m_dotsOnlyScale += f.frequency;
                f.maxDotsOnlyFrequency = m_dotsOnlyScale;
                
                if (f.item instanceof Dot)
                    m_numDotColors++;
            }
        }
    }
    
    protected DominoGenerator getDominoGenerator()
    {
        if (m_dominoGenerator == null)
        {
            m_dominoGenerator = new DominoGenerator(maxDomino);
        }
        return m_dominoGenerator;
    }
    
    protected ItemFrequency getNext(boolean initial)
    {
        double d = getRandom().nextDouble() * (initial ? m_dotsOnlyScale : m_scale);

        for (ItemFrequency f : m_list)
        {
            if (d < (initial ? f.maxDotsOnlyFrequency : f.maxFrequency))
                return f;
        }
        return m_list.get(0); // shouldn't happen
    }
    
    protected ItemFrequency getNextDot()
    {
        while (true)
        {
            ItemFrequency f = getNext(false);
            if (f.item instanceof Dot)
                return f;
        }
    }
    
    public Item getNextItem(Context ctx, boolean initial)
    {
        if (initial && dominoMode)
        {
            //TODO Wild
            return getNextDomino(ctx);
        }
        
        Item item;
        while (true)
        {
            item = getNext(initial).createItem(ctx, generateLetters ? s_letterGenerator : null, getDominoGenerator());
            
            if (m_list.size() > 1)
            {
                if (maxAnchors > 0 && item instanceof Anchor && 
                    ctx.score.getAnchorsInGrid() >= maxAnchors)
                {
                    continue;
                }
            
                if (m_excludeDotColor != -1 && m_numDotColors > 1 && item instanceof Dot && ((Dot) item).color == m_excludeDotColor)
                    continue;
            }
            
            if (item instanceof DotBomb)
            {
                Dot dot = null;
                do
                {
                    // Generate dot
                    dot = (Dot) getNextDot().createItem(ctx, generateLetters ? s_letterGenerator : null, getDominoGenerator());
                }
                while (m_excludeDotColor != -1 && m_numDotColors > 1 && dot.color == m_excludeDotColor);
                
                ((DotBomb) item).setDot(dot);
            }
            else if (item instanceof Anchor)
            {
                ctx.score.generatedAnchor();
            }
            else if (item instanceof Animal)
            {
                ctx.score.generatedAnimal();
            }
            else if (item instanceof Knight)
            {
                ctx.score.generatedKnight();
            }
            else if (item instanceof Clock)
            {
                ctx.score.generatedClock();
            }
            else if (item instanceof Mirror)
            {
                ctx.score.generatedMirror();
            }
            else if (item instanceof Laser)
            {
                ctx.score.generatedLaser();
            }
            else if (item instanceof Rocket)
            {
                ctx.score.generatedRocket();
            }
            break;
        }        
        
        item.init(ctx);
        return item;
    }
    
    protected Item getNextDomino(Context ctx)
    {
        Domino domino = getDominoGenerator().nextDomino(getRandom());
        domino.init(ctx);
        return domino;
    }

    public Item changeColor(Context ctx, Item oldItem)
    {
        boolean isDomino = oldItem instanceof Domino;
        if (isDomino)
        {
            return getNextDomino(ctx);
        }
        
        if (!hasAtLeastTwoColors())
            return oldItem;
        
        Item item;
        while (true)
        {
            item = getNextDot().createItem(ctx, generateLetters ? s_letterGenerator : null, getDominoGenerator());
            if (oldItem.getColor() == item.getColor())
                continue;
            
            break;
        }
        
        if (oldItem instanceof DotBomb)
        {
            DotBomb bomb = new DotBomb((Dot) item, ((DotBomb) oldItem).getStrength());
            bomb.init(ctx);
            return bomb;
        }
        
        item.init(ctx);
        return item;
    }

    public boolean hasAtLeastTwoColors()
    {
        return m_numDotColors > 1;
    }
    
    public List<IColor> getColors(Context ctx)
    {
        List<IColor> list = new ArrayList<IColor>();
        for (ItemFrequency freq : m_list)
        {
            if (freq.item instanceof Dot)
            {
                int color = ((Dot) freq.item).color;
                list.add(ctx.cfg.colors[color]);
            }
        }
        return list;
    }

    public static FrequencyGenerator<String> createLetterGenerator()
    {
        FrequencyGenerator<String> f = new FrequencyGenerator<String>();
        f.add(12.02, "E");
        f.add(9.10, "T");
        f.add(8.12, "A");
        f.add(7.68, "O");
        f.add(7.31, "I");
        f.add(6.95, "N");
        f.add(6.28, "S");
        f.add(6.02, "R");
        f.add(5.92, "H");
        f.add(4.32, "D");
        f.add(3.98, "L");
        f.add(2.88, "U");
        f.add(2.71, "C");
        f.add(2.61, "M");
        f.add(2.30, "F");
        f.add(2.11, "Y");
        f.add(2.09, "W");
        f.add(2.03, "G");
        f.add(1.82, "P");
        f.add(1.49, "B");
        f.add(1.11, "V");
        f.add(0.69, "K");
        f.add(0.17, "X");
        f.add(0.11, "Qu");
        f.add(0.10, "J");
        f.add(0.07, "Z");
        return f;
    }
    
    public static class ItemFrequency
    {
        public Item item;
        public double frequency;
        
        protected double maxFrequency;
        protected double maxDotsOnlyFrequency;
        
        public ItemFrequency(Item item, double frequency)
        {
            this.item = item;
            this.frequency = frequency;
        }
        
        public ItemFrequency copy()
        {
            return new ItemFrequency(item.copy(), frequency);
        }

        public Item createItem(Context ctx, FrequencyGenerator<String> letterGenerator, DominoGenerator dominoGenerator)
        {
            if (item instanceof Animal)
            {
                Animal animal = (Animal) item;
                animal.setStrength(ctx.generator.animalStrength);
                animal.setType(ctx.generator.animalType);
            }
            else if (item instanceof Knight)
            {
                Knight knight = (Knight) item;
                knight.setStrength(ctx.generator.knightStrength);
            }
            else if (item instanceof Clock)
            {
                Clock clock = (Clock) item;
                clock.setStrength(ctx.generator.clockStrength + 1); // when it drops in, it loses 1
            }
            else if (item instanceof Laser)
            {
                Laser laser = (Laser) item;
                laser.setDirection(Direction.randomDirection(ctx.generator.getRandom()));
            }
            else if (item instanceof Mirror)
            {
                Mirror mirror = (Mirror) item;
                mirror.setFlipped(ctx.generator.getRandom().nextBoolean());
            }
            else if (item instanceof Rocket)
            {
                Rocket rocket = (Rocket) item;
                rocket.setDirection(Direction.randomDirection(ctx.generator.getRandom()));
            }
            else if (item instanceof Turner)
            {
                Turner turner = (Turner) item;
                turner.n = 1 + ctx.generator.getRandom().nextInt(3);
            }
            else if (item instanceof DotBomb)
            {
                DotBomb bomb = (DotBomb) item;
                bomb.setStrength(ctx.generator.bombStrength);
            }
            else if (item instanceof Dot && letterGenerator != null)
            {
                ((Dot) item).letter = letterGenerator.next(ctx.generator.getRandom());
            }
            else if (item instanceof Domino)
            {
                return dominoGenerator.nextDomino(ctx.generator.getRandom());
            }
            return item.copy();
        }
    }
    
    public static class DominoGenerator extends FrequencyGenerator<Pt>
    {
        public DominoGenerator(int max)
        {
            for (int top = 0; top <= max; top++)
            {
                for (int bottom = 0; bottom <= top; bottom++)
                {
                    add(1, new Pt(top, bottom));
                }
            }
        }
        
        public Domino nextDomino(Random rnd)
        {
            Pt pt = next(rnd);
            return new Domino(pt.col, pt.row, Direction.randomDirection(rnd));
        }
    }
}