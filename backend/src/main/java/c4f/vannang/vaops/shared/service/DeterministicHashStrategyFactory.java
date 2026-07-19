package c4f.vannang.vaops.shared.service;

import c4f.vannang.vaops.shared.enumeration.DeterministicHashAlgorithm;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DeterministicHashStrategyFactory {

  private final Map<DeterministicHashAlgorithm, DeterministicHashStrategy> strategies;

  public DeterministicHashStrategyFactory(List<DeterministicHashStrategy> strategyList) {
    this.strategies = strategyList.stream().collect(Collectors.toMap(s -> s.getAlgorithm(), Function.identity()));
  }

  public DeterministicHashStrategy getStrategy(DeterministicHashAlgorithm algorithm) {
    return Optional.ofNullable(strategies.get(algorithm))
        .orElseThrow(() ->
            new IllegalArgumentException("Unsupported deterministic hash algorithm: " + algorithm));
  }
}
