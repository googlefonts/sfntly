/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.typography.font.tools.fontinfo;

import com.google.typography.font.sfntly.Font;
import com.google.typography.font.sfntly.FontFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.logging.Logger;

/**
 * A tool to report a summary of a font's information.
 * @author Brian Stell
 */
public class ReportFontSummary {
  private static final Logger logger =
    Logger.getLogger(Font.class.getCanonicalName());
  
  /**
   * Main function: read input args, create and report the font summary.
   * 
   * @param args - command line arguments 
   */
  public static void main(String[] args) {

    boolean reportCsv = false;

    int argPos = 0;
    for (; argPos < args.length; argPos++) {
      String option = null; 
      if (args[argPos].charAt(0) != '-') {
        break;  
      }
      option = args[argPos].substring(1);

      if (option == null) { 
        printUsage();
        System.exit(0);
      }
      if (option.equals("csv")) {
        reportCsv = true;
      } else {
        printUsage();
        System.exit(0);
      }
    }

    boolean reportedCsvHeader = false;
    FontFactory fontFactory = FontFactory.getInstance();

    for (; argPos < args.length; argPos++) {
      ReportFontSummary fontinfo = new ReportFontSummary();
      File fontFile = null; 

      try {
        fontFile = new File(args[argPos]);
        FileInputStream fis = new FileInputStream(fontFile);
        long length = fontFile.length();
        Font[] fontArray = fontFactory.loadFonts(fis);
        for (Font font : fontArray) {
          FontSummary fontSummary = new FontSummary(font, length);
          if (reportCsv) {
            if (!reportedCsvHeader) {
              reportedCsvHeader = true;
              System.out.println("Family Name,Style,Version,File Size,Cmap,# CodePoints," 
                  + "# Glyphs,#Glyph Bytes,# Instruction Bytes");
            }
            fontinfo.reportSummaryCsv(fontSummary);
          } else {
            fontinfo.reportSummary(fontSummary);
          }
        }
      } catch (IOException e) {
        logger.severe(e.getLocalizedMessage());
        System.exit(0);
      }
    }
  }

  /**
   * Help message that describes usage and input parameters
   */
  private static final void printUsage() {
    System.err.println("FontInfo [options] fontfile [...]");
    System.err.println("\t-csv\tprint results as csv");
    System.err.println("\t-h,--help\tprint this help information");
  }

  /**
   * Report the summary in a CSV format
   * 
   * @param fontSummary the object that summarizes font values
   */
  public void reportSummary(FontSummary fontSummary) {
    DecimalFormat formatter = new DecimalFormat("#,###,###");
    System.out.println("     Family: " + fontSummary.getFontFamilyName());
    System.out.println("  Subfamily: " + fontSummary.getFontSubfamilyName());
    System.out.println("    Version: " + fontSummary.getVersion());
    System.out.println("     Length: " + formatter.format(fontSummary.getLength()));
    System.out.println("       CMap: " + fontSummary.getCMapName());
    System.out.println("Code Points: " + formatter.format(fontSummary.getNumCodePoints()));
    System.out.println("     Glyphs: " + formatter.format(fontSummary.getNumGlyphs()));
    System.out.println("Glyph Bytes: " + formatter.format(fontSummary.getGlyphsLength()));
    System.out.println("  BCI Bytes: " + formatter.format(fontSummary.getInstructionsLength()));
    System.out.println();
  }

  /**
   * Report the summary in a CSV format
   * 
   * @param fontSummary the object that summarizes font values
   */
  public void reportSummaryCsv(FontSummary fontSummary) {
    System.out.print(fontSummary.getFontFamilyName());
    System.out.print(",");
    System.out.print(fontSummary.getFontSubfamilyName());
    System.out.print(",");
    System.out.print(fontSummary.getVersion());
    System.out.print(",");
    System.out.print(fontSummary.getLength());
    System.out.print(",");
    System.out.print(fontSummary.getCMapName());
    System.out.print(",");
    System.out.print(fontSummary.getNumCodePoints());
    System.out.print(",");
    System.out.print(fontSummary.getNumGlyphs());
    System.out.print(",");
    System.out.print(fontSummary.getGlyphsLength());
    System.out.print(",");
    System.out.print(fontSummary.getInstructionsLength());
    System.out.println();
  }
}
