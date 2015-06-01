package org.ihtsdo.reconciliation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.ihtsdo.reconciliation.ChangeConsolidator.FILE_TYPE;

public class ConsolidateInfRelsSnapshotAndDelta  {

	private File snapshotSortedPreviousfile;
	private File snapshotSortedExportedfile;
	private File snapshotFinalFile;
	private Integer[] fieldsToCompare;
//	private int index;
	private String releaseDate;
	private String newLine="\r\n";
	private int colLen;
	private File deltaFinalFile;
	private BufferedWriter bw;
	private BufferedWriter dbw;
	private int effectiveTimeColIndex;
	private int[] indexes;

	public ConsolidateInfRelsSnapshotAndDelta(FILE_TYPE fType,
			File snapshotSortedPreviousfile, File snapshotSortedExportedfile,
			File snapshotFinalFile, File deltaFinalFile, String releaseDate) {
		this.snapshotSortedPreviousfile=snapshotSortedPreviousfile;	
		this.snapshotSortedExportedfile=snapshotSortedExportedfile;
		this.snapshotFinalFile=snapshotFinalFile;
		this.deltaFinalFile=deltaFinalFile;
		this.fieldsToCompare=fType.getColumnsToCompare();
		this.indexes=fType.getSnapshotIndex();
		this.effectiveTimeColIndex=fType.getEffectiveTimeColIndex();
		this.releaseDate=releaseDate;
	}

	public void execute() throws Exception {

		try {


			FileOutputStream fos = new FileOutputStream( snapshotFinalFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
			bw = new BufferedWriter(osw);

			FileOutputStream dfos = new FileOutputStream( deltaFinalFile);
			OutputStreamWriter dosw = new OutputStreamWriter(dfos,"UTF-8");
			dbw = new BufferedWriter(dosw);

			FileInputStream fis = new FileInputStream(snapshotSortedPreviousfile	);
			InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
			BufferedReader br1 = new BufferedReader(isr);

			FileInputStream fis2 = new FileInputStream(snapshotSortedExportedfile	);
			InputStreamReader isr2 = new InputStreamReader(fis2,"UTF-8");
			BufferedReader br2 = new BufferedReader(isr2);


			String line1;
			String header=br1.readLine();
			br2.readLine();

			bw.append(header);
			bw.append(newLine);
			dbw.append(header);
			dbw.append(newLine);
			
			String[] columns=header.split("\t",-1);
			colLen=columns.length;
			String[] splittedLine1;
			String line2;
			String[] splittedLine2=null;

			line2=br2.readLine();
			if (line2!=null){
				splittedLine2=line2.split("\t",-1);
			}			

			while ((line1= br1.readLine()) != null) {
				splittedLine1 = line1.split("\t",-1);

				if (line2!=null){

					int comp = idxCompare(splittedLine1,splittedLine2);
//					int comp = splittedLine1[index].compareTo(splittedLine2[index]);
					if ( comp<0){

						addPreviousLine(splittedLine1,"0");
					}else{
						if (comp>0){
							while (comp>0){
								addExportedLine(splittedLine2);
								line2=br2.readLine();
								if (line2==null){
									comp=-1;
									break;
								}
								splittedLine2=line2.split("\t",-1);
								comp = idxCompare(splittedLine1,splittedLine2);
//								comp = splittedLine1[index].compareTo(splittedLine2[index]);
							}
							if ( comp<0){
								addPreviousLine(splittedLine1,"0");
							}
						}
						while(comp==0){
							if (fieldsCompare(splittedLine1,splittedLine2)!=0){
								addExportedLine(splittedLine2);
							}else{
								addPreviousLine(splittedLine1);
							}
							line2=br2.readLine();
							if (line2==null){
								break;
							}
							splittedLine2=line2.split("\t",-1);
							comp = idxCompare(splittedLine1,splittedLine2);
//							comp = splittedLine1[index].compareTo(splittedLine2[index]);

						}
					}
				}else{
					addPreviousLine(splittedLine1,"0");
				}
			}

			if (line2!=null){

				addExportedLine(splittedLine2);

				while ((line2= br2.readLine()) != null) {
					splittedLine2=line2.split("\t",-1);
					addExportedLine(splittedLine2);
				}
			}
			br1.close();
			br2.close();
			bw.close();
			dbw.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void addExportedLine( String[] splittedLine) throws Exception {
		StringBuffer sb=new StringBuffer();
		for (int i = 0; i < colLen; i++) {
			if (i==this.effectiveTimeColIndex){
				sb.append(releaseDate);
				
			}else{
			
				sb.append(splittedLine[i]);
			}
			if (i + 1 < colLen) {
				sb.append('\t');
			}
		}
		sb.append(newLine);
		String tmp=sb.toString();
		bw.append(tmp);
		dbw.append(tmp);
	}

	private int idxCompare(String[] splittedLine1, String[] splittedLine2) {
		int iComp;
		for (int i : indexes){
			iComp=splittedLine1[i].compareTo(splittedLine2[i]);
			if (iComp!=0)
				return iComp;
		}
		return 0;
	}
	private void addPreviousLine(String[] splittedLine) throws Exception {
		for (int i = 0; i < splittedLine.length; i++) {
			bw.append(splittedLine[i]);
			if (i + 1 < splittedLine.length) {
				bw.append('\t');
			}
		}
		bw.append(newLine);
	}

	private void addPreviousLine(String[] splittedLine,String status) throws Exception {

		if (splittedLine[2].compareTo(status)!=0){
			StringBuffer sb=new StringBuffer();

			for (int i = 0; i < splittedLine.length; i++) {
				if (i==effectiveTimeColIndex){
					sb.append(releaseDate);
				}else if (i==2){
					sb.append(status);
				}else{
					sb.append(splittedLine[i]);
				}
				if (i + 1 < splittedLine.length) {
					sb.append('\t');
				}
			}
			sb.append(newLine);

			String tmp=sb.toString();
			bw.append(tmp);
			dbw.append(tmp);

		}else{		
			addPreviousLine(splittedLine);
		}

	}

	private int fieldsCompare(String[] splittedLine1, String[] splittedLine2) {
		int iComp;
		for (int i : fieldsToCompare){
			iComp=splittedLine1[i].compareTo(splittedLine2[i]);
			if (iComp!=0)
				return iComp;
		}
		return 0;
	}

	

}

