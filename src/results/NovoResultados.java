/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package results;

import io.IODat;
import io.IOText;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author guilherme
 */
public class NovoResultados {
    
    private static boolean gerarDat = true;
    private static String datHeader;

    public static void main(String[] args) throws InterruptedException, FileNotFoundException, IOException {
        Properties props = new Properties();
        FileInputStream file
                = new FileInputStream(System.getProperty("user.dir").concat(File.separator)
                        + "/src/properties/propriedades.properties");
        props.load(file);

        String OPC_APRENDIZADO = props.getProperty("prop.aprendizado");
        String ORDENACAO = props.getProperty("prop.ordenacao");
        String CLASSIFICADOR = props.getProperty("prop.classificador");
        String METODOSELECAO = props.getProperty("prop.selecaoFronteira");
        String AGRUPAMENTO = props.getProperty("prop.agrupamento");

        String[] aprendizado = OPC_APRENDIZADO.split(" ");
        String[] ordenacao = ORDENACAO.split(" ");
        String[] selecao = METODOSELECAO.split(" ");
        String[] classificadores = CLASSIFICADOR.split(" ");
        String[] agrupamentos = AGRUPAMENTO.split(" ");

        for (int i = 0; i < aprendizado.length; i++) {
            for (int l = 0; l < classificadores.length; l++) {
                
                loop_act:
                for (int j = 0; j < ordenacao.length; j++) {
                    for (int k = 0; k < selecao.length; k++) {

                        for (int m = 0; m < agrupamentos.length; m++) {

                            String folderName = props.getProperty("prop.inputNormal").split("/")[props.getProperty("prop.inputNormal").split("/").length - 1].split(".arff")[0]
                                    + "_-_" + aprendizado[i] + "_-_" + agrupamentos[m] + "_-_" + ordenacao[j] + "_-_" + selecao[k] + "_-_" + classificadores[l] + "_-_exec_";

                            String[] params = folderName.split("_-_");

                            System.out.println("Dataset: " + params[0]);
                            System.out.println("Aprendizado: " + params[1]);
                            System.out.println("Agrupamento: " + params[2]);
                            System.out.println("Organização amostras de fronteira: " + params[3]);
                            System.out.println("Seleção das amostras de fronteira: " + params[4]);
                            System.out.println("Classificador: " + params[5]);
                            
                            datHeader = params[0]+"_"+params[1]+"_"+params[2]+"_"+params[3]+"_"+params[4]+"_"+params[5];

                            calc(folderName);
                            Thread.sleep(50);
                            if (aprendizado[i].equals("rand")) {
                                break loop_act;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Método responsavel por calcular a média e desvio padrao da acurácia,
     * tempo de teste, tempo de treinamento, tempo de seleção, classes
     * conhecidas e quantidade de amostras corrigidas.
     *
     * @param folderName Nome da pasta onde os resultados estão armazenados. Por
     * exemplo, <i>opfsuper</i>, <i>svmcross</i>, <i>YATSI_results</i> etc
     */
    public static void calc(String folderName) {

        //caminho para a pasta com os resultados
        //lista os arquivos da pasta root
        int contaExecucoes = verificaQntdPastas(
                System.getProperty("user.dir").concat(File.separator),
                folderName);

        int contaIteracoes = considerarIteracoes(folderName);
        
        List<Double> sumAccs = new ArrayList<>();
        List<Double> sumTTests = new ArrayList<>();
        List<Double> sumTTrains = new ArrayList<>();
        List<Double> sumTSelecs = new ArrayList<>();
        List<Double> sumKnowns = new ArrayList<>();
        List<Double> sumWrongClassifs = new ArrayList<>();
        List<Double> sumPctErroUnlab = new ArrayList<>();

        List<List<Double>> todosValoresAcc = new ArrayList<>();
        List<List<Double>> todosValoresTTest = new ArrayList<>();
        List<List<Double>> todosValoresTTrain = new ArrayList<>();
        List<List<Double>> todosValoresTSelec = new ArrayList<>();
        List<List<Double>> todosValoresKnown = new ArrayList<>();
        List<List<Double>> todosValoresWrongClassif = new ArrayList<>();
        List<List<Double>> todosValoresPctErroUnlab = new ArrayList<>();

        for (int i = 0; i < contaExecucoes; i++) {

            String pathExec = System.getProperty("user.dir").concat(File.separator)
                    .concat(folderName + "" + i).concat(File.separator);

            List<Double> accsIt = new ArrayList<>();
            List<Double> tTestsIt = new ArrayList<>();
            List<Double> tTrainsIt = new ArrayList<>();
            List<Double> tSelecsIt = new ArrayList<>();
            List<Double> knownIt = new ArrayList<>();
            List<Double> wrongClassifIt = new ArrayList<>();
            List<Double> pctErroUnlabIt = new ArrayList<>();

            for (int j = 0; j < contaIteracoes; j++) {

                String pathIt = pathExec + "it" + j + File.separator;

                IOText io = new IOText();

                double acc, tSelec, tTest, tTrain, knownClasses, wrongClassif, pctErroUnlab;

                acc = Double.valueOf(io.open(pathIt + "acc.txt").get(0));
                tSelec = Double.valueOf(io.open(pathIt + "tempoSelecao.txt").get(0));
                tTest = Double.valueOf(io.open(pathIt + "tempoTeste.txt").get(0));
                tTrain = Double.valueOf(io.open(pathIt + "tempoTreino.txt").get(0));
                knownClasses = Double.valueOf(io.open(pathIt + "classesConhecidas.txt").get(0));
                wrongClassif = Double.valueOf(io.open(pathIt + "classificadasErradas.txt").get(0).split("/")[0]);
                pctErroUnlab = Double.valueOf(io.open(pathIt + "pctErroPropUnlab.txt").get(0));

                accsIt.add(acc);
                tTestsIt.add(tTest);
                tTrainsIt.add(tTrain);
                tSelecsIt.add(tSelec);
                knownIt.add(knownClasses);
                wrongClassifIt.add(wrongClassif);
                pctErroUnlabIt.add(pctErroUnlab);

                if (i == 0) {
                    sumAccs.add(acc);
                    sumTTests.add(tTest);
                    sumTTrains.add(tTrain);
                    sumTSelecs.add(tSelec);
                    sumKnowns.add(knownClasses);
                    sumWrongClassifs.add(wrongClassif);
                    sumPctErroUnlab.add(pctErroUnlab);
                } else {
                    sumAccs.set(j, (sumAccs.get(j) + acc));
                    sumTTests.set(j, (sumTTests.get(j) + tTest));
                    sumTTrains.set(j, (sumTTrains.get(j) + tTrain));
                    sumTSelecs.set(j, (sumTSelecs.get(j) + tSelec));
                    sumKnowns.set(j, (sumKnowns.get(j) + knownClasses));
                    sumWrongClassifs.set(j, (sumWrongClassifs.get(j) + wrongClassif));
                    sumPctErroUnlab.set(j, (sumPctErroUnlab.get(j) + pctErroUnlab));
                }

            }

            todosValoresAcc.add(accsIt);
            todosValoresTTest.add(tTestsIt);
            todosValoresTTrain.add(tTrainsIt);
            todosValoresTSelec.add(tSelecsIt);
            todosValoresKnown.add(knownIt);
            todosValoresWrongClassif.add(wrongClassifIt);
            todosValoresPctErroUnlab.add(pctErroUnlabIt);

        }

        List<Double> dpAcc = desvioPadrao(contaIteracoes, contaExecucoes, todosValoresAcc, sumAccs);
        List<Double> dpt_test = desvioPadrao(contaIteracoes, contaExecucoes, todosValoresTTest, sumTTests);
        List<Double> dpt_train = desvioPadrao(contaIteracoes, contaExecucoes, todosValoresTTrain, sumTTrains);
        List<Double> dpt_selec = desvioPadrao(contaIteracoes, contaExecucoes, todosValoresTSelec, sumTSelecs);
        List<Double> dpt_Known = desvioPadrao(contaIteracoes, contaExecucoes, todosValoresKnown, sumKnowns);
        List<Double> dpt_WrongClassif = desvioPadrao(contaIteracoes, contaExecucoes, todosValoresWrongClassif, sumWrongClassifs);
        List<Double> dpt_PctErroUnlab = desvioPadrao(contaIteracoes, contaExecucoes, todosValoresPctErroUnlab, sumPctErroUnlab);

        //imprime certo para fazer contas/graficos (medias e desvios separados)
//        System.out.println("AVERAGES");
//        System.out.println("it(#)\tacc(%)\tt_test(s)\tt_train(s)\tt_selec(s)\tknown(#)\twrong(#)\tpctErroUnlab");
//        
//        List<String> conteudoAccs = new ArrayList<>();
//        List<String> conteudoTempTest = new ArrayList<>();
//        List<String> conteudoTempTreino = new ArrayList<>();
//        List<String> conteudoTempSelec = new ArrayList<>();
//        List<String> conteudoPctErroUnlab = new ArrayList<>();
//        
//        for (int i = 0; i < sumAccs.size(); i++) {
//            System.out.printf("%d\t%.2f\t%.6f\t%.6f\t%.6f\t%.2f\t%.2f\t%.2f\n", (i + 1), (sumAccs.get(i) / (contaExecucoes)), (sumTTests.get(i) / (contaExecucoes)), (sumTTrains.get(i) / (contaExecucoes)), (sumTSelecs.get(i) / (contaExecucoes)), (sumKnowns.get(i) / (contaExecucoes)), (sumWrongClassifs.get(i) / (contaExecucoes)), (sumPctErroUnlab.get(i) / (contaExecucoes)));
//            
//            if(gerarDat){
//                conteudoAccs.add(i+1+"\t"+(sumAccs.get(i) / (contaExecucoes))+"\t"+dpAcc.get(i));
//                conteudoTempTest.add(i+1+"\t"+(sumTTests.get(i) / (contaExecucoes))+"\t"+dpt_test.get(i));
//                conteudoTempTreino.add(i+1+"\t"+(sumTTrains.get(i) / (contaExecucoes))+"\t"+dpt_train.get(i));
//                conteudoTempSelec.add(i+1+"\t"+(sumTSelecs.get(i) / (contaExecucoes))+"\t"+dpt_selec.get(i));
//                conteudoPctErroUnlab.add(i+1+"\t"+(sumPctErroUnlab.get(i) / (contaExecucoes))+"\t"+dpt_PctErroUnlab.get(i));
//            }
//        }
//        if(gerarDat){
//            new IODat().save(System.getProperty("user.dir").concat(File.separator), datHeader+"_acc", conteudoAccs);
//            new IODat().save(System.getProperty("user.dir").concat(File.separator), datHeader+"_teste", conteudoTempTest);
//            new IODat().save(System.getProperty("user.dir").concat(File.separator), datHeader+"_treino", conteudoTempTreino);
//            new IODat().save(System.getProperty("user.dir").concat(File.separator), datHeader+"_selec", conteudoTempSelec);
//            new IODat().save(System.getProperty("user.dir").concat(File.separator), datHeader+"_pctErroUnlab", conteudoPctErroUnlab);
//        }
//        
//        System.out.println("");
//        System.out.println("STD DEVS");
//        System.out.println("it(#)\tacc(%)\tt_test(s)\tt_train(s)\tt_selec(s)\tknown(#)\twrong(#)\terroProp(%)");
//        for (int i = 0; i < dpAcc.size(); i++) {
//            System.out.printf("%d\t%.2f\t%.6f\t%.6f\t%.6f\t%.2f\t%.2f\t%.2f\n", (i + 1), dpAcc.get(i), dpt_test.get(i), dpt_train.get(i), dpt_selec.get(i), dpt_Known.get(i), dpt_WrongClassif.get(i), dpt_PctErroUnlab.get(i));
//        }
//        
        
        
        
        //imprime de um jeito bom para ler (media +/- desvio na mesma coluna)
        System.out.println("it(#)\tacc(%)\tt_test(s)\tt_train(s)\tt_selec(s)\tknown(#)\twrong(#)\terroProp(%)");
        for (int i = 0; i < sumAccs.size(); i++) {
            System.out.printf("%d\t%.2f ± %.2f\t%.6f ± %.6f\t%.6f ± %.6f\t%.6f ± %.6f\t%.2f ± %.2f\t%.2f ± %.2f\t%.2f ± %.2f\n",
                    (i + 1),
                    (sumAccs.get(i) / (contaExecucoes)), dpAcc.get(i),
                    (sumTTests.get(i) / (contaExecucoes)), dpt_test.get(i),
                    (sumTTrains.get(i) / (contaExecucoes)), dpt_train.get(i),
                    (sumTSelecs.get(i) / (contaExecucoes)), dpt_selec.get(i),
                    (sumKnowns.get(i) / (contaExecucoes)), dpt_Known.get(i),
                    (sumWrongClassifs.get(i) / (contaExecucoes)), dpt_WrongClassif.get(i),
                    (sumPctErroUnlab.get(i) / (contaExecucoes)), dpt_PctErroUnlab.get(i)
            );
        }
        
        
        
        
        
        
        
        
        

        System.out.println("\n\n");

    }

    private static int verificaQntdPastas(String path, String folder) {
        File file = new File(path);
        File afile[] = file.listFiles();
        int contaExecucoes = 0;
        for (int i = 0; i < afile.length; i++) {
            File arquivos = afile[i];
            if (arquivos.getName().contains(folder)) {
                contaExecucoes++;
            }
        }
        return contaExecucoes;
    }

    private static List<Double> desvioPadrao(int indiceIt, int indiceExec,
            List<List<Double>> todosValores, List<Double> sum) {

        List<Double> dp = new ArrayList<>();
        for (int i = 0; i < indiceIt; i++) {
            double sumPow = 0.0;
            for (int j = 0; j < indiceExec; j++) {
                sumPow += Math.pow((todosValores.get(j).get(i) - (sum.get(i) / indiceExec)  ), 2);
            }
            double var = (sumPow / (indiceExec));
            dp.add(Math.sqrt(var));
        }
        return dp;
    }

    private static int considerarIteracoes(String folderName) {

        int ret = Integer.MAX_VALUE;

        int numExecs = verificaQntdPastas(System.getProperty("user.dir").concat(File.separator), folderName);

        for (int i = 0; i < numExecs; i++) {
            int numIts = verificaQntdPastas(System.getProperty("user.dir")
                    .concat(File.separator).concat(folderName + i)
                    .concat(File.separator), "it");

            if (numIts < ret) {
                ret = numIts;
            }

        }

        return ret;

    }

}
