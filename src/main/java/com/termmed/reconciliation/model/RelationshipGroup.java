/**
 * Copyright (c) 2015 TermMed SA
 * Organization
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package com.termmed.reconciliation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * The Class RelationshipGroup.
 * 
 * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */
public class RelationshipGroup extends ArrayList<Relationship> {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
    
    /**
     * Instantiates a new relationship group.
     *
     * @param relationships the relationships
     * @param sort the sort
     */
    public RelationshipGroup(List<Relationship> relationships, boolean sort) {
        super();
        // set doSort = true if list not pre-sorted to C1-Group-Type-C2 order
        if (sort)
            Collections.sort(relationships);
        this.addAll(relationships);
       
    }

    /**
     * Instantiates a new relationship group.
     *
     * @param o the o
     */
    public RelationshipGroup(Relationship o) {
        super();
        this.add(o); // 
    }

    /**
     * Instantiates a new relationship group.
     */
    public RelationshipGroup() {
        super();
    }

    /**
     * Adds the all with sort.
     *
     * @param roleGroupB the role group b
     * @return the relationship group
     */
    public RelationshipGroup addAllWithSort(RelationshipGroup roleGroupB) {

        this.addAll(roleGroupB);
        // SORT BY [ROLE-C2-GROUP-C2]
        Comparator<Relationship> comp = new Comparator<Relationship>() {
            public int compare(Relationship o1, Relationship o2) {
                int thisMore = 1;
                int thisLess = -1;
                if (o1.typeId > o2.typeId) {
                    return thisMore;
                } else if (o1.typeId < o2.typeId) {
                    return thisLess;
                } else {
                    if (o1.destinationId > o2.destinationId) {
                        return thisMore;
                    } else if (o1.destinationId < o2.destinationId) {
                        return thisLess;
                    } else {
                        return 0; // EQUAL
                    }
                }
            } // compare()
        };
        Collections.sort(this, comp);

        return this;
    }

    /**
     * Sort by type.
     *
     * @return the relationship group
     */
    public RelationshipGroup sortByType() {
        // SORT BY [ROLE-C2-GROUP-C2]
        Comparator<Relationship> comp = new Comparator<Relationship>() {
            public int compare(Relationship o1, Relationship o2) {
                int thisMore = 1;
                int thisLess = -1;
                if (o1.typeId > o2.typeId) {
                    return thisMore;
                } else if (o1.typeId < o2.typeId) {
                    return thisLess;
                } else {
                    if (o1.destinationId > o2.destinationId) {
                        return thisMore;
                    } else if (o1.destinationId < o2.destinationId) {
                        return thisLess;
                    } else {
                        return 0; // EQUAL
                    }
                }
            } // compare()
        };
        Collections.sort(this, comp);
        return this;
    }

    /**
     * Equals.
     *
     * @param roleGroupB the role group b
     * @return true, if successful
     */
    public boolean equals(RelationshipGroup roleGroupB) {
        int sizeA = this.size();
        if (sizeA != roleGroupB.size())
            return false; // trivial case, does not have same number of elements

        if (sizeA == 0)
            return true; // trivial case, both empty

        int i = 0;
        boolean isSame = true;
        while (i < sizeA) {
            if (this.get(i).typeId != roleGroupB.get(i).typeId || this.get(i).destinationId != roleGroupB.get(i).destinationId) {
                isSame = false;
                break;
            }
            i++;
        }

        return isSame;
    }

    /**
     * Find logical equivalent.
     *
     * @param groupList_B the group list_ b
     * @return the relationship group
     */
    public RelationshipGroup findLogicalEquivalent(RelationshipGroupList groupList_B) {
        for (RelationshipGroup relationshipGroup : groupList_B) {
            if (this.equals(relationshipGroup)) {
                return relationshipGroup;
            }
        }
        return null;
    }
    
    /**
     * Gets the distance to group in same concept.
     *
     * @param relationshipGroup the relationship group
     * @return the distance to group in same concept
     */
    public int getDistanceToGroupInSameConcept(RelationshipGroup relationshipGroup){
    	int ret=0;
    	if (this.equals(relationshipGroup)) {
            return 0;
        }
    	int sizeA = this.size();
    	int sizeB = relationshipGroup.size();
    	List<Integer> indexMatches=new ArrayList<Integer>();
    	int val;
    	for (int i=0;i<sizeA;i++){
    		for (Integer j=0;j<sizeB;j++){
    			
    			if (!indexMatches.contains(j)){
	    			val=this.get(i).compareSameConceptWOGroupTo(relationshipGroup.get(j));
	    			if (val==0){
	    				indexMatches.add(j);
	    				break;
	    			}
    			}
    		}
    	}
    	int sizeMatches=indexMatches.size();
    	if (sizeMatches==0){
    		return Integer.MAX_VALUE;
    	}
    	if (sizeA>sizeB){
    		ret+= ((sizeB-sizeMatches) * 2);
    		ret+=sizeA-sizeB;
    	}else{
    		ret+= ((sizeA-sizeMatches) * 2);
    		ret+=sizeB-sizeA;
    	}
    	
    	return ret;
    }

} 

