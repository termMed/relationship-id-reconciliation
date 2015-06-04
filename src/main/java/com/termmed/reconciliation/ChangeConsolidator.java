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

public class ChangeConsolidator {
	
	String relationship;
	String relationshipPrevious;
	private String releaseDate;
	private File snapshotFinalFile;
	private File deltaFinalFile;
	private File config;
	private XMLConfiguration xmlConfig;
	private String[] previousInferredRelationshipsFile;
	private File mergefolderTmp;
	private String additionalCharType;
	private static final Logger log = Logger.getLogger(ChangeConsolidator.class);
	
	public static enum FILE_TYPE{

		RF2_CONCEPT(new int[]{0,1},new int[]{0},new Integer[]{2,3,4},"sct2_Concept_SUFFIX_INT",1),
		RF2_DESCRIPTION(new int[]{0,1},new int[]{0},new Integer[]{2,3,4,5,6,7,8},"sct2_Description_SUFFIX-en_INT",1),
		RF2_RELATIONSHIP(new int[]{0,1},new int[]{0},new Integer[]{2,3,4,5,6,7,8,9},"sct2_Relationship_SUFFIX_INT",1), 
		RF2_STATED_RELATIONSHIP(new int[]{0,1},new int[]{0},new Integer[]{2,3,4,5,6,7,8,9},"sct2_StatedRelationship_SUFFIX_INT",1), 
		RF2_IDENTIFIER(new int[]{1,2},new int[]{1},new Integer[]{3,4,5},"sct2_Identifier_SUFFIX_INT",2),
		RF2_COMPATIBILITY_IDENTIFIER(new int[]{1,2},new int[]{1},new Integer[]{5},"res2_Identifier_SUFFIX_INT",2),
		RF2_TEXTDEFINITION(new int[]{0,1},new int[]{0},new Integer[]{2,4,5,6,7,8},"sct2_TextDefinition_SUFFIX-en_INT",1),
		RF2_LANGUAGE_REFSET(new int[]{0,1},new int[]{0},new Integer[]{2,4,5,6},"der2_cRefset_LanguageSUFFIX-en_INT",1), 
		RF2_ATTRIBUTE_VALUE(new int[]{0,1},new int[]{0},new Integer[]{2,4,5,6},"der2_cRefset_AttributeValueSUFFIX_INT",1),
		RF2_SIMPLE_MAP(new int[]{4,5,6,1},new int[]{4,5,6},new Integer[]{2},"der2_sRefset_SimpleMapSUFFIX_INT",1),
		RF2_SIMPLE(new int[]{4,5,1},new int[]{4,5},new Integer[]{2},"der2_Refset_SimpleSUFFIX_INT",1),
		RF2_ASSOCIATION(new int[]{4,5,6,1},new int[]{4,5,6},new Integer[]{2},"der2_cRefset_AssociationReferenceSUFFIX_INT",1),
		RF2_QUALIFIER(new int[]{0,1},new int[]{0},new Integer[]{2,4,5,6,7,8,9},"sct2_Qualifier_SUFFIX_INT",1),
		RF2_ICD9_MAP(new int[]{0,1},new int[]{0},new Integer[]{2,4,5,6,7,8,9,10,11},"der2_iissscRefset_ICD9CMEquivalenceMapSUFFIX_INT",1), 
		RF2_ISA_RETIRED(new int[]{0,1},new int[]{0},new Integer[]{2,3,4,5,6,7,8,9},"res2_RetiredIsaRelationship_SUFFIX_INT",1), 
		RF2_ICDO_TARGETS(new int[]{6,1},new int[]{6},new Integer[]{2},"res2_CrossMapTargets_ICDO_INT",1), 
		RF2_STATED_ISA_RETIRED(new int[]{0,1},new int[]{0},new Integer[]{2,3,4,5,6,7,8,9},"res2_RetiredStatedIsaRelationship_SUFFIX_INT",1);
		

		private int[] columnIndexes;
		private Integer[] columnsToCompare;
		private int[] snapshotIndex;
		private String fileName;
		private int effectiveTimeColIndex;
		
		public Integer[] getColumnsToCompare() {
			return columnsToCompare;
		}

		FILE_TYPE(int[] columnIndexes,int[] snapshotIndex,Integer[] columnsToCompare,String fileName, int effectiveTimeColIndex){
			this.columnIndexes=columnIndexes;
			this.columnsToCompare=columnsToCompare;
			this.snapshotIndex=snapshotIndex;
			this.fileName=fileName;
			this.effectiveTimeColIndex=effectiveTimeColIndex;
		}

		public int[] getColumnIndexes() {
			return columnIndexes;
		}

		public int[] getSnapshotIndex() {
			return snapshotIndex;
		}

		public String getFileName() {
			return fileName;
		}
		public int getEffectiveTimeColIndex(){
			return effectiveTimeColIndex;
		}
	};

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

	private void addAdditionalCharTypeRels() {
		
	}

	public ChangeConsolidator(String relationship, String relationshipPrevious,
			String releaseDate, File snapshotFinalFile, File deltaFinalFile) {
		super();
		this.relationship = relationship;
		this.relationshipPrevious = relationshipPrevious;
		this.releaseDate = releaseDate;
		this.snapshotFinalFile = snapshotFinalFile;
		this.deltaFinalFile = deltaFinalFile;
	}

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

