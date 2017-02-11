package com.enno.dotz.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.ait.tooling.nativetools.client.NArray;
import com.ait.tooling.nativetools.client.NObject;
import com.enno.dotz.client.Cell.Slot;
import com.enno.dotz.client.Generator.ItemFrequency;
import com.enno.dotz.client.SlotMachines.SlotMachine;
import com.enno.dotz.client.SlotMachines.SlotMachineInfo;
import com.enno.dotz.client.SlotMachines.SlotMachineReward.BoostReward;
import com.enno.dotz.client.SlotMachines.SlotMachineReward.CoinReward;
import com.enno.dotz.client.SlotMachines.SlotMachineReward.LaunchItemReward;
import com.enno.dotz.client.SlotMachines.SlotMachineReward.PointReward;
import com.enno.dotz.client.SlotMachines.SlotMachineReward.ReduceItemReward;
import com.enno.dotz.client.SoundManager.Sound;
import com.enno.dotz.client.anim.Transition.ArchTransition;
import com.enno.dotz.client.anim.Transition.ParabolaTransition;
import com.enno.dotz.client.anim.TransitionList;
import com.enno.dotz.client.editor.FrequencySliderGroup;
import com.enno.dotz.client.editor.LevelParser;
import com.enno.dotz.client.item.Coin;
import com.enno.dotz.client.item.Diamond;
import com.enno.dotz.client.item.Dot;
import com.enno.dotz.client.item.DotBomb;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.Wild;
import com.enno.dotz.client.ui.MXButtonsPanel;
import com.enno.dotz.client.ui.MXListGrid;
import com.enno.dotz.client.ui.MXListGridField;
import com.enno.dotz.client.ui.MXSelectItem;
import com.enno.dotz.client.ui.MXTextInput;
import com.enno.dotz.client.ui.MXVBox;
import com.enno.dotz.client.ui.MXWindow;
import com.enno.dotz.client.ui.UTabSet;
import com.enno.dotz.client.util.Font;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.ListGridEditEvent;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.grid.ListGridEditorContext;
import com.smartgwt.client.widgets.grid.ListGridEditorCustomizer;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.ChangedEvent;
import com.smartgwt.client.widgets.grid.events.ChangedHandler;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;

public class SlotMachines extends ArrayList<SlotMachine>
{
    public static class SlotException extends Exception
    {
        
    }

    public boolean m_canHold;
    
    public static class RewardInfo
    {
        public SlotMachineReward reward;
        public SlotMachine slotMachine;
        
        public RewardInfo(SlotMachineReward reward, SlotMachine slotMachine)
        {
            this.reward = reward;
            this.slotMachine = slotMachine;
        }
    }
    
    public static class SlotMachine extends ArrayList<Slot>
    {
        private boolean m_triggered;
        
//        public boolean canHold()
//        {
//            return m_canHold;
//        }

        public void setTriggered(boolean triggered)
        {
            m_triggered = triggered;
        }
        
        public boolean isTriggered()
        {
            return m_triggered;
        }
        
        public SlotMachineInfo getSlotMachineInfo()
        {
            SlotMachineInfo info = get(0).getSlotMachineInfo();
            return info == null ? SlotMachineInfo.createDefaultInfo() : info;
        }
        
        public RewardInfo getReward(Context ctx)
        {
            
//            return new RewardInfo(new PointReward(100), this);
            
//            return new RewardInfo(new ReduceItemReward(3, "dot"), this);
//            return new RewardInfo(new BoostReward(3, "yinyang"), this);

            ComboFinder finder = new ComboFinder(this, ctx);
            SlotMachineInfo info = getSlotMachineInfo();
            for (SlotComboReward c : info.rewards)
            {
                if (c.combo.match(finder))
                {
                    return new RewardInfo(c.reward.getReward(finder), this);
                }
            }
            return null;
        }

        public boolean isHolding()
        {
            for (Slot slot : this)
            {
                if (slot.isHold())
                    return true;
            }
            return false;
        }

        public int notOnHoldCount()
        {
            int notHoldCount = 0;
            for (Slot slot : this)
            {
                if (!slot.isHold())
                    notHoldCount++;
            }
            return notHoldCount;
        }
    }

    private GridState m_state;
    
    public SlotMachines()
    {
    }
    
    public void locateSlotMachines(GridState state) throws SlotException
    {
        m_state = state;
        
        for (int row = 0; row < m_state.numRows; row++)
        {
            for (int col = 0; col < m_state.numColumns; col++)
            {
                Cell cell = m_state.cell(col, row);
                if (cell instanceof Slot)
                {
                    SlotMachine m = new SlotMachine();
                    m.add((Slot) cell);
                    
                    if (col > m_state.numColumns - 3)
                        throw new SlotException();
                    
                    cell = m_state.cell(col + 1, row);
                    if (!(cell instanceof Slot))
                        throw new SlotException();
                    
                    m.add((Slot) cell);
                    
                    cell = m_state.cell(col + 2, row);
                    if (!(cell instanceof Slot))
                        throw new SlotException();
                    
                    m.add((Slot) cell);
                    col += 2;
                    
                    add(m);
                }
            }
        }
    }

