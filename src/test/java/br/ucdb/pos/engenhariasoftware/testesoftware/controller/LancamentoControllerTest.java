package br.ucdb.pos.engenhariasoftware.testesoftware.controller;


import br.ucdb.pos.engenhariasoftware.testesoftware.converter.DateToStringConverter;
import br.ucdb.pos.engenhariasoftware.testesoftware.converter.MoneyToStringConverter;
import br.ucdb.pos.engenhariasoftware.testesoftware.modelo.Categoria;
import br.ucdb.pos.engenhariasoftware.testesoftware.modelo.Lancamento;
import br.ucdb.pos.engenhariasoftware.testesoftware.modelo.TipoLancamento;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;

public class LancamentoControllerTest {

    private final String URL_TESTE = "http://localhost";
    private final int PORTA_TESTE = 8080;
    private Random aleatorio;

    @BeforeTest
    public void inicializador(){
        RestAssured.baseURI = URL_TESTE;
        RestAssured.port = PORTA_TESTE;
        aleatorio = new Random();
    }

    private BigDecimal cadastrarLancamentos(int nVezes){

        BigDecimal menorValor = null;
        List<Lancamento> lancamentos = new ArrayList<>();
        int incrementador = 100;

        for(int i = 0; i < nVezes; i++){
            BigDecimal valorAleatorio;
            if(nVezes < 5 || (nVezes > 5 &&  (i%2 == 0)))
                valorAleatorio = new BigDecimal(
                    (aleatorio.nextInt(5) + incrementador)
                    + "."
                    + (aleatorio.nextInt(34) + 23));
            else
                valorAleatorio = menorValor;

            if(menorValor == null || menorValor.compareTo(valorAleatorio) == 1)
                menorValor = valorAleatorio;

            lancamentos.add(montarLancamento(valorAleatorio));
            incrementador = incrementador + 100;
        }

        postarLancamentos(lancamentos);

        return menorValor;
    }

    private Lancamento montarLancamento(BigDecimal valor){
        Lancamento lancamento = new Lancamento();

        lancamento.setValor(valor);
        lancamento.setDescricao("Trabalho 04 - Assured Test/Filipe Assad");
        lancamento.setDataLancamento(obterDataAleatorios());
        lancamento.setTipoLancamento(aleatorio.nextBoolean() ? TipoLancamento.SAIDA : TipoLancamento.ENTRADA);
        lancamento.setCategoria(Categoria.ALIMENTACAO);

        return lancamento;
    }

    private Date obterDataAleatorios(){
        LocalDateTime dataHora = LocalDateTime.now();
        Calendar calendar = Calendar.getInstance();
        calendar.set(dataHora.getYear(), dataHora.getMonth().getValue(), aleatorio.nextInt(25) + 1);
        return calendar.getTime();
    }

    private void postarLancamentos(List<Lancamento> lancamentos){
        for(Lancamento lancamento : lancamentos){
            Response response = given().when()
                    .formParam("descricao", lancamento.getDescricao())
                    .formParam("valor", new MoneyToStringConverter().convert(lancamento.getValor()))
                    .formParam("dataLancamento", new DateToStringConverter().convert(lancamento.getDataLancamento()))
                    .formParam("tipoLancamento", lancamento.getTipoLancamento())
                    .formParam("categoria", lancamento.getCategoria())
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .post("/salvar");

            assertEquals(response.getStatusCode(), 302, "Não foi possível cadastrar o Lançamento.");
        }
    }

    private InputStream obterInputStreamBuscaAPI(){
        Response response = given()
                .when()
                .body("Assured")
                .post("/buscaLancamentos");
        InputStream inputStream = response.asInputStream();
        assertEquals(response.getStatusCode(), 200, "Não foi possível buscar lançamentos");

        return inputStream;
    }

    private void limparBase(){
        List<Lancamento> lancamentos = JsonPath.with(obterInputStreamBuscaAPI()).getList("lancamentos", Lancamento.class);

        for(Lancamento lancamento : lancamentos){
            Response response = given().pathParam("id", lancamento.getId()).when().get("/remover/{id}");
            //Esse reponse ta vindo com 200 ou 500.
        }
    }

    private BigDecimal obterMenorLancamentoPorJava(){
        List<Lancamento> lancamentos = JsonPath.with(obterInputStreamBuscaAPI()).getList("lancamentos", Lancamento.class);
        BigDecimal menorValor = lancamentos.get(0).getValor();

        for(Lancamento lancamento : lancamentos){
            if(menorValor.compareTo(lancamento.getValor()) == 1)
                menorValor = lancamento.getValor();
        }

        return menorValor;
    }

    private BigDecimal obterMenorLancamentoPorJSONPath(){
        return converterStringParaBigDecimal(
                JsonPath.with(obterInputStreamBuscaAPI())
                        .getString("lancamentos.min{it.valor}.valor"));
    }

    private BigDecimal converterStringParaBigDecimal(String valor){
        valor = valor.replace(",",".");
        return new BigDecimal(valor);
    }

    @Test
    public void testarListaLancamentoTamanho1(){
        limparBase();
        BigDecimal valorEsperado = cadastrarLancamentos(1);
        assertEquals(obterMenorLancamentoPorJava(), valorEsperado, "JAVA: O valor obtido é diferente do esperada.");
        assertEquals(obterMenorLancamentoPorJSONPath(), valorEsperado, "JSON Path: O valor obtido é diferente do esperada.");
        /*Aqui a unica diferença dos dois é que a busca por jsonpath consome menos código, porém o resultado é o mesmo.*/
    }

    @Test
    public void testarListaLancamentoTamanho2(){
        limparBase();
        BigDecimal valorEsperado = cadastrarLancamentos(2);
        assertEquals(obterMenorLancamentoPorJava(), valorEsperado, "JAVA: O valor obtido é diferente do esperada.");
        assertEquals(obterMenorLancamentoPorJSONPath(), valorEsperado, "JSON Path: O valor obtido é diferente do esperada.");
        /*
            Nessa parte tem que tomar cuidado com o resultado do JSON Path porque ele ta levando em consideração
            a ordem alfabética e não o valor real. Por exemplo o valor 300 é considerado menor que o valor 60, pq na
            ordem alfabética 3 vem primeiro que 6.
            Para contornar isso eu criei um incrementor de 100 em 100, assim meu primeiro número vai ser sempre o menor.
         */
    }

    @Test
    public void testarListaLancamentoTamanhoMaiorQue5(){
        limparBase();
        BigDecimal valorEsperado = cadastrarLancamentos(aleatorio.nextInt(2) + 6);
        assertEquals(obterMenorLancamentoPorJava(), valorEsperado, "JAVA: O valor obtido é diferente do esperada.");
        assertEquals(obterMenorLancamentoPorJSONPath(), valorEsperado, "JSON Path: O valor obtido é diferente do esperada.");
        /*
            Nessa parte tem que tomar cuidado com o resultado do JSON Path porque ele ta levando em consideração
            a ordem alfabética e não o valor real. Por exemplo o valor 300 é considerado menor que o valor 60, pq na
            ordem alfabética 3 vem primeiro que 6.
            Para contornar isso eu criei um incrementor de 100 em 100, assim meu primeiro número vai ser sempre o menor.
         */
    }
}
