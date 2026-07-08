# CalculadoraDeRendimentosZgInvest

Calculadora do rendimento da carteira de um investidor em uma ação, considerando o histórico de compras/vendas e os preços de fechamento diários do ativo.

## Requisitos

- Java 17 (o projeto usa `zulu-17.46.27`, ver `.tool-versions`)
- Não é necessário ter o Gradle instalado — o projeto usa o Gradle Wrapper (`gradlew`)

## Executando os testes

```bash
./gradlew test
```

No Windows:

```bash
gradlew.bat test
```

O relatório de resultados é gerado em `build/reports/tests/test/index.html`.

## Compilando o projeto

```bash
./gradlew build
```

O `.jar` gerado fica em `build/libs/CalculadoraDeRendimentosZgInvest-1.0.0.jar`.

## Estrutura do projeto

```
src/main/java/br/com/zginvest/calculadora/
├── CalculadoraDeRendimentosZgInvest.java   # cálculo da posição da carteira em uma data
├── Negociacao.java                          # compra/venda registrada pelo investidor
├── PosicaoCarteira.java                     # quantidade, saldo e rendimento em uma data
└── TipoOperacao.java                        # COMPRA ou VENDA
```

## Uso

```java
List<Negociacao> negociacoes = List.of(
        new Negociacao(LocalDate.of(2020, 3, 1), TipoOperacao.COMPRA, 20, new BigDecimal("8")),
        new Negociacao(LocalDate.of(2020, 4, 1), TipoOperacao.COMPRA, 10, new BigDecimal("9"))
);

Map<LocalDate, BigDecimal> precosFechamento = Map.of(
        LocalDate.of(2020, 3, 31), new BigDecimal("10"),
        LocalDate.of(2020, 4, 1), new BigDecimal("11")
);

CalculadoraDeRendimentosZgInvest calculadora =
        new CalculadoraDeRendimentosZgInvest(negociacoes, precosFechamento);

PosicaoCarteira posicao = calculadora.calcularPosicaoEm(LocalDate.of(2020, 3, 31));

posicao.quantidadeAcoes();       // 20
posicao.saldoAtual();            // 200.00
posicao.rendimentoPercentual();  // 25.00
```

`calcularPosicaoEm` considera todas as negociações até a data informada (inclusive) e exige que haja um preço de fechamento cadastrado para essa data, lançando `IllegalArgumentException` caso contrário.
