package com.enno.dotz.server.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.log4j.Logger;

public class IOUtil
{
    private static Logger s_log = Logger.getLogger(IOUtil.class);
    
    public static final int BUFSIZE = 8192;
    
    public static void copy(Reader in, Writer out) throws IOException
    {
        copy(in, out, true);
    }
    
    public static void copy(Reader in, Writer out, boolean closeStreams) throws IOException
    {
        try
        {
            in = buffer(in);
            out = buffer(out);
            
            char[] buf = new char[BUFSIZE];
            int n;
            while ((n = in.read(buf)) > 0)
            {
                out.write(buf, 0, n);
            }
        }
        catch (Exception e)
        {
            s_log.error("can't copy Reader to Writer", e);
            throw e;
        }
        finally
        {
            if (closeStreams)
            {
                close(in);
                close(out);
            }
        }
    }
    
    public static Reader buffer(Reader in)
    {
        if (in instanceof BufferedReader || in instanceof StringReader)
            return in;
        else
            return new BufferedReader(in);
    }
    
    public static Writer buffer(Writer out)
    {
        if (out instanceof BufferedWriter || out instanceof StringWriter)
            return out;
        else
            return new BufferedWriter(out);
    }
    
    public static InputStream buffer(InputStream in)
    {
        if (in instanceof BufferedInputStream)
            return in;
        else
            return new BufferedInputStream(in);
    }
    
    public static OutputStream buffer(OutputStream out)
    {
        if (out instanceof BufferedOutputStream)
            return out;
        else
            return new BufferedOutputStream(out);
    }
    
    public static void close(Reader r)
    {
        try
        {
            r.close();
        }
        catch (Exception e)
        {
            s_log.warn("can't close Reader", e);
        }
    }
    
    public static void close(Writer r)
    {
        try
        {
            r.close();
        }
        catch (Exception e)
        {
            s_log.warn("can't close Writer", e);
        }
    }
}
