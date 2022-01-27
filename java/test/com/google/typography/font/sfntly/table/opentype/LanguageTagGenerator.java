package com.google.typography.font.sfntly.table.opentype;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Updates the ISO 639-3 names in the {@link LanguageTag} enum. This is not completely automated but
 * still needs a little bit of manual correction work.
 */
public class LanguageTagGenerator {

  // http://www-01.sil.org/iso639-3/download.asp
  // http://www-01.sil.org/iso639-3/iso-639-3_Code_Tables_20171221.zip
  private static final String ISO_639_3_DATA = "iso-639-3_20171221.tab";

  public static void main(String[] args) {
    crosscheckWithIso639();
  }

  private static void crosscheckWithIso639() {
    List<String> errors = new ArrayList<>();
    List<String> enumValues = new ArrayList<>();

    Map<String, String> languageNameByIso3 = getIso639Languages();
    for (LanguageTag languageTag : LanguageTag.values()) {
      String languageSystem = languageTag.languageSystem();
      String tag = languageTag.name();

      if (languageTag.iso3List().size() != 1) {
        enumValues.add(
            String.format(
                "%s(\"%s\", null /* ambiguous */, \"%s\"),",
                tag, languageSystem, languageTag.iso3List));
        continue;
      }

      for (String iso3 : languageTag.iso3List()) {
        String iso639Name = languageNameByIso3.get(iso3);
        if (iso639Name == null) {
          errors.add(String.format("For tag %s, code %s is not in ISO 639-3.", tag, iso3));
          enumValues.add(
              String.format(
                  "%s(\"%s\", null /* not in ISO 639-3 */, \"%s\"),", tag, languageSystem, iso3));
        } else {
          enumValues.add(
              String.format("%s(\"%s\", \"%s\", \"%s\"),", tag, languageSystem, iso639Name, iso3));
          if (!iso639Name.equals(languageSystem)) {
            errors.add(
                String.format(
                    "For tag %s and code %s, languageSystem %s doesn't equal ISO 639-3 name %s.",
                    tag, iso3, languageSystem, iso639Name));
          }
        }
      }
    }

    for (String error : errors) {
      System.out.println(error);
    }

    for (String value : enumValues) {
      System.out.println(value);
    }
  }

  private static Map<String, String> getIso639Languages() {
    List<String> lines = new ArrayList<>();
    try {
      try (FileInputStream inputStream = new FileInputStream(ISO_639_3_DATA)) {
        BufferedReader rd =
            new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
        rd.readLine(); // Skip the header
        String line;
        while ((line = rd.readLine()) != null) {
          lines.add(line);
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    Map<String, String> languageNameByIso3 = new LinkedHashMap<>();

    Matcher m =
        Pattern.compile(
                ""
                    + "([a-z]{3})\t" // Id
                    + "([^\t]*)\t" // Part2B
                    + "([^\t]*)\t" // Part2T
                    + "([^\t]*)\t" // Part1
                    + "([^\t]*)\t" // Scope
                    + "([^\t]*)\t" // Language_Type
                    + "([\\p{L}[0-9]\\p{Space}!'()\\-./|\u02bc\u0301\u0303\u0331\u2019\u2021]+)\t" // Ref_Name
                    + "([^\t]*)" // Comment
                )
            .matcher("");
    for (String line : lines) {
      if (m.reset(line).matches()) {
        languageNameByIso3.put(m.group(1), m.group(7));
      } else {
        System.err.println("Unknown line: " + escape(line));
      }
    }
    return languageNameByIso3;
  }

  private static String escape(String str) {
    StringBuilder sb = new StringBuilder();
    int i = 0;
    while (i < str.length()) {
      int cp = str.codePointAt(i);
      if (' ' <= cp && cp <= '~' && cp != '\\') {
        sb.appendCodePoint(cp);
      } else {
        sb.append(String.format("\\u%04X", cp));
      }
      i += Character.charCount(cp);
    }
    return sb.toString();
  }
}
