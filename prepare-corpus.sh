#!/usr/bin/env bash
mvn clean compile exec:java -Dexec.mainClass="io.github.cbadenes.crosslingual.tasks.CorpusDownload"
mvn clean compile exec:java -Dexec.mainClass="io.github.cbadenes.crosslingual.tasks.CorpusPrepare"
mvn clean compile exec:java -Dexec.mainClass="io.github.cbadenes.crosslingual.tasks.CorpusPrepareStatistics"
mvn clean compile exec:java -Dexec.mainClass="io.github.cbadenes.crosslingual.tasks.CorpusSplitTrainAndTest"