    public void initSlots()
    {
        for (SlotMachine m : this)
        {
            SlotMachineInfo info = m.get(0).getSlotMachineInfo();
            for (Slot s : m)
            {
                s.setSlotMachineInfo(info);
                s.setCanHold(true);
                s.setHold(false);
                s.setup();
            }
        }
    }
    
    public void afterSpin(SlotMachine m, boolean won)
    {
        boolean canHold = true;
        if (won || m.isHolding())
        {
            canHold = false;
        }
        
        for (Slot s : m)
        {
            s.setCanHold(canHold);
            s.setHold(false);
        }
    }
    
    public boolean clickHold(Cell c)
    {
        if (c instanceof Slot)
        {
            Slot slot = (Slot) c;
            if (slot.canHold())
            {
                slot.toggleHold();
                return true;
            }
        }
        return false;
    }

    public boolean isLeftSlot(Slot slot)
    {
        return findLeftSlot(slot) == slot;
    }
    
    public Slot findLeftSlot(Slot slot)
    {
        return findSlotMachine(slot).get(0);
    }
    
    private SlotMachine findSlotMachine(Slot slot)
    {
        for (SlotMachine m : this)
        {
            if (m.contains(slot))
                return m;
        }
        return null;
    }

    public void triggeredSlot(Slot slot)
    {
        findSlotMachine(slot).setTriggered(true);
    }
    
    public void startOfMove()
    {
        for (SlotMachine m : this)
        {
            m.setTriggered(false);
        }
    }
    
    public static class ComboFinder
    {
        private int[] m_dots;

        private Map<String,Integer> m_map = new HashMap<String,Integer>();
        
        public ComboFinder(SlotMachine m, Context ctx)
        {
            m_dots = new int[Config.MAX_COLORS];
            
            for (Slot slot : m)
            {
                Item item = slot.getCurrentItem();
                countItem(item);
            }
        }
        
        private void countItem(Item item)
        {
            if (item instanceof Dot)
            {
                m_dots[((Dot) item).color]++;
            }
            else
            {
                count(item.getType());
            }
        }

        public int getWilds()
        {
            return getCount("wild");
        }
        
        public int getCount(String itemName)
        {
            Integer i = m_map.get(itemName);
            return i == null ? 0 : i;
        }
        
        private void count(String itemName)
        {
            Integer i = m_map.get(itemName);
            if (i == null)
                m_map.put(itemName, 1);
            else
                m_map.put(itemName, i + 1);
        }

        public int getDotColor()
        {
            int maxIndex = -1;
            int max = -1;
            for (int i = 0; i < m_dots.length; i++)
            {
                if (m_dots[i] > max)
                {
                    max = m_dots[i];
                    maxIndex = i;
                }
            }
            return maxIndex;
        }
    }
    
    public abstract static class Combo
    {
        private static List<Combo> s_list = initCombos();
        
        private String m_name;

        public Combo(String name)
        {
            m_name = name;
        }
        
        public String getName()
        {
            return m_name;
        }
        
        public abstract boolean match(ComboFinder finder);
        

        public static LinkedHashMap<String,String> getComboMap()
        {
            LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
            for (Combo combo : s_list)
            {
                map.put(combo.getName(), combo.getName());
            }
            return map;
        }

        public static Combo findCombo(String comboName)
        {
            for (Combo combo : s_list)
            {
                if (combo.m_name.equals(comboName))
                    return combo;
            }
            return null;
        }
        
        
        public static class Match3 extends Combo
        {
            private String m_itemName;

            public Match3(String itemName)
            {
                super("3 " + itemName + "s");
                m_itemName = itemName;
            }

            @Override
            public boolean match(ComboFinder finder)
            {
                int n = finder.getCount(m_itemName);
                return n + finder.getWilds() == 3;
            }
        }
        
//        public static class Match2PlusWild extends Combo
//        {
//            private String m_itemName;
//
//            public Match2PlusWild(String itemName)
//            {
//                super("2 " + itemName + "s + wild card");
//                m_itemName = itemName;
//            }
//
//            @Override
//            public boolean match(ComboFinder finder)
//            {
//                return finder.getCount(m_itemName) == 2 && finder.getWilds() == 1;
//            }
//        }
        
        // TODO sort after Match2AndWild !
        public static class Match2 extends Combo
        {
            private String m_itemName;

            public Match2(String itemName)
            {
                super("2 " + itemName + "s");
                m_itemName = itemName;
            }

            @Override
            public boolean match(ComboFinder finder)
            {
                int n = finder.getCount(m_itemName);
                return n + finder.getWilds() == 2;
            }
        }
        
