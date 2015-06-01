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

Parameters
* Config file name: points to the name of the configuration file (like "RunConfiguration.xml"), always located in the project root folder.
* -R : will run reconciliation
* -G : will run Ids generation for new relationships
* -C : will generate the consolidated Snapshot and Delta files (otherwise it generates only the debug output file)

The process will generate a "logs" folder automatically with debug information.

## Reconciliation algorithm
The process will try to identify a relationshipId for each relationship in the current relationships file, based on the previously released relationships file.

To identify if the relationship has been published before, the matching process uses an incremental approach, starting with a strict policy, and relaxing the policy in subsequent runs. If after all the steps the relationship was not identified is considered new and it will be assigned a new id.

Steps:

1. Match with: sourceId, typeId, destinationId, effectiveTime, groupId
2. Match with: sourceId, typeId, destinationId, effectiveTime
3. Match with: sourceId, typeId, destinationId, groupId
4. Match with: sourceId, typeId, destinationId

If in any of these steps more than one relationship matches, the first match is assigned the previously published Id, and the rest gets a new Id.

The runner executes these 4 steps for different sets and resolves remaining relationships:

1. Current active vs previous active, using Groups composition matching
2. Current active vs previous active, individual relationships
3. Current active vs previous inactive
4. Assign new ids to the current active relationships that were not matched
5. Inactivate previously released inferred relationships that were not matched

When a relationship or a group matches, it is removed from the candidate set and the following iteration match with the rest of the candidates.


