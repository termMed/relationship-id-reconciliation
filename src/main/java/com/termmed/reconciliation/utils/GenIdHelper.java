/**
 * Copyright (c) 2015 TermMed SA
 * Organization
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */
package com.termmed.reconciliation.utils;


/**
 * The Class FileSorter.
 *
 * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */
public class GenIdHelper {

	/** The Fn f. */
	private static int[][] FnF = { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, { 1, 5, 7, 6, 2, 8, 3, 0, 9, 4 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };

	static {
		for (int i = 2; i < 8; i++) {
			for (int j = 0; j < 10; j++) {
				FnF[i][j] = FnF[i - 1][FnF[1][j]];
			}
		}
	}
	
	/** The Dihedral. */
	private static int[][] Dihedral = { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, { 1, 2, 3, 4, 0, 6, 7, 8, 9, 5 }, { 2, 3, 4, 0, 1, 7, 8, 9, 5, 6 }, { 3, 4, 0, 1, 2, 8, 9, 5, 6, 7 }, { 4, 0, 1, 2, 3, 9, 5, 6, 7, 8 }, { 5, 9, 8, 7, 6, 0, 4, 3, 2, 1 }, { 6, 5, 9, 8, 7, 1, 0, 4, 3, 2 },
			{ 7, 6, 5, 9, 8, 2, 1, 0, 4, 3 }, { 8, 7, 6, 5, 9, 3, 2, 1, 0, 4 }, { 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 } };

	/** The Inverse d5. */
	private static int[] InverseD5 = { 0, 4, 3, 2, 1, 5, 6, 7, 8, 9 };

	/**
	 * Verhoeff compute.
	 *
	 * @param idAsString the id as string
	 * @return the long
	 */
	public static long verhoeffCompute(String idAsString) {
		int check = 0;
		for (int i = idAsString.length() - 1; i >= 0; i--) {
			check = Dihedral[check][FnF[((idAsString.length() - i) % 8)][new Integer(new String(new char[] { idAsString.charAt(i) }))]];

		}
		return InverseD5[check];
	}



}
