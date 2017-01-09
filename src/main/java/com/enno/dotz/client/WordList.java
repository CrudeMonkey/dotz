package com.enno.dotz.client;

import java.util.List;
import java.util.Set;

import com.enno.dotz.client.io.ClientRequest;
import com.enno.dotz.client.io.MAsyncCallback;
import com.enno.dotz.client.item.Dot;
import com.enno.dotz.client.item.DotBomb;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.Wild;
import com.enno.dotz.client.util.Debug;
import com.enno.dotz.shared.BinSearchDictionary;
import com.enno.dotz.shared.Timer;
import com.enno.dotz.shared.WordFinder;

public class WordList
{
    private static BinSearchDictionary s_dictionary;

    public static String getWord(String word, Set<String> invalidWords)
    {
        return s_dictionary.find(word.toLowerCase(), invalidWords);
    }
    
    public static void loadWordList(boolean generateLetters, final Runnable next)
    {
        if (!generateLetters || s_dictionary != null)
        {
            next.run();
            return;
        }
        
        ClientRequest.getWordList(new MAsyncCallback<String>()
        {
            @Override
            public void onSuccess(String list)
            {
                s_dictionary = new BinSearchDictionary(list);
                next.run();
            }
        });
    }

    public static List<String> findWordList(GridState state, Context ctx)
    {
        Timer timer = new Timer("findWords");
        
        WordFinder w = new WordFinder(state.numColumns, state.numRows, ctx.cfg.goals.getWords(), s_dictionary);
        w.maxWordLength = ctx.generator.maxWordLength;
//        w.minWordLength = 5;
        
        for (int row = 0; row < state.numRows; row++)
        {
            for (int col = 0; col < state.numColumns; col++)
            {
                Cell cell = state.cell(col, row);
                Item item = cell.item;
                if (cell.item instanceof DotBomb)
                    item = ((DotBomb) item).getDot();
                
                if (item instanceof Dot)
                {
                    Dot dot = (Dot) item;
                    char c = dot.getLetter().charAt(0);
                    int pts = dot.getLetterPoints();
                    int wordMultiplier = 0;
                    
                    LetterMultiplier mult = dot.getLetterMultiplier();
                    if (mult != null)
                    {
                        if (mult.isWordMultiplier())
                            wordMultiplier = mult.getMultiplier();
                        else // letter multiplier
                            pts *= mult.getMultiplier();  
                    }
                    
                    //NOTE this favors words with more points over longer words... Do we really care?
//                    w.set(col, row, c, pts, wordMultiplier);
                    w.set(col, row, c, 1, 0);
                }
                else if (item instanceof Wild)
                {
                    w.set(col, row, '?', 0, 0);
                }
                else
                {
                    w.setUnusable(col, row);
                }
            }
        }
        
        w.findWords();
        
        Debug.p(timer.toString());
        
        List<String> list = w.getWordList();
        return list;
    }
}
