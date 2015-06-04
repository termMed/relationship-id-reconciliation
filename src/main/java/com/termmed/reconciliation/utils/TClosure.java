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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;


/**
 * The class TClosure.
 *
 * @author Alejandro Rodriguez.
 * @version 1.0
 */

public class TClosure {
	
	/** The parent hier. */
	HashMap<Long,HashSet<Long>>parentHier;
	
	/** The children hier. */
	HashMap<Long,HashSet<Long>>childrenHier;
	
	/** The isarelationshiptypeid. */
	private long ISARELATIONSHIPTYPEID=116680003l;
	
	/** The root concept. */
	private String ROOT_CONCEPT = "138875005";
	
	/** The rf2 rels. */
	String rf2Rels;
	
	/**
	 * Instantiates a new t closure.
	 *
	 * @param rf2Rels the rf2 rels
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public TClosure(String rf2Rels) throws FileNotFoundException, IOException{
		parentHier=new HashMap<Long,HashSet<Long>>();
		childrenHier=new HashMap<Long,HashSet<Long>>();
		this.rf2Rels=rf2Rels;
		loadIsas();
	}
	
	/**
	 * Load isas.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws FileNotFoundException the file not found exception
	 */
	private void loadIsas() throws IOException, FileNotFoundException {
		System.out.println("Starting Isas Relationships from: " + rf2Rels);
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(rf2Rels), "UTF8"));
		try {
			String line = br.readLine();
			line = br.readLine(); // Skip header
			int count = 0;
			while (line != null) {
				if (line.isEmpty()) {
					continue;
				}
				String[] columns = line.split("\\t");
				if (Long.parseLong(columns[7])==ISARELATIONSHIPTYPEID 
						&& columns[2].equals("1") 
						&& !columns[4].equals(ROOT_CONCEPT)){
					addRel(Long.parseLong(columns[5]),Long.parseLong(columns[4]));
					
					count++;
					if (count % 100000 == 0) {
						System.out.print(".");
					}
				}
				line = br.readLine();
			}
			System.out.println(".");
			System.out.println("Parent isas Relationships loaded = " + parentHier.size());
			System.out.println("Children isas Relationships loaded = " + childrenHier.size());
		} finally {
			br.close();
		}		
	}
	
	/**
	 * Adds the rel.
	 *
	 * @param parent the parent
	 * @param child the child
	 */
	public void addRel(Long parent, Long child){
		HashSet<Long> parentList=parentHier.get(child);
		if (parentList==null){
			parentList=new HashSet<Long>();
		}
		parentList.add(parent);
		parentHier.put(child, parentList);
		
		HashSet<Long> childrenList=childrenHier.get(parent);
		if (childrenList==null){
			childrenList=new HashSet<Long>();
		}
		childrenList.add(child);
		childrenHier.put(parent, childrenList);
	}
	
	/**
	 * Checks if is ancestor of.
	 *
	 * @param ancestor the ancestor
	 * @param descendant the descendant
	 * @return true, if is ancestor of
	 */
	public boolean isAncestorOf(Long ancestor,Long descendant){
		
		HashSet<Long>parent=parentHier.get(descendant);
		if (parent==null){
			return false;
		}
		if (parent.contains(ancestor)){
			return true;
		}
		for(Long par:parent){
			if (isAncestorOf(ancestor,par)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets the parent.
	 *
	 * @param conceptId the concept id
	 * @return the parent
	 */
	public HashSet<Long> getParent(Long conceptId) {
		return parentHier.get(conceptId);
	}

	/**
	 * Gets the children.
	 *
	 * @param conceptId the concept id
	 * @return the children
	 */
	public HashSet<Long> getChildren(Long conceptId) {
		return childrenHier.get(conceptId);
	}
}