        public static List<Combo> initCombos()
        {
            List<Combo> list = new ArrayList<Combo>();
            
            list.add(new Combo("3 dots") {
                @Override
                public boolean match(ComboFinder finder)
                {
                    int wild = finder.getWilds();
                    if (wild == 3)
                        return false;
                    
                    for (int i = 0; i < finder.m_dots.length; i++)
                    {
                        if (finder.m_dots[i] + wild == 3)
                            return true;
                    }
                    return false;
                }
            });
//            list.add(new Combo("2 dots + wild card") {
//                @Override
//                public boolean match(ComboFinder finder)
//                {
//                    int wild = finder.m_wilds;
//                    if (wild != 1)
//                        return false;
//                    
//                    for (int i = 0; i < finder.m_dots.length; i++)
//                    {
//                        if (finder.m_dots[i] == 2)
//                            return true;
//                    }
//                    return false;
//                }
//            });
            list.add(new Combo("2 dots") {
                @Override
                public boolean match(ComboFinder finder)
                {
                    int wild = finder.getWilds();
                    for (int i = 0; i < finder.m_dots.length; i++)
                    {
                        if (finder.m_dots[i] + wild == 2)
                            return true;
                    }
                    return false;
                }
            });
            
            list.add(new Match3("wild"));
            
            list.add(new Match3("diamond"));
            list.add(new Match2("diamond"));
            
            list.add(new Match3("fire"));
            list.add(new Match2("fire"));
            
            return list;
        }
    }
    
    public static class SlotComboReward
    {
        Combo combo;
        SlotMachineReward reward;
        
        public SlotComboReward(Combo combo, SlotMachineReward reward)
        {
            this.combo = combo;
            this.reward = reward;
        }
    }
    
    public static class SlotMachineRewards extends ArrayList<SlotComboReward>
    {
        private static SlotMachineRewardMeta[] ALL_REWARD_META = {
                PointReward.META,
                CoinReward.META,
                LaunchItemReward.META,
                ReduceItemReward.META,
                BoostReward.META,
        };
        
        public static final LinkedHashMap<String,SlotMachineRewardMeta> REWARD_META_MAP = createRewardMetaMap();
 
        public static SlotMachineRewards parse(NArray a)
        {
            SlotMachineRewards list = new SlotMachineRewards();
            
            for (int i = 0, n = a.size(); i < n; i++)
            {
                NObject obj = a.getAsObject(i);
                
                Combo combo = Combo.findCombo(obj.getAsString("combo"));                

                String type = obj.getAsString("type");
                SlotMachineReward reward = getMeta(type).parse(obj);
                
                if (reward != null && combo != null)
                {
                    list.add(new SlotComboReward(combo, reward));
                }
            }
            return list;
        }
        
        public NArray asNArray()
        {
            NArray a = new NArray();
            
            for (SlotComboReward r : this)
            {
                NObject row = r.reward.asNObject();
                row.put("combo", r.combo.getName());
                a.push(row);
            }
            return a;
        }
        
        public static LinkedHashMap<String,String> getTypeMap()
        {
            LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
            for (String type : REWARD_META_MAP.keySet())
            {
                map.put(type, type);
            }
            return map;
        }

        public static FormItem createAmountEditor(String type, ListGridRecord record)
        {
            return getMeta(type).getAmountEditor(record);
        }

        public static FormItem createDetailsEditor(String type, ListGridRecord record)
        {
            return getMeta(type).getDetailsEditor(record);
        }

        public static int initType(String type, ListGridRecord record)
        {
//            SlotMachineReward reward = createReward(type);
//            if (reward == null)
//                return;
            
            return getMeta(type).initType(record);
        }
        
        private static LinkedHashMap<String,SlotMachineRewardMeta> createRewardMetaMap()
        {
            LinkedHashMap<String,SlotMachineRewardMeta> map = new LinkedHashMap<String,SlotMachineRewardMeta>();
            for (SlotMachineRewardMeta meta : ALL_REWARD_META)
            {
                map.put(meta.getType(), meta);
            }
            return map;
        }
        
        public static SlotMachineRewardMeta getMeta(String type)
        {
            return REWARD_META_MAP.get(type);
        }

        public static SlotMachineReward getSlotMachineReward(ListGridRecord rec, int row) throws Exception
        {
            String type = rec.getAttribute("type");
            if (type == null)
                throw new Exception("Reward Type is not specified on row " + row);
            
            return getMeta(type).getReward(rec, row);
        }
    }
    
    public abstract static class SlotMachineRewardMeta
    {
        private String m_type;

        protected SlotMachineRewardMeta(String type)
        {
            m_type = type;
        }
        
        public int initType(ListGridRecord record)
        {
            record.setAttribute("amount", (String) null);
            record.setAttribute("details", (String) null);
            return SlotMachineEditor.AMOUNT_INDEX;
        }

        public String getType()
        {
            return m_type;
        }
        
        public abstract SlotMachineReward parse(NObject obj);
        public abstract SlotMachineReward getReward(ListGridRecord rec, int row) throws Exception;

        public FormItem getAmountEditor(ListGridRecord record)
        {
            String val = record.getAttribute("amount");
//          if (val != null)
//          {
//              
//          }
          
            MXTextInput t = new MXTextInput();
            t.setShowTitle(false);
            t.setValue(val);
            return t;
        }
        
        public FormItem getDetailsEditor(ListGridRecord record)
        {
            String val = record.getAttribute("details");
//          if (val != null)
//          {
//              
//          }
          
            return null;
        }
        
