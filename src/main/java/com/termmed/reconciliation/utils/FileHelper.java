/**
 * Copyright (c) 2015 TermMed SA
 * Organization
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */
package com.termmed.reconciliation.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Comparator;

import org.apache.log4j.Logger;


/**
 * The Class FileHelper.
 *
 * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */
public class FileHelper {
	
	/** The Constant log. */
	private static final Logger log = Logger.getLogger(FileHelper.class);
	
	
	/**
	 * Count lines.
	 *
	 * @param file the file
	 * @param firstLineHeader the first line header
	 * @return the int
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static int countLines(File file, boolean firstLineHeader) throws IOException {

		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
		LineNumberReader reader = new LineNumberReader(isr);
		int cnt = 0;
		String lineRead = "";
		while ((lineRead = reader.readLine()) != null) {
		}
		
		cnt = reader.getLineNumber();
		reader.close();
		isr.close();
		fis.close();
		if(firstLineHeader){
			return cnt-1;
		}else{
			return cnt;
		}
	}

	/**
	 * Empty folder.
	 *
	 * @param folder the folder
	 */
	public static void emptyFolder(File folder){
		if(folder.isDirectory()){
			File[] files = folder.listFiles();
			for (File file : files) {
				if(file.isDirectory()){
					emptyFolder(file);
				}else{
					file.delete();
				}
			}
		}
	}
}


class FileNameComparator implements Comparator<String>{

	private static final Logger log = Logger.getLogger(FileNameComparator.class);
	private int fieldToCompare;
	private String separator;
	
	public FileNameComparator(int fieldToCompare, String separator){
		this.separator = separator;
		this.fieldToCompare = fieldToCompare;
	}
	
	public int compare(String file1, String file2) {
		String[] file1Split = file1.split(separator); 
		String[] file2Split = file2.split(separator);
		
		String date1 = file1Split[fieldToCompare];
		String date2 = file2Split[fieldToCompare];
		log.debug("First file date: " + date1);
		log.debug("Second file date: " + date2);
		
		return date1.compareTo(date2);
	}
	
}