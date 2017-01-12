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
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;

public class Rewards
{
    public enum Where
    {
        AT_START("<", "At Start"),
        AT_END(">", "At End"),
        RANDOM("?", "Random");
        
        private String m_code;
        private String m_description;
        
        Where(String code, String description)
        {
            m_code = code;
            m_description = description;
        }
        
        public String getCode()
        {
            return m_code;
        }
        
        public String getDescription()
        {
            return m_description;
        }

        public static Where find(String code)
        {
            for (Where w : values())
            {
                if (w.getCode().equals(code))
                    return w;
            }
            return null;
        }
    }

    public abstract static class Reward
    {
        protected Where where;
        protected Cell cell;
        
        protected Reward(Where where, Cell cell)
        {
            this.where = where;
            this.cell = cell;
        }
        
        public abstract Item upgrade(Item item, Context ctx);

        public boolean isRandom()
        {
            return where == Where.RANDOM;
        }
    }
    
    //TODO Key, Reshuffle, Drop, Turner
    //TODO bad reward: DotBomb, Fire, Animal
    
    public static abstract class Meta
    {
        private static Meta[] ALL = {
                BlasterReward.META,
                BombifyReward.META,
                BombReward.META,
                ColorBombReward.META,
                ExplodyReward.META,
                MultiplierReward.META,
                RocketReward.META,
                StripedReward.META,
                WildCardReward.META,
                WrappedReward.META,
        };
        
        public static final LinkedHashMap<String,Meta> MAP = createMap();
        
        protected String m_code;
        protected String m_description;
        protected boolean m_randomOnly;
        
        public static LinkedHashMap<String,Meta> createMap()
        {
            LinkedHashMap<String,Meta> map = new LinkedHashMap<String,Meta>();
            for (Meta meta : ALL)
            {
                map.put(meta.getCode(), meta);
            }
            return map;
        }
        
        public Meta(String code, String description)
        {
           this(code, description, false);
        }

        public Meta(String code, String description, boolean randomOnly)
        {
            m_code = code;
            m_description = description;
            m_randomOnly = randomOnly;
        }
        
        public abstract Reward createReward(Where where, Cell cell);

        public String getCode()
        {
            return m_code;
        }

        public String getDescription()
        {
            return m_description;
        }
        
        public boolean isRandomOnly()
        {
            return m_randomOnly;
        }

        public boolean isInvalidWhere(Where w)
        {
            return isRandomOnly() && w != Where.RANDOM;
        }
    }
    
    public static class StripedReward extends Reward
    {
        public static Meta META = new Meta("s", "Striped") {
            @Override
            public Reward createReward(Where where, Cell cell)
            {
                return new StripedReward(where, cell);
            }
        };
        
        public StripedReward(Where where, Cell cell)
        {
            super(where, cell);
        }
        
        @Override
        public Item upgrade(Item item, Context ctx)
        {
            int color = item == null ? ctx.generator.getNextDotColor() : item.getColor();
            return new Striped(color, ctx.generator.getRandom().nextBoolean());
        }
    }
    
    public static class WrappedReward extends Reward
    {
        public static Meta META = new Meta("w", "Wrapped") {
            @Override
            public Reward createReward(Where where, Cell cell)
            {
                return new WrappedReward(where, cell);
            }
        };

        public WrappedReward(Where where, Cell cell)
        {
            super(where, cell);
        }
        
        @Override
        public Item upgrade(Item item, Context ctx)
        {
            int color = item == null ? ctx.generator.getNextDotColor() : item.getColor();
            return new WrappedDot(color);
        }
    }

    public static class WildCardReward extends Reward
    {
        public static Meta META = new Meta("o", "Wild Card") {
            @Override
            public Reward createReward(Where where, Cell cell)
            {
                return new WildCardReward(where, cell);
            }
        };

        public WildCardReward(Where where, Cell cell)
        {
            super(where, cell);
        }

        @Override
        public Item upgrade(Item item, Context ctx)
        {
            return new Wild(false);
        }
    }

    public static class ExplodyReward extends Reward
    {
        public static Meta META = new Meta("x", "Explody") {
            @Override
            public Reward createReward(Where where, Cell cell)
            {
                return new ExplodyReward(where, cell);
            }
        };

        public ExplodyReward(Where where, Cell cell)
        {
            super(where, cell);
        }

        @Override
        public Item upgrade(Item item, Context ctx)
        {
            return new Explody();
        }
    }

    public static class RocketReward extends Reward
    {
        public static Meta META = new Meta("r", "Rocket") {
            @Override
            public Reward createReward(Where where, Cell cell)
            {
                return new RocketReward(where, cell);
            }
        };

        public RocketReward(Where where, Cell cell)
        {
            super(where, cell);
        }

        @Override
        public Item upgrade(Item item, Context ctx)
        {
            return new Rocket(Direction.randomDirection(ctx.generator.getRandom()), false);
        }
    }

    public static class ColorBombReward extends Reward
    {
        public static Meta META = new Meta("c", "Color Bomb") {
            @Override
            public Reward createReward(Where where, Cell cell)
            {
                return new ColorBombReward(where, cell);
            }
        };

