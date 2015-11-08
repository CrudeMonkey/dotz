package com.enno.dotz.client.ui;

import com.ait.tooling.nativetools.client.NArray;
import com.ait.tooling.nativetools.client.NArrayJSO;
import com.ait.tooling.nativetools.client.NObject;
import com.ait.tooling.nativetools.client.NObjectJSO;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.widgets.grid.ListGridRecord;

public class MXRecordList
{
    public static final Record[] toRecordArray(NArray array)
    {
        return toRecordArray(array, false);
    }

    // Generates a record number for each record based on the Start Count
    public static final Record[] toRecordArray(NArray array, int startCount)
    {
    	if (null == array)
        {
            return new Record[0];
        }
        NArrayJSO ajso = array.getJSO();

        int leng = ajso.size();

        Record[] records = new Record[leng];

        for (int i = 0; i < leng; i++)
        {
        	records[i] = new Record(ajso.getAsJSO(i));
            records[i].setAttribute("recordCount", startCount + i);
            
        }
        return records;
    }
    
//    public static final Record[] toRecordArray(NArray array, MXGridObjectFindFunction find)
//    {
//        return toRecordArray(array, false, find);
//    }
//
//    public static final Record[] toRecordArray(final NArray array, final boolean storeNObject, final MXGridObjectFindFunction find)
//    {
//        if (null == array)
//        {
//            return new Record[0];
//        }
//        int leng = array.getLength();
//
//        ArrayList<Record> records = new ArrayList<Record>();
//
//        for (int i = 0; i < leng; i++)
//        {
//            NObject object = array.getAsNObject(i);
//
//            if (find.matches(object))
//            {
//                Record record = new Record(object.getJSO());
//
//                if (storeNObject)
//                {
//                    try
//                    {
//                        record.setAttribute("originalnobject", object.deep());
//                    }
//                    catch (Exception e)
//                    {
//                        e.printStackTrace();
//                    }
//                }
//                records.add(record);
//            }
//        }
//        return records.toArray(new Record[0]);
//    }

    public static final Record[] toRecordArray(NArray array, boolean storeNObject)
    {
        if (null == array)
        {
            return new Record[0];
        }
        NArrayJSO ajso = array.getJSO();

        int leng = ajso.size();

        Record[] records = new Record[leng];

        for (int i = 0; i < leng; i++)
        {
            records[i] = new Record(ajso.getAsJSO(i));

            if (storeNObject)
            {
                try
                {
                    records[i].setAttribute("originalnobject", array.getAsObject(i).deep());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        return records;
    }

    public static final NArray toNArray(Record[] records)
    {
        NArray array = new NArray();

        if ((null == records) || (records.length < 1))
        {
            return array;
        }
        for (Record record : records)
        {
            array.push(new NObject((NObjectJSO) record.getJsObj()));
        }
        return array;
    }

    public static NObject getOriginalObject(ListGridRecord rec)
    {
        Object obj = rec.getAttributeAsObject("originalnobject");
        return obj == null ? null : ((NObject) obj);
    }

    public static void setOriginalObject(Record rec, NObject obj)
    {
        try
        {
            rec.setAttribute("originalnobject", obj.deep());
        }
        catch (Exception e)
        {
            throw new RuntimeException("can't deep-copy", e);
        }
    }
}
