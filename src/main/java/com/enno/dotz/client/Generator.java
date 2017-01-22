package com.enno.dotz.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.ait.lienzo.shared.core.types.IColor;
import com.enno.dotz.client.anim.Pt;
import com.enno.dotz.client.item.Anchor;
import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.item.Blaster;
import com.enno.dotz.client.item.Blocker;
import com.enno.dotz.client.item.Chest;
import com.enno.dotz.client.item.Clock;
import com.enno.dotz.client.item.Diamond;
import com.enno.dotz.client.item.Domino;
import com.enno.dotz.client.item.Dot;
import com.enno.dotz.client.item.DotBomb;
import com.enno.dotz.client.item.Drop;
import com.enno.dotz.client.item.IcePick;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.Key;
import com.enno.dotz.client.item.Knight;
import com.enno.dotz.client.item.Laser;
import com.enno.dotz.client.item.Mirror;
import com.enno.dotz.client.item.Rocket;
import com.enno.dotz.client.item.Turner;
import com.enno.dotz.client.item.Wild;
import com.enno.dotz.client.util.FrequencyGenerator;

public class Generator
{
    public static final int DOT_MODE = 0;
    public static final int DOMINO_MODE = 1;
    public static final int LETTER_MODE = 2;
    public static final int SWAP_MODE = 3;
    public static final int DIAGONAL_MODE = 4;
    public static final int CLICK_MODE = 5;
    
    public static final long RANDOM_SEED = -1;

    protected static FrequencyGenerator<String> s_letterGenerator = createLetterGenerator();
    
    protected List<Generator.ItemFrequency> m_list = new ArrayList<Generator.ItemFrequency>();
    protected long                          m_seed = RANDOM_SEED;

    protected double m_scale;
    protected double m_dotsOnlyScale;
    protected Random m_rnd;

    public int           fireGrowthRate   = 1;                  // default: 1 fire per turn
    public int           animalStrength   = 10;
    public Animal.Type   animalType       = Animal.Type.DEFAULT;
    public Animal.Action animalAction     = Animal.Action.DEFAULT;
    public int           maxAnchors       = 3;                  // also applies to Diamonds
    public int           knightStrength   = 3;
    public int           blockerStrength  = 2;
    public int           bombStrength     = 9;
    public Integer       clockStrength    = 10;
    public Integer       chestStrength    = 1;
    public boolean       initialDotsOnly  = false;
    public boolean       generateLetters  = false;
    public boolean       swapMode         = false;
    public boolean       rollMode         = false;
    public boolean       diagonalMode     = false;
    public boolean       dominoMode       = false;
    public boolean       clickMode        = false;
    public int           maxDomino        = 9;
    public boolean       slipperyAnchors  = false;              // also applies to Diamonds
    public int           minChainLength   = 2;
    public String        rewardStrategies = "";
    public int           icePickRadius    = 3;
    public int           dropRadius       = 3;
    public double        radioActivePct   = 0;
    public int           eggsNeeded       = 3;                  // 3 eggs makes a cracked egg, 3 cracked eggs makes a bird
    
    // Word Mode settings
    public boolean       removeLetters    = true;
    public boolean       findWords        = false;
    public int           maxWordLength    = 9;
    
    private int          m_excludeDotColor = -1; // when user makes a square, that color is not generated

    private int          m_numDotColors;
    private boolean      m_noChest = false;
    
    private DominoGenerator m_dominoGenerator;
    private Long m_usedSeed;

    public Generator()
    {
    }    
    
    public int getMode()
    {
        if (dominoMode)
            return DOMINO_MODE;
        if (swapMode)
            return SWAP_MODE;
        if (generateLetters)
            return LETTER_MODE;
        if (diagonalMode)
            return DIAGONAL_MODE;
        if (clickMode)
            return CLICK_MODE;
        
        return DOT_MODE;
    }
    
