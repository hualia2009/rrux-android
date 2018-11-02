package com.ucredit.dream.utils;

import java.util.Comparator;

import com.ucredit.dream.bean.CitySortModel;

public class PinyinComparator implements Comparator<CitySortModel> {

	public int compare(CitySortModel o1, CitySortModel o2) {
		if (o1.getSortLetters().equals("@")
				|| o2.getSortLetters().equals("#")) {
			return -1;
		} else if (o1.getSortLetters().equals("#")
				|| o2.getSortLetters().equals("@")) {
			return 1;
		} else {
			return o1.getSortLetters().compareTo(o2.getSortLetters());
		}
	}

}
