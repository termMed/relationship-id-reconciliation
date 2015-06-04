/**
 * Copyright (c) 2015 TermMed SA
 * Organization
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */
package com.termmed.reconciliation.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


/**
 * The class SnapshotGeneratorMultiColumn.
 *
 * @author Alejandro Rodriguez.
 * @version 1.0
 */
public class SnapshotGeneratorMultiColumn extends AbstractTask {

	/** The sorted file. */
	private File sortedFile;
	
	/** The column filter ixs. */
	private Integer[] columnFilterIxs;
	
	/** The column filter values. */
	private String[] columnFilterValues;
	
	/** The date. */
	private String date;
	
	/** The effective time column. */
	private int effectiveTimeColumn;
	
	/** The output file. */
	private File outputFile;
	
	/** The component columns id. */
	private int[] componentColumnsId;

	/**
	 * Instantiates a new snapshot generator multi column.
	 *
	 * @param sortedFile the sorted file
	 * @param date the date
	 * @param componentColumnsId the component columns id
	 * @param effectiveTimeColumn the effective time column
	 * @param outputFile the output file
	 * @param columnFilterIxs the column filter ixs
	 * @param columnFilterValues the column filter values
	 */
	public SnapshotGeneratorMultiColumn(File sortedFile, String date,
			int[] componentColumnsId, int effectiveTimeColumn, File outputFile,
			Integer[] columnFilterIxs,String[] columnFilterValues) {
		super();
		this.sortedFile = sortedFile;
		this.date = date;
		this.componentColumnsId = componentColumnsId;
		this.effectiveTimeColumn = effectiveTimeColumn;
		this.outputFile = outputFile;
		this.columnFilterIxs=columnFilterIxs;
		this.columnFilterValues=columnFilterValues;
	}

	/* (non-Javadoc)
	 * @see com.termmed.reconciliation.utils.AbstractTask#execute()
	 */
	public void execute(){
		
		try {
			long start1 = System.currentTimeMillis();

			FileInputStream fis = new FileInputStream(sortedFile);
			InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
			BufferedReader br = new BufferedReader(isr);

			double lines = 0;
			String nextLine;
			String header = br.readLine();

			if (outputFile.exists()){
				outputFile.delete();
			}
			FileOutputStream fos = new FileOutputStream( outputFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
			BufferedWriter bw = new BufferedWriter(osw);
			
			bw.append(header);
			bw.append("\r\n");

			String[] prevCompoId=new String[componentColumnsId.length];
			String prevLine="";
			boolean newId;
			String[] splittedLine;
			String[] prevSplittedLine;
			boolean bContinue=true;

			while ((prevLine= br.readLine()) != null) {
				prevSplittedLine =prevLine.split("\t",-1);
				
				if (columnFilterIxs!=null){
					bContinue = true;
					for (int i=0;i<columnFilterIxs.length;i++){
						if (prevSplittedLine[columnFilterIxs[i]].compareTo(columnFilterValues[i])!=0){
							bContinue=false;
							break;
						}
					}
				}
				if (bContinue){
					if (prevSplittedLine[effectiveTimeColumn].compareTo(date)<=0){
						for (int i=0;i<componentColumnsId.length;i++){
							prevCompoId[i]=prevSplittedLine[componentColumnsId[i]];
						}
						break;
					}
				}
			}
			if ( prevCompoId[0]!=null && !prevCompoId[0].equals("") ){
				while ((nextLine= br.readLine()) != null) {
					splittedLine = nextLine.split("\t",-1);
					
					if (columnFilterIxs!=null){
						bContinue = true;
						for (int i=0;i<columnFilterIxs.length;i++){
							if (splittedLine[columnFilterIxs[i]].compareTo(columnFilterValues[i])!=0){
								bContinue=false;
								break;
							}
						}
					}
					if (bContinue){

						newId=false;
						for (int i=0;i<componentColumnsId.length;i++){
							if(!splittedLine[componentColumnsId[i]].equals(prevCompoId[i])){
								newId=true;
								break;
							}
						}
						if (!newId){
							if (splittedLine[effectiveTimeColumn].compareTo(date)<=0){
								prevLine=nextLine;
								
							}
						}else{
							if (splittedLine[effectiveTimeColumn].compareTo(date)<=0){
								bw.append(prevLine);
								bw.append("\r\n");
								prevLine=nextLine;

								for (int i=0;i<componentColumnsId.length;i++){
									prevCompoId[i]=splittedLine[componentColumnsId[i]];
								}
								lines++;
							}
						}
					}
				}

				bw.append(prevLine);
				bw.append("\r\n");
				lines++;
				
			}
			
			bw.close();
			br.close();
			long end1 = System.currentTimeMillis();
			long elapsed1 = (end1 - start1);
			System.out.println("Lines in output file  : " + lines);
			System.out.println("Completed in " + elapsed1 + " ms");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
