/**
 * Copyright (c) 2015 TermMed SA
 * Organization
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */
package com.termmed.reconciliation.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import com.termmed.reconciliation.RelationshipReconciliation;
import com.termmed.reconciliation.model.Relationship;
import com.termmed.reconciliation.utils.I_Constants;


/**
 * The class IdReconciliationTest.
 *
 * @author Alejandro Rodriguez.
 * @version 1.0
 */
public class IdReconciliationTest extends TestCase {

	/**
	 * Test id reconciliation.
	 *
	 * @throws Exception the exception
	 */
	public void testIdReconciliation() throws Exception{
		File TestGroupMumberReconciliationFile=new File("src/test/resources/com/termmed/reconciliation/test/IdReconciliationConfig.xml");

		if (!TestGroupMumberReconciliationFile.exists()){
			throw new Exception("Config file for testGroupMumberReconciliation doesn't exist.");
		} 
		RelationshipReconciliation cc=new RelationshipReconciliation(TestGroupMumberReconciliationFile);
		cc.execute();
		String outputFile=getOutputFile(TestGroupMumberReconciliationFile);
		
		ArrayList<Relationship> outputRelationships=new ArrayList<Relationship>();
		String[] outputFiles=new String[]{outputFile};
		loadOutputInferredRelationship(outputFiles,outputRelationships,1);
		
		String[] previousFiles=getPreviousFile(TestGroupMumberReconciliationFile);
		ArrayList<Relationship> previousRelationships=new ArrayList<Relationship>();
		loadOutputInferredRelationship(previousFiles,previousRelationships,1);
		
		for(Relationship prevRelationship:previousRelationships){
			Relationship outputRelationship=getSameReconciledRel(prevRelationship, outputRelationships);
			if (outputRelationship!=null){
				assertTrue(outputRelationship.sourceId==prevRelationship.sourceId &&
						outputRelationship.destinationId==prevRelationship.destinationId &&
						outputRelationship.typeId==prevRelationship.typeId);
			}else{
				assertFalse(existsSameTripleWithoutId(prevRelationship,outputRelationships));
			}
		}

	}
	
	/**
	 * Exists same triple without id.
	 *
	 * @param prevRelationship the prev relationship
	 * @param outputRelationships the output relationships
	 * @return true, if successful
	 */
	private boolean existsSameTripleWithoutId(Relationship prevRelationship, ArrayList<Relationship> outputRelationships) {

		for (Relationship outputRelationship:outputRelationships){
			if (outputRelationship.sourceId==prevRelationship.sourceId &&
					outputRelationship.destinationId==prevRelationship.destinationId &&
					outputRelationship.typeId==prevRelationship.typeId &&
					outputRelationship.getRelId()=="null"){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets the same reconciled rel.
	 *
	 * @param relationship the relationship
	 * @param outputRelationships the output relationships
	 * @return the same reconciled rel
	 */
	private Relationship getSameReconciledRel(Relationship relationship,
			ArrayList<Relationship> outputRelationships) {
		
		for (Relationship outputRelationship:outputRelationships){
			if (outputRelationship.getRelId().equals(relationship.getRelId())){
				return outputRelationship;
			}
		}
		return null;
	}
	
	/**
	 * Load output inferred relationship.
	 *
	 * @param relationshipFiles the relationship files
	 * @param rels the rels
	 * @param i the i
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public  void loadOutputInferredRelationship( String[] relationshipFiles,ArrayList<Relationship> rels, int i)throws IOException {

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
	 * Gets the previous file.
	 *
	 * @param configFile the config file
	 * @return the previous file
	 * @throws ConfigurationException the configuration exception
	 */
	private String[] getPreviousFile(File configFile) throws ConfigurationException {
		XMLConfiguration xmlConfig;
		String[] previousInferredRelationshipsFile=null;
		xmlConfig=new XMLConfiguration(configFile);
		List<String> prevRelFiles= xmlConfig
				.getList(I_Constants.PREVIOUS_INFERRED_RELATIONSHIP_FILES);
		if (prevRelFiles!=null && prevRelFiles.size()>0){
			previousInferredRelationshipsFile = new String[prevRelFiles.size()];
			prevRelFiles.toArray(previousInferredRelationshipsFile);
		}
		return previousInferredRelationshipsFile;
	}

	/**
	 * Gets the output file.
	 *
	 * @param configFile the config file
	 * @return the output file
	 * @throws ConfigurationException the configuration exception
	 */
	private String getOutputFile(File configFile) throws ConfigurationException {
		XMLConfiguration xmlConfig;
		
		xmlConfig=new XMLConfiguration(configFile);

		return xmlConfig.getString(I_Constants.RELATIONSHIP_FILE);
		
	}

}
