package com.google.typography.font.sfntly.table.opentype;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class LanguageTagTest {

    @Test
    public void testDataConsistency() {
        for (LanguageTag languageTag : LanguageTag.values()) {
            assertThat(languageTag.longName(), is(languageTag.longName().trim()));

            for (String iso3 : languageTag.iso3List()) {
                assertThat(iso3.replaceAll("^[a-z]{3}$", ""), is(""));
            }
        }
    }
}
