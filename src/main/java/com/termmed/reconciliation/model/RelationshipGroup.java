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
package com.termmed.reconciliation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The Class RelationshipGroup.
 * Represents a relationships list.
 */
public class RelationshipGroup extends ArrayList<Relationship> {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
    
    /** The Constant debug. */
    private static final boolean debug = false; // :DEBUG:

    /**
     * Instantiates a new sno grp.
     *
     * @param relationships the relationships list
     * @param sort true if the list needs to be sorted
     */
    public RelationshipGroup(List<Relationship> relationships, boolean sort) {
        super();
        // set doSort = true if list not pre-sorted to C1-Group-Type-C2 order
        if (sort)
            Collections.sort(relationships);
        this.addAll(relationships);
       
    }

    /**
     * Instantiates a new sno grp.
     *
     * @param o the o
     */
    public RelationshipGroup(Relationship o) {
        super();
        this.add(o); // 
    }

    /**
     * Instantiates a new sno grp.
     */
    public RelationshipGroup() {
        super();
    }

    /**
     * Adds the all with sort.
     *
     * @param roleGroupB the role group b
     * @return the sno grp
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
     * @return the sno grp
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
     * Does roleGroupA Role-Value match roleGroupB Role-Values?<br>
     * <br>
     * <font color=#990099> IMPLEMENTATION NOTE: roleGroups MUST be pre-sorted
     * in C1-Group-Type-C2 order for this routine. Pre-sorting is used to
     * provide overall computational efficiency.</font>
     *
     * @param roleGroupB the role group b
     * @return true iff RoleValues match
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
     * Find logically equivalent role group from role group list provided. <br>
     * <br>
     * <font color=#990099> IMPLEMENTATION NOTE: roleGroups MUST be pre-sorted
     * in C1-Group-Type-C2 order for this routine. Pre-sorting is used to
     * provide overall computational efficiency.</font>
     *
     * @param groupList_B the group list_ b
     * @return RelationshipGroup iff logically equivalent role group found
     */
    public RelationshipGroup findLogicalEquivalent(RelationshipGroupList groupList_B) {
        for (RelationshipGroup relationshipGroup : groupList_B) {
            if (this.equals(relationshipGroup)) {
                return relationshipGroup;
            }
        }
        return null;
    }
    
    public int getDistanceToGroupInSameConcept(RelationshipGroup relationshipGroup){
    	int ret=0;
    	if (this.equals(relationshipGroup)) {
            return 0;
        }
    	int sizeA = this.size();
    	int sizeB = relationshipGroup.size();
    	List<Integer> indexMatches=new ArrayList<Integer>();
    	boolean match;
    	int val;
    	for (int i=0;i<sizeA;i++){
    		match=false;
    		for (Integer j=0;j<sizeB;j++){
    			
    			if (!indexMatches.contains(j)){
	    			val=this.get(i).compareSameConceptWOGroupTo(relationshipGroup.get(j));
	    			if (val==0){
	    				match=true;
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

} // class RelationshipGroup

