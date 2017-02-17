#!/bin/bash
CP=""
(
cd ..
for i in `ls -1 libs/*.jar`
do
  if [ "$CP" = "" ]
  then
    CP=$i
  else
    CP="$CP:$i"
  fi
done

CP="$CP:bin"
java -cp $CP $@
)
