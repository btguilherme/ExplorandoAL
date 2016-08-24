/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package selecao;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import learning.BeanAmostra;
import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author guilherme
 */
public class SelecaoListas extends Selecao{
    
    private Instances novasAmostrasDeFronteiraSelcionadas;
    List<BeanAmostra> fronteiras;
    private boolean isFronteiraEmpty = false;
    private int indiceSelecaoPorLista = 0;

    public SelecaoListas(List<BeanAmostra> fronteiras) {
        this.fronteiras = fronteiras;
    }
    
    @Override
    public Instances seleciona(SimpleKMeans clusterer, Instances raizes) {

        int numFronteira = clusterer.getNumClusters();
        novasAmostrasDeFronteiraSelcionadas = new Instances(raizes);
        novasAmostrasDeFronteiraSelcionadas.delete();

        if (numFronteira > fronteiras.size()) {
            numFronteira = fronteiras.size() - 1;
            isFronteiraEmpty = true;
        }

        HashMap<String, Instances> listas = new HashMap<>();
        Enumeration<Object> classes = raizes.attribute("class").enumerateValues();

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

        Object[] keys = listas.keySet().toArray();
        
        //
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
//        //
        
        
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
            BeanAmostra amostra = new BeanAmostra(aux.instance(0), 0.0, 0);
            aux.delete(0);
            listas.put((String) keys[indiceSelecaoPorLista], aux);
            raizes.add(amostra.getAmostra());
            
            //
            //System.out.println("Amostra selecionada: "+amostra.getAmostra().toString());
            //
            novasAmostrasDeFronteiraSelcionadas.add(amostra.getAmostra());

            if (indiceSelecaoPorLista == keys.length - 1) {
                indiceSelecaoPorLista = 0;
            } else {
                indiceSelecaoPorLista++;
            }
        }

        for (int i = 0; i < novasAmostrasDeFronteiraSelcionadas.size(); i++) {
            String novaAmostra = novasAmostrasDeFronteiraSelcionadas.get(i).toString();
            for (int j = fronteiras.size() - 1; j >= 0; j--) {
                String amostraFronteira = fronteiras.get(j).getAmostra().toString();
                if(novaAmostra.equals(amostraFronteira)){
                    fronteiras.remove(j);
                }
                
            }
        }
        
        return raizes;
    }
    
    @Override
    public boolean isEmpty(){
        return this.isFronteiraEmpty;
    }

    @Override
    public Instances getAmostrasSelecionadas() {
        return this.novasAmostrasDeFronteiraSelcionadas;
    }
    
    
    
}
