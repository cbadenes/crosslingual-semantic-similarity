#!/usr/bin/env bash
mvn exec:java -Dexec.mainClass="io.github.cbadenes.crosslingual.tasks.CorpusDownload"
mvn exec:java -Dexec.mainClass="io.github.cbadenes.crosslingual.tasks.CorpusPrepare"
mvn exec:java -Dexec.mainClass="io.github.cbadenes.crosslingual.tasks.CorpusPrepareStatistics"
mvn exec:java -Dexec.mainClass="io.github.cbadenes.crosslingual.tasks.CorpusSplitAndTest"