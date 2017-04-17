/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splitter;

import io.IOArff;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author guilherme
 */
public class Splitter {

    private static List<String> data;
    private static List<String> att;
    private static List<String> classes;
    public static List<String> cabecalho;
    private static List<String> conjTreinamento;
    private static List<String> conjTeste;
    private static String name1;
    private static String name2;
    
    
    public static void main(String[] args) {
        if(args.length < 4){
            usage();
            System.exit(0);
        }
        IOArff io = new IOArff();
        
        name1 = args[2];
        name2 = args[3];
        
        split(io.open(args[0]), Double.parseDouble(args[1]));
    }
    
    private static void usage(){
        System.out.println("Program that generates training and test sets in arff format");
        System.out.println("");
        System.out.println("usage java -jar Splitter.jar <P1> <P2>");
        System.out.println("P1: input dataset in the ARFF file format");
        System.out.println("P2: percentage for the training set size [0,100]");
        System.out.println("P3: name file 1 (training set)");
        System.out.println("P4: name file 2 (testing set)");
        System.out.println("");
    }
    
    public static void split(List<String> arquivoOriginal, double porcentagemTreino) {

        data = new ArrayList<>();
        att = new ArrayList<>();
        classes = new ArrayList<>();
        cabecalho = new ArrayList<>();

        dados(arquivoOriginal);

        List<List<String>> classesSeparadas = separaClasses();

        _split(classesSeparadas, porcentagemTreino);
        
        IOArff io = new IOArff();
        io.save(System.getProperty("user.dir").concat(File.separator), name1, conjTreinamento);
        io.save(System.getProperty("user.dir").concat(File.separator), name2, conjTeste);
        
        classesSeparadas.clear();
    }

    private static void _split(List<List<String>> classesSeparadas, double porcentagemTreino) {
        conjTreinamento = new ArrayList<>();
        conjTeste = new ArrayList<>();

        for (int i = 0; i < cabecalho.size(); i++) {
            conjTeste.add(cabecalho.get(i));
            conjTreinamento.add((cabecalho.get(i)));
        }
        
        for (int i = 0; i < classesSeparadas.size(); i++) {
            List<String> classe = classesSeparadas.get(i);
            
            int quantidadeAmostras = (int) Math.round(classe.size() * (porcentagemTreino / 100));
            
            Collections.shuffle(classe);     
            
            for (int j = 0; j < quantidadeAmostras; j++) {
                conjTreinamento.add(classe.get(j));
            }
            
            for (int j = quantidadeAmostras; j < classe.size(); j++) {
                conjTeste.add(classe.get(j));
            }
            
        }
        
    }

    private static void dados(List<String> arquivoOriginal) {
        //pega so as informacoes
        for (int i = 0; i < arquivoOriginal.size(); i++) {

            if (arquivoOriginal.get(i).startsWith("@RELATION") || arquivoOriginal.get(i).startsWith("@relation")) {
                cabecalho.add(arquivoOriginal.get(i));
            }
            
            if (arquivoOriginal.get(i).startsWith("@ATTRIBUTE") || arquivoOriginal.get(i).startsWith("@attribute")) {
                cabecalho.add(arquivoOriginal.get(i));
                String[] aux = arquivoOriginal.get(i).split(" ");
 
                //recupera os nomes das classes
                if (aux[1].equals("class") || aux[1].equals("CLASS")
                        || aux[1].equals("classe") || aux[1].equals("CLASSE")
                        || aux[1].equals("classes") || aux[1].equals("CLASSES")) {
                    //é classe
                    String[] aux2 = arquivoOriginal.get(i).split(" ");
                    String[] aux3 = aux2[2].split(",");

                    for (int j = 0; j < aux3.length; j++) {
                        if (j == 0) {
                            //retira a abertura de chaves
                            classes.add(aux3[j].substring(1));
                        } else if (j == aux3.length - 1) {
                            //retira o fechamento de chaves
                            classes.add(aux3[j].substring(0, aux3[j].length() - 1));
                        } else {
                            //classes que estao entre a primeira e ultima classe
                            classes.add(aux3[j]);
                        }
                    }
                } else {
                    att.add(aux[1]);
                }
            }
            
            if (arquivoOriginal.get(i).equals("@DATA") || arquivoOriginal.get(i).equals("@data")) {
                for (int j = i + 1; j < arquivoOriginal.size(); j++) {
                    if (!arquivoOriginal.get(j).startsWith("%")) {
                        data.add(arquivoOriginal.get(j));
                    }
                }
            }
        }
        cabecalho.add("@DATA");
    }

    private static List<List<String>> separaClasses() {
        List<List<String>> classesSeparadas = new ArrayList<>();

        for (int i = 0; i < classes.size(); i++) {
            List<String> umaClasse = new ArrayList<>();
            for (int j = 0; j < data.size(); j++) {
                if (data.get(j).endsWith(classes.get(i))) {
                    umaClasse.add(data.get(j));
                }
            }
            classesSeparadas.add(umaClasse);
        }

        return classesSeparadas;
    }
    
    public static void setClasses(List<String> classes) {
        Splitter.classes = classes;
    }

    public static List<String> getConjTreinamento() {
        return conjTreinamento;
    }

    public static List<String> getConjTeste() {
        return conjTeste;
    }

    public static List<String> getClasses() {
        return classes;
    }
}
