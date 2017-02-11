package com.enno.dotz.client;

import java.util.List;

import com.ait.lienzo.client.core.animation.LayerRedrawManager;
import com.ait.lienzo.client.core.shape.FastLayer;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.client.widget.LienzoPanel;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.IColor;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.google.gwt.dom.client.Style.FontWeight;

public class WordDisplayPanel extends LienzoPanel
{
    private static final int HEIGHT = 50;
    
    private Context ctx;
    private Layer m_layer;
    private int m_width;

    private Text m_findWord;

    public WordDisplayPanel(Context ctx)
    {
        super(ctx.gridWidth, HEIGHT);

        this.ctx = ctx;
        m_width = ctx.cfg.numColumns * ctx.cfg.size;
        
        setBackgroundColor(ColorName.WHITE);
        
        m_layer = new FastLayer();
        add(m_layer);
    }
    
    public void setFindWord(String word)
    {
        if (m_findWord == null)
        {
            m_findWord = createText(word, ColorName.BLACK);
            m_layer.add(m_findWord);
            
            redraw();
        }
        else
        {
            m_findWord.setText(word);
            redraw();
        }
    }
    
    public Text createText(String word, IColor color)
    {
        Text text = new Text(word);
        
        text.setFillColor(color);
        text.setFontSize(30);
        text.setFontStyle(FontWeight.BOLD.getCssName());
        text.setTextAlign(TextAlign.CENTER);
        text.setTextBaseLine(TextBaseLine.MIDDLE); // y position is position of top of the text
        text.setX(m_width / 2);
        text.setY(HEIGHT / 2);
        
        return text;
    }
    
    public void redraw()
    {
        LayerRedrawManager.get().schedule(m_layer);
    }
    
    public void addFoundWord(Text text)
    {
        if (m_findWord != null)
            m_findWord.setVisible(false);
        
        m_layer.add(text);
        
        redraw();
    }
    
    public void removeFoundWord(Text text)
    {
        m_layer.remove(text);
        
        if (m_findWord != null)
            m_findWord.setVisible(true);

        redraw();
    }

    public static class FindWordList
    {
        private List<String> m_wordList;
        private WordDisplayPanel m_display;
        
        public FindWordList(Context ctx)
        {
            m_display = ctx.wordDisplayPanel;
            m_wordList = WordList.findWordList(ctx.state, ctx);
            //TODO what if found words < goal.words?
            String word = m_wordList.get(0);
            
            m_display.setFindWord(word);
        }
        
        public boolean foundWord(String word)
        {
            if (word.equals(m_wordList.get(0)))
            {
                m_wordList.remove(0);
                
                if (m_wordList.size() > 0)
                    m_display.setFindWord(m_wordList.get(0));
                
                return true;
            }
            return false;
        }
        
        public String getCurrentWord()
        {
            return m_wordList.size() > 0 ? m_wordList.get(0) : null;
        }
        
        public void undoCurrentWord(String word)
        {
            if (word == null)
                return;
            
            if (!word.equals(getCurrentWord()))
            {
                m_wordList.add(0, word);
                m_display.setFindWord(word);
            }
        }
        
        public void redoCurrentWord(String word)
        {
            if (word == null)
                return;
            
            if (!word.equals(getCurrentWord()))
            {
                m_wordList.remove(0);
                m_display.setFindWord(word);
            }
        }
    }
}
