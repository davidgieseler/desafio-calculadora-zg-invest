# CalculadoraDeRendimentosZgInvest

Calculadora do rendimento da carteira de um investidor em uma ação, considerando o histórico de negociações (compras e vendas, inclusive vendas a descoberto) e os preços de fechamento diários do ativo.

Dada uma data qualquer, a calculadora responde: **quantas ações a carteira tem, quanto essa posição vale hoje (R$) e qual o rendimento acumulado (%)** — independentemente de ter havido ou não uma negociação naquele dia exato.

## Índice

- [Requisitos](#requisitos)
- [Executando os testes](#executando-os-testes)
- [Compilando o projeto](#compilando-o-projeto)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Modelo de domínio](#modelo-de-domínio)
- [Regras de negócio](#regras-de-negócio)
- [Uso](#uso)
- [Suíte de testes](#suíte-de-testes)
- [Premissas assumidas](#premissas-assumidas)

## Requisitos

- Java 17 (o projeto usa `zulu-17.46.27`, ver [`.tool-versions`](.tool-versions))
- Não é necessário ter o Gradle instalado — o projeto usa o Gradle Wrapper (`gradlew` / `gradlew.bat`)

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
├── OperacaoNegociacao.java                  # COMPRA ou VENDA
└── PosicaoCarteira.java                     # quantidade, saldo e rendimento em uma data

src/test/java/br/com/zginvest/calculadora/
└── CalculadoraDeRendimentosZgInvestTest.java # suíte de testes (organizada em @Nested por cenário)
```

## Modelo de domínio

| Tipo | Natureza | Campos |
|---|---|---|
| `Negociacao` | record (entrada) | `data: LocalDate`, `tipo: OperacaoNegociacao`, `quantidade: int`, `preco: BigDecimal` |
| `OperacaoNegociacao` | enum | `COMPRA`, `VENDA` |
| `PosicaoCarteira` | record (saída) | `data: LocalDate`, `quantidadeAcoes: long`, `saldoAtual: BigDecimal`, `rendimentoPercentual: BigDecimal` |

`CalculadoraDeRendimentosZgInvest` recebe, no construtor, a lista completa de negociações e o mapa de preços de fechamento (`Map<LocalDate, BigDecimal>`), e expõe um único método:

```java
PosicaoCarteira calcularPosicaoEm(LocalDate data)
```

`quantidadeAcoes` é assinada: valor positivo representa posição comprada ("long"), valor negativo representa posição vendida a descoberto ("short"). Não há um campo separado indicando a direção da posição — o sinal da própria quantidade já carrega essa informação.

## Regras de negócio

### Long (posição comprada)

Abertura com compra, encerramento com venda. Há lucro quando o preço de saída é maior que o custo médio de entrada.

### Short (venda a descoberto)

O fluxo é invertido: a venda ocorre antes da recompra. O investidor vende um ativo que não possui (esperando desvalorização) e depois recompra por um preço inferior. Há lucro quando o preço de recompra é menor que o preço médio da venda inicial.

Como `OperacaoNegociacao` só distingue `COMPRA`/`VENDA` — não existe um campo explícito de "abertura de short" — a posição vendida é **inferida**: uma `VENDA` que excede a quantidade atualmente possuída abre (ou estende) uma posição negativa; uma `COMPRA` feita com a posição negativa cobre o short e, se sobrar quantidade, abre uma posição comprada nova com o excedente.

### Algoritmo (custo médio ponderado com inversão de sinal)

A posição é rastreada como um par `(quantidade assinada, custo médio)`. Cada negociação processada em ordem cronológica (múltiplas negociações no mesmo dia mantêm a ordem em que foram informadas) atualiza esse estado seguindo um de três casos:

| Caso | Condição | Efeito |
|---|---|---|
| **A** — mesmo sentido ou abertura do zero | quantidade atual é `0`, ou tem o mesmo sinal do delta da operação | Custo médio recalculado pela média ponderada entre a posição existente e a nova operação |
| **B** — sentido oposto, sem cruzar zero | `\|delta\| <= \|quantidade atual\|` | Quantidade reduz (podendo zerar exatamente); custo médio **não muda** |
| **C** — sentido oposto, cruzando zero (flip) | `\|delta\| > \|quantidade atual\|` | Posição anterior é encerrada; o excedente abre uma posição nova no sentido contrário, com custo médio igual ao preço desta operação |

Ao consultar uma data:

- `saldoAtual = quantidade × preço de fechamento na data`
- `rendimentoPercentual = 0,00%` se a posição estiver zerada (evita divisão por zero); caso contrário, `(quantidade × (preço de fechamento − custo médio)) / (|quantidade| × custo médio) × 100` — fórmula assinada que funciona tanto para long (lucro quando o preço sobe) quanto para short (lucro quando o preço cai)

### Arredondamento

Todos os valores (custo médio, saldo, rendimento) têm **no máximo duas casas decimais**, truncadas com `RoundingMode.DOWN` (trunca em direção a zero) — inclusive nos valores intermediários, que já são truncados antes de alimentar o próximo cálculo. Esse comportamento foi confirmado por engenharia reversa do exemplo oficial do desafio: usar arredondamento padrão (`HALF_UP`) produz valores diferentes dos esperados (ex.: 44,05% viraria 44,06%).

### Validação

- Se não houver preço de fechamento cadastrado para a data solicitada, é lançada `IllegalArgumentException`.
- Uma data anterior a qualquer negociação, mas com preço cadastrado, retorna posição zerada (`quantidade=0`, `saldoAtual=0.00`, `rendimentoPercentual=0.00`) sem lançar exceção.

## Uso

### Posição comprada (long)

```java
List<Negociacao> negociacoes = List.of(
        new Negociacao(LocalDate.of(2020, 3, 1), OperacaoNegociacao.COMPRA, 20, new BigDecimal("8")),
        new Negociacao(LocalDate.of(2020, 4, 1), OperacaoNegociacao.COMPRA, 10, new BigDecimal("9"))
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

### Posição vendida a descoberto (short)

```java
List<Negociacao> negociacoes = List.of(
        new Negociacao(LocalDate.of(2020, 1, 2), OperacaoNegociacao.VENDA, 10, new BigDecimal("20.00")),
        new Negociacao(LocalDate.of(2020, 1, 6), OperacaoNegociacao.COMPRA, 5, new BigDecimal("17.00"))
);

Map<LocalDate, BigDecimal> precosFechamento = Map.of(
        LocalDate.of(2020, 1, 2), new BigDecimal("20.00"),
        LocalDate.of(2020, 1, 6), new BigDecimal("17.50")
);

CalculadoraDeRendimentosZgInvest calculadora =
        new CalculadoraDeRendimentosZgInvest(negociacoes, precosFechamento);

PosicaoCarteira posicao = calculadora.calcularPosicaoEm(LocalDate.of(2020, 1, 6));

posicao.quantidadeAcoes();       // -5   (posição vendida, cobertura parcial)
posicao.saldoAtual();            // -87.50
posicao.rendimentoPercentual();  // 12.50 (lucro, pois recomprou mais barato do que vendeu)
```

`calcularPosicaoEm` considera todas as negociações até a data informada (inclusive) e exige que haja um preço de fechamento cadastrado para essa data, lançando `IllegalArgumentException` caso contrário.

## Suíte de testes

A suíte (`CalculadoraDeRendimentosZgInvestTest`) é organizada em classes `@Nested`, cada uma isolando um cenário:

| Classe `@Nested` | Cobertura |
|---|---|
| `ExemploOficialAbcd3` | Exemplo oficial do desafio (long, custo médio ponderado, múltiplas negociações no mesmo dia) |
| `PosicoesCompradasCasosDeBorda` | Zerar a posição comprada exatamente e reabrir a partir do zero |
| `PosicoesVendidasADescoberto` | Abertura, extensão e cobertura parcial de posição vendida — lucro e prejuízo |
| `InversaoDeSinalFlipLongShort` | Inversão de sinal num único trade, nos dois sentidos (long→short e short→long) |
| `ArredondamentoETruncamento` | Casos desenhados para discriminar truncamento (`DOWN`) de arredondamento padrão (`HALF_UP`) |
| `ValidacaoDeDatasLimite` | Data antes da primeira negociação, data com negociação e data sem preço cadastrado |

## Premissas assumidas

- A quantidade de cada `Negociacao` é sempre um valor positivo (o domínio do desafio não contempla negociações com quantidade zero ou negativa).
- Múltiplas negociações no mesmo dia são processadas na ordem em que aparecem na lista de entrada (o construtor ordena por data de forma estável, preservando essa ordem relativa).

## Contato

* **Outlook:** [david.gieseler@hotmail.com](mailto:david.gieseler@hotmail.com)
* **Gmail:** [davidmgieseler@gmail.com](mailto:davidmgieseler@gmail.com)
* **LinkedIn:** [David Gieseler](https://www.linkedin.com/in/davidmgieseler/)