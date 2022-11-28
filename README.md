
# Supplementary material of a comparative user study between a keyword search and a semantic search

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.7374399.svg)](https://doi.org/10.5281/zenodo.7374399)

[![DOI](https://zenodo.org/badge/571790916.svg)](https://zenodo.org/badge/latestdoi/571790916)

This repository provides the code for a Java application to expand a given search term on semantically related concepts.
It supports the expansion on synonyms, descendant and ancestor nodes.
The system takes a list of search terms as input and generates a csv file as output with related terms from ontologies. 

The csv structure also contains the kind of expansion (synonym, child, parent), e.g.,

* "ontology";"uri";"sourceTerm";"expansionTerm";"synonym";"childNode";"siblingNode";"parentNode"

Two terminology services are currently supported:

* GFBio TS: https://terminologies.gfbio.org (all available ontologies)
* Bioportal: https://data.bioontology.org (limited to [OBO Foundry ontologies](https://obofoundry.org/) GO and CMO)


## Prerequisites

The code has been implemented and tested with Java 1.8., Apache ANT 1.10 and ivy need to be installed, too.

For Bioportal an access key is needed: https://bioportal.bioontology.org/accounts/new

##Installation

* open a command line and navigate to the folder

```
ant run

```

## License
The code in this project is distributed under the terms of the [GNU LGPL v3.0.](https://www.gnu.org/licenses/lgpl-3.0.en.html)


## Data

The primary data of the user study are available at Zenodo: https://doi.org/10.5281/zenodo.7374398

## Publication
Löffler, F. and Klan, F. (2016): Does Term Expansion Matter for the Retrieval of Biodiversity Data? in Joint Proceedings of the Posters and Demos Track of the 12th International Conference on Semantic Systems - SEMANTiCS2016 and the 1st International Workshop on Semantic Change & Evolving Semantics (SuCCESS'16), co-located with the 12th International Conference on Semantic Systems (SEMANTiCS 2016),2016, http://ceur-ws.org/Vol-1695/paper2.pdf
