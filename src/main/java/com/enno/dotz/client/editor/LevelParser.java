package com.enno.dotz.client.editor;

import com.ait.tooling.nativetools.client.NArray;
import com.ait.tooling.nativetools.client.NObject;
import com.enno.dotz.client.Boosts;
import com.enno.dotz.client.Cell;
import com.enno.dotz.client.Cell.Cage;
import com.enno.dotz.client.Cell.ChangeColorCell;
import com.enno.dotz.client.Cell.CircuitCell;
import com.enno.dotz.client.Cell.ConveyorCell;
import com.enno.dotz.client.Cell.Door;
import com.enno.dotz.client.Cell.Hole;
import com.enno.dotz.client.Cell.ItemCell;
import com.enno.dotz.client.Cell.Rock;
import com.enno.dotz.client.Cell.Slide;
import com.enno.dotz.client.Cell.Teleport;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Direction;
import com.enno.dotz.client.Generator;
import com.enno.dotz.client.Generator.ItemFrequency;
import com.enno.dotz.client.Goal;
import com.enno.dotz.client.GridState;
import com.enno.dotz.client.anim.Pt;
import com.enno.dotz.client.item.Anchor;
import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.item.Clock;
import com.enno.dotz.client.item.ColorBomb;
import com.enno.dotz.client.item.Domino;
import com.enno.dotz.client.item.Dot;
import com.enno.dotz.client.item.DotBomb;
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
import com.enno.dotz.client.item.Turner;
import com.enno.dotz.client.item.Wild;
import com.enno.dotz.client.item.YinYang;
import com.enno.dotz.client.ui.ErrorWindow;
import com.enno.dotz.client.util.Debug;

