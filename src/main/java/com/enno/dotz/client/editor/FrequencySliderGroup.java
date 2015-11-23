package com.enno.dotz.client.editor;

import java.util.ArrayList;
import java.util.List;

import com.enno.dotz.client.Config;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.Direction;
import com.enno.dotz.client.Generator;
import com.enno.dotz.client.Generator.ItemFrequency;
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
import com.enno.dotz.client.item.Mirror;
import com.enno.dotz.client.item.Rocket;
import com.enno.dotz.client.item.Turner;
import com.enno.dotz.client.item.Wild;
import com.enno.dotz.client.item.YinYang;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

public class FrequencySliderGroup extends HLayout
{
    private Context ctx;
    private List<FrequencySlider> m_list = new ArrayList<FrequencySlider>();
    
    public FrequencySliderGroup(boolean isNew, Config level)
    {
        ctx = new Context(); // just used for drawing the sliders
        ctx.cfg = new Config();      //TODO could copy the colors later       
        
        VLayout left = new VLayout();
        VLayout right = new VLayout();
        addMember(left);
        addMember(right);
        
        int index = 0;
        
        List<FrequencySlider> dots = new ArrayList<FrequencySlider>();
        for (int col = 0; col < Config.MAX_COLORS; col++)
        {
            FrequencySlider slider = createDotSlider(index++, col);
            dots.add(slider);
            
            m_list.add(slider);
            left.addMember(slider);
        }
        
        List<FrequencySlider> animals = new ArrayList<FrequencySlider>();        
        for (int col = 0; col < Config.MAX_COLORS; col++)
        {
            FrequencySlider slider = createAnimalSlider(index++, col);
            animals.add(slider);
            
            m_list.add(slider);
            right.addMember(slider);
        }
        
        FrequencySlider wild = createWildSlider(index++);
        m_list.add(wild);
        left.addMember(wild);
        
        FrequencySlider anchor = createAnchorSlider(index++);
        m_list.add(anchor);
        left.addMember(anchor);
        
        FrequencySlider fire = createFireSlider(index++);
        m_list.add(fire);
        left.addMember(fire);
     
        FrequencySlider rocket = createRocketSlider(index++);
        m_list.add(rocket);
        left.addMember(rocket);

        FrequencySlider yinyang = createYinYangSlider(index++);
        m_list.add(yinyang);
        left.addMember(yinyang);

        FrequencySlider egg = createEggSlider(index++);
        m_list.add(egg);
        left.addMember(egg);

        FrequencySlider turner = createTurnerSlider(index++);
        m_list.add(turner);
        left.addMember(turner);

        FrequencySlider colorBomb = createColorBombSlider(index++);
        m_list.add(colorBomb);
        left.addMember(colorBomb);

        FrequencySlider pick = createIcePickSlider(index++);
        m_list.add(pick);
        left.addMember(pick);

        FrequencySlider knight = createKnightSlider(index++);
        m_list.add(knight);
        right.addMember(knight);
        
        FrequencySlider clock = createClockSlider(index++);
        m_list.add(clock);
        right.addMember(clock);
        
        FrequencySlider mirror = createMirrorSlider(index++);
        m_list.add(mirror);
        right.addMember(mirror);
        
        FrequencySlider laser = createLaserSlider(index++);
        m_list.add(laser);
        right.addMember(laser);
        
        FrequencySlider bomb = createBombSlider(index++);
        m_list.add(bomb);
        right.addMember(bomb);
        
        FrequencySlider domino = createDominoSlider(index++);
        m_list.add(domino);
        right.addMember(domino);
        
        FrequencySlider drop = createDropSlider(index++);
        m_list.add(drop);
        right.addMember(drop);
        
        FrequencySlider key = createKeySlider(index++);
        m_list.add(key);
        right.addMember(key);
        
        if (isNew)
        {
            for (FrequencySlider slider : dots)
            {
                slider.initFrequency(20);
            }
            dots.get(5).initFrequency(0);
            dots.get(5).setSelected(false);
        }
        else
        {
            Generator g = level.generator;
            if (g != null)
            {
                double total = 0;
                for (ItemFrequency f : g.getFrequencies())
                {
                    total += f.frequency;
                }
                
                for (ItemFrequency f : g.getFrequencies())
                {
                    double freq = f.frequency * 100 / total;
                    
                    Item item = f.item;
                    if (item instanceof Fire)
                        fire.initFrequency(freq);
                    else if (item instanceof Anchor)
                        anchor.initFrequency(freq);
                    else if (item instanceof Wild)
                        wild.initFrequency(freq);
                    else if (item instanceof Knight)
                        knight.initFrequency(freq);
                    else if (item instanceof Clock)
                        clock.initFrequency(freq);
                    else if (item instanceof Rocket)
                        rocket.initFrequency(freq);
                    else if (item instanceof Laser)
                        laser.initFrequency(freq);
                    else if (item instanceof Mirror)
                        mirror.initFrequency(freq);
                    else if (item instanceof YinYang)
                        yinyang.initFrequency(freq);
                    else if (item instanceof Egg)
                        egg.initFrequency(freq);
                    else if (item instanceof DotBomb)
                        bomb.initFrequency(freq);
                    else if (item instanceof Domino)
                        domino.initFrequency(freq);
                    else if (item instanceof Turner)
                        turner.initFrequency(freq);
                    else if (item instanceof Drop)
                        drop.initFrequency(freq);
                    else if (item instanceof ColorBomb)
                        colorBomb.initFrequency(freq);
                    else if (item instanceof Key)
                        key.initFrequency(freq);
                    else if (item instanceof IcePick)
                        pick.initFrequency(freq);
                    else if (item instanceof Animal)
                    {
                        int color = ((Animal) item).getColor();                    
                        animals.get(color).initFrequency(freq);
                    }
                    else if (item instanceof Dot)
                    {
                        int color = ((Dot) item).getColor();                    
                        dots.get(color).initFrequency(freq);
                    }
                }
            }
        }
    }
    
