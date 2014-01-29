package uk.ac.gla.psy.talklab.lex;

import java.util.Random;

public class ShuffleArray {
	
	protected Integer[] mArr;
	
	public ShuffleArray(Integer[] a) {
		Integer n = a.length;
		Random random = new Random();
		random.nextInt();
		for (Integer i = 0; i < n; i++) {
			Integer change = i + random.nextInt(n - i);
			swap(a, i, change);
		}
		mArr = a;
	}
	
	public Integer[] get() {
		return mArr;
	}

	private static void swap(Integer[] a, Integer i, Integer change) {
		int helper = a[i];
		a[i] = a[change];
		a[change] = helper;
	}
}