public class LevelParser
{
    public Config parse(NObject p)
    {
        try
        {
            Config cfg = new Config();
           
            if (p.isInteger("id"))
                cfg.id = p.getAsInteger("id");
            if (p.isString("name"))
                cfg.name = p.getAsString("name");
            if (p.isString("folder"))
                cfg.folder = p.getAsString("folder");
            if (p.isString("creator"))
                cfg.creator = p.getAsString("creator");
            if (p.isString("description"))
                cfg.description = p.getAsString("description");
            if (p.isInteger("numRows"))
                cfg.numRows = p.getAsInteger("numRows");
            if (p.isInteger("numColumns"))
                cfg.numColumns = p.getAsInteger("numColumns");
            if (p.isString("lastModified"))
                cfg.lastModified = Long.parseLong(p.getAsString("lastModified"));
            
            //TODO map color numbers to actual colors
            
            GridState grid = cfg.grid = new GridState(cfg.numColumns, cfg.numRows);
            
            NArray gridStr = p.getAsArray("grid");            
            parseGrid(cfg.grid, gridStr);
            
            if (p.isArray("teleporters"))
            {
                NArray t = p.getAsArray("teleporters");
                for (int i = 0, n = t.size(); i < n; i++)
                {
                    NObject row = t.getAsObject(i);
                    Pt from = pt(row.getAsArray("from"));
                    Pt to = pt(row.getAsArray("to"));
                    Teleport fromCell = (Teleport) grid.cell(from.col, from.row);
                    Teleport toCell = (Teleport) grid.cell(to.col, to.row);
                    fromCell.setOther(to.col, to.row);
                    toCell.setOther(from.col, from.row);
                }
            }
            
            if (p.isArray("doors"))
            {
                NArray t = p.getAsArray("doors");
                for (int i = 0, n = t.size(); i < n; i++)
                {
                    NObject row = t.getAsObject(i);
                    Pt pt = pt(row.getAsArray("p"));
                    int strength = row.getAsInteger("strength");
                    int direction = parseDirection(row.getAsString("direction"));                    
                    
                    int rotDir = 0;
                    if (row.isInteger("rotate"))
                        rotDir = row.getAsInteger("rotate");
                    
                    Door c = new Door(strength, direction, rotDir);
                    grid.setCell(pt.col, pt.row, c);
                }
            }
            if (p.isArray("cages"))
            {
                NArray t = p.getAsArray("cages");
                for (int i = 0, n = t.size(); i < n; i++)
                {
                    NObject row = t.getAsObject(i);
                    Pt pt = pt(row.getAsArray("p"));
                    int strength = row.getAsInteger("strength");                  
                    
                    Cage c = new Cage(strength);
                    grid.setCell(pt.col, pt.row, c);
                }
            }
            if (p.isArray("conveyors"))
            {
                NArray t = p.getAsArray("conveyors");
                for (int i = 0, n = t.size(); i < n; i++)
                {
                    NObject row = t.getAsObject(i);
                    Pt pt = pt(row.getAsArray("p"));
                    int turn = row.getAsInteger("turn");
                    int direction = parseDirection(row.getAsString("direction"));                    
                    
                    ConveyorCell c = new ConveyorCell(direction, turn);
                    grid.setCell(pt.col, pt.row, c);
                }
            }
            
            NArray ice = p.getAsArray("ice");
            if (ice != null)
                parseIce(cfg.grid, ice);
            
            if (p.isArray("animals"))
            {
                NArray t = p.getAsArray("animals");
                for (int i = 0, n = t.size(); i < n; i++)
                {
                    NObject row = t.getAsObject(i);
                    Pt pt = pt(row.getAsArray("p"));
                    int strength = row.getAsInteger("strength");
                    int color = row.getAsInteger("color");
                    String type = row.getAsString("type");
                    
                    Cell c = grid.cell(pt.col, pt.row);
                    c.item = new Animal(color, strength, Animal.Type.fromName(type));
                }
            }
            if (p.isArray("lasers"))
            {
                NArray t = p.getAsArray("lasers");
                for (int i = 0, n = t.size(); i < n; i++)
                {
                    NObject row = t.getAsObject(i);
                    Pt pt = pt(row.getAsArray("p"));
                    int direction = parseDirection(row.getAsString("direction"));
                    
                    Cell c = grid.cell(pt.col, pt.row);
                    c.item = new Laser(direction);
                }
            }
            if (p.isArray("rockets"))
            {
                NArray t = p.getAsArray("rockets");
                for (int i = 0, n = t.size(); i < n; i++)
                {
                    NObject row = t.getAsObject(i);
                    Pt pt = pt(row.getAsArray("p"));
                    int direction = parseDirection(row.getAsString("direction"));
                    
                    Cell c = grid.cell(pt.col, pt.row);
                    c.item = new Rocket(direction);
                }
            }
            if (p.isArray("mirrors"))
            {
                NArray t = p.getAsArray("mirrors");
                for (int i = 0, n = t.size(); i < n; i++)
                {
                    NObject row = t.getAsObject(i);
                    Pt pt = pt(row.getAsArray("p"));
                    boolean flip = row.getAsBoolean("flip");
                    
                    Cell c = grid.cell(pt.col, pt.row);
                    c.item = new Mirror(flip);
                }
            }
            if (p.isArray("knights"))
            {
                NArray t = p.getAsArray("knights");
                for (int i = 0, n = t.size(); i < n; i++)
                {
                    NObject row = t.getAsObject(i);
                    Pt pt = pt(row.getAsArray("p"));
                    int strength = row.getAsInteger("strength");
                    
                    Cell c = grid.cell(pt.col, pt.row);
                    c.item = new Knight(strength);
                }
            }
            if (p.isArray("clocks"))
            {
                NArray t = p.getAsArray("clocks");
                for (int i = 0, n = t.size(); i < n; i++)
                {
                    NObject row = t.getAsObject(i);
                    Pt pt = pt(row.getAsArray("p"));
                    int strength = row.getAsInteger("strength");
                    
                    Cell c = grid.cell(pt.col, pt.row);
                    c.item = new Clock(strength);
                }
            }
            if (p.isArray("drops"))
            {
                NArray t = p.getAsArray("drops");
                for (int i = 0, n = t.size(); i < n; i++)
                {
                    NObject row = t.getAsObject(i);
                    Pt pt = pt(row.getAsArray("p"));
                    //int strength = row.getAsInteger("strength");
                    
                    Cell c = grid.cell(pt.col, pt.row);
                    c.item = new Drop();
                }
            }
            if (p.isArray("picks"))
            {
                NArray t = p.getAsArray("picks");
                for (int i = 0, n = t.size(); i < n; i++)
                {
                    NObject row = t.getAsObject(i);
                    Pt pt = pt(row.getAsArray("p"));
                    
                    Cell c = grid.cell(pt.col, pt.row);
                    c.item = new IcePick();
                }
            }
            if (p.isArray("colorBombs"))
            {
                NArray t = p.getAsArray("colorBombs");
                for (int i = 0, n = t.size(); i < n; i++)
                {
                    NObject row = t.getAsObject(i);
                    Pt pt = pt(row.getAsArray("p"));
                    //int strength = row.getAsInteger("strength");
                    
                    Cell c = grid.cell(pt.col, pt.row);
                    c.item = new ColorBomb();
                }
            }
            if (p.isArray("dominoes"))
            {
                NArray t = p.getAsArray("dominoes");
                for (int i = 0, n = t.size(); i < n; i++)
                {
                    NObject row = t.getAsObject(i);
                    Pt pt = pt(row.getAsArray("p"));
                    Pt num = pt(row.getAsArray("num"));
                    boolean vertical = row.getAsBoolean("vertical");                                        
                    
                    Cell c = grid.cell(pt.col, pt.row);
                    c.item = new Domino(num.col, num.row, vertical);
                }
            }
            if (p.isArray("dots"))
            {
                NArray t = p.getAsArray("dots");
                for (int i = 0, n = t.size(); i < n; i++)
                {
                    NObject row = t.getAsObject(i);
                    Pt pt = pt(row.getAsArray("p"));
                    int color = row.getAsInteger("color");
                    String letter = row.getAsString("letter");
                    
                    Cell c = grid.cell(pt.col, pt.row);
                    c.item = new Dot(color, letter);
                }
            }
            if (p.isArray("bombs"))
            {
                NArray t = p.getAsArray("bombs");
                for (int i = 0, n = t.size(); i < n; i++)
                {
                    NObject row = t.getAsObject(i);
                    Pt pt = pt(row.getAsArray("p"));
                    int color = row.getAsInteger("color");
                    String letter = null;
                    if (row.isString("letter"))
                        letter = row.getAsString("letter");
                    int strength = row.getAsInteger("strength");
                    
                    Cell c = grid.cell(pt.col, pt.row);
                    c.item = new DotBomb(new Dot(color, letter), strength);
                }
            }
            if (p.isArray("turners"))
            {
                NArray t = p.getAsArray("turners");
                for (int i = 0, n = t.size(); i < n; i++)
                {
                    NObject row = t.getAsObject(i);
                    Pt pt = pt(row.getAsArray("p"));
                    int turn = row.getAsInteger("n");
                    
                    Cell c = grid.cell(pt.col, pt.row);
                    c.item = new Turner(turn);
                }
            }
            if (p.isArray("keys"))
            {
                NArray t = p.getAsArray("keys");
                for (int i = 0, n = t.size(); i < n; i++)
                {
                    NObject row = t.getAsObject(i);
                    Pt pt = pt(row.getAsArray("p"));
                    
                    Cell c = grid.cell(pt.col, pt.row);
                    c.item = new Key();
                }
            }
            if (p.isArray("eggs"))
            {
                NArray t = p.getAsArray("eggs");
                for (int i = 0, n = t.size(); i < n; i++)
                {
                    NObject row = t.getAsObject(i);
                    Pt pt = pt(row.getAsArray("p"));
                    boolean cracked = row.isBoolean("cracked");
                    
                    Cell c = grid.cell(pt.col, pt.row);
                    c.item = new Egg(cracked);
                }
            }
            
            NArray itemStr = p.getAsArray("items");            
            parseItems(cfg.grid, itemStr);
            
            if (p.isArray("lazySusans"))
            {
                NArray t = p.getAsArray("lazySusans");
                for (int i = 0, n = t.size(); i < n; i++)
                {
                    NObject row = t.getAsObject(i);
                    Pt pt = pt(row.getAsArray("p"));
                    boolean cw = row.getAsBoolean("clockwise");
                    
                    LazySusan s = new LazySusan(pt.col, pt.row, cw);
                    grid.add(s);
                }
            }
            
            if (p.isObject("generator"))
            {
                cfg.generator = generator(p.getAsObject("generator"));
            }              
            
            cfg.goals = parseGoal(p.getAsObject("goals"));
            cfg.boosts = parseBoosts(p.getAsObject("boosts"));
            
            return cfg;        
        }
        catch (Exception e)
        {
            Debug.p("error parsing level" + e.getMessage());
            new ErrorWindow(e);
            return null;
        }
    }

