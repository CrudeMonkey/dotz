
package com.enno.dotz.client.ui;

import com.ait.tooling.nativetools.client.NArray;
import com.ait.tooling.nativetools.client.NObject;
import com.smartgwt.client.types.TreeModelType;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeNode;

public class MXTree extends Tree
{
    public MXTree()
    {
        super();
    }

    public MXTree(String name, String id, String parent)
    {
        super();

        setShowRoot(false);

        setNameProperty(name);

        setIdField(id);

        setParentIdField(parent);

        setModelType(TreeModelType.PARENT);
    }

    public MXTree(String name, String id, String parent, NArray array)
    {
        this(name, id, parent);
        
        if (null != array)
        {
            int leng = array.size();

            TreeNode[] data = new TreeNode[leng];

            for (int i = 0; i < leng; i++)
            {
                NObject object = array.getAsObject(i);

                if (null != object)
                {
                    data[i] = new TreeNode(object.getJSO());
                }
            }
            setData(data);
        }
    }
}
