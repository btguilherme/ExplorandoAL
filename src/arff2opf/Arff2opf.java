/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arff2opf;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author guilherme
 */
public class Arff2opf {

    private static Info info;
    private static boolean finished = false;

    public static void main(String[] args) {
        
        if (args.length < 2) {
            showUsage();
            System.exit(0);
        }

        info = new Info();

        List<String> arff = IO.abreArquivo(args[0]);

        List<String> data = getData(arff);

        List<String> opf = makeOPF(data);

        IO.salvaEmDisco(opf, args[1]);
        
        finished = true;
        
    }
    
    public static boolean isFinished(){
        return finished;
    }

    private static List<String> getData(List<String> arff) {
        List<String> data = new ArrayList<>();
        for (String arff1 : arff) {
            if (arff1.contains("@") || arff1.contains("%")) {
                if (arff1.contains("{")) {
                    info.setNumLabels(arff1.split(",").length);
                    mapsLabels(arff1);
                }
            } else {
                data.add(arff1);
            }
        }
        info.setNumSamples(data.size());
        return data;
    }

    private static List<String> makeOPF(List<String> data) {
        List<String> opf = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            String[] line = data.get(i).split(",");

            if (i == 0) {
                info.setNumFeatures(line.length - 1);
                opf.add(String.valueOf(info.getNumSamples()) + " "
                        + String.valueOf(info.getNumLabels()) + " "
                        + String.valueOf(info.getNumFeatures()));
            }
            
            String lineOPF = String.valueOf(i) + " " + info.getLabels().
                    get(line[line.length - 1]);
            for (int j = 0; j < line.length - 1; j++) {
                lineOPF = lineOPF.concat(" " + line[j]);
            }
            opf.add(lineOPF);
        }
        return opf;
    }

    private static void showUsage() {
        System.err.println("Conversor de ARFF para OPF (ASCII).");
        System.err.println("Uso: arff2opf <P1> <P2>");
        System.err.println("\tP1: Arquivo no formato ARFF");
        System.err.println("\tP2: Nome do arquivo de saída no formato OPF\n");
    }

    private static void mapsLabels(String arff) {
        String[] s1 = arff.split("\\{");
        String[] s2 = s1[1].split("}");
        String[] classes = s2[0].split(",");
        
        for (int i = 0; i < classes.length; i++) {
            info.getLabels().put(classes[i], (i+1));
        }
    }

}
