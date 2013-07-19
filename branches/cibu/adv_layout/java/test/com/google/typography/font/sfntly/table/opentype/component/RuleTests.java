package com.google.typography.font.sfntly.table.opentype.component;

import com.google.typography.font.sfntly.Font;
import com.google.typography.font.sfntly.FontFactory;
import com.google.typography.font.sfntly.Tag;
import com.google.typography.font.sfntly.table.core.CMapTable;
import com.google.typography.font.sfntly.table.opentype.GSubTable;
import com.google.typography.font.sfntly.table.opentype.ScriptListTable;
import com.google.typography.font.sfntly.table.opentype.ScriptTag;

import junit.framework.Assert;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Comparison data is generated from Harfbuzz by running:
 * util/hb-ot-shape-closure --no-glyph-names NotoSansMalayalam.ttf <text>
 */
public class RuleTests {

  public static String[][] langScriptData = { { "aa", "Latn" },
      { "ab", "Cyrl" },
      { "abq", "Cyrl" },
      { "ace", "Latn" },
      { "ach", "Latn" },
      { "ada", "Latn" },
      { "ady", "Cyrl" },
      { "ae", "Avst" },
      { "af", "Latn" },
      { "agq", "Latn" },
      { "aii", "Cyrl" },
      { "aii", "Syrc" },
      { "ain", "Kana", "Latn" },
      { "ak", "Latn" },
      { "akk", "Xsux" },
      { "ale", "Latn" },
      { "alt", "Cyrl" },
      { "am", "Ethi" },
      { "amo", "Latn" },
      { "an", "Latn" },
      { "anp", "Deva" },
      { "ar", "Arab" },
      { "ar", "Syrc" },
      { "arc", "Armi" },
      { "arn", "Latn" },
      { "arp", "Latn" },
      { "arw", "Latn" },
      { "as", "Beng" },
      { "asa", "Latn" },
      { "ast", "Latn" },
      { "av", "Cyrl" },
      { "awa", "Deva" },
      { "ay", "Latn" },
      { "az", "Arab", "Cyrl", "Latn" },
      { "ba", "Cyrl" },
      { "bal", "Arab", "Latn" },
      { "ban", "Latn" },
      { "ban", "Bali" },
      { "bas", "Latn" },
      { "bax", "Bamu" },
      { "bbc", "Latn" },
      { "bbc", "Batk" },
      { "be", "Cyrl" },
      { "bej", "Arab" },
      { "bem", "Latn" },
      { "bez", "Latn" },
      { "bfq", "Taml" },
      { "bft", "Arab" },
      { "bft", "Tibt" },
      { "bfy", "Deva" },
      { "bg", "Cyrl" },
      { "bh", "Deva", "Kthi" },
      { "bhb", "Deva" },
      { "bho", "Deva" },
      { "bi", "Latn" },
      { "bik", "Latn" },
      { "bin", "Latn" },
      { "bjj", "Deva" },
      { "bku", "Latn" },
      { "bku", "Buhd" },
      { "bla", "Latn" },
      { "blt", "Tavt" },
      { "bm", "Latn" },
      { "bn", "Beng" },
      { "bo", "Tibt" },
      { "bqv", "Latn" },
      { "br", "Latn" },
      { "bra", "Deva" },
      { "brx", "Deva" },
      { "bs", "Latn" },
      { "btv", "Deva" },
      { "bua", "Cyrl" },
      { "buc", "Latn" },
      { "bug", "Latn" },
      { "bug", "Bugi" },
      { "bya", "Latn" },
      { "byn", "Ethi" },
      { "ca", "Latn" },
      { "cad", "Latn" },
      { "car", "Latn" },
      { "cay", "Latn" },
      { "cch", "Latn" },
      { "ccp", "Beng" },
      { "ccp", "Cakm" },
      { "ce", "Cyrl" },
      { "ceb", "Latn" },
      { "cgg", "Latn" },
      { "ch", "Latn" },
      { "chk", "Latn" },
      { "chm", "Cyrl", "Latn" },
      { "chn", "Latn" },
      { "cho", "Latn" },
      { "chp", "Latn" },
      { "chp", "Cans" },
      { "chr", "Cher", "Latn" },
      { "chy", "Latn" },
      { "cja", "Arab" },
      { "cja", "Cham" },
      { "cjm", "Cham" },
      { "cjm", "Arab" },
      { "cjs", "Cyrl" },
      { "ckb", "Arab" },
      { "ckt", "Cyrl" },
      { "co", "Latn" },
      { "cop", "Arab", "Copt", "Grek" },
      { "cpe", "Latn" },
      { "cr", "Cans", "Latn" },
      { "crh", "Cyrl" },
      { "crk", "Cans" },
      { "cs", "Latn" },
      { "csb", "Latn" },
      { "cu", "Glag" },
      { "cv", "Cyrl" },
      { "cy", "Latn" },
      { "da", "Latn" },
      { "dak", "Latn" },
      { "dar", "Cyrl" },
      { "dav", "Latn" },
      { "de", "Latn" },
      { "de", "Runr" },
      { "del", "Latn" },
      { "den", "Latn" },
      { "den", "Cans" },
      { "dgr", "Latn" },
      { "din", "Latn" },
      { "dje", "Latn" },
      { "dng", "Cyrl" },
      { "doi", "Arab" },
      { "dsb", "Latn" },
      { "dua", "Latn" },
      { "dv", "Thaa" },
      { "dyo", "Arab" },
      { "dyo", "Latn" },
      { "dyu", "Latn" },
      { "dz", "Tibt" },
      { "ebu", "Latn" },
      { "ee", "Latn" },
      { "efi", "Latn" },
      { "egy", "Egyp" },
      { "eka", "Latn" },
      { "eky", "Kali" },
      { "el", "Grek" },
      { "en", "Latn" },
      { "en", "Dsrt", "Shaw" },
      { "eo", "Latn" },
      { "es", "Latn" },
      { "et", "Latn" },
      { "ett", "Ital", "Latn" },
      { "eu", "Latn" },
      { "evn", "Cyrl" },
      { "ewo", "Latn" },
      { "fa", "Arab" },
      { "fan", "Latn" },
      { "ff", "Latn" },
      { "fi", "Latn" },
      { "fil", "Latn" },
      { "fil", "Tglg" },
      { "fiu", "Latn" },
      { "fj", "Latn" },
      { "fo", "Latn" },
      { "fon", "Latn" },
      { "fr", "Latn" },
      { "frr", "Latn" },
      { "frs", "Latn" },
      { "fur", "Latn" },
      { "fy", "Latn" },
      { "ga", "Latn" },
      { "gaa", "Latn" },
      { "gag", "Latn" },
      { "gag", "Cyrl" },
      { "gay", "Latn" },
      { "gba", "Arab" },
      { "gbm", "Deva" },
      { "gcr", "Latn" },
      { "gd", "Latn" },
      { "gez", "Ethi" },
      { "gil", "Latn" },
      { "gl", "Latn" },
      { "gld", "Cyrl" },
      { "gn", "Latn" },
      { "gon", "Deva", "Telu" },
      { "gor", "Latn" },
      { "got", "Goth" },
      { "grb", "Latn" },
      { "grc", "Cprt", "Grek", "Linb" },
      { "grt", "Beng" },
      { "gsw", "Latn" },
      { "gu", "Gujr" },
      { "guz", "Latn" },
      { "gv", "Latn" },
      { "gwi", "Latn" },
      { "ha", "Arab", "Latn" },
      { "hai", "Latn" },
      { "haw", "Latn" },
      { "he", "Hebr" },
      { "hi", "Deva" },
      { "hil", "Latn" },
      { "hit", "Xsux" },
      { "hmn", "Latn" },
      { "hne", "Deva" },
      { "hnn", "Latn" },
      { "hnn", "Hano" },
      { "ho", "Latn" },
      { "hoc", "Deva" },
      { "hoj", "Deva" },
      { "hop", "Latn" },
      { "hr", "Latn" },
      { "hsb", "Latn" },
      { "ht", "Latn" },
      { "hu", "Latn" },
      { "hup", "Latn" },
      { "hy", "Armn" },
      { "hz", "Latn" },
      { "ia", "Latn" },
      { "iba", "Latn" },
      { "ibb", "Latn" },
      { "id", "Latn" },
      { "id", "Arab" },
      { "ig", "Latn" },
      { "ii", "Yiii" },
      { "ii", "Latn" },
      { "ik", "Latn" },
      { "ilo", "Latn" },
      { "inh", "Cyrl" },
      { "inh", "Arab", "Latn" },
      { "is", "Latn" },
      { "it", "Latn" },
      { "iu", "Cans" },
      { "iu", "Latn" },
      { "ja", "Jpan" },
      { "jmc", "Latn" },
      { "jpr", "Hebr" },
      { "jrb", "Hebr" },
      { "jv", "Latn" },
      { "jv", "Java" },
      { "ka", "Geor" },
      { "kaa", "Cyrl" },
      { "kab", "Latn" },
      { "kac", "Latn" },
      { "kaj", "Latn" },
      { "kam", "Latn" },
      { "kbd", "Cyrl" },
      { "kca", "Cyrl" },
      { "kcg", "Latn" },
      { "kde", "Latn" },
      { "kdt", "Thai" },
      { "kea", "Latn" },
      { "kfo", "Latn" },
      { "kfr", "Deva" },
      { "kg", "Latn" },
      { "kha", "Latn" },
      { "kha", "Beng" },
      { "khb", "Talu" },
      { "khq", "Latn" },
      { "kht", "Mymr" },
      { "ki", "Latn" },
      { "kj", "Latn" },
      { "kjh", "Cyrl" },
      { "kk", "Cyrl" },
      { "kk", "Arab" },
      { "kl", "Latn" },
      { "kln", "Latn" },
      { "km", "Khmr" },
      { "kmb", "Latn" },
      { "kn", "Knda" },
      { "ko", "Hang", "Kore" },
      { "koi", "Cyrl" },
      { "kok", "Deva" },
      { "kos", "Latn" },
      { "kpe", "Latn" },
      { "kpy", "Cyrl" },
      { "kr", "Latn" },
      { "krc", "Cyrl" },
      { "kri", "Latn" },
      { "krl", "Cyrl", "Latn" },
      { "kru", "Deva" },
      { "ks", "Arab", "Deva" },
      { "ksb", "Latn" },
      { "ksf", "Latn" },
      { "ksh", "Latn" },
      { "ku", "Arab", "Cyrl", "Latn" },
      { "kum", "Cyrl" },
      { "kut", "Latn" },
      { "kv", "Cyrl", "Latn" },
      { "kw", "Latn" },
      { "ky", "Arab", "Cyrl" },
      { "ky", "Latn" },
      { "kyu", "Kali" },
      { "la", "Latn" },
      { "lad", "Hebr" },
      { "lag", "Latn" },
      { "lah", "Arab" },
      { "lam", "Latn" },
      { "lb", "Latn" },
      { "lbe", "Cyrl" },
      { "lcp", "Thai" },
      { "lep", "Lepc" },
      { "lez", "Cyrl" },
      { "lg", "Latn" },
      { "li", "Latn" },
      { "lif", "Deva", "Limb" },
      { "lis", "Lisu" },
      { "lki", "Arab" },
      { "lmn", "Telu" },
      { "ln", "Latn" },
      { "lo", "Laoo" },
      { "lol", "Latn" },
      { "loz", "Latn" },
      { "lt", "Latn" },
      { "lu", "Latn" },
      { "lua", "Latn" },
      { "lui", "Latn" },
      { "lun", "Latn" },
      { "luo", "Latn" },
      { "lus", "Beng" },
      { "lut", "Latn" },
      { "luy", "Latn" },
      { "lv", "Latn" },
      { "lwl", "Thai" },
      { "mad", "Latn" },
      { "mag", "Deva" },
      { "mai", "Deva" },
      { "mak", "Latn" },
      { "mak", "Bugi" },
      { "man", "Latn", "Nkoo" },
      { "mas", "Latn" },
      { "mdf", "Cyrl" },
      { "mdh", "Latn" },
      { "mdr", "Latn" },
      { "mdr", "Bugi" },
      { "men", "Latn" },
      { "mer", "Latn" },
      { "mfe", "Latn" },
      { "mg", "Latn" },
      { "mgh", "Latn" },
      { "mh", "Latn" },
      { "mi", "Latn" },
      { "mic", "Latn" },
      { "min", "Latn" },
      { "mk", "Cyrl" },
      { "ml", "Mlym" },
      { "mn", "Cyrl", "Mong" },
      { "mn", "Phag" },
      { "mnc", "Mong" },
      { "mni", "Beng" },
      { "mni", "Mtei" },
      { "mnk", "Latn" },
      { "mns", "Cyrl" },
      { "mnw", "Mymr" },
      { "moh", "Latn" },
      { "mos", "Latn" },
      { "mr", "Deva" },
      { "ms", "Latn" },
      { "ms", "Arab" },
      { "mt", "Latn" },
      { "mua", "Latn" },
      { "mus", "Latn" },
      { "mwl", "Latn" },
      { "mwr", "Deva" },
      { "my", "Mymr" },
      { "myv", "Cyrl" },
      { "myz", "Mand" },
      { "na", "Latn" },
      { "nap", "Latn" },
      { "naq", "Latn" },
      { "nb", "Latn" },
      { "nd", "Latn" },
      { "nds", "Latn" },
      { "ne", "Deva" },
      { "new", "Deva" },
      { "ng", "Latn" },
      { "nia", "Latn" },
      { "niu", "Latn" },
      { "nl", "Latn" },
      { "nmg", "Latn" },
      { "nn", "Latn" },
      { "nod", "Lana" },
      { "nog", "Cyrl" },
      { "nqo", "Nkoo" },
      { "nr", "Latn" },
      { "nso", "Latn" },
      { "nus", "Latn" },
      { "nv", "Latn" },
      { "ny", "Latn" },
      { "nym", "Latn" },
      { "nyn", "Latn" },
      { "nyo", "Latn" },
      { "nzi", "Latn" },
      { "oc", "Latn" },
      { "oj", "Cans" },
      { "oj", "Latn" },
      { "om", "Latn" },
      { "om", "Ethi" },
      { "or", "Orya" },
      { "os", "Cyrl", "Latn" },
      { "osa", "Latn" },
      { "osc", "Ital", "Latn" },
      { "otk", "Orkh" },
      { "pa", "Guru" },
      { "pa", "Arab" },
      { "pag", "Latn" },
      { "pal", "Phli" },
      { "pam", "Latn" },
      { "pap", "Latn" },
      { "pau", "Latn" },
      { "peo", "Xpeo" },
      { "phn", "Phnx" },
      { "pi", "Deva", "Sinh", "Thai" },
      { "pl", "Latn" },
      { "pon", "Latn" },
      { "pra", "Brah", "Khar" },
      { "prd", "Arab" },
      { "prg", "Latn" },
      { "prs", "Arab" },
      { "ps", "Arab" },
      { "pt", "Latn" },
      { "qu", "Latn" },
      { "raj", "Latn" },
      { "rap", "Latn" },
      { "rar", "Latn" },
      { "rcf", "Latn" },
      { "rej", "Latn" },
      { "rej", "Rjng" },
      { "rjs", "Deva" },
      { "rkt", "Beng" },
      { "rm", "Latn" },
      { "rn", "Latn" },
      { "ro", "Latn" },
      { "ro", "Cyrl" },
      { "rof", "Latn" },
      { "rom", "Cyrl", "Latn" },
      { "ru", "Cyrl" },
      { "rup", "Latn" },
      { "rw", "Latn" },
      { "rwk", "Latn" },
      { "sa", "Deva", "Sinh" },
      { "sad", "Latn" },
      { "saf", "Latn" },
      { "sah", "Cyrl" },
      { "sam", "Hebr", "Samr" },
      { "saq", "Latn" },
      { "sas", "Latn" },
      { "sat", "Latn" },
      { "sat", "Beng", "Deva", "Olck", "Orya" },
      { "saz", "Saur" },
      { "sbp", "Latn" },
      { "sc", "Latn" },
      { "scn", "Latn" },
      { "sco", "Latn" },
      { "sd", "Arab", "Deva" },
      { "sdh", "Arab" },
      { "se", "Latn" },
      { "se", "Cyrl" },
      { "see", "Latn" },
      { "seh", "Latn" },
      { "sel", "Cyrl" },
      { "ses", "Latn" },
      { "sg", "Latn" },
      { "sga", "Latn", "Ogam" },
      { "shi", "Arab" },
      { "shi", "Tfng" },
      { "shn", "Mymr" },
      { "si", "Sinh" },
      { "sid", "Latn" },
      { "sk", "Latn" },
      { "sl", "Latn" },
      { "sm", "Latn" },
      { "sma", "Latn" },
      { "smi", "Latn" },
      { "smj", "Latn" },
      { "smn", "Latn" },
      { "sms", "Latn" },
      { "sn", "Latn" },
      { "snk", "Latn" },
      { "so", "Latn" },
      { "so", "Arab", "Osma" },
      { "son", "Latn" },
      { "sq", "Latn" },
      { "sr", "Cyrl", "Latn" },
      { "srn", "Latn" },
      { "srr", "Latn" },
      { "ss", "Latn" },
      { "ssy", "Latn" },
      { "st", "Latn" },
      { "su", "Latn" },
      { "su", "Sund" },
      { "suk", "Latn" },
      { "sus", "Latn" },
      { "sus", "Arab" },
      { "sv", "Latn" },
      { "sw", "Latn" },
      { "swb", "Arab" },
      { "swb", "Latn" },
      { "swc", "Latn" },
      { "syl", "Beng" },
      { "syl", "Sylo" },
      { "syr", "Syrc" },
      { "ta", "Taml" },
      { "tab", "Cyrl" },
      { "tbw", "Latn" },
      { "tbw", "Tagb" },
      { "tcy", "Knda" },
      { "tdd", "Tale" },
      { "te", "Telu" },
      { "tem", "Latn" },
      { "teo", "Latn" },
      { "ter", "Latn" },
      { "tet", "Latn" },
      { "tg", "Arab", "Cyrl", "Latn" },
      { "th", "Thai" },
      { "ti", "Ethi" },
      { "tig", "Ethi" },
      { "tiv", "Latn" },
      { "tk", "Arab", "Cyrl", "Latn" },
      { "tkl", "Latn" },
      { "tli", "Latn" },
      { "tmh", "Latn" },
      { "tn", "Latn" },
      { "to", "Latn" },
      { "tog", "Latn" },
      { "tpi", "Latn" },
      { "tr", "Latn" },
      { "tr", "Arab" },
      { "tru", "Latn" },
      { "tru", "Syrc" },
      { "trv", "Latn" },
      { "ts", "Latn" },
      { "tsg", "Latn" },
      { "tsi", "Latn" },
      { "tt", "Cyrl" },
      { "tts", "Thai" },
      { "tum", "Latn" },
      { "tut", "Cyrl" },
      { "tvl", "Latn" },
      { "twq", "Latn" },
      { "ty", "Latn" },
      { "tyv", "Cyrl" },
      { "tzm", "Latn", "Tfng" },
      { "ude", "Cyrl" },
      { "udm", "Cyrl" },
      { "udm", "Latn" },
      { "ug", "Arab" },
      { "ug", "Cyrl", "Latn" },
      { "uga", "Ugar" },
      { "uk", "Cyrl" },
      { "uli", "Latn" },
      { "umb", "Latn" },
      { "unr", "Beng", "Deva" },
      { "unx", "Beng", "Deva" },
      { "ur", "Arab" },
      { "uz", "Arab", "Cyrl", "Latn" },
      { "vai", "Vaii" },
      { "ve", "Latn" },
      { "vi", "Latn" },
      { "vi", "Hani" },
      { "vo", "Latn" },
      { "vot", "Latn" },
      { "vun", "Latn" },
      { "wa", "Latn" },
      { "wae", "Latn" },
      { "wak", "Latn" },
      { "wal", "Ethi" },
      { "war", "Latn" },
      { "was", "Latn" },
      { "wo", "Latn" },
      { "wo", "Arab" },
      { "xal", "Cyrl" },
      { "xcr", "Cari" },
      { "xh", "Latn" },
      { "xog", "Latn" },
      { "xpr", "Prti" },
      { "xsa", "Sarb" },
      { "xsr", "Deva" },
      { "xum", "Ital", "Latn" },
      { "yao", "Latn" },
      { "yap", "Latn" },
      { "yav", "Latn" },
      { "yi", "Hebr" },
      { "yo", "Latn" },
      { "yrk", "Cyrl" },
      { "yue", "Hans" },
      { "za", "Latn" },
      { "za", "Hans" },
      { "zap", "Latn" },
      { "zen", "Tfng" },
      { "zh", "Hans", "Hant" },
      { "zh", "Bopo", "Phag" },
      { "zu", "Latn" },
      { "zun", "Latn" },
      { "zza", "Arab" }, };

