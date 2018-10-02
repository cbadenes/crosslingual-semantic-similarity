 
DICTIONARIES FOR LEMMATIZATION
--------------------------------

The dictionaries contained in this tarball have been prepared using the dictionaries
distributed by the Language Tool project (http://languagetool.org/),
which distributes its data contents under a variety of licenses. 

http://wiki.languagetool.org/archive-developing-a-tagger-dictionary

Please read the COPYING file in this tarball for terms about
distribution and use of these dictionaries. 

Languages: 
    + English: 301402 word-lemma-pos combinations. 


The format of the dictionaries consist of "word\tab\lemma\tabpos" and it can be 
used to perform dictionary-based lemmatization. They are distributed as 
spanish.dict, english.dict, etc., in a finite state automata format created 
using Morfologik (https://github.com/morfologik/). These dictionaries need 
the corresponding $lang.info files to work. 

These dictionaries (in both binary and plain text format) are being
used by the ready to use IXA pipes (ixa2.si.ehu.es/ixa-pipes) pos tagger 
(https://github.com/ixa-ehu/ixa-pipe-pos) to perform English and
Spanish dictionary-based lemmatization.  

Note that plain text dictionaries can also be used in their current form
via the API (check SimpleLemmatizer class). 

Contact details: 
----------------
Rodrigo Agerri <rodrigo.agerri@ehu.es>
IXA NLP Group
University of the Basque Country (UPV/EHU)
