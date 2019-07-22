# crosslingual-semantic-similarity
Definition and implementation of experiments aimed at evaluating the ability of algorithms based on probabilistic topic models to calculate the similarity between texts in different languages

This code supports the work described in the article: 

    Badenes-Olmedo Carlos, Redondo-Garcia Jose-Luis and Corcho Oscar. 2019. "Scalable Cross-lingual Document Similarity through Language-specific Concept Hierarchies", Knowledge Capture Conference (K-CAP 2019), (under-review)

Download corpora from: https://delicias.dia.fi.upm.es/nextcloud/index.php/s/wkirGFYoC873DwC/download


Evaluations have been implemented through JUnit tests using the JRC-Acquis corpora: 
* `jrc/AnnotateJRCDocumentsTask`:  retrieves texts stored in an external document repository and annotates them with its hierarchy of topics and concepts based on Wordnet-Synset. 
* `jrc/ClassEvaluationTask`: randomly selects a list of documents and compares their performance by sorting or sorting them according to previous annotations. The reference data set is constructed from the categorical annotations previously held by the documents.
