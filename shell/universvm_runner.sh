#!/bin/bash

welcome_msg()
{
	echo "##########################################################################################"
	echo "#####  Shell Script desenvolvido para realizar os experimentos utilizando UniverSVM. #####"
	echo "#####  É necessário informar 3 parâmetros: número de execuções, número de iterações  #####"
	echo "#####  e nome da pasta que contêm os resultados.                                     #####"
	echo "#####                      Escrito por Guilherme Camargo.                            #####"
	echo "##########################################################################################"
}

verifica_parametros()
{
	if [ $1 -ne 3 ];
		then
		echo "Necessita 3 parâmetros: "
		echo "			Número de execuções;"
		echo "			número de iterações e "
		echo "			nome da pasta que contêm os resultados.\n"
		exit 1
	fi
}

arff2svm()
{
	path=`pwd`;
	cp /home/guilherme/NetBeansProjects/ExplorandoAL/lib/arff2opf.jar $path

	java -jar arff2opf.jar teste.arff testing
	opf2svm testing.opf testing.svm

	java -jar arff2opf.jar $1 training
	opf2svm training.opf training.svm

	java -jar arff2opf.jar unlabeled.arff unlabeled
	opf2svm unlabeled.opf unlabeled.svm

	rm $path/arff2opf.jar
}

conta_classes()
{
	while read linha
	do
		case "$linha" in *class*)
			num=`echo $linha |  awk 'BEGIN{FS=","} {print NF?NF-1:0}'`
			num=$((num+1))
			break
		;;
		esac
	done < $1
	return "$num"
}


clear
welcome_msg

verifica_parametros "$#"

n_execucoes=$1;
n_execucoes=$(($n_execucoes-1))
n_iteracoes=$2;
n_iteracoes=$(($n_iteracoes-1))
pasta_resultado=$3

cd ..

for i in $(seq 0 1 $n_execucoes);
do
	cd "execution_$i/$pasta_resultado"

	for j in $(seq 0 1 $n_iteracoes);
	do
		cd "it$j"
		conta_classes "raizes$j.arff"
		n_classes=$?
		arff2svm "raizes$j.arff"
		echo "oooooooooooooooooooppppppppaaaaaaa       $n_classes"
		universvm -B 0 -M $n_classes -u unlabeled.svm.txt -f universvm.output -T testing.svm.txt training.svm.txt
		cd ..
	done

done


