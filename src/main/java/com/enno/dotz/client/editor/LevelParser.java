package com.enno.dotz.client.editor;

import java.util.ArrayList;
import java.util.List;

import com.ait.tooling.nativetools.client.NArray;
import com.ait.tooling.nativetools.client.NObject;
import com.enno.dotz.client.Boosts;
import com.enno.dotz.client.Cell;
import com.enno.dotz.client.Cell.Bubble;
import com.enno.dotz.client.Cell.Cage;
import com.enno.dotz.client.Cell.ChangeColorCell;
import com.enno.dotz.client.Cell.CircuitCell;
import com.enno.dotz.client.Cell.ConveyorCell;
import com.enno.dotz.client.Cell.Door;
import com.enno.dotz.client.Cell.Hole;
import com.enno.dotz.client.Cell.ItemCell;
import com.enno.dotz.client.Cell.Machine;
import com.enno.dotz.client.Cell.Rock;
import com.enno.dotz.client.Cell.Slide;
import com.enno.dotz.client.Cell.Slot;
import com.enno.dotz.client.Cell.Teleport;
import com.enno.dotz.client.ChainGoal;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Direction;
import com.enno.dotz.client.Generator;
import com.enno.dotz.client.Generator.ItemFrequency;
import com.enno.dotz.client.Goal;
import com.enno.dotz.client.GridState;
import com.enno.dotz.client.LetterMultiplier;
import com.enno.dotz.client.SlotMachines;
import com.enno.dotz.client.SlotMachines.SlotMachineInfo;
import com.enno.dotz.client.anim.Pt;
import com.enno.dotz.client.item.Anchor;
import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.item.Blaster;
import com.enno.dotz.client.item.Blocker;
import com.enno.dotz.client.item.Bomb;
import com.enno.dotz.client.item.Chest;
import com.enno.dotz.client.item.Clock;
import com.enno.dotz.client.item.Coin;
import com.enno.dotz.client.item.ColorBomb;
import com.enno.dotz.client.item.Diamond;
import com.enno.dotz.client.item.Domino;
import com.enno.dotz.client.item.Dot;
import com.enno.dotz.client.item.DotBomb;
import com.enno.dotz.client.item.Drop;
import com.enno.dotz.client.item.Egg;
import com.enno.dotz.client.item.Explody;
import com.enno.dotz.client.item.Fire;
import com.enno.dotz.client.item.IcePick;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.Key;
import com.enno.dotz.client.item.Knight;
import com.enno.dotz.client.item.Laser;
import com.enno.dotz.client.item.LazySusan;
import com.enno.dotz.client.item.Mirror;
import com.enno.dotz.client.item.Pacman;
import com.enno.dotz.client.item.RandomItem;
import com.enno.dotz.client.item.Rocket;
import com.enno.dotz.client.item.Spider;
import com.enno.dotz.client.item.Striped;
import com.enno.dotz.client.item.Turner;
import com.enno.dotz.client.item.Wild;
import com.enno.dotz.client.item.WrappedDot;
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
            
            if (p.isArray("machines"))
            {
                NArray t = p.getAsArray("machines");
                for (int i = 0, n = t.size(); i < n; i++)
                {
                    NObject row = t.getAsObject(i);
                    Pt pt = pt(row.getAsArray("p"));
                    
                    int howMany = row.getAsInteger("howMany");
                    int every = row.getAsInteger("every");
                    String trigger = row.getAsString("trigger");
                    String type = row.getAsString("type");
                    String coinFreq = row.getAsString("coinFreq");
                    
                    Item launchItem = null;
                    NObject item = row.getAsObject("item");
                    if (item != null)
                    {
                        String itemClass = item.getAsString("class");
                        launchItem = parseItem(itemClass, item);
                    }
                    
                    Machine machine = new Machine(type, launchItem, every, howMany, trigger, coinFreq);
                    grid.setCell(pt.col, pt.row, machine);
                }
            }
            
            if (p.isArray("doors"))
            {
                NArray t = p.getAsArray("doors");
                for (int i = 0, n = t.size(); i < n; i++)
                {
                    NObject row = t.getAsObject(i);
                    Pt pt = pt(row.getAsArray("p"));
                    
                    Door door;
                    if (row.isDefined("seq"))
                    {
                        door = new Door(row.getAsString("seq"));
                    }
                    else
                    {
                        int strength = row.getAsInteger("strength");
                        int direction = parseDirection(row.getAsString("direction"));                    
                        
                        int rotDir = 0;
                        if (row.isInteger("rotate"))
                            rotDir = row.getAsInteger("rotate");
                        
                        door = new Door(strength, direction, rotDir);
                    }
                    grid.setCell(pt.col, pt.row, door);
                }
            }
            
            if (p.isArray("cages"))
            {
                NArray t = p.getAsArray("cages");
                for (int i = 0, n = t.size(); i < n; i++)
                {
                    NObject row = t.getAsObject(i);
                    Pt pt = pt(row.getAsArray("p"));
                    
                    Cage cage;
                    if (row.isDefined("strength"))
                    {
                        int strength = row.getAsInteger("strength");
                        boolean blocking = row.isBoolean("blocking") ? row.getAsBoolean("blocking") : false;
                        cage = new Cage(strength, blocking);
                    }
                    else
                    {
                        String seq = row.getAsString("seq");
                        cage = new Cage(seq);
                    }
                    grid.setCell(pt.col, pt.row, cage);
                }
            }
            if (p.isArray("slots"))
            {
                NArray t = p.getAsArray("slots");
                for (int i = 0, n = t.size(); i < n; i++)
                {
                    NObject row = t.getAsObject(i);
                    Pt pt = pt(row.getAsArray("p"));
                    
                    Slot slot = new Slot();
                    
                    SlotMachineInfo info = SlotMachineInfo.parseInfo(row.getAsObject("info"));
                    slot.setSlotMachineInfo(info);
                    
                    grid.setCell(pt.col, pt.row, slot);
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
            
            addItems("animal", p, grid);
            addItems("laser", p, grid);
            addItems("pacman", p, grid);
            addItems("rocket", p, grid);
            addItems("blocker", p, grid);
            addItems("blaster", p, grid);
            addItems("bomb", p, grid);
            addItems("mirror", p, grid);
            addItems("knight", p, grid);
            addItems("clock", p, grid);
            addItems("drop", p, grid);
            addItems("pick", p, grid);
            addItems("colorBomb", p, grid);
            addItems("dominoes", "domino", p, grid);
            addItems("dot", p, grid);
            addItems("dotBomb", p, grid);
            addItems("turner", p, grid);
            addItems("key", p, grid);
            addItems("egg", p, grid);
            addItems("spider", p, grid);
            addItems("chest", p, grid);
            addItems("fire", p, grid);
            addItems("wild", p, grid);
            addItems("anchor", p, grid);
            addItems("diamond", p, grid);
            addItems("yinyang", p, grid);
            addItems("random", p, grid);
            
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

    private void addItems(String singularType, NObject p, GridState grid)
    {
        addItems(singularType + "s", singularType, p, grid);
    }
    
    private void addItems(String pluralType, String singularType, NObject p, GridState grid)
    {
        if (p.isArray(pluralType))
        {
            NArray t = p.getAsArray(pluralType);
            for (int i = 0, n = t.size(); i < n; i++)
            {
                NObject row = t.getAsObject(i);
                Pt pt = pt(row.getAsArray("p"));                
                
                Cell c = grid.cell(pt.col, pt.row);
                c.item = parseItem(singularType, row);
            }
        }
    }
    
    public static Item parseItem(NObject row)
    {
        return parseItem(row.getAsString("class"), row);
    }
    
    public static Item parseItem(String itemClass, NObject row)
    {
        boolean stuck = row.isBoolean("stuck") ? row.getAsBoolean("stuck") : false;
        
        if (itemClass.equals("animal"))
        {
            int strength = row.getAsInteger("strength");
            int color = row.getAsInteger("color");
            String type = row.getAsString("type");
            String action = row.getAsString("action");
            return action == null ? new Animal(color, strength, Animal.Type.fromName(type), stuck) 
                    : new Animal(color, strength, Animal.Type.fromName(type), Animal.Action.fromName(action), stuck);
        }
        if (itemClass.equals("laser"))
        {
            int direction = parseDirection(row.getAsString("direction"));
            return new Laser(direction, stuck);
        }
        if (itemClass.equals("pacman"))
        {
            int direction = parseDirection(row.getAsString("direction"));
            return new Pacman(direction, stuck);
        }
        if (itemClass.equals("coin"))
        {
            int amount = row.getAsInteger("amount");
            return new Coin(amount, stuck);
        }
        if (itemClass.equals("rocket"))
        {
            int direction = parseDirection(row.getAsString("direction"));
            return new Rocket(direction, stuck);
        }
        if (itemClass.equals("blocker"))
        {
            int strength = row.isInteger("strength") ? row.getAsInteger("strength") : 1;
            boolean zapOnly = row.isBoolean("zapOnly") ? row.getAsBoolean("zapOnly") : false;
            return new Blocker(strength, stuck, zapOnly);
        }
        if (itemClass.equals("blaster"))
        {
            boolean vertical = row.getAsBoolean("vertical");
            return new Blaster(vertical, stuck);
        }
        if (itemClass.equals("bomb"))
        {
            int radius = row.getAsInteger("radius");
            return new Bomb(radius, stuck);
        }
        if (itemClass.equals("mirror"))
        {
            boolean flip = row.getAsBoolean("flip");
            return new Mirror(flip, stuck);
        }
        if (itemClass.equals("knight"))
        {
            int strength = row.getAsInteger("strength");
            return new Knight(strength, stuck);
        }
        if (itemClass.equals("clock"))
        {
            int strength = row.getAsInteger("strength");
            return new Clock(strength, stuck);
        }
        if (itemClass.equals("spider"))
        {
            int strength = row.getAsInteger("strength");
            return new Spider(strength, stuck);
        }
        if (itemClass.equals("drop"))
        {
            int radius = row.isDefined("radius") ? row.getAsInteger("radius") : 3;
            return new Drop(radius, stuck);
        }
        if (itemClass.equals("pick"))
        {
            int radius = row.isDefined("radius") ? row.getAsInteger("radius") : 3;
            return new IcePick(radius, stuck);
        }
        if (itemClass.equals("colorBomb"))
        {
            return new ColorBomb(stuck);
        }
        if (itemClass.equals("domino"))
        {
            Pt num = pt(row.getAsArray("num"));
            boolean vertical = row.getAsBoolean("vertical");
            return new Domino(num.col, num.row, vertical, stuck);
        }
        if (itemClass.equals("dot"))
        {
            int color = row.getAsInteger("color");
            String letter = row.getAsString("letter");
            boolean radioActive = row.isBoolean("radioActive") ? row.getAsBoolean("radioActive") : false;
            
            Dot dot = new Dot(color, letter, stuck, radioActive);

            NObject mult = row.getAsObject("mult");
            if (mult != null)
                dot.setLetterMultiplier(LetterMultiplier.fromJson(mult));

            return dot;
        }
        if (itemClass.equals("dotBomb"))
        {
            int color = row.getAsInteger("color");
            String letter = null;
            if (row.isString("letter"))
                letter = row.getAsString("letter");
            int strength = row.getAsInteger("strength");
            boolean radioActive = row.isBoolean("radioActive") ? row.getAsBoolean("radioActive") : false;
            
            Dot dot = new Dot(color, letter, stuck, radioActive);
            NObject mult = row.getAsObject("mult");
            if (mult != null)
                dot.setLetterMultiplier(LetterMultiplier.fromJson(mult));

            return new DotBomb(dot, strength, stuck);
        }
        if (itemClass.equals("turner"))
        {
            int turn = row.getAsInteger("n");
            return new Turner(turn, stuck);
        }
        if (itemClass.equals("explody"))
        {
            int radius = row.getAsInteger("radius");
            return new Explody(radius);
        }
        if (itemClass.equals("key"))
        {
            return new Key(stuck);
        }
        if (itemClass.equals("egg"))
        {
            boolean cracked = row.isBoolean("cracked");
            return new Egg(cracked, stuck);
        }
        if (itemClass.equals("fire"))
        {
            return new Fire(stuck);
        }
        if (itemClass.equals("wild"))
        {
            return new Wild(stuck);
        }
        if (itemClass.equals("anchor"))
        {
            return new Anchor(stuck);
        }
        if (itemClass.equals("diamond"))
        {
            return new Diamond();
        }
        if (itemClass.equals("yinyang"))
        {
            return new YinYang(stuck);
        }
        if (itemClass.equals("random"))
        {
            boolean radioActive = row.isBoolean("radioActive") ? row.getAsBoolean("radioActive") : false;
            return new RandomItem(stuck, radioActive);
        }
        if (itemClass.equals("chest"))
        {
            int strength = row.getAsInteger("strength");
                        
            NObject itemObj = row.getAsObject("item");
            Item item = itemObj == null ? null : parseItem(itemObj.getAsString("class"), itemObj); // chest can be empty
            return new Chest(item, strength, stuck);
        }
        //TODO striped, wrappedDot, 
        return null; // should never happen
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
        
        if (json.isInteger("diamonds"))
            goal.setDiamonds(json.getAsInteger("diamonds"));
        
        if (json.isInteger("maxMoves"))
            goal.setMaxMoves(json.getAsInteger("maxMoves"));
        
        if (json.isInteger("doors"))
            goal.setDoors(json.getAsInteger("doors"));

        if (json.isInteger("cages"))
            goal.setCages(json.getAsInteger("cages"));

        if (json.isInteger("bubbles"))
            goal.setBubbles(json.getAsInteger("bubbles"));

        if (json.isInteger("spiders"))
            goal.setSpiders(json.getAsInteger("spiders"));

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

        if (json.isInteger("colorBombs"))
            goal.setColorBombs(json.getAsInteger("colorBombs"));

        if (json.isInteger("bombs"))
            goal.setBombs(json.getAsInteger("bombs"));

        if (json.isInteger("blasters"))
            goal.setBlasters(json.getAsInteger("blasters"));

        if (json.isInteger("blockers"))
            goal.setBlockers(json.getAsInteger("blockers"));

        if (json.isInteger("zapBlockers"))
            goal.setZapBlockers(json.getAsInteger("zapBlockers"));

        if (json.isInteger("score"))
            goal.setScore(json.getAsInteger("score"));
        
        if (json.isInteger("words"))
            goal.setWords(json.getAsInteger("words"));
        
        if (json.isInteger("coins"))
            goal.setCoins(json.getAsInteger("coins"));
        
        if (json.isInteger("chests"))
            goal.setChests(json.getAsInteger("chests"));
        
        if (json.isInteger("striped"))
            goal.setStriped(json.getAsInteger("striped"));
        
        if (json.isInteger("wrappedDots"))
            goal.setWrappedDots(json.getAsInteger("wrappedDots"));
        
        if (json.isInteger("time"))
            goal.setTime(json.getAsInteger("time"));

        if (json.isObject("chainGoal"))
            goal.setChainGoal(new ChainGoal(json.getAsObject("chainGoal")));
        
        return goal;
    }

    private static int parseDirection(String d)
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

    private static Pt pt(NArray a)
    {
        return new Pt(a.getAsInteger(0), a.getAsInteger(1));
    }

    protected void parseGrid(GridState grid, NArray arr)
    {
        if (arr == null)
            return;
        
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
                    case 'B': cell = new Bubble(); grid.setCell(col, row, cell); break;
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
                    case 'W': item = new Wild(false); break;
                    case 'F': item = new Fire(false); break;
                    case 'A': item = new Anchor(false); break;
                    case 'Y': item = new YinYang(false); break;
                    case '?': item = new RandomItem(false, false); break;
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

        if (p.isBoolean("clickMode"))
            g.clickMode = p.getAsBoolean("clickMode");

        if (p.isBoolean("rollMode"))
            g.rollMode = p.getAsBoolean("rollMode");

        if (p.isBoolean("diagonalMode"))
            g.diagonalMode = p.getAsBoolean("diagonalMode");

        if (p.isBoolean("dominoMode"))
            g.dominoMode = p.getAsBoolean("dominoMode");
        
        if (p.isBoolean("slipperyAnchors"))
            g.slipperyAnchors = p.getAsBoolean("slipperyAnchors");

        if (p.isInteger("minChainLength"))
            g.minChainLength = p.getAsInteger("minChainLength");
        
        if (p.isInteger("fireGrowthRate"))
            g.fireGrowthRate = p.getAsInteger("fireGrowthRate");
        
        if (p.isInteger("maxAnchors"))
            g.maxAnchors = p.getAsInteger("maxAnchors");
        
        if (p.isInteger("maxDomino"))
            g.maxDomino = p.getAsInteger("maxDomino");
        
        if (p.isInteger("animalStrength"))
            g.animalStrength = p.getAsInteger("animalStrength");
        
        if (p.isInteger("clockStrength"))
            g.clockStrength = p.getAsInteger("clockStrength");
        
        if (p.isInteger("chestStrength"))
            g.chestStrength = p.getAsInteger("chestStrength");
        
        if (p.isInteger("bombStrength"))
            g.bombStrength = p.getAsInteger("bombStrength");
        
        if (p.isInteger("blockerStrength"))
            g.blockerStrength = p.getAsInteger("blockerStrength");
        
        if (p.isInteger("eggsNeeded"))
            g.eggsNeeded = p.getAsInteger("eggsNeeded");
        
        if (p.isBoolean("initialDotsOnly"))
            g.initialDotsOnly = p.getAsBoolean("initialDotsOnly");
        
        if (p.isString("animalType"))
            g.animalType = Animal.Type.fromName((p.getAsString("animalType")));
        
        if (p.isString("animalAction"))
            g.animalAction = Animal.Action.fromName((p.getAsString("animalAction")));
        
        if (p.isString("rewardStrategies"))
            g.rewardStrategies = p.getAsString("rewardStrategies");
        
        if (p.isInteger("icePickRadius"))
            g.icePickRadius = p.getAsInteger("icePickRadius");

        if (p.isInteger("dropRadius"))
            g.dropRadius = p.getAsInteger("dropRadius");

        if (p.isBoolean("removeLetters"))
            g.removeLetters = p.getAsBoolean("removeLetters");

        if (p.isBoolean("findWords"))
            g.findWords = p.getAsBoolean("findWords");

        if (p.isInteger("maxWordLength"))
            g.maxWordLength = p.getAsInteger("maxWordLength");
        
        if (p.isDouble("radioActivePct"))
            g.radioActivePct = p.getAsDouble("radioActivePct");
        
        if (p.isDouble("spiderGrowth"))
            g.spiderGrowth = p.getAsDouble("spiderGrowth");
        
        NArray freq = p.getAsArray("freq");
        List<ItemFrequency> freqList = new ArrayList<ItemFrequency>();
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
                        freqList.add(new ItemFrequency(new Dot(a.getAsInteger(2)), f));
                    }
                    else if (a.isArray(2))
                    {
                        NArray colors = a.getAsArray(2);
                        for (int j = 0; j < colors.size(); j++)
                        {
                            int color = colors.getAsInteger(j);
                            freqList.add(new ItemFrequency(new Dot(color), f));
                        }
                    }
                }
                else if (item.equals("dotBomb"))
                {
                    freqList.add(new ItemFrequency(new DotBomb(new Dot(0), g.bombStrength, false), f));
                }
                else if (item.equals("blocker"))
                {
                    freqList.add(new ItemFrequency(new Blocker(g.blockerStrength, false, false), f));
                }
                else if (item.equals("zapBlocker"))
                {
                    freqList.add(new ItemFrequency(new Blocker(g.blockerStrength, false, true), f));
                }
                else if (item.equals("animal"))
                {
                    if (a.isInteger(2))
                    {
                        freqList.add(new ItemFrequency(new Animal(a.getAsInteger(2), g.animalStrength, g.animalType, g.animalAction, false), f));
                    }
                    else if (a.isArray(2))
                    {
                        NArray colors = a.getAsArray(2);
                        for (int j = 0; j < colors.size(); j++)
                        {
                            int color = colors.getAsInteger(j);
                            freqList.add(new ItemFrequency(new Animal(color, g.animalStrength, g.animalType, g.animalAction, false), f));
                        }
                    }
                }
                else if (item.equals("knight"))
                {
                    freqList.add(new ItemFrequency(new Knight(g.knightStrength, false), f));
                }
                else if (item.equals("clock"))
                {
                    freqList.add(new ItemFrequency(new Clock(g.clockStrength, false), f));
                }
                else
                {
                    freqList.add(new ItemFrequency(createItem(item), f));
                }
            }
            g.setFrequencies(freqList);
        }
        return g;
    }
    
    public static Item createItem(String item)
    {
        if (item.equals("fire"))
        {
            return new Fire(false);
        }
        else if (item.equals("anchor"))
        {
            return new Anchor(false);
        }
        else if (item.equals("diamond"))
        {
            return new Diamond();
        }
        else if (item.equals("yinyang"))
        {
            return new YinYang(false);
        }
        else if (item.equals("egg"))
        {
            return new Egg();
        }
        else if (item.equals("spider"))
        {
            return new Spider();
        }
        else if (item.equals("domino"))
        {
            return new Domino();
        }
        else if (item.equals("wild"))
        {
            return new Wild(false);
        }
        else if (item.equals("wrappedDot"))
        {
            return new WrappedDot(0);
        }
        else if (item.equals("striped"))
        {
            return new Striped(0, false);
        }
        else if (item.equals("blaster"))
        {
            return new Blaster(true, false);
        }
        else if (item.equals("bomb"))
        {
            return new Bomb();
        }
        else if (item.equals("drop"))
        {
            return new Drop();
        }
        else if (item.equals("pick"))
        {
            return new IcePick();
        }
        else if (item.equals("colorBomb"))
        {
            return new ColorBomb(false);
        }
        else if (item.equals("mirror"))
        {
            return new Mirror(false, false);
        }
        else if (item.equals("laser"))
        {
            return new Laser(Direction.EAST, false);
        }
        else if (item.equals("rocket"))
        {
            return new Rocket(Direction.EAST, false);
        }
        else if (item.equals("turner"))
        {
            return new Turner(1, false);
        }
        else if (item.equals("key"))
        {
            return new Key(false);
        }
        else if (item.equals("chest"))
        {
            return new Chest(new RandomItem(), 1);
        }
        
        return null;
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
        NArray machines = new NArray();
        NArray animals = new NArray();
        NArray knights = new NArray();
        NArray clocks = new NArray();
        NArray drops = new NArray();
        NArray picks = new NArray();
        NArray colorBombs = new NArray();
        NArray doors = new NArray();
        NArray cages = new NArray();
        NArray slots = new NArray();
        NArray conveyors = new NArray();
        NArray mirrors = new NArray();
        NArray lasers = new NArray();
        NArray rockets = new NArray();
        NArray dots = new NArray();
        NArray dotBombs = new NArray();
        NArray dominoes = new NArray();
        NArray turners = new NArray();
        NArray keys = new NArray();
        NArray eggs = new NArray();
        NArray spiders = new NArray();
        NArray blockers = new NArray();
        NArray blasters = new NArray();
        NArray bombs = new NArray();
        NArray chests = new NArray();
        NArray wilds = new NArray();
        NArray fires = new NArray();
        NArray anchors = new NArray();
        NArray diamonds = new NArray();
        NArray yinyangs = new NArray();
        NArray randoms = new NArray();
        NArray pacmans = new NArray();
        
        SlotMachines slotMachines = new SlotMachines();
        try
        {
            slotMachines.locateSlotMachines(c.grid);
        }
        catch (Exception e)
        {
            // can't happen
        }
        
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
                else if (cell instanceof Bubble)
                {
                    gridLine += "B";
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
                    if (b.isBlinking())
                    {
                        a.put("seq", b.getSequence());
                    }
                    else
                    {
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
                    }
                    
                    gridLine += '.';
                    doors.push(a);
                }
                else if (cell instanceof Cage)
                {
                    Cage b = (Cage) cell;
                    NObject a = new NObject();
                    a.put("p", pt(col, row));
                    
                    if (b.isBlinking())
                        a.put("seq", b.getSequence());
                    else
                    {
                        a.put("strength", b.getStrength());
                        
                        if (b.isBlocking())
                            a.put("blocking", true);
                    }
                    
                    gridLine += '.';
                    cages.push(a);
                }
                else if (cell instanceof Slot)
                {
                    Slot b = (Slot) cell;
                    NObject a = new NObject();
                    a.put("p", pt(col, row));
                    
                    if (slotMachines.isLeftSlot(b))   // save info of leftmost slot
                    {
                        if (b.getSlotMachineInfo() != null)
                            a.put("info", b.getSlotMachineInfo().asNObject());
                    }
                    
                    gridLine += '.';
                    slots.push(a);
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
                else if (cell instanceof Machine)
                {
                    Machine b = (Machine) cell;
                    NObject a = new NObject();
                    a.put("p", pt(col, row));
                    a.put("every", b.getEvery());
                    a.put("howMany", b.getHowMany());
                    a.put("trigger", b.getTrigger().name);
                    a.put("type", b.getMachineType().name);
                    
                    String coinFreq = b.getCoinFrequencies();
                    if (coinFreq != null)
                        a.put("coinFreq", coinFreq);
                    
                    Item launchItem = b.getLaunchItem();
                    if (launchItem != null)
                    {
                        NObject item = toJson(launchItem, true);
                        a.put("item", item);
                    }
                    
                    gridLine += '.';
                    machines.push(a);
                }
                else if (cell instanceof ItemCell)
                {
                    gridLine += ".";
                }
                              
                char ch = '.';
                if (cell.item instanceof Animal)
                {
                    animals.push(toJson(cell.item, col, row));
                }
                else if (cell.item instanceof Knight)
                {
                    knights.push(toJson(cell.item, col, row));
                }
                else if (cell.item instanceof Clock)
                {
                    clocks.push(toJson(cell.item, col, row));
                }
                else if (cell.item instanceof Drop)
                {
                    drops.push(toJson(cell.item, col, row));
                }
                else if (cell.item instanceof IcePick)
                {
                    picks.push(toJson(cell.item, col, row));
                }
                else if (cell.item instanceof ColorBomb)
                {
                    colorBombs.push(toJson(cell.item, col, row));
                }
                else if (cell.item instanceof Rocket)
                {
                    rockets.push(toJson(cell.item, col, row));
                }
                else if (cell.item instanceof Blocker)
                {
                    blockers.push(toJson(cell.item, col, row));
                }
                else if (cell.item instanceof Blaster)
                {
                    blasters.push(toJson(cell.item, col, row));
                }
                else if (cell.item instanceof Laser)
                {
                    lasers.push(toJson(cell.item, col, row));
                }
                else if (cell.item instanceof Pacman)
                {
                    pacmans.push(toJson(cell.item, col, row));
                }
                else if (cell.item instanceof Mirror)
                {
                    mirrors.push(toJson(cell.item, col, row));
                }
                else if (cell.item instanceof Domino)
                {
                    dominoes.push(toJson(cell.item, col, row));
                }
                else if (cell.item instanceof Turner)
                {
                    turners.push(toJson(cell.item, col, row));
                }
                else if (cell.item instanceof Bomb)
                {
                    bombs.push(toJson(cell.item, col, row));
                }
                else if (cell.item instanceof Key)
                {
                    keys.push(toJson(cell.item, col, row));
                }
                else if (cell.item instanceof Egg)
                {
                    eggs.push(toJson(cell.item, col, row));
                }
                else if (cell.item instanceof Spider)
                {
                    spiders.push(toJson(cell.item, col, row));
                }
                else if (cell.item instanceof Chest)
                {
                    chests.push(toJson(cell.item, col, row));
                }
                else if (cell.item instanceof DotBomb)
                {
                    dotBombs.push(toJson(cell.item, col, row));                  
                }
                else if (cell.item instanceof Dot)
                {
                    dots.push(toJson(cell.item, col, row));
                }
                else if (cell.item instanceof Fire)
                {
                    fires.push(toJson(cell.item, col, row));                  
                }
                else if (cell.item instanceof Wild)
                {
                    wilds.push(toJson(cell.item, col, row));                  
                }
                else if (cell.item instanceof Anchor)
                {
                    anchors.push(toJson(cell.item, col, row));                  
                }
                else if (cell.item instanceof Diamond)
                {
                    diamonds.push(toJson(cell.item, col, row));                  
                }
                else if (cell.item instanceof RandomItem)
                {
                    randoms.push(toJson(cell.item, col, row));                  
                }
                else if (cell.item instanceof YinYang)
                {
                    yinyangs.push(toJson(cell.item, col, row));                  
                }
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
        add(p, "slots", slots);
        add(p, "conveyors", conveyors);
        add(p, "machines", machines);
        add(p, "lazySusans", lazySusans);
        add(p, "dots", dots);
        add(p, "dotBombs", dotBombs);
        add(p, "animals", animals);
        add(p, "knights", knights);
        add(p, "clocks", clocks);
        add(p, "drops", drops);
        add(p, "picks", picks);
        add(p, "colorBombs", colorBombs);
        add(p, "lasers", lasers);
        add(p, "pacmans", pacmans);
        add(p, "mirrors", mirrors);
        add(p, "rockets", rockets);
        add(p, "dominoes", dominoes);
        add(p, "turners", turners);
        add(p, "keys", keys);
        add(p, "eggs", eggs);
        add(p, "spiders", spiders);
        add(p, "chests", chests);
        add(p, "blockers", blockers);
        add(p, "blasters", blasters);
        add(p, "bombs", bombs);
        add(p, "fires", fires);
        add(p, "wilds", wilds);
        add(p, "anchors", anchors);
        add(p, "diamonds", diamonds);
        add(p, "yinyangs", yinyangs);
        add(p, "randoms", randoms);
        
        p.put("generator", generator(c.generator));        
        p.put("goals", goal(c.goals));
        p.put("boosts", boosts(c.boosts));
        
        return p;
    }

    protected static NObject toJson(Item item, int col, int row)
    {
        NObject a = toJson(item, false);
        a.put("p", pt(col, row));
        return a;
    }
    
    public static NObject toJson(Item item, boolean addClass)
    {
        NObject a = new NObject();
        if (item.isStuck())
            a.put("stuck", true);
        
        if (item instanceof Animal)
        {
            Animal an = (Animal) item;
            a.put("strength", an.getStrength());
            a.put("color", an.getColor());
            a.put("type", an.getAnimalType().getName());
            if (an.getAction() != Animal.Action.DEFAULT)
                a.put("action", an.getAction().getName());
            if (addClass)
                a.put("class", "animal");
        }
        else if (item instanceof Explody)
        {
            Explody an = (Explody) item;
            a.put("radius", an.getRadius());
            if (addClass)
                a.put("class", "explody");
        }
        else if (item instanceof Knight)
        {
            Knight an = (Knight) item;
            a.put("strength", an.getStrength());
            if (addClass)
                a.put("class", "knight");
        }
        else if (item instanceof Coin)
        {
            Coin an = (Coin) item;
            a.put("amount", an.getAmount());
            if (addClass)
                a.put("class", "coin");
        }
        else if (item instanceof Clock)
        {
            Clock an = (Clock) item;
            a.put("strength", an.getStrength());
            if (addClass)
                a.put("class", "clock");
        }
        else if (item instanceof Drop)
        {
            Drop an = (Drop) item;
            a.put("radius", an.getRadius());
            if (addClass)
                a.put("class", "drop");
        }
        else if (item instanceof IcePick)
        {
            IcePick an = (IcePick) item;
            a.put("radius", an.getRadius());
            if (addClass)
                a.put("class", "pick");
        }
        else if (item instanceof ColorBomb)
        {
            if (addClass)
                a.put("class", "colorBomb");
        }
        else if (item instanceof Rocket)
        {
            Rocket an = (Rocket) item;
            setDirection(a, an.getDirection());
            if (addClass)
                a.put("class", "rocket");
        }
        else if (item instanceof Blocker)
        {
            Blocker an = (Blocker) item;
            a.put("strength", an.getStrength());
            if (an.isZapOnly())
                a.put("zapOnly", true);
            if (addClass)
                a.put("class", "blocker");
        }
        else if (item instanceof Blaster)
        {
            Blaster an = (Blaster) item;
            a.put("vertical", an.isVertical());
            if (addClass)
                a.put("class", "blaster");
        }
        else if (item instanceof Laser)
        {
            Laser an = (Laser) item;
            setDirection(a, an.getDirection());
            if (addClass)
                a.put("class", "laser");
        }
        else if (item instanceof Pacman)
        {
            Pacman an = (Pacman) item;
            setDirection(a, an.getDirection());
            if (addClass)
                a.put("class", "pacman");
        }
        else if (item instanceof Mirror)
        {
            Mirror an = (Mirror) item;
            a.put("flip", an.isFlipped());
            if (addClass)
                a.put("class", "mirror");
        }
        else if (item instanceof Domino)
        {
            Domino an = (Domino) item;
            a.put("num", pt(an.num[0], an.num[1]));
            a.put("vertical", an.vertical);
            if (addClass)
                a.put("class", "domino");
        }
        else if (item instanceof Turner)
        {
            Turner an = (Turner) item;
            a.put("n", an.n);
            if (addClass)
                a.put("class", "turner");
        }
        else if (item instanceof Bomb)
        {
            Bomb an = (Bomb) item;
            a.put("radius", an.getRadius());
            if (addClass)
                a.put("class", "bomb");
        }
        else if (item instanceof Key)
        {
            if (addClass)
                a.put("class", "key");
        }
        else if (item instanceof Egg)
        {
            Egg an = (Egg) item;
            if (an.isCracked())
                a.put("cracked", true);
            if (addClass)
                a.put("class", "egg");
        }
        else if (item instanceof Spider)
        {
            Spider an = (Spider) item;
            a.put("strength", an.getStrength());
            if (addClass)
                a.put("class", "spider");
        }
        else if (item instanceof Chest)
        {
            Chest an = (Chest) item;
            a.put("strength", an.getStrength());
            if (an.getItem() != null)
                a.put("item", toJson(an.getItem(), true));
            if (addClass)
                a.put("class", "chest");
        }
        else if (item instanceof Dot)
        {
            Dot dot = (Dot) item;
            a.put("color", dot.color);
            if (dot.isRadioActive())
                a.put("radioActive", true);
            
            if (addClass)
                a.put("class", "dot");

            if (dot.isLetter())
            {
                a.put("letter", dot.getLetter());
                LetterMultiplier m = dot.getLetterMultiplier();
                if (m != null)
                    a.put("mult", m.toJson());
            }
        }
        else if (item instanceof DotBomb)
        {
            DotBomb bomb = (DotBomb) item;
            Dot dot = bomb.getDot();
            
            a.put("color", dot.color);
            if (dot.isRadioActive())
                a.put("radioActive", true);
            
            if (dot.isLetter())
            {
                a.put("letter", dot.getLetter());
                LetterMultiplier m = dot.getLetterMultiplier();
                if (m != null)
                    a.put("mult", m.toJson());
            }
            a.put("strength", bomb.getStrength());
            if (addClass)
                a.put("class", "dotBomb");               
        }
        else if (item instanceof Fire)
        {
            if (addClass)
                a.put("class", "fire");
        }
        else if (item instanceof Wild)
        {
            if (addClass)
                a.put("class", "wild");
        }
        else if (item instanceof Anchor)
        {
            if (addClass)
                a.put("class", "anchor");
        }
        else if (item instanceof Diamond)
        {
            if (addClass)
                a.put("class", "diamond");
        }
        else if (item instanceof RandomItem)
        {
            RandomItem rnd = (RandomItem) item;
            if (rnd.isRadioActive())
                a.put("radioActive", true);
            
            if (addClass)
                a.put("class", "random");
        }
        else if (item instanceof YinYang)
        {
            if (addClass)
                a.put("class", "yinyang");
        }
        return a;
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
        
        if (g.getDiamonds() != 0)
            p.put("diamonds", g.getDiamonds());
        
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
        
        if (g.getColorBombs() != 0)
            p.put("colorBombs", g.getColorBombs());
        
        if (g.getBombs() != 0)
            p.put("bombs", g.getBombs());
        
        if (g.getBlasters() != 0)
            p.put("blasters", g.getBlasters());
        
        if (g.getBlockers() != 0)
            p.put("blockers", g.getBlockers());
        
        if (g.getZapBlockers() != 0)
            p.put("zapBlockers", g.getZapBlockers());
        
        if (g.getMaxMoves() != 0)
            p.put("maxMoves", g.getMaxMoves());
        
        if (g.getIce() != 0)
            p.put("ice", g.getIce());
        
        if (g.getDoors() != 0)
            p.put("doors", g.getDoors());
        
        if (g.getCages() != 0)
            p.put("cages", g.getCages());
        
        if (g.getBubbles() != 0)
            p.put("bubbles", g.getBubbles());
        
        if (g.getSpiders() != 0)
            p.put("spiders", g.getSpiders());
        
        if (g.getScore() != 0)
            p.put("score", g.getScore());

        if (g.getTime() != 0)
            p.put("time", g.getTime());
        
        if (g.getWords() != 0)
            p.put("words", g.getWords());
        
        if (g.getCoins() != 0)
            p.put("coins", g.getCoins());
        
        if (g.getChests() != 0)
            p.put("chests", g.getChests());
        
        if (g.getWrappedDots() != 0)
            p.put("wrappedDots", g.getWrappedDots());
        
        if (g.getStriped() != 0)
            p.put("striped", g.getStriped());
        
        ChainGoal ch = g.getChainGoal();
        if (ch != null)
            p.put("chainGoal", ch.toNObject());
        
        return p;
    }
    
    protected static NObject generator(Generator g)
    {
        NObject p = new NObject();
        p.put("generateLetters", g.generateLetters);
        p.put("swapMode", g.swapMode);
        p.put("clickMode", g.clickMode);
        p.put("rollMode", g.rollMode);
        p.put("diagonalMode", g.diagonalMode);
        p.put("dominoMode", g.dominoMode);
        p.put("slipperyAnchors", g.slipperyAnchors);
        p.put("minChainLength", g.minChainLength);
        p.put("seed", g.getSeed());
        p.put("fireGrowthRate", g.fireGrowthRate);
        p.put("maxAnchors", g.maxAnchors);
        p.put("maxDomino", g.maxDomino);
        p.put("initialDotsOnly", g.initialDotsOnly);
        p.put("animalStrength", g.animalStrength);
        p.put("knightStrength", g.knightStrength);
        p.put("clockStrength", g.clockStrength);
        p.put("chestStrength", g.chestStrength);
        p.put("bombStrength", g.bombStrength);
        p.put("blockerStrength", g.blockerStrength);
        p.put("animalType", g.animalType.getName());
        p.put("animalAction", g.animalAction.getName());
        p.put("eggsNeeded", g.eggsNeeded);
        
        if (g.radioActivePct > 0)
            p.put("radioActivePct", g.radioActivePct);
        
        if (g.spiderGrowth > 0)
            p.put("spiderGrowth", g.spiderGrowth);
        
        String rewards = g.rewardStrategies;
        if (rewards != null && rewards.length() > 0)
            p.put("rewardStrategies", rewards);
        
        p.put("icePickRadius", g.icePickRadius);
        p.put("dropRadius", g.dropRadius);
        
        if (g.generateLetters)
        {
            p.put("removeLetters", g.removeLetters);
            p.put("findWords", g.findWords);
            p.put("maxWordLength", g.maxWordLength);
        }
        
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
        if (item instanceof Blocker)
        {
            if (((Blocker) item).isZapOnly())
                a.push("zapBlocker");
            else
                a.push("blocker");
        }
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
        else
        {
            a.push(item.getType());
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
