package com.enno.dotz.client;

import java.util.Random;

import com.ait.lienzo.client.core.animation.LayerRedrawManager;
import com.ait.lienzo.client.core.event.NodeMouseDownHandler;
import com.ait.lienzo.client.core.event.NodeMouseMoveHandler;
import com.ait.lienzo.client.core.event.NodeMouseOutHandler;
import com.ait.lienzo.client.core.event.NodeMouseUpHandler;
import com.ait.lienzo.client.core.shape.Layer;
import com.enno.dotz.client.Cell.Bubble;
import com.enno.dotz.client.DragConnectMode.ColorBombMode;
import com.enno.dotz.client.DragConnectMode.DropMode;
import com.enno.dotz.client.DragConnectMode.ExplodyMode;
import com.enno.dotz.client.DragConnectMode.IcePickMode;
import com.enno.dotz.client.DragConnectMode.KeyMode;
import com.enno.dotz.client.DragConnectMode.ReshuffleMode;
import com.enno.dotz.client.DragConnectMode.SpecialMode;
import com.enno.dotz.client.DragConnectMode.TurnMode;
import com.enno.dotz.client.DragConnectMode.WildCardMode;
import com.enno.dotz.client.SoundManager.Sound;
import com.enno.dotz.client.anim.AnimList;
import com.enno.dotz.client.anim.Transition.DropTransition;
import com.enno.dotz.client.anim.TransitionList;
import com.enno.dotz.client.anim.TransitionList.NukeTransitionList;
import com.enno.dotz.client.item.Blaster;
import com.enno.dotz.client.item.Bomb;
import com.enno.dotz.client.item.ColorBomb;
import com.enno.dotz.client.item.Domino;
import com.enno.dotz.client.item.Dot;
import com.enno.dotz.client.item.DotBomb;
import com.enno.dotz.client.item.Drop;
import com.enno.dotz.client.item.Explody;
import com.enno.dotz.client.item.IcePick;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.Key;
import com.enno.dotz.client.item.Mirror;
import com.enno.dotz.client.item.Rocket;
import com.enno.dotz.client.item.Turner;
import com.enno.dotz.client.item.Wild;
import com.enno.dotz.client.item.YinYang;
import com.google.gwt.event.shared.HandlerRegistration;

public abstract class ConnectMode
{
    protected Layer m_layer;
    protected GridState m_state;
    
    protected NodeMouseDownHandler m_mouseDownHandler;
    protected NodeMouseMoveHandler m_lineMoveHandler;
    protected NodeMouseUpHandler   m_mouseUpHandler;
    protected NodeMouseOutHandler m_mouseOutHandler;
    
    protected HandlerRegistration m_mouseDownReg;
    protected HandlerRegistration m_lineMoveReg;
    protected HandlerRegistration m_mouseUpReg;
    protected HandlerRegistration m_mouseOutReg;

    protected Context ctx;
    protected Config cfg;
    protected SpecialMode m_specialMode;
    
    protected ConnectMode(Context ctx, Layer layer)
    {
        this.ctx = ctx;
        this.cfg = ctx.cfg;
        m_layer = layer;
        m_state = ctx.state;
    }
    
    /**
     * Start listening to user input (mouse down)
     */
    public void start()
    {
        m_mouseDownReg = m_layer.addNodeMouseDownHandler(m_mouseDownHandler);
        m_mouseOutReg = m_layer.addNodeMouseOutHandler(m_mouseOutHandler);
        
        ctx.boostPanel.setActive(true);
    }
    
    public void stop()
    {
        ctx.boostPanel.setActive(false);
        
        m_mouseDownReg.removeHandler();
        m_mouseOutReg.removeHandler();
    }
    
    protected void redraw()
    {
        LayerRedrawManager.get().schedule(m_layer);
    }
    
    protected boolean isTriggeredBySingleClick(Cell cell)
    {
        return isTriggeredBySingleClick2(cell) || igniteBlaster(cell) || igniteBomb(cell);
    }
    
    protected boolean isTriggeredBySingleClick2(Cell cell)
    {
        return flipMirror(cell) || fireRocket(cell) || reshuffle(cell);
    }