        protected int parseAmount(NObject obj)
        {
            return obj.getAsInteger("amount");
        }

        protected int getAmount(ListGridRecord rec, int row) throws Exception
        {
            String s = rec.getAttribute("amount");
            try
            {
                return Integer.parseInt(s);
            }
            catch (Exception e)
            {
                throw new Exception("Invalid amount on row " + row);
            }
        }
        
        protected String getDetails(ListGridRecord rec, int row) throws Exception
        {
            String s = rec.getAttribute("details");
            if (s != null && s.length() > 0)
                return s;
            
            throw new Exception("Invalid details on row " + row);
        }
        
        public boolean hasDetails()
        {
            return false;
        }
    }
    
    public abstract static class SlotMachineReward
    {
        protected int m_amount;
        
        protected SlotMachineReward()
        {
        }
        
        public SlotMachineReward getReward(ComboFinder finder)
        {
            return this;
        }

        protected SlotMachineReward(int amount)
        {
            m_amount = amount;
        }
        
        public int getAmount()
        {
            return m_amount;
        }
        
        public NObject asNObject()
        {
            NObject obj = new NObject();
            obj.put("type", meta().getType());
            obj.put("amount", getAmount());
            return obj;
        }
        
        protected Item createItem(String type, int color)
        {
            if (type.equals("dot"))
                return new Dot(color);
            
            return LevelParser.createItem(type);
        }
        
        public abstract void addTransitions(Context ctx, RewardInfo rewardInfo, TransitionList list, CellList verboten);
        public abstract SlotMachineRewardMeta meta();
        
        public Cell getTargetCell(CellList verboten, Context ctx)
        {
            GridState state = ctx.state;
            Random rnd = ctx.generator.getRandom();
                    
            for (int i = 0; i < 200; i++)
            {
                int col = rnd.nextInt(state.numColumns);
                int row = rnd.nextInt(state.numRows);
                
                if (verboten.containsCell(col, row))
                    continue;
                
                Cell cell = state.cell(col, row);
                if (canTargetCell(cell))
                    return cell;
            }
            return null;
        }

        private boolean canTargetCell(Cell cell)
        {
            if (cell.isLocked() || !cell.canContainItems())
                return false;
            
            if (cell.item == null)
                return false;
            
//            Dot dot = null;
            if (cell.item instanceof Dot)
            {
//                dot = (Dot) cell.item;
            }
            else if (cell.item instanceof DotBomb)
            {
//                dot = ((DotBomb) cell.item).getDot();
            }
            else
                return false;
            
            return true;
        }
        
        public static class PointReward extends SlotMachineReward
        {
            public static final SlotMachineRewardMeta META = new SlotMachineRewardMeta("Points") {
                @Override
                public PointReward parse(NObject obj)
                {
                    return new PointReward(parseAmount(obj));
                }

                @Override
                public SlotMachineReward getReward(ListGridRecord rec, int row) throws Exception
                {
                    int p = getAmount(rec, row);
                    return new PointReward(p);
                }
            };
            
            @Override
            public SlotMachineRewardMeta meta()
            {
                return META;
            }
            
            public PointReward(int points)
            {
                super(points);
            }

            @Override
            public void addTransitions(final Context ctx, RewardInfo rewardInfo, TransitionList list, CellList verboten)
            {
                Slot slot = rewardInfo.slotMachine.get(1);
                final int points = rewardInfo.reward.getAmount();
                
                final Text text = new Text("" + points);
                text.setFontFamily(Font.POINTS);
                text.setFontSize(40);
                text.setFontStyle("bold");
                text.setTextAlign(TextAlign.CENTER);
                text.setTextBaseLine(TextBaseLine.MIDDLE);
                text.setStrokeColor(ColorName.BLACK);
                text.setStrokeWidth(1);
                text.setFillColor(ColorName.YELLOW);
                
                double dy = ctx.state.size() * -2;
                list.add(new ArchTransition(ctx.state.x(slot.col), ctx.state.y(slot.row), ctx.state.x(ctx.state.numColumns / 2), ctx.state.y(ctx.state.numRows + 1), dy, text) {          
                    @Override
                    public void afterStart()
                    {
                        ctx.nukeLayer.add(text);
                        
//                        if (playSound)
//                            Sound.WOOSH.play();
                        Sound.CASH_REGISTER.play();
                    }

                    @Override
                    public void afterEnd()
                    {
                        ctx.nukeLayer.remove(text);
                        ctx.score.addPoints(points);
                    }
                }.fadeOut());
            }
        }
        
        public static class CoinReward extends SlotMachineReward
        {
            public static final SlotMachineRewardMeta META = new SlotMachineRewardMeta("Coins") {
                @Override
                public CoinReward parse(NObject obj)
                {
                    return new CoinReward(parseAmount(obj));
                }

                @Override
                public SlotMachineReward getReward(ListGridRecord rec, int row) throws Exception
                {
                    int p = getAmount(rec, row);
                    return new CoinReward(p);
                }
            };
            
