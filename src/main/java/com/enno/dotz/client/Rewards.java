package com.enno.dotz.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import com.ait.tooling.nativetools.client.NArray;
import com.ait.tooling.nativetools.client.NObject;
import com.enno.dotz.client.anim.AnimList;
import com.enno.dotz.client.item.Blaster;
import com.enno.dotz.client.item.Bomb;
import com.enno.dotz.client.item.ColorBomb;
import com.enno.dotz.client.item.Dot;
import com.enno.dotz.client.item.DotBomb;
import com.enno.dotz.client.item.Explody;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.Rocket;
import com.enno.dotz.client.item.Striped;
import com.enno.dotz.client.item.Wild;
import com.enno.dotz.client.item.WrappedDot;
import com.enno.dotz.client.ui.MXButtonsPanel;
import com.enno.dotz.client.ui.MXListGrid;
import com.enno.dotz.client.ui.MXListGridField;
import com.enno.dotz.client.ui.MXVBox;
import com.enno.dotz.client.ui.MXWindow;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridEditEvent;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;

public class Rewards
{
    public abstract static class Reward
    {
        protected boolean random;
        protected Cell cell;
        
        protected Reward(boolean random, Cell cell)
        {
            this.random = random;
            this.cell = cell;
        }
        
        public abstract Item upgrade(Item item, Context ctx);

        public boolean isRandom()
        {
            return random;
        }
    }
    
    //TODO Key, Reshuffle, Drop, Turner
    //TODO bad reward: DotBomb, Fire, Animal
    
    /** Abbr: 's', 'S' */
    public static class StripedReward extends Reward
    {
        public StripedReward(boolean random, Cell cell)
        {
            super(random, cell);
        }
        
        public Item upgrade(Item item, Context ctx)
        {
            int color = item == null ? ctx.generator.getNextDotColor() : item.getColor();
            return new Striped(color, ctx.generator.getRandom().nextBoolean());
        }
    }
    
    /** Abbr: 'w', 'W' */
    public static class WrappedReward extends Reward
    {
        public WrappedReward(boolean random, Cell cell)
        {
            super(random, cell);
        }
        
        public Item upgrade(Item item, Context ctx)
        {
            int color = item == null ? ctx.generator.getNextDotColor() : item.getColor();
            return new WrappedDot(color);
        }
    }

    /** Abbr: 'o' */
    public static class WildCardReward extends Reward
    {
        public WildCardReward(boolean random, Cell cell)
        {
            super(random, cell);
        }

        public Item upgrade(Item item, Context ctx)
        {
            return new Wild(false);
        }
    }

    /** Abbr: 'x' */
    public static class ExplodyReward extends Reward
    {
        public ExplodyReward(boolean random, Cell cell)
        {
            super(random, cell);
        }

        public Item upgrade(Item item, Context ctx)
        {
            return new Explody();
        }
    }

    /** Abbr: 'r' */
    public static class RocketReward extends Reward
    {
        public RocketReward(boolean random, Cell cell)
        {
            super(random, cell);
        }

        public Item upgrade(Item item, Context ctx)
        {
            return new Rocket(Direction.randomDirection(ctx.generator.getRandom()), false);
        }
    }
    
    /** Abbr: 'c' */
    public static class ColorBombReward extends Reward
    {
        public ColorBombReward(boolean random, Cell cell)
        {
            super(random, cell);
        }

        public Item upgrade(Item item, Context ctx)
        {
            return new ColorBomb(false);
        }
    }
    
    /** Abbr: 'b', 'B' */
    public static class BlasterReward extends Reward
    {
        public BlasterReward(boolean random, Cell cell)
        {
            super(random, cell);
        }

        public Item upgrade(Item item, Context ctx)
        {
            return new Blaster(ctx.generator.getRandom().nextBoolean(), false);
        }
    }

    /** Abbr: 'e', 'E' */
    public static class BombReward extends Reward
    {
        public BombReward(boolean random, Cell cell)
        {
            super(random, cell);
        }

        public Item upgrade(Item item, Context ctx)
        {
            return new Bomb(); // TODO wider radius
        }
    }
    
    public static class MultiplierReward extends Reward
    {
        public MultiplierReward(boolean random, Cell cell)
        {
            super(random, cell);
        }

