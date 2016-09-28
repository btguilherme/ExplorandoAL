#!/bin/bash

for path in `ls -d ecoli_no_string_att_number_class_-_act_-_none_-_lista_-_*`
do
	cd $path
	cp fronteira.arff /home/guilherme/$path.arff
	echo ""
	cd ..
done

exit
