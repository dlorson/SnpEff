#!/bin/sh -e

SNPEFF="java -Xmx2G -jar snpEff.jar"

#---
# Build "special" cases
#---
# Test cases hg37.70
cp db/jaspar/pwms.bin data/testHg3770Chr22/
$SNPEFF build -noLog -gtf22 testHg3770Chr22

#---
# Buils all GTF 2.2
#---
for gen in testCase testLukas testHg19Chr1 testHg3761Chr15 testHg3761Chr16 testHg3763Chr1 testHg3763Chr20 testHg3763ChrY testHg3765Chr22 testHg3766Chr1 testHg3767Chr21Mt testHg3769Chr12 testHg3771Chr1 testHg3775Chr1 testENST00000268124 testENST00000398332 testHg3775Chr1 testHg3775Chr6 testHg3775Chr7 testHg3775Chr12 testHg3775Chr14 testHg3775Chr22
do
	echo
	echo
	echo Genome: $gen
	$SNPEFF build -noLog $gen
done


