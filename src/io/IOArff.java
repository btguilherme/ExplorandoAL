/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.RemoveType;

/**
 *
 * @author guilherme
 */
public class IOArff implements IIO {

    @Override
    public List<String> open(String src) {
        List<String> ret = new ArrayList<>();
        File file = new File(src);
        try {
            Scanner arq = new Scanner(file);
            while (arq.hasNextLine()) {
                String linha = arq.nextLine();
                if (linha.startsWith("%") || linha.equals("")) {
                    //do nothing
                } else {
                    ret.add(linha);
                }
            }
            arq.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IOArff.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    @Override
    public boolean save(String src, String nome, List<String> conteudo) {
        String p = src;
        p = p.concat(nome).concat(".arff");
        File arquivo = new File(p);
        try (FileWriter fw = new FileWriter(arquivo)) {
            for (int i = 0; i < conteudo.size(); i++) {
                fw.write(conteudo.get(i));
                fw.write(System.lineSeparator());
            }
            fw.flush();
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    public Instances openSplit(String src) throws Exception {
        String[] options = new String[2];
        options[0] = "-T";
        options[1] = "string";
        RemoveType remove = new RemoveType();
        remove.setOptions(options);

        BufferedReader loader = new BufferedReader(new FileReader(src));//carrega arquivo de treino em memoria
        Instances _instancias = new Instances(loader); //conjunto de aprendizado (treino)
        remove.setInputFormat(_instancias);
        Instances instancias = Filter.useFilter(_instancias, remove);

        return instancias;
    }

    public boolean saveArffFile(Instances arff, String fileName) {
        String p = System.getProperty("user.dir").concat(File.separator).
                concat(fileName + ".arff");
        File arquivo = new File(p);
        try (FileWriter fw = new FileWriter(arquivo)) {
            fw.write(arff.toString());
            fw.flush();
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

}