    private Boosts parseBoosts(NObject json)
    {
        Boosts boosts = new Boosts();
        if (json != null)
        {
            if (json.isInteger("turners"))
                boosts.turners = json.getAsInteger("turners");
            if (json.isInteger("drops"))
                boosts.drops = json.getAsInteger("drops");
            if (json.isInteger("picks"))
                boosts.picks = json.getAsInteger("picks");
            if (json.isInteger("colorBombs"))
                boosts.colorBombs = json.getAsInteger("colorBombs");
            if (json.isInteger("wildCards"))
                boosts.wildCards = json.getAsInteger("wildCards");
            if (json.isInteger("explodies"))
                boosts.explodies = json.getAsInteger("explodies");
            if (json.isInteger("reshuffles"))
                boosts.reshuffles = json.getAsInteger("reshuffles");
            if (json.isInteger("keys"))
                boosts.keys = json.getAsInteger("keys");
        }
        return boosts;
    }
    
    private Goal parseGoal(NObject json)
    {
        Goal goal = new Goal();
        if (json == null)
            return goal;
        
        NObject dots = json.getAsObject("dots");
        if (dots != null)
        {
            for (String key : dots.keys())
            {
                int color = Integer.parseInt(key);
                int need = dots.getAsInteger(key);
                goal.setDots(color, need);
            }
        }
        
        if (json.isInteger("animals"))
            goal.setAnimals(json.getAsInteger("animals"));

        if (json.isInteger("knights"))
            goal.setKnights(json.getAsInteger("knights"));
        
        if (json.isInteger("clocks"))
            goal.setClocks(json.getAsInteger("clocks"));
        
        if (json.isInteger("fire"))
            goal.setFire(json.getAsInteger("fire"));
        
        if (json.isInteger("ice"))
            goal.setIce(json.getAsInteger("ice"));
        
        if (json.isInteger("anchors"))
            goal.setAnchors(json.getAsInteger("anchors"));
        
        if (json.isInteger("maxMoves"))
            goal.setMaxMoves(json.getAsInteger("maxMoves"));
        
        if (json.isInteger("doors"))
            goal.setDoors(json.getAsInteger("doors"));

        if (json.isInteger("circuits"))
            goal.setCircuits(json.getAsInteger("circuits"));

        if (json.isInteger("lasers"))
            goal.setLasers(json.getAsInteger("lasers"));
        
        if (json.isInteger("birds"))
            goal.setBirds(json.getAsInteger("birds"));

        if (json.isInteger("dominoes"))
            goal.setDominoes(json.getAsInteger("dominoes"));

        if (json.isInteger("mirrors"))
            goal.setMirrors(json.getAsInteger("mirrors"));

        if (json.isInteger("rockets"))
            goal.setRockets(json.getAsInteger("rockets"));

        if (json.isInteger("score"))
            goal.setScore(json.getAsInteger("score"));
        
        if (json.isInteger("time"))
            goal.setTime(json.getAsInteger("time"));

        return goal;
    }

