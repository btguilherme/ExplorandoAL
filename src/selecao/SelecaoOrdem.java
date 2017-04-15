/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package selecao;

import java.util.List;
import learning.BeanAmostra;
import weka.core.Instances;

/**
 *
 * @author guilherme
 */
public class SelecaoOrdem extends Selecao{

    private Instances novasAmostrasDeFronteiraSelcionadas;
    List<BeanAmostra> fronteiras;
    private boolean isFronteiraEmpty = false;

    public SelecaoOrdem(List<BeanAmostra> fronteiras) {
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
        for (int i = 0; i < numFronteira; i++) {
            BeanAmostra amostra = fronteiras.get(i);
            raizes.add(amostra.getAmostra());
            novasAmostrasDeFronteiraSelcionadas.add(amostra.getAmostra());
        }
        for (int i = numFronteira - 1; i >= 0; i--) {
            fronteiras.remove(i);
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
