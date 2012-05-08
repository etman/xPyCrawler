#!/bin/sh
working_dir=`pwd`
curl -s $1 | extractor/extrator-cmd.sh $1 $2 | extractor/swf2links.sh $working_dir

