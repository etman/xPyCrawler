#!/bin/sh
ARG_WORKING_DIR=$1

WORK_PATH=$ARG_WORKING_DIR/extractor
LIBS_PATH=`ls $WORK_PATH/libs/*.jar | tr '\n' ':'`
java -cp $LIBS_PATH com.armorize.hackalert.extractor.HAExtractorTool 2>/dev/null <&0

