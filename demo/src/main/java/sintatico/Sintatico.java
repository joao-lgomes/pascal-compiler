package sintatico;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Sintatico {
	private Lexico lexico;
	private Token token;
	private int linha;
	private int coluna;

	private List<Registro> ultimasVariaveisDeclaradas = new ArrayList<>();

	private TabelaSimbolos tabela;
	private int endereco;
	private String rotulo = "";

	private String nomeArquivoSaida;
	private String caminhoArquivoSaida;
	private BufferedWriter bw;
	private FileWriter fw;

	public Sintatico(String nomeArquivo) {
		linha = 1;
		coluna = 1;
		this.lexico = new Lexico(nomeArquivo);

		endereco = 0;
		nomeArquivoSaida = "arquivoC.c";
		caminhoArquivoSaida = Paths.get(nomeArquivoSaida).toAbsolutePath().toString();
		bw = null;
		fw = null;

		try {
			fw = new FileWriter(caminhoArquivoSaida, Charset.forName("UTF-8"));
			bw = new BufferedWriter(fw);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void GerarNoArquivo(String codigo) {
		try {
			if (rotulo.isEmpty()) {
				bw.write(codigo + "\n");
			} else {
				bw.write(rotulo + ": " + codigo + "\n");
				rotulo = "";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void analisar() {
		lerToken();
		try {
			programa();
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("TABELA DE SIMBOLOS CRIADA:");
		System.out.println(this.tabela);
	}

	public void lerToken() {
		token = lexico.getToken(linha, coluna);
		System.out.println(token);
		coluna = token.getColuna() + token.getTamanhoToken();
		linha = token.getLinha();
	}

	// <programa> ::= program <id> {A1} <corpo> • {A30}
	public void programa() {
		if (token.getClasse() == Classe.cPalRes &&
				token.getValor().getValorIdentificador().equalsIgnoreCase("program")) {
			lerToken();
			if (token.getClasse() == Classe.cId) { // id()
				lerToken();

				acaoA1();

				corpo();
				if (token.getClasse() == Classe.cPonto) {
					lerToken();
					// {A30}
				} else {
					System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
							+ " -> Faltou um ponto final no program");
				}
				acaoA2();
			} else {
				System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
						+ " -> Faltou o identificador do program");
			}
		} else {
			System.out.println("Faltou começar por PROGRAM");
		}
	}

	public void acaoA1() {
		tabela = new TabelaSimbolos();

		tabela.setTabelaPai(null);

		Registro registro = new Registro();
		registro.setNome(token.getValor().getValorIdentificador());
		registro.setCategoria(Categoria.PROGRAMAPRINCIPAL);

		registro.setNivel(0);
		registro.setOffset(0);
		registro.setTabelaSimbolos(tabela);
		registro.setRotulo("main");
		tabela.inserirRegistro(registro);
		String codigo = "#include <stdio.h>\n" +
				"\nint main(){\n";

		GerarNoArquivo(codigo);

	}

	public void acaoA2() {
		Registro registro = new Registro();
		registro.setNome(null);
		registro.setCategoria(Categoria.PROGRAMAPRINCIPAL);
		registro.setNivel(0);
		registro.setOffset(0);
		registro.setTabelaSimbolos(tabela);
		registro.setRotulo("finalCode");
		tabela.inserirRegistro(registro);
		String codigo = "\n}\n";
		GerarNoArquivo(codigo);
	}

	private void acaoA3(String type) {
        String codigo= '\t'+type;
        for(int i=0;i<this.ultimasVariaveisDeclaradas.size();i++)
        {
            codigo=codigo+' '+ this.ultimasVariaveisDeclaradas.get(i).getNome();
            if(i == this.ultimasVariaveisDeclaradas.size()-1)
            {
                codigo=codigo + ';';
            }
            else{
                codigo=codigo + ',';
            }
        }
        GerarNoArquivo(codigo);
    }

	public void acaoA4()
    {
        Registro registro=new Registro();
        registro.setNome(token.getValor().getValorIdentificador());
        registro.setCategoria(Categoria.VARIAVEL);
        registro.setNivel(0);
        registro.setOffset(0);
        registro.setTabelaSimbolos(tabela);
        this.endereco++;
        registro.setRotulo("variavel"+this.endereco);
        ultimasVariaveisDeclaradas.add(registro);
        this.tabela.inserirRegistro(registro);
    }

	// <corpo> ::= <declara> <rotina> begin <sentencas> end
	public void corpo() {
		declara();
		// rotina(); NÃO É PRA FAZER
		if ((token.getClasse() == Classe.cPalRes)
				&& (token.getValor().getValorIdentificador().equalsIgnoreCase("begin"))) {
			lerToken();
			sentencas();
			if ((token.getClasse() == Classe.cPalRes)
					&& (token.getValor().getValorIdentificador().equalsIgnoreCase("end"))) {
				lerToken();
			} else {
				System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
						+ " -> Faltou o end no fim do programa");
			}
		} else {
			System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
					+ " -> Faltou o begin no corpo do programa");
		}
	}

	// <declara> ::= var <dvar> <mais_dc> | 
	public void declara() {
		if ((token.getClasse() == Classe.cPalRes)
				&& (token.getValor().getValorIdentificador().equalsIgnoreCase("var"))) {
			lerToken();
			dvar();
			mais_dc();
		}
	}

	// <mais_dc> ::= ; <cont_dc>
	public void mais_dc() {
		if (token.getClasse() == Classe.cPontoVirgula) {
			lerToken();
			cont_dc();
		} else {
			System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
					+ " -> Faltou colocar o ; no mais_dc");
		}
	}

	// <cont_dc> ::= <dvar> <mais_dc> | nada
	public void cont_dc() {
		if (token.getClasse() == Classe.cId) {
			dvar();
			mais_dc();
		}
	}

	// <variaveis> : <tipo_var>
	public void dvar() {
		variaveis();
		if (token.getClasse() == Classe.cDoisPontos) {
			lerToken();
			tipo_var();
		} else {
			System.out
					.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna() + " -> Faltou o : no dvar");
		}
	}

	// integer
	public void tipo_var() {
		if ((token.getClasse() == Classe.cPalRes)
				&& (token.getValor().getValorIdentificador().equalsIgnoreCase("integer"))) {
					acaoA3("int");
			lerToken();
		} else if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("real"))) {
            acaoA3("float");
            lerToken();
        }else {
			System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
					+ " -> Faltou a declaração de Integer no tipo_var");
		}
	}

	// <id> {A2} <mais_var>
	public void variaveis() {
		if (token.getClasse() == Classe.cId) {
			acaoA4();
			lerToken();
			// {A2}
			mais_var();
		} else {
			System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
					+ " -> Faltou o identificador no variáveis");
		}
	}

	// , <variaveis> | nada
	public void mais_var() {
		if (token.getClasse() == Classe.cVirgula) {
			lerToken();
			// {A2}
			variaveis();
		}
	}

	// <sentencas> ::= <comando> <mais_sentencas>
	public void sentencas() {
		comando();
		mais_sentencas();
	}

	// <mais_sentencas> ::= ; <cont_sentencas>
	public void mais_sentencas() {
		if (token.getClasse() == Classe.cPontoVirgula) {
			lerToken();
			cont_sentencas();
		} else {
			System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
					+ " -> Faltou o ; no mais_sentencas");
		}
	}

	// <cont_sentencas> ::= <sentencas> | vazio
	public void cont_sentencas() {
		if (((token.getClasse() == Classe.cPalRes)
				&& (token.getValor().getValorIdentificador().equalsIgnoreCase("read"))) ||
				((token.getClasse() == Classe.cPalRes)
						&& (token.getValor().getValorIdentificador().equalsIgnoreCase("write")))
				||
				((token.getClasse() == Classe.cPalRes)
						&& (token.getValor().getValorIdentificador().equalsIgnoreCase("for")))
				||
				((token.getClasse() == Classe.cPalRes)
						&& (token.getValor().getValorIdentificador().equalsIgnoreCase("repeat")))
				||
				((token.getClasse() == Classe.cPalRes)
						&& (token.getValor().getValorIdentificador().equalsIgnoreCase("while")))
				||
				((token.getClasse() == Classe.cPalRes)
						&& (token.getValor().getValorIdentificador().equalsIgnoreCase("if")))
				||
				((token.getClasse() == Classe.cId))) {
			sentencas();
		}
	}

	// <var_read> ::= <id> {A5} <mais_var_read>
	public List<Token> var_read(List<Token> arrayTokens) {
		if (token.getClasse() == Classe.cId) {
			arrayTokens.add(token);
			lerToken();
			// {A5}
			arrayTokens = mais_var_read(arrayTokens);
		} else {
			System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
					+ " -> Faltou o identificador no var_read");
		}
		return arrayTokens;
	}

	// <mais_var_read> ::= , <var_read> | vazio
	public List<Token> mais_var_read(List<Token> arrayTokens) {
		if (token.getClasse() == Classe.cVirgula) {
			lerToken();
			arrayTokens = var_read(arrayTokens);
		}
		return arrayTokens;
	}

	// <var_write> ::= <id> {A6} <mais_var_write>
	public String var_write(String codigo) {
		if (token.getClasse() == Classe.cId) {
			codigo = codigo + token.getValor().getValorIdentificador();
			lerToken();
			// {A6}
			codigo = mais_var_write(codigo);
		} else {
			System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
					+ " -> Faltou o identificador no var_write");
		}
		return codigo;
	}

	// <mais_var_write> ::= , <var_write> | vazio
	public String mais_var_write(String codigo) {
		if (token.getClasse() == Classe.cVirgula) {
			codigo = codigo + ',';
			lerToken();
			codigo = var_write(codigo);
		}
		return codigo;
	}

	/*
	 * read ( <var_read> ) |
	 * write ( <var_write> ) |
	 * for <id> {A25} := <expressao> {A26} to {A27} <expressao> {A28}
	 * do begin <sentencas> end {A29} |
	 * repeat {A23} <sentencas> until ( <condicao> ) {A24} |
	 * while {A20} ( <condicao> ) {A21} do begin <sentencas> end {A22} |
	 * if ( <condicao> ) {A17} then begin <sentencas> end {A18}
	 * <pfalsa> {A19} |
	 * <id> {A13} := <expressao> {A14}
	 * 
	 */
	public void comando() {
		// read ( <var_read> )
		if ((token.getClasse() == Classe.cPalRes)
				&& (token.getValor().getValorIdentificador().equalsIgnoreCase("read"))) {
			String codigoC = "\tscanf";
			lerToken();
			if (token.getClasse() == Classe.cParEsq) {
				codigoC = codigoC + "(\"";
				lerToken();
				List<Token> arrayToken = new ArrayList<Token>();
				arrayToken = var_read(arrayToken);
				for (Token i : arrayToken) {
					codigoC = codigoC + "%d ";
				}
				codigoC = codigoC + "\", ";
				for (Token i : arrayToken) {
					if (i == arrayToken.get(arrayToken.size() - 1)) {
						codigoC = codigoC + "&" + i.getValor().getValorIdentificador();
					} else {
						codigoC = codigoC + "&" + i.getValor().getValorIdentificador() + ", ";
					}
				}
				if (token.getClasse() == Classe.cParDir) {
					codigoC = codigoC + ");";
					GerarNoArquivo(codigoC);
					lerToken();
				} else {
					System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
							+ " -> Faltou o ( no comando após o var_read do read");
				}
			} else {
				System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
						+ " -> Faltou o ( no comando após o read");
			}
		} else
		// write ( <var_write> )
		if ((token.getClasse() == Classe.cPalRes)
				&& (token.getValor().getValorIdentificador().equalsIgnoreCase("write"))) {
			String codigoEsq = "\tprintf";
			String codigoDir = "";
			lerToken();
			if (token.getClasse() == Classe.cParEsq) {
				codigoEsq = codigoEsq + "(\"";
				lerToken();
				codigoDir = codigoDir + var_write("");

				if (codigoDir.length() > 0) {
					codigoEsq = codigoEsq + "%d ".repeat(codigoDir.split(",").length);
					codigoEsq = codigoEsq + "\", ";
				} else {
					codigoEsq = codigoEsq + "\"";
				}

				if (token.getClasse() == Classe.cParDir) {
					codigoDir = codigoDir + ");";
					GerarNoArquivo(codigoEsq + codigoDir);
					lerToken();
				} else {
					System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
							+ " -> Faltou o ) no comando após o var_write do write");
				}
			} else {
				System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
						+ " -> Faltou o ( no comando após o write");
			}
		} else
		// for <id> {A25} := <expressao> {A26} to {A27} <expressao> {A28}
		// do begin <sentencas> end {A29}
		if ((token.getClasse() == Classe.cPalRes)
				&& (token.getValor().getValorIdentificador().equalsIgnoreCase("for"))) {
			String codigo = "\n\tfor(";
			lerToken();
			if (token.getClasse() == Classe.cId) {
				String identificador = token.getValor().getValorIdentificador();
				codigo = codigo + identificador;
				lerToken();
				// {A25}
				if (token.getClasse() == Classe.cAtribuicao) {
					codigo = codigo + "=";
					lerToken();
					codigo = codigo + expressao();
					// {A26}
					if ((token.getClasse() == Classe.cPalRes)
							&& (token.getValor().getValorIdentificador().equalsIgnoreCase("to"))) {
						codigo=codigo+";";
						lerToken();
						// {A27}
						codigo=codigo+identificador;
                        codigo=codigo+"<="+expressao()+";";
                        codigo=codigo+identificador + "++)";
						// {A28}
						if ((token.getClasse() == Classe.cPalRes)
								&& (token.getValor().getValorIdentificador().equalsIgnoreCase("do"))) {
							lerToken();
							if ((token.getClasse() == Classe.cPalRes)
									&& (token.getValor().getValorIdentificador().equalsIgnoreCase("begin"))) {
										codigo=codigo+"{";
										GerarNoArquivo(codigo);
								lerToken();
								sentencas();
								if ((token.getClasse() == Classe.cPalRes)
										&& (token.getValor().getValorIdentificador().equalsIgnoreCase("end"))) {
											String codigoFinal = "\t}";
											GerarNoArquivo(codigoFinal);
									lerToken();
									// {A29}
								} else {
									System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
											+ " -> Faltou o End no fim do for");
								}
							} else {
								System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
										+ " -> Faltou o Begin após o Do do for");
							}
						} else {
							System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
									+ " -> Faltou o Do no meio do for");
						}
					} else {
						System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
								+ " -> Faltou o To no meio do for");
					}
				} else {
					System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
							+ " -> Faltou o := no meio do for");
				}
			} else {
				System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
						+ " -> Faltou o identificador no início do for");
			}
		} else
		// repeat {A23} <sentencas> until ( <condicao> ) {A24}
		if ((token.getClasse() == Classe.cPalRes)
				&& (token.getValor().getValorIdentificador().equalsIgnoreCase("repeat"))) {
					String codigo="\n\tdo {\n\t";
			lerToken();
			GerarNoArquivo(codigo);
			// {A23}
			sentencas();
			if ((token.getClasse() == Classe.cPalRes)
					&& (token.getValor().getValorIdentificador().equalsIgnoreCase("until"))) {
				lerToken();
				if (token.getClasse() == Classe.cParEsq) {
					String codigoFinal="\n\t}while";
                    codigoFinal=codigoFinal+"(";
					lerToken();
					codigoFinal=codigoFinal+condicao();
					if (token.getClasse() == Classe.cParDir) {
						codigoFinal=codigoFinal+");";
                        GerarNoArquivo(codigoFinal);
						lerToken();
						// {A24}
					} else {
						System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
								+ " -> Faltou fechou o parenteses do repeat após a condição");
					}
				} else {
					System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
							+ " -> Faltou abrir o parentese no repeat após o until");
				}
			} else {
				System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
						+ " -> Faltou o Util após o repeat");
			}
		}
		// while {A20} ( <condicao> ) {A21} do begin <sentencas> end {A22} |
		else if ((token.getClasse() == Classe.cPalRes)
				&& (token.getValor().getValorIdentificador().equalsIgnoreCase("while"))) {
					String codigo="\n\twhile";
			lerToken();
			// {A20}
			if (token.getClasse() == Classe.cParEsq) {
				codigo=codigo+"(";
				lerToken();
				codigo=codigo+condicao();
				if (token.getClasse() == Classe.cParDir) {
					codigo=codigo+")";
					lerToken();
					// {A21}
					if ((token.getClasse() == Classe.cPalRes)
							&& (token.getValor().getValorIdentificador().equalsIgnoreCase("do"))) {
						lerToken();
						if ((token.getClasse() == Classe.cPalRes)
								&& (token.getValor().getValorIdentificador().equalsIgnoreCase("begin"))) {
									codigo=codigo+"{\n";
									GerarNoArquivo(codigo);
							lerToken();
							sentencas();
							if ((token.getClasse() == Classe.cPalRes)
									&& (token.getValor().getValorIdentificador().equalsIgnoreCase("end"))) {
										codigo="\t}\n";
                                            GerarNoArquivo(codigo);										
								lerToken();
								// {A22}
							} else {
								System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
										+ " -> Faltou o end para finalizar o While");
							}
						} else {
							System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
									+ " -> Faltou o Begin após o Do do while");
						}
					} else {
						System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
								+ " -> Faltou o Do dentro do while");
					}
				} else {
					System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
							+ " -> Faltou fechar o parenteses no while");
				}
			} else {
				System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
						+ " -> Faltou abrir o parenteses no while");
			}
		}
		// if ( <condicao> ) {A17} then begin <sentencas> end {A18} <pfalsa> {A19} |
		else if ((token.getClasse() == Classe.cPalRes)
				&& (token.getValor().getValorIdentificador().equalsIgnoreCase("if"))) {
					String codigo="";
			lerToken();
			if (token.getClasse() == Classe.cParEsq) {
				codigo=codigo+"\n\tif(";
				lerToken();
				codigo=codigo+condicao();
				if (token.getClasse() == Classe.cParDir) {
					codigo=codigo+")";
					lerToken();
					// {A17}
					if ((token.getClasse() == Classe.cPalRes)
							&& (token.getValor().getValorIdentificador().equalsIgnoreCase("then"))) {
						lerToken();
						if ((token.getClasse() == Classe.cPalRes)
								&& (token.getValor().getValorIdentificador().equalsIgnoreCase("begin"))) {
									codigo=codigo +" {";
                                        GerarNoArquivo(codigo);
							lerToken();
							sentencas();
							if ((token.getClasse() == Classe.cPalRes)
									&& (token.getValor().getValorIdentificador().equalsIgnoreCase("end"))) {
								lerToken();
								String codigoFinal = "";
                                            codigoFinal = codigoFinal + "\t}";
                                            GerarNoArquivo(codigoFinal);
								// {A18}
								pfalsa();
								// {A19}
							} else {
								System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
										+ " -> Faltou o end no fim do if");
							}
						} else {
							System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
									+ " -> Faltou begin após o then do if");
						}
					} else {
						System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
								+ " -> Faltou o then do if");
					}
				} else {
					System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
							+ " -> Faltou fechar o parenteses no if");
				}
			} else {
				System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
						+ " -> Faltou abrir o parenteses no if");
			}
		}
		// <id> {A13} := <expressao> {A14} | <chamada_procedimento>
		else if (token.getClasse() == Classe.cId) {
			String codigo="\n\t";
            codigo=codigo+token.getValor().getValorIdentificador();
			lerToken();
			// {A13}
			if (token.getClasse() == Classe.cAtribuicao) {
				codigo=codigo+"=";
				lerToken();
				codigo=codigo+expressao()+";";
				GerarNoArquivo(codigo);
				// {A14}
			} else {
				System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
						+ " -> Faltou o atribuição do Id do comando");
			}
		} else {
			// chamada_procedimento();
		}
	}

	public String condicao() {
		String expressao1 = expressao();
		String relacao = relacao();
		// {A15}
		String expressao2 = expressao();
		// {A16}

		return expressao1 + relacao + expressao2;
	}

	// <pfalsa> ::= else begin <sentencas> end | vazio
	public void pfalsa() {
		String codigo = "";
		if ((token.getClasse() == Classe.cPalRes)
				&& (token.getValor().getValorIdentificador().equalsIgnoreCase("else"))) {
			codigo = codigo + "\telse";
			lerToken();
			if ((token.getClasse() == Classe.cPalRes)
					&& (token.getValor().getValorIdentificador().equalsIgnoreCase("begin"))) {
				codigo = codigo + "{";
				GerarNoArquivo(codigo);
				lerToken();
				sentencas();
				if ((token.getClasse() == Classe.cPalRes)
						&& (token.getValor().getValorIdentificador().equalsIgnoreCase("end"))) {
					String codigoFinal = "\n\t}";
					GerarNoArquivo(codigoFinal);
					lerToken();
				} else {
					System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
							+ " -> Faltou finalizar o pfalsa com o end");
				}
			} else {
				System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
						+ " -> Faltou o begin após o else do pfalsa");
			}
		}
	}

	// <relacao> ::= = | > | < | >= | <= | <>
	public String relacao() {
		String operador = "";
		if (token.getClasse() == Classe.cIgual) {
			operador = "=";
			lerToken();
		} else if (token.getClasse() == Classe.cMaior) {
			operador = ">";
			lerToken();
		} else if (token.getClasse() == Classe.cMenor) {
			operador = "<";
			lerToken();
		} else if (token.getClasse() == Classe.cMaiorIgual) {
			operador = ">=";
			lerToken();
		} else if (token.getClasse() == Classe.cMenorIgual) {
			operador = "<=";
			lerToken();
		} else if (token.getClasse() == Classe.cDiferente) {
			operador = "!=";
			lerToken();
		} else {
			System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
					+ " -> Faltou o operador matemático de relação");
		}
		return operador;
	}

	// <expressao> ::= <termo> <outros_termos>
	public String expressao() {
		String termo = termo();
		String outrosTermos = outros_termos();

		return termo + outrosTermos;
	}

	// <outros_termos> ::= <op_ad> {A9} <termo> {A10} <outros_termos> | vazio
	public String outros_termos() {
		String operador = "";
		String termo = "";
		String outrosTermos = "";
		if (token.getClasse() == Classe.cMais || token.getClasse() == Classe.cMenos) {
			operador = op_ad();
			// {A9}
			termo = termo();
			// {A10}
			outrosTermos = outros_termos();
		}

		return operador + termo + outrosTermos;
	}

	// <op_ad> ::= + | -
	public String op_ad() {
		String operador = "";
		if (token.getClasse() == Classe.cMais) {
			operador = "+";
			lerToken();
		} else if (token.getClasse() == Classe.cMenos) {
			operador = "-";
			lerToken();
		} else {
			System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
					+ " -> Faltou o operador de mais ou de menos no op_ad");
		}
		return operador;
	}

	// <termo> ::= <fator> <mais_fatores>
	public String termo() {
		String fator = fator();
		String maisFatores = mais_fatores();

		return fator + maisFatores;
	}

	// <mais_fatores> ::= <op_mul> {A11} <fator> {A12} <mais_fatores> | vazio
	public String mais_fatores() {
		if (token.getClasse() == Classe.cMultiplicacao || token.getClasse() == Classe.cDivisao) {
			String operador = op_mul();
			// {A11}
			String fator = fator();
			// {A12}
			String outrosFatores = mais_fatores();

			return operador + fator + outrosFatores;
		}

		return "";
	}

	// <op_mul> ::= * | /
	public String op_mul() {
		String operador = "";
		if (token.getClasse() == Classe.cMultiplicacao) {
			operador = "*";
			lerToken();
		} else if (token.getClasse() == Classe.cDivisao) {
			operador = "/";
			lerToken();
		} else {
			System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
					+ " -> Faltou o operador de multiplicação ou de divisão no op_mul");
		}

		return operador;
	}

	// <fator> ::= <id_var> {A7} | <intnum> {A8} | (<expressao>) | <id_funcao>
	// <argumentos>
	public String fator() {
		String fator = "";
		if (token.getClasse() == Classe.cId) {
			fator = token.getValor().getValorIdentificador();
			lerToken();
			// {A7}
		} else if (token.getClasse() == Classe.cInt) {
			fator = String.valueOf(token.getValor().getValorInteiro());
			lerToken();
			// {A8}
		} else if (token.getClasse() == Classe.cReal) {
			fator = String.valueOf(token.getValor().getValorDecimal());
			lerToken();
			// {A8}
		} else if (token.getClasse() == Classe.cParEsq) {
			fator = "(";
			lerToken();
			fator = fator + expressao();
			if (token.getClasse() == Classe.cParDir) {
				fator = fator + ")";
				lerToken();
			} else {
				System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
						+ " -> Faltou fechar o parenteses da expressão");
			}
		} else {
			System.out.println("Linha: " + token.getLinha() + " Coluna: " + token.getColuna()
					+ " -> Faltou usar o fator (identificador, numero ou expressão)");
		}

		return fator;
	}

	// verifica se é um identificador
	public void id() {
		if (token.getClasse() == Classe.cId) {
			// {A3}
		} else {
			System.out.println(
					"Linha: " + token.getLinha() + " Coluna: " + token.getColuna() + " -> Faltou o id após o program");
		}
	}
}