    private int parseDirection(String d)
    {
        if (d == null)
            return Direction.NONE;
        
        if (d.equals("W"))
            return Direction.WEST;
        if (d.equals("E"))
            return Direction.EAST;
        if (d.equals("N"))
            return Direction.NORTH;
        if (d.equals("S"))
            return Direction.SOUTH;
        
        return Direction.NONE;
    }

    private Pt pt(NArray a)
    {
        return new Pt(a.getAsInteger(0), a.getAsInteger(1));
    }

    protected void parseGrid(GridState grid, NArray arr)
    {
        for (int row = 0; row < grid.numRows; row++)
        {
            String str = arr.getAsString(row);
            for (int col = 0; col < grid.numColumns; col++)
            {
                char c = str.charAt(col);
                Cell cell;
                switch (c)
                {
                    case ' ': cell = new Hole(); grid.setCell(col, row, cell); break;
                    case 'X': cell = new Rock(); grid.setCell(col, row, cell); break;
                    case 'L': cell = new Slide(true); grid.setCell(col, row, cell); break;
                    case 'R': cell = new Slide(false); grid.setCell(col, row, cell); break;
                    case '-': cell = new Teleport(false); grid.setCell(col, row, cell); break;
                    case '+': cell = new Teleport(true); grid.setCell(col, row, cell); break;
                    case 'C': cell = new ChangeColorCell(); grid.setCell(col, row, cell); break;
                    case 'E': cell = new CircuitCell(); grid.setCell(col, row, cell); break;
                    default:  cell = new ItemCell(); grid.setCell(col, row, cell); break;
                }
            }
        }
    }

    protected void parseItems(GridState grid, NArray arr)
    {
        if (arr == null)
            return;
        
        for (int row = 0; row < grid.numRows; row++)
        {
            String str = arr.getAsString(row);
            for (int col = 0; col < grid.numColumns; col++)
            {
                char c = str.charAt(col);
                Item item = null;
                switch (c)
                {
                    case 'W': item = new Wild(); break;
                    case 'F': item = new Fire(); break;
                    case 'A': item = new Anchor(); break;
                    case 'Y': item = new YinYang(); break;
                    case '?': item = new RandomItem(); break;
                    default: 
                        if (Character.isDigit(c))
                        {
                            int color = (int) (c - '0');
                            item = new Dot(color);
                        }
                        break;
                }
                if (item != null)
                    grid.cell(col, row).item = item;
            }
        }
    }
    
    protected void parseIce(GridState grid, NArray arr)
    {
        for (int row = 0; row < grid.numRows; row++)
        {
            String str = arr.getAsString(row);
            for (int col = 0; col < grid.numColumns; col++)
            {
                char c = str.charAt(col);
                if (Character.isDigit(c))
                {
                    int count = (int) (c - '0');
                    grid.cell(col, row).ice = count;
                }
            }
        }
    }
    
