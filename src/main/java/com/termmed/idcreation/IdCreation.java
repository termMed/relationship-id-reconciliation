/**
 * Copyright (c) 2015 TermMed SA
 * Organization
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */
package com.termmed.idcreation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import com.termmed.reconciliation.utils.GenIdHelper;
import com.termmed.reconciliation.utils.I_Constants;


/**
 * The Class IdCreation.
 * 
 * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */
public class IdCreation {
	
	/** The component file. */
	String componentFile;
	
	/** The namespace id. */
	int namespaceId;
	
	/** The partition id. */
	long partitionId;
	
	/** The end point url. */
	String endPointURL;
	
	/** The username. */
	String username;
	
	/** The pass. */
	String pass;
	
	/** The release date. */
	private String releaseDate;
	
	/** The xml config. */
	private XMLConfiguration xmlConfig;
	
	/** The config. */
	private File config;
	
	/** The Constant log. */
	private static final Logger log = Logger.getLogger(IdCreation.class);
	
	/**
	 * Execute.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void execute() throws IOException{

		IdAssignmentBI idAssignment = new IdAssignmentImpl(endPointURL,username,pass);

		File relFile=new File(componentFile);
		File newRelFile=new File(relFile.getParent(),"uuids_" + relFile.getName());
		FileInputStream rfis = new FileInputStream(componentFile);
		InputStreamReader risr = new InputStreamReader(rfis,"UTF-8");
		BufferedReader rbr = new BufferedReader(risr);
		String header=rbr.readLine();


		FileOutputStream fos = new FileOutputStream( newRelFile);
		OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
		BufferedWriter bw = new BufferedWriter(osw);
		bw.append(header);
		bw.append("\r\n");

		String line;
		String[] spl;
		int i;
		UUID uuid;
		List<UUID> list = new ArrayList<UUID>();
		while((line=rbr.readLine())!=null){
			spl=line.split("\t",-1);
			Long sctid=null;
			try{
				sctid=Long.parseLong(spl[0]);
				bw.append(line);
				bw.append("\r\n");
			}catch (Exception e){
				try{
					uuid=UUID.fromString(spl[0]);
				}catch(Exception e2){
					uuid=UUID.randomUUID();
				}

				list.add(uuid);
				bw.append(uuid.toString());
				bw.append("\t");
				for (i=1;i<spl.length;i++){
					bw.append(spl[i]);
					if (i==spl.length-1){
						bw.append("\r\n");
					}else{
						bw.append("\t");
					}
				}
			}
		}
		bw.close();
		bw=null;
		rbr.close();
		rbr=null;
		HashMap<UUID, Long> sctIdMap = new HashMap<UUID,Long>();
		String sPart=("0" + String.valueOf(partitionId)).substring(0, 2);
		try {
			sctIdMap = idAssignment.createSCTIDList(list, namespaceId, sPart, releaseDate, releaseDate, releaseDate);
		} catch (Exception cE) {
			log.error("Message : SCTID creation error for list " , cE);
		}
		File finalRelFile=new File(relFile.getParent(),"tmp_" + relFile.getName());
		rfis = new FileInputStream(newRelFile);
		risr = new InputStreamReader(rfis,"UTF-8");
		rbr = new BufferedReader(risr);
		header=rbr.readLine();


		fos = new FileOutputStream( finalRelFile);
		osw = new OutputStreamWriter(fos,"UTF-8");
		bw = new BufferedWriter(osw);
		bw.append(header);
		bw.append("\r\n");

		while((line=rbr.readLine())!=null){
			spl=line.split("\t",-1);
			if (spl[0].contains("-")){
				uuid=UUID.fromString(spl[0]);
				Long id=sctIdMap.get(uuid);
				if (id!=null){
					bw.append(id.toString());
					bw.append("\t");
					for (i=1;i<spl.length;i++){
						bw.append(spl[i]);
						if (i==spl.length-1){
							bw.append("\r\n");
						}else{
							bw.append("\t");
						}
					}
				}else{
					bw.append(line);
					bw.append("\r\n");

				}
			}else{
				bw.append(line);
				bw.append("\r\n");

			}
		}
		bw.close();
		bw=null;
		rbr.close();
		rbr=null;
		
		if (newRelFile.exists()){
			newRelFile.delete();
		}
		if (relFile.exists()){
			String inputFile=relFile.getAbsolutePath();
			File reconFile=new File(relFile.getParent(),"Reconcil_" + relFile.getName());
			relFile.renameTo(reconFile);
			File outFile= new File(inputFile);
			finalRelFile.renameTo(outFile);
			
		}
		
	}


	/**
	 * Instantiates a new id creation.
	 *
	 * @param componentFile the component file
	 * @param namespaceId the namespace id
	 * @param partitionId the partition id
	 * @param endPointURL the end point url
	 * @param username the username
	 * @param pass the pass
	 * @param releaseDate the release date
	 */
	public IdCreation(String componentFile, int namespaceId,
			long partitionId, String endPointURL, String username, String pass,
			String releaseDate) {
		super();
		this.componentFile = componentFile;
		this.namespaceId = namespaceId;
		this.partitionId = partitionId;
		this.endPointURL = endPointURL;
		this.username = username;
		this.pass = pass;
		this.releaseDate = releaseDate;
	}


