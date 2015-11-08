
package com.enno.dotz.client.ui;

import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.UploadItem;

public class MXForm extends DynamicForm
{
    public static final String REQUIRED_FIELD = "<sup><img src='images/required.gif' alt='Required Field'></sup>";
    
    public MXForm()
    {
        super();

        setBrowserSpellCheck(false);
    }

    @Override
    public void setFields(FormItem... fields)
    {
        setFields(true, fields);
    }
    
    public void setFields(boolean xssclean, FormItem... fields)
    {
        boolean hasFileUpload = false;
        if ((null != fields) && (fields.length > 0))
        {
            for (int i = 0; i < fields.length; i++)
            {
                FormItem field = fields[i];
                if (field instanceof UploadItem)
                    hasFileUpload = true;

                String title = field.getTitle();

                if ((null != title) && (false == title.trim().isEmpty()))
                {
                    field.setTitle(createTitle(title, (field.getRequired() != null ? field.getRequired() : false), xssclean));
                }
                field.setWrapTitle(false);
            }
        }
        
//        if (hasFileUpload)
//        {
//            // Add hidden field with sessionId
//            HiddenItem sessionId = new HiddenItem("sessionId");
//            sessionId.setValue(ClientCache.getInstance().getSessionId());
//            int n = fields.length;
//            FormItem[] newFields = new FormItem[n + 1]; // NOTE: GWT does not support Arrays.copyOf()
//            for (int i = 0; i < n; i++)
//                newFields[i] = fields[i];
//            newFields[n] = sessionId;
//            fields = newFields;
//        }
        
        super.setFields(fields);
    }
    
    
    private String createTitle(String title, Boolean required, boolean xssclean)
    {
        if (xssclean)
        {
            if (required)
                return XSS.clean(title) + " " + REQUIRED_FIELD;
            else
                return "<b>" + XSS.clean(title) + "</b>";
        }
        else
        {
            if (required)
                return title + " " + REQUIRED_FIELD;
            else
                return "<b>" + title + "</b>";
        }
    }

    @Override
    public void setValue(String fieldName, String value)
    {
        super.setValue(fieldName, XSS.clean(value));
    }
    
    public void setSafeValue(String fieldName, String value)
    {
        super.setValue(fieldName, value);
    }
    
    public static String createTitle(String title, boolean required)
    {
       if (required)
           return XSS.clean(title) + " " + REQUIRED_FIELD;
       else
           return "<b>" + XSS.clean(title) + "</b>";
    }
    
    public static void updateTitle(FormItem field, String title)
    {
        field.setTitle(createTitle(title, field.getRequired()));
    }
}
