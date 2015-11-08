
package com.enno.dotz.client.ui;

import com.smartgwt.client.types.AnimationAcceleration;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.DrawEvent;
import com.smartgwt.client.widgets.events.DrawHandler;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeNode;
import com.smartgwt.client.widgets.tree.events.FolderClickEvent;
import com.smartgwt.client.widgets.tree.events.FolderClickHandler;

public class MXTreeGrid extends TreeGrid
{
    private boolean m_open_root = true;

    private boolean m_open_tree = false;

    private boolean m_on_folder = true;
    
    private boolean m_expandOnClick = true;

    public boolean isExpandOnClick()
    {
        return m_expandOnClick;
    }

    public void setExpandOnClick(boolean expandOnClick)
    {
        m_expandOnClick = expandOnClick;
    }

    public MXTreeGrid()
    {
        super();
        
        setWidth100();

        setHeight100();

        setAnimateTime(200);
        
        setCanAutoFitFields(false);

        setAnimateAcceleration(AnimationAcceleration.SMOOTH_START_END);

//        setBaseStyle("MXTree");

        addDrawHandler(new DrawHandler()
        {
            @Override
            public void onDraw(DrawEvent event)
            {
                if (m_open_tree)
                {
                    Tree tree = getData();

                    if (null != tree)
                    {
                        tree.openAll();
                    }
                }
                else if (m_open_root)
                {
                    Tree tree = getData();

                    if (null != tree)
                    {
                        TreeNode root = tree.getRoot();

                        if (null != root)
                        {
                            TreeNode[] folders = tree.getFolders(root);

                            if (null != folders)
                            {
                                tree.openFolders(folders);
                            }
                        }
                    }
                }
            }
        });
        addFolderClickHandler(new FolderClickHandler()
        {
            @Override
            public void onFolderClick(FolderClickEvent event)
            {
                if (m_on_folder && m_expandOnClick)
                {
                    openCloseNode(getData(), event.getFolder());
                }
            }
        });
        setShowHeader(false);
        
        setShowAllRecords(true);
        
        setSelectionType(SelectionStyle.SINGLE);
    }

    public void setOnFolderOpenClose(boolean on_folder)
    {
        m_on_folder = on_folder;
    }

    public TreeNode getFirstNode()
    {
        Tree tree = getData();

        if (null != tree)
        {
            TreeNode root = tree.getRoot();

            if (null != root)
            {
                TreeNode[] children = tree.getChildren(root);

                if ((null != children) && (children.length > 0))
                {
                    return children[0];
                }
            }
        }
        return null;
    }

    public TreeNode getFirstChild(TreeNode node)
    {
        Tree tree = getData();

        if (null != tree)
        {
            if (null != node)
            {
                TreeNode[] children = tree.getChildren(node);

                if ((null != children) && (children.length > 0))
                {
                    return children[0];
                }
            }
        }
        return null;
    }

    public void doSelectNode(TreeNode node)
    {
    	selectRecord(node);
    }

    public TreeNode getFirstLeafDepthFirst()
    {
        Tree tree = getData();

        if (null != tree)
        {
            return getFirstLeafDepthFirst(tree, tree.getRoot());
        }
        return null;
    }

    public void openTreeParents(TreeNode node)
    {
        if (null != node)
        {
            Tree tree = getData();

            if (null != tree)
            {
                if (tree.isFolder(node))
                {
                    tree.openFolder(node);
                }
                while (null != (node = tree.getParent(node)))
                {
                    tree.openFolder(node);
                }
            }
        }
    }

    private final TreeNode getFirstLeafDepthFirst(Tree tree, TreeNode root)
    {
        if (null != root)
        {
            TreeNode[] children = tree.getChildren(root);

            if ((null != children) && (children.length > 0))
            {
                for (int i = 0; i < children.length; i++)
                {
                    TreeNode node = children[i];

                    if (tree.isFolder(node))
                    {
                        TreeNode found = getFirstLeafDepthFirst(tree, node);

                        if (null != found)
                        {
                            return found;
                        }
                    }
                    if (tree.isLeaf(node))
                    {
                        return node;
                    }
                }
            }
        }
        return null;
    }

    public TreeNode findNode(MXTreeGridFindFunction finder)
    {
        Tree tree = getData();

        if ((null != tree) && (null != finder))
        {
            return findNodeDepthFirst(tree, tree.getRoot(), finder);
        }
        return null;
    }

    private final TreeNode findNodeDepthFirst(Tree tree, TreeNode root, MXTreeGridFindFunction finder)
    {
        TreeNode found = null;

        if (null != root)
        {
            TreeNode[] children = tree.getChildren(root);

            if ((null != children) && (children.length > 0))
            {
                for (int i = 0; ((i < children.length) && (null == found)); i++)
                {
                    TreeNode node = children[i];

                    if (finder.matches(node))
                    {
                        return node;
                    }
                    if (tree.isFolder(node))
                    {
                        found = findNodeDepthFirst(tree, node, finder);
                    }
                }
            }
        }
        return found;
    }

    public MXTreeGrid setOpenRoot(boolean open_root)
    {
        m_open_root = open_root;

        return this;
    }

    public MXTreeGrid setOpenTree(boolean open_tree)
    {
        m_open_tree = open_tree;

        return this;
    }

    public void openCloseNode(Tree tree, TreeNode node)
    {
        if (tree.isOpen(node))
        {
            tree.closeFolder(node);
        }
        else
        {
            tree.openFolder(node);
        }
    }

    public void openAll()
    {
        Tree tree = getData();

        if (null != tree)
        {
            tree.openAll();
        }
    }

    public void closeAll()
    {
        Tree tree = getData();

        if (null != tree)
        {
            tree.closeAll();
        }
    }

    public boolean isLeaf(TreeNode node)
    {
        Tree tree = getData();

        if (null != tree)
        {
            return tree.isLeaf(node);
        }
        return false;
    }

    public boolean isFolder(TreeNode node)
    {
        Tree tree = getData();

        if (null != tree)
        {
            return tree.isFolder(node);
        }
        return false;
    }
    
    public MXImageButton getExpandControl()
    {
        MXImageButton open = new MXImageButton("Expand Tree", "opentree.png", 16, 16);

        open.setShowFocused(false);

        open.setShowRollOver(false);

        open.setShowDown(false);

        open.addClickHandler(new ClickHandler()
        {
            public void onClick(ClickEvent event)
            {
                openAll();
            }
        });
        
        return open;
    }
    
    public MXImageButton getCollapseControl()
    {
        MXImageButton fold = new MXImageButton("Collapse Tree", "foldtree.png", 16, 16);

        fold.setShowFocused(false);

        fold.setShowRollOver(false);

        fold.setShowDown(false);

        fold.addClickHandler(new ClickHandler()
        {
            public void onClick(ClickEvent event)
            {
                closeAll();
            }
        });
        
        return fold;
    }
}
