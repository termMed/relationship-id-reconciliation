/**
 * Copyright (c) 2015 TermMed SA
 * Organization
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package com.termmed.reconciliation.model;



/**
 * The Class Relationship.
 * 
 * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */
public class Relationship implements Comparable<Object> {

	/** The relationship Id. */
	public String relationshipId;

	/** The source Id. */
	public long sourceId; 

	/** The destination Id. */
	public long destinationId;

	/** The type id. */
	public long typeId;

	/** The group. */
	public int group;

	/** The eff time. */
	public int effTime;

	/** The module. */
	public long module;

	/** The char type. */
	public long charType;

	/** The active. */
	public short active;

	/**
	 * Instantiates a new relationship.
	 *
	 * @param sourceId the source id
	 * @param destinationId the destination id
	 * @param roleTypeId the role type id
	 * @param group the group
	 * @param relationshipId the relationship id
	 * @param effTime the eff time
	 * @param module the module
	 * @param active the active
	 * @param charType the char type
	 */
	public Relationship(long sourceId, long destinationId, long roleTypeId, int group, String relationshipId,int effTime,long module,short active,long charType) {
		this.sourceId = sourceId;
		this.destinationId = destinationId;
		this.typeId = roleTypeId;
		this.group = group;
		this.relationshipId = relationshipId;
		this.effTime=effTime;
		this.module=module;
		this.charType=charType;
		this.active=active;
	}



	/**
	 * Gets the rel id.
	 *
	 * @return the rel id
	 */
	public String getRelId() {
		return relationshipId;
	}


	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		Relationship other = (Relationship) o;
		int thisMore = 1;
		int thisLess = -1;
		if (this.sourceId > other.sourceId) {
			return thisMore;
		} else if (this.sourceId < other.sourceId) {
			return thisLess;
		} else {
			if (this.group > other.group) {
				return thisMore;
			} else if (this.group < other.group) {
				return thisLess;
			} else {
				if (this.typeId > other.typeId) {
					return thisMore;
				} else if (this.typeId < other.typeId) {
					return thisLess;
				} else {
					if (this.destinationId > other.destinationId) {
						return thisMore;
					} else if (this.destinationId < other.destinationId) {
						return thisLess;
					} else {
						return 0; // this == received
					}
				}
			}
		}
	} 
	
	/**
	 * Compare wo group to.
	 *
	 * @param o the o
	 * @return the int
	 */
	public int compareWOGroupTo(Object o) {
		Relationship other = (Relationship) o;
		int thisMore = 1;
		int thisLess = -1;
		if (this.sourceId > other.sourceId) {
			return thisMore;
		} else if (this.sourceId < other.sourceId) {
			return thisLess;
		} else {
			if (this.typeId > other.typeId) {
				return thisMore;
			} else if (this.typeId < other.typeId) {
				return thisLess;
			} else {
				if (this.destinationId > other.destinationId) {
					return thisMore;
				} else if (this.destinationId < other.destinationId) {
					return thisLess;
				} else {
					return 0; // this == received
				}
			}
		}
	} 
	
	/**
	 * Compare same concept wo group to.
	 *
	 * @param o the o
	 * @return the int
	 */
	public int compareSameConceptWOGroupTo(Object o) {
		Relationship other = (Relationship) o;
		int thisMore = 1;
		int thisLess = -1;
		if (this.typeId > other.typeId) {
			return thisMore;
		} else if (this.typeId < other.typeId) {
			return thisLess;
		} else {
			if (this.destinationId > other.destinationId) {
				return thisMore;
			} else if (this.destinationId < other.destinationId) {
				return thisLess;
			} else {
				return 0; // this == received
			}
		}
	} 

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(sourceId);
		sb.append(": ");
		sb.append(typeId);
		sb.append(": ");
		sb.append(destinationId);
		return sb.toString();
	}

} 