    protected boolean reshuffle(Cell cell)
    {
        if (!cell.isLocked() && cell.item instanceof YinYang)
        {
            stop();

            ctx.dotLayer.remove(cell.item.shape);
            cell.item = null;
            
            ctx.state.reshuffle(new Runnable() {
                @Override
                public void run()
                {
                    m_state.processChain(new Runnable() {
                        public void run()
                        {
                            start(); // next move
                        }
                    });
                }
            });
            
            return true;
        }
        return false;
    }
    
    protected boolean flipMirror(Cell cell)
    {
        if (!cell.isLocked() && cell.item instanceof Mirror)
        {
            ctx.state.activateLasers(false);
            ((Mirror) cell.item).rotate(1);
            Sound.FLIP_MIRROR.play();
            ctx.dotLayer.redraw();
            ctx.state.activateLasers(true);
            
            stop();
            
            m_state.processChain(new Runnable() {
                public void run()
                {
                    start(); // next move
                }
            });
            
            return true;
        }
        return false;
    }
    
    protected boolean igniteBlaster(Cell cell)
    {
        if (!cell.isLocked() && cell.item instanceof Blaster)
        {
            ctx.score.usedBlaster();
            
            ctx.state.activateLasers(false);
            popBubble(cell);
            ((Blaster) cell.item).arm();
            Sound.FLIP_MIRROR.play(); //TODO sound
            ctx.dotLayer.redraw();
            ctx.state.activateLasers(true);
            
            stop();
            
            m_state.processChain(new Runnable() {
                public void run()
                {
                    start(); // next move
                }
            });
            
            return true;
        }
        return false;
    }

    protected boolean igniteBomb(Cell cell)
    {
        if (!cell.isLocked() && cell.item instanceof Bomb)
        {
            ctx.score.usedBomb();
            
            ctx.state.activateLasers(false);
            popBubble(cell);
            ((Bomb) cell.item).arm();
            Sound.FLIP_MIRROR.play(); //TODO sound
            ctx.dotLayer.redraw();
            ctx.state.activateLasers(true);
            
            stop();
            
            m_state.processChain(new Runnable() {
                public void run()
                {
                    start(); // next move
                }
            });
            
            return true;
        }
        return false;
    }

    protected void popBubble(Cell cell)
    {
        if (cell instanceof Bubble && !((Bubble) cell).isPopped())
        {
            cell.explode(null, 0);
        }
    }
    
    protected boolean fireRocket(Cell cell)
    {
        if (!cell.isLocked() && cell.item instanceof Rocket)
        {
            stop();
            m_state.fireRocket(cell, new Runnable() {
                public void run()
                {
                    start(); // next move
                }
            });
            return true;
        }
        return false;
    }    

    protected boolean startSpecialMode(Cell cell, int x, int y)
    {
        if (cell.isLocked() || cell.isUnpoppedBubble() || cell.item == null)
            return false;
        
        if (cell.item instanceof Turner)
        {
            m_specialMode = new TurnMode(cell, x, y, ctx, this);
            return true;
        }
        else if (cell.item instanceof Drop)
        {
            m_specialMode = new DropMode(cell, x, y, ctx, this);
            return true;
        }
        else if (cell.item instanceof Key)
        {
            m_specialMode = new KeyMode(cell, x, y, ctx, this);
            return true;
        }
        else if (cell.item instanceof IcePick)
        {
            m_specialMode = new IcePickMode(cell, x, y, ctx, this);
            return true;
        }
        else if (cell.item instanceof ColorBomb && this instanceof DragConnectMode)
        {
            m_specialMode = new ColorBombMode(cell, x, y, ctx, this);
            return true;
        }
        else if (cell.item instanceof ColorBomb && this instanceof ClickConnectMode)
        {
            m_specialMode = new ColorBombMode(cell, x, y, ctx, this);
            return true;
        }
        
        return false;
    }
    
