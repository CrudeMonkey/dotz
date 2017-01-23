package com.enno.dotz.client.anim;

import com.enno.dotz.client.Cell;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.GridState;
import com.enno.dotz.client.SoundManager.Sound;
import com.enno.dotz.client.anim.Transition.RadioActiveTransition;
import com.enno.dotz.client.anim.TransitionList.NukeTransitionList;
import com.enno.dotz.client.item.Dot;
import com.enno.dotz.client.item.DotBomb;
import com.enno.dotz.client.item.Item;

public class RadioActives
{
    private Context ctx;
    private Config cfg;
    private GridState state;

    public RadioActives(TransitionList list, Context ctx)
    {
        this.ctx = ctx;
        this.cfg = ctx.cfg;
        this.state = ctx.state;
        
        addTransitions(list);
    }

    public static TransitionList createTransitions(final Context ctx)
    {
        return new NukeTransitionList("radio actives", ctx, ctx.cfg.growFireDuration)
        {
            @Override
            public void init()
            {
                new RadioActives(this, ctx);
            }
        };
    }
    
    private void addTransitions(TransitionList list)
    {
        for (int row = 0; row < cfg.numRows; row++)
        {
            for (int col = 0; col < cfg.numColumns; col++)
            {
                final Cell cell = state.cell(col, row);
                final Item oldItem = cell.item;
                if (oldItem != null && oldItem.isRadioActive())
                {
                    double x = state.x(cell.col);
                    double y = state.y(cell.row);
                    
                    final Item newItem = ctx.generator.changeColor(ctx, oldItem);
                    newItem.moveShape(x, y);
                    newItem.shape.setAlpha(0);
                    
                    final Dot newDot = newItem instanceof DotBomb ? ((DotBomb) newItem).getDot() : (Dot) newItem;
                    final Dot oldDot = oldItem instanceof DotBomb ? ((DotBomb) oldItem).getDot() : (Dot) oldItem;
                    newDot.copyRadioActiveInfo(oldDot);
                            
                    final boolean play = list.isEmpty();
                    list.add(new RadioActiveTransition(x, y) {
                        @Override
                        public void afterStart()
                        {
                            if (play)
                                Sound.SWAP_RADIOACTIVE.play();
                            
                            newItem.addShapeToLayer(ctx.nukeLayer);
                        }
                        
                        @Override
                        public void afterEnd()
                        {
                            oldItem.removeShapeFromLayer(ctx.dotLayer);
                            newItem.removeShapeFromLayer(ctx.nukeLayer);
                            newItem.addShapeToLayer(ctx.dotLayer);
                            cell.item = newItem;
                        }
                        
                        @Override
                        public void move(double perc)
                        {
                            double scale = perc < 0.5 ? (1 - perc * 2) : 2 * (perc - 0.5); // [0,0.5> -> [1,0>    [0.5,1> -> [0,1>
                            
                            newDot.copyRadioActiveInfo(oldDot);
                            newItem.shape.setAlpha(perc);
                            
                            newItem.shape.setScale(scale);
                            oldItem.shape.setScale(scale);
                        }
                    });
                }
            }
        }
    }
}
