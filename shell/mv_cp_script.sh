#!/bin/bash

echo "Command: "$1
echo "Source: "$2
echo "Destiny: "$3
cp="cp"
mv="mv"

if [ "$1" = "$cp" ]
then
	cp $2 $3
	echo "entrou cp"
else
	mv $2 $3
	echo "entrou mv"
fi
