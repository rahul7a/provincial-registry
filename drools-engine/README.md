# PHMS Drools Engine Library

This is a library the implements Drool's Rule engine for PHMS's business rules.

### Drools Rule Engine

Facts – represents data that serves as input for rules

Working Memory – a storage with Facts, where they are used for pattern matching and can be modified, inserted and
removed

Rule – represents a single rule which associates Facts with matching actions. It can be written in Drools Rule Language
in the .drl files or as Decision Table in an excel spreadsheet

Knowledge Session – it holds all the resources required for firing rules; all Facts are inserted into session, and then
matching rules are fired

Knowledge Base – represents the knowledge in the Drools ecosystem, it has the information about the resources where
Rules are found, and also it creates the Knowledge Session

Module – A module holds multiple Knowledge Bases which can hold different sessions

#### Java Configuration:

To fire rules on a given data, we need to instantiate the framework provided classes with information about the location
of rule files and the Facts:

1. KieFileSystem: we need to set the KieFileSystem bean; this is an in-memory file system provided by the framework.
   Following code provides the container to define the Drools resources like rules files, decision tables,
   programmatically:

2. KieContainer: we need to set the KieContainer which is a placeholder for all the KieBases for particular KieModule.
   KieContainer is built with the help of other beans including KieFileSystem, KieModule, and KieBuilder.

3. KieSession: The rules are fired by opening a KieSession bean – which can be retrieved from KieContainer

#### Drools Rule File (.drl):

A rule includes a When-Then construct, here the When section lists the condition to be checked, and Then section lists
the action to be taken if the condition is met.

For additional information visit https://confluence.lblw.cloud/display/VP/Drools+Engine+for+Business+Rules
