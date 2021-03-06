/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package selecao;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import learning.BeanAmostra;
import learning.Learn;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author guilherme
 */
public class SelecaoListas extends Selecao {

    private Instances novasAmostrasDeFronteiraSelcionadas;
    private List<BeanAmostra> fronteiras;
    private boolean isFronteiraEmpty = false;
    private int indiceSelecaoPorLista = 0;
    private Object[] keys;
    private HashMap<String, Instances> listas;
    private boolean isFirstTime = true;

    public SelecaoListas(List<BeanAmostra> fronteiras) {
        this.fronteiras = fronteiras;
    }

    @Override
    public Instances seleciona(int numAmostras, Instances raizes) {

        int numFronteira = numAmostras;
        novasAmostrasDeFronteiraSelcionadas = new Instances(raizes);
        novasAmostrasDeFronteiraSelcionadas.delete();

        if (numFronteira > fronteiras.size()) {
            numFronteira = fronteiras.size() - 1;
            isFronteiraEmpty = true;
        }

        if (isFirstTime) {
            listas = new HashMap<>();
            Enumeration<Object> classes = raizes.attribute(raizes.numAttributes()-1).enumerateValues();

            //cria a estrutura da lista
            while (classes.hasMoreElements()) {
                Instances is = new Instances(raizes);
                is.delete();
                listas.put((String) classes.nextElement(), is);
            }

            //popula a estrutura com as amostras de fronteira, separadas 
            //por cada classe
            for (int j = 0; j < fronteiras.size(); j++) {
                Instance amostraTemp = fronteiras.get(j).getAmostra();
                String classeRealAmostraTemp = amostraTemp.toString(amostraTemp.numAttributes() - 1);
                Instances temp = listas.get(classeRealAmostraTemp);
                temp.add(amostraTemp);
                listas.put(classeRealAmostraTemp, temp);
            }

            keys = listas.keySet().toArray();
            isFirstTime = false;
        }

//        //
//        for (int i = 0; i < keys.length; i++) {
//            String key = (String)keys[i];
//            System.out.println(key);
//            
//            Instances values = listas.get(key);
//            
//            for (int j = 0; j < values.numInstances(); j++) {
//                System.out.println("\t"+values.instance(j));
//            }
//            System.out.println("");
//            
//        }
//        System.exit(0);
        //
        //init  - seleção sem classificador
//        for (int i = 0; i < numFronteira; i++) {
//
//            //controle da variavel 'index' (evitar null pointer)
//            if (listas.get((String) keys[indiceSelecaoPorLista]).numInstances() == 0) {
//                do {
//                    if (indiceSelecaoPorLista == keys.length - 1) {
//                        indiceSelecaoPorLista = 0;
//                    } else {
//                        indiceSelecaoPorLista++;
//                    }
//                } while (listas.get((String) keys[indiceSelecaoPorLista]).numInstances() == 0);
//            }
//
//            Instances aux = listas.get((String) keys[indiceSelecaoPorLista]);
//            BeanAmostra amostra = new BeanAmostra(aux.instance(0), 0.0, 0);
//            aux.delete(0);
//            listas.put((String) keys[indiceSelecaoPorLista], aux);
//            raizes.add(amostra.getAmostra());
//
//            //
//            //System.out.println("Amostra selecionada: "+amostra.getAmostra().toString());
//            //
//            novasAmostrasDeFronteiraSelcionadas.add(amostra.getAmostra());
//
//            if (indiceSelecaoPorLista == keys.length - 1) {
//                indiceSelecaoPorLista = 0;
//            } else {
//                indiceSelecaoPorLista++;
//            }
//        }
        //end  - seleção sem classificador
        
        
//        System.out.println("Amostras selecionadas sem classificador");
//        for (int i = 0; i < novasAmostrasDeFronteiraSelcionadas.numInstances(); i++) {
//            System.out.println(novasAmostrasDeFronteiraSelcionadas.instance(i));
//        }
//        novasAmostrasDeFronteiraSelcionadas.clear();
        
        
        
        
        //init - seleção com classificador
        Classifier classificador = Learn.getClassificador();
        for (int i = 0; i < numFronteira; i++) {

            //controle da variavel 'index' (evitar null pointer)
            if (listas.get((String) keys[indiceSelecaoPorLista]).numInstances() == 0) {
                do {
                    if (indiceSelecaoPorLista == keys.length - 1) {
                        indiceSelecaoPorLista = 0;
                    } else {
                        indiceSelecaoPorLista++;
                    }
                } while (listas.get((String) keys[indiceSelecaoPorLista]).numInstances() == 0);
            }

            Instances aux = listas.get((String) keys[indiceSelecaoPorLista]);

            for (int j = 0; j < aux.numInstances(); j++) {
                try {
                    double pred = classificador.classifyInstance(aux.instance(j));
                    String actual = aux.classAttribute().value((int) aux.instance(j).classValue());
                    String predicted = aux.classAttribute().value((int) pred);
                    
                    //se os labels forem distintos ou se for a ultima amostra do conjunto...
                    if(!actual.equals(predicted) || (aux.numInstances() - 1 == j)){
                        BeanAmostra amostra = new BeanAmostra(aux.instance(j), 0.0, 0);
                        aux.delete(j);
                        listas.put((String) keys[indiceSelecaoPorLista], aux);
                        raizes.add(amostra.getAmostra());
                        novasAmostrasDeFronteiraSelcionadas.add(amostra.getAmostra());
                        break;
                    }
                } catch (Exception ex) {
                    Logger.getLogger(SelecaoListas.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if (indiceSelecaoPorLista == keys.length - 1) {
                indiceSelecaoPorLista = 0;
            } else {
                indiceSelecaoPorLista++;
            }
        }
        //end - seleção com classificador

        //apaga as amostras selecionadas do conjunto de fronteira
        for (int i = 0; i < novasAmostrasDeFronteiraSelcionadas.size(); i++) {
            String novaAmostra = novasAmostrasDeFronteiraSelcionadas.get(i).toString();
            for (int j = fronteiras.size() - 1; j >= 0; j--) {
                String amostraFronteira = fronteiras.get(j).getAmostra().toString();
                if (novaAmostra.equals(amostraFronteira)) {
                    fronteiras.remove(j);
                }
            }
        }

        return raizes;
    }

    @Override
    public boolean isEmpty() {
        return this.isFronteiraEmpty;
    }

    @Override
    public Instances getAmostrasSelecionadas() {
        return this.novasAmostrasDeFronteiraSelcionadas;
    }

}
