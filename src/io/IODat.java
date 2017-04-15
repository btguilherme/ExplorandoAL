/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author guilherme
 */
public class IODat implements IIO{

    @Override
    public List<String> open(String src) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean save(String dst, String nome, List<String> conteudo) {
        String p = dst;
        p = p.concat(nome).concat(".dat");
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
        p = p.concat(nome).concat(".dat");
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
