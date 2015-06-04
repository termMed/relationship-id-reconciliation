/**
 * Copyright (c) 2015 TermMed SA
 * Organization
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */
package com.termmed.reconciliation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import com.termmed.reconciliation.model.Relationship;
import com.termmed.reconciliation.model.RelationshipGroup;
import com.termmed.reconciliation.model.RelationshipGroupList;
import com.termmed.reconciliation.utils.I_Constants;


/**
 * The Class RelationshipReconciliation.
 *
 * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */
public class RelationshipReconciliation {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args){

		logger = Logger.getLogger("com.termmed.reconciliation.RelationshipReconciliation");
		try {
			
			File file =new File(I_Constants.RUN_CONFIGURATION_FILE);
			if (!file.exists()){
				logger.info("Error happened getting params. Params file doesn't exist");
				System.exit(0);
			}

			RelationshipReconciliation cc=new RelationshipReconciliation(file);
			cc.execute();
			cc=null;

		} catch (Exception e) {
			e.printStackTrace();
		} 
		System.exit(0);
	}
	/** The prev inferred rels. */
	private String[] previousInferredRelationshipsFile;
	
	/** The temp relationship store. */
	private File tempRelationshipStore;

	/** The config. */
	private File config;
	
	/** The output relationships. */
	private String outputRelationships;

	/**
	 * Instantiates a new classification runner.
	 *
	 * @param previousInferredRelationships the prev inferred rels
	 * @param currentInferredRelationships the output rels
	 * @param outputRelationships the output relationship file
	 */
	public RelationshipReconciliation(
			String[] previousInferredRelationships, String[] currentInferredRelationships,
			String outputRelationships) {
		super();

		logger = Logger.getLogger("com.termmed.reconciliation.RelationshipReconciliation");

		this.currentRelationshipsFile = currentInferredRelationships;
		this.previousInferredRelationshipsFile = previousInferredRelationships;

		this.outputRelationships=outputRelationships;
	}

	/**
	 * Instantiates a new relationship reconciliation.
	 *
	 * @param config the config
	 * @throws ConfigurationException the configuration exception
	 */
	public RelationshipReconciliation(File config) throws ConfigurationException {
		this.config=config;

		logger = Logger.getLogger("com.termmed.reconciliation.RelationshipReconciliation");

		getParams();

	}

	/** The edit snomed rels. */
	private  ArrayList<Relationship> previousRelationships;

	/** The logger. */
	private static Logger logger;

	/** The c rocket sno rels. */
	private  ArrayList<Relationship> currentRelationships;

	/** The isa. */
	private  long isa;

	/** The current relationships file. */
	private  String[] currentRelationshipsFile;

	/** The xml config. */
	private XMLConfiguration xmlConfig;

	/** The new no rec. */
	private HashMap<Long,ArrayList<Relationship>> newNoRec;

	/** The prev act now ret. */
	private HashMap<Long,ArrayList<Relationship>> prevActNowRet;

	/** The prev inact. */
	private HashMap<Long,TreeMap<String,Relationship>> prevInact;

	/** The bw. */
	private BufferedWriter bw;

	/** The tmp new no rec. */
	private HashMap<Long, ArrayList<Relationship>> tmpNewNoRec;
	
	/** The sum b_ total. */
	private int sumB_Total;
	
	/** The sum same isa. */
	private int sumSameISA;
	
	/** The sum a_ diff. */
	private int sumA_Diff;
	
	/** The sum a_ diff isa. */
	private int sumA_DiffISA;
	
	/** The sum b_ diff. */
	private int sumB_Diff;
	
	/** The sum b_ diff isa. */
	private int sumB_DiffISA;


	/**
	 * Execute the reconciliation.
	 */
	public void execute(){

		try {
			sumB_Total = 0;
			sumSameISA = 0;
			sumA_Diff = 0;
			sumA_DiffISA = 0;
			sumB_Diff = 0;
			sumB_DiffISA = 0;

			long startTime = System.currentTimeMillis();
			previousRelationships = new ArrayList<Relationship>();
			prevActNowRet = new HashMap<Long,ArrayList<Relationship>>();
			prevInact = new HashMap<Long,TreeMap<String,Relationship>>();
			newNoRec = new HashMap<Long,ArrayList<Relationship>>();
			tmpNewNoRec=new HashMap<Long,ArrayList<Relationship>>();
			tempRelationshipStore=new File (outputRelationships);
			isa=Long.parseLong(I_Constants.ISA);
			FileOutputStream fos = new FileOutputStream( tempRelationshipStore);
			OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
			bw = new BufferedWriter(osw);

			bw.append("id");
			bw.append("\t");
			bw.append("effectiveTime");
			bw.append("\t");
			bw.append("active");
			bw.append("\t");
			bw.append("moduleId");
			bw.append("\t");
			bw.append("sourceId");
			bw.append("\t");
			bw.append("destinationId");
			bw.append("\t");
			bw.append("relationshipGroup");
			bw.append("\t");
			bw.append("typeId");
			bw.append("\t");
			bw.append("characteristicTypeId");
			bw.append("\t");
			bw.append("modifierId");
			bw.append("\r\n");

			currentRelationships = new ArrayList<Relationship>();
			
			loadActiveInferredRelationship(currentRelationshipsFile,currentRelationships,0);

			previousRelationships = new ArrayList<Relationship>();

			loadActiveInferredRelationship(previousInferredRelationshipsFile,previousRelationships,1);

			groupReassignment(previousRelationships, currentRelationships);

			logger.info(compareActivesAndWriteBack(previousRelationships, currentRelationships));

			previousRelationships=null;
			currentRelationships=null;
			newNoRec=tmpNewNoRec;
			tmpNewNoRec=new HashMap<Long, ArrayList<Relationship>>();

			for (int i=1;i<5;i++){
				if (newNoRec.size()>0 && prevActNowRet.size()>0){	

					logger.info(compareActivesWOMatch(i));
				}else{
					break;
				}
				newNoRec=tmpNewNoRec;
				tmpNewNoRec=new HashMap<Long, ArrayList<Relationship>>();

			}
			prevActNowRet=null;
			if (newNoRec.size()>0 ){

				loadNotActiveInferredRelationship(previousInferredRelationshipsFile);

				for (int i=1;i<5;i++){
					if (newNoRec.size()>0 && prevInact.size()>0){	

						logger.info(comparePrevInact(i));
					}else{
						break;
					}
					newNoRec=tmpNewNoRec;
					tmpNewNoRec=new HashMap<Long, ArrayList<Relationship>>();
				}
			}
			if (newNoRec.size()>0 ){
				writeNoReconciledRel();
			}
			bw.close();
			bw=null;
			osw=null;
			fos=null;

			StringBuilder s = new StringBuilder();
			s.append("\r\n::: Complete Process statistics:");
			s.append("\r\n::: Reconciled relationships:  \t").append(sumB_Total);
			s.append("\r\n::: Reconciled Isa's relationships:  \t").append(sumSameISA);
			s.append("\r\n::: Previous relationships without match :   \t").append(sumA_Diff);
//			s.append("\r\n::: Previous Isa's relationships without match:\t").append(sumA_DiffISA);
			s.append("\r\n::: Current relationships without match:   \t").append(sumB_Diff);
			s.append("\r\n::: Current Isa's relationships without match:\t").append(sumB_DiffISA);
			s.append("\r\n::: ");
			s.append("\r\n::: *** WROTE *** LAPSED TIME =\t" + toStringLapseSec(startTime) + "\t ***");
			logger.info(s.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Group reassignment.
	 *
	 * @param snorelA the snorel a
	 * @param snorelB the snorel b
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void groupReassignment(
			ArrayList<Relationship> snorelA,
			ArrayList<Relationship> snorelB) 
					throws  IOException {

		int countChangedGroupNumber = 0;
		long startTime = System.currentTimeMillis();

		StringBuilder s = new StringBuilder();
		s.append("\r\n::: [Start group number reconciliation]");
		Collections.sort(snorelA);
		Collections.sort(snorelB);
		Iterator<Relationship> itA = snorelA.iterator();
		Iterator<Relationship> itB = snorelB.iterator();
		Relationship rel_A = null;
		boolean done_A = false;
		if (itA.hasNext()) {
			rel_A = itA.next();
		} else {
			done_A = true;
		}
		Relationship rel_B = null;
		boolean done_B = false;
		if (itB.hasNext()) {
			rel_B = itB.next();
		} else {
			done_B = true;
		}


		// BY SORT ORDER, LOWER NUMBER ADVANCES FIRST
		while (!done_A && !done_B) {

			if (rel_A.sourceId == rel_B.sourceId) {
				long thisC1 = rel_A.sourceId;
				// REMAINDER LIST_A GROUP 0 FOR C1
				while (rel_A.sourceId == thisC1 && rel_A.group == 0 && !done_A) {

					if (itA.hasNext()) {
						rel_A = itA.next();
					} else {
						done_A = true;
						break;
					}
				}

				// REMAINDER LIST_B GROUP 0 FOR C1
				while (rel_B.sourceId == thisC1 && rel_B.group == 0 && !done_B) {

					if (itB.hasNext()) {
						rel_B = itB.next();
					} else {
						done_B = true;
						break;
					}
				}

				// ** SEGMENT GROUPS **
				RelationshipGroupList groupList_A = new RelationshipGroupList();
				RelationshipGroupList groupList_B = new RelationshipGroupList();
				RelationshipGroup groupA = null;
				RelationshipGroup groupB = null;

				// SEGMENT GROUPS IN LIST_A
				int prevGroup = Integer.MIN_VALUE;
				while (rel_A.sourceId == thisC1 && !done_A) {
					if (rel_A.group != prevGroup) {
						groupA = new RelationshipGroup();
						groupList_A.add(groupA);
					}

					groupA.add(rel_A);

					prevGroup = rel_A.group;
					if (itA.hasNext()) {
						rel_A = itA.next();
					} else {
						done_A = true;
					}
				}
				// SEGMENT GROUPS IN LIST_B
				prevGroup = Integer.MIN_VALUE;
				while (rel_B.sourceId == thisC1 && !done_B) {
					if (rel_B.group != prevGroup) {
						groupB = new RelationshipGroup();
						groupList_B.add(groupB);
					}

					groupB.add(rel_B);

					prevGroup = rel_B.group;
					if (itB.hasNext()) {
						rel_B = itB.next();
					} else {
						done_B = true;
					}
				}

				int dist;
				HashMap<Integer,List<Integer[]>> mapGroups=new HashMap<Integer, List<Integer[]>>(); 
				HashMap<Integer,List<Integer[]>> minDist=new HashMap<Integer, List<Integer[]>>();
				Integer prevRGNum;
				Integer currRGNum;
				if (groupList_B.size() > 0) {
					for (Integer sgb=0 ;sgb< groupList_B.size();sgb++) {
						currRGNum=groupList_B.get(sgb).get(0).group;
						if (currRGNum != 0) {
							for (Integer  sga=0 ;sga< groupList_A.size();sga++) {
								prevRGNum=groupList_A.get(sga).get(0).group;
								if (prevRGNum != 0 ) {

									dist=groupList_B.get(sgb).getDistanceToGroupInSameConcept(groupList_A.get(sga));
									List<Integer[]> tmplist;
									if (mapGroups.containsKey(currRGNum)){
										tmplist=mapGroups.get(currRGNum);
									}else{
										tmplist=new ArrayList<Integer[]>();

									}
									tmplist.add(new Integer[]{prevRGNum,dist});
									mapGroups.put(currRGNum,tmplist);

									List<Integer[]> tmplist2;
									if (minDist.containsKey(prevRGNum)){
										tmplist2=minDist.get(prevRGNum); 
									}else{
										tmplist2=new  ArrayList<Integer[]>();
									}
									tmplist2.add(new Integer[]{currRGNum,dist});
									minDist.put(prevRGNum, tmplist2);
								}
							}
						} 
					}
					HashMap<Integer,Integer> endMap=new HashMap<Integer,Integer>();

					if (mapGroups.size()>0){
						Integer bestPrevRGNum;
						Integer bestCurrRGNum;
						List<Integer> usageCurrGroups=new ArrayList<Integer>();
						List<Integer> usagePrevGroups=new ArrayList<Integer>();
						boolean incomplete=true;
						boolean tooMuchPrev=false;
						boolean tooMuchCurr=false;
						while (incomplete && !tooMuchPrev && !tooMuchCurr){
							incomplete=false;
							for (Integer sgb=0 ;sgb< groupList_B.size();sgb++) {
								currRGNum=groupList_B.get(sgb).get(0).group;
								if (!usageCurrGroups.contains(currRGNum)){
									incomplete=true;
									List<Integer[]> currDistances=mapGroups.get(currRGNum);
									bestPrevRGNum=getBestGroupNumber(currDistances, usagePrevGroups);
									if (bestPrevRGNum==null){
										tooMuchCurr=true;
										break;
									}
									List<Integer[]> prevDistances=minDist.get(bestPrevRGNum);
									bestCurrRGNum=getBestGroupNumber(prevDistances, usageCurrGroups);
									if (bestCurrRGNum==null){
										tooMuchPrev=true;
										break;
									}
									if (bestCurrRGNum==currRGNum){
										endMap.put(currRGNum,bestPrevRGNum);
										usageCurrGroups.add(currRGNum);
										usagePrevGroups.add(bestPrevRGNum);
									}
								}
							}
						}
					}
					Integer nextNum;
					for (Integer sgb=0 ;sgb< groupList_B.size();sgb++) {
						currRGNum=groupList_B.get(sgb).get(0).group;
						if (!endMap.containsKey(currRGNum)){
							nextNum=nextRoleGroupNumber(endMap);
							endMap.put(currRGNum,nextNum);
						}
					}
					for (RelationshipGroup relationshipGroup:groupList_B){
						for (Relationship relationship:relationshipGroup){
							if (relationship.group!=endMap.get(relationship.group)){
								countChangedGroupNumber++;
								relationship.group=endMap.get(relationship.group);
							}
						}
					}
				}
			} else if (rel_A.sourceId > rel_B.sourceId) {
				// CASE 2: LIST_B HAS CONCEPT NOT IN LIST_A
				long thisC1 = rel_B.sourceId;
				while (rel_B.sourceId == thisC1) {

					if (itB.hasNext()) {
						rel_B = itB.next();
					} else {
						done_B = true;
						break;
					}
				}

			} else {
				// CASE 3: LIST_A HAS CONCEPT NOT IN LIST_B
				long thisC1 = rel_A.sourceId;
				while (rel_A.sourceId == thisC1) {
					if (itA.hasNext()) {
						rel_A = itA.next();
					} else {
						done_A = true;
						break;
					}
				}
			}
		}

		s.append("\r\n::: Relationships with group number changes = \t" + countChangedGroupNumber);
		s.append("\r\n::: [Partial time] Sort/Compare Input & Output: \t" + toStringLapseSec(startTime) + "\t(mS)\t");
		s.append("\r\n");
		logger.info(s.toString());
	}

	/**
	 * Gets the best group number.
	 *
	 * @param distances the distances
	 * @param usageGroups the usage groups
	 * @return the best group number
	 */
	private Integer getBestGroupNumber(List<Integer[]> distances,
			List<Integer> usageGroups) {
		Integer bestGr=null;
		Integer prevDist=Integer.MAX_VALUE;
		for (Integer[] tuple:distances){
			if (!usageGroups.contains(tuple[0])){
				Integer dist=tuple[1];
				if (dist<=prevDist ){
					bestGr=tuple[0];
					prevDist=dist;
				}
			}
		}
		return bestGr;
	}

	/**
	 * Next role group number.
	 *
	 * @param map the map
	 * @return the integer
	 */
	private Integer nextRoleGroupNumber(HashMap<Integer,Integer> map) {

		Integer testNum = 1;
		boolean exists=true;
		while (exists){
			exists=false;
			for (Integer i :map.keySet()) {
				if (map.get(i) == testNum) {
					exists = true;
					break;
				}
			}

			if (exists) {
				testNum++;
			}
		}

		return testNum;
	}
	
	/**
	 * Compare prev inact.
	 *
	 * @param step the step
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private String comparePrevInact(int step) throws IOException {
		// STATISTICS COUNTERS
		int countSameISA = 0;
		int countA_Diff = 0;
		int countB_Diff = 0;
		int countB_DiffISA = 0;
		int countB_Total = 0;
		int sumPrevInact = 0;
		int sumNewNoRec = 0;
		long startTime = System.currentTimeMillis();


		StringBuilder s = new StringBuilder();
		s.append("\r\n::: [Reconciliation by previous inactives vs current actives, without grouping comparation - step:" + step + "]");

		boolean reconciled=false;
		for (Long conceptId:newNoRec.keySet()){
			TreeMap<String, Relationship> relsPrev = prevInact.get(conceptId);
			ArrayList<Relationship> relsCurr = newNoRec.get(conceptId);
			sumNewNoRec+=relsCurr.size();
			if (relsPrev!=null){
				sumPrevInact+=relsPrev.size();
				for (Relationship relC:relsCurr){
					reconciled=false;
					for (String key:relsPrev.descendingKeySet()){
						Relationship relP=relsPrev.get(key);
						if (compareRelsStep(relC, relP,step)) {
							writeReconciled(bw,relC,relP);

							countB_Total++;
							if (relC.typeId == isa) {
								countSameISA++;
							}
							reconciled=true;
							relsPrev.remove(key);
							break;
						}
					}
					if(!reconciled){
						countB_Diff++;
						if (relC.typeId == isa) {
							countB_DiffISA++;
						}
						writeNewNoRec(relC);
					}
				}
				prevInact.put(conceptId, relsPrev);
			}else{
				for (Relationship relC:relsCurr){
					countB_Diff++;
					if (relC.typeId == isa) {
						countB_DiffISA++;
					}
					writeNewNoRec(relC);

				}
			}

		}


		s.append("\r\n::: Current active relationships to reconcile = \t" + sumNewNoRec);
		s.append("\r\n::: Candidate previous inactive relationships to match = \t" + sumPrevInact);
		
		s.append("\r\n::: Partial process statistics:");
		s.append("\r\n::: Reconciled relationships:  \t").append(countB_Total);
		s.append("\r\n::: Reconciled Isa's relationships:  \t").append(countSameISA);
		s.append("\r\n::: Previous relationships without match :   \t").append(countA_Diff);
		s.append("\r\n::: Current relationships without match:   \t").append(countB_Diff);
		s.append("\r\n::: Current Isa's relationships without match:\t").append(countB_DiffISA);
		s.append("\r\n::: ");

		long lapseTime = System.currentTimeMillis() - startTime;
		s.append("\r\n::: [Partial time] Sort/Compare Input & Output: \t").append(lapseTime);
		s.append("\t(mS)\t");
		s.append("\r\n");

		sumB_Total+=countB_Total;
		sumSameISA+=countSameISA;
		sumA_Diff=countA_Diff;
		sumB_Diff=countB_Diff;
		sumB_DiffISA=countB_DiffISA;
		
		return s.toString();
	}

	/**
	 * Compare actives wo match.
	 *
	 * @param step the step
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private String compareActivesWOMatch(  int step) throws IOException {
		// STATISTICS COUNTERS
		int countSameISA = 0;
		int countA_Diff = 0;
		int countB_Diff = 0;
		int countB_DiffISA = 0;
		int countB_Total = 0;
		int sumNewNoRec = 0;
		int sumPrevActNowRet = 0 ;
		long startTime = System.currentTimeMillis();

		StringBuilder s = new StringBuilder();
		s.append("\r\n::: [Reconciliation by previous actives vs current actives, without grouping comparation - step:" + step + "]");

		boolean reconciled=false;
		for (Long conceptId:newNoRec.keySet()){
			
			ArrayList<Relationship> relsPrev = prevActNowRet.get(conceptId);
			ArrayList<Relationship> relsCurr = newNoRec.get(conceptId);

			sumNewNoRec+=relsCurr.size();
			if (relsPrev!=null){
				sumPrevActNowRet+=relsPrev.size();
				for (Relationship relC:relsCurr){
					reconciled=false;
					for (Relationship relP:relsPrev){
						if (compareRelsStep(relC, relP,step)) {
							writeReconciled(bw,relC,relP);
							countB_Total++;
							if (relC.typeId == isa) {
								countSameISA++;
							}
							reconciled=true;
							relsPrev.remove(relP);
							break;
						}
					}
					if(!reconciled){
						countB_Diff++;
						if (relC.typeId == isa) {
							countB_DiffISA++;
						}
						writeNewNoRec(relC);
					}
				}

				countA_Diff+=relsPrev.size();
				prevActNowRet.put(conceptId, relsPrev);
			}else{
				for (Relationship relC:relsCurr){
					countB_Diff++;
					if (relC.typeId == isa) {
						countB_DiffISA++;
					}
					writeNewNoRec(relC);
				}
			}

		}

		s.append("\r\n::: Current active relationships to reconcile = \t" + sumNewNoRec);
		s.append("\r\n::: Candidate previous active relationships to match = \t" + sumPrevActNowRet);
		
		s.append("\r\n::: Partial process statistics:");
		s.append("\r\n::: Reconciled relationships:  \t").append(countB_Total);
		s.append("\r\n::: Reconciled Isa's relationships:  \t").append(countSameISA);
		s.append("\r\n::: Previous relationships without match :   \t").append(countA_Diff);
		s.append("\r\n::: Current relationships without match:   \t").append(countB_Diff);
		s.append("\r\n::: Current Isa's relationships without match:\t").append(countB_DiffISA);
		s.append("\r\n::: ");

		long lapseTime = System.currentTimeMillis() - startTime;
		s.append("\r\n::: [Partial time] Sort/Compare Input & Output: \t").append(lapseTime);
		s.append("\t(mS)\t");
		s.append("\r\n");

		sumB_Total+=countB_Total;
		sumSameISA+=countSameISA;
		sumA_Diff=countA_Diff;
		sumB_Diff=countB_Diff;
		sumB_DiffISA=countB_DiffISA;
		
		return s.toString();
	}

	/**
	 * Compare rels step.
	 *
	 * @param inR the in r
	 * @param outR the out r
	 * @param step the step
	 * @return true, if successful
	 */
	private boolean compareRelsStep(Relationship inR, Relationship outR,int step) {

		switch (step){

		case 1:
			if ((inR.sourceId == outR.sourceId) && (inR.group == outR.group) && (inR.typeId == outR.typeId)
					&& (inR.destinationId == outR.destinationId) && (inR.effTime == outR.effTime)) {
				return true; 
			}
			return false;
		case 2:
			if ((inR.sourceId == outR.sourceId)  && (inR.typeId == outR.typeId)
					&& (inR.destinationId == outR.destinationId) && (inR.effTime == outR.effTime)) {
				return true; 
			}
			return false;
		case 3:
			if ((inR.sourceId == outR.sourceId)  && (inR.typeId == outR.typeId)
					&& (inR.destinationId == outR.destinationId) && (inR.group == outR.group)) {
				return true; 
			}
			return false;
		case 4:
			if ((inR.sourceId == outR.sourceId)  && (inR.typeId == outR.typeId)
					&& (inR.destinationId == outR.destinationId) ) {
				return true; 
			}
			return false;
		}
		return false;
	}



	/**
	 * To string lapse sec.
	 *
	 * @param startTime the start time
	 * @return the string
	 */
	private  String toStringLapseSec(long startTime) {
		StringBuilder s = new StringBuilder();
		long stopTime = System.currentTimeMillis();
		long lapseTime = stopTime - startTime;
		s.append((float) lapseTime / 1000).append(" (seconds)");
		return s.toString();
	}


	/**
	 * Load active inferred relationship.
	 *
	 * @param relationshipFiles the relationship files
	 * @param rels the rels
	 * @param i the i
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public  void loadActiveInferredRelationship( String[] relationshipFiles,ArrayList<Relationship> rels, int i)throws IOException {

		String line;
		String[] spl;

		long c1;
		long c2;
		int rg;
		long ty;
		int et;
		long mo;
		short ac;
		long ch=Long.parseLong(I_Constants.INFERRED);

		for (String relFile:relationshipFiles){
			FileInputStream rfis = new FileInputStream(relFile);
			InputStreamReader risr = new InputStreamReader(rfis,"UTF-8");
			BufferedReader rbr = new BufferedReader(risr);
			rbr.readLine();

			while((line=rbr.readLine())!=null){

				spl=line.split("\t",-1);
				if (spl[2].equals("1") && spl[8].equals(I_Constants.INFERRED)){
					c1=Long.parseLong(spl[4]);
					c2=Long.parseLong(spl[5]);
					rg=Integer.parseInt(spl[6]);
					ty=Long.parseLong(spl[7]);
					et=Integer.parseInt(spl[1]);
					mo=Long.parseLong(spl[3]);
					ac=Short.parseShort(spl[2]);
					String rid;
					if (i==0){
						rid="";
					}else{
						rid=spl[0];
					}
					Relationship rel=new Relationship(c1,c2,ty,rg,rid,et,mo,ac,ch);
					rels.add(rel);
				}
			}
			rbr.close();
			rbr=null;
		}
	}

	/**
	 * Load not active inferred relationship.
	 *
	 * @param relationshipFiles the relationship files
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public  void loadNotActiveInferredRelationship( String[] relationshipFiles)throws IOException {

		String line;
		String[] spl;

		long c1;
		long c2;
		int rg;
		long ty;
		int et;
		long mo;
		short ac;
		long ch;

		for (String relFile:relationshipFiles){
			FileInputStream rfis = new FileInputStream(relFile);
			InputStreamReader risr = new InputStreamReader(rfis,"UTF-8");
			BufferedReader rbr = new BufferedReader(risr);
			rbr.readLine();

			TreeMap<String,Relationship> tRels;
			while((line=rbr.readLine())!=null){

				spl=line.split("\t",-1);
				if (!spl[2].equals("1") && spl[8].equals(I_Constants.INFERRED)){

					c1=Long.parseLong(spl[4]);
					c2=Long.parseLong(spl[5]);
					rg=Integer.parseInt(spl[6]);
					ty=Long.parseLong(spl[7]);
					et=Integer.parseInt(spl[1]);
					mo=Long.parseLong(spl[3]);
					ac=Short.parseShort(spl[2]);
					ch=Long.parseLong(spl[8]);

					Relationship rel=new Relationship(c1,c2,ty,rg,spl[0],et,mo,ac,ch);

					if (prevInact.containsKey(c1)){
						tRels=prevInact.get(c1);
					}else{
						tRels=new TreeMap<String,Relationship>();
					}
					tRels.put(spl[1] +  spl[0],rel);
					prevInact.put(c1,tRels);
				}
			}
			rbr.close();
			rbr=null;
		}
	}

	/**
	 * Write no reconciled rel.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private  void writeNoReconciledRel( )
			throws IOException {

		for (Long conceptId:newNoRec.keySet()){
			List<Relationship> rels=newNoRec.get(conceptId);
			for (Relationship infRel:rels){
				writeRF2TypeLine(bw,"null",infRel.effTime,1,infRel.module,infRel.sourceId,
						infRel.destinationId,infRel.group,infRel.typeId,
						I_Constants.INFERRED, I_Constants.SOMEMODIFIER);
			}
		}

	}

	/**
	 * Write reconciled.
	 *
	 * @param bw the bw
	 * @param infRel the inf rel
	 * @param prevRel the prev rel
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private  void writeReconciled(BufferedWriter bw,Relationship infRel, Relationship prevRel)
			throws  IOException {

		writeRF2TypeLine(bw,prevRel.getRelId(),infRel.effTime,1,infRel.module,infRel.sourceId,
				infRel.destinationId,infRel.group,infRel.typeId,
				I_Constants.INFERRED, I_Constants.SOMEMODIFIER);

	}
	
	/**
	 * Write r f2 type line.
	 *
	 * @param bw the bw
	 * @param relationshipId the relationship id
	 * @param effectiveTime the effective time
	 * @param active the active
	 * @param moduleId the module id
	 * @param sourceId the source id
	 * @param destinationId the destination id
	 * @param relationshipGroup the relationship group
	 * @param relTypeId the rel type id
	 * @param characteristicTypeId the characteristic type id
	 * @param modifierId the modifier id
	 * @throws IOException Signals that an I/O exception has occurred.
	 */

	public static void writeRF2TypeLine(BufferedWriter bw, String relationshipId, int effectiveTime, int active, long moduleId, long sourceId, long destinationId, int relationshipGroup, long relTypeId,
			String characteristicTypeId, String modifierId) throws IOException {
		bw.append( relationshipId + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + sourceId + "\t" + destinationId + "\t" + relationshipGroup + "\t" + relTypeId
				+ "\t" + characteristicTypeId + "\t" + modifierId);
		bw.append( "\r\n");
	}

	/**
	 * Compare actives and write back.
	 *
	 * @param snorelA the snorel a
	 * @param snorelB the snorel b
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private  String compareActivesAndWriteBack(List<Relationship> snorelA, List<Relationship> snorelB)
			throws  IOException {

		// STATISTICS COUNTERS
		int countA_Diff = 0;
		int countA_DiffISA = 0;
		int countB_Diff = 0;
		int countB_DiffISA = 0;
		int countB_Total = 0;
		int countSameISA = 0;

		long startTime = System.currentTimeMillis();
		Collections.sort(snorelA);
		Collections.sort(snorelB);

		Iterator<Relationship> itA = snorelA.iterator();
		Iterator<Relationship> itB = snorelB.iterator();
		Relationship rel_A = null;
		boolean done_A = false;
		if (itA.hasNext()) {
			rel_A = itA.next();
		} else {
			done_A = true;
		}
		Relationship rel_B = null;
		boolean done_B = false;
		if (itB.hasNext()) {
			rel_B = itB.next();
		} else {
			done_B = true;
		}

		StringBuilder s = new StringBuilder();
		s.append("\r\n::: [Reconciliation by previous actives vs current actives, ungrouped and grouped comparation]"
				+ "\r\n::: Current active relationships to reconcile = \t" + snorelB.size()
				+ "\r\n::: Candidate Previous active relationships to match = \t" + snorelA.size());

		// BY SORT ORDER, LOWER NUMBER ADVANCES FIRST
		while (!done_A && !done_B) {

			if (rel_A.sourceId == rel_B.sourceId) {
				// COMPLETELY PROCESS ALL C1 FOR BOTH IN & OUT
				// PROCESS C1 WITH GROUP == 0
				long thisC1 = rel_A.sourceId;
				// PROCESS WHILE BOTH HAVE GROUP 0
				while (rel_A.sourceId == thisC1 && rel_B.sourceId == thisC1 && rel_A.group == 0
						&& rel_B.group == 0 && !done_A && !done_B) {

					// PROGESS GROUP ZERO
					switch (compareSnoRel(rel_A, rel_B)) {
					case 1: // SAME
						// GATHER STATISTICS
						writeReconciled(bw,rel_B,rel_A);
						countB_Total++;
						if (rel_A.typeId == isa) {
							countSameISA++;
						}
						if (itA.hasNext()) {
							rel_A = itA.next();
						} else {
							done_A = true;
						}
						if (itB.hasNext()) {
							rel_B = itB.next();
						} else {
							done_B = true;
						}
						break;

					case 2: // REL_A > REL_B -- B has extra stuff
						countB_Diff++;
						if (rel_B.typeId == isa) {
							countB_DiffISA++;
						}
						writeNewNoRec(rel_B);

						if (itB.hasNext()) {
							rel_B = itB.next();
						} else {
							done_B = true;
						}
						break;

					case 3: // REL_A < REL_B -- A has extra stuff
						// GATHER STATISTICS
						countA_Diff++;
						if (rel_A.typeId == isa) {
							countA_DiffISA++;
						}
						writePrevActNowRet(rel_A);

						if (itA.hasNext()) {
							rel_A = itA.next();
						} else {
							done_A = true;
						}
						break;
					} // switch
				}

				// REMAINDER LIST_A GROUP 0 FOR C1
				while (rel_A.sourceId == thisC1 && rel_A.group == 0 && !done_A) {

					countA_Diff++;
					if (rel_A.typeId == isa) {
						countA_DiffISA++;
					}
					writePrevActNowRet(rel_A);
					if (itA.hasNext()) {
						rel_A = itA.next();
					} else {
						done_A = true;
						break;
					}
				}

				// REMAINDER LIST_B GROUP 0 FOR C1
				while (rel_B.sourceId == thisC1 && rel_B.group == 0 && !done_B) {
					countB_Diff++;
					if (rel_B.typeId == isa) {
						countB_DiffISA++;
					}
					writeNewNoRec(rel_B);
					if (itB.hasNext()) {
						rel_B = itB.next();
					} else {
						done_B = true;
						break;
					}
				}

				// ** SEGMENT GROUPS **
				RelationshipGroupList groupList_A = new RelationshipGroupList();
				RelationshipGroupList groupList_B = new RelationshipGroupList();
				RelationshipGroup groupA = null;
				RelationshipGroup groupB = null;

				// SEGMENT GROUPS IN LIST_A
				int prevGroup = Integer.MIN_VALUE;
				while (rel_A.sourceId == thisC1 && !done_A) {
					if (rel_A.group != prevGroup) {
						groupA = new RelationshipGroup();
						groupList_A.add(groupA);
					}

					groupA.add(rel_A);

					prevGroup = rel_A.group;
					if (itA.hasNext()) {
						rel_A = itA.next();
					} else {
						done_A = true;
					}
				}
				// SEGMENT GROUPS IN LIST_B
				prevGroup = Integer.MIN_VALUE;
				while (rel_B.sourceId == thisC1 && !done_B) {
					if (rel_B.group != prevGroup) {
						groupB = new RelationshipGroup();
						groupList_B.add(groupB);
					}

					groupB.add(rel_B);

					prevGroup = rel_B.group;
					if (itB.hasNext()) {
						rel_B = itB.next();
					} else {
						done_B = true;
					}
				}

				// FIND GROUPS IN GROUPLIST_A WITHOUT AN EQUAL IN GROUPLIST_B
				RelationshipGroupList groupList_NotEqual;
				if (groupList_A.size() > 0) {
					groupList_NotEqual = groupList_A.whichNotEqual(groupList_B);
					for (RelationshipGroup sg : groupList_NotEqual) {
						for (Relationship sr_A : sg) {

							countA_Diff++;
							writePrevActNowRet(sr_A);
						}
					}
				}

				// FIND GROUPS IN GROUPLIST_B WITHOUT AN EQUAL IN GROUPLIST_A
				if (groupList_B.size() > 0) {
					groupList_NotEqual = groupList_B.whichNotEqual(groupList_A);
					for (RelationshipGroup sg : groupList_NotEqual) {
						if (sg.get(0).group != 0) {
							for (Relationship sr_B : sg) {
								countB_Diff++;
								writeNewNoRec(sr_B);
							}
						} else {
							for (Relationship sr_B : sg) {

								countB_Diff++;
								writeNewNoRec(sr_B);
							}
						}
					}
				}
				if (groupList_A.size() > 0 && groupList_B.size() > 0) {
					Map	<Relationship,Relationship> relsMap;
					relsMap = groupList_B.getEqualRelationshipInGroup(groupList_A);
					for (Relationship sr_B : relsMap.keySet()) {
						countB_Total++;
						if (rel_B.typeId == isa) {
							countSameISA++;
						}
						writeReconciled(bw,sr_B,relsMap.get(sr_B));

					}
				}
			} else if (rel_A.sourceId > rel_B.sourceId) {
				// CASE 2: LIST_B HAS CONCEPT NOT IN LIST_A
				long thisC1 = rel_B.sourceId;
				while (rel_B.sourceId == thisC1) {
					countB_Diff++;
					if (rel_B.typeId == isa) {
						countB_DiffISA++;
					}
					writeNewNoRec(rel_B);
					if (itB.hasNext()) {
						rel_B = itB.next();
					} else {
						done_B = true;
						break;
					}
				}

			} else {
				// CASE 3: LIST_A HAS CONCEPT NOT IN LIST_B
				long thisC1 = rel_A.sourceId;
				while (rel_A.sourceId == thisC1) {
					countA_Diff++;
					if (rel_A.typeId == isa) {
						countA_DiffISA++;
					}
					writePrevActNowRet(rel_A);
					if (itA.hasNext()) {
						rel_A = itA.next();
					} else {
						done_A = true;
						break;
					}
				}
			}
		}

		// AT THIS POINT, THE PREVIOUS C1 HAS BE PROCESSED COMPLETELY
		// AND, EITHER REL_A OR REL_B HAS BEEN COMPLETELY PROCESSED
		// AND, ANY REMAINDER IS ONLY ON REL_LIST_A OR ONLY ON REL_LIST_B
		// AND, THAT REMAINDER HAS A "STANDALONE" C1 VALUE
		//
		// LASTLY, IF .NOT.DONE_A THEN THE NEXT REL_A IN ALREADY IN PLACE
		while (!done_A) {
			countA_Diff++;
			if (rel_A.typeId == isa) {
				countA_DiffISA++;
			}
			writePrevActNowRet(rel_A);
			if (itA.hasNext()) {
				rel_A = itA.next();
			} else {
				done_A = true;
				break;
			}
		}

		while (!done_B) {
			countB_Diff++;
			if (rel_B.typeId == isa) {
				countB_DiffISA++;
			}
			writeNewNoRec(rel_B);
			if (itB.hasNext()) {
				rel_B = itB.next();
			} else {
				done_B = true;
				break;
			}
		}

		s.append("\r\n::: Partial process statistics:");
		s.append("\r\n::: Reconciled relationships:  \t").append(countB_Total);
		s.append("\r\n::: Reconciled Isa's relationships:  \t").append(countSameISA);
		s.append("\r\n::: Previous relationships without match :   \t").append(countA_Diff);
//		s.append("\r\n::: Previous Isa's relationships without match:\t").append(countA_DiffISA);
		s.append("\r\n::: Current relationships without match:   \t").append(countB_Diff);
		s.append("\r\n::: Current Isa's relationships without match:\t").append(countB_DiffISA);
		s.append("\r\n::: ");
		long lapseTime = System.currentTimeMillis() - startTime;
		s.append("\r\n::: [Partial time] Sort/Compare Input & Output: \t").append(lapseTime);
		s.append("\t(mS)\t");
		s.append("\r\n");
		
		sumB_Total+=countB_Total;
		sumSameISA+=countSameISA;
		sumA_Diff+=countA_Diff;
//		sumA_DiffISA+=countA_DiffISA;
		sumB_Diff+=countB_Diff;
		sumB_DiffISA+=countB_DiffISA;
		
		return s.toString();
	}

	/**
	 * Write prev act now ret.
	 *
	 * @param rel_A the rel_ a
	 */
	private void writePrevActNowRet(Relationship rel_A) {
		ArrayList<Relationship> rels;
		if (prevActNowRet.containsKey(rel_A.sourceId)){
			rels=prevActNowRet.get(rel_A.sourceId);
		}else{
			rels=new ArrayList<Relationship>();
		}
		rels.add(rel_A);
		prevActNowRet.put(rel_A.sourceId,rels);
	}

	/**
	 * Write new no rec.
	 *
	 * @param rel_B the rel_ b
	 */
	private void writeNewNoRec( Relationship rel_B) {
		ArrayList<Relationship> rels;
		if (tmpNewNoRec.containsKey(rel_B.sourceId)){
			rels=tmpNewNoRec.get(rel_B.sourceId);
		}else{
			rels=new ArrayList<Relationship>();
		}
		rels.add(rel_B);
		tmpNewNoRec.put(rel_B.sourceId,rels);

	}


	/**
	 * Compare sno rel.
	 *
	 * @param inR the in r
	 * @param outR the out r
	 * @return the int
	 */
	private static int compareSnoRel(Relationship inR, Relationship outR) {
		if ((inR.sourceId == outR.sourceId) && (inR.group == outR.group) && (inR.typeId == outR.typeId)
				&& (inR.destinationId == outR.destinationId)) {
			return 1; // SAME
		} else if (inR.sourceId > outR.sourceId) {
			return 2; // ADDED
		} else if ((inR.sourceId == outR.sourceId) && (inR.group > outR.group)) {
			return 2; // ADDED
		} else if ((inR.sourceId == outR.sourceId) && (inR.group == outR.group)
				&& (inR.typeId > outR.typeId)) {
			return 2; // ADDED
		} else if ((inR.sourceId == outR.sourceId) && (inR.group == outR.group)
				&& (inR.typeId == outR.typeId) && (inR.destinationId > outR.destinationId)) {
			return 2; // ADDED
		} else {
			return 3; // DROPPED
		}
	} // compareSnoRel

	/**
	 * Gets the params.
	 *
	 * @return the params
	 * @throws ConfigurationException the configuration exception
	 */
	@SuppressWarnings("unchecked")
	private void getParams() throws ConfigurationException  {

		try {
			xmlConfig=new XMLConfiguration(config);
		} catch (ConfigurationException e) {
			logger.info("Reconciliation Runner - Error happened getting params file." + e.getMessage());
			throw e;
		}

		List<String> relFiles= xmlConfig
				.getList(I_Constants.CURRENT_INFERRED_RELATIONSHIP_FILES);
		currentRelationshipsFile=new String[relFiles.size()];
		relFiles.toArray(currentRelationshipsFile);

		List<String> prevRelFiles= xmlConfig
				.getList(I_Constants.PREVIOUS_INFERRED_RELATIONSHIP_FILES);
		if (prevRelFiles!=null && prevRelFiles.size()>0){
			previousInferredRelationshipsFile=new String[prevRelFiles.size()];
			prevRelFiles.toArray(previousInferredRelationshipsFile);
		}
		outputRelationships= xmlConfig.getString(I_Constants.RELATIONSHIP_FILE);
		logger.info("Reconciliation - Parameters:");
		logger.info("Current Relationship files : " );
		if (currentRelationshipsFile!=null){
			for (String relFile:currentRelationshipsFile){
				logger.info(relFile);
			}
		}
		logger.info("Previous Relationship files : " );
		if (previousInferredRelationshipsFile!=null){
			for (String relFile:previousInferredRelationshipsFile){
				logger.info(relFile);
			}
		}
		logger.info("Output Relationship file : " + outputRelationships );
	}
}
