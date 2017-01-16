package com.enno.dotz.client;

import java.util.Random;

import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.item.Fire;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.Knight;
import com.enno.dotz.client.item.Laser;
import com.enno.dotz.client.util.CallbackChain;
import com.enno.dotz.client.util.CallbackChain.Callback;

public class ReshuffleSwap extends Reshuffle
    {
        public ReshuffleSwap(Context ctx, Random rnd)
        {
            super(ctx, rnd);
        }
        
        @Override
        public boolean mustReshuffle()
        {
            for (int row = 0; row < numRows; row++)
            {
                for (int col = 0; col < numColumns; col++)
                {
                    Cell cell = state.cell(col, row);
                    if (cell.isLocked())
                        continue;
                    
                    Item item = cell.item;
                    if (item == null)
                        continue;
                    
                    if (isActionItem(item))
                        return false;
                }
            }
            
            // Try vertical swaps
            for (int row = 0; row < numRows - 1; row++)
            {
                for (int col = 0; col < numColumns; col++)
                {
                    if (trySwap(col, row, col, row + 1))
                        return false;
                }
            }
            
            // Try horizontal swaps
            for (int row = 0; row < numRows; row++)
            {
                for (int col = 0; col < numColumns - 1; col++)
                {
                    if (trySwap(col, row, col + 1, row))
                        return false;
                }
            }
            
//            Debug.p("mustReshuffle=true");            
            
            if (m_initialState == null)
                m_initialState = getReshuffleCells();
            
            return true;
        }
        
        protected void clear()
        {
            m_initialState = null;
        }
        
        public void reshuffle(final Runnable nextMoveCallback, final Runnable reshuffleFailedCallback)
        {
            boolean success = doReshuffle(MAX_RESHUFFLES);
            if (success)
            {
                animateReshuffle(new Runnable() {
                    @Override
                    public void run()
                    {
                        // Do explodeLoop after reshuffle                
                        CallbackChain chain = new CallbackChain();
                        chain.add(state.transitions());
                        chain.add(state.explodeLoop(false, false)); // don't check clocks
                        chain.add(new Callback() {
                            @Override
                            public void run()
                            {
                                clear();
                                if (mustReshuffle())
                                    reshuffle(nextMoveCallback, reshuffleFailedCallback);
                                else
                                    nextMoveCallback.run();
                            }
                        });
                        chain.run();
                    }
                });
            }
            else
            {
                reshuffleFailedCallback.run();   
            }
        }
        
        protected boolean trySwap(int col, int row, int col2, int row2)
        {
            Cell cell = state.cell(col, row);
            if (cell.isLocked())
                return false;
            
            Item item = cell.item;
            if (item == null || cantSwap(item))
                return false;

            Cell cell2 = state.cell(col2, row2);
            if (cell2.isLocked())
                return false;
            
            Item item2 = cell2.item;
            if (item2 == null || cantSwap(item))
                return false;
            
            cell.item = item2;
            cell2.item = item;
            boolean canSwap = trySwap2(col, row, col2, row2);
            cell.item = item;
            cell2.item = item2;
            return canSwap;
        }
        
        protected static boolean cantSwap(Item item)
        {
            return item instanceof Animal || item instanceof Fire || item instanceof Laser || item instanceof Knight;
        }
        
        protected boolean trySwap2(int col, int row, int col2, int row2)
        {
            boolean hor = row == row2;
            if (hor)
            {
                if (findCombo(col - 2, row, 1, 0, 6)
                 || findCombo(col, row - 2, 0, 1, 5)
                 || findCombo(col2, row - 2, 0, 1, 5))
                    return true;
            }
            else
            {
                if (findCombo(col, row - 2, 0, 1, 6)
                 || findCombo(col - 2, row, 1, 0, 5)
                 || findCombo(col - 2, row2, 1, 0, 5))
                    return true;
            }
            return false;
        }
        
        protected boolean findCombo(int col, int row, int dcol, int drow, int length)
        {
            int c = col, r = row;
            int n = 0;
            Integer comboColor = null;
            for (int i = 0; i < length; i++, c += dcol, r += drow)
            {
                Integer cellColor = cellColor(c, r);
                if (cellColor == null)
                {
                    n = 0;
                    comboColor = null;
                }
                else if (comboColor == null)
                {
                    comboColor = cellColor;
                    n++;
                }
                else if (cellColor == Config.WILD_ID)
                {
                    n++;
                }
                else if (comboColor == Config.WILD_ID)
                {
                    comboColor = cellColor;
                    n++;
                }
                else if (comboColor == cellColor)
                {
                    n++;
                }
                else
                {
                    n = 0;
                    comboColor = null;
                }
                if (n == 3)
                    return true;
                
                if (!state.isValidCell(c, r))
                {
                    n = 0;
                    continue;
                }
            }
            return false;
        }

        protected Integer cellColor(int col, int row)
        {
            if (!state.isValidCell(col, row))
                return null;
            
            Cell cell = state.cell(col, row);
            if (cell.isLocked())
                return null;
            
            Item item = cell.item;
            if (item == null)
                return null;
            
            if (GetSwapMatches.isColorDot(item))
                return item.getColor();
            
            return null;
        }
    }