            @Override
            public SlotMachineRewardMeta meta()
            {
                return META;
            }
            
            public CoinReward(int points)
            {
                super(points);
            }

            @Override
            public void addTransitions(final Context ctx, RewardInfo rewardInfo, TransitionList list, CellList verboten)
            {
                Slot slot = rewardInfo.slotMachine.get(1);
                int points = rewardInfo.reward.getAmount();
                
                for (Coin coin : Coin.getCoins(points))
                {
                    list.add(GridState.getCoinTransition(coin, slot.col, slot.row, ctx));                    
                }
            }
        }
        
        public static class LaunchItemReward extends SlotMachineReward
        {
            public static final SlotMachineRewardMeta META = new SlotMachineRewardMeta("Launch") {
                @Override
                public LaunchItemReward parse(NObject obj)
                {
                    return new LaunchItemReward(parseAmount(obj), obj.getAsString("details"));
                }
                
                @Override
                public FormItem getDetailsEditor(ListGridRecord record)
                {
                    String val = record.getAttribute("details");
                    MXSelectItem sel = new MXSelectItem();
                    sel.setShowTitle(false);
                    
                    LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
                    map.put("fire", "fire");
                    map.put("spider", "spider");
                    map.put("wild", "wild");
                    sel.setValueMap(map);
                    
                    sel.setValue((String) val);
                    return sel;
                }
                
                @Override
                public boolean hasDetails()
                {
                    return true;
                }

                @Override
                public SlotMachineReward getReward(ListGridRecord rec, int row) throws Exception
                {
                    int p = getAmount(rec, row);
                    String type = getDetails(rec, row);
                    return new LaunchItemReward(p, type);
                }
            };
            
            private String m_type;
            
            @Override
            public SlotMachineRewardMeta meta()
            {
                return META;
            }
            
            public LaunchItemReward(int amount, String type)
            {
                super(amount);
                m_type = type;
            }

            @Override
            public void addTransitions(final Context ctx, RewardInfo rewardInfo, TransitionList list, CellList verboten)
            {
                Slot slot = rewardInfo.slotMachine.get(1);
                int n = rewardInfo.reward.getAmount();
                
                for (int i = 0; i < n; i++)
                {
                    Item item = createItem(m_type, 0);
                    if (item == null)
                        continue;
                    
                    Cell target = getTargetCell(verboten, ctx);
                    if (target == null)
                        continue;
                    
                    list.add(GridState.getLaunchItemTransition(item, slot.col, slot.row, target, ctx, true));                    
                }
            }
            
            @Override
            public NObject asNObject()
            {
                NObject obj = new NObject();
                obj.put("type", meta().getType());
                obj.put("amount", getAmount());
                obj.put("details", m_type);
                return obj;
            }
        }
        
        public static class ReduceItemReward extends SlotMachineReward
        {
            public static final SlotMachineRewardMeta META = new SlotMachineRewardMeta("Reduce") {
                @Override
                public ReduceItemReward parse(NObject obj)
                {
                    return new ReduceItemReward(parseAmount(obj), obj.getAsString("details"));
                }
                
                @Override
                public FormItem getDetailsEditor(ListGridRecord record)
                {
                    String val = record.getAttribute("details");
                    MXSelectItem sel = new MXSelectItem();
                    sel.setShowTitle(false);
                    
                    LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
                    map.put("fire", "fire");
                    map.put("spider", "spider");
                    map.put("dot", "dot");
                    map.put("anchor", "anchor");
                    map.put("animal", "animal");
                    sel.setValueMap(map);
                    
                    sel.setValue((String) val);
                    return sel;
                }
                
                @Override
                public boolean hasDetails()
                {
                    return true;
                }

                @Override
                public SlotMachineReward getReward(ListGridRecord rec, int row) throws Exception
                {
                    int p = getAmount(rec, row);
                    String type = getDetails(rec, row);
                    return new ReduceItemReward(p, type);
                }
            };
            
            @Override
            public SlotMachineRewardMeta meta()
            {
                return META;
            }
            
            private String m_type;
            private int m_color;
            
            public ReduceItemReward(int amount, String type)
            {
                super(amount);
                m_type = type;
            }

