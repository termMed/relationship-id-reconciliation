/**
 * Copyright (c) 2015 TermMed SA
 * Organization
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */
package com.termmed.reconciliation;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import com.termmed.reconciliation.utils.CommonUtils;
import com.termmed.reconciliation.utils.FileFilterAndSorter;
import com.termmed.reconciliation.utils.FileHelper;
import com.termmed.reconciliation.utils.FileSorter;
import com.termmed.reconciliation.utils.I_Constants;


/**
 * The Class ChangeConsolidator.
 *
 * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */
public class ChangeConsolidator {
	
	/** The relationship. */
	String relationship;
	
	/** The relationship previous. */
	String relationshipPrevious;
	
	/** The release date. */
	private String releaseDate;
	
	/** The snapshot final file. */
	private File snapshotFinalFile;
	
	/** The delta final file. */
	private File deltaFinalFile;
	
	/** The config. */
	private File config;
	
	/** The xml config. */
	private XMLConfiguration xmlConfig;
	
	/** The previous inferred relationships file. */
	private String[] previousInferredRelationshipsFile;
	
	/** The mergefolder tmp. */
	private File mergefolderTmp;
	
	/** The additional char type. */
	private String additionalCharType;
	
	/** The Constant log. */
	private static final Logger log = Logger.getLogger(ChangeConsolidator.class);
	
	/**
	 * The Enum FILE_TYPE.
	 */
	public static enum FILE_TYPE{

		/** The R f2_ concept. */
		RF2_CONCEPT(new int[]{0,1},new int[]{0},new Integer[]{2,3,4},"sct2_Concept_SUFFIX_INT",1),
		
		/** The R f2_ description. */
		RF2_DESCRIPTION(new int[]{0,1},new int[]{0},new Integer[]{2,3,4,5,6,7,8},"sct2_Description_SUFFIX-en_INT",1),
		
		/** The R f2_ relationship. */
		RF2_RELATIONSHIP(new int[]{0,1},new int[]{0},new Integer[]{2,3,4,5,6,7,8,9},"sct2_Relationship_SUFFIX_INT",1), 
		
		/** The R f2_ state d_ relationship. */
		RF2_STATED_RELATIONSHIP(new int[]{0,1},new int[]{0},new Integer[]{2,3,4,5,6,7,8,9},"sct2_StatedRelationship_SUFFIX_INT",1), 
		
		/** The R f2_ identifier. */
		RF2_IDENTIFIER(new int[]{1,2},new int[]{1},new Integer[]{3,4,5},"sct2_Identifier_SUFFIX_INT",2),
		
		/** The R f2_ compatibilit y_ identifier. */
		RF2_COMPATIBILITY_IDENTIFIER(new int[]{1,2},new int[]{1},new Integer[]{5},"res2_Identifier_SUFFIX_INT",2),
		
		/** The R f2_ textdefinition. */
		RF2_TEXTDEFINITION(new int[]{0,1},new int[]{0},new Integer[]{2,4,5,6,7,8},"sct2_TextDefinition_SUFFIX-en_INT",1),
		
		/** The R f2_ languag e_ refset. */
		RF2_LANGUAGE_REFSET(new int[]{0,1},new int[]{0},new Integer[]{2,4,5,6},"der2_cRefset_LanguageSUFFIX-en_INT",1), 
		
		/** The R f2_ attribut e_ value. */
		RF2_ATTRIBUTE_VALUE(new int[]{0,1},new int[]{0},new Integer[]{2,4,5,6},"der2_cRefset_AttributeValueSUFFIX_INT",1),
		
		/** The R f2_ simpl e_ map. */
		RF2_SIMPLE_MAP(new int[]{4,5,6,1},new int[]{4,5,6},new Integer[]{2},"der2_sRefset_SimpleMapSUFFIX_INT",1),
		
		/** The R f2_ simple. */
		RF2_SIMPLE(new int[]{4,5,1},new int[]{4,5},new Integer[]{2},"der2_Refset_SimpleSUFFIX_INT",1),
		
		/** The R f2_ association. */
		RF2_ASSOCIATION(new int[]{4,5,6,1},new int[]{4,5,6},new Integer[]{2},"der2_cRefset_AssociationReferenceSUFFIX_INT",1),
		
		/** The R f2_ qualifier. */
		RF2_QUALIFIER(new int[]{0,1},new int[]{0},new Integer[]{2,4,5,6,7,8,9},"sct2_Qualifier_SUFFIX_INT",1),
		
		/** The R f2_ ic d9_ map. */
		RF2_ICD9_MAP(new int[]{0,1},new int[]{0},new Integer[]{2,4,5,6,7,8,9,10,11},"der2_iissscRefset_ICD9CMEquivalenceMapSUFFIX_INT",1), 
		
