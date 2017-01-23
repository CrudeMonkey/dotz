package com.enno.dotz.client.anim;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ait.lienzo.client.core.animation.IAnimationCallback;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.anim.TransitionList.NukeTransitionList;
import com.enno.dotz.client.util.CallbackChain;

public class AnimList extends CallbackChain
{
    public AnimList()
    {
        
    }
    
    public void animate(Runnable doneCallback)
    {
        m_doneCallback = doneCallback;
       
        run();
    }
    
    public void addNukeTransition(String name, Context ctx, double duration, Transition trans)
    {
        TransitionList list = new NukeTransitionList(name, ctx, duration);
        list.add(trans);
        add(list);
    }
    
    public void addBounce()
    {
        Set<Integer> last = new HashSet<Integer>();
        List<Callback> callbacks = getCallbacks();
        for (int i = callbacks.size() - 1; i >= 0; i--)
        {
            TransitionList list = (TransitionList) callbacks.get(i);
            for (IAnimationCallback anim : list.getTransitions())
            {
                Transition tr = (Transition) anim;
                Integer id = tr.item.id; //TODo can we do without id?
                if (!last.contains(id))
                {
                    tr.bounce = true;
                    last.add(id);
                }
            }
        }
    }
}