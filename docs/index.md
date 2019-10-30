---
title: "Sub-Hmm Viewer"
permalink: /index.html
---
A sub-HMM is subset of a full HMM. In this case, we used HMMs from PFAM families. For each full HMM, we extract several sub-HMMs which capture the most important parts of the full HMM. 
See "[Predicting Conserved Protein Motifs with Sub-HMMs](https://www.researchgate.net/publication/43344964_Predicting_conserved_protein_motifs_with_Sub-HMMs)" for more details.

The Sub-HMM viewer web application has now been retired. The application and data files can still be downloaded using the links below. 


Applications
========

[ExtractSubHMM](extractSubHMM-v4.tgz)
---------

This tool will take an hmm and split it into several subHMMs, writing each to a new file. It can read and write the new HMMER3 format as well.

Usage example: java -jar extractSubHMM.jar -l 8 -m 8 test.hmm

-l is the minimum length of a sub-hmm, -m is the amount of smoothing. Several HMMs can be given on the command line at once if desired. They can be run in parallel using the -h option to specify the number of threads.
If you just want to cut out a specific sub-hmm, you can give the coordinates yourself with the -c option:

java -jar extractSubHMM.jar -c 3,10 test.hmm

Extracted sub-HMMs will be named by adding a number after the name. For example, if test.hmm generated 3 sub-HMMs they would be named test-0.hmm, test-1.hmm, and test-2.hmm.

ExtractSubHMM can also be used to create logos with the -p flag:

java -jar extractSubHMM.jar -p test1.hmm test2.hmm ...

This will create files 'test1.hmm.png', 'test2.hmm.png', etc.

[extraction details](extractionDetails.pdf)

[ScoreSubHMM](scoreSubHMM-v2.tgz)
-------

This tool will score a list of sub-HMMs and return a sorted list of scores.

Usage example: java -jar scoreSubHMM.jar -s proteins.fasta test1.hmm test2.hmm ...

The sequence data should be a fasta file given by the -s option. The sub-HMMs to use can be either listed on the command line, or their full paths can be put in a file and the file given with the -f option. The number of hits returned is given by the -r option. By default, scoreSubHMM will just score each sequence against each given sub-HMM and report the top scoring sub-HMMs for each sequence. The other way to use it is in group mode, enabled by passing the -g option. In this mode all the given sub-HMMs are used to compute one score for each sequence.

The output format is a tab delimited file. The first column is the sub-HMM name, the second column is the sequence name, and the third column is the score. The remaining columns show the match information for each sub-HMM used to score a sequence, it consists of the start position, the length, and the score of the match, seperated by commas. The score printed is multipled by 1000 and truncated, so you must divide it by 1000 to recover the real score. In the default mode there will only be one such match, but in group mode there will be an addional column for each sub-HMM used. Also, in group mode the first column will always say 'group' since there is no single model name.

Data
=====

[pfam-22-shmm.gz](https://github.com/girke-lab/subHmmWeb/releases/download/v1.0/pfam-22-shmm.gz)

All sub-HMMs extracted from PFAM version 22.

[shmm-familiy-matches.gz](https://github.com/girke-lab/subHmmWeb/releases/download/v1.0/shmm-familiy-matches.gz)

This file documents every link between a sub-HMM and a family that it was not created from. See top of file for field descriptions 