            @Override
            public void addTransitions(final Context ctx, RewardInfo rewardInfo, TransitionList list, CellList verboten)
            {
                Slot slot = rewardInfo.slotMachine.get(1);
                final int n = rewardInfo.reward.getAmount();
                
                final Group g = new Group();
                
                double w = 100;
                double h = 50;
                Rectangle r = new Rectangle(w, h);
                r.setX(-w/2);
                r.setY(-h/2);
                r.setFillColor(ColorName.WHITE);
                r.setStrokeColor(ColorName.BLUE);
                r.setCornerRadius(8);
                g.add(r);
                
                final Text text = new Text("x " + n);
                text.setFontFamily(Font.POINTS);
                text.setFontSize(25);
                text.setFontStyle("bold");
                text.setTextAlign(TextAlign.CENTER);
                text.setTextBaseLine(TextBaseLine.MIDDLE);
//                text.setStrokeColor(ColorName.BLACK);
//                text.setStrokeWidth(1);
                text.setFillColor(ColorName.BLACK);
                text.setX(w * 0.25);
                g.add(text);
                
                Item item = createItem(m_type, m_color);
                IPrimitive<?> shape = item.createShape(50);
                shape.setX(w * -0.25);
                g.add(shape);
                
                double dy = ctx.state.size() * -2;
                list.add(new ParabolaTransition(ctx.state.x(slot.col), ctx.state.y(slot.row), ctx.state.x(ctx.state.numColumns / 2), ctx.state.y(0), g) {          
                    @Override
                    public void afterStart()
                    {
                        ctx.nukeLayer.add(g);
                        
//                        if (playSound)
//                            Sound.WOOSH.play();
                        Sound.CASH_REGISTER.play();
                    }

                    @Override
                    public void afterEnd()
                    {
                        ctx.nukeLayer.remove(g);
                        ctx.score.reduce(n, m_type, m_color);
                    }
                }); //.fadeOut());                                
            }
            
            @Override
            public SlotMachineReward getReward(ComboFinder finder)
            {
                ReduceItemReward r = new ReduceItemReward(m_amount, m_type);
                r.m_color = finder.getDotColor();
                return r;
            }

            @Override
            public NObject asNObject()
            {
                NObject obj = new NObject();
                obj.put("type", meta().getType());
                obj.put("amount", getAmount());
                obj.put("details", m_type);
                return obj;
            }
        }
        
        public static class BoostReward extends SlotMachineReward
        {
            public static final SlotMachineRewardMeta META = new SlotMachineRewardMeta("Boost") {
                @Override
                public BoostReward parse(NObject obj)
                {
                    return new BoostReward(parseAmount(obj), obj.getAsString("details"));
                }
                
                @Override
                public FormItem getDetailsEditor(ListGridRecord record)
                {
                    String val = record.getAttribute("details");
                    MXSelectItem sel = new MXSelectItem();
                    sel.setShowTitle(false);
                    
                    LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
                    map.put("wild", "wild");
                    map.put("key", "key");
                    map.put("explody", "explody");
                    map.put("turner", "turner");
                    map.put("drop", "drop");
                    map.put("colorBomb", "colorBomb");
                    map.put("yinyang", "yinyang");
                    map.put("pick", "pick");
                    sel.setValueMap(map);
                    
                    sel.setValue((String) val);
                    return sel;
                }
                
                @Override
                public boolean hasDetails()
                {
                    return true;
                }

                @Override
                public SlotMachineReward getReward(ListGridRecord rec, int row) throws Exception
                {
                    int p = getAmount(rec, row);
                    String type = getDetails(rec, row);
                    return new BoostReward(p, type);
                }

                @Override
                public int initType(ListGridRecord record)
                {
                    record.setAttribute("amount", "1");
                    record.setAttribute("details", (String) null);
                    return SlotMachineEditor.DETAILS_INDEX;
                }
            };
            
            @Override
            public SlotMachineRewardMeta meta()
            {
                return META;
            }
            
            private String m_type;
            
            public BoostReward(int amount, String type)
            {
                super(amount);
                m_type = type;
            }
            
            @Override
            public void addTransitions(final Context ctx, RewardInfo rewardInfo, TransitionList list, CellList verboten)
            {
                Slot slot = rewardInfo.slotMachine.get(1);
                final int n = rewardInfo.reward.getAmount();
                
                final Group g = new Group();
                
                double w = 100;
                double h = 50;
                Rectangle r = new Rectangle(w, h);
                r.setX(-w/2);
                r.setY(-h/2);
                r.setFillColor(ColorName.WHITE);
                r.setStrokeColor(ColorName.BLUE);
                r.setCornerRadius(8);
                g.add(r);
                
                final Text text = new Text("x " + n);
                text.setFontFamily(Font.POINTS);
                text.setFontSize(25);
                text.setFontStyle("bold");
                text.setTextAlign(TextAlign.CENTER);
                text.setTextBaseLine(TextBaseLine.MIDDLE);
//                text.setStrokeColor(ColorName.BLACK);
//                text.setStrokeWidth(1);
                text.setFillColor(ColorName.BLACK);
                text.setX(w * 0.25);
                g.add(text);
                
                Item item = createItem(m_type, 0);
                IPrimitive<?> shape = item.createShape(50);
                shape.setX(w * -0.25);
                g.add(shape);
                
                list.add(new ParabolaTransition(ctx.state.x(slot.col), ctx.state.y(slot.row), ctx.state.x(ctx.state.numColumns / 2), ctx.state.y(ctx.state.numRows), g) {          
                    @Override
                    public void afterStart()
                    {
                        ctx.nukeLayer.add(g);
                        
//                        if (playSound)
//                            Sound.WOOSH.play();
                        Sound.CASH_REGISTER.play();
                    }

                    @Override
                    public void afterEnd()
                    {
                        ctx.nukeLayer.remove(g);
                        ctx.boostPanel.addBoost(m_type,  m_amount);
                    }
                }); //.fadeOut());                                
            }

