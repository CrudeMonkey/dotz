package com.enno.dotz.server.manual

class GenManual
{
    static final String NL = "\n"
    
    Map<String,Anchor> anchors = [:]
    List<Anchor> anchor_list = []
    
    class Anchor
    {
        List words
        String id
        
        Anchor(List<String> words)
        {
            this.words = words
            id = words[0].replaceAll(/\W+/, "_")
            words.each{ anchors[it] = this }
            anchor_list << this
        }
        
        String href(String html)
        {
            "<a href='#${id}'>$html</a>"
        }
        
        String toString()
        {
            "anchor <id: $id, words: $words>"
        }
    }
    
    class Section
    {
        Anchor anchor
        String id
        List<Section> sections = []
        
        void addAnchor(List words, boolean pluralize = true)
        {
            println "addAnchor $words"
            
            if (pluralize)
            {
                String word1 = words[0].toLowerCase()
                String word2
                def m = (word1 =~ /(\w+)\z/)
                //println m[0]
                String lastWord = m[0][1]
                if (pluralize)
                {
                    if (isPlural(lastWord))
                    {
                        word2 = word1.substring(0, word1.length() - lastWord.length()) + getSingular(lastWord)
                    }
                    else if (isSingular(lastWord))
                    {
                        word2 = word1.substring(0, word1.length() - lastWord.length()) + getPlural(lastWord)
                    }
                }
                if (word2)
                    words = [word1, word2]
                else
                    words = [word1]
            }
            
            Anchor a = new Anchor(words)
            anchor = a
        }
        
        void linkText()
        {
            sections*.linkText()
        }
        
        void forceLineBreak()
        {
            if (!sections)
                return
                
            def last = sections[sections.size() - 1]
            if (last instanceof LineBreak)
                return
                
            sections << new LineBreak()
        }
        
        void html()
        {
            sections*.html()
        }
    }
    
    class AnchoredSection extends Section
    {
        AnchoredSection(String text, List words)
        {
            addAnchor(words)
            sections << new LinkedText(text: text) // don't link this text!
        }
        
        void html()
        {
            buf << "<span id='${anchor.id}'>"
            sections*.html()
            buf << "</span>"
        }
    }
    
    class LineBreak extends Section
    {
        void html()
        {
            buf << "<BR><BR>\n"
        }
    }
    
    class Text extends Section
    {
        String text
        
        void html()
        {
            buf << text
        }
        
        void linkText()
        {
            text = linkText(text)
        }
    }
    
    class LinkedText extends Section
    {
        String text
        String anchor_word
        String anchor_id
        
        void html()
        {
            Anchor a
            if (anchor_word)
            {
                a = anchors[anchor_word.toLowerCase()]
                anchor_id = a?.id
            }
            
            if (anchor_id)
                buf << a.href(escapeHtml(text)) << NL
            else
                buf << escapeHtml(text) << NL
        }
        
        void linkText()
        {
            //text = linkText(text)
        }
    }
    
    class HtmlList extends Section
    {
        int level = 1
        
        void html()
        {
            buf << "<UL>\n"
            sections.each{
                if (it instanceof HtmlList)
                    it.html()
                else
                {
                    buf << '<LI>'
                    it.html()
                    buf << NL
                }
            }
            buf << "</UL>\n"
        }
    }
    
    boolean isPlural(String w)
    {
        return w =~ /s$/
    }
    
    boolean isSingular(String w)
    {
        return !(w =~ /s$/)
    }
    
    String getSingular(String w)
    {
        def m = w =~ /(\w+)s$/
        m[0][1]
    }
    
    String getPlural(String w)
    {
        w + 's'
    }

    String escapeHtml(String txt)
    {
        txt //TODO
    }
    
    class Chapter extends Section
    {
        String title
        int level
        String number // e.g. '1.1'
        
        List<Chapter> getChapters() { sections.findAll{ it instanceof Chapter } }
        
        void setChapterNumber(String n)
        {
            number = n
            int i = 1
            chapters.each{ s -> s.setChapterNumber(n + '.' + (i++)) }
        }
        
        void process(Map code)
        {
            if (code)
            {
                if (code.tags)
                {
                    addAnchor(code.tags, false)
                }
                else
                    addAnchor([title])
            }
            else
            {
                addAnchor([title])
            }
        }
        
        void html()
        {
            buf << "<H${level} id='${anchor?.id}'>$number - $title</H${level}>" << NL
            
            sections*.html()
        }
        
        void index()
        {
            buf << "<LI>" << anchor.href("$number - $title") << NL
            List ch = chapters
            if (ch)
            {
                buf << "<UL>\n"
                ch.each{ it.index() }
                buf << "</UL>\n"
            }
        }
    }

    List<Chapter> chapters = []
    Stack<Chapter> stack = []
    Section curr

    Stack<HtmlList> listStack
    
