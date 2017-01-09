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
    }

    // ClickConnectMode
    public UserAction(CellList cells)
    {
        this.cells = cells;
        this.color = cells.getColor();
    }
}