		/** The R f2_ is a_ retired. */
		RF2_ISA_RETIRED(new int[]{0,1},new int[]{0},new Integer[]{2,3,4,5,6,7,8,9},"res2_RetiredIsaRelationship_SUFFIX_INT",1), 
		
		/** The R f2_ icd o_ targets. */
		RF2_ICDO_TARGETS(new int[]{6,1},new int[]{6},new Integer[]{2},"res2_CrossMapTargets_ICDO_INT",1), 
		
		/** The R f2_ state d_ is a_ retired. */
		RF2_STATED_ISA_RETIRED(new int[]{0,1},new int[]{0},new Integer[]{2,3,4,5,6,7,8,9},"res2_RetiredStatedIsaRelationship_SUFFIX_INT",1);
		

		/** The column indexes. */
		private int[] columnIndexes;
		
		/** The columns to compare. */
		private Integer[] columnsToCompare;
		
		/** The snapshot index. */
		private int[] snapshotIndex;
		
		/** The file name. */
		private String fileName;
		
		/** The effective time col index. */
		private int effectiveTimeColIndex;
		
		/**
		 * Gets the columns to compare.
		 *
		 * @return the columns to compare
		 */
		public Integer[] getColumnsToCompare() {
			return columnsToCompare;
		}

		/**
		 * Instantiates a new file type.
		 *
		 * @param columnIndexes the column indexes
		 * @param snapshotIndex the snapshot index
		 * @param columnsToCompare the columns to compare
		 * @param fileName the file name
		 * @param effectiveTimeColIndex the effective time col index
		 */
		FILE_TYPE(int[] columnIndexes,int[] snapshotIndex,Integer[] columnsToCompare,String fileName, int effectiveTimeColIndex){
			this.columnIndexes=columnIndexes;
			this.columnsToCompare=columnsToCompare;
			this.snapshotIndex=snapshotIndex;
			this.fileName=fileName;
			this.effectiveTimeColIndex=effectiveTimeColIndex;
		}

		/**
		 * Gets the column indexes.
		 *
		 * @return the column indexes
		 */
		public int[] getColumnIndexes() {
			return columnIndexes;
		}

		/**
		 * Gets the snapshot index.
		 *
		 * @return the snapshot index
		 */
		public int[] getSnapshotIndex() {
			return snapshotIndex;
		}

		/**
		 * Gets the file name.
		 *
		 * @return the file name
		 */
		public String getFileName() {
			return fileName;
		}
		
		/**
		 * Gets the effective time col index.
		 *
		 * @return the effective time col index
		 */
		public int getEffectiveTimeColIndex(){
			return effectiveTimeColIndex;
		}
	};

	/**
	 * Execute.
	 *
	 * @throws Exception the exception
	 */
	public void execute() throws Exception{
		File rels=new File(relationship);
		File relsPrev=new File (relationshipPrevious);

		File folderTmp=new File(rels.getParent() + "/temp" );
		if (!folderTmp.exists()){
			folderTmp.mkdir();
		}
		File sortedfolderTmp=new File(rels.getParent() + "/Sort");
		if (!sortedfolderTmp.exists()){
			sortedfolderTmp.mkdir();
		}
		File sortedPreviousfile=new File(sortedfolderTmp,"Sort" + relsPrev.getName());
		FileFilterAndSorter fsc=new FileFilterAndSorter(relsPrev, sortedPreviousfile, folderTmp,new int[]{0},new Integer[]{8},new String[]{"900000000000011006"});
		fsc.execute();
		fsc=null;
		System.gc();
		
		File sortedExportedfile=new File(sortedfolderTmp,"Sort_" + rels.getName());
		FileSorter fs=new FileSorter(rels, sortedExportedfile, folderTmp, new int[]{0});
		fs.execute();
		fs=null;
		System.gc();
		
		ConsolidateInfRelsSnapshotAndDelta  cis=new ConsolidateInfRelsSnapshotAndDelta(FILE_TYPE.RF2_RELATIONSHIP,sortedPreviousfile,sortedExportedfile,snapshotFinalFile,deltaFinalFile,releaseDate);
		cis.execute();
		cis=null;
		System.gc();
		
		if (additionalCharType!=null && !additionalCharType.equals("")){
			File additionalCharTypeFile=new File (additionalCharType);
			File sortedAddfile=new File(sortedfolderTmp,"Sort" + additionalCharTypeFile.getName());
			fsc=new FileFilterAndSorter(additionalCharTypeFile, sortedAddfile, folderTmp,new int[]{0},new Integer[]{8},new String[]{"900000000000227009"});
			fsc.execute();
			fsc=null;
			System.gc();
			HashSet<File> hFile=new HashSet<File>();
			
			hFile.add(sortedAddfile);
			hFile.add(snapshotFinalFile);
			CommonUtils.concatFile(hFile, snapshotFinalFile);
			
			File deltaAddFile=new File(sortedfolderTmp,"Filt" + additionalCharTypeFile.getName());
			fsc=new FileFilterAndSorter( sortedAddfile,deltaAddFile, folderTmp,new int[]{0},new Integer[]{1},new String[]{releaseDate});
			fsc.execute();
			fsc=null;
			System.gc();
			hFile=new HashSet<File>();
			
			hFile.add(deltaAddFile);
			hFile.add(deltaFinalFile);
			CommonUtils.concatFile(hFile, deltaFinalFile);
			
		}
		if (folderTmp.exists()){
			FileHelper.emptyFolder(folderTmp);
			folderTmp.delete();
		}
		if (sortedfolderTmp.exists()){
			FileHelper.emptyFolder(sortedfolderTmp);
			sortedfolderTmp.delete();
		}
		if (mergefolderTmp!=null && mergefolderTmp.exists()){
			FileHelper.emptyFolder(mergefolderTmp);
			mergefolderTmp.delete();
		}
	}