  @Test
  public void allFonts() throws IOException {
    List<File> fontFiles = new ArrayList<File>();
    getFontFiles(
        fontFiles, new File("/usr/local/google/home/cibu/sfntly/fonts"), "UrdType.ttf", true);

    for (File fontFile : fontFiles) {
      System.out.println(fontFile.getAbsolutePath());
      assertClosureByFontFile(fontFile);
    }
  }

  @Test
  public void fontWordPairs() throws IOException {
    assertFontWordPairs("/usr/local/google/home/cibu/sfntly/fonts/windows8/UrdType.ttf", "کے");
    assertFontWordPairs("/usr/local/google/home/cibu/sfntly/fonts/windows7/kokila.ttf", "श्रेणी");
    assertFontWordPairs("/usr/local/google/home/cibu/sfntly/fonts/windows7/msyi.ttf", "、");
    assertFontWordPairs("/usr/local/google/home/cibu/sfntly/fonts/windows7/calibri.ttf", "http",
        "align", "Потребител", "на");
    assertFontWordPairs(
        "/usr/local/google/home/cibu/sfntly/fonts/windows7/arabtype.ttf", "به", "اور​");
    assertFontWordPairs("/usr/local/google/home/cibu/sfntly/fonts/windows7/andlso.ttf", "ہے");
    assertFontWordPairs(
        "/usr/local/google/home/cibu/sfntly/fonts/windows7/tahoma.ttf", "ללא", "รัชกาลปัจจุบัน");
  }

