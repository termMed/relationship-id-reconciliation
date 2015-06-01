/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.reconciliation;

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
import org.ihtsdo.reconciliation.model.Relationship;
import org.ihtsdo.reconciliation.model.RelationshipGroup;
import org.ihtsdo.reconciliation.model.RelationshipGroupList;
import org.ihtsdo.reconciliation.utils.I_Constants;


/**
 * The Class RelationshipReconciliation.
 *
 * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */
public class RelationshipReconciliation {

	public static void main(String[] args){

		logger = Logger.getLogger("org.ihtsdo.reconciliation.RelationshipReconciliation");
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
	private File tempRelationshipStore;

	private File config;
	private String outputRelationships;

	/**
	 * Instantiates a new classification runner.
	 *
	 * @param module the module
	 * @param previousInferredRelationships the prev inferred rels
	 * @param currentInferredRelationships the output rels
	 * @param outputRelationships the output relationship file
	 */
	public RelationshipReconciliation(
			String[] previousInferredRelationships, String[] currentInferredRelationships,
			String outputRelationships) {
		super();

		logger = Logger.getLogger("org.ihtsdo.reconciliation.RelationshipReconciliation");

		this.currentRelationshipsFile = currentInferredRelationships;
		this.previousInferredRelationshipsFile = previousInferredRelationships;

		this.outputRelationships=outputRelationships;
	}

	public RelationshipReconciliation(File config) throws ConfigurationException {
		this.config=config;

		logger = Logger.getLogger("org.ihtsdo.reconciliation.RelationshipReconciliation");

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

	private  String[] currentRelationshipsFile;

	private XMLConfiguration xmlConfig;

	private HashMap<Long,ArrayList<Relationship>> newNoRec;

	private HashMap<Long,ArrayList<Relationship>> prevActNowRet;

	private HashMap<Long,TreeMap<String,Relationship>> prevInact;

	private BufferedWriter bw;

	private HashMap<Long, ArrayList<Relationship>> tmpNewNoRec;


	/**
	 * Execute the reconciliation.
	 */
	public void execute(){

		try {

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
				writeNoReconciliatedRel();
			}
			bw.close();
			bw=null;
			osw=null;
			fos=null;
			logger.info("\r\n::: *** WROTE *** LAPSED TIME =\t" + toStringLapseSec(startTime) + "\t ***");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String comparePrevInact(int step) throws IOException {
		// STATISTICS COUNTERS
		int countConSeen = 0;
		int countSame = 0;
		int countSameISA = 0;
		int countA_Diff = 0;
		int countA_DiffISA = 0;
		int countA_Total = 0;
		int countB_Diff = 0;
		int countB_DiffISA = 0;
		int countB_Total = 0;

		long startTime = System.currentTimeMillis();

		boolean reconciliated=false;
		for (Long conceptId:newNoRec.keySet()){
			if (++countConSeen % 25000 == 0) {
				logger.info("::: [comparePrevInact] @ #\t" + countConSeen);
			}
			TreeMap<String, Relationship> relsPrev = prevInact.get(conceptId);
			ArrayList<Relationship> relsCurr = newNoRec.get(conceptId);
			
			if (relsPrev!=null){
				for (Relationship relC:relsCurr){
					reconciliated=false;
					for (String key:relsPrev.descendingKeySet()){
						Relationship relP=relsPrev.get(key);
						if (compareRelsStep(relC, relP,step)) {
							writeReconciliated(bw,relC,relP);
							countA_Total++;
							countB_Total++;
							countSame++;
							reconciliated=true;
							relsPrev.remove(key);
							break;
						}
					}
					if(!reconciliated){
						countB_Diff++;
						countB_Total++;
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
					countB_Total++;
					if (relC.typeId == isa) {
						countB_DiffISA++;
					}
					writeNewNoRec(relC);

				}
			}

		}

		StringBuilder s = new StringBuilder();
		s.append("\r\n::: [comparePreviousInactives()]");
		long lapseTime = System.currentTimeMillis() - startTime;
		s.append("\r\n::: [Time] Sort/Compare Input & Output: \t").append(lapseTime);
		s.append("\t(mS)\t").append(((float) lapseTime / 1000) / 60).append("\t(min)");
		s.append("\r\n");
		s.append("\r\n::: ");
		s.append("\r\n::: countSame:     \t").append(countSame);
		s.append("\r\n::: countSameISA:  \t").append(countSameISA);
		s.append("\r\n::: A == Classifier Output Path");
		s.append("\r\n::: countA_Diff:   \t").append(countA_Diff);
		s.append("\r\n::: countA_DiffISA:\t").append(countA_DiffISA);
		s.append("\r\n::: countA_Total:  \t").append(countA_Total);
		s.append("\r\n::: B == Classifier Solution Set");
		s.append("\r\n::: countB_Diff:   \t").append(countB_Diff);
		s.append("\r\n::: countB_DiffISA:\t").append(countB_DiffISA);
		s.append("\r\n::: countB_Total:  \t").append(countB_Total);
		s.append("\r\n::: ");

		return s.toString();
	}

	private String compareActivesWOMatch(  int step) throws IOException {
		// STATISTICS COUNTERS
		int countConSeen = 0;
		int countSame = 0;
		int countSameISA = 0;
		int countA_Diff = 0;
		int countA_DiffISA = 0;
		int countA_Total = 0;
		int countB_Diff = 0;
		int countB_DiffISA = 0;
		int countB_Total = 0;

		long startTime = System.currentTimeMillis();


		logger.info("\r\n::: [compareActivesWOMatch]"
				+ "\r\n::: previous active concept size = \t" + prevActNowRet.size()
				+ "\r\n::: current active concept size = \t" + newNoRec.size());


		boolean reconciliated=false;
		for (Long conceptId:newNoRec.keySet()){
			if (++countConSeen % 25000 == 0) {
				logger.info("::: [compareActivesWOMatch] @ #\t" + countConSeen);
			}
			ArrayList<Relationship> relsPrev = prevActNowRet.get(conceptId);
			ArrayList<Relationship> relsCurr = newNoRec.get(conceptId);
			if (relsPrev!=null){
				for (Relationship relC:relsCurr){
					reconciliated=false;
					for (Relationship relP:relsPrev){
						if (compareRelsStep(relC, relP,step)) {
							writeReconciliated(bw,relC,relP);
							countA_Total++;
							countB_Total++;
							countSame++;
							reconciliated=true;
							relsPrev.remove(relP);
							break;
						}
					}
					if(!reconciliated){
						countB_Diff++;
						countB_Total++;
						if (relC.typeId == isa) {
							countB_DiffISA++;
						}
						writeNewNoRec(relC);
					}
				}
				prevActNowRet.put(conceptId, relsPrev);
			}else{
				for (Relationship relC:relsCurr){
					countB_Diff++;
					countB_Total++;
					if (relC.typeId == isa) {
						countB_DiffISA++;
					}
					writeNewNoRec(relC);
				}
			}

		}

		StringBuilder s = new StringBuilder();
		s.append("\r\n::: [compareActivesWOMatch()]");
		long lapseTime = System.currentTimeMillis() - startTime;
		s.append("\r\n::: [Time] Sort/Compare Input & Output: \t").append(lapseTime);
		s.append("\t(mS)\t").append(((float) lapseTime / 1000) / 60).append("\t(min)");
		s.append("\r\n");
		s.append("\r\n::: ");
		s.append("\r\n::: countSame:     \t").append(countSame);
		s.append("\r\n::: countSameISA:  \t").append(countSameISA);
		s.append("\r\n::: A == Classifier Output Path");
		s.append("\r\n::: countA_Diff:   \t").append(countA_Diff);
		s.append("\r\n::: countA_DiffISA:\t").append(countA_DiffISA);
		s.append("\r\n::: countA_Total:  \t").append(countA_Total);
		s.append("\r\n::: B == Classifier Solution Set");
		s.append("\r\n::: countB_Diff:   \t").append(countB_Diff);
		s.append("\r\n::: countB_DiffISA:\t").append(countB_DiffISA);
		s.append("\r\n::: countB_Total:  \t").append(countB_Total);
		s.append("\r\n::: ");

		return s.toString();
	}

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

	private  void writeNoReconciliatedRel( )
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

	private  void writeReconciliated(BufferedWriter bw,Relationship infRel, Relationship prevRel)
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
	 * @throws java.io.IOException Signals that an I/O exception has occurred.
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
	 * @throws java.io.IOException Signals that an I/O exception has occurred.
	 */
	private  String compareActivesAndWriteBack(List<Relationship> snorelA, List<Relationship> snorelB)
			throws  IOException {

		// STATISTICS COUNTERS
		int countConSeen = 0;
		int countSame = 0;
		int countSameISA = 0;
		int countA_Diff = 0;
		int countA_DiffISA = 0;
		int countA_Total = 0;
		int countB_Diff = 0;
		int countB_DiffISA = 0;
		int countB_Total = 0;

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

		logger.info("\r\n::: [compareActivesAndWriteBack]"
				+ "\r\n::: snorelA.size() = \t" + snorelA.size()
				+ "\r\n::: snorelB.size() = \t" + snorelB.size());

		// BY SORT ORDER, LOWER NUMBER ADVANCES FIRST
		while (!done_A && !done_B) {
			if (++countConSeen % 25000 == 0) {
				logger.info("::: [compareActivesAndWriteBack] @ #\t" + countConSeen);
			}

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
						writeReconciliated(bw,rel_B,rel_A);
						countA_Total++;
						countB_Total++;
						countSame++;
						// NOTHING TO WRITE IN THIS CASE
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
						// WRITEBACK REL_B (Classifier Results) AS CURRENT
						countB_Diff++;
						countB_Total++;
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
						// WRITEBACK REL_A (Classifier Input) AS RETIRED
						// GATHER STATISTICS
						countA_Diff++;
						countA_Total++;
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
					countA_Total++;
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
					countB_Total++;
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
				// WRITE THESE GROUPED RELS AS "RETIRED"
				RelationshipGroupList groupList_NotEqual;
				if (groupList_A.size() > 0) {
					groupList_NotEqual = groupList_A.whichNotEqual(groupList_B);
					for (RelationshipGroup sg : groupList_NotEqual) {
						for (Relationship sr_A : sg) {
							writePrevActNowRet(sr_A);
						}
					}
					countA_Total += groupList_A.countRels();
					countA_Diff += groupList_NotEqual.countRels();
				}

				// FIND GROUPS IN GROUPLIST_B WITHOUT AN EQUAL IN GROUPLIST_A
				// WRITE THESE GROUPED RELS AS "NEW, CURRENT"
				//				int rgNum = 0; // USED TO DETERMINE "AVAILABLE" ROLE GROUP NUMBERS
				if (groupList_B.size() > 0) {
					groupList_NotEqual = groupList_B.whichNotEqual(groupList_A);
					for (RelationshipGroup sg : groupList_NotEqual) {
						if (sg.get(0).group != 0) {
							//							rgNum = nextRoleGroupNumber(groupList_A, rgNum);
							for (Relationship sr_B : sg) {
								//								sr_B.group = rgNum;
								writeNewNoRec(sr_B);
							}
						} else {
							for (Relationship sr_B : sg) {
								writeNewNoRec(sr_B);
							}
						}
					}
					countB_Total += groupList_A.countRels();
					countB_Diff += groupList_NotEqual.countRels();
				}
				if (groupList_A.size() > 0 && groupList_B.size() > 0) {
					Map	<Relationship,Relationship> relsMap;
					relsMap = groupList_B.getEqualRelationshipInGroup(groupList_A);
					for (Relationship sr_B : relsMap.keySet()) {
						writeReconciliated(bw,sr_B,relsMap.get(sr_B));
					}
					countA_Total++;
					countB_Total++;
					countSame++;
				}
			} else if (rel_A.sourceId > rel_B.sourceId) {
				// CASE 2: LIST_B HAS CONCEPT NOT IN LIST_A
				// COMPLETELY *ADD* ALL THIS C1 FOR REL_B AS NEW, CURRENT
				long thisC1 = rel_B.sourceId;
				while (rel_B.sourceId == thisC1) {
					countB_Diff++;
					countB_Total++;
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
				// COMPLETELY *RETIRE* ALL THIS C1 FOR REL_A
				long thisC1 = rel_A.sourceId;
				while (rel_A.sourceId == thisC1) {
					countA_Diff++;
					countA_Total++;
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
		// THEREFORE THAT REMAINDER WRITEBACK COMPLETELY
		// AS "NEW CURRENT" OR "OLD RETIRED"
		//
		// LASTLY, IF .NOT.DONE_A THEN THE NEXT REL_A IN ALREADY IN PLACE
		while (!done_A) {
			countA_Diff++;
			countA_Total++;
			if (rel_A.typeId == isa) {
				countA_DiffISA++;
			}
			// COMPLETELY UPDATE ALL REMAINING REL_A AS RETIRED
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
			countB_Total++;
			if (rel_B.typeId == isa) {
				countB_DiffISA++;
			}
			// COMPLETELY UPDATE ALL REMAINING REL_B AS NEW, CURRENT
			writeNewNoRec(rel_B);
			if (itB.hasNext()) {
				rel_B = itB.next();
			} else {
				done_B = true;
				break;
			}
		}


		StringBuilder s = new StringBuilder();
		s.append("\r\n::: [compareActivesAndWriteBack()]");
		long lapseTime = System.currentTimeMillis() - startTime;
		s.append("\r\n::: [Time] Sort/Compare Input & Output: \t").append(lapseTime);
		s.append("\t(mS)\t").append(((float) lapseTime / 1000) / 60).append("\t(min)");
		s.append("\r\n");
		s.append("\r\n::: ");
		s.append("\r\n::: countSame:     \t").append(countSame);
		s.append("\r\n::: countSameISA:  \t").append(countSameISA);
		s.append("\r\n::: A == Classifier Output Path");
		s.append("\r\n::: countA_Diff:   \t").append(countA_Diff);
		s.append("\r\n::: countA_DiffISA:\t").append(countA_DiffISA);
		s.append("\r\n::: countA_Total:  \t").append(countA_Total);
		s.append("\r\n::: B == Classifier Solution Set");
		s.append("\r\n::: countB_Diff:   \t").append(countB_Diff);
		s.append("\r\n::: countB_DiffISA:\t").append(countB_DiffISA);
		s.append("\r\n::: countB_Total:  \t").append(countB_Total);
		s.append("\r\n::: ");

		return s.toString();
	}

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
	 * Next role group number.
	 *
	 * @param sgl the sgl
	 * @param gnum the gnum
	 * @return the int
	 */
	private static int nextRoleGroupNumber(RelationshipGroupList sgl, int gnum) {

		int testNum = gnum + 1;
		int sglSize = sgl.size();
		int trial = 0;
		while (trial <= sglSize) {

			boolean exists = false;
			for (int i = 0; i < sglSize; i++) {
				if (sgl.get(i).get(0).group == testNum) {
					exists = true;
				}
			}

			if (exists == false) {
				return testNum;
			} else {
				testNum++;
				trial++;
			}
		}

		return testNum;
	}

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