            @Override
            public NObject asNObject()
            {
                NObject obj = new NObject();
                obj.put("type", meta().getType());
                obj.put("amount", getAmount());
                obj.put("details", m_type);
                return obj;
            }
        }
    }
    
    public static class SlotMachineInfo
    {
        public List<ItemFrequency> frequencies;
        public SlotMachineRewards  rewards;
        
        public static SlotMachineInfo createDefaultInfo()
        {
            SlotMachineInfo info = new SlotMachineInfo();
            
            List<ItemFrequency> frequencies = new ArrayList<ItemFrequency>();
            frequencies.add(new ItemFrequency(new Dot(0), 5));
            frequencies.add(new ItemFrequency(new Dot(1), 5));
            frequencies.add(new ItemFrequency(new Dot(2), 5));
            frequencies.add(new ItemFrequency(new Dot(3), 5));
            frequencies.add(new ItemFrequency(new Dot(4), 5));
            frequencies.add(new ItemFrequency(new Diamond(), 1));
            frequencies.add(new ItemFrequency(new Wild(false), 1));
            ItemFrequency.normalizeList(frequencies);
            info.frequencies = frequencies;
            
            SlotMachineRewards rewards = new SlotMachineRewards();
            SlotMachineReward reward = new PointReward(100);
            rewards.add(new SlotComboReward(Combo.findCombo("3 dots"), reward));
            info.rewards = rewards;
            
            return info;
        }
        
        public NObject asNObject()
        {
            NObject obj = new NObject();
            
            NArray freq = new NArray();
            obj.put("freq",  freq);
            
            for (ItemFrequency f : frequencies)
            {
                NObject row = new NObject();
                row.put("item", LevelParser.toJson(f.item, true));
                row.put("f", f.frequency);
                freq.push(row);                
            }
            
            obj.put("rewards", rewards.asNArray());
            
            return obj;
        }
        
        public static SlotMachineInfo parseInfo(NObject obj)
        {
            if (obj == null)
                return null;
            
            SlotMachineInfo info = new SlotMachineInfo();

            List<ItemFrequency> frequencies = new ArrayList<ItemFrequency>();            
            NArray freq = obj.getAsArray("freq");
            for (int i = 0, n = freq.size(); i < n; i++)
            {
                NObject row = freq.getAsObject(i);
                NObject itemObj = row.getAsObject("item");
                Item item = LevelParser.parseItem(itemObj);
                double f = row.getAsDouble("f");
                frequencies.add(new ItemFrequency(item, f));
            }
            info.frequencies = frequencies;
            
            NArray rewards = obj.getAsArray("rewards");         
            info.rewards = SlotMachineRewards.parse(rewards);
            
            return info;
        }
    }
    
    public static class SlotMachineEditor extends MXWindow
    {
        public static final int COMBO_INDEX = 1;
        public static final int TYPE_INDEX = 2;
        public static final int AMOUNT_INDEX = 3;
        public static final int DETAILS_INDEX = 4;
        public static final int DELETE_INDEX = 5;
        
        private UTabSet m_tabs;
        private MXListGrid m_grid;
        private Slot m_slot;
        private FrequencySliderGroup m_freqSliders;

        public SlotMachineEditor(Slot slot, Context ctx)
        {
            m_slot = slot;
            
            setTitle("Slot Machine Rewards");
            
            SlotMachineInfo info = slot.getSlotMachineInfo();
            if (info == null)
                info = SlotMachineInfo.createDefaultInfo();
            
            addItem(createPane(info));
            
            setWidth(850);
            setHeight(800);
            
            setLeft(50);
            setTop(50);
            
            setCanDragResize(true);
            setCanDragReposition(true);
            
            show();
        }

        private Canvas createPane(SlotMachineInfo info)
        {
            MXVBox pane = new MXVBox();
            pane.setPadding(5);
            pane.setMembersMargin(10);
            
            m_tabs = new UTabSet();
            
            m_tabs.addTab("Score Board", createScorePanel(info));
            m_tabs.addTab("Frequencies", createFreqPanel(info));

            pane.addMember(m_tabs);        

            MXButtonsPanel buttons = new MXButtonsPanel();
            
            buttons.add("Save", new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    SlotMachineInfo info = null;
                    try
                    {
                        info = getInfo();
                    }
                    catch (Exception e)
                    {
                        SC.warn(e.getMessage());
                        return;
                    }
                    
                    closeWindow();
                    saveInfo(info);
                    
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

        private Canvas createFreqPanel(SlotMachineInfo info)
        {
            MXVBox box = new MXVBox();
            m_freqSliders = new FrequencySliderGroup(false, info.frequencies, null);
            box.addMember(m_freqSliders);
            return box;
        }

        private SlotMachineInfo getInfo() throws Exception
        {
            //TODO validate
            SlotMachineInfo info = new SlotMachineInfo();
            
            info.frequencies = m_freqSliders.getFrequencies();

            SlotMachineRewards rewards = new SlotMachineRewards();
            int row = 1;
            for (ListGridRecord rec : m_grid.getRecords())
            {
                String combo = rec.getAttribute("combo");
                if (combo == null)
                    throw new Exception("Combo is not specified on row " + row);
                
                SlotComboReward scr = new SlotComboReward(Combo.findCombo(combo), SlotMachineRewards.getSlotMachineReward(rec, row));
                rewards.add(scr);
                
                row++;
            }
            info.rewards = rewards;
            
            return info;
        }
        
        protected void saveInfo(SlotMachineInfo info)
        {
            m_slot.setSlotMachineInfo(info);
        }
        
        private MXVBox createScorePanel(SlotMachineInfo info)
        {
            MXVBox box = new MXVBox();
            box.setPadding(5);
            box.setMembersMargin(10);
            
            m_grid = createScoreGrid(info.rewards);
            box.addMember(m_grid);
            
            MXButtonsPanel buttons = new MXButtonsPanel();
            buttons.add("Add", new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    Record rec = new Record();
//                    rec.setAttribute("combo", (String) null);
//                    rec.setAttribute("type", (String) null);
//                    rec.setAttribute("details", (String) null);
                    m_grid.addData(rec);
                    
                }
            });
            buttons.add("Delete All", new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    m_grid.removeAllRecords();
                }
            });
            box.addMember(buttons);
            
