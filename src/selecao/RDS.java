/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package selecao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import learning.MapUtil;
import utils.RemoveAttClass;
import weka.clusterers.AbstractClusterer;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

/**
 *
 * @author guilherme
 * 
 * 
 * Realiza o agrupamento do conjunto Z2.
 * A partir do agrupamento, as amostras raízes são selecionadas para fazer parte
 * da primeira instância do classificador.
 * São criadas 'c' listas, representando cada um dos clusters criados
 * Cada lista é composta por amostras daquele cluster.
 * As amostras são organizadas de acordo com a distância entre uma dada amostra
 * até a raiz que a representa (ordem decrescente de distância).
 * 
 * 
 * SAITO, P. T. M. et al. Robust active learning for the diagnosis of parasites.  Pattern  Recognition.  2015.
 * http://www.sciencedirect.com/science/article/pii/S0031320315001995
 * 
 */
public class RDS extends Selecao {

    private AbstractClusterer clusterer;
    private Instances z2, z2SemRotulo, z2ComRotulo, novasAmostrasDeFronteiraSelcionadas;
    private List<Instances> listas;

    public RDS(AbstractClusterer clusterer, Instances z2) {
        this.clusterer = clusterer;
        this.z2ComRotulo = new Instances(z2);
        this.z2 = new Instances(z2);
        this.z2SemRotulo = RemoveAttClass.removeAtributoClasse(z2);
        this.listas = new ArrayList<>();
    }

    @Override
    public Instances seleciona(int numAmostras, Instances raizes) {

        novasAmostrasDeFronteiraSelcionadas = new Instances(raizes);
        novasAmostrasDeFronteiraSelcionadas.delete();
        
        Instances raizesSemRotulos = RemoveAttClass.removeAtributoClasse(raizes);

        if (listas.isEmpty()) {
            selecaoOrganizacao(raizesSemRotulos);
        }
 
        raizesSemRotulos = selecaoRetorno(raizesSemRotulos, numAmostras);
        
        Instances raizesRetorno = new Instances(raizes);
        raizesRetorno.delete();
        
        for (int i = 0; i < raizesSemRotulos.numInstances(); i++) {
//            for (int j = 0; j < raizes.numInstances(); j++) {
//                if (raizes.instance(j).toString().contains(raizesSemRotulos.instance(i).toString())) {
//                    raizesRetorno.add(raizes.instance(j));
//                    break;
//                }
//            }
            for (int j = 0; j < z2.numInstances(); j++) {
                if(z2.instance(j).toString().contains(raizesSemRotulos.instance(i).toString())){
                    raizesRetorno.add(z2.instance(j));
                    break;
                }
            }
        }
        
        return raizesRetorno;
    }

    private void selecaoOrganizacao(Instances raizes) {
        listas = new ArrayList<>();

        for (int i = 0; i < raizes.numInstances(); i++) {
            Instances temp = new Instances(raizes);
            temp.delete();
            Instance amostraRaiz = raizes.instance(i);

            //Selecao
            temp = selecao(amostraRaiz, temp);

            //Organização
            organizacao(amostraRaiz, temp);
        //System.exit(0);
        }
        
    }

    private Instances selecao(Instance amostraRaiz, Instances temp) {
        for (int j = z2SemRotulo.numInstances() - 1; j >= 0; j--) {
            int clusterRaiz = 0, clusterAmostra = 0;
            try {
                clusterRaiz = clusterer.clusterInstance(amostraRaiz);
                clusterAmostra = clusterer.clusterInstance(z2SemRotulo.instance(j));
            } catch (Exception ex) {
                Logger.getLogger(RDS.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (clusterAmostra == clusterRaiz) {
                temp.add(z2SemRotulo.instance(j));
                z2ComRotulo.delete(j);
                z2SemRotulo.delete(j);
            }
        }
        
        
        return temp;
    }

    private void organizacao(Instance amostraRaiz, Instances temp) {
        Map<Instance, Double> mapa = new HashMap<>();

        for (Instance temp1 : temp) {

            EuclideanDistance eucl = new EuclideanDistance(temp);

            double dist = eucl.distance(amostraRaiz, temp1);
            mapa.put(temp1, dist);
        }
        mapa = MapUtil.sortByValue(mapa);

        int cont = temp.size() - 1;
        for (Map.Entry<Instance, Double> entrySet : mapa.entrySet()) {
            Instance key = entrySet.getKey();
            temp.set(cont, key);
            cont--;
        }
        
        

        listas.add(temp);
    }

    private Instances selecaoRetorno(Instances raizes, int numAmostras) {
        
        int contAmostras = 0;
        int contListas = 0;
        int indiceLista = 0;

        do {
            Instances temp = listas.get(indiceLista);

            if (temp.numInstances() > 0) {
                contListas = 0;
            
                novasAmostrasDeFronteiraSelcionadas.add(temp.instance(0));
                raizes.add(temp.instance(0));
                temp.delete(0);
                listas.set(indiceLista, temp);
                
                contAmostras++;
                if (contAmostras == numAmostras) {
                    break;
                }
            } else {
                contListas++;
            }

            if (contListas == listas.size() - 1) {
                break;
            }

            if (indiceLista == listas.size() - 1) {
                indiceLista = 0;
            } else {
                indiceLista++;
            }

        } while (contAmostras != numAmostras || contListas != listas.size());

        for (int i = 0; i < novasAmostrasDeFronteiraSelcionadas.numInstances(); i++) {
            for (int j = 0; j < z2.numInstances(); j++) {
                
                if(z2.instance(j).toString().contains(novasAmostrasDeFronteiraSelcionadas.instance(i).toString())){
                    novasAmostrasDeFronteiraSelcionadas.set(i, z2.instance(j));
                }
            }
        }
    
        return raizes;

    }

    @Override
    public boolean isEmpty() {
        for (Instances lista : listas) {
            if (lista.size() > 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Instances getAmostrasSelecionadas() {
        return this.novasAmostrasDeFronteiraSelcionadas;
    }

    
}
