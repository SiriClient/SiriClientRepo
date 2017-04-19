package com.nituv.common.util;

import java.util.ArrayList;
import java.util.List;

import com.nituv.mot.siri.client.ILbiStationArrivalsDataResponse;

public class NtvArraysUtils {

	public static ArrayList<Integer> createArrayList(Integer firstValue) 
	{
		ArrayList<Integer> list = new ArrayList<Integer>(1);
		list.add(firstValue);
		return list;
	}

	public static String toString(List<Integer> list) {
		StringBuffer buf = new StringBuffer(list.size()*10);
		boolean isFirst = true;
				
		for (Integer i: list) {
			if (isFirst) {
				isFirst = false;
			} else {
				buf.append(",");
			}
			buf.append(i);
		}
		return buf.toString();
	}

	public static <T> boolean isEmpty(ArrayList<ArrayList<T>> list) {
		if (list == null || list.size() == 0 ) {
			return true;
		}
		ArrayList<T> innerList = list.get(0);
		if (innerList == null || innerList.size() == 0 ) {
			return true;
		}
		return false;
	}
}
