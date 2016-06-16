/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io;

import java.util.List;

/**
 *
 * @author guilherme
 */
public interface IIO {
    List<String> open(String src);
    boolean save(String src, String nome, List<String> conteudo);
}
