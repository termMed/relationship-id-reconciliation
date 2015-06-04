package com.termmed.reconciliation.utils;


public class GenIdHelper {

	private static int[][] FnF = { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, { 1, 5, 7, 6, 2, 8, 3, 0, 9, 4 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };

	static {
		for (int i = 2; i < 8; i++) {
			for (int j = 0; j < 10; j++) {
				FnF[i][j] = FnF[i - 1][FnF[1][j]];
			}
		}
	}
	private static int[][] Dihedral = { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, { 1, 2, 3, 4, 0, 6, 7, 8, 9, 5 }, { 2, 3, 4, 0, 1, 7, 8, 9, 5, 6 }, { 3, 4, 0, 1, 2, 8, 9, 5, 6, 7 }, { 4, 0, 1, 2, 3, 9, 5, 6, 7, 8 }, { 5, 9, 8, 7, 6, 0, 4, 3, 2, 1 }, { 6, 5, 9, 8, 7, 1, 0, 4, 3, 2 },
			{ 7, 6, 5, 9, 8, 2, 1, 0, 4, 3 }, { 8, 7, 6, 5, 9, 3, 2, 1, 0, 4 }, { 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 } };

	private static int[] InverseD5 = { 0, 4, 3, 2, 1, 5, 6, 7, 8, 9 };

	public static long verhoeffCompute(String idAsString) {
		int check = 0;
		for (int i = idAsString.length() - 1; i >= 0; i--) {
			check = Dihedral[check][FnF[((idAsString.length() - i) % 8)][new Integer(new String(new char[] { idAsString.charAt(i) }))]];

		}
		return InverseD5[check];
	}



}