            return box;
        }
        
        private MXListGrid createScoreGrid(SlotMachineRewards rewards)
        {
            MXListGrid grid = new MXListGrid() {
                @Override
                public boolean canEditCell(int rowNum, int colNum)
                {
                    if (colNum == DELETE_INDEX || colNum == 0)    // Delete or Row Number
                        return false;
                    
                    if (colNum == DETAILS_INDEX)    // Details
                    {
                        String type = m_grid.getRecord(rowNum).getAttribute("type");
                        if (type == null)
                            return false;
                        
                        return SlotMachineRewards.getMeta(type).hasDetails();
                    }
                    return true;
                }
            };
            grid.setCanEdit(true);
            grid.setEditEvent(ListGridEditEvent.CLICK);
            grid.setShowRowNumbers(true);
            
            MXListGridField combo = new MXListGridField("combo", "Combo", ListGridFieldType.TEXT, 120);
            MXListGridField type = new MXListGridField("type", "Type", ListGridFieldType.TEXT, 120);
            MXListGridField amount = new MXListGridField("amount", "Amount", ListGridFieldType.TEXT, 120);
            MXListGridField details = new MXListGridField("details", "Details", ListGridFieldType.TEXT, 120);
            MXListGridField del = new MXListGridField("del", " ", ListGridFieldType.ICON);
            del.setIcon("delete2.png");
            
            type.setValueMap(SlotMachineRewards.getTypeMap());
            combo.setValueMap(Combo.getComboMap());
            
            combo.addChangedHandler(new ChangedHandler()
            {
                @Override
                public void onChanged(ChangedEvent event)
                {
                    int i = event.getRowNum();
                    m_grid.endEditing();
                    m_grid.startEditing(i, TYPE_INDEX);
                }
            });
            
            type.addChangedHandler(new ChangedHandler()
            {
                @Override
                public void onChanged(ChangedEvent event)
                {
                    int i = event.getRowNum();
                    String type = (String) event.getValue();
                    ListGridRecord rec = m_grid.getRecord(i);
                    rec.setAttribute("type", type);
                    int nextCol = selectedType(rec);
                    //m_grid.focusInCell(i, 2);
                    m_grid.endEditing();
                    m_grid.startEditing(i, nextCol);
                }
            });
            
            grid.setFields(combo, type, amount, details, del);
            
            del.addRecordClickHandler(new RecordClickHandler()
            {
                @Override
                public void onRecordClick(RecordClickEvent event)
                {
                    m_grid.removeData(event.getRecord());
                }
            });
            
            grid.setEditorCustomizer(new ListGridEditorCustomizer()
            {
                public FormItem getEditor(ListGridEditorContext context)
                {
                    ListGridField field = context.getEditField();
                    if (field.getName().equals("amount"))
                    {
                        ListGridRecord record = context.getEditedRecord();
                        String type = record.getAttribute("type");
                        if (type == null)
                            return context.getDefaultProperties();
                        
                        FormItem reward = SlotMachineRewards.createAmountEditor(type, record);                        
                        return reward;
                    }
                    else if (field.getName().equals("details"))
                    {
                        ListGridRecord record = context.getEditedRecord();
                        String type = record.getAttribute("type");
                        if (type == null)
                            return context.getDefaultProperties();
                        
                        FormItem reward = SlotMachineRewards.createDetailsEditor(type, record);                        
                        return reward;
                    }
                    return context.getDefaultProperties();
                }
            });  

            NArray rows = new NArray();
            for (SlotComboReward r : rewards)
            {
                NObject obj = r.reward.asNObject();
                obj.put("combo", r.combo.getName());
                rows.push(obj);
            }
            grid.setData(rows);
            
            return grid;
        }

        protected int selectedType(ListGridRecord record)
        {
            String type = record.getAttribute("type");
            return SlotMachineRewards.initType(type, record); 
        }
    }
}
