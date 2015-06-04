package com.termmed.reconciliation;

import java.io.File;

import org.apache.log4j.Logger;

import com.termmed.idcreation.IdCreation;
import com.termmed.reconciliation.utils.I_Constants;

public class ReconciliationRunner {

	/** The logger. */
	private static Logger logger;

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