        //TODO add isSuitable
        
        public Item upgrade(Item item, Context ctx)
        {
            Dot dot = (Dot) item;
            
            int n, type;
            LetterMultiplier m = dot.getLetterMultiplier();
            if (m == null)
            {
                n = 2;
                type = ctx.generator.getRandom().nextBoolean() ? LetterMultiplier.LETTER_MULTIPLIER : LetterMultiplier.WORD_MULTIPLIER;
            }
            else
            {
                type = m.getType();
                n = m.getMultiplier() + 1;
            }
            if (ctx.generator.getRandom().nextBoolean())
                n++;
            
            Dot newDot = new Dot(dot.color, dot.getLetter(), false);
            LetterMultiplier m2 = new LetterMultiplier(n, type);
            newDot.setLetterMultiplier(m2);
            
            return newDot;
        }
    }
    
    
    public static class BombifyReward extends Reward
    {
        public BombifyReward(boolean random, Cell cell)
        {
            super(random, cell);
        }

        //TODO add isSuitable
        
        public Item upgrade(Item item, Context ctx)
        {
            Dot dot = (Dot) item;
            
            Dot newDot = dot.copy();
            return new DotBomb(newDot, 9, dot.isStuck());
        }
    }
    
    public static class RewardStrategy
    {
        public static final Comparator<RewardStrategy> BY_LENGTH_REV = new Comparator<RewardStrategy>()
        {
            @Override
            public int compare(RewardStrategy a, RewardStrategy b)
            {
                return b.m_chainLength - a.m_chainLength; // largest value first
            }
        };

        private char m_code;
        private int m_chainLength;
        private int m_every = 1;
        private int m_howMany = 2;
        
        private int m_counter = 0;
        
        public RewardStrategy(NObject obj)
        {
            m_code = obj.getAsString("type").charAt(0);
            m_chainLength = obj.getAsInteger("len");
            m_every = obj.getAsInteger("every");
            m_howMany = obj.getAsInteger("howMany");
        }
        
        public boolean checkChainSizeReward(List<Cell> chain, int chainSize, List<Reward> list)
        {
            Cell cell = chain.get(0);
            
            if (chainSize >= m_chainLength)
            {
                m_counter++;
                if (m_counter < m_every)
                    return true; // skip this one
                
                m_counter = 0;
                
                for (int i = 0; i < m_howMany; i++)
                {
                    switch (m_code)
                    {
                        case 's':   list.add(new StripedReward(true, null)); break;
                        case 'S':   list.add(new StripedReward(false, cell)); break;
                        case 'w':   list.add(new WrappedReward(true, null)); break;
                        case 'W':   list.add(new WrappedReward(false, cell)); break;
                        case 'o':   list.add(new WildCardReward(true, cell)); break;
                        case 'O':   list.add(new WildCardReward(false, cell)); break;
                        case 'x':   list.add(new ExplodyReward(true, cell)); break;
                        case 'X':   list.add(new ExplodyReward(false, cell)); break;
                        case 'r':   list.add(new RocketReward(true, cell)); break;
                        case 'R':   list.add(new RocketReward(false, cell)); break;
                        case 'c':   list.add(new ColorBombReward(true, cell)); break;
                        case 'C':   list.add(new ColorBombReward(false, cell)); break;
                        case 'b':   list.add(new BlasterReward(true, cell)); break;
                        case 'B':   list.add(new BlasterReward(false, cell)); break;
                        case 'e':   list.add(new BombReward(true, cell)); break;
                        case 'E':   list.add(new BombReward(false, cell)); break;
                        case 'm':   list.add(new MultiplierReward(true, cell)); break;
                        case 'z':   list.add(new BombifyReward(true, cell)); break;
                    }
                }
                return true;
            }
            return false;
        }
        
