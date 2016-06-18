/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import classificar.ClassificadorOPF;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author guilherme
 */
public class RunCommand {

    public static void runCommand(String comando) {

//        String[] split = comando.split(" ");
//        List<String> args = new ArrayList<>();
//        args.addAll(Arrays.asList(split));
//        System.err.println(" running: "+comando);
//        try {
//            Process process = new ProcessBuilder(split)
//                    .redirectErrorStream(false)
//                    //.redirectOutput(new File(System.getProperty("user.dir").concat(File.separator).concat("errors.txt")))
//                    .start();
//
//            BufferedReader br = new BufferedReader(
//                    new InputStreamReader(process.getInputStream()));
//            
//            String line = null;
//            while ( (line = br.readLine()) != null )
//                System.err.println(line);
//            
//            System.err.println("finished: "+comando);
//
//        } catch (IOException ex) {
//            Logger.getLogger(RunCommand.class.getName()).log(Level.SEVERE, null, ex);
//        }
        int exitVal;
        try {
            do {

                Runtime rt = Runtime.getRuntime();
                Process proc = rt.exec(comando);
                InputStream stderr = proc.getErrorStream();
                InputStreamReader isr = new InputStreamReader(stderr);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                //System.out.println("<ERROR>");
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
                //System.out.println("</ERROR>");
                exitVal = proc.waitFor();
                
                if(exitVal != 0){
                    System.err.println("repetindo comando "+comando);
                }
                
                
            } while (exitVal != 0);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }
}
