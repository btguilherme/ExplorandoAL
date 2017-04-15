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
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import learning.MapUtil;
import utils.RemoveAttClass;
import weka.clusterers.AbstractClusterer;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author guilherme
 *
 * Realiza o agrupamento do conjunto Z2. Seleciona amostras de cada cluster,
 * aleatoriamente.
 *
 *
 * Alajlan N, Pasolli E, Melgani F, Franzoso A. Large-Scale Image Classification
 * Using Active Learning. IEEE GEOSCI REMOTE S. 2014;11 (1):259-263.
 * https://ieeexplore.ieee.org/iel7/8859/6658878/06544206.pdf
 * https://www.google.com.br/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&ved=0ahUKEwjgwrLSu5zTAhXGW5AKHe52DSsQFggmMAA&url=http%3A%2F%2Fwww.teses.usp.br%2Fteses%2Fdisponiveis%2F55%2F55134%2Ftde-14092015-100714%2Fpublico%2FDissertacao_FabioJorge_revisada.pdf&usg=AFQjCNGOMLZ_CCXkGWgWO9PJYFHe-lny8A
 *
 */
public class Clu extends Selecao {

    private Instances z2, z2SemRotulo, z2ComRotulo, novasAmostrasDeFronteiraSelcionadas;
    private final AbstractClusterer clusterer;
    private List<Instances> listas;

    public Clu(AbstractClusterer clusterer, Instances z2) {
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

        boolean jump = false;

        for (int i = 0; i < raizesSemRotulos.numInstances(); i++) {

            for (int j = 0; j < raizes.numInstances(); j++) {
                if (raizes.instance(j).toString().contains(raizesSemRotulos.instance(i).toString())) {
                    raizesRetorno.add(raizes.instance(j));
                    jump = true;
                    break;
                }
            }

            if (!jump) {
                for (int j = 0; j < z2.numInstances(); j++) {
                    if (z2.instance(j).toString().contains(raizesSemRotulos.instance(i).toString())) {
                        raizesRetorno.add(z2.instance(j));
                        break;
                    }
                }
            }
            jump = false;
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

            listas.add(temp);

        }

    }

    private Instances selecao(Instance amostraRaiz, Instances temp) {

        for (int i = z2SemRotulo.numInstances() - 1; i >= 0; i--) {
            int clusterRaiz = 0, clusterAmostra = 0;
            try {
                clusterRaiz = clusterer.clusterInstance(amostraRaiz);
                clusterAmostra = clusterer.clusterInstance(z2SemRotulo.instance(i));
            } catch (Exception ex) {
                Logger.getLogger(Clu.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (clusterAmostra == clusterRaiz) {
                temp.add(z2SemRotulo.instance(i));
                z2ComRotulo.delete(i);
                z2SemRotulo.delete(i);
            }
        }

        return temp;
    }

    private Instances selecaoRetorno(Instances raizes, int numAmostras) {

        int contAmostras = 0;
        int contListas = 0;
        int indiceLista = 0;

        do {
            Instances temp = listas.get(indiceLista);

            if (temp.numInstances() > 0) {
                contListas = 0;

                Random rand = new Random();
                int indiceRand = rand.nextInt(temp.numInstances());

                Instance amostraSelecionada = temp.instance(indiceRand);

                novasAmostrasDeFronteiraSelcionadas.add(amostraSelecionada);
                raizes.add(amostraSelecionada);
                temp.delete(indiceRand);
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

                if (z2.instance(j).toString().contains(novasAmostrasDeFronteiraSelcionadas.instance(i).toString())) {
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