        public static String rewardLabel(char code)
        {
            switch (code)
            {
                case 's':   return "Striped (Random)";
                case 'S':   return "Striped (Chain Start)";
                case 'w':   return "Wrapped (Random)";
                case 'W':   return "Wrapped (Chain Start)";
                case 'o':   return "Wild Card (Random)";
                case 'O':   return "Wild Card (Chain Start)";
                case 'x':   return "Explody (Random)";
                case 'X':   return "Explody (Chain Start)";
                case 'r':   return "Rocket (Random)";
                case 'R':   return "Rocket (Chain Start)";
                case 'c':   return "Color Bomb (Random)";
                case 'C':   return "Color Bomb (Chain Start)";
                case 'b':   return "Blaster (Random)";
                case 'B':   return "Blaster (Chain Start)";
                case 'e':   return "Bomb (Random)";
                case 'E':   return "Bomb (Chain Start)";
                case 'm':   return "Multiplier (Random)";
                case 'z':   return "Bombify (Random)";
            }
            return "unknown code=" + code;
        }

        public static final char[] REWARD_CODES = { 's', 'S', 'w', 'W', 'b', 'B', 'o', 'O', 'x', 'X', 'r', 'R', 'c', 'C', 'e', 'E', 'm', 'z'};
        
        public static LinkedHashMap<String,String> getValueMap()
        {
            LinkedHashMap<String,String> list = new LinkedHashMap<String,String>();
            for (char c : REWARD_CODES)
                list.put("" + c, rewardLabel(c));
            return list;
        }
        
        public static List<RewardStrategy> parseStrategies(String rewardStrategies)
        {
            List<RewardStrategy> list = new ArrayList<RewardStrategy>();
            NArray a = toNArray(rewardStrategies);
            for (int i = 0, n = a.size(); i < n; i++)
            {
                list.add(new RewardStrategy(a.getAsObject(i)));
            }
            Collections.sort(list, BY_LENGTH_REV);
            return list;
        }
        

        public static NArray toNArray(String rewardStrategies)
        {
            NArray data = new NArray();
            if (rewardStrategies != null && rewardStrategies.length() > 0)
            {
                for (String code : rewardStrategies.split(","))
                {
                    String type = code.substring(0, 1);
                    int every = 1;
                    int howMany = 1;
                    int len;
                    
                    String rest = code.substring(1);
                    int e = rest.indexOf("e");
                    int x = rest.indexOf("x");
                    
                    if (e != -1 )
                    {
                        len = Integer.parseInt(rest.substring(0, e));
                        if (x != -1)
                        {
                            every = Integer.parseInt(rest.substring(e + 1, x));
                            howMany = Integer.parseInt(rest.substring(e + 1, x));
                        }
                        else
                            every = Integer.parseInt(rest.substring(e + 1));
                    }
                    else if (x != -1)
                    {
                        len = Integer.parseInt(rest.substring(0, x));
                        howMany = Integer.parseInt(rest.substring(x + 1));
                    }
                    else
                    {
                        len = Integer.parseInt(rest);
                    }
                    
                    NObject row = new NObject();
                    row.put("len", len);
                    row.put("type", type);
                    row.put("howMany", howMany);
                    row.put("every", every);
                    
                    data.push(row);
                }
            }
            return data;
        }
        
    }
    
    List<Reward> m_list = new ArrayList<Reward>();
    List<RewardStrategy> m_strategies = new ArrayList<RewardStrategy>();
    
    public Rewards(List<RewardStrategy> strategies)
    {
        m_strategies = strategies;
    }
    
    public void addChainSizeReward(List<Cell> chain, int chainSize)
    {
        if (chainSize == 0)
            return;
        
        for (RewardStrategy strategy : m_strategies)
        {
            if (strategy.checkChainSizeReward(chain, chainSize, m_list))
                return;
        }
    }
    
    public boolean hasRewards()
    {
        return m_list.size() > 0;
    }

    public void addAnimations(AnimList list, GridState state)
    {
        for (Reward reward : m_list)
        {
            state.addRewardAnimation(list, reward);
        }
    }
    
    public abstract static class RewardEditor extends MXWindow
    {
        private RewardEditorPanel m_rewards;

        public RewardEditor(String rewards)
        {
            setTitle("Edit Rewards");
            
            addItem(createPane());
            
            setCanDragResize(true);
            setCanDragReposition(true);
            
            setWidth(500);
            setHeight(190);
            centerInPage();
            
            setRewards(rewards);
            
            show();
        }
        
