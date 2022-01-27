package com.google.typography.font.sfntly.testutils;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class TestUtilsTest {

  @Test
  public void hexFrom() {
    Assertions.assertThat(TestUtils.fromHex("").size()).isEqualTo(0);

    Assertions.assertThat(TestUtils.fromHex("12 34 56 78").size()).isEqualTo(4);

    Assertions.assertThat(TestUtils.fromHex("12 34 56 78").readULong(0)).isEqualTo(0x12345678);
  }
}
