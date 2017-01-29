package com.enno.dotz.client;

public class UserAction
{
    CellList cells;
    boolean is_square;
    boolean isKnightMode;
    boolean isWordMode;
    boolean isEggMode;
    String word;
    Integer wordPoints;
    Integer color;
    public int direction = Direction.NONE;
    
    public boolean dousedFire = false;
    public GetSwapMatches swapMatches;
    
    public UserAction()
    {
        cells = new CellList();
    }
    
    public UserAction(CellList cells, boolean is_square, boolean isKnightMode, boolean isWordMode, boolean isEggMode, String word, Integer wordPoints, Integer color)
    {
        this.cells = cells;
        this.is_square = is_square;
        this.isKnightMode = isKnightMode;
        this.isWordMode = isWordMode;
        this.isEggMode = isEggMode;
        this.word = word;
        this.wordPoints = wordPoints;
        this.color = color;
        
        int n = cells.size();
        Cell p1 = cells.get(n - 2);
        Cell p2 = cells.get(n - 1);        
        direction = Direction.fromXY(p1.col, p1.row, p2.col, p2.row);
    }

    // ClickConnectMode
    public UserAction(CellList cells, boolean isEggMode)
    {
        this.cells = cells;
        this.isEggMode = isEggMode;
        if (!isEggMode)
            this.color = cells.getColor();

    }
}