    public void setSeed(long seed)
    {
        m_seed = seed;
        m_rnd = null;
        m_usedSeed = null;
    }
    
    public long getUsedSeed()
    {
        if (m_usedSeed == null)
        {
            m_usedSeed = m_seed == RANDOM_SEED ? System.currentTimeMillis() : m_seed;
        }
        return m_usedSeed;
    }
    
    public Random getRandom()
    {
        if (m_rnd == null)
        {
            m_rnd = new Random(getUsedSeed());
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
        g.animalAction = animalAction;
        g.fireGrowthRate = fireGrowthRate;
        g.maxAnchors = maxAnchors;
        g.knightStrength = knightStrength;
        g.blockerStrength = blockerStrength;
        g.clockStrength = clockStrength;
        g.chestStrength = chestStrength;
        g.bombStrength = bombStrength;
        g.generateLetters = generateLetters;
        g.swapMode = swapMode;
        g.rollMode = rollMode;
        g.diagonalMode = diagonalMode;
        g.clickMode = clickMode;
        g.dominoMode = dominoMode;
        g.maxDomino = maxDomino;
        g.slipperyAnchors = slipperyAnchors;
        g.minChainLength = minChainLength;
        g.rewardStrategies = rewardStrategies;
        g.icePickRadius = icePickRadius;
        g.dropRadius = dropRadius;
        g.removeLetters = removeLetters;
        g.findWords = findWords;
        g.maxWordLength = maxWordLength;
        g.radioActivePct = radioActivePct;
        g.eggsNeeded = eggsNeeded;
        
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
    
    public int getNextDotColor()
    {
        ItemFrequency dot = getNextDot();
        return ((Dot) dot.item).color;
    }
    
    private static final int COIN_10_DISTRIB = 1;
    private static final int COIN_5_DISTRIB = 3;
    private static final int COIN_1_DISTRIB = 10;
    private static final double TOTAL_DISTRIB = COIN_10_DISTRIB + COIN_5_DISTRIB + COIN_1_DISTRIB;
    
    private static final double COIN_10_CHANCE = COIN_10_DISTRIB / TOTAL_DISTRIB;
    private static final double COIN_5_CHANCE = COIN_5_DISTRIB / TOTAL_DISTRIB;
    private static final double COIN_1_CHANCE = COIN_1_DISTRIB / TOTAL_DISTRIB;
    
    public int nextCoinAmount()
    {
        double d = getRandom().nextDouble();
        if (d < COIN_10_CHANCE)
            return 10;
        if (d < COIN_10_CHANCE + COIN_5_CHANCE)
            return 5;
        return 1;
    }
    
    public Item getNextItem(Context ctx, boolean initial, Boolean radioActive)
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
                if (maxAnchors > 0 && item instanceof Anchor && ctx.score.getAnchorsInGrid() >= maxAnchors)
                {
                    continue;
                }
                if (maxAnchors > 0 && item instanceof Diamond && ctx.score.getDiamondsInGrid() >= maxAnchors)
                {
                    continue;
                }
            
                if (m_excludeDotColor != -1 && m_numDotColors > 1 && item instanceof Dot && ((Dot) item).color == m_excludeDotColor)
                    continue;
                
                if (item instanceof Drop && ctx.score.getFireInGrid() == 0)                    
                    continue;
                if (item instanceof Key && ctx.score.getDoorsInGrid() == 0)                    
                    continue;
                if (item instanceof IcePick && ctx.score.getIceInGrid() == 0)                    
                    continue;
                
                if (m_noChest && item instanceof Chest)
                    continue;
            }
            
            if (item instanceof Dot)
            {
                ((Dot) item).setRadioActive(makeRadioActive(radioActive));
            }
            else if (item instanceof DotBomb)
            {
                Dot dot = null;
                do
                {
                    // Generate dot
                    dot = (Dot) getNextDot().createItem(ctx, generateLetters ? s_letterGenerator : null, getDominoGenerator());
                }
                while (m_excludeDotColor != -1 && m_numDotColors > 1 && dot.color == m_excludeDotColor);
                
                dot.setRadioActive(makeRadioActive(radioActive));
                
                ((DotBomb) item).setDot(dot);
            }
            else if (item instanceof Chest)
            {
                Chest chest = (Chest) item;
                m_noChest = true;   // prevent chest inside chest
                chest.setItem(getNextItem(ctx, initial, radioActive));
                m_noChest = false;
                ctx.score.generatedChest();
            }
            else if (item instanceof Anchor)
            {
                ctx.score.generatedAnchor();
            }
            else if (item instanceof Diamond)
            {
                ctx.score.generatedDiamond();
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
            else if (item instanceof Blocker)
            {
                if (((Blocker) item).isZapOnly())
                    ctx.score.generatedZapBlocker();
                else
                    ctx.score.generatedBlocker();
            }
            break;
        }        
        
        item.init(ctx);
        return item;
    }
    
