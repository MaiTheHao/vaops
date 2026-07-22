package c4f.vannang.vaops.shared.crypto;

import c4f.vannang.vaops.shared.enumeration.DeterministicHashAlgorithm;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

@Component
public class Sha512DeterministicHashStrategy implements DeterministicHashStrategy {

  @Override
  public String hash(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-512");
      byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hashBytes);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-512 algorithm not available", e);
    }
  }

  @Override
  public DeterministicHashAlgorithm getAlgorithm() {
    return DeterministicHashAlgorithm.SHA_512;
  }
}
