
package com.enno.dotz.client;

import com.enno.dotz.client.util.Font;
import com.enno.dotz.client.util.WebFontLoader;
import com.enno.dotz.client.util.WebFontLoader.WebFonts;
import com.google.gwt.core.client.EntryPoint;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Dotz implements EntryPoint
{
    /**
     * This is the entry point method.
     */
    public void onModuleLoad()
    {
        WebFonts webFonts = new WebFonts();
        for (String font : Font.GOOGLE_FONTS)
            webFonts.addGoogleFamily(font);

//             webFonts.addTypeKitId("mykitid1", "mykitid2");
//              webFonts.setAscenderKey("myAscenderKey");
//             webFonts.addAscenderFamily("AscenderSans:bold,bolditalic,italic,regular");
              
              WebFontLoader.loadFonts(true, webFonts, new Runnable() {
                  public void run() {
                      //init(); // start the rest of your app
                  }
              });
        
        MainPanel mainPanel = new MainPanel();
        mainPanel.draw();
        //RootPanel.get().add(mainPanel); <-- this does not work!
        mainPanel.init();
    }
}
