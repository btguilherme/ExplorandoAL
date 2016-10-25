/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clustering;

import io.IOArff;
import moa.cluster.Clustering;
import moa.clusterers.streamkm.StreamKM;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

/**
 *
 * @author guilherme
 */
public class ClusteringTest {

    public static void main(String[] args) throws Exception {
        
        ArffLoader loader = new ArffLoader();
        loader.setURL("/media/guilherme/Arquivos/NetBeansProjects/"
                + "ExplorandoAL/bases/ecoli_no_string_att_number_class.arff");
        
        Instances dataset = loader.getStructure();
        
        StreamKM streamKM = new StreamKM();

        streamKM.numClustersOption.setValue(5);
        streamKM.widthOption.setValue(100000);
        streamKM.prepareForUse();
        
        
        
        
        for (int i = 0; i < dataset.numInstances(); i++) {
            //streamKM.trainOnInstanceImpl((Instance) dataset.instance(i));
        }

        Clustering result = streamKM.getClusteringResult();
        System.out.println("size = " + result.size());
        System.out.println("dimension = " + result.dimension());
    }


}
