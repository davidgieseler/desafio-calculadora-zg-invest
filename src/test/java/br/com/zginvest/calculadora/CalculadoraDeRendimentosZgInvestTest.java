package br.com.zginvest.calculadora;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculadoraDeRendimentosZgInvestTest {

	@Nested
	class ExemploOficialAbcd3 {

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
		void em05DeAbril() {
			PosicaoCarteira posicao = calculadora.calcularPosicaoEm(LocalDate.of(2020, 4, 5));

			assertEquals(40, posicao.quantidadeAcoes());
			assertEquals(new BigDecimal("336.00"), posicao.saldoAtual());
			assertEquals(new BigDecimal("-13.04"), posicao.rendimentoPercentual());
		}

		@Test
		void em06DeAbrilAposAsDuasVendasDoDia() {
			PosicaoCarteira posicao = calculadora.calcularPosicaoEm(LocalDate.of(2020, 4, 6));

			assertEquals(15, posicao.quantidadeAcoes());
			assertEquals(new BigDecimal("225.00"), posicao.saldoAtual());
			assertEquals(new BigDecimal("55.27"), posicao.rendimentoPercentual());
		}

		@Test
		void lancaExcecaoQuandoNaoHaPrecoDeFechamentoParaAData() {
			assertThrows(IllegalArgumentException.class,
					() -> calculadora.calcularPosicaoEm(LocalDate.of(2020, 3, 30)));
		}
	}

	@Nested
	class PosicoesCompradasCasosDeBorda {

		private CalculadoraDeRendimentosZgInvest calculadora;

		@BeforeEach
		void montarCarteira() {
			List<Negociacao> negociacoes = List.of(
					new Negociacao(LocalDate.of(2020, 5, 1), OperacaoNegociacao.COMPRA, 10, new BigDecimal("40.00")),
					new Negociacao(LocalDate.of(2020, 5, 4), OperacaoNegociacao.VENDA, 10, new BigDecimal("45.00")),
					new Negociacao(LocalDate.of(2020, 5, 6), OperacaoNegociacao.COMPRA, 6, new BigDecimal("42.00"))
			);

			Map<LocalDate, BigDecimal> precosFechamento = Map.of(
					LocalDate.of(2020, 5, 4), new BigDecimal("50.00"),
					LocalDate.of(2020, 5, 5), new BigDecimal("33.00"),
					LocalDate.of(2020, 5, 6), new BigDecimal("41.00")
			);

			calculadora = new CalculadoraDeRendimentosZgInvest(negociacoes, precosFechamento);
		}

		@Test
		void posicaoZeradaExatamenteNaoLancaExcecaoENaoDivideePorZero() {
			PosicaoCarteira posicao = calculadora.calcularPosicaoEm(LocalDate.of(2020, 5, 4));

			assertEquals(0, posicao.quantidadeAcoes());
			assertEquals(new BigDecimal("0.00"), posicao.saldoAtual());
			assertEquals(new BigDecimal("0.00"), posicao.rendimentoPercentual());
		}

		@Test
		void posicaoContinuaZeradaSemNegociacaoNoDia() {
			PosicaoCarteira posicao = calculadora.calcularPosicaoEm(LocalDate.of(2020, 5, 5));

			assertEquals(0, posicao.quantidadeAcoes());
			assertEquals(new BigDecimal("0.00"), posicao.saldoAtual());
			assertEquals(new BigDecimal("0.00"), posicao.rendimentoPercentual());
		}

		@Test
		void reaberturaDePosicaoAPartirDoZero() {
			PosicaoCarteira posicao = calculadora.calcularPosicaoEm(LocalDate.of(2020, 5, 6));

			assertEquals(6, posicao.quantidadeAcoes());
			assertEquals(new BigDecimal("246.00"), posicao.saldoAtual());
			assertEquals(new BigDecimal("-2.38"), posicao.rendimentoPercentual());
		}
	}

	@Nested
	class PosicoesVendidasADescoberto {

		private CalculadoraDeRendimentosZgInvest calculadora;

		@BeforeEach
		void montarCarteiraShort() {
			List<Negociacao> negociacoes = List.of(
					new Negociacao(LocalDate.of(2020, 1, 2), OperacaoNegociacao.VENDA, 10, new BigDecimal("20.00")),
					new Negociacao(LocalDate.of(2020, 1, 3), OperacaoNegociacao.VENDA, 5, new BigDecimal("18.00")),
					new Negociacao(LocalDate.of(2020, 1, 6), OperacaoNegociacao.COMPRA, 5, new BigDecimal("17.00"))
			);

			Map<LocalDate, BigDecimal> precosFechamento = Map.of(
					LocalDate.of(2020, 1, 2), new BigDecimal("20.00"),
					LocalDate.of(2020, 1, 3), new BigDecimal("19.00"),
					LocalDate.of(2020, 1, 5), new BigDecimal("21.00"),
					LocalDate.of(2020, 1, 6), new BigDecimal("17.50")
			);

			calculadora = new CalculadoraDeRendimentosZgInvest(negociacoes, precosFechamento);
		}

		@Test
		void lucroEmPosicaoVendidaQuandoPrecoCai() {
			PosicaoCarteira posicao = calculadora.calcularPosicaoEm(LocalDate.of(2020, 1, 3));

			assertEquals(-15, posicao.quantidadeAcoes());
			assertEquals(new BigDecimal("-285.00"), posicao.saldoAtual());
			assertEquals(new BigDecimal("1.70"), posicao.rendimentoPercentual());
		}

		@Test
		void prejuizoEmPosicaoVendidaQuandoPrecoSobe() {
			PosicaoCarteira posicao = calculadora.calcularPosicaoEm(LocalDate.of(2020, 1, 5));

			assertEquals(-15, posicao.quantidadeAcoes());
			assertEquals(new BigDecimal("-315.00"), posicao.saldoAtual());
			assertEquals(new BigDecimal("-8.63"), posicao.rendimentoPercentual());
		}

		@Test
		void coberturaParcialMantemCustoMedio() {
			PosicaoCarteira posicao = calculadora.calcularPosicaoEm(LocalDate.of(2020, 1, 6));

			assertEquals(-10, posicao.quantidadeAcoes());
			assertEquals(new BigDecimal("-175.00"), posicao.saldoAtual());
			assertEquals(new BigDecimal("9.46"), posicao.rendimentoPercentual());
		}
	}

	@Nested
	class InversaoDeSinalFlipLongShort {

		@Test
		void flipDeCompradoParaVendido() {
			List<Negociacao> negociacoes = List.of(
					new Negociacao(LocalDate.of(2020, 2, 3), OperacaoNegociacao.COMPRA, 10, new BigDecimal("50.00")),
					new Negociacao(LocalDate.of(2020, 2, 4), OperacaoNegociacao.VENDA, 15, new BigDecimal("55.00"))
			);

			Map<LocalDate, BigDecimal> precosFechamento = Map.of(
					LocalDate.of(2020, 2, 3), new BigDecimal("52.00"),
					LocalDate.of(2020, 2, 4), new BigDecimal("53.00"),
					LocalDate.of(2020, 2, 5), new BigDecimal("58.00")
			);

			CalculadoraDeRendimentosZgInvest calculadora =
					new CalculadoraDeRendimentosZgInvest(negociacoes, precosFechamento);

			PosicaoCarteira antesDoFlip = calculadora.calcularPosicaoEm(LocalDate.of(2020, 2, 3));
			assertEquals(10, antesDoFlip.quantidadeAcoes());
			assertEquals(new BigDecimal("520.00"), antesDoFlip.saldoAtual());
			assertEquals(new BigDecimal("4.00"), antesDoFlip.rendimentoPercentual());

			PosicaoCarteira noDiaDoFlip = calculadora.calcularPosicaoEm(LocalDate.of(2020, 2, 4));
			assertEquals(-5, noDiaDoFlip.quantidadeAcoes());
			assertEquals(new BigDecimal("-265.00"), noDiaDoFlip.saldoAtual());
			assertEquals(new BigDecimal("3.63"), noDiaDoFlip.rendimentoPercentual());

			PosicaoCarteira depoisDoFlip = calculadora.calcularPosicaoEm(LocalDate.of(2020, 2, 5));
			assertEquals(-5, depoisDoFlip.quantidadeAcoes());
			assertEquals(new BigDecimal("-290.00"), depoisDoFlip.saldoAtual());
			assertEquals(new BigDecimal("-5.45"), depoisDoFlip.rendimentoPercentual());
		}

		@Test
		void flipDeVendidoParaComprado() {
			List<Negociacao> negociacoes = List.of(
					new Negociacao(LocalDate.of(2020, 3, 2), OperacaoNegociacao.VENDA, 8, new BigDecimal("30.00")),
					new Negociacao(LocalDate.of(2020, 3, 3), OperacaoNegociacao.COMPRA, 20, new BigDecimal("28.00"))
			);

			Map<LocalDate, BigDecimal> precosFechamento = Map.of(
					LocalDate.of(2020, 3, 2), new BigDecimal("31.00"),
					LocalDate.of(2020, 3, 3), new BigDecimal("29.00"),
					LocalDate.of(2020, 3, 4), new BigDecimal("26.00")
			);

			CalculadoraDeRendimentosZgInvest calculadora =
					new CalculadoraDeRendimentosZgInvest(negociacoes, precosFechamento);

			PosicaoCarteira antesDoFlip = calculadora.calcularPosicaoEm(LocalDate.of(2020, 3, 2));
			assertEquals(-8, antesDoFlip.quantidadeAcoes());
			assertEquals(new BigDecimal("-248.00"), antesDoFlip.saldoAtual());
			assertEquals(new BigDecimal("-3.33"), antesDoFlip.rendimentoPercentual());

			PosicaoCarteira noDiaDoFlip = calculadora.calcularPosicaoEm(LocalDate.of(2020, 3, 3));
			assertEquals(12, noDiaDoFlip.quantidadeAcoes());
			assertEquals(new BigDecimal("348.00"), noDiaDoFlip.saldoAtual());
			assertEquals(new BigDecimal("3.57"), noDiaDoFlip.rendimentoPercentual());

			PosicaoCarteira depoisDoFlip = calculadora.calcularPosicaoEm(LocalDate.of(2020, 3, 4));
			assertEquals(12, depoisDoFlip.quantidadeAcoes());
			assertEquals(new BigDecimal("312.00"), depoisDoFlip.saldoAtual());
			assertEquals(new BigDecimal("-7.14"), depoisDoFlip.rendimentoPercentual());
		}
	}

	@Nested
	class ArredondamentoETruncamento {

		@Test
		void custoMedioETruncadoParaBaixoAoEstenderPosicao() {
			List<Negociacao> negociacoes = List.of(
					new Negociacao(LocalDate.of(2020, 7, 1), OperacaoNegociacao.COMPRA, 3, new BigDecimal("10.00")),
					new Negociacao(LocalDate.of(2020, 7, 2), OperacaoNegociacao.COMPRA, 1, new BigDecimal("10.99"))
			);

			Map<LocalDate, BigDecimal> precosFechamento = Map.of(
					LocalDate.of(2020, 7, 2), new BigDecimal("10.30")
			);

			CalculadoraDeRendimentosZgInvest calculadora =
					new CalculadoraDeRendimentosZgInvest(negociacoes, precosFechamento);

			PosicaoCarteira posicao = calculadora.calcularPosicaoEm(LocalDate.of(2020, 7, 2));

			assertEquals(4, posicao.quantidadeAcoes());
			assertEquals(new BigDecimal("41.20"), posicao.saldoAtual());
			assertEquals(new BigDecimal("0.58"), posicao.rendimentoPercentual());
		}

		@Test
		void custoMedioETruncadoParaBaixoAoAbrirNovaPosicaoNoFlip() {
			List<Negociacao> negociacoes = List.of(
					new Negociacao(LocalDate.of(2020, 8, 3), OperacaoNegociacao.COMPRA, 10, new BigDecimal("20.00")),
					new Negociacao(LocalDate.of(2020, 8, 4), OperacaoNegociacao.VENDA, 15, new BigDecimal("21.999"))
			);

			Map<LocalDate, BigDecimal> precosFechamento = Map.of(
					LocalDate.of(2020, 8, 4), new BigDecimal("20.50")
			);

			CalculadoraDeRendimentosZgInvest calculadora =
					new CalculadoraDeRendimentosZgInvest(negociacoes, precosFechamento);

			PosicaoCarteira posicao = calculadora.calcularPosicaoEm(LocalDate.of(2020, 8, 4));

			assertEquals(-5, posicao.quantidadeAcoes());
			assertEquals(new BigDecimal("-102.50"), posicao.saldoAtual());
			assertEquals(new BigDecimal("6.77"), posicao.rendimentoPercentual());
		}
	}

	@Nested
	class ValidacaoDeDatasLimite {

		private CalculadoraDeRendimentosZgInvest calculadora;

		@BeforeEach
		void montarCarteiraComUmaUnicaNegociacao() {
			List<Negociacao> negociacoes = List.of(
					new Negociacao(LocalDate.of(2020, 6, 10), OperacaoNegociacao.COMPRA, 5, new BigDecimal("100.00"))
			);

			Map<LocalDate, BigDecimal> precosFechamento = Map.of(
					LocalDate.of(2020, 6, 1), new BigDecimal("90.00"),
					LocalDate.of(2020, 6, 10), new BigDecimal("105.00")
			);

			calculadora = new CalculadoraDeRendimentosZgInvest(negociacoes, precosFechamento);
		}

		@Test
		void dataAnteriorAPrimeiraNegociacaoRetornaPosicaoZeradaSemExcecao() {
			PosicaoCarteira posicao = calculadora.calcularPosicaoEm(LocalDate.of(2020, 6, 1));

			assertEquals(0, posicao.quantidadeAcoes());
			assertEquals(new BigDecimal("0.00"), posicao.saldoAtual());
			assertEquals(new BigDecimal("0.00"), posicao.rendimentoPercentual());
		}

		@Test
		void dataDaUnicaNegociacao() {
			PosicaoCarteira posicao = calculadora.calcularPosicaoEm(LocalDate.of(2020, 6, 10));

			assertEquals(5, posicao.quantidadeAcoes());
			assertEquals(new BigDecimal("525.00"), posicao.saldoAtual());
			assertEquals(new BigDecimal("5.00"), posicao.rendimentoPercentual());
		}

		@Test
		void lancaExcecaoQuandoNaoHaPrecoCadastradoParaAData() {
			assertThrows(IllegalArgumentException.class,
					() -> calculadora.calcularPosicaoEm(LocalDate.of(2020, 6, 15)));
		}
	}
}
