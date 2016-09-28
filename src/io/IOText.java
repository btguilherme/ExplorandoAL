/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author guilherme
 */
public class IOText implements IIO {

    @Override
    public List<String> open(String src) {
        List<String> ret = new ArrayList<>();
        File file = new File(src);
        try {
            Scanner arq = new Scanner(file);
            while (arq.hasNextLine()) {
                ret.add(arq.nextLine());
            }
            arq.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IOArff.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    @Override
    public boolean save(String dst, String nome, List<String> conteudo) {
        String p = dst;
        p = p.concat(nome).concat(".txt");
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

    @Override
    public boolean save(String dst, String nome, String conteudo) {
        String p = dst;
        p = p.concat(nome).concat(".txt");
        File arquivo = new File(p);
        FileWriter fw;
        try {
            fw = new FileWriter(arquivo);
            fw.write(conteudo);
            fw.flush();
        } catch (IOException ex) {
            return false;
        }
            
        return true;
    }

}
