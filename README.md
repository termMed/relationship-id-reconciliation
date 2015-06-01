# relationship-id-reconciliation
This projects runs an id reconciliation algorithm for inferred relationship files.

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
The method requires a parameter that points to the location of the configuration file (like "RunConfiguration.xml").


