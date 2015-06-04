# SNOMED CT RelationshipId Reconciliation
This projects runs an id reconciliation algorithm for inferred relationship files.

The problem that aims to resolve is that after a classification, inferred relationships don't have an id, and looking for the id by the triplet (source, type, target) in previous released file is not enough. Many concepts have the same triplet twice in different relationship groups. Also, the relationship groups number and composition may change in each release, difficulting the identification of relationships contained in these.

## Configuration
The file "RunConfiguration.xml" contains all the execution parameters, all parameters are mandatory and need to be specified before the run:

Input files:
* current_inferred_relationship_snapshot_files: This array contains the path to the current inferred relationship files, usually there will be only one, but an array is supported for extension content. 
This is the file that contains freshly generated inferred relationships, put from the classifier, the output file will contain this exact same set of relationships but it will assign relationship ids based on the previous publication.
* previous_inferred_relationship_snapshot_files: This array contains the path to the previously released inferred relationship files, usually there will be only one, but an array is supported for extension content.
This is the file will be the gold standard for assigning the same ids to current relationships that can be identified as equivalent.
* additional_rels_snapshot_file: a file with the additional relationships, that are not present in the output of the classifier.

Id Assigner: (it will be used with the relationship is new and no reconciliation is possible)
* end_point_url: The URL for the ID assigner service.
* user: The username for the ID assigner service.
* pass: The password for the ID assigner service.
* release_date: The planned release date for future reference in the ID assigner, also used for new relationships.
* namespace: SCTID Namespace for new relationshipIds
* partition: Fixed values for the partition indentifier, either "02" for the international edition or "12" from an extension.

Debug files:
* relationships_output_file: Raw relationships file, it will be used during th eexceution, and helps debugging.

Output files:
* consolidated_snapshot_file: Snapshot file, ready to be tested
* consolidated_delta_file: Delta relationships file, ready to be tested

## Running the reconciliation processs
The main method is located in the class "org.ihtsdo.reconciliation.ReconciliationRunner.java", this should be executed from an IDE at the time, or it can be compiled into an executable jar (not provided in this version).

The process will look for additional parameters in the mandatory "RunConfiguration.xml" file, always located in the project root folder.

Parameters

* -R : will run reconciliation
* -G : will run Ids generation for new relationships
* -C : will generate the consolidated Snapshot and Delta files (otherwise it generates only the debug output file)

The process will generate a "logs" folder automatically with debug information.

## Reconciliation algorithm
The process will try to identify a relationshipId for each relationship in the current relationships file, based on the previously released relationships file.

The process includes 4 stages, some of them optional according to the parameters passed to the runner class.

When a relationship or a group matches, it is removed from the candidate set and the following iteration matches with the rest of the candidates.

#### 1. Relationship Groups reconciliation
The first stage uses group composition matching, ignoring the groupId, to identify previously released groups that have not changed the composition.

If the entire group matches, all the relatiomship ids are inherited from previous release, and the groupId is also inherited from last release.

The process also uses partial group matching, to identify groups that have changed from last release (lost or gained a relationship). When there are no "whole group" matches, the process selects the most similar group, that has some components in common, and then inherits relationshipIds and groupIds from the pevious release. New relationships in groups are left without id for the next step, in case they match with previosuly ungrouped relationships.

#### 2. Individual relationships matching
This second stage searches for matches in the remaining relationships.

To identify individual relationships, the matching process uses an incremental approach, starting with a strict policy, and relaxing the policy in subsequent runs. If, after all the steps the relationship was not identified, its considered new and it will be assigned a new id.

Individual relationships are matched inside groups, tracking them through different groups and ungrouped state changes.

Steps:

1. Match with: sourceId, typeId, destinationId, effectiveTime, groupId
2. Match with: sourceId, typeId, destinationId, effectiveTime
3. Match with: sourceId, typeId, destinationId, groupId
4. Match with: sourceId, typeId, destinationId

Each time a relationship matches, the id is assigned from the previous release and both relationships are removed from the candidate sets for the next iterations.

The runner executes these 4 steps for different sets and resolves remaining relationships, first for current active vs previous active and then for current active vs previous inactive.

#### 3. Id Assignment 
In this third stage all relationshps that were not matched get new ids from the Ids Assignment Service. The UUID in this step is a random UUID, to avoid any colision with other ids assigned in the past or the future.

#### 4. Consolidation
In this fourth stage the process creates Delta and Snapshot files based on the Ids Reconciliation results.

1. Previously inactive relationships that match with current active relationships are reactivated
2. Inactivate previously released inferred relationships that were not matched
3. Merge for Delta and Snapshot

