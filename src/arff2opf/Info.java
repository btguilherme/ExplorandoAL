/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arff2opf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author guilherme
 */
public class Info {

    private int numSamples;
    private int numLabels;
    private int numFeatures;
    private List<String> data;
    private Map<String, Integer> labels;

    public Info(){
        this.data = new ArrayList<>();
        this.labels = new HashMap<>();
    }
    
    public Map<String, Integer> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, Integer> labels) {
        this.labels = labels;
    }

    public int getNumSamples() {
        return numSamples;
    }

    public void setNumSamples(int numSamples) {
        this.numSamples = numSamples;
    }

    public int getNumLabels() {
        return numLabels;
    }

    public void setNumLabels(int numLabels) {
        this.numLabels = numLabels;
    }

    public int getNumFeatures() {
        return numFeatures;
    }

    public void setNumFeatures(int numFeatures) {
        this.numFeatures = numFeatures;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

}