    protected Generator generator(NObject p)
    {
        Generator g = new Generator();
        if (p.isInteger("seed"))
            g.setSeed(p.getAsInteger("seed"));

        if (p.isBoolean("generateLetters"))
            g.generateLetters = p.getAsBoolean("generateLetters");
        
        if (p.isBoolean("swapMode"))
            g.swapMode = p.getAsBoolean("swapMode");

        if (p.isBoolean("rollMode"))
            g.rollMode = p.getAsBoolean("rollMode");

        if (p.isBoolean("dominoMode"))
            g.dominoMode = p.getAsBoolean("dominoMode");

        if (p.isInteger("fireGrowthRate"))
            g.fireGrowthRate = p.getAsInteger("fireGrowthRate");
        
        if (p.isInteger("maxAnchors"))
            g.maxAnchors = p.getAsInteger("maxAnchors");
        
        if (p.isInteger("maxDomino"))
            g.maxDomino = p.getAsInteger("maxDomino");
        
        if (p.isInteger("animalStrength"))
            g.animalStrength = p.getAsInteger("animalStrength");
        
        if (p.isInteger("animalStrength"))
            g.animalStrength = p.getAsInteger("animalStrength");
        
        if (p.isInteger("clockStrength"))
            g.clockStrength = p.getAsInteger("clockStrength");
        
        if (p.isInteger("bombStrength"))
            g.bombStrength = p.getAsInteger("bombStrength");
        
        if (p.isBoolean("initialDotsOnly"))
            g.initialDotsOnly = p.getAsBoolean("initialDotsOnly");
        
        if (p.isString("animalType"))
            g.animalType = Animal.Type.fromName((p.getAsString("animalType")));
        
        NArray freq = p.getAsArray("freq");
        for (int i = 0, n = freq.size(); i < n; i++)
        {
            NArray a = freq.getAsArray(i);
            double f = a.getAsDouble(0);
            if (a.isString(1))
            {
                String item = a.getAsString(1);                
                if (item.equals("dot"))
                {
                    if (a.isInteger(2))
                    {
                        g.add(new ItemFrequency(new Dot(a.getAsInteger(2)), f));
                    }
                    else if (a.isArray(2))
                    {
                        NArray colors = a.getAsArray(2);
                        for (int j = 0; j < colors.size(); j++)
                        {
                            int color = colors.getAsInteger(j);
                            g.add(new ItemFrequency(new Dot(color), f));
                        }
                    }
                }
                else if (item.equals("fire"))
                {
                    g.add(new ItemFrequency(new Fire(), f));
                }
                else if (item.equals("anchor"))
                {
                    g.add(new ItemFrequency(new Anchor(), f));
                }
                else if (item.equals("yinyang"))
                {
                    g.add(new ItemFrequency(new YinYang(), f));
                }
                else if (item.equals("egg"))
                {
                    g.add(new ItemFrequency(new Egg(), f));
                }
                else if (item.equals("domino"))
                {
                    g.add(new ItemFrequency(new Domino(), f));
                }
                else if (item.equals("wild"))
                {
                    g.add(new ItemFrequency(new Wild(), f));
                }
                else if (item.equals("bomb"))
                {
                    g.add(new ItemFrequency(new DotBomb(new Dot(0), g.bombStrength), f));
                }
                else if (item.equals("animal"))
                {
                    if (a.isInteger(2))
                    {
                        g.add(new ItemFrequency(new Animal(a.getAsInteger(2), g.animalStrength, g.animalType), f));
                    }
                    else if (a.isArray(2))
                    {
                        NArray colors = a.getAsArray(2);
                        for (int j = 0; j < colors.size(); j++)
                        {
                            int color = colors.getAsInteger(j);
                            g.add(new ItemFrequency(new Animal(color, g.animalStrength, g.animalType), f));
                        }
                    }
                }
                else if (item.equals("knight"))
                {
                    g.add(new ItemFrequency(new Knight(g.knightStrength), f));
                }
                else if (item.equals("clock"))
                {
                    g.add(new ItemFrequency(new Clock(g.clockStrength), f));
                }
                else if (item.equals("drop"))
                {
                    g.add(new ItemFrequency(new Drop(), f));
                }
                else if (item.equals("pick"))
                {
                    g.add(new ItemFrequency(new IcePick(), f));
                }
                else if (item.equals("colorBomb"))
                {
                    g.add(new ItemFrequency(new ColorBomb(), f));
                }
                else if (item.equals("mirror"))
                {
                    g.add(new ItemFrequency(new Mirror(false), f));
                }
                else if (item.equals("laser"))
                {
                    g.add(new ItemFrequency(new Laser(Direction.EAST), f));
                }
                else if (item.equals("rocket"))
                {
                    g.add(new ItemFrequency(new Rocket(Direction.EAST), f));
                }
                else if (item.equals("turner"))
                {
                    g.add(new ItemFrequency(new Turner(1), f));
                }
                else if (item.equals("key"))
                {
                    g.add(new ItemFrequency(new Key(), f));
                }
            }
        }
        return g;
    }

