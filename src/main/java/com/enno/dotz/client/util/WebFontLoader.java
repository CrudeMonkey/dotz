package com.enno.dotz.client.util;

//package com.emitrom.pilot.font.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.enno.dotz.client.util.WebFontLoader.WebFonts.WebFontsJSO;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * WebFontLoader provides a wrapper around the 
 * <a href="http://developers.google.com/webfonts/docs/webfont_loader">WebFont Loader</a> 
 * API developed by Google and TypeKit.
 * <p>
 * Make sure you use it in the onModuleLoad method of the EntryPoint of your GWT application, or it may not load the fonts.
 * We've also seen it failing to load when you have certain style rules in your CSS files,
 * see <a href="https://github.com/typekit/webfontloader/issues/70">WebFont loader issue list.</a>
 * <p>
 * 
 * Typical usage:
 * <pre>
 * public void onModuleLoad()
 * {
 *      WebFonts webFonts = new WebFonts();
 *      webFonts.addGoogleFamily("Open Sans", "Josefin Slab", "Arvo");
 *      webFonts.addTypeKitId("mykitid1", "mykitid2");
 *      webFonts.addAscenderKey("myAscenderKey");
 *      webFonts.addAscenderFamily("AscenderSans:bold,bolditalic,italic,regular");
 *      
 *      WebFontLoader.loadFonts(true, webFonts, new Runnable() {
 *          public void run() {
 *              init(); // start the rest of your app
 *          }
 *      });
 * }
 * </pre>
 * You first need to create a {@link WebFonts} object and specify
 * which web fonts you want to load from which vendor. 
 * <p>
 * The Runnable will be called when it's done loading the fonts, regardless of whether it actually succeeded or not.
 * If you need finer control on whether it (partially) succeeded or completely failed, or if you need to know 
 * which fonts didn't load, implement the {@link WebFontHandler} and call 
 * {@link WebFontLoader#loadFonts(boolean, WebFonts, WebFontHandler)}.
 * <p>
 * If you specify a <code>debug</code> value of <code>true</code> it will print debug messages for each of the callbacks
 * to the Developer Console (e.g. in FireFox or Chrome.)
 * <p>
 * It should invoke the following callbacks:
 * <ul>
 * <li>loading - indicates that the Google WebFont Loader script started
 * <li>fontLoading - when it starts to load a font
 * <li>fontActive - when it succeeded in loading a font
 * <li>fontInactive - when it failed to load the font
 * <li>active - when it's done loading all the fonts and at least one font loaded successfully
 * <li>inactive - when it's done loading and none of the fonts loaded successfully
 * </ul>
 * 
 * When a font is loaded successfully (<code>fontActive</code>), WebFontLoader adds it to a map,
 * which you can query with {@link WebFonts#getActiveFonts()}. 
 * <br>Similarly, inactive fonts can be queried via {@link WebFonts#getInactiveFonts()}.
 * <p>
 * Note that the script times out after 5 seconds, so for every font that didn't load
 * yet, it will then call <code>fontInactive</code>.
 * <p>
 * As always, when using web fonts, make sure you provide "backup fonts" in your font definitions, 
 * just in case a font didn't load, e.g.
 * <pre>
 * Text text = new Text(5, 5, "Some text");
 * text.setFontFamily("Fancy Web Font, serif");
 * </pre>
 * 
 * <b>Note that WebFontLoader can't be used more than once in an application!</b>
 * 
 * @see WebFonts
 * @see WebFontHandler
 * @see <a href="http://developers.google.com/webfonts/docs/webfont_loader">Google's WebFont Loader API</a>
 * @see <a href="http://developers.google.com/webfonts/docs/getting_started#Syntax">Syntax for Google font families</a>
 */
public class WebFontLoader
{
    /**
     * WebFontHandler is used by {@link WebFontLoader}.
     * It contains the callback methods provided by 
     * <a href="http://developers.google.com/webfonts/docs/webfont_loader">Google's WebFont Loader API</a>.
     * See {@link WebFontLoader} for details.
     * 
     * @see WebFontLoader
     * @see <a href="http://developers.google.com/webfonts/docs/webfont_loader">Google's WebFont Loader API</a>
     */
    public static interface WebFontHandler
    {
        void loading();

        void fontLoading(String fontFamily, String fontDescription);

        void fontActive(String fontFamily, String fontDescription);

        void fontInactive(String fontFamily, String fontDescription);

        void active();

        void inactive();
    }

    /**
     * WebFonts provides an easy interface for specifying the web fonts
     * that the {@link WebFontLoader} should load.
     * <p>
     * It also collects lists of which fonts were successfully loaded 
     * (and which failed to load) during the loading process.
     * See {@link #getActiveFonts()} and {@link #getInactiveFonts()}
     * 
     * @see WebFontLoader
     * @see WebFontHandler
     * @see <a href="http://developers.google.com/webfonts/docs/webfont_loader">Google's WebFont Loader API</a>
     */
    public static class WebFonts
    {
        private WebFontsJSO               m_jso           = WebFontsJSO.make();
        private Map<String, List<String>> m_activeFonts   = new HashMap<String, List<String>>();
        private Map<String, List<String>> m_inactiveFonts = new HashMap<String, List<String>>();

        /**
         * @param family
         * 
         * @see <a href="http://developers.google.com/webfonts/docs/getting_started#Syntax">Syntax for Google font families</a>
         */
        public void addGoogleFamily(String... family)
        {
            for (String fam : family)
                m_jso.addNestedArray("google", "families", fam);
        }

        public void addTypeKitId(String... ids)
        {
            for (String id : ids)
                m_jso.addNestedArray("typekit", "id", id);
        }

        public void setAscenderKey(String key)
        {
            m_jso.setNested("ascender", "key", key);
        }

        public void addAscenderFamily(String... family)
        {
            for (String fam : family)
                m_jso.addNestedArray("ascender", "families", fam);
        }

        public void setMonotypeProjectId(String id)
        {
            m_jso.setNested("monotype", "projectId", id);
        }

        public void setFontDeckId(String id)
        {
            m_jso.setNested("fontdeck", "id", id);
        }

        public void addCustomFamily(String... family)
        {
            for (String fam : family)
                m_jso.addNestedArray("custom", "families", fam);
        }

        public void addCustomURL(String... url)
        {
            for (String u : url)
                m_jso.addNestedArray("custom", "urls", u);
        }

        public static class WebFontsJSO extends JavaScriptObject
        {
            protected WebFontsJSO()
            {
            }

            static final native WebFontsJSO make()
            /*-{
                return {};
            }-*/;

            private final native void setNested(String key1, String key2, String val)
            /*-{
                var that = this;
                if (!(key1 in that)) {
                    that[key1] = {};
                }
                var hash = that[key1];
                hash[key2] = val;
            }-*/;

            private final native void addNestedArray(String key1, String key2, String val)
            /*-{
                var that = this;
                if (!(key1 in that)) {
                    that[key1] = {};
                }
                var hash = that[key1];
                if (!(key2 in hash)) {
                    hash[key2] = [];
                }
                hash[key2].push(val);
            }-*/;
        }

        public void addActiveFont(String fontFamily, String fontDescription)
        {
            List<String> descriptions = m_activeFonts.get(fontFamily);
            if (descriptions == null)
            {
                descriptions = new ArrayList<String>();
                m_activeFonts.put(fontFamily, descriptions);
            }
            descriptions.add(fontDescription);
        }
        
        public void addInactiveFont(String fontFamily, String fontDescription)
        {
            List<String> descriptions = m_inactiveFonts.get(fontFamily);
            if (descriptions == null)
            {
                descriptions = new ArrayList<String>();
                m_inactiveFonts.put(fontFamily, descriptions);
            }
            descriptions.add(fontDescription);
        }

        /**
         * Returns a map with successfully loaded fonts.
         * The map key is the fontFamily, the corresponding value is a
         * list with fontDescriptions as returned by the <code>activeFont</code>
         * callback.
         * 
         * @return Map&lt;String, List&lt;String&gt;&gt;</code>
         */
        public Map<String, List<String>> getActiveFonts()
        {
            return m_activeFonts;
        }
        
        /**
         * Returns a map with fonts that failed to load.
         * The map key is the fontFamily, the corresponding value is a
         * list with fontDescriptions as returned by the <code>inactiveFont</code>
         * callback.
         * 
         * @return Map&lt;String, List&lt;String&gt;&gt;</code>
         */
        public Map<String, List<String>> getInactiveFonts()
        {
            return m_inactiveFonts;
        }
    }

    private static WebFontHandler s_handler;
    private static WebFonts       s_fonts;
    private static boolean        s_debug;

    /**
     * Convenience method that calls {@link WebFontLoader#loadFonts(boolean, WebFonts, WebFontHandler)}
     * and invokes the <code>whenDone</code> callback when it's done, either via the 
     * <code>active</code> callback or the <code>inactive</code> callback.
     * 
     * @param debug Whether to print debug messages to the Console
     * @param fonts WebFonts defines which fonts to load
     * @param whenDone Callback invoked when it's done loading, whether it succeeded or not.
     */
    public static void loadFonts(boolean debug, WebFonts fonts, final Runnable whenDone)
    {
        loadFonts(debug, fonts, whenDone, whenDone);
    }
     
    /**
     * Convenience method that calls {@link WebFontLoader#loadFonts(boolean, WebFonts, WebFontHandler)}
     * and invokes the <code>onSuccess</code> callback when it's done loading and at least one font was loaded successfully
     * (see <code>active</code> callback), or the <code>onFailure</code> callback when it's done loading and all fonts failed to load
     * (see <code>inactive</code> callback.)
     * 
     * @param debug Whether to print debug messages to the Console
     * @param fonts WebFonts defines which fonts to load
     * @param onSuccess Callback invoked when it's done loading and at least one font was loaded successfully.
     * @param onFailure Callback invoked when it's done loading and none of the fonts were loaded successfully.
     */
    public static void loadFonts(boolean debug, WebFonts fonts, final Runnable onSuccess, final Runnable onFailure)
    {
        WebFontHandler handler = new WebFontHandler() 
        {
            public void active()
            {
                onSuccess.run();
            }

            public void inactive()
            {
                onFailure.run();
            }

            public void loading() {}
            public void fontLoading(String fontFamily, String fontDescription) {}
            public void fontActive(String fontFamily, String fontDescription) {}
            public void fontInactive(String fontFamily, String fontDescription) {}
        };

        loadFonts(debug, fonts, handler);
    }

    /**
     * Attempts to load the web fonts defined in the WebFonts object.
     * It invokes the callbacks of the WebFontHandler whenever the underlying 
     * <a href="http://developers.google.com/webfonts/docs/webfont_loader">WebFont Loader script</a>
     * fires them.
     * 
     * @param debug Whether to print debug messages to the Console
     * @param fonts WebFonts defines which fonts to load
     * @param handler WebFontHandler that provides detailed callbacks as defined by the 
     *      <a href="http://developers.google.com/webfonts/docs/webfont_loader">Google's WebFont Loader API</a>
     */
    public static void loadFonts(boolean debug, WebFonts fonts, WebFontHandler handler)
    {
        s_debug = debug;
        s_fonts = fonts;
        s_handler = handler;

        loadFontsNative(s_fonts.m_jso);
    }

    private static void loading()
    {
        if (s_handler != null)
            s_handler.loading();
        
        if (s_debug)
            Console.log("loading");
    }

    private static void fontLoading(final String fontFamily, final String fontDescription)
    {
        if (s_handler != null)
            s_handler.fontLoading(fontFamily, fontDescription);
        
        if (s_debug)
            Console.log("fontLoading " + fontFamily + " " + fontDescription);
    }

    private static void fontActive(String fontFamily, String fontDescription)
    {
        if (s_handler != null)
            s_handler.fontActive(fontFamily, fontDescription);

        if (s_debug)
            Console.log("fontActive " + fontFamily + " " + fontDescription);

        s_fonts.addActiveFont(fontFamily, fontDescription);
    }

    private static void fontInactive(String fontFamily, String fontDescription)
    {
        if (s_handler != null)
            s_handler.fontInactive(fontFamily, fontDescription);
        
        if (s_debug)
            Console.log("fontInactive " + fontFamily + " " + fontDescription);
        
        s_fonts.addInactiveFont(fontFamily, fontDescription);
    }

    private static void active()
    {
        if (s_handler != null)
            s_handler.active();
        
        if (s_debug)
            Console.log("active");
    }

    private static void inactive()
    {
        if (s_handler != null)
            s_handler.inactive();
        
        if (s_debug)
            Console.log("inactive");
    }

    private static final native void loadFontsNative(WebFontsJSO webFonts)
    /*-{
        webFonts['loading'] = function() {
            $entry(@com.enno.dotz.client.util.WebFontLoader::loading())();
        };
        
        webFonts['active'] = function() {
            $entry(@com.enno.dotz.client.util.WebFontLoader::active())();
        };
        
        webFonts['inactive'] = function() {
            $entry(@com.enno.dotz.client.util.WebFontLoader::inactive())();
        };
        
        webFonts['fontloading'] = function(fontFamily, fontDescription) {
            $entry(@com.enno.dotz.client.util.WebFontLoader::fontLoading(Ljava/lang/String;Ljava/lang/String;))(fontFamily, fontDescription);
        };
    
        webFonts['fontactive'] = function(fontFamily, fontDescription) {
            $entry(@com.enno.dotz.client.util.WebFontLoader::fontActive(Ljava/lang/String;Ljava/lang/String;))(fontFamily, fontDescription);
        };
    
        webFonts['fontinactive'] = function(fontFamily, fontDescription) {
            $entry(@com.enno.dotz.client.util.WebFontLoader::fontInactive(Ljava/lang/String;Ljava/lang/String;))(fontFamily, fontDescription);
        };
    
        $wnd.WebFontConfig = webFonts;
//        alert(JSON.stringify(webFonts));
        
        var document = $doc;
        var wf = document.createElement('script');
        wf.src = ('https:' == document.location.protocol ? 'https' : 'http') +
            '://ajax.googleapis.com/ajax/libs/webfont/1/webfont.js';
        wf.type = 'text/javascript';
        wf.async = 'true';
        var s = document.getElementsByTagName('script')[0];
        s.parentNode.insertBefore(wf, s);
    }-*/;
}