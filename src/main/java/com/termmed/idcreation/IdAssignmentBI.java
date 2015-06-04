/**
 * Copyright (c) 2015 TermMed SA
 * Organization
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */
package com.termmed.idcreation;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * The Interface IdAssignmentBI.
 */
public interface IdAssignmentBI {

	/**
	 * The Enum IDENTIFIER.
	 */
	public enum IDENTIFIER {
		
		/** The tdeuid. */
		TDEUID(1), 
 /** The sctid. */
 SCTID(2), 
 /** The snomedid. */
 SNOMEDID(3), 
 /** The CT v3 id. */
 CTV3ID(4), 
 /** The wbuid. */
 WBUID(5);
		
		/** The id number. */
		private int idNumber;
		
		/**
		 * Instantiates a new identifier.
		 *
		 * @param idNumber the id number
		 */
		IDENTIFIER(int idNumber){
			this.setIdNumber(idNumber);
		}

		/**
		 * Sets the id number.
		 *
		 * @param idNumber the new id number
		 */
		public void setIdNumber(int idNumber) {
			this.idNumber = idNumber;
		}

		/**
		 * Gets the id number.
		 *
		 * @return the id number
		 */
		public int getIdNumber() {
			return idNumber;
		}
		
	}

	/**
	 * Returns the SCTID related to the component UUID.
	 *
	 * @param componentUuid the component UUID
	 * @return the SCTID
	 * @throws Exception the exception
	 */
	public Long getSCTID(UUID componentUuid) throws Exception;

	/**
	 * Returns the component SnomedID.
	 *
	 * @param componentUuid the component uuid
	 * @return SnomedID
	 * @throws Exception the exception
	 */
	public String getSNOMEDID(UUID componentUuid) throws Exception;

	/**
	 * Returns the component CTV3ID.
	 *
	 * @param componentUuid the component uuid
	 * @return CTV3ID
	 * @throws Exception the exception
	 */
	public String getCTV3ID(UUID componentUuid) throws Exception;

	/**
	 * Returns a map of componentUUID - SCTID for every componentUUID of the parameter.
	 *
	 * @param componentUuidList the component uuid list
	 * @return componentUUID - SCTID pairs
	 * @throws Exception the exception
	 */
	public HashMap<UUID, Long> getSCTIDList(List<UUID> componentUuidList) throws Exception;

	/**
	 * Creates and returns SCTID.
	 *
	 * @param componentUuid the component uuid
	 * @param namespaceId the namespace id
	 * @param partitionId the partition id
	 * @param releaseId the release id
	 * @param executionId the execution id
	 * @param moduleId the module id
	 * @return SCTID
	 * @throws Exception the exception
	 */
	public Long createSCTID(UUID componentUuid, Integer namespaceId, String partitionId, 
			String releaseId, String executionId, String moduleId) throws Exception;

	/**
	 * Creates and returns SNOMEDID.
	 *
	 * @param componentUuid the component uuid
	 * @param parentSnomedId the parent snomed id
	 * @return SNOMEDID
	 * @throws Exception the exception
	 */
	public String createSNOMEDID(UUID componentUuid, String parentSnomedId) throws Exception;

	/**
	 * Creates and returns CTV3ID.
	 *
	 * @param componentUuid the component uuid
	 * @return CTV3ID
	 * @throws Exception the exception
	 */
	public String createCTV3ID(UUID componentUuid) throws Exception;
	
	/**
	 * Returns identifier-conceptid map.
	 *
	 * @param componentUuid the component uuid
	 * @param parentSnomedId the parent snomed id
	 * @param namespaceId the namespace id
	 * @param partitionId the partition id
	 * @param releaseId the release id
	 * @param executionId the execution id
	 * @param moduleId the module id
	 * @return Concept Ids
	 * @throws Exception the exception
	 */
	public HashMap<IDENTIFIER, String> createConceptIds(UUID componentUuid, String parentSnomedId, 
			Integer namespaceId, String partitionId, String releaseId, String executionId, 
			String moduleId) throws Exception;
	
	/**
	 * Returns componentUUID - SCTID map.
	 *
	 * @param componentUuidList the component uuid list
	 * @param namespaceId the namespace id
	 * @param partitionId the partition id
	 * @param releaseId the release id
	 * @param executionId the execution id
	 * @param moduleId the module id
	 * @return componentUUID - SCTID map
	 * @throws Exception the exception
	 */
	public HashMap<UUID, Long> createSCTIDList(List<UUID> componentUuidList, Integer namespaceId, String partitionId, 
			String releaseId, String executionId, String moduleId) throws Exception;

	/**
	 * For every componentUUID and ParentSnomedID pair returns <br>
	 * a map of the componentUUID - (identifier-conceptid) map.
	 *
	 * @param componentUUIDandParentSnomedId the component uui dand parent snomed id
	 * @param namespaceId the namespace id
	 * @param partitionId the partition id
	 * @param releaseId the release id
	 * @param executionId the execution id
	 * @param moduleId the module id
	 * @return <ComponentUUID, HashMap<IDENTIFIER, concpetid>>
	 * @throws Exception the exception
	 */
	public HashMap<UUID, HashMap<IDENTIFIER, String>> createConceptIDList(HashMap<UUID, String> componentUUIDandParentSnomedId, 
			Integer namespaceId, String partitionId, String releaseId,
			String executionId, String moduleId) throws Exception;

}