    public static NObject toJson(Config c)
    {
        NObject p = new NObject();
        
        if (c.id != Config.UNDEFINED_ID)
            p.put("id", c.id);
        
        p.put("name", c.name);
        p.put("creator", c.creator);
        p.put("description", c.description);
        p.put("folder", c.folder);
        p.put("numColumns", c.numColumns);
        p.put("numRows", c.numRows);
        
        NArray grid = new NArray();
        NArray ice = new NArray();
        NArray items = new NArray();
        NArray teleporters = new NArray();
        NArray animals = new NArray();
        NArray knights = new NArray();
        NArray clocks = new NArray();
        NArray drops = new NArray();
        NArray picks = new NArray();
        NArray colorBombs = new NArray();
        NArray doors = new NArray();
        NArray cages = new NArray();
        NArray conveyors = new NArray();
        NArray mirrors = new NArray();
        NArray lasers = new NArray();
        NArray rockets = new NArray();
        NArray dots = new NArray();
        NArray bombs = new NArray();
        NArray dominoes = new NArray();
        NArray turners = new NArray();
        NArray keys = new NArray();
        NArray eggs = new NArray();
        
        for (int row = 0; row < c.numRows; row++)
        {
            String gridLine = "";
            String iceLine = "";
            String itemLine = "";

            for (int col = 0; col < c.numColumns; col++)
            {
                Cell cell = c.grid.cell(col, row);
                if (cell instanceof Hole)
                {
                    gridLine += " ";
                }
                else if (cell instanceof Rock)
                {
                    gridLine += "X";
                }
                else if (cell instanceof ChangeColorCell)
                {
                    gridLine += "C";
                }
                else if (cell instanceof CircuitCell)
                {
                    gridLine += "E";
                }
                else if (cell instanceof Slide)
                {
                    Slide slide = (Slide) cell;
                    gridLine += slide.isToLeft() ? 'L' : 'R';
                }
                else if (cell instanceof Teleport)
                {
                    Teleport tr = (Teleport) cell;
                    gridLine += tr.isTarget() ? '+' : '-';
                    
                    if (tr.isTarget())
                    {
                        NObject trans = new NObject();
                        trans.put("from", pt(cell.col, cell.row));
                        trans.put("to", pt(tr.getOtherCol(), tr.getOtherRow()));
                        teleporters.push(trans);
                    }
                }
                else if (cell instanceof Door)
                {
                    Door b = (Door) cell;
                    NObject a = new NObject();
                    a.put("p", pt(col, row));
                    a.put("strength", b.getStrength());
                    
                    setDirection(a, b.getDirection());
                    
                    if (b.getDirection() != Direction.NONE)
                    {
                        int rotDir = b.getRotationDirection();
                        if (rotDir != 0)
                        {
                            a.put("rotate", rotDir);
                        }
                    }
                    gridLine += '.';
                    doors.push(a);
                }
                else if (cell instanceof Cage)
                {
                    Cage b = (Cage) cell;
                    NObject a = new NObject();
                    a.put("p", pt(col, row));
                    a.put("strength", b.getStrength());
                    
                    gridLine += '.';
                    cages.push(a);
                }
                else if (cell instanceof ConveyorCell)
                {
                    ConveyorCell b = (ConveyorCell) cell;
                    NObject a = new NObject();
                    a.put("p", pt(col, row));
                    a.put("turn", b.getTurn());                    
                    setDirection(a, b.getDirection());
                    
                    gridLine += '.';
                    conveyors.push(a);
                }
                else if (cell instanceof ItemCell)
                {
                    gridLine += ".";
                }
                              
                char ch = '.';
                if (cell.item instanceof Animal)
                {
                    Animal an = (Animal) cell.item;
                    NObject a = new NObject();
                    a.put("p", pt(col, row));
                    a.put("strength", an.getStrength());
                    a.put("color", an.getColor());
                    a.put("type", an.getType().getName());
                    animals.push(a);
                }
                else if (cell.item instanceof Knight)
                {
                    Knight an = (Knight) cell.item;
                    NObject a = new NObject();
                    a.put("p", pt(col, row));
                    a.put("strength", an.getStrength());
                    knights.push(a);
                }
                else if (cell.item instanceof Clock)
                {
                    Clock an = (Clock) cell.item;
                    NObject a = new NObject();
                    a.put("p", pt(col, row));
                    a.put("strength", an.getStrength());
                    clocks.push(a);
                }
                else if (cell.item instanceof Drop)
                {
                    Drop an = (Drop) cell.item;
                    NObject a = new NObject();
                    a.put("p", pt(col, row));
                    drops.push(a);
                }
                else if (cell.item instanceof IcePick)
                {
                    NObject a = new NObject();
                    a.put("p", pt(col, row));
                    picks.push(a);
                }
                else if (cell.item instanceof ColorBomb)
                {
                    NObject a = new NObject();
                    a.put("p", pt(col, row));
                    colorBombs.push(a);
                }
                else if (cell.item instanceof Rocket)
                {
                    Rocket an = (Rocket) cell.item;
                    NObject a = new NObject();
                    a.put("p", pt(col, row));
                    setDirection(a, an.getDirection());
                    rockets.push(a);
                }
                else if (cell.item instanceof Laser)
                {
                    Laser an = (Laser) cell.item;
                    NObject a = new NObject();
                    a.put("p", pt(col, row));
                    setDirection(a, an.getDirection());
                    lasers.push(a);
                }
                else if (cell.item instanceof Mirror)
                {
                    Mirror an = (Mirror) cell.item;
                    NObject a = new NObject();
                    a.put("p", pt(col, row));
                    a.put("flip", an.isFlipped());
                    mirrors.push(a);
                }
                else if (cell.item instanceof Domino)
                {
                    Domino an = (Domino) cell.item;
                    NObject a = new NObject();
                    a.put("p", pt(col, row));
                    a.put("num", pt(an.num[0], an.num[1]));
                    a.put("vertical", an.vertical);
                    dominoes.push(a);
                }
                else if (cell.item instanceof Turner)
                {
                    Turner an = (Turner) cell.item;
                    NObject a = new NObject();
                    a.put("p", pt(col, row));
                    a.put("n", an.n);
                    turners.push(a);
                }
                else if (cell.item instanceof Key)
                {
                    NObject a = new NObject();
                    a.put("p", pt(col, row));
                    keys.push(a);
                }
                else if (cell.item instanceof Egg)
                {
                    Egg an = (Egg) cell.item;
                    NObject a = new NObject();
                    a.put("p", pt(col, row));
                    if (an.isCracked())
                        a.put("cracked", true);
                    eggs.push(a);
                }
                else if (cell.item instanceof Dot)
                {
                    Dot dot = (Dot) cell.item;
                    if (dot.letter == null)
                    {
                        ch = (char) ('0' + dot.color);
                    }
                    else
                    {
                        NObject a = new NObject();
                        a.put("p", pt(col, row));
                        a.put("color", dot.color);
                        a.put("letter", dot.letter);
                        dots.push(a);
                    }
                }
                else if (cell.item instanceof DotBomb)
                {
                    DotBomb bomb = (DotBomb) cell.item;
                    Dot dot = bomb.getDot();
                    
                    NObject a = new NObject();
                    a.put("p", pt(col, row));
                    a.put("color", dot.color);
                    if (dot.letter != null)
                        a.put("letter", dot.letter);
                    a.put("strength", bomb.getStrength());
                    bombs.push(a);                    
                }
                else if (cell.item instanceof Fire)
                    ch = 'F';
                else if (cell.item instanceof Wild)
                    ch = 'W';
                else if (cell.item instanceof Anchor)
                    ch = 'A';
                else if (cell.item instanceof RandomItem)
                    ch = '?';
                else if (cell.item instanceof YinYang)
                    ch = 'Y';
                
                itemLine += ch;                
                
                iceLine += cell.ice == 0 ? '.' : (char) ('0' + cell.ice);
            }
            grid.push(gridLine);
            ice.push(iceLine);
            items.push(itemLine);
        }
        
        NArray lazySusans = new NArray();
        for (LazySusan su : c.grid.getLazySusans())
        {
            NObject a = new NObject();
            a.put("p", pt(su.getCol(), su.getRow()));
            a.put("clockwise", su.isClockwise());
            lazySusans.push(a);
        }

        p.put("grid", grid);
        p.put("ice", ice);
        p.put("items", items);
        add(p, "teleporters", teleporters);
        add(p, "doors", doors);
        add(p, "cages", cages);
        add(p, "conveyors", conveyors);
        add(p, "lazySusans", lazySusans);
        add(p, "dots", dots);
        add(p, "bombs", bombs);
        add(p, "animals", animals);
        add(p, "knights", knights);
        add(p, "clocks", clocks);
        add(p, "drops", drops);
        add(p, "picks", picks);
        add(p, "colorBombs", colorBombs);
        add(p, "lasers", lasers);
        add(p, "mirrors", mirrors);
        add(p, "rockets", rockets);
        add(p, "dominoes", dominoes);
        add(p, "turners", turners);
        add(p, "keys", keys);
        add(p, "eggs", eggs);
        p.put("generator", generator(c.generator));        
        p.put("goals", goal(c.goals));
        p.put("boosts", boosts(c.boosts));
        
        return p;
    }
    
