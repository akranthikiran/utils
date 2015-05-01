package com.fw.ccg.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollectionUtil
{
	public static <T> Collection<T> copy(Collection<T> col, T... arr)
	{
		if(col == null)
		{
			col = new ArrayList<T>(arr.length);
		}

		for(T t : arr)
		{
			col.add(t);
		}

		return col;
	}

	public static <T> Set<T> toSet(Set<T> set, T... arr)
	{
		if(set == null)
		{
			set = new HashSet<T>();
		}

		return (Set<T>)copy(set, arr);
	}

	public static <T> List<T> toList(List<T> lst, T... arr)
	{
		if(lst == null)
		{
			lst = new ArrayList<T>(arr.length);
		}

		return (List<T>)copy(lst, arr);
	}
}
