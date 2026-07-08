package br.com.zginvest.calculadora;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Negociacao(LocalDate data, OperacaoNegociacao tipo, int quantidade, BigDecimal preco) {
}