    protected boolean isTriggeredMerge(Cell cell)
    {
        return new ClickMerge(ctx, m_state) {
            @Override
            protected void preMerge()
            {
                stop();
                ctx.state.activateLasers(false);                
            }
            
            @Override
            protected void postMerge()
            {
                ctx.state.activateLasers(true);
            }
            
            @Override
            protected Runnable process()
            {
                return new Runnable() {
                    @Override
                    public void run()
                    {
                        m_state.processChain(new Runnable() {
                            public void run()
                            {
                                start(); // next move
                            }
                        });
                    }
                };
            }
        }.isTriggeredMerge(cell);
    }
    
    public abstract static class ClickMerge
    {
        private Context ctx;
        private GridState m_state;
        
        public ClickMerge(Context ctx, GridState state)
        {
            this.ctx = ctx;
            m_state = state;
        }

        protected abstract void preMerge();
        protected abstract void postMerge();
        protected abstract Runnable process();
        
        public boolean isTriggeredMerge(final Cell cell)
        {
            final Item originItem = cell.item;
            if (cell.item instanceof ColorBomb)
            {
                final Cell neighbor = findSpecialNeighbor(cell);
                if (neighbor == null)
                    return false;
                
                final Item neighborItem = neighbor.item;
                
                mergeCells(cell, neighbor, new Runnable() {
                    public void run()
                    {
                        ctx.score.usedColorBomb();
                        if (neighborItem instanceof ColorBomb)
                        {
                            ctx.score.usedColorBomb();
                            ctx.scorePanel.update();
                            
                            GetSwapMatches.animateTotalBomb(m_state, process());
                        }
                        else
                        {
                            if (neighborItem instanceof Blaster)
                                ctx.score.usedBlaster();
                            else if (neighborItem instanceof Bomb)
                                ctx.score.usedBomb();
                            ctx.scorePanel.update();
                            
                            replace(cell, neighborItem);
                        }
                    }
                });
                return true;
            }
            if (cell.item instanceof Blaster)
            {
                final Cell neighbor = findSpecialNeighbor(cell);
                if (neighbor == null)
                    return false;
                
                final Item neighborItem = neighbor.item;
                
                mergeCells(cell, neighbor, new Runnable() {
                    public void run()
                    {
                        if (neighborItem instanceof ColorBomb)
                        {
                            ctx.score.usedColorBomb();
                            ctx.score.usedBlaster();
                            ctx.scorePanel.update();
                            
                            replace(cell, originItem);
                        }
                        else if (neighborItem instanceof Bomb)
                        {
                            ctx.score.usedBomb(); // NOTE: we didn't use a Blast yet!
                            ctx.scorePanel.update();
                            
                            Blaster b = (Blaster) originItem;
                            
                            Blaster newBlaster = new Blaster(b.isVertical(), false);
                            newBlaster.arm();
                            newBlaster.setWide(true);
                            newBlaster.setBothWays(true);
                            newBlaster.init(ctx);
                            newBlaster.addShapeToLayer(ctx.dotLayer);   
                            newBlaster.moveShape(m_state.x(cell.col), m_state.y(cell.row));
                            cell.item = newBlaster;
                            process().run();
                        }
                        else if (neighborItem instanceof Blaster)
                        {
                            ctx.score.usedBlaster();
                            ctx.scorePanel.update();
                            
                            Blaster b = (Blaster) originItem;
                            
                            Blaster newBlaster = new Blaster(b.isVertical(), false);
                            newBlaster.arm();
                            newBlaster.setBothWays(true);
                            newBlaster.init(ctx);
                            newBlaster.addShapeToLayer(ctx.dotLayer);   
                            newBlaster.moveShape(m_state.x(cell.col), m_state.y(cell.row));
                            cell.item = newBlaster;
                            process().run();
                        }
                    }
                });
                return true;
            }
            if (cell.item instanceof Bomb)
            {
                final Cell neighbor = findSpecialNeighbor(cell);
                if (neighbor == null)
                    return false;
                
                final Item neighborItem = neighbor.item;
                
                mergeCells(cell, neighbor, new Runnable() {
                    public void run()
                    {
                        if (neighborItem instanceof ColorBomb)
                        {
                            ctx.score.usedBomb();
                            ctx.score.usedColorBomb();
                            ctx.scorePanel.update();
                            
                            replace(cell, originItem);
                        }
                        else if (neighborItem instanceof Bomb)
                        {
                            ctx.score.usedBomb();   // only one so far!
                            ctx.scorePanel.update();
                            
                            Bomb bomb = new Bomb(2, false); // 2: 5x5
                            bomb.arm();
                            bomb.init(ctx);
                            bomb.addShapeToLayer(ctx.dotLayer);   
                            bomb.moveShape(m_state.x(cell.col), m_state.y(cell.row));
                            cell.item = bomb;
                            process().run();
                        }
                        else if (neighborItem instanceof Blaster)
                        {
                            ctx.score.usedBomb();   // NOTE we didn't use a Blaster yet!
                            ctx.scorePanel.update();
                            
                            Blaster b = (Blaster) neighborItem;
                            
                            Blaster newBlaster = new Blaster(b.isVertical(), false);
                            newBlaster.arm();
                            newBlaster.setWide(true);
                            newBlaster.setBothWays(true);                            
                            newBlaster.init(ctx);
                            newBlaster.addShapeToLayer(ctx.dotLayer);   
                            newBlaster.moveShape(m_state.x(cell.col), m_state.y(cell.row));
                            cell.item = newBlaster;
                            process().run();
                        }
                    }
                });
                return true;
            }
            
            return false;
        }
        
