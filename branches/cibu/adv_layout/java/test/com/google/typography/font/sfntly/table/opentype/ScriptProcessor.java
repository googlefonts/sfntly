// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.Tag;
import com.google.typography.font.sfntly.table.opentype.LayoutCommonTable.Builder.FeatureId;
import com.google.typography.font.sfntly.table.opentype.LayoutCommonTable.Builder.LangSysId;
import com.google.typography.font.sfntly.table.opentype.LayoutCommonTable.Builder.LookupId;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * @author dougfelt@google.com (Doug Felt)
 *
 */
public class ScriptProcessor {
  private LayoutCommonTable.Builder<?> builder;
  private int lineNum;
  private String currentLine;
  private String currentCmdName;
  private LangSysId<?> currentLangSys;
  private FeatureId<?> currentFeature;
  private Object currentLookup;
  
  private enum Command {
    langsys,
    feature,
    addf2ls,
    addl2f;
    
    static Command forName(String name) {
      name = name.trim().toLowerCase();
      if (name.equals("langsys")) return langsys;
      if (name.equals("feature")) return feature;
      if (name.equals("setfeature")) return addf2ls;
      if (name.equals("setlookup")) return addl2f;
      return null;
    }
  }
  
  private enum Error {
    UNRECOGNIZED_COMMAND, INSUFFICIENT_ARGS, NO_CURRENT_LANGSYS, NO_CURRENT_FEATURE, NO_CURRENT_LOOKUP;
  }
  
  static class ScriptError extends IllegalArgumentException {
    final Error error;
    final int lineNum;
    final String context;
    
    private ScriptError(Error error, String cmdName, int lineNum, String context) {
      super(String.format("%s (cmd '%s') on line %3d: '%s'", error, cmdName, lineNum, context));
      this.error = error;
      this.lineNum = lineNum;
      this.context = context;
    }
  }
  
  private ScriptError newError(Error error) {
    return new ScriptError(error, currentCmdName, lineNum, currentLine);
  }
  
  private void notify(String message) {
    System.err.println(message);
  }
  
  public ScriptProcessor(LayoutCommonTable.Builder<?> builder) {
    this.builder = builder;
  }
  
  private void reset() {
    lineNum = 0;
  }
  
  public void runScript(String script) {
    try {
      runScript(new BufferedReader(new StringReader(script)));
    } catch (IOException e) {
      // should not happen
      e.printStackTrace();
    }
  }
  
  public void runScript(BufferedReader br) throws IOException {
    reset();
    String line;
    while (null != (line = br.readLine())) {
      ++lineNum;
      runCommand(line);
    }
  }

  public void runCommand(String line) {
    currentLine = line.trim();
    if (line.isEmpty() || line.startsWith("#")) {
      return;
    }
    String[] args = line.split(" ");
    Command cmd = Command.forName(args[0]);
    if (cmd == null) {
      throw newError(Error.UNRECOGNIZED_COMMAND);
    }
    
    switch(cmd) {
      case langsys: newLangSysCmd(args, 1); break;
      case feature: newFeatureCmd(args, 1); break;
      case addf2ls: addFeatureToLangSysCmd(); break;
      case addl2f: addLookupToFeatureCmd(); break;
      default: throw new IllegalStateException("unhandled command: " + cmd);
    }
  }
  
  private void newLangSysCmd(String[] args, int offset) {
    if (args.length < offset + 2) {
      throw newError(Error.INSUFFICIENT_ARGS);
    }
    String scriptName = args[offset];
    String languageName = args[offset + 1];
    ScriptTag scriptTag = ScriptTag.valueOf(scriptName);
    int stag;
    if (scriptTag == null) {
      notify(String.format("script '%s' is not a predefined script", scriptName));
      stag = Tag.intValue(scriptName);
    } else {
      stag = scriptTag.tag();
    }
    LanguageTag languageTag = LanguageTag.valueOf(languageName);
    int ltag;
    if (languageTag == null) {
      notify(String.format("language '%s' is not a predefined language system", languageName));
      ltag = Tag.intValue(languageName);
    } else {
      ltag = languageTag.tag();
    }
    currentLangSys = builder.newLangSys(stag, ltag);
  }
  
  private void newFeatureCmd(String[] args, int offset) {
    if (args.length < offset + 1) {
      throw newError(Error.INSUFFICIENT_ARGS);
    }
    String featureName = args[offset];
    FeatureTag featureTag = FeatureTag.valueOf(featureName);
    int ftag;
    if (featureTag == null) {
      notify(String.format("feature '%s' is not a predefined feature", featureName));
      ftag = Tag.intValue(featureName);
    } else {
      ftag = featureTag.tag();
    }
    currentFeature = builder.newFeature(ftag);
  }
  
  @SuppressWarnings({"rawtypes", "unchecked"})
  private void addFeatureToLangSysCmd() {
    if (currentLangSys == null) {
      throw newError(Error.NO_CURRENT_LANGSYS);
    }
    if (currentFeature == null) {
      throw newError(Error.NO_CURRENT_FEATURE);
    }
    builder.addFeatureToLangSys((FeatureId) currentFeature, (LangSysId) currentLangSys);
  }
  
  @SuppressWarnings({"rawtypes", "unchecked"})
  private void addLookupToFeatureCmd() {
    if (currentFeature == null) {
      throw newError(Error.NO_CURRENT_FEATURE);
    }
    if (currentLookup == null) {
      throw newError(Error.NO_CURRENT_LOOKUP);
    }
    builder.addLookupToFeature((LookupId) currentLookup, (FeatureId) currentFeature);
  }
}
