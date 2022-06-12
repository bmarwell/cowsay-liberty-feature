package io.github.bmarwell.liberty.cowsay.it.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public abstract class AbstractCowsayIT {

  @Test
  public void assertHasCow() {
    final String logfilePath = System.getProperty("wlp.logfile");

    assertThat(Assertions.linesOf(Paths.get(logfilePath).toFile()))
        .anyMatch(line -> line.contains("COWSY0001I:"))
    ;
  }

}