  public void assertFontWordPairs(String fontFilename, String... wordArray) throws IOException {
    List<String> words = Arrays.asList(wordArray);
    File file = new File(fontFilename);
    Font font = getFont(file);
    List<Rule> featuredRules = Rule.featuredRules(font);

    Rule.dumpLookups(font);

    if (featuredRules == null) {
      return;
    }
    CMapTable cmapTable = font.getTable(Tag.cmap);
    assertClosureByWord(fontFilename, cmapTable, featuredRules, words);
  }

  private void assertClosureByFontFile(File fontFile) throws IOException {
    Font font = getFont(fontFile);
    List<Rule> featuredRules = Rule.featuredRules(font);
    if (featuredRules == null) {
      return;
    }
    CMapTable cmapTable = font.getTable(Tag.cmap);

    for (String lang : langsOfFont(font)) {
      String wordsFile = "/usr/local/google/home/cibu/sfntly/testdata/" + lang + ".txt";
      List<String> words = new ArrayList<String>();
      wordList(words, wordsFile);
      if (words.size() == 0) {
        continue;
      }
      System.out.println("   " + wordsFile);
      assertClosureByWord(fontFile.getAbsolutePath(), cmapTable, featuredRules, words);
    }
  }

  private void assertClosureByWord(
      String fontFileName, CMapTable cmapTable, List<Rule> featuredRules, List<String> words)
      throws IOException {

    Process proc = harfBuzzProc(fontFileName);
    harfBuzzWrite(proc, words);

    List<GlyphGroup> expecteds = harfBuzzRead(proc);
    proc.destroy();

    if (words.size() != expecteds.size()) {
      throw new IllegalStateException(
          "word size=" + words.size() + " expecteds size=" + expecteds.size());
    }
    for (int i = 0; i < words.size(); i++) {
      String word = words.get(i);
      GlyphGroup expected = expecteds.get(i);

      GlyphGroup glyphGroup = Rule.glyphGroupForText(word, cmapTable);
      GlyphGroup closure = Rule.closure(featuredRules, glyphGroup);

      if (expected.size() == 0 && closure.size() > 0) {
        System.err.println("Skipped: " + word);
      } else if (!expected.equals(closure)) {
        System.err.println("Didn't match " + word + ", HB: " + expected + ", mine: " + closure);
        Assert.assertEquals(word, expected, closure);
      }
    }
  }

