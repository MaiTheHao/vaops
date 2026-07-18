package c4f.vannang.vaops.shared.service;

import c4f.vannang.vaops.shared.enumeration.DeterministicHashAlgorithm;

public interface DeterministicHashStrategy {

  String hash(String input);

  DeterministicHashAlgorithm getAlgorithm();
}
