/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author guilherme
 */
public class RunCommand {

    public static void runCommand(String comando) {
        int exitVal;
        try {
            do {
                Runtime rt = Runtime.getRuntime();
                Process proc = rt.exec(comando);
                InputStream stderr = proc.getErrorStream();
                InputStreamReader isr = new InputStreamReader(stderr);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
                exitVal = proc.waitFor();
                if (exitVal != 0) {
                    System.err.println("repetindo comando " + comando);
                }
            } while (exitVal != 0);
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(RunCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