  private Font getFont(File fontFile) throws IOException {
    Font[] fonts = loadFont(fontFile);
    if (fonts == null) {
      throw new IllegalArgumentException("No font found");
    }
    return fonts[0];
  }

  public static void getFontFiles(
      List<File> fonts, File sDir, String startFrom, boolean foundStart) {
    File[] faFiles = sDir.listFiles();
    for (File file : faFiles) {
      if (file.getName().endsWith(".ttf")) {
        if (foundStart || startFrom.endsWith(file.getName())) {
          foundStart = true;
          fonts.add(file);
        }
      }
      if (file.isDirectory()) {
        getFontFiles(fonts, file, startFrom, foundStart);
      }
    }
  }

  public static Map<String, ScriptTag> fontSpecificScript = new HashMap<String, ScriptTag>();
  public static Map<ScriptTag, Set<String>> scriptLangMap = new HashMap<ScriptTag, Set<String>>();
  static {
    fontSpecificScript.put("laoo", ScriptTag.lao);
    fontSpecificScript.put("yiii", ScriptTag.yi);
    fontSpecificScript.put("jpan", ScriptTag.kana);
    fontSpecificScript.put("kore", ScriptTag.hang);
    fontSpecificScript.put("nkoo", ScriptTag.nko);
    fontSpecificScript.put("vaii", ScriptTag.vai);
    fontSpecificScript.put("hans", ScriptTag.hani);
    fontSpecificScript.put("hant", ScriptTag.hani);

    createScriptLangMap();
    scriptLangMap.put(ScriptTag.DFLT, new HashSet<String>());
    scriptLangMap.put(ScriptTag.brai, new HashSet<String>());
    scriptLangMap.put(ScriptTag.math, new HashSet<String>());
    scriptLangMap.put(ScriptTag.musc, new HashSet<String>());
    scriptLangMap.put(ScriptTag.musi, new HashSet<String>());
    scriptLangMap.put(ScriptTag.mly2, scriptLangMap.get(ScriptTag.mlym));
    scriptLangMap.put(ScriptTag.mlm2, scriptLangMap.get(ScriptTag.mlym));
    scriptLangMap.put(ScriptTag.dev2, scriptLangMap.get(ScriptTag.deva));
    scriptLangMap.put(ScriptTag.mym2, scriptLangMap.get(ScriptTag.mymr));
    scriptLangMap.put(ScriptTag.tml2, scriptLangMap.get(ScriptTag.taml));
    scriptLangMap.put(ScriptTag.tel2, scriptLangMap.get(ScriptTag.telu));
    scriptLangMap.put(ScriptTag.knd2, scriptLangMap.get(ScriptTag.knda));
    scriptLangMap.put(ScriptTag.gur2, scriptLangMap.get(ScriptTag.guru));
    scriptLangMap.put(ScriptTag.gjr2, scriptLangMap.get(ScriptTag.gujr));
    scriptLangMap.put(ScriptTag.bng2, scriptLangMap.get(ScriptTag.beng));
    scriptLangMap.put(ScriptTag.ory2, scriptLangMap.get(ScriptTag.orya));
    scriptLangMap.put(ScriptTag.jamo, scriptLangMap.get(ScriptTag.hang));
  }

