package br.ucdb.pos.engenhariasoftware.testesoftware.builders;

import br.ucdb.pos.engenhariasoftware.testesoftware.modelo.Lancamento;
import br.ucdb.pos.engenhariasoftware.testesoftware.modelo.TipoLancamento;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class LancamentoBuilder {

    private Random aleatorio;

    public LancamentoBuilder(Random aleatorio){
        this.aleatorio = aleatorio;
    }

    private Lancamento gerarLancamento(){
        Lancamento lancamento;

        Calendar calendario = Calendar.getInstance();
        calendario.set(aleatorio.nextInt(2050 - 2010) + 2010, // Ano entre 2050 e 2010
                aleatorio.nextInt(11), // Mês entre 0 e 11
                aleatorio.nextInt(20) + 1); // Dia entre 1 e 20
        lancamento = new Lancamento();
        lancamento.setId(aleatorio.nextLong());
        lancamento.setDataLancamento(calendario.getTime());
        lancamento.setTipoLancamento(aleatorio.nextBoolean() ? TipoLancamento.ENTRADA : TipoLancamento.SAIDA);
        lancamento.setValor(new BigDecimal(aleatorio.nextDouble()));
        lancamento.setDescricao(lancamento.getId()+  " - Lançamento de " + lancamento.getTipoLancamento() +
                " na data de " + new SimpleDateFormat("yyyy-MM-dd").format(lancamento.getDataLancamento()) +
                " no valor de R$ " + lancamento.getValor());

        return lancamento;
    }

    public List<Lancamento> build(int nLancamentos){
        List<Lancamento> lancamentos = new ArrayList<>();

        for(int i = 0; i < nLancamentos; i++){
            lancamentos.add(gerarLancamento());
        }

        return lancamentos;
    }

}
