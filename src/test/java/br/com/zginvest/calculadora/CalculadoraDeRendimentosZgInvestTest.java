package br.com.zginvest.calculadora;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculadoraDeRendimentosZgInvestTest {

	private CalculadoraDeRendimentosZgInvest calculadora;

	@BeforeEach
	void montarCarteiraAbcd3() {
		List<Negociacao> negociacoes = List.of(
				new Negociacao(LocalDate.of(2020, 3, 1), OperacaoNegociacao.COMPRA, 20, new BigDecimal("8")),
				new Negociacao(LocalDate.of(2020, 4, 1), OperacaoNegociacao.COMPRA, 10, new BigDecimal("9")),
				new Negociacao(LocalDate.of(2020, 4, 3), OperacaoNegociacao.VENDA, 5, new BigDecimal("8.5")),
				new Negociacao(LocalDate.of(2020, 4, 6), OperacaoNegociacao.VENDA, 5, new BigDecimal("9")),
				new Negociacao(LocalDate.of(2020, 4, 4), OperacaoNegociacao.VENDA, 5, new BigDecimal("9.5")),
				new Negociacao(LocalDate.of(2020, 4, 5), OperacaoNegociacao.COMPRA, 20, new BigDecimal("11")),
				new Negociacao(LocalDate.of(2020, 4, 6), OperacaoNegociacao.VENDA, 20, new BigDecimal("8"))
		);

		Map<LocalDate, BigDecimal> precosFechamento = Map.of(
				LocalDate.of(2020, 3, 31), new BigDecimal("10"),
				LocalDate.of(2020, 4, 1), new BigDecimal("11"),
				LocalDate.of(2020, 4, 2), new BigDecimal("12"),
				LocalDate.of(2020, 4, 3), new BigDecimal("7"),
				LocalDate.of(2020, 4, 4), new BigDecimal("10.5"),
				LocalDate.of(2020, 4, 5), new BigDecimal("8.4"),
				LocalDate.of(2020, 4, 6), new BigDecimal("15")
		);

		calculadora = new CalculadoraDeRendimentosZgInvest(negociacoes, precosFechamento);
	}

	@Test
	void em31DeMarco() {
		PosicaoCarteira posicao = calculadora.calcularPosicaoEm(LocalDate.of(2020, 3, 31));

		assertEquals(20, posicao.quantidadeAcoes());
		assertEquals(new BigDecimal("200.00"), posicao.saldoAtual());
		assertEquals(new BigDecimal("25.00"), posicao.rendimentoPercentual());
	}

	@Test
	void em01DeAbril() {
		PosicaoCarteira posicao = calculadora.calcularPosicaoEm(LocalDate.of(2020, 4, 1));

		assertEquals(30, posicao.quantidadeAcoes());
		assertEquals(new BigDecimal("330.00"), posicao.saldoAtual());
		assertEquals(new BigDecimal("32.05"), posicao.rendimentoPercentual());
	}

	@Test
	void em02DeAbril() {
		PosicaoCarteira posicao = calculadora.calcularPosicaoEm(LocalDate.of(2020, 4, 2));

		assertEquals(30, posicao.quantidadeAcoes());
		assertEquals(new BigDecimal("360.00"), posicao.saldoAtual());
		assertEquals(new BigDecimal("44.05"), posicao.rendimentoPercentual());
	}

	@Test
	void em03DeAbril() {
		PosicaoCarteira posicao = calculadora.calcularPosicaoEm(LocalDate.of(2020, 4, 3));

		assertEquals(25, posicao.quantidadeAcoes());
		assertEquals(new BigDecimal("175.00"), posicao.saldoAtual());
		assertEquals(new BigDecimal("-15.96"), posicao.rendimentoPercentual());
	}

	@Test
	void lancaExcecaoQuandoNaoHaPrecoDeFechamentoParaAData() {
		assertThrows(IllegalArgumentException.class,
				() -> calculadora.calcularPosicaoEm(LocalDate.of(2020, 3, 30)));
	}
}