    protected static void add(NObject p, String name, NArray ar)
    {
        if (ar != null && ar.size() > 0)
            p.put(name,  ar);
    }
    
    protected static void setDirection(NObject a, int direction)
    {
        switch (direction)
        {
            case Direction.NORTH: a.put("direction", "N"); break;
            case Direction.SOUTH: a.put("direction", "S"); break;
            case Direction.EAST: a.put("direction", "E"); break;
            case Direction.WEST: a.put("direction", "W"); break;
        }
    }

    protected static NObject boosts(Boosts b)
    {
        NObject p = new NObject();
        
        if (b.turners != 0)
            p.put("turners", b.turners);
        if (b.drops != 0)
            p.put("drops", b.drops);
        if (b.picks != 0)
            p.put("picks", b.picks);
        if (b.colorBombs != 0)
            p.put("colorBombs", b.colorBombs);
        if (b.wildCards != 0)
            p.put("wildCards", b.wildCards);
        if (b.explodies != 0)
            p.put("explodies", b.explodies);
        if (b.reshuffles != 0)
            p.put("reshuffles", b.reshuffles);
        if (b.keys != 0)
            p.put("keys", b.keys);
        
        return p;
    }
    
    protected static NObject goal(Goal g)
    {
        NObject p = new NObject();
        
        NObject dots = new NObject();
        int[] needDots = g.getDots();
        for (int i = 0; i < needDots.length; i++)
        {
            if (needDots[i] != 0)
                dots.put("" + i, needDots[i]);
        }
        p.put("dots", dots);
        
        if (g.getAnimals() != 0)
            p.put("animals", g.getAnimals());
        
        if (g.getKnights() != 0)
            p.put("knights", g.getKnights());
        
        if (g.getClocks() != 0)
            p.put("clocks", g.getClocks());
        
        if (g.getAnchors() != 0)
            p.put("anchors", g.getAnchors());
        
        if (g.getFire() != 0)
            p.put("fire", g.getFire());
        
        if (g.getCircuits() != 0)
            p.put("circuits", g.getCircuits());
        
        if (g.getLasers() != 0)
            p.put("lasers", g.getLasers());
        
        if (g.getBirds() != 0)
            p.put("birds", g.getBirds());
        
        if (g.getDominoes() != 0)
            p.put("dominoes", g.getDominoes());
        
        if (g.getMirrors() != 0)
            p.put("mirrors", g.getMirrors());
        
        if (g.getRockets() != 0)
            p.put("rockets", g.getRockets());
        
        if (g.getMaxMoves() != 0)
            p.put("maxMoves", g.getMaxMoves());
        
        if (g.getIce() != 0)
            p.put("ice", g.getIce());
        
        if (g.getDoors() != 0)
            p.put("doors", g.getDoors());
        
        if (g.getScore() != 0)
            p.put("score", g.getScore());

        if (g.getTime() != 0)
            p.put("time", g.getTime());
                
        return p;
    }
    
