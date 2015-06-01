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
package org.ihtsdo.reconciliation.model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ihtsdo.reconciliation.utils.I_Constants;

/**
 * The Class Relationship.
 * Represents a relationship.
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

	public int effTime;

	public long module;

	public long charType;

	public short active;

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


	// default sort order [c1-group-type-c2]
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
	} // Relationship.compareTo()
	
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
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(sourceId);
		sb.append(": ");
		sb.append(typeId);
		sb.append(": ");
		sb.append(destinationId);
		return sb.toString();
	}

} // class Relationship


