/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arff2opf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author guilherme
 */
public class IO {

    public static List<String> abreArquivo(String path) {
        List<String> ret = new ArrayList<>();
        File file = new File(path);
        try {
            Scanner arq = new Scanner(file);
            while (arq.hasNextLine()) {
                String linha = arq.nextLine();
                if(linha.startsWith("%") || linha.equals("")){
                    //do nothing
                }else{
                    ret.add(linha);
                }
            }
            arq.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return ret;
    }

    public static void salvaEmDisco(List<String> dados, String nome) {
        
//        String p = System.getProperty("user.dir").concat(File.separator).
//                concat(nome).concat(".opf");
        
        String p = nome +".opf";
        
        File arquivo = new File(p);
        try (FileWriter fw = new FileWriter(arquivo)) {
            for (int i = 0; i < dados.size(); i++) {
                fw.write(dados.get(i));
                fw.write(System.lineSeparator());
            }
            fw.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
