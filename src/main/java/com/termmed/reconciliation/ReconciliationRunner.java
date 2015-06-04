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

import org.apache.log4j.Logger;

import com.termmed.idcreation.IdCreation;
import com.termmed.reconciliation.utils.I_Constants;


/**
 * The Class ReconciliationRunner.
 *
 * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */
public class ReconciliationRunner {

	/** The logger. */
	private static Logger logger;

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args){

		logger = Logger.getLogger("com.termmed.reconciliation.ReconciliationRunner");
		try {
			File file =new File(I_Constants.RUN_CONFIGURATION_FILE);
			if (!file.exists()){
				logger.info("Error happened getting params. Params file doesn't exist");
				System.exit(0);
			}

			for (String arg : args){
				if (arg.equals("-R") ){
					RelationshipReconciliation cc=new RelationshipReconciliation(file);
					cc.execute();
					cc=null;
				}
			}
			for (String arg : args){
				if (arg.equals("-G")){
					
					IdCreation ic=new IdCreation(file);
					ic.execute();
					ic=null;
				}
			}

			for (String arg : args){
				if (arg.equals("-C")){
					
					ChangeConsolidator cc=new ChangeConsolidator(file);
					cc.execute();
					cc=null;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} 
		System.exit(0);
	}
}
