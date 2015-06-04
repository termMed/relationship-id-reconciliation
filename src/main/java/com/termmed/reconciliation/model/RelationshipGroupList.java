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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * The Class RelationshipGroupList.
 * 
 * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */
public class RelationshipGroupList extends ArrayList<RelationshipGroup> {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new relationship group list.
     */
    public RelationshipGroupList() {
        super();
    }

    /**
     * Instantiates a new relationship group list.
     *
     * @param rels the rels
     */
    public RelationshipGroupList(List<Relationship> rels) {
        super();
        // First Group
        RelationshipGroup group = new RelationshipGroup();
        this.add(group);

        // First Relationship in First Group
        Iterator<Relationship> it = rels.iterator();
        Relationship relationshipA = it.next();
        group.add(relationshipA);

        while (it.hasNext()) {
            Relationship relationshipB = it.next();
            if (relationshipB.group == relationshipA.group) {
                group.add(relationshipB); // ADD TO SAME GROUP
            } else {
                group = new RelationshipGroup(); // CREATE NEW GROUP
                this.add(group); // ADD GROUP TO GROUP LIST
                group.add(relationshipB);
            }
            relationshipA = relationshipB;
        }
    }

    /**
     * Count rels.
     *
     * @return the int
     */
    public int countRels() {
        int returnCount = 0;
        for (RelationshipGroup sg : this)
            returnCount += sg.size();
        return returnCount;
    }

   
    /**
     * Which not equal.
     *
     * @param groupListB the group list b
     * @return the relationship group list
     */
    public RelationshipGroupList whichNotEqual(RelationshipGroupList groupListB) {
        RelationshipGroupList sg = new RelationshipGroupList();
        for (RelationshipGroup groupA : this) {
            boolean foundEqual = false;
            for (RelationshipGroup groupB : groupListB) {
                if (groupA.equals(groupB)) {
                    foundEqual = true;
                    break;
                }
            }
            if (!foundEqual) {
                sg.add(groupA);
            }
        }
        return sg;
    }

    /**
     * Gets the equal relationship in group.
     *
     * @param groupListB the group list b
     * @return the equal relationship in group
     */
    public Map<Relationship,Relationship> getEqualRelationshipInGroup(RelationshipGroupList groupListB) {
        Map	<Relationship,Relationship> relsMap=new HashMap<Relationship,Relationship>();
        for (RelationshipGroup groupA : this) {
            for (RelationshipGroup groupB : groupListB) {
                if (groupA.equals(groupB)) {
                	for (Relationship rel_A:groupA){
                		for (Relationship rel_B:groupB){
                			if (rel_A.compareWOGroupTo(rel_B)==0){
                				relsMap.put(rel_A, rel_B);
                				break;
                			}
                		}
                	}
                    break;
                }
            }
        }
        return relsMap;
    }

}