  private static void createScriptLangMap() {
    for (String[] entry : langScriptData) {
      String lang = entry[0];
      for (int i = 1; i < entry.length; i++) {
        String script = entry[i].toLowerCase();
        ScriptTag scriptTag = fontSpecificScript.containsKey(script) ? fontSpecificScript.get(
            script)
            : ScriptTag.valueOf(script);
        addLangScriptMap(lang, scriptTag);
      }
    }
  }

  private static void addLangScriptMap(String lang, ScriptTag scriptTag) {
    if (!scriptLangMap.containsKey(scriptTag)) {
      scriptLangMap.put(scriptTag, new HashSet<String>());
    }
    Set<String> langs = scriptLangMap.get(scriptTag);
    langs.add(lang);
  }

  private static List<String> langsOfFont(Font font) {
    GSubTable gsub = font.getTable(Tag.GSUB);
    if (gsub == null) {
      throw new IllegalArgumentException("No GSUB Table found");
    }

    List<String> langs = new ArrayList<String>();
    ScriptListTable scriptList = gsub.scriptList();
    System.out.print("   ");
    for (int i = 0; i < scriptList.count(); i++) {
      ScriptTag script = scriptList.scriptAt(i);
      if (!scriptLangMap.containsKey(script)) {
        throw new IllegalArgumentException("Entry not found: " + script);
      }
      System.out.print(script + " ");
      langs.addAll(scriptLangMap.get(script));
    }
    System.out.println();
    // System.out.println(langs);
    return langs;
  }

