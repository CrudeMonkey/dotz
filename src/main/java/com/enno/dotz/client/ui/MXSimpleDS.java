
package com.enno.dotz.client.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ait.tooling.nativetools.client.NArray;
import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceBooleanField;
import com.smartgwt.client.data.fields.DataSourceDateField;
import com.smartgwt.client.data.fields.DataSourceFloatField;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;

public class MXSimpleDS extends DataSource
{
    private static int s_id = 0;
    
    public MXSimpleDS(NArray data, MXListGridField... fields)
    {
        this(data, new ArrayList<MXListGridField>(Arrays.asList(fields)).toArray(new ListGridField[] {}));
    }

    public MXSimpleDS(NArray data, ListGridField... fields)
    {
        setID("MXSimpleDS_" + s_id++);

        ArrayList<DataSourceField> dsfields = new ArrayList<DataSourceField>();

        for (int i = 0; i < fields.length; i++)
        {
            ListGridField field = fields[i];

            DataSourceField f = createDataSourceField(field);
            
            if (f != null)
            {
                dsfields.add(f);
            }
            else if (ListGridFieldType.ICON != field.getType())
            {
                GWT.log("TYPE MISSING FOR=" + field.getName());
            }
        }
        if (dsfields.size() > 0)
        {
            dsfields.get(0).setPrimaryKey(true);
        }
        setFields(dsfields.toArray(new DataSourceField[0]));

        ListGridRecord[] records = new ListGridRecord[data.size()];

        for (int i = 0; i < records.length; i++)
        {
            records[i] = new ListGridRecord(data.getAsJSO(i));

        }
        setCacheData(records);

        setClientOnly(true);
    }
    
    protected DataSourceField createDataSourceField(ListGridField field)
    {
        ListGridFieldType type = field.getType();

        if (ListGridFieldType.INTEGER == type)
        {
            return new DataSourceIntegerField(field.getName(), field.getTitle());
        }
        else if (ListGridFieldType.TEXT == type)
        {
            DataSourceTextField f = null;
                    
            if (field.getAttribute("length") != null)
            {
                f = new DataSourceTextField(field.getName(), field.getTitle(), Integer.parseInt(field.getAttribute("length")));
            }
            else
            {
                f = new DataSourceTextField(field.getName(), field.getTitle());
            }
            f.setEscapeHTML(field.getEscapeHTML());

            return f;
        }
        else if (ListGridFieldType.BOOLEAN == type)
        {
            return new DataSourceBooleanField(field.getName(), field.getTitle());
        }
        else if (ListGridFieldType.DATE == type)
        {
            return new DataSourceDateField(field.getName(), field.getTitle());
        }
        else if (ListGridFieldType.FLOAT == type)
        {
            return new DataSourceFloatField(field.getName(), field.getTitle());
        }
        else
        {
            return null;
        }
    }

    public MXSimpleDS(NArray data)
    {
        this(data, autocreatefields(data));
    }

    private static MXListGridField[] autocreatefields(NArray data)
    {
        if (null != data)
        {
            if (data.size() > 0)
            {
                List<String> keys = data.getAsObject(0).keys();

                if ((null != keys) && (keys.size() > 0))
                {
                    int leng = keys.size();

                    MXListGridField[] fields = new MXListGridField[leng];

                    for (int i = 0; i < leng; i++)
                    {
                        String name = keys.get(i);

                        MXListGridField field = new MXListGridField(name, name);

                        field.setType(ListGridFieldType.TEXT);

                        fields[i] = field;
                    }
                    return fields;
                }
            }
        }
        return new MXListGridField[0];
    }
}