    private boolean makeRadioActive(Boolean radioActive)
    {
        if (radioActive != null)
            return radioActive.booleanValue();
        else
            return radioActivePct > 0 && m_rnd.nextDouble() * 100 < radioActivePct;
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
        
        Dot oldDot = (oldItem instanceof DotBomb) ? ((DotBomb) oldItem).getDot() : (Dot) oldItem;
        
        Dot newDot;
        while (true)
        {
            newDot = (Dot) getNextDot().createItem(ctx, generateLetters ? s_letterGenerator : null, getDominoGenerator());
            if (oldItem.getColor() == newDot.getColor())
                continue;
            
            break;
        }
        
        newDot.setRadioActive(oldDot.isRadioActive());        
        
        if (oldItem instanceof DotBomb)
        {
            DotBomb bomb = new DotBomb(newDot, ((DotBomb) oldItem).getStrength(), oldItem.isStuck());
            bomb.init(ctx);
            return bomb;
        }
        
        newDot.init(ctx);
        return newDot;
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

    public static int getLetterPoints(String letter)
    {
        switch (letter.charAt(0))
        {
            case 'Q': 
            case 'Z':   return 10;
            
            case 'J': 
            case 'X':   return 8;
            
            case 'K':   return 5;
                
            case 'F': 
            case 'H': 
            case 'V': 
            case 'W': 
            case 'Y':   return 4;
                
            case 'B': 
            case 'C': 
            case 'M': 
            case 'P':   return 3;
            
            case 'D': 
            case 'G':   return 2;
            
            default:    return 1;
        }
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
                animal.setAction(ctx.generator.animalAction);
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
            else if (item instanceof Blocker)
            {
                Blocker blocker = (Blocker) item;
                blocker.setStrength(ctx.generator.blockerStrength);
            }
            else if (item instanceof Chest)
            {
                Chest chest = (Chest) item;
                chest.setStrength(ctx.generator.chestStrength);
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
            else if (item instanceof IcePick)
            {
                IcePick pick = (IcePick) item;
                pick.setRadius(ctx.generator.icePickRadius);
            }
            else if (item instanceof Drop)
            {
                Drop drop = (Drop) item;
                drop.setRadius(ctx.generator.dropRadius);
            }
            else if (item instanceof DotBomb)
            {
                DotBomb bomb = (DotBomb) item;
                bomb.setStrength(ctx.generator.bombStrength); //TODO generate letter bombs?
            }
            else if (item instanceof Blaster)
            {
                Blaster blaster = (Blaster) item;
                blaster.setVertical(ctx.generator.getRandom().nextBoolean());
            }
            else if (item instanceof Dot && letterGenerator != null)
            {
                ((Dot) item).setLetter(letterGenerator.next(ctx.generator.getRandom()));
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
            return new Domino(pt.col, pt.row, Direction.randomDirection(rnd), false);
        }
    }
}