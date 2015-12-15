# vcmtool-java
Tool to handle Visual Components VCM files

This tool was written to manage VCM components for the FlexLink Design Tool. 

## Components
The repository consists of two parts:
* vcm:api - An API to read and write VCM files and also parsing the resource and component data within a VCM file
* vcm:console - A command line tool to manipulate 

## Console application
* generateweb - Generates the eCat for 2012 and 2014
* grep - Greps data from component.dat or resource.dat
* import - Import files into VCM
* tags - List or modifies tags in meta data (component.dat)
* keywords - List or modifies key words in meta data (component.dat)
* props - List or modifies properties (component.rsc)
* vcid - List or modifies VCID (component.dat, component.rsc)
* items - List or modifies meta data (component.dat)
* revisions - List or modifies detailed revision (component.dat)
* location - List or modifies location of model
* verify-propvalues - Verifies property values
* verify-propexists - Verifies that certain properties exists in components
* verify-compname - Verifies that component names matches a regex
* verify-keywords - Verifies property names does not start or end with space
* verify-propnames - Verifies property names does not start or end with space
* verify-zeropos - Verifies that the component is saved into a [1,0,0,0][0,1,0,0][0,0,1,0][0,0,0,1] position
* search - Searches for any text in resource tree (component.rsc)
* replace - Replaces any text in resource tree (component.rsc)
* snippet - Replaces text snippets in python scripts (component.rsc)
* batchupdate - Batch updates several files using a CSV file.
* export - Export files from VCM
* list-nodes
* help                Displays usage for a command (this)