    private FrequencySlider createWildSlider(int index)
    {
        Wild dot = new Wild();
        dot.setContext(ctx);
        return new FrequencySlider(index, dot, this) {
            
        };
    }

    private FrequencySlider createFireSlider(int index)
    {
        Fire dot = new Fire();
        dot.setContext(ctx);
        return new FrequencySlider(index, dot, this) {
            
        };
    }

    private FrequencySlider createMirrorSlider(int index)
    {
        Mirror dot = new Mirror(false);
        dot.setContext(ctx);
        return new FrequencySlider(index, dot, this) {
            
        };
    }

    private FrequencySlider createLaserSlider(int index)
    {
        Laser dot = new Laser(Direction.EAST);
        dot.setContext(ctx);
        return new FrequencySlider(index, dot, this) {
            
        };
    }

    private FrequencySlider createRocketSlider(int index)
    {
        Rocket dot = new Rocket(Direction.EAST);
        dot.setContext(ctx);
        return new FrequencySlider(index, dot, this) {
            
        };
    }

    protected FrequencySlider createDotSlider(int index, int color)
    {
        Dot dot = new Dot(color);
        dot.setContext(ctx);
        return new FrequencySlider(index, dot, this) {
            
        };
    }

    protected FrequencySlider createAnimalSlider(int index, int color)
    {
        Animal dot = new Animal(color, 10, Animal.Type.DEFAULT);
        dot.setContext(ctx);
        return new FrequencySlider(index, dot, this) {
            
        };
    }
    
    protected FrequencySlider createAnchorSlider(int index)
    {
        Anchor dot = new Anchor();
        dot.setContext(ctx);
        return new FrequencySlider(index, dot, this) {
            
        };
    }

    protected FrequencySlider createKnightSlider(int index)
    {
        Knight dot = new Knight(1);
        dot.setContext(ctx);
        return new FrequencySlider(index, dot, this) {
            
        };
    }

    protected FrequencySlider createIcePickSlider(int index)
    {
        IcePick dot = new IcePick();
        dot.setContext(ctx);
        return new FrequencySlider(index, dot, this) {
            
        };
    }

    protected FrequencySlider createClockSlider(int index)
    {
        Clock dot = new Clock(1);
        dot.setContext(ctx);
        return new FrequencySlider(index, dot, this) {
            
        };
    }

    protected FrequencySlider createYinYangSlider(int index)
    {
        YinYang dot = new YinYang();
        dot.setContext(ctx);
        return new FrequencySlider(index, dot, this) {
            
        };
    }

