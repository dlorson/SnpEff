#!/bin/sh

source `dirname $0`/config.sh

#---
# Build SnpEff
#---

cd `dirname $0`/../

mvn -U clean compile assembly:assembly

cp target/snpEff-$VERSION_SNPEFF-jar-with-dependencies.jar $HOME/snpEff/snpEff.jar