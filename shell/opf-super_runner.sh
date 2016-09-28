#!/bin/bash

welcome_msg()
{
	echo "##########################################################################################"
	echo "#####  Shell Script desenvolvido para realizar os experimentos utilizando a LibOPF.  #####"
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

arff2dat()
{
	path=`pwd`;
	cp /home/guilherme/NetBeansProjects/ExplorandoAL/lib/arff2opf.jar $path
	java -jar arff2opf.jar teste.arff testing
	txt2opf testing.opf testing.dat
	java -jar arff2opf.jar $1 training
	txt2opf training.opf training.dat
	rm $path/arff2opf.jar
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
		arff2dat "raizes$j.arff"
		opf_train training.dat
		opf_classify testing.dat
		opf_accuracy testing.dat
		cd ..
	done

done


