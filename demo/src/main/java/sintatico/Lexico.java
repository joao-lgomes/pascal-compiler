package sintatico;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class Lexico {
    private String caminhoArquivo;
    private String nomeArquivo;
    private int c;
    PushbackReader br;
    BufferedReader initialBr;
    private ArrayList<String> reservedWords = new ArrayList<String>(Arrays.asList(
            "and", "array", "begin", "case", "const", "div",
            "do", "downto", "else", "end", "file", "for",
            "function", "goto", "if", "in", "label", "mod",
            "nil", "not", "of", "or", "packed", "procedure",
            "program", "record", "repeat", "set", "then",
            "to", "type", "until", "var", "while", "with", "real", "integer", "read", "write"));

    public Lexico(String nomeArquivo) {
        this.caminhoArquivo = Paths.get(nomeArquivo).toAbsolutePath().toString();
        this.nomeArquivo = nomeArquivo;

        try {
            this.initialBr = new BufferedReader(new FileReader(caminhoArquivo, StandardCharsets.UTF_8));
            this.br = new PushbackReader(initialBr);
            this.c = this.br.read();
        } catch (IOException err) {
            System.err.println("Não foi possível abrir o arquivo ou ler do arquivo: " + this.nomeArquivo);
            err.printStackTrace();
        }
    }

    public Token getToken(int linha, int coluna) {
        int tamanhoToken = 0;
        int qtdEspacos = 0;
        int e;
        StringBuilder lexema = new StringBuilder("");
        char caractere;
        Token token = new Token();

        try {

            while (c != -1) { // -1 fim da stream
                caractere = (char) c;
                if (!(c == 13 || c == 10)) {
                    if (caractere == ' ') {
                        while (caractere == ' ') {
                            c = this.br.read();
                            qtdEspacos++;
                            caractere = (char) c;
                        }
                    } else if (Character.isLetter(caractere)) {
                        while (Character.isLetter(caractere) || Character.isDigit(caractere)) {
                            lexema.append(caractere);
                            c = this.br.read();
                            tamanhoToken++;
                            caractere = (char) c;
                        }

                        if (returnIfIsReservedWord(lexema.toString())) {
                            token.setClasse(Classe.cPalRes);
                        } else {
                            token.setClasse(Classe.cId);
                        }
                        token.setTamanhoToken(tamanhoToken);
                        token.setColuna(coluna + qtdEspacos);
                        token.setLinha(linha);
                        Valor valor = new Valor(lexema.toString());
                        token.setValor(valor);
                        return token;
                    } else if (Character.isDigit(caractere)) {
                        int numberOfPoints = 0;
                        while (Character.isDigit(caractere) || caractere == '.') {
                            if (caractere == '.') {
                                numberOfPoints++;
                            }
                            lexema.append(caractere);
                            c = this.br.read();
                            tamanhoToken++;
                            caractere = (char) c;
                        }
                        if (numberOfPoints <= 1) {
                            if (numberOfPoints == 0) {
                                token.setClasse(Classe.cInt);
                                Valor valor = new Valor(Integer.parseInt(lexema.toString()));
                                token.setValor(valor);
                            } else {
                                token.setClasse(Classe.cReal);
                                Valor valor = new Valor(Float.parseFloat(lexema.toString()));
                                token.setValor(valor);
                            }

                            // if(numberConverted-Math.round(numberConverted)==0){
                            // token.setClasse(Classe.cInt);
                            // Valor valor = new Valor(Integer.parseInt(lexema.toString()));
                            // token.setValor(valor);
                            // }
                            // else{

                            // }
                            token.setTamanhoToken(tamanhoToken);
                            token.setColuna(coluna + qtdEspacos);
                            token.setLinha(linha);
                            return token;
                        }
                    } else {
                        tamanhoToken++;
                        if (caractere == ':') {
                            int proximo = this.br.read();
                            caractere = (char) proximo;
                            if (caractere == '=') {
                                tamanhoToken++;
                                token.setClasse(Classe.cAtribuicao);
                            } else {
                                this.br.unread(proximo);
                                token.setClasse(Classe.cDoisPontos);
                            }
                        } else if (caractere == '+') {
                            token.setClasse(Classe.cMais);
                        } else if (caractere == '-') {
                            token.setClasse(Classe.cMenos);
                        } else if (caractere == '/') {
                            token.setClasse(Classe.cDivisao);
                        } else if (caractere == '*') {
                            token.setClasse(Classe.cMultiplicacao);
                        } else if (caractere == '>') {
                            int proximo = this.br.read();
                            caractere = (char) proximo;
                            if (caractere == '=') {
                                tamanhoToken++;
                                token.setClasse(Classe.cMaiorIgual);
                            } else {
                                this.br.unread(proximo);
                                token.setClasse(Classe.cMaior);
                            }
                        } else if (caractere == '<') {
                            int proximo = this.br.read();
                            caractere = (char) proximo;
                            if (caractere == '=') {
                                tamanhoToken++;
                                token.setClasse(Classe.cMenorIgual);
                            } else if (caractere == '>') {
                                tamanhoToken++;
                                token.setClasse(Classe.cDiferente);
                            } else {
                                this.br.unread(proximo);
                                token.setClasse(Classe.cMenor);
                            }
                        } else if (caractere == '=') {
                            token.setClasse(Classe.cIgual);
                        } else if (caractere == ',') {
                            token.setClasse(Classe.cVirgula);
                        } else if (caractere == ';') {
                            token.setClasse(Classe.cPontoVirgula);
                        } else if (caractere == '.') {
                            token.setClasse(Classe.cPonto);
                        } else if (caractere == '(') {
                            token.setClasse(Classe.cParEsq);
                        } else if (caractere == ')') {
                            token.setClasse(Classe.cParDir);
                        } else {
                            token.setClasse(Classe.cEOF);
                        }
                        token.setTamanhoToken(tamanhoToken);
                        token.setColuna(coluna + qtdEspacos);
                        token.setLinha(linha);
                        token.setValor(null);
                        c = this.br.read();
                        tamanhoToken++;
                        return token;
                    }
                } else {
                    c = this.br.read();
                    linha++;
                    qtdEspacos = 0;
                    tamanhoToken = 0;
                    coluna = 1;
                }
            }

            token.setClasse(Classe.cEOF);
            return token;
        } catch (IOException err) {
            System.err.println("Não foi possível abrir o arquivo ou ler do arquivo: " + this.nomeArquivo);
            err.printStackTrace();
        }
        return null;
    }

    boolean returnIfIsReservedWord(String word) {
        return this.reservedWords.contains(word.toLowerCase());
    }
}