## Testing / Demonstration
The project includes junit tests that run the algorithm from specific test resources, sets of relationships prepared to demonstrate the effect.

Example output for groupId reconciliation example dataset:
```
[jun 04 17:50:27] INFO  (RelationshipReconciliation.java:1266) - Reconciliation - Parameters:
[jun 04 17:50:27] INFO  (RelationshipReconciliation.java:1267) - Current Relationship files : 
[jun 04 17:50:27] INFO  (RelationshipReconciliation.java:1270) - src/test/resources/org/ihtsdo/reconciliation/test/GroupNumberReconciliation_Current_File.txt
[jun 04 17:50:27] INFO  (RelationshipReconciliation.java:1273) - Previous Relationship files : 
[jun 04 17:50:27] INFO  (RelationshipReconciliation.java:1276) - src/test/resources/org/ihtsdo/reconciliation/test/GroupNumberReconciliation_Prev_File.txt
[jun 04 17:50:27] INFO  (RelationshipReconciliation.java:1279) - Output Relationship file : src/test/resources/org/ihtsdo/reconciliation/test/GroupNumber_Inferred_Reconciliated.txt
[jun 04 17:50:27] INFO  (RelationshipReconciliation.java:476) - 
::: [Start group number reconciliation]
::: Relationships with group number changes = 	6
::: [Partial time] Sort/Compare Input & Output: 	0.0020 (seconds)	(mS)	

[jun 04 17:50:27] INFO  (RelationshipReconciliation.java:199) - 
::: [Reconciliation by previous actives vs current actives, ungrouped and grouped comparation]
::: Previous active relationships to match = 	11
::: Current active relationships to match = 	11
::: Partial process statistics:
::: Reconciliated relationships:  	11
::: Reconciliated Isa's relationships:  	3
::: Previous relationships without match :   	0
::: Current relationships without match:   	0
::: Current Isa's relationships without match:	0
::: 
::: [Partial time] Sort/Compare Input & Output: 	1	(mS)	

[jun 04 17:50:27] INFO  (RelationshipReconciliation.java:251) - 
::: Complete Process statistics:
::: Reconciliated relationships:  	11
::: Reconciliated Isa's relationships:  	3
::: Previous relationships without match :   	0
::: Current relationships without match:   	0
::: Current Isa's relationships without match:	0
::: 
::: *** WROTE *** LAPSED TIME =	0.02 (seconds)	 ***
```