    protected static NObject generator(Generator g)
    {
        NObject p = new NObject();
        p.put("generateLetters", g.generateLetters);
        p.put("swapMode", g.swapMode);
        p.put("rollMode", g.rollMode);
        p.put("dominoMode", g.dominoMode);
        p.put("seed", g.getSeed());
        p.put("fireGrowthRate", g.fireGrowthRate);
        p.put("maxAnchors", g.maxAnchors);
        p.put("maxDomino", g.maxDomino);
        p.put("initialDotsOnly", g.initialDotsOnly);
        p.put("animalStrength", g.animalStrength);
        p.put("knightStrength", g.knightStrength);
        p.put("clockStrength", g.clockStrength);
        p.put("bombStrength", g.bombStrength);
        p.put("animalType", g.animalType.getName());
        
        NArray freq = new NArray();
        for (ItemFrequency f : g.getFrequencies())
        {
            NArray q = new NArray();
            q.push(f.frequency);
            
            addItem(q, f.item);
            
            freq.push(q);
        }
        p.put("freq", freq);
        
        return p;
    }
        
    protected static void addItem(NArray a, Item item)
    {
        if (item instanceof Fire)
            a.push("fire");
        else if (item instanceof Anchor)
            a.push("anchor");
        else if (item instanceof Wild)
            a.push("wild");
        else if (item instanceof RandomItem)
            a.push("random");
        else if (item instanceof Knight)
            a.push("knight");
        else if (item instanceof Clock)
            a.push("clock");
        else if (item instanceof Turner)
            a.push("turner");
        else if (item instanceof Key)
            a.push("key");
        else if (item instanceof Drop)
            a.push("drop");
        else if (item instanceof IcePick)
            a.push("pick");
        else if (item instanceof ColorBomb)
            a.push("colorBomb");
        else if (item instanceof Laser)
            a.push("laser");
        else if (item instanceof Mirror)
            a.push("mirror");
        else if (item instanceof Rocket)
            a.push("rocket");
        else if (item instanceof YinYang)
            a.push("yinyang");
        else if (item instanceof Egg)
            a.push("egg");
        else if (item instanceof DotBomb)
            a.push("bomb");
        else if (item instanceof Domino)
            a.push("domino");
        else if (item instanceof Dot)
        {
            a.push("dot");
            a.push(((Dot) item).getColor());
        }
        else if (item instanceof Animal)
        {
            a.push("animal");
            a.push(((Animal) item).getColor());
        }        
    }
    
    protected static NArray pt(int col, int row)
    {
        NArray a = new NArray();
        a.push(col);
        a.push(row);
        return a;
    }
}