  public static Font[] loadFont(File file) throws IOException {
    FontFactory fontFactory = FontFactory.getInstance();
    fontFactory.fingerprintFont(true);
    FileInputStream is = null;
    try {
      is = new FileInputStream(file);
      return fontFactory.loadFonts(is);
    } catch (FileNotFoundException e) {
      System.err.println("Could not load the font : " + file.getName());
      return null;
    } finally {
      if (is != null) {
        is.close();
      }
    }
  }

  public static void wordList(List<String> words, String fileName) {
    Scanner scanner;
    try {
      scanner = new Scanner(new File(fileName));
    } catch (FileNotFoundException e) {
      return;
    }
    int count = 0;
    while (scanner.hasNextLine()) {
      String[] fields = scanner.nextLine().split(" ");
      String word = fields[0];
      if (count >= 3000) {
        break;
      }
      words.add(word);
      count++;
    }
    scanner.close();
  }

  public static Process harfBuzzProc(String fontName) throws IOException {
    String[] commands = {
        "/usr/local/google/home/cibu/harfbuzz/harfbuzz-0.9.19/util/hb-ot-shape-closure",
        "--no-glyph-names", fontName };

    ProcessBuilder pb = new ProcessBuilder(commands);
    Process proc = pb.start();
    return proc;
  }

  public static void harfBuzzWrite(Process proc, List<String> words) {
    PrintWriter out = new PrintWriter(new OutputStreamWriter(proc.getOutputStream()), true);
    int i = 0;
    for (String word : words) {
      // System.out.println(word);
      out.println(word + "\n");
      i++;
    }
    out.close();
  }

  public static List<GlyphGroup> harfBuzzRead(Process proc) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    List<GlyphGroup> glyphSets = new ArrayList<GlyphGroup>();
    String out;
    while ((out = in.readLine()) != null) {
      in.skip(1);
      // System.out.println(out);
      GlyphGroup glyphSet = new GlyphGroup();
      if (out.length() > 0) {
        String[] tokens = out.split(" ");
        for (String intStr : tokens) {
          glyphSet.add(Integer.parseInt(intStr));
        }
      }
      glyphSets.add(glyphSet);
    }
    return glyphSets;
  }
}