        private void replace(final Cell origin, Item item)
        {
            int n = item instanceof Blaster ? 14 : 18;
            
            CellList cells = new CellList();
            for (int i = 0; i < n; i++)    // TODO how many?
            {
                Cell cell = getRandomCell(cells);
                if (cell == null)
                    break;
            }
            
            AnimList list = new AnimList();
            for (final Cell cell : cells)
            {
                final Item newItem = item.copy();
                if (newItem instanceof Blaster)
                {
                    Blaster blaster = (Blaster) newItem;
                    blaster.arm();
                    blaster.setVertical(ctx.generator.getRandom().nextBoolean());
                }
                else if (newItem instanceof Bomb)
                {
                    ((Bomb) newItem).arm();
                }
                
                newItem.init(ctx);
                
                list.addNukeTransition("replaceOne", ctx, 100, new DropTransition(m_state.x(origin.col), m_state.y(origin.row), m_state.x(cell.col), m_state.y(cell.row), newItem) {
                    @Override
                    public void afterStart()
                    {
                        //Debug.p("replaceOne start");
                        newItem.addShapeToLayer(ctx.nukeLayer);
                        
                        Sound.WOOSH.play(); //TODO sound
                    }
                    
                    @Override
                    public void afterEnd()
                    {
                        //Debug.p("replaceOne end");
                        cell.explode(null, 0);
                        newItem.removeShapeFromLayer(ctx.nukeLayer);
                        newItem.addShapeToLayer(ctx.dotLayer);
                        cell.item = newItem;
                    }
                });
            }
            list.animate(process());
        }
        
        private Cell getRandomCell(CellList list)
        {
            Random rnd = ctx.generator.getRandom();
            for (int i = 0; i < 200; i++)
            {
                int col = rnd.nextInt(m_state.numColumns);
                int row = rnd.nextInt(m_state.numRows);
                if (list.containsCell(col, row))
                    continue;
                
                Cell c = m_state.cell(col, row);
                if (c.isLocked() || !isReplacableDot(c.item))
                    continue;
                
                list.add(c);
                return c;
            }
            return null; // took too long
        }
        
        private boolean isReplacableDot(Item item)
        {
            if (item == null)
                return false;
            
            return item instanceof Dot || item instanceof DotBomb || item instanceof Wild || item instanceof Domino;
        }

