/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.util;

/**
 * @author ff
 *
 */
public class StringUtils {
	
	/**
	 * determines the Damerau Levenstein distance (edit distance including transpositions)
	 * between to strings, where each substitution/deletion/insertion/transposition
	 * has a cost value of 1.
	 * @see http://en.wikipedia.org/wiki/DamerauLevenshtein_distance
	 * @param l1
	 * @param l2
	 * @return
	 */
	public static int calculateDamLevDistance(String l1, String l2) {
		int[][] d = new int[l1.length() + 1][l2.length() + 1];
		int i, j, cost;
		char[] str1 = l1.toCharArray();
		char[] str2 = l2.toCharArray();

		for (i = 0; i <= str1.length; i++) {
			d[i][0] = i;
		}
		for (j = 0; j <= str2.length; j++) {
			d[0][j] = j;
		}
		for (i = 1; i <= str1.length; i++) {
			for (j = 1; j <= str2.length; j++) {

				if (str1[i - 1] == str2[j - 1])
					cost = 0;
				else
					cost = 1;

				d[i][j] = Math.min(d[i - 1][j] + 1, // Deletion
						Math.min(d[i][j - 1] + 1, // Insertion
								d[i - 1][j - 1] + cost)); // Substitution

				if ((i > 1) && (j > 1) && (str1[i - 1] == str2[j - 2])
						&& (str1[i - 2] == str2[j - 1])) {
					d[i][j] = Math.min(d[i][j], d[i - 2][j - 2] + cost); //transposition
				}
			}
		}
		int _editDistance = d[str1.length][str2.length];
		return _editDistance;
	}
	
	/**
	 * determines the edit distance between to strings, 
	 * where each substitution/deletion/insertion
	 * has a cost value of 1.
	 * @param l1
	 * @param l2
	 * @return
	 */
	public static int calculateEditDistance(String l1, String l2) {
		int[][] d = new int[l1.length() + 1][l2.length() + 1];
		int i, j, cost;
		char[] str1 = l1.toCharArray();
		char[] str2 = l2.toCharArray();

		for (i = 0; i <= str1.length; i++) {
			d[i][0] = i;
		}
		for (j = 0; j <= str2.length; j++) {
			d[0][j] = j;
		}
		for (i = 1; i <= str1.length; i++) {
			for (j = 1; j <= str2.length; j++) {

				if (str1[i - 1] == str2[j - 1])
					cost = 0;
				else
					cost = 1;

				d[i][j] = Math.min(d[i - 1][j] + 1, // Deletion
						Math.min(d[i][j - 1] + 1, // Insertion
								d[i - 1][j - 1] + cost)); // Substitution

				if ((i > 1) && (j > 1) && (str1[i - 1] == str2[j - 2])
						&& (str1[i - 2] == str2[j - 1])) {
					d[i][j] = Math.min(d[i][j], d[i - 2][j - 2] + cost); //transposition
				}
			}
		}
		int _editDistance = d[str1.length][str2.length];
		return _editDistance;
	}

}
