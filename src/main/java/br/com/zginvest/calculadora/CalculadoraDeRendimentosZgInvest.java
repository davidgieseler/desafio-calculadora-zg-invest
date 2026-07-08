package br.com.zginvest.calculadora;

import java.math.BigDecimal;
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
		return new PosicaoCarteira(data, -1, BigDecimal.ZERO, BigDecimal.ZERO);
	}
}