        private void mergeCells(final Cell cell, final Cell neighbor, final Runnable callback)
        {
            preMerge();
            
            final Item item = neighbor.item;
            TransitionList list = new NukeTransitionList("merge", ctx, 500);
            list.add(new DropTransition(m_state.x(neighbor.col), m_state.y(neighbor.row), m_state.x(cell.col), m_state.y(cell.row), item) {
                @Override
                public void afterStart()
                {
                    //Debug.p("spawn " + src);
                    item.removeShapeFromLayer(ctx.dotLayer);
                    item.addShapeToLayer(ctx.nukeLayer);
                    
                    Sound.WOOSH.play(); //TODO sound
                }
                
                @Override
                public void afterEnd()
                {
                    item.removeShapeFromLayer(ctx.nukeLayer);
                    neighbor.item = null;
                    
                    cell.item.removeShapeFromLayer(ctx.dotLayer);
                    cell.item = null;
                    
//                    LayerRedrawManager.get().schedule(ctx.dotLayer);
//                    LayerRedrawManager.get().schedule(ctx.nukeLayer);

                    postMerge();
                    
                    callback.run();
                }
            });
            list.run();
        }
    
        protected Cell findSpecialNeighbor(Cell cell)
        {
            Cell cell2;
            // Look for ColorBomb first
            if ((cell2 = canBeMatched(cell.col - 1, cell.row)) != null && cell2.item instanceof ColorBomb)
                return cell2;
            if ((cell2 = canBeMatched(cell.col + 1, cell.row)) != null && cell2.item instanceof ColorBomb)
                return cell2;
            if ((cell2 = canBeMatched(cell.col, cell.row - 1)) != null && cell2.item instanceof ColorBomb)
                return cell2;
            if ((cell2 = canBeMatched(cell.col, cell.row + 1)) != null && cell2.item instanceof ColorBomb)
                return cell2;
            
            if ((cell2 = canBeMatched(cell.col - 1, cell.row)) != null && (cell2.item instanceof Blaster || cell2.item instanceof Bomb))
                return cell2;
            if ((cell2 = canBeMatched(cell.col + 1, cell.row)) != null && (cell2.item instanceof Blaster || cell2.item instanceof Bomb))
                return cell2;
            if ((cell2 = canBeMatched(cell.col, cell.row - 1)) != null && (cell2.item instanceof Blaster || cell2.item instanceof Bomb))
                return cell2;
            if ((cell2 = canBeMatched(cell.col, cell.row + 1)) != null && (cell2.item instanceof Blaster || cell2.item instanceof Bomb))
                return cell2;
            
            return null;
        }
        
        private Cell canBeMatched(int col, int row)
        {
            if (!m_state.isValidCell(col, row))
                return null;
            
            Cell cell = m_state.cell(col, row);
            if (cell.item == null || cell.isLocked())
                return null;
            
            return cell;
        }
    }
    
    public void startBoostMode(Item item, Runnable removeItem)
    {
        if (m_specialMode != null)
        {
            m_specialMode.cancel();
        }
        
        if (item instanceof Turner)
        {
            m_specialMode = new TurnMode(item, ctx, this, removeItem);
        }
        else if (item instanceof Drop)
        {
            m_specialMode = new DropMode(item, ctx, this, removeItem);
        }
        else if (item instanceof ColorBomb)
        {
            m_specialMode = new ColorBombMode(item, ctx, this, removeItem);
        }
        else if (item instanceof Wild)
        {
            m_specialMode = new WildCardMode(item, ctx, this, removeItem);
        }
        else if (item instanceof Explody)
        {
            m_specialMode = new ExplodyMode(item, ctx, this, removeItem);
        }
        else if (item instanceof YinYang)
        {
            m_specialMode = new ReshuffleMode(item, ctx, this, removeItem);
            m_specialMode.done(null, false);
        }
        else if (item instanceof Key)
        {
            m_specialMode = new KeyMode(item, ctx, this, removeItem);
        }
        else if (item instanceof IcePick)
        {
            m_specialMode = new IcePickMode(item, ctx, this, removeItem);
        }
    }
    
    public void cancelSpecialMode()
    {
        m_specialMode = null;
    }

    public void cancelBoostMode()
    {
        if (m_specialMode != null)
        {
            m_specialMode.cancel();
            m_specialMode = null;
        }
    }
}