Example output for relationshipIds reconciliation example dataset:
```
[jun 04 17:51:28] INFO  (RelationshipReconciliation.java:1266) - Reconciliation - Parameters:
[jun 04 17:51:28] INFO  (RelationshipReconciliation.java:1267) - Current Relationship files : 
[jun 04 17:51:28] INFO  (RelationshipReconciliation.java:1270) - src/test/resources/org/ihtsdo/reconciliation/test/IdReconciliation_Current_File.txt
[jun 04 17:51:28] INFO  (RelationshipReconciliation.java:1273) - Previous Relationship files : 
[jun 04 17:51:28] INFO  (RelationshipReconciliation.java:1276) - src/test/resources/org/ihtsdo/reconciliation/test/IdReconciliation_Prev_File.txt
[jun 04 17:51:28] INFO  (RelationshipReconciliation.java:1279) - Output Relationship file : src/test/resources/org/ihtsdo/reconciliation/test/Id_Inferred_Reconciliated.txt
[jun 04 17:51:28] INFO  (RelationshipReconciliation.java:476) - 
::: [Start group number reconciliation]
::: Relationships with group number changes = 	6
::: [Partial time] Sort/Compare Input & Output: 	0.0010 (seconds)	(mS)	

[jun 04 17:51:28] INFO  (RelationshipReconciliation.java:199) - 
::: [Reconciliation by previous actives vs current actives, ungrouped and grouped comparation]
::: Previous active relationships to match = 	9
::: Current active relationships to match = 	12
::: Partial process statistics:
::: Reconciliated relationships:  	6
::: Reconciliated Isa's relationships:  	4
::: Previous relationships without match :   	3
::: Current relationships without match:   	6
::: Current Isa's relationships without match:	0
::: 
::: [Partial time] Sort/Compare Input & Output: 	0	(mS)	

[jun 04 17:51:28] INFO  (RelationshipReconciliation.java:209) - 
::: [Reconciliation by previous actives vs current actives, without grouping comparation - step:1]
::: Current active relationships to reconciliate = 	6
::: Candidate previous active relationships to match = 	3
::: Partial process statistics:
::: Reconciliated relationships:  	0
::: Reconciliated Isa's relationships:  	0
::: Previous relationships without match :   	3
::: Current relationships without match:   	6
::: Current Isa's relationships without match:	0
::: 
::: [Partial time] Sort/Compare Input & Output: 	0	(mS)	

[jun 04 17:51:28] INFO  (RelationshipReconciliation.java:209) - 
::: [Reconciliation by previous actives vs current actives, without grouping comparation - step:2]
::: Current active relationships to reconciliate = 	6
::: Candidate previous active relationships to match = 	3
::: Partial process statistics:
::: Reconciliated relationships:  	0
::: Reconciliated Isa's relationships:  	0
::: Previous relationships without match :   	3
::: Current relationships without match:   	6
::: Current Isa's relationships without match:	0
::: 
::: [Partial time] Sort/Compare Input & Output: 	0	(mS)	

[jun 04 17:51:28] INFO  (RelationshipReconciliation.java:209) - 
::: [Reconciliation by previous actives vs current actives, without grouping comparation - step:3]
::: Current active relationships to reconciliate = 	6
::: Candidate previous active relationships to match = 	3
::: Partial process statistics:
::: Reconciliated relationships:  	2
::: Reconciliated Isa's relationships:  	0
::: Previous relationships without match :   	1
::: Current relationships without match:   	4
::: Current Isa's relationships without match:	0
::: 
::: [Partial time] Sort/Compare Input & Output: 	0	(mS)	

[jun 04 17:51:28] INFO  (RelationshipReconciliation.java:209) - 
::: [Reconciliation by previous actives vs current actives, without grouping comparation - step:4]
::: Current active relationships to reconciliate = 	4
::: Candidate previous active relationships to match = 	1
::: Partial process statistics:
::: Reconciliated relationships:  	1
::: Reconciliated Isa's relationships:  	0
::: Previous relationships without match :   	0
::: Current relationships without match:   	3
::: Current Isa's relationships without match:	0
::: 
::: [Partial time] Sort/Compare Input & Output: 	0	(mS)	

[jun 04 17:51:28] INFO  (RelationshipReconciliation.java:225) - 
::: [Reconciliation by previous inactives vs current actives, without grouping comparation - step:1]
::: Current active relationships to reconciliate = 	3
::: Candidate previous inactive relationships to match = 	9
::: Partial process statistics:
::: Reconciliated relationships:  	0
::: Reconciliated Isa's relationships:  	0
::: Previous relationships without match :   	0
::: Current relationships without match:   	3
::: Current Isa's relationships without match:	0
::: 
::: [Partial time] Sort/Compare Input & Output: 	1	(mS)	

[jun 04 17:51:28] INFO  (RelationshipReconciliation.java:225) - 
::: [Reconciliation by previous inactives vs current actives, without grouping comparation - step:2]
::: Current active relationships to reconciliate = 	3
::: Candidate previous inactive relationships to match = 	9
::: Partial process statistics:
::: Reconciliated relationships:  	0
::: Reconciliated Isa's relationships:  	0
::: Previous relationships without match :   	0
::: Current relationships without match:   	3
::: Current Isa's relationships without match:	0
::: 
::: [Partial time] Sort/Compare Input & Output: 	1	(mS)	

[jun 04 17:51:28] INFO  (RelationshipReconciliation.java:225) - 
::: [Reconciliation by previous inactives vs current actives, without grouping comparation - step:3]
::: Current active relationships to reconciliate = 	3
::: Candidate previous inactive relationships to match = 	9
::: Partial process statistics:
::: Reconciliated relationships:  	0
::: Reconciliated Isa's relationships:  	0
::: Previous relationships without match :   	0
::: Current relationships without match:   	3
::: Current Isa's relationships without match:	0
::: 
::: [Partial time] Sort/Compare Input & Output: 	0	(mS)	

[jun 04 17:51:28] INFO  (RelationshipReconciliation.java:225) - 
::: [Reconciliation by previous inactives vs current actives, without grouping comparation - step:4]
::: Current active relationships to reconciliate = 	3
::: Candidate previous inactive relationships to match = 	9
::: Partial process statistics:
::: Reconciliated relationships:  	0
::: Reconciliated Isa's relationships:  	0
::: Previous relationships without match :   	0
::: Current relationships without match:   	3
::: Current Isa's relationships without match:	0
::: 
::: [Partial time] Sort/Compare Input & Output: 	0	(mS)	

[jun 04 17:51:28] INFO  (RelationshipReconciliation.java:251) - 
::: Complete Process statistics:
::: Reconciliated relationships:  	9
::: Reconciliated Isa's relationships:  	4
::: Previous relationships without match :   	0
::: Current relationships without match:   	3
::: Current Isa's relationships without match:	0
::: 
::: *** WROTE *** LAPSED TIME =	0.014 (seconds)	 ***
```

termMed 2015




