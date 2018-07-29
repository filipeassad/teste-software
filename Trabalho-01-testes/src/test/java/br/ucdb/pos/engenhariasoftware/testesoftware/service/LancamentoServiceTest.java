package br.ucdb.pos.engenhariasoftware.testesoftware.service;

import br.ucdb.pos.engenhariasoftware.testesoftware.anotacoes.AnotacoesTest;
import br.ucdb.pos.engenhariasoftware.testesoftware.builders.LancamentoBuilder;
import br.ucdb.pos.engenhariasoftware.testesoftware.controller.vo.LancamentoVO;
import br.ucdb.pos.engenhariasoftware.testesoftware.controller.vo.ResultadoVO;
import br.ucdb.pos.engenhariasoftware.testesoftware.modelo.Lancamento;
import br.ucdb.pos.engenhariasoftware.testesoftware.modelo.TipoLancamento;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class LancamentoServiceTest {

    @Mock
    private LancamentoService lancamentoService;
    private Random aleatorio = new Random();

    @BeforeClass
    public void init(){
        MockitoAnnotations.initMocks(this);
    }

    @DataProvider(name = "lancamentos")
    protected Object[][] getLancamentos(Method metodoTeste){
        AnotacoesTest anotacao = metodoTeste.getAnnotation(AnotacoesTest.class);
        return new Object[][]{
                new Object[]{new LancamentoBuilder(aleatorio).build(anotacao.nLancamentos())}
        };
    }

    /*@AnotacoesTest(nLancamentos = 10)
    @Test(dataProvider = "lancamentos")
    public void getTotalSaidaTest(List<Lancamento> lancamentos){
        given(lancamentoService.getTotalSaida(lancamentos)).willCallRealMethod();
        given(lancamentoService.somaValoresPorTipo(lancamentos, SAIDA)).willCallRealMethod();

        final BigDecimal totalEsperado = BigDecimal.valueOf(658.12);
        final BigDecimal totalObtido = lancamentoService.getTotalSaida(lancamentos);
        assertEquals(totalObtido, totalEsperado);
    }*/

    @AnotacoesTest(nLancamentos = 0)
    @Test(dataProvider = "lancamentos")
    public void texteLancamentos0(List<Lancamento> lancamentos){
        testeBuscaAjax(lancamentos,0);
    }

    @AnotacoesTest(nLancamentos = 1)
    @Test(dataProvider = "lancamentos")
    public void texteLancamentos1(List<Lancamento> lancamentos){
        testeBuscaAjax(lancamentos,1);
    }

    @AnotacoesTest(nLancamentos = 3)
    @Test(dataProvider = "lancamentos")
    public void texteLancamentos3(List<Lancamento> lancamentos){
        testeBuscaAjax(lancamentos,3);
    }

    @AnotacoesTest(nLancamentos = 9)
    @Test(dataProvider = "lancamentos")
    public void texteLancamentos9(List<Lancamento> lancamentos){
        testeBuscaAjax(lancamentos,9);
    }

    @AnotacoesTest(nLancamentos = 10)
    @Test(dataProvider = "lancamentos")
    public void texteLancamentos10(List<Lancamento> lancamentos){
        testeBuscaAjax(lancamentos,10);
    }

    private void testeBuscaAjax(List<Lancamento> lancamentos, int tamanhoEsperado){

        when(lancamentoService.getTotalEntrada(anyListOf(Lancamento.class))).thenCallRealMethod();
        when(lancamentoService.getTotalSaida(anyListOf(Lancamento.class))).thenCallRealMethod();
        when(lancamentoService.somaValoresPorTipo(anyListOf(Lancamento.class), any(TipoLancamento.class))).thenCallRealMethod();
        when(lancamentoService.getResultadoVO(anyListOf(Lancamento.class), anyInt(), anyLong())).thenCallRealMethod();
        when(lancamentoService.busca(anyString())).thenReturn(lancamentos);
        when(lancamentoService.conta(anyString())).thenReturn((long) lancamentos.size());
        when(lancamentoService.tamanhoPagina()).thenReturn(10);
        given(lancamentoService.buscaAjax(anyString())).willCallRealMethod();

        ResultadoVO resultadoVO = lancamentoService.buscaAjax(anyString());

        validarValorTotalEntrada(lancamentos);
        validarValorTotalSaida(lancamentos);
        validarTamanhoLista(lancamentos, tamanhoEsperado);
        validarExisteCampo(lancamentos, resultadoVO);
        validarValorCampo(lancamentos, resultadoVO);

    }

    private void validarValorTotalEntrada(List<Lancamento> lancamentos){
        BigDecimal totalEsperado = calculaTotal(lancamentos, TipoLancamento.ENTRADA);
        BigDecimal totalObtido = lancamentoService.getTotalEntrada(lancamentos);
        assertEquals(totalObtido, totalEsperado,  totalEsperado.toString() + " != " + totalObtido.toString());
    }

    private void validarValorTotalSaida(List<Lancamento> lancamentos){
        BigDecimal totalEsperado = calculaTotal(lancamentos, TipoLancamento.SAIDA);
        BigDecimal totalObtido = lancamentoService.getTotalSaida(lancamentos);
        assertEquals(totalObtido, totalEsperado,  totalEsperado.toString() + " != " + totalObtido.toString());
    }

    private void validarTamanhoLista(List<Lancamento> lancamentos, int tamanhoEsperado){
        long tamanhoObtido = lancamentoService.buscaAjax(anyString()).getTotalRegistros();
        assertEquals(tamanhoObtido, tamanhoEsperado,  tamanhoEsperado + " != " + tamanhoObtido );
    }

    private void validarExisteCampo(List<Lancamento> lancamentos, ResultadoVO resultadoVO){
        Field[] campos = Lancamento.class.getDeclaredFields();
        for (LancamentoVO lancamentoVO : resultadoVO.getLancamentos()) {
            for (Field campo : campos) {
                assertTrue(comparaCampos(lancamentoVO, campo.getName()), campo.getName() + " não está presente no LancamentoVO.");
            }
        }
    }

    private void validarValorCampo(List<Lancamento> lancamentos, ResultadoVO resultadoVO){
        Field[] campos = Lancamento.class.getDeclaredFields();
        for (LancamentoVO lancamentoVO : resultadoVO.getLancamentos()) {
            for (Field campo : campos) {
                String valorObtido = getMethodValue(lancamentoVO, campo.getName()).toString();
                assertTrue(!valorObtido.equals("") && valorObtido != null, campo.getName() + " é nulo dentro LancamentoVO.");
            }
        }
    }

    private Object getMethodValue(Object object, String atributo){
        try {
            String metodoGetGerado = "get" + atributo.substring(0, 1).toUpperCase() + atributo.substring(1);
            Method metodo = object.getClass().getMethod(metodoGetGerado, new Class[]{});
            return metodo.invoke(object, new Object[]{});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean comparaCampos(LancamentoVO lancamentoVO, String nomeCampo) {
        Field[] campos = lancamentoVO.getClass().getDeclaredFields();
        for(Field campo: campos){
            if(campo.getName().equals(nomeCampo))
                return true;
        }
        return false;
    }

    private BigDecimal calculaTotal(List<Lancamento> lancamentos, TipoLancamento tipoLancamento){
        BigDecimal total = new BigDecimal(0);

        for(Lancamento lancamento :lancamentos){
            if(lancamento.getTipoLancamento() == tipoLancamento)
                total.add(lancamento.getValor());
        }

        return total;
    }

}