        public void setRewards(String rewardStrategies)
        {
            m_rewards.setRewards(rewardStrategies);
        }

        private Canvas createPane()
        {
            MXVBox pane = new MXVBox();
            pane.setPadding(5);
            pane.setMembersMargin(10);
            
            m_rewards = new RewardEditorPanel();
            pane.addMember(m_rewards);
            
            MXButtonsPanel buttons = new MXButtonsPanel();
            buttons.add("Add", new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    m_rewards.addRecord();
                    
                }
            });
            buttons.add("Save", new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    String rewards = m_rewards.getRewards();
                    if (rewards == null)
                        return;
                    
                    closeWindow();
                    saveRewards(rewards);
                    
                }
            });
            buttons.add("Cancel", new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    closeWindow();
                }
            });
            pane.addMember(buttons);
            
            return pane;
        }
        
        public abstract void saveRewards(String rewards);
    }
    
    public static class RewardEditorPanel extends MXVBox
    {
        private MXListGrid m_grid;

        public RewardEditorPanel()
        {
            m_grid = createGrid();
            addMember(m_grid);
        }
        
        public void addRecord()
        {
            Record rec = new Record();
            //rec.setAttribute("len", value);
            rec.setAttribute("every", 1);
            rec.setAttribute("howMany", 1);
            m_grid.addData(rec);
        }

        public void setRewards(String rewardStrategies)
        {
            NArray data = RewardStrategy.toNArray(rewardStrategies);
            m_grid.setData(data);
        }
        
        public String getRewards()
        {
            StringBuilder b = new StringBuilder();
            for (ListGridRecord rec : m_grid.getRecords())
            {
                String type = rec.getAttribute("type");
                Integer len = rec.getAttributeAsInt("len");
                if (len != null && type != null && type.length() > 0)
                {
                    if (b.length() > 0)
                        b.append(",");
                    
                    b.append(type).append(len);
                    
                    Integer every = rec.getAttributeAsInt("every");
                    if (every != null && every != 1)
                        b.append("e").append(every);
                    
                    Integer howMany = rec.getAttributeAsInt("howMany");
                    if (howMany != null && howMany != 1)
                        b.append("x").append(howMany);
                }
            }
            return b.toString();
        }
        
        protected MXListGrid createGrid()
        {
            MXListGrid grid = new MXListGrid();
            grid.setCanEdit(true);
            grid.setEditEvent(ListGridEditEvent.CLICK);
            MXListGridField len = new MXListGridField("len", "Chain Length", ListGridFieldType.INTEGER, 120);
            MXListGridField type = new MXListGridField("type", "Reward Type", ListGridFieldType.TEXT, 135);
            MXListGridField every = new MXListGridField("every", "Every", ListGridFieldType.INTEGER, 80);
            MXListGridField howMany = new MXListGridField("howMany", "How Many", ListGridFieldType.INTEGER, 80);
            MXListGridField del = new MXListGridField("del", " ", ListGridFieldType.ICON);
            del.setIcon("delete2.png");
            
            type.setValueMap(RewardStrategy.getValueMap());
            type.setCellAlign(Alignment.CENTER);
            len.setCellAlign(Alignment.CENTER);
            every.setCellAlign(Alignment.CENTER);
            howMany.setCellAlign(Alignment.CENTER);
            
            del.addRecordClickHandler(new RecordClickHandler()
            {
                @Override
                public void onRecordClick(RecordClickEvent event)
                {
                    m_grid.removeData(event.getRecord());
                }
            });

            
//            grid.setEditorCustomizer(new ListGridEditorCustomizer() {  
//                public FormItem getEditor(ListGridEditorContext context) {  
//                    ListGridField field = context.getEditField();  
//                    if (field.getName().equals("type")) {  
//                        ListGridRecord record = context.getEditedRecord();  
//                        char ch = record.getAttribute("type").charAt(0);
//                        MXSelectItem sel = new MXSelectItem();
//                        sel.setValueMap(RewardStrategy.getValueMap());
//                        sel.setValue("" + ch);
//                        return sel;
//                    }  
//                    return context.getDefaultProperties();  
//                }  
//            });  
            
            grid.setFields(del, type, len, every, howMany);
            
            return grid;
        }
    }
}