    protected FrequencySlider createEggSlider(int index)
    {
        Egg dot = new Egg();
        dot.setContext(ctx);
        return new FrequencySlider(index, dot, this) {
            
        };
    }

    protected FrequencySlider createKeySlider(int index)
    {
        Key dot = new Key();
        dot.setContext(ctx);
        return new FrequencySlider(index, dot, this) {
            
        };
    }

    protected FrequencySlider createBombSlider(int index)
    {
        DotBomb dot = new DotBomb(new Dot(0), 9);
        dot.setContext(ctx);
        return new FrequencySlider(index, dot, this) {
            
        };
    }
    
    protected FrequencySlider createColorBombSlider(int index)
    {
        ColorBomb dot = new ColorBomb();
        dot.setContext(ctx);
        return new FrequencySlider(index, dot, this) {
            
        };
    }

    protected FrequencySlider createDominoSlider(int index)
    {
        Domino dot = new Domino(3, 6, true);
        dot.setContext(ctx);
        return new FrequencySlider(index, dot, this) {
            
        };
    }

    protected FrequencySlider createDropSlider(int index)
    {
        Drop dot = new Drop();
        dot.setContext(ctx);
        return new FrequencySlider(index, dot, this) {
            
        };
    }

    protected FrequencySlider createTurnerSlider(int index)
    {
        Turner dot = new Turner(1);
        dot.setContext(ctx);
        return new FrequencySlider(index, dot, this) {
            
        };
    }

    public void setSliderValue(int index, double freq)
    {
        double left = 100 - freq;
        
        double total = 0;
        int n = 0;
        for (FrequencySlider slider : m_list)
        {
            if (slider.getIndex() != index && slider.isSelected())
            {
                total += slider.getFrequency();
                n++;
            }
        }
        for (FrequencySlider slider : m_list)
        {
            if (slider.getIndex() != index && slider.isSelected())
            {
                if (total > 0)
                    slider.setFrequency(slider.getFrequency() * left / total);
                else
                    slider.setFrequency(left / n);
            }
        }
    }

    public void setSelected(int index, boolean selected)
    {
        if (selected)
        {
            // Give new one the leftovers
            double total = 0;
            for (FrequencySlider slider : m_list)
            {
                if (slider.getIndex() != index && slider.isSelected())
                {
                    total += slider.getFrequency();
                }
            }
            
            m_list.get(index).setFrequency(100 - total);
        }
        else
        {
            double total = 0;
            int n = 0;
            for (FrequencySlider slider : m_list)
            {
                if (slider.getIndex() != index && slider.isSelected())
                {
                    n++;
                    total += slider.getFrequency();
                }
            }
            if (n > 0)
            {
                if (total == 0)
                {
                    double freq = 100 / n;
                    for (FrequencySlider slider : m_list)
                    {
                        if (slider.getIndex() != index && slider.isSelected())
                        {
                            slider.setFrequency(freq);
                        }
                    }
                }
                else
                {
                    double factor = 100 / total;
                    for (FrequencySlider slider : m_list)
                    {
                        if (slider.getIndex() != index && slider.isSelected())
                        {
                            slider.setFrequency(slider.getFrequency() * factor);
                        }
                    }
                }
            }
        }
    }

    public boolean validate()
    {
        for (FrequencySlider s : m_list)
        {
            if (s.isSelected())
            {
                if (s.getFrequency() > 0)
                    return true;
            }
        }
        
        SC.warn("Invalid generator frequencies. There must be at least one frequency > 0");
        return false;
    }

    public void prepareSave(Config level)
    {
        Generator g = level.generator;
        g.getFrequencies().clear();
        
        for (FrequencySlider s : m_list)
        {
            if (s.isSelected())
            {
                double freq = s.getFrequency();
                if (freq > 0)
                    g.add(new ItemFrequency(s.getItem(), freq));
            }
        }
    }

    public void equalizeDotFreq()
    {
        double total = 0;
        int n = 0;
        for (FrequencySlider slider : m_list)
        {
            if (slider.getItem() instanceof Dot && slider.isSelected())
            {
                total += slider.getFrequency();
                n++;
            }
        }
        for (FrequencySlider slider : m_list)
        {
            if (slider.getItem() instanceof Dot && slider.isSelected())
            {
                slider.setFrequency(total / n);
            }
        }
    }
}