    void gen()
    {
        new File('manual.txt').eachLine{ String line ->
            if (line =~ /(?s)^\s*$/ || line.startsWith('==='))
            {
                listStack = null
                if (curr)
                    curr.forceLineBreak()
                return
            }
            
            def matchChapter = (line =~ /^(-+)\s+([^@]+)(?:@(.+))?/)
            if (matchChapter)
            {
                String title = matchChapter[0][2].trim()
                int level = matchChapter[0][1].length()    
                Map code = eval(matchChapter[0][3])
                
                Chapter ch = new Chapter(title: title, level: level)
                ch.process(code)
                if (level == 1)
                {
                    chapters << ch
                    stack = [ch] as Stack<Chapter>
                }
                else
                {
                    while (stack.peek().level >= level)
                        stack.pop()
                    
                    stack.peek().sections << ch
                    stack.push(ch)
                }
                curr = ch
                listStack = null
                return
            }
            
            def matchList = (line =~ /^(\*+)\s+(.+)/)
            if (matchList)
            {
                int level = matchList[0][1].length()
                String text = matchList[0][2]
                
                
                if (listStack)
                {
                    while (listStack.peek().level > level)
                        listStack.pop()
                    
                    HtmlList top = listStack.peek()
                    if (top.level == level)
                    {
                        addText(top, text)
                    }
                    else
                    {
                        HtmlList list = new HtmlList(level: level)
                        addText(list, text)
                        top.sections << list
                        listStack.push(list)
                    }
                }
                else
                {
                    HtmlList list = new HtmlList(level: level)
                    addText(list, text)
                    listStack = [list] as Stack
                    curr.sections << list // top-level list
                }
                
                return
            }
            else
            {
                if (curr)
                    addText(curr, line)
            }
        }
        
        prep()
        dump()
        
        String h = html()
        println h
        
        File file = new File("manual.html")
        file.delete()
        file << h
    }
    
    protected addText(Section s, String text)
    {
//        println "addText $text"
        
        String t = BEGIN_TXT + text.replaceAll(/(\[\[.+?\]\]|\[.+?\])/) { match, expr -> END_TXT + expr + BEGIN_TXT} + END_TXT
//        /println "addText t=$t"
        
        if (s instanceof HtmlList)
        {
            Section section = new Section()
            s.sections << section
            s = section
        }
        
        (t =~/(\[\[(.+?)\]\]|\[(.+?)\]|\Q${BEGIN_TXT}\E(.*?)\Q${END_TXT}\E)/).each { match, String expr, String ans, String link, String txt ->
            if (expr.startsWith('[['))
            {
                addAnchoredSection(s, ans)
            }
            else if (expr.startsWith('['))
            {
                linkText(s, link)
            }
            else if (txt.length() > 0)
                s.sections << new Text(text: txt)
        }
    }
    
    // [[word:tag1,...]]
    void addAnchoredSection(Section section, String str)
    {
        int i = str.indexOf(':')
        String text = str
        List words = [str]
        if (i != -1)
        {
            text = str.substring(0, i)
            words = str.substring(i + 1).split(",")
        }
        section.sections << new AnchoredSection(text, words)
    }
    
    void linkText(Section section, String str)
    {
        int i = str.lastIndexOf(':')
        String anchor_word = null
        if (i != -1)
        {
            anchor_word = str.substring(i + 1)
            str = str.substring(0, i)
        }
        section.sections << new LinkedText(text: str, anchor_word: anchor_word)
    }
    
    
    protected def eval(String code)
    {
        code == null ? null : Eval.me(code)
    }
    
    String link_regex
    
    String BEGIN_TXT = "##"
    String END_TXT = "&&"
    
    String linkText(String text)
    {
        text.replaceAll(link_regex) { match, word -> anchors[word.toLowerCase()].href(word) }
    }
    
    void prep()
    {
        int i = 1
        
        List anchor_words = []
        anchor_list.each{ 
            Anchor a -> anchor_words.addAll(a.words) 
        }
        anchor_words.sort{ a, b -> b <=> a }  // reverse order
        link_regex = "(?i)(\\b" + anchor_words.join("\\b|\\b") + "\\b)"
        
        chapters.each{ Chapter ch ->
            ch.setChapterNumber(i as String)
            i++
            
            ch.linkText()
        }
    }
    
    void dump()
    {
        def dumpChapter
        dumpChapter = { Chapter ch ->
            println "${ch.number} ${ch.title} id=${ch.anchor?.id}"
            ch.chapters.each{ dumpChapter(it) }
        }
        chapters.each{ dumpChapter(it) }
    }
    
    StringBuilder buf = new StringBuilder()
    
    void addIndex()
    {
        buf << "<H1>Index</H1>"
        buf << "<UL>\n"
        chapters.each{
            it.index() 
        }
        buf << "</UL>\n"
    }
    
    String html()
    {
        buf << "<HTML>\n"
        buf << "<BODY>\n"
        
        addIndex()
        
        chapters.each{ it.html() }
        buf << "</BODY>\n"
        buf << "</HTML>\n"
        buf.toString()
    }
    
    static void main(String[] args)
    {
        GenManual g = new GenManual()
        g.gen()
    }
}
