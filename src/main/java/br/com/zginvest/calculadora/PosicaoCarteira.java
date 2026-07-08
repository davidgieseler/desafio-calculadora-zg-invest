package br.com.zginvest.calculadora;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PosicaoCarteira(LocalDate data, long quantidadeAcoes, BigDecimal saldoAtual, BigDecimal rendimentoPercentual) {

}
