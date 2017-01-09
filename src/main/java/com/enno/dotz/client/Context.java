package com.enno.dotz.client;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.ait.lienzo.client.core.animation.LayerRedrawManager;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.enno.dotz.client.anim.Pt;
import com.enno.dotz.shared.WordFinder.ResultList;
import com.google.gwt.dom.client.Style.FontWeight;

public class Context
{
    public Config cfg;
    public GridState state;
    public Score score = new Score();
    
    public Generator generator;
    public Random beastRandom;

    public Layer backgroundLayer;
    public Layer iceLayer;
    public Layer dotLayer;
    public Layer doorLayer;
    public Layer nukeLayer;
    public Layer laserLayer;
    public Layer connectLayer;
    
    public DotzGridPanel gridPanel;
    public BoostPanel boostPanel;
    public ScorePanel scorePanel;
    public StatsPanel statsPanel;

    public Pt lastMove; // location of last connect
    
    public boolean killed;
    
    public Set<String> guessedWords = new HashSet<String>();
    public ResultList bestWords = new ResultList(20);
    public FindWordList findWordList;
    
    public boolean isWild(Integer color)
    {
        return cfg.isWild(color);
    }

    public void init()
    {
        generator = cfg.generator.copy();
        beastRandom = new Random(generator.getSeed());
    }
    
    public void prepareWords()
    {
        if (generator.findWords)
        {
            findWordList = new FindWordList(this);
        }
    }
    
    public static class FindWordList
    {
        private Text m_text;
        private Layer m_layer;

        private List<String> m_wordList;
        
        public FindWordList(Context ctx)
        {
            m_wordList = WordList.findWordList(ctx.state, ctx);
            //TODO what if found words < goal.words?
            String word = m_wordList.get(0);
            
            // see also ShowWordCallback
            double width = ctx.cfg.numColumns * ctx.cfg.size;
            m_layer = ctx.dotLayer;
        
            m_text = new Text(word);
            //m_text.setFillColor(ColorName.BLACK);
            m_text.setFillColor(ColorName.BLACK);
            m_text.setFontSize(30);
            m_text.setFontStyle(FontWeight.BOLD.getCssName());
            m_text.setTextAlign(TextAlign.CENTER);
            m_text.setTextBaseLine(TextBaseLine.MIDDLE); // y position is position of top of the text
            m_text.setY(ctx.cfg.size / 2);
            m_text.setX(width / 2);
//            m_text.setAlpha(0);
            
            m_layer.add(m_text);
        }
        
        public boolean foundWord(String word)
        {
            if (word.equals(m_wordList.get(0)))
            {
                m_wordList.remove(0);
                
                if (m_wordList.size() > 0)
                m_text.setText(m_wordList.get(0));
                
                return true;
            }
            return false;
        }

        public void setVisible(boolean visible)
        {
            m_text.setVisible(visible);
            LayerRedrawManager.get().schedule(m_layer);
        }
    }
}
