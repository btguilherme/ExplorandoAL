/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package learning;

import weka.core.Instance;

/**
 *
 * @author guilherme
 */
public class BeanAmostra {
    private Instance amostra;
    private int clusterPredito;
    private int indiceZ2;
    
    public BeanAmostra(){}
    
    public BeanAmostra(Instance amostra, int clusterPredito, int indiceZ2){
        this.amostra = amostra;
        this.clusterPredito = clusterPredito;
        this.indiceZ2 = indiceZ2;
    }

    public Instance getAmostra() {
        return amostra;
    }

    public void setAmostra(Instance amostra) {
        this.amostra = amostra;
    }

    public int getClusterPredito() {
        return clusterPredito;
    }

    public void setClusterPredito(int clusterPredito) {
        this.clusterPredito = clusterPredito;
    }

    public int getIndiceZ2() {
        return indiceZ2;
    }

    public void setIndiceZ2(int indiceZ2) {
        this.indiceZ2 = indiceZ2;
    }
    
    
}
