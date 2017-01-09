package com.enno.dotz.server.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Collection;

import com.enno.dotz.shared.BinSearchDictionary;

public class Dictionary extends BinSearchDictionary
{
    public Dictionary(Collection<String> list)
    {
        super(list);
    }


    public void save(String fileName) throws IOException
    {
        save(new FileOutputStream(fileName));
    }
    
    public void save(OutputStream out) throws IOException
    {
        save(new OutputStreamWriter(out));
    }
    
    public void save(Writer out) throws IOException
    {
        out = IOUtil.buffer(out);
        
        for (int i = 0; i < m_size; i++)
        {
            out.write(m_words[i]);
            out.write('\n');
        }
        
        IOUtil.close(out);
    }
    
    public static BinSearchDictionary load(InputStream in) throws IOException
    {        
        return load(new InputStreamReader(in));
    }
    
    public static BinSearchDictionary load(Reader in) throws IOException
    {
        StringWriter buf = new StringWriter();
        IOUtil.copy(in, buf);
        return new BinSearchDictionary(buf.toString());
    }
    
    public static BinSearchDictionary load(String fileName) throws FileNotFoundException, IOException
    {
        return load(new FileInputStream(fileName));
    }
    
    public static BinSearchDictionary load(URL url) throws FileNotFoundException, IOException
    {
        return load(url.openStream());
    }
}