	/**
	 * Adds the additional char type rels.
	 */
	private void addAdditionalCharTypeRels() {
		
	}

	/**
	 * Instantiates a new change consolidator.
	 *
	 * @param relationship the relationship
	 * @param relationshipPrevious the relationship previous
	 * @param releaseDate the release date
	 * @param snapshotFinalFile the snapshot final file
	 * @param deltaFinalFile the delta final file
	 */
	public ChangeConsolidator(String relationship, String relationshipPrevious,
			String releaseDate, File snapshotFinalFile, File deltaFinalFile) {
		super();
		this.relationship = relationship;
		this.relationshipPrevious = relationshipPrevious;
		this.releaseDate = releaseDate;
		this.snapshotFinalFile = snapshotFinalFile;
		this.deltaFinalFile = deltaFinalFile;
	}

	/**
	 * Instantiates a new change consolidator.
	 *
	 * @param config the config
	 * @throws ConfigurationException the configuration exception
	 */
	public ChangeConsolidator(File config) throws ConfigurationException {

		this.config=config;

		getParams();	
		
		HashSet<File> hFile=new HashSet<File>();
		
		for (String sfile:previousInferredRelationshipsFile){
			File nfile=new File(sfile);
			hFile.add(nfile);
		}
		File rels=new File(relationship);

		mergefolderTmp=new File(rels.getParent() + "/Merge");
		if (!mergefolderTmp.exists()){
			mergefolderTmp.mkdir();
		}
		File outputfile=new File(mergefolderTmp,"MergedPrev_" + rels.getName());
		CommonUtils.concatFile(hFile, outputfile);
		relationshipPrevious=outputfile.getAbsolutePath();
	}
	
	/**
	 * Gets the params.
	 *
	 * @return the params
	 * @throws ConfigurationException the configuration exception
	 */
	private void getParams() throws ConfigurationException  {

		try {
			xmlConfig=new XMLConfiguration(config);
		} catch (ConfigurationException e) {
			log.info("ChangeConsolidator - Error happened getting params file." + e.getMessage());
			throw e;
		}

		relationship= xmlConfig.getString(I_Constants.RELATIONSHIP_FILE);
		String snapshotFinal= xmlConfig.getString(I_Constants.CONSOLIDATED_SNAPSHOT_FILE);
		snapshotFinalFile=new File(snapshotFinal);
		String deltaFinal= xmlConfig.getString(I_Constants.CONSOLIDATED_DELTA_FILE);
		deltaFinalFile=new File(deltaFinal);
		additionalCharType=xmlConfig.getString(I_Constants.ADDITIONAL_RELS_SNAPSHOT_FILE);
		List<String> prevRelFiles= xmlConfig
				.getList(I_Constants.PREVIOUS_INFERRED_RELATIONSHIP_FILES);
		if (prevRelFiles!=null && prevRelFiles.size()>0){
			previousInferredRelationshipsFile=new String[prevRelFiles.size()];
			prevRelFiles.toArray(previousInferredRelationshipsFile);
		}
		
		releaseDate= xmlConfig.getString(I_Constants.RELEASEDATE);
		log.info("Consolidator - Parameters:");
		log.info("Input file : " + relationship );

		log.info("Previous Relationship files : " );
		if (previousInferredRelationshipsFile!=null){
			for (String relFile:previousInferredRelationshipsFile){
				log.info(relFile);
			}
		}
		log.info("Snapshot final file : " + snapshotFinal );
		log.info("Delta final file : " + deltaFinal );
		log.info("Release date : " + releaseDate );
		
	}
}

