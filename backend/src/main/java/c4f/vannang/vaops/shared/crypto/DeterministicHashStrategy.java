package c4f.vannang.vaops.shared.crypto;

import c4f.vannang.vaops.shared.enumeration.DeterministicHashAlgorithm;

public interface DeterministicHashStrategy {

  String hash(String input);

  DeterministicHashAlgorithm getAlgorithm();
}
