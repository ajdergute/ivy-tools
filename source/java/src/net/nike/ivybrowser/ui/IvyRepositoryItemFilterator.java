package net.nike.ivybrowser.ui;

import ca.odell.glazedlists.TextFilterator;

import net.nike.ivybrowser.domain.IvyRepositoryItem;

import java.util.List;


/** Created by IntelliJ IDEA. User: douglasbullard Date: Jan 25, 2008 Time: 10:36:57 PM To change this template use File | Settings | File Templates. */
@SuppressWarnings({ "unchecked" })
public class IvyRepositoryItemFilterator implements TextFilterator
{
    public void getFilterStrings(List   list,
                                 Object o)
    {
        IvyRepositoryItem item = (IvyRepositoryItem) o;
        list.add(item.getOrg());
        list.add(item.getModule());
        list.add(item.getRev());
    }
}
