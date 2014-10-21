#!/bin/sh

export VERSION_SNPEFF=4.0
export SUBVERSION_SNPEFF=""
export VERSION_SNPSIFT=$VERSION_SNPEFF

export ENSEMBL_RELEASE=76
export ENSEMBL_BFMPP_RELEASE=22

# Version values using underscores ('3_2' instead of '3.2')
export SNPEFF_VERSION=`echo $VERSION_SNPEFF | tr "." "_"`
export SNPSIFT_VERSION=`echo $VERSION_SNPSIFT | tr "." "_"`

export SNPEFF_VERSION_REV=$SNPEFF_VERSION"$SUBVERSION_SNPEFF"

# HUman and mouse versions
GRCH=GRCh38
GRCM=GRCm38