        public ColorBombReward(Where where, Cell cell)
        {
            super(where, cell);
        }

        @Override
        public Item upgrade(Item item, Context ctx)
        {
            return new ColorBomb(false);
        }
    }
    
    /** Abbr: 'b', 'B' */
    public static class BlasterReward extends Reward
    {
        public static Meta META = new Meta("b", "Blaster") {
            @Override
            public Reward createReward(Where where, Cell cell)
            {
                return new BlasterReward(where, cell);
            }
        };

        public BlasterReward(Where where, Cell cell)
        {
            super(where, cell);
        }

        @Override
        public Item upgrade(Item item, Context ctx)
        {
            return new Blaster(ctx.generator.getRandom().nextBoolean(), false);
        }
    }

    public static class BombReward extends Reward
    {
        public static Meta META = new Meta("e", "Bomb") {
            @Override
            public Reward createReward(Where where, Cell cell)
            {
                return new BombReward(where, cell);
            }
        };

        public BombReward(Where where, Cell cell)
        {
            super(where, cell);
        }

        @Override
        public Item upgrade(Item item, Context ctx)
        {
            return new Bomb(); // TODO wider radius
        }
    }
    
    public static class MultiplierReward extends Reward
    {
        public static Meta META = new Meta("m", "Multiplier", true) {
            @Override
            public Reward createReward(Where where, Cell cell)
            {
                return new MultiplierReward(where, cell);
            }
        };

        public MultiplierReward(Where where, Cell cell)
        {
            super(where, cell);
        }

        //TODO add isSuitable
        
        @Override
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
        public static Meta META = new Meta("z", "Bombify", true) {
            @Override
            public Reward createReward(Where where, Cell cell)
            {
                return new BombifyReward(where, cell);
            }
        };

        public BombifyReward(Where where, Cell cell)
        {
            super(where, cell);
        }

        //TODO add isSuitable
        
        @Override
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
      
        private String m_code;
        private Where m_where;
        private int m_chainLength;
        private int m_every = 1;
        private int m_howMany = 2;
        
        private int m_counter = 0;
        
        public RewardStrategy(NObject obj)
        {
            m_code = obj.getAsString("type");
            m_where = Where.find(obj.getAsString("where"));
            m_chainLength = obj.getAsInteger("len");
            m_every = obj.getAsInteger("every");
            m_howMany = obj.getAsInteger("howMany");
        }
        
        public boolean checkChainSizeReward(List<Cell> chain, int chainSize, List<Reward> list)
        {
            Cell cell = m_where == Where.RANDOM ? null : (m_where == Where.AT_START ? chain.get(0) : chain.get(chain.size() - 1));
            
            if (chainSize >= m_chainLength)
            {
                m_counter++;
                if (m_counter < m_every)
                    return true; // skip this one
                
                m_counter = 0;
                
                Meta meta = Meta.MAP.get(m_code);
                
                for (int i = 0; i < m_howMany; i++)
                {
                    list.add(meta.createReward(m_where, cell));                    
                }
                return true;
            }
            return false;
        }

        public static LinkedHashMap<String,String> getTypeMap()
        {
            LinkedHashMap<String,String> list = new LinkedHashMap<String,String>();
            for (Meta meta : Meta.MAP.values())
            {
                list.put(meta.getCode(), meta.getDescription());
            }
            return list;
        }
        
        public static LinkedHashMap<String,String> getWhereMap()
        {
            LinkedHashMap<String,String> list = new LinkedHashMap<String,String>();
            for (Where w : Where.values())
            {
                list.put(w.getCode(), w.getDescription());
            }
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
                    String where = code.substring(1, 2);
                    
                    int every = 1;
                    int howMany = 1;
                    int len;
                    
                    String rest = code.substring(2);
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
                    row.put("where", where);
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
            rec.setAttribute("where", Where.RANDOM.getCode());
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
                String where = rec.getAttribute("where");
                Integer len = rec.getAttributeAsInt("len");
                if (len != null && type != null && type.length() > 0)
                {
                    Meta meta = Meta.MAP.get(type);
                    Where w = Where.find(where);
                    if (meta.isInvalidWhere(w))
                    {
                        SC.warn("Type " + meta.getDescription() + " does not support Where = " + w.getDescription());
                        continue;
                    }
                    
                    if (b.length() > 0)
                        b.append(",");
                    
                    b.append(type).append(where).append(len);
                    
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
            MXListGridField where = new MXListGridField("where", "Where", ListGridFieldType.TEXT, 135);
            MXListGridField every = new MXListGridField("every", "Every", ListGridFieldType.INTEGER, 80);
            MXListGridField howMany = new MXListGridField("howMany", "How Many", ListGridFieldType.INTEGER, 80);
            MXListGridField del = new MXListGridField("del", " ", ListGridFieldType.ICON);
            del.setIcon("delete2.png");
            
            type.setValueMap(RewardStrategy.getTypeMap());
            type.setCellAlign(Alignment.CENTER);
            where.setValueMap(RewardStrategy.getWhereMap());
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
            
            grid.setFields(del, type, where, len, every, howMany);
            
            return grid;
        }
    }
}
