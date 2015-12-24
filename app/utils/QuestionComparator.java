package utils;

import java.util.Comparator;
import java.util.Map;

import models.Question;

public class QuestionComparator implements Comparator {

	Map base;

	public QuestionComparator(Map base) {
		this.base = base;
	}

	public int compare(Object a, Object b) {

		if (((Question) base.get(a)).getPosition() > ((Question) base.get(b)).getPosition()) {
			return 1;
		} else if (((Question) base.get(a)).getPosition() == ((Question) base.get(b)).getPosition()) {
			return 0;
		} else {
			return -1;
		}
	}
}