	/**
	 * Instantiates a new id creation.
	 *
	 * @param config the config
	 * @throws ConfigurationException the configuration exception
	 */
	public IdCreation(File config) throws ConfigurationException {

		this.config=config;

		getParams();	
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
			log.info("IdCreation - Error happened getting params file." + e.getMessage());
			throw e;
		}

		componentFile= xmlConfig.getString(I_Constants.RELATIONSHIP_FILE);
		String nSpace= xmlConfig.getString(I_Constants.NAMESPACE);
		if (nSpace!=null){
			namespaceId=Integer.parseInt(nSpace);
		}
		String partId= xmlConfig.getString(I_Constants.PARTITION);
		if(partId!=null){
			partitionId=Long.parseLong(partId);
		}
		endPointURL= xmlConfig.getString(I_Constants.ENDPOINTURL);
		username= xmlConfig.getString(I_Constants.USERNAME);
		pass= xmlConfig.getString(I_Constants.PASSWORD);
		releaseDate= xmlConfig.getString(I_Constants.RELEASEDATE);
		log.info("Id Creation - Parameters:");
		log.info("Input file : " + componentFile );
		log.info("NamespaceId : " + namespaceId );
		log.info("PartitionId : " + partitionId );
		log.info("End point url : " + endPointURL );
		log.info("Username : " + username );
		log.info("Release date : " + releaseDate );
		
	}
	
	/**
	 * Gets the cont base.
	 *
	 * @return the cont base
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private Long getContBase() throws IOException {
		Long contBase=null;
		FileInputStream rfis = new FileInputStream(componentFile);
		InputStreamReader risr = new InputStreamReader(rfis,"UTF-8");
		BufferedReader rbr = new BufferedReader(risr);
		rbr.readLine();
		String sPart=String.valueOf(partitionId);
		String sName=String.valueOf(namespaceId);
		String line;
		String[] spl;
		while((line=rbr.readLine())!=null){
			spl=line.split("\t",-1);
			if (spl[1].equals(sPart) && spl[1].equals(sName)){
				contBase=Long.parseLong( spl[2]);
			}

		}
		rbr.close();
		rbr=null;
		return contBase;
	}
	
	/**
	 * Gets the id.
	 *
	 * @param contBase the cont base
	 * @return the id
	 */
	private Long getId(long contBase){
		long num=contBase;
		long multip = 100;
		if (namespaceId > 0) {
			log.debug("Extension number > 0");
			multip = 1000000000;
		}
		num = (num * multip) + ((long)(namespaceId * 100)) + (partitionId);
		log.debug("NUMBER: " + num);
		long result = GenIdHelper.verhoeffCompute(String.valueOf(num));
		Long newSctId = num * 10 + result;
		log.debug("verhoeff Compute: " + newSctId);
		return newSctId; 

	}
}
