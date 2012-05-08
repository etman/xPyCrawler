#!/bin/sh
header=working.folder=./
header=$header"\nurl=$1"
header=$header"\ncontent.type=$2"

#make extrator input
echo $(printf "%010i" ${#header})
echo $header
cat<&0

