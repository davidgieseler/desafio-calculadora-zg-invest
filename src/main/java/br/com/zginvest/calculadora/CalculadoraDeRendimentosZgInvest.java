package br.com.zginvest.calculadora;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CalculadoraDeRendimentosZgInvest {

	private final List<Negociacao> negociacoes;
	private final Map<LocalDate, BigDecimal> precosFechamento;

	public CalculadoraDeRendimentosZgInvest(List<Negociacao> negociacoes, Map<LocalDate, BigDecimal> precosFechamento) {
		this.negociacoes = negociacoes.stream()
				.sorted(Comparator.comparing(Negociacao::data))
				.collect(Collectors.toList());
		this.precosFechamento = precosFechamento;
	}

	public PosicaoCarteira calcularPosicaoEm(LocalDate data) {
		BigDecimal precoFechamento = precosFechamento.get(data);
		if (precoFechamento == null) {
			throw new IllegalArgumentException("Não há preço de fechamento cadastrado para " + data);
		}

		EstadoPosicao estado = EstadoPosicao.ZERO;
		for (Negociacao negociacao : negociacoes) {
			if (negociacao.data().isAfter(data)) {
				break;
			}
			estado = aplicar(estado, negociacao);
		}

		return montarPosicao(data, estado, precoFechamento);
	}

	private EstadoPosicao aplicar(EstadoPosicao atual, Negociacao negociacao) {
		long delta = negociacao.tipo() == OperacaoNegociacao.COMPRA ? negociacao.quantidade() : -negociacao.quantidade();
		long novaQtd = atual.quantidade() + delta;

		boolean mesmoSentidoOuAbertura = atual.quantidade() == 0 || Long.signum(atual.quantidade()) == Long.signum(delta);
		if (mesmoSentidoOuAbertura) {
			BigDecimal custoTotalAnterior = BigDecimal.valueOf(Math.abs(atual.quantidade())).multiply(atual.custoMedio());
			BigDecimal custoOperacao = BigDecimal.valueOf(negociacao.quantidade()).multiply(negociacao.preco());
			BigDecimal novoCustoMedio = custoTotalAnterior.add(custoOperacao)
					.divide(BigDecimal.valueOf(Math.abs(novaQtd)), 2, RoundingMode.DOWN);
			return new EstadoPosicao(novaQtd, novoCustoMedio);
		}

		boolean reduzSemCruzar = Math.abs(delta) <= Math.abs(atual.quantidade());
		if (reduzSemCruzar) {
			return new EstadoPosicao(novaQtd, atual.custoMedio());
		}

		return new EstadoPosicao(novaQtd, negociacao.preco().setScale(2, RoundingMode.DOWN));
	}

	private PosicaoCarteira montarPosicao(LocalDate data, EstadoPosicao estado, BigDecimal precoFechamento) {
		BigDecimal saldoAtual = BigDecimal.valueOf(estado.quantidade())
				.multiply(precoFechamento).setScale(2, RoundingMode.DOWN);

		BigDecimal custoTotal = BigDecimal.valueOf(Math.abs(estado.quantidade())).multiply(estado.custoMedio());

		BigDecimal rendimentoPercentual;
		if (custoTotal.compareTo(BigDecimal.ZERO) == 0) {
			rendimentoPercentual = BigDecimal.ZERO.setScale(2, RoundingMode.DOWN);
		} else {
			BigDecimal rendimentoReais = BigDecimal.valueOf(estado.quantidade())
					.multiply(precoFechamento.subtract(estado.custoMedio()));
			rendimentoPercentual = rendimentoReais.multiply(BigDecimal.valueOf(100))
					.divide(custoTotal, 2, RoundingMode.DOWN);
		}

		return new PosicaoCarteira(data, estado.quantidade(), saldoAtual, rendimentoPercentual);
	}

	private record EstadoPosicao(long quantidade, BigDecimal custoMedio) {
		private static final EstadoPosicao ZERO = new EstadoPosicao(0L, BigDecimal.ZERO.setScale(2, RoundingMode.DOWN));
	}
}
