/*
 * Copyright 2010 Google Inc. All Rights Reserved.
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

package com.google.typography.font.sfntly.table.core;

import com.google.typography.font.sfntly.Font;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.SfObjects;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.Header;
import com.google.typography.font.sfntly.table.SubTableContainerTable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

// TODO(stuartg): support format 1 name tables
/**
 * A Name table.
 *
 * @author Stuart Gill
 */
public final class NameTable extends SubTableContainerTable
    implements Iterable<NameTable.NameEntry> {

  /** Offsets to specific elements in the underlying data, relative to the start of the table. */
  private interface HeaderOffsets {
    int format = 0;
    int count = 2;
    int stringOffset = 4;
    int nameRecordStart = 6;

    // format 1 - offset from the end of the name records
    int langTagCount = 0;
    int langTagRecord = 2;
  }

  private interface NameRecord {
    int SIZE = 12;
    // Name Records
    int platformId = 0;
    int encodingId = 2;
    int languageId = 4;
    int nameId = 6;
    int stringLength = 8;
    int stringOffset = 10;
  }

  public enum NameId {
    Unknown(-1),
    CopyrightNotice(0),
    FontFamilyName(1),
    FontSubfamilyName(2),
    UniqueFontIdentifier(3),
    FullFontName(4),
    VersionString(5),
    PostscriptName(6),
    Trademark(7),
    ManufacturerName(8),
    Designer(9),
    Description(10),
    VendorURL(11),
    DesignerURL(12),
    LicenseDescription(13),
    LicenseInfoURL(14),
    Reserved15(15),
    PreferredFamily(16),
    PreferredSubfamily(17),
    CompatibleFullName(18),
    SampleText(19),
    PostscriptCID(20),
    WWSFamilyName(21),
    WWSSubfamilyName(22);

    private final int value;

    private NameId(int value) {
      this.value = value;
    }

    public int value() {
      return value;
    }

    public boolean equals(int value) {
      return value == this.value;
    }

    public static NameId valueOf(int value) {
      for (NameId name : NameId.values()) {
        if (name.equals(value)) {
          return name;
        }
      }
      return Unknown;
    }
  }

  public enum UnicodeLanguageId {
    // Unicode Language IDs (platform ID = 0)
    Unknown(-1),
    All(0);

    private final int value;

    private UnicodeLanguageId(int value) {
      this.value = value;
    }

    public int value() {
      return value;
    }

    public boolean equals(int value) {
      return value == this.value;
    }

    public static UnicodeLanguageId valueOf(int value) {
      for (UnicodeLanguageId language : UnicodeLanguageId.values()) {
        if (language.equals(value)) {
          return language;
        }
      }
      return Unknown;
    }
  }

  /** Macinstosh Language IDs (platform ID = 1) */
  public enum MacintoshLanguageId {
    Unknown(-1),
    English(0),
    French(1),
    German(2),
    Italian(3),
    Dutch(4),
    Swedish(5),
    Spanish(6),
    Danish(7),
    Portuguese(8),
    Norwegian(9),
    Hebrew(10),
    Japanese(11),
    Arabic(12),
    Finnish(13),
    Greek(14),
    Icelandic(15),
    Maltese(16),
    Turkish(17),
    Croatian(18),
    Chinese_Traditional(19),
    Urdu(20),
    Hindi(21),
    Thai(22),
    Korean(23),
    Lithuanian(24),
    Polish(25),
    Hungarian(26),
    Estonian(27),
    Latvian(28),
    Sami(29),
    Faroese(30),
    FarsiPersian(31),
    Russian(32),
    Chinese_Simplified(33),
    Flemish(34),
    IrishGaelic(35),
    Albanian(36),
    Romanian(37),
    Czech(38),
    Slovak(39),
    Slovenian(40),
    Yiddish(41),
    Serbian(42),
    Macedonian(43),
    Bulgarian(44),
    Ukrainian(45),
    Byelorussian(46),
    Uzbek(47),
    Kazakh(48),
    Azerbaijani_Cyrillic(49),
    Azerbaijani_Arabic(50),
    Armenian(51),
    Georgian(52),
    Moldavian(53),
    Kirghiz(54),
    Tajiki(55),
    Turkmen(56),
    Mongolian_Mongolian(57),
    Mongolian_Cyrillic(58),
    Pashto(59),
    Kurdish(60),
    Kashmiri(61),
    Sindhi(62),
    Tibetan(63),
    Nepali(64),
    Sanskrit(65),
    Marathi(66),
    Bengali(67),
    Assamese(68),
    Gujarati(69),
    Punjabi(70),
    Oriya(71),
    Malayalam(72),
    Kannada(73),
    Tamil(74),
    Telugu(75),
    Sinhalese(76),
    Burmese(77),
    Khmer(78),
    Lao(79),
    Vietnamese(80),
    Indonesian(81),
    Tagalong(82),
    Malay_Roman(83),
    Malay_Arabic(84),
    Amharic(85),
    Tigrinya(86),
    Galla(87),
    Somali(88),
    Swahili(89),
    KinyarwandaRuanda(90),
    Rundi(91),
    NyanjaChewa(92),
    Malagasy(93),
    Esperanto(94),
    Welsh(128),
    Basque(129),
    Catalan(130),
    Latin(131),
    Quenchua(132),
    Guarani(133),
    Aymara(134),
    Tatar(135),
    Uighur(136),
    Dzongkha(137),
    Javanese_Roman(138),
    Sundanese_Roman(139),
    Galician(140),
    Afrikaans(141),
    Breton(142),
    Inuktitut(143),
    ScottishGaelic(144),
    ManxGaelic(145),
    IrishGaelic_WithDotAbove(146),
    Tongan(147),
    Greek_Polytonic(148),
    Greenlandic(149),
    Azerbaijani_Roman(150);

    private final int value;

    private MacintoshLanguageId(int value) {
      this.value = value;
    }

    public int value() {
      return value;
    }

    public boolean equals(int value) {
      return value == this.value;
    }

    public static MacintoshLanguageId valueOf(int value) {
      for (MacintoshLanguageId language : MacintoshLanguageId.values()) {
        if (language.equals(value)) {
          return language;
        }
      }
      return Unknown;
    }
  }

  /** Windows Language IDs (platform ID = 3) */
  public enum WindowsLanguageId {
    Unknown(-1),
    Afrikaans_SouthAfrica(0x0436),
    Albanian_Albania(0x041C),
    Alsatian_France(0x0484),
    Amharic_Ethiopia(0x045E),
    Arabic_Algeria(0x1401),
    Arabic_Bahrain(0x3C01),
    Arabic_Egypt(0x0C01),
    Arabic_Iraq(0x0801),
    Arabic_Jordan(0x2C01),
    Arabic_Kuwait(0x3401),
    Arabic_Lebanon(0x3001),
    Arabic_Libya(0x1001),
    Arabic_Morocco(0x1801),
    Arabic_Oman(0x2001),
    Arabic_Qatar(0x4001),
    Arabic_SaudiArabia(0x0401),
    Arabic_Syria(0x2801),
    Arabic_Tunisia(0x1C01),
    Arabic_UAE(0x3801),
    Arabic_Yemen(0x2401),
    Armenian_Armenia(0x042B),
    Assamese_India(0x044D),
    Azeri_Cyrillic_Azerbaijan(0x082C),
    Azeri_Latin_Azerbaijan(0x042C),
    Bashkir_Russia(0x046D),
    Basque_Basque(0x042D),
    Belarusian_Belarus(0x0423),
    Bengali_Bangladesh(0x0845),
    Bengali_India(0x0445),
    Bosnian_Cyrillic_BosniaAndHerzegovina(0x201A),
    Bosnian_Latin_BosniaAndHerzegovina(0x141A),
    Breton_France(0x047E),
    Bulgarian_Bulgaria(0x0402),
    Catalan_Catalan(0x0403),
    Chinese_HongKongSAR(0x0C04),
    Chinese_MacaoSAR(0x1404),
    Chinese_PeoplesRepublicOfChina(0x0804),
    Chinese_Singapore(0x1004),
    Chinese_Taiwan(0x0404),
    Corsican_France(0x0483),
    Croatian_Croatia(0x041A),
    Croatian_Latin_BosniaAndHerzegovina(0x101A),
    Czech_CzechRepublic(0x0405),
    Danish_Denmark(0x0406),
    Dari_Afghanistan(0x048C),
    Divehi_Maldives(0x0465),
    Dutch_Belgium(0x0813),
    Dutch_Netherlands(0x0413),
    English_Australia(0x0C09),
    English_Belize(0x2809),
    English_Canada(0x1009),
    English_Caribbean(0x2409),
    English_India(0x4009),
    English_Ireland(0x1809),
    English_Jamaica(0x2009),
    English_Malaysia(0x4409),
    English_NewZealand(0x1409),
    English_RepublicOfThePhilippines(0x3409),
    English_Singapore(0x4809),
    English_SouthAfrica(0x1C09),
    English_TrinidadAndTobago(0x2C09),
    English_UnitedKingdom(0x0809),
    English_UnitedStates(0x0409),
    English_Zimbabwe(0x3009),
    Estonian_Estonia(0x0425),
    Faroese_FaroeIslands(0x0438),
    Filipino_Philippines(0x0464),
    Finnish_Finland(0x040B),
    French_Belgium(0x080C),
    French_Canada(0x0C0C),
    French_France(0x040C),
    French_Luxembourg(0x140c),
    French_PrincipalityOfMonoco(0x180C),
    French_Switzerland(0x100C),
    Frisian_Netherlands(0x0462),
    Galician_Galician(0x0456),
    Georgian_Georgia(0x0437),
    German_Austria(0x0C07),
    German_Germany(0x0407),
    German_Liechtenstein(0x1407),
    German_Luxembourg(0x1007),
    German_Switzerland(0x0807),
    Greek_Greece(0x0408),
    Greenlandic_Greenland(0x046F),
    Gujarati_India(0x0447),
    Hausa_Latin_Nigeria(0x0468),
    Hebrew_Israel(0x040D),
    Hindi_India(0x0439),
    Hungarian_Hungary(0x040E),
    Icelandic_Iceland(0x040F),
    Igbo_Nigeria(0x0470),
    Indonesian_Indonesia(0x0421),
    Inuktitut_Canada(0x045D),
    Inuktitut_Latin_Canada(0x085D),
    Irish_Ireland(0x083C),
    isiXhosa_SouthAfrica(0x0434),
    isiZulu_SouthAfrica(0x0435),
    Italian_Italy(0x0410),
    Italian_Switzerland(0x0810),
    Japanese_Japan(0x0411),
    Kannada_India(0x044B),
    Kazakh_Kazakhstan(0x043F),
    Khmer_Cambodia(0x0453),
    Kiche_Guatemala(0x0486),
    Kinyarwanda_Rwanda(0x0487),
    Kiswahili_Kenya(0x0441),
    Konkani_India(0x0457),
    Korean_Korea(0x0412),
    Kyrgyz_Kyrgyzstan(0x0440),
    Lao_LaoPDR(0x0454),
    Latvian_Latvia(0x0426),
    Lithuanian_Lithuania(0x0427),
    LowerSorbian_Germany(0x082E),
    Luxembourgish_Luxembourg(0x046E),
    Macedonian_FYROM_FormerYugoslavRepublicOfMacedonia(0x042F),
    Malay_BruneiDarussalam(0x083E),
    Malay_Malaysia(0x043E),
    Malayalam_India(0x044C),
    Maltese_Malta(0x043A),
    Maori_NewZealand(0x0481),
    Mapudungun_Chile(0x047A),
    Marathi_India(0x044E),
    Mohawk_Mohawk(0x047C),
    Mongolian_Cyrillic_Mongolia(0x0450),
    Mongolian_Traditional_PeoplesRepublicOfChina(0x0850),
    Nepali_Nepal(0x0461),
    Norwegian_Bokmal_Norway(0x0414),
    Norwegian_Nynorsk_Norway(0x0814),
    Occitan_France(0x0482),
    Oriya_India(0x0448),
    Pashto_Afghanistan(0x0463),
    Polish_Poland(0x0415),
    Portuguese_Brazil(0x0416),
    Portuguese_Portugal(0x0816),
    Punjabi_India(0x0446),
    Quechua_Bolivia(0x046B),
    Quechua_Ecuador(0x086B),
    Quechua_Peru(0x0C6B),
    Romanian_Romania(0x0418),
    Romansh_Switzerland(0x0417),
    Russian_Russia(0x0419),
    Sami_Inari_Finland(0x243B),
    Sami_Lule_Norway(0x103B),
    Sami_Lule_Sweden(0x143B),
    Sami_Northern_Finland(0x0C3B),
    Sami_Northern_Norway(0x043B),
    Sami_Northern_Sweden(0x083B),
    Sami_Skolt_Finland(0x203B),
    Sami_Southern_Norway(0x183B),
    Sami_Southern_Sweden(0x1C3B),
    Sanskrit_India(0x044F),
    Serbian_Cyrillic_BosniaAndHerzegovina(0x1C1A),
    Serbian_Cyrillic_Serbia(0x0C1A),
    Serbian_Latin_BosniaAndHerzegovina(0x181A),
    Serbian_Latin_Serbia(0x081A),
    SesothoSaLeboa_SouthAfrica(0x046C),
    Setswana_SouthAfrica(0x0432),
    Sinhala_SriLanka(0x045B),
    Slovak_Slovakia(0x041B),
    Slovenian_Slovenia(0x0424),
    Spanish_Argentina(0x2C0A),
    Spanish_Bolivia(0x400A),
    Spanish_Chile(0x340A),
    Spanish_Colombia(0x240A),
    Spanish_CostaRica(0x140A),
    Spanish_DominicanRepublic(0x1C0A),
    Spanish_Ecuador(0x300A),
    Spanish_ElSalvador(0x440A),
    Spanish_Guatemala(0x100A),
    Spanish_Honduras(0x480A),
    Spanish_Mexico(0x080A),
    Spanish_Nicaragua(0x4C0A),
    Spanish_Panama(0x180A),
    Spanish_Paraguay(0x3C0A),
    Spanish_Peru(0x280A),
    Spanish_PuertoRico(0x500A),
    Spanish_ModernSort_Spain(0x0C0A),
    Spanish_TraditionalSort_Spain(0x040A),
    Spanish_UnitedStates(0x540A),
    Spanish_Uruguay(0x380A),
    Spanish_Venezuela(0x200A),
    Sweden_Finland(0x081D),
    Swedish_Sweden(0x041D),
    Syriac_Syria(0x045A),
    Tajik_Cyrillic_Tajikistan(0x0428),
    Tamazight_Latin_Algeria(0x085F),
    Tamil_India(0x0449),
    Tatar_Russia(0x0444),
    Telugu_India(0x044A),
    Thai_Thailand(0x041E),
    Tibetan_PRC(0x0451),
    Turkish_Turkey(0x041F),
    Turkmen_Turkmenistan(0x0442),
    Uighur_PRC(0x0480),
    Ukrainian_Ukraine(0x0422),
    UpperSorbian_Germany(0x042E),
    Urdu_IslamicRepublicOfPakistan(0x0420),
    Uzbek_Cyrillic_Uzbekistan(0x0843),
    Uzbek_Latin_Uzbekistan(0x0443),
    Vietnamese_Vietnam(0x042A),
    Welsh_UnitedKingdom(0x0452),
    Wolof_Senegal(0x0448),
    Yakut_Russia(0x0485),
    Yi_PRC(0x0478),
    Yoruba_Nigeria(0x046A);

    private final int value;

    private WindowsLanguageId(int value) {
      this.value = value;
    }

    public int value() {
      return value;
    }

    public boolean equals(int value) {
      return value == this.value;
    }

    public static WindowsLanguageId valueOf(int value) {
      for (WindowsLanguageId language : WindowsLanguageId.values()) {
        if (language.equals(value)) {
          return language;
        }
      }
      return Unknown;
    }
  }

  private NameTable(Header header, ReadableFontData data) {
    super(header, data);
  }

  public int format() {
    return data.readUShort(HeaderOffsets.format);
  }

  /**
   * Get the number of names in the name table.
   *
   * @return the number of names
   */
  public int nameCount() {
    return data.readUShort(HeaderOffsets.count);
  }

  /**
   * Get the offset to the string data in the name table.
   *
   * @return the string offset
   */
  private int stringOffset() {
    return data.readUShort(HeaderOffsets.stringOffset);
  }

  /**
   * Get the offset for the given name record.
   *
   * @param index the index of the name record
   * @return the offset of the name record
   */
  private int offsetForNameRecord(int index) {
    return HeaderOffsets.nameRecordStart + index * NameRecord.SIZE;
  }

  /**
   * Get the platform id for the given name record.
   *
   * @param index the index of the name record
   * @return the platform id
   * @see Font.PlatformId
   */
  public int platformId(int index) {
    return data.readUShort(NameRecord.platformId + offsetForNameRecord(index));
  }

  /**
   * Get the encoding id for the given name record.
   *
   * @param index the index of the name record
   * @return the encoding id
   * @see Font.MacintoshEncodingId
   * @see Font.WindowsEncodingId
   * @see Font.UnicodeEncodingId
   */
  public int encodingId(int index) {
    return data.readUShort(NameRecord.encodingId + offsetForNameRecord(index));
  }

  /**
   * Get the language id for the given name record.
   *
   * @param index the index of the name record
   * @return the language id
   */
  public int languageId(int index) {
    return data.readUShort(NameRecord.languageId + offsetForNameRecord(index));
  }

  /**
   * Get the name id for given name record.
   *
   * @param index the index of the name record
   * @return the name id
   */
  public int nameId(int index) {
    return data.readUShort(NameRecord.nameId + offsetForNameRecord(index));
  }

  /**
   * Get the length of the string data for the given name record.
   *
   * @param index the index of the name record
   * @return the length of the string data in bytes
   */
  private int nameLength(int index) {
    return data.readUShort(NameRecord.stringLength + offsetForNameRecord(index));
  }

  /**
   * Get the offset of the string data for the given name record.
   *
   * @param index the index of the name record
   * @return the offset of the string data from the start of the table
   */
  private int nameOffset(int index) {
    return data.readUShort(NameRecord.stringOffset + offsetForNameRecord(index)) + stringOffset();
  }

  /**
   * Get the name as bytes for the given name record.
   *
   * @param index the index of the name record
   * @return the bytes for the name
   */
  public byte[] nameAsBytes(int index) {
    int length = nameLength(index);
    byte[] b = new byte[length];
    data.readBytes(nameOffset(index), b, 0, length);
    return b;
  }

  /**
   * Get the name as bytes for the specified name. If there is no entry for the requested name then
   * {@code null} is returned.
   */
  public byte[] nameAsBytes(int platformId, int encodingId, int languageId, int nameId) {
    NameEntry entry = nameEntry(platformId, encodingId, languageId, nameId);
    if (entry != null) {
      return entry.nameAsBytes();
    }
    return null;
  }

  /**
   * Get the name as a String for the given name record. If there is no encoding conversion
   * available for the name record then a best attempt String will be returned.
   *
   * @param index the index of the name record
   */
  public String name(int index) {
    return convertFromNameBytes(nameAsBytes(index), platformId(index), encodingId(index));
  }

  /**
   * Get the name as a String for the specified name. If there is no entry for the requested name
   * then {@code null} is returned. If there is no encoding conversion available for the name then a
   * best attempt String will be returned.
   */
  public String name(int platformId, int encodingId, int languageId, int nameId) {
    NameEntry entry = nameEntry(platformId, encodingId, languageId, nameId);
    if (entry != null) {
      return entry.name();
    }
    return null;
  }

  /**
   * Get the name entry record for the given name entry.
   *
   * @param index the index of the name record
   * @return the name entry
   */
  public NameEntry nameEntry(int index) {
    return new NameEntry(
        platformId(index), encodingId(index), languageId(index), nameId(index), nameAsBytes(index));
  }

  /**
   * Get the name entry record for the specified name. If there is no entry for the requested name
   * then {@code null} is returned.
   */
  public NameEntry nameEntry(int platformId, int encodingId, int languageId, int nameId) {
    Iterator<NameEntry> nameEntryIter =
        iterator(
            (pid, eid, lid, nid) ->
                pid == platformId && eid == encodingId && lid == languageId && nid == nameId);
    // can only be one name for each set of ids
    if (nameEntryIter.hasNext()) {
      return nameEntryIter.next();
    }
    return null;
  }

  /**
   * Get all the name entry records.
   *
   * @return the set of all name entry records
   */
  public Set<NameEntry> names() {
    Set<NameEntry> nameSet = new HashSet<>(nameCount());
    for (NameEntry entry : this) {
      nameSet.add(entry);
    }
    return nameSet;
  }

  private static class NameEntryId implements Comparable<NameEntryId> {
    /** @see Font.PlatformId */
    protected int platformId;
    /**
     * @see Font.UnicodeEncodingId
     * @see Font.MacintoshEncodingId
     * @see Font.WindowsEncodingId
     */
    protected int encodingId;
    /**
     * @see NameTable.UnicodeLanguageId
     * @see NameTable.MacintoshLanguageId
     * @see NameTable.WindowsLanguageId
     */
    protected int languageId;
    /** @see NameTable.NameId */
    protected int nameId;

    protected NameEntryId(int platformId, int encodingId, int languageId, int nameId) {
      this.platformId = platformId;
      this.encodingId = encodingId;
      this.languageId = languageId;
      this.nameId = nameId;
    }

    /**
     * Get the platform id.
     *
     * @return the platform id
     */
    protected int getPlatformId() {
      return platformId;
    }

    /**
     * Get the encoding id.
     *
     * @return the encoding id
     */
    protected int getEncodingId() {
      return encodingId;
    }

    /**
     * Get the language id.
     *
     * @return the language id
     */
    protected int getLanguageId() {
      return languageId;
    }

    /**
     * Get the name id.
     *
     * @return the name id
     */
    protected int getNameId() {
      return nameId;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof NameEntryId)) {
        return false;
      }
      NameEntryId other = (NameEntryId) obj;
      return encodingId == other.encodingId
          && languageId == other.languageId
          && platformId == other.platformId
          && nameId == other.nameId;
    }

    @Override
    public int hashCode() {
      /*
       * - this takes advantage of the sizes of the various entries and the fact
       * that the ranges of their values have an almost zero probability of ever
       * changing - this allows us to generate a unique hash at low cost - if
       * the ranges do change then we will potentially generate non-unique hash
       * values which is a common result
       */
      return ((encodingId & 0x3f) << 26)
          | ((nameId & 0x3f) << 16)
          | ((platformId & 0x0f) << 12)
          | (languageId & 0xff);
    }

    /**
     * Name entries are sorted by platform id, encoding id, language id, and name id in order of
     * decreasing importance.
     *
     * @return less than zero if this entry is less than the other; greater than zero if this entry
     *     is greater than the other; and zero if they are equal
     */
    @Override
    public int compareTo(NameEntryId o) {
      int rv = Integer.compare(o.platformId, platformId);
      if (rv == 0) {
        rv = Integer.compare(o.encodingId, encodingId);
      }
      if (rv == 0) {
        rv = Integer.compare(o.languageId, languageId);
      }
      if (rv == 0) {
        rv = Integer.compare(o.nameId, nameId);
      }
      return rv;
    }

    @Override
    public String toString() {
      NameId nameId = NameId.valueOf(this.nameId);
      String nameIdStr = nameId != null ? nameId.toString() : Integer.toHexString(this.nameId);
      return String.format(
          "P=%s, E=%#x, L=%#x, N=%s",
          Font.PlatformId.valueOf(platformId), encodingId, languageId, nameIdStr);
    }
  }

  /** Class to represent a name entry in the name table. */
  public static class NameEntry {
    NameEntryId nameEntryId;
    protected int length;
    protected byte[] nameBytes;

    protected NameEntry() {}

    protected NameEntry(NameEntryId nameEntryId, byte[] nameBytes) {
      this.nameEntryId = nameEntryId;
      this.nameBytes = nameBytes;
    }

    protected NameEntry(
        int platformId, int encodingId, int languageId, int nameId, byte[] nameBytes) {
      this(new NameEntryId(platformId, encodingId, languageId, nameId), nameBytes);
    }

    protected NameEntryId getNameEntryId() {
      return nameEntryId;
    }

    /**
     * Get the platform id.
     *
     * @return the platform id
     */
    public int platformId() {
      return nameEntryId.getPlatformId();
    }

    /**
     * Get the encoding id.
     *
     * @return the encoding id
     */
    public int encodingId() {
      return nameEntryId.getEncodingId();
    }

    /**
     * Get the language id.
     *
     * @return the language id
     */
    public int languageId() {
      return nameEntryId.getLanguageId();
    }

    /**
     * Get the name id.
     *
     * @return the name id
     */
    public int nameId() {
      return nameEntryId.getNameId();
    }

    /**
     * Get the bytes for name.
     *
     * @return the name bytes
     */
    public byte[] nameAsBytes() {
      return nameBytes;
    }

    /**
     * Get the name as a String. If there is no encoding conversion available for the name bytes
     * then a best attempt String will be returned.
     */
    public String name() {
      return convertFromNameBytes(nameBytes, platformId(), encodingId());
    }

    @Override
    public String toString() {
      return String.format("[%s, \"%s\"]", nameEntryId, name());
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof NameEntry)) {
        return false;
      }
      NameEntry other = (NameEntry) obj;
      return SfObjects.equals(nameEntryId, other.nameEntryId)
          && Arrays.equals(nameBytes, other.nameBytes);
    }

    @Override
    public int hashCode() {
      return SfObjects.hash(nameEntryId, Arrays.hashCode(nameBytes));
    }
  }

  public static class NameEntryBuilder extends NameEntry {

    protected NameEntryBuilder() {
      super();
    }

    protected NameEntryBuilder(NameEntryId nameEntryId, byte[] nameBytes) {
      super(nameEntryId, nameBytes);
    }

    protected NameEntryBuilder(NameEntryId nameEntryId) {
      this(nameEntryId, null);
    }

    protected NameEntryBuilder(NameEntry nameEntry) {
      this(nameEntry.getNameEntryId(), nameEntry.nameAsBytes());
    }

    public void setName(String name) {
      if (name == null) {
        this.nameBytes = new byte[0];
        return;
      }
      this.nameBytes =
          convertToNameBytes(name, nameEntryId.getPlatformId(), nameEntryId.getEncodingId());
    }

    public void setName(byte[] nameBytes) {
      this.nameBytes = Arrays.copyOf(nameBytes, nameBytes.length);
    }

    public void setName(byte[] nameBytes, int offset, int length) {
      this.nameBytes = Arrays.copyOfRange(nameBytes, offset, offset + length);
    }
  }

  /**
   * An interface for a filter to use with the name entry iterator. This allows name entries to be
   * iterated and only those acceptable to the filter will be returned.
   */
  public interface NameEntryFilter {
    /**
     * Callback to determine if a name entry is acceptable.
     *
     * @return true if the name entry is acceptable; false otherwise
     */
    boolean accept(int platformId, int encodingId, int languageId, int nameId);
  }

  protected class NameEntryIterator implements Iterator<NameEntry> {
    private int nameIndex = 0;
    private NameEntryFilter filter = null;

    private NameEntryIterator() {
      // no filter - iterate all name entries
    }

    private NameEntryIterator(NameEntryFilter filter) {
      this.filter = filter;
    }

    @Override
    public boolean hasNext() {
      if (filter == null) {
        return nameIndex < nameCount();
      }
      for (; nameIndex < nameCount(); this.nameIndex++) {
        if (filter.accept(
            platformId(nameIndex), encodingId(nameIndex),
            languageId(nameIndex), nameId(nameIndex))) {
          return true;
        }
      }
      return false;
    }

    @Override
    public NameEntry next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return nameEntry(this.nameIndex++);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Cannot remove a CMap table from an existing font.");
    }
  }

  @Override
  public Iterator<NameEntry> iterator() {
    return new NameEntryIterator();
  }

  /**
   * Get an iterator over name entries in the name table.
   *
   * @param filter a filter to select acceptable name entries
   * @return an iterator over name entries
   */
  public Iterator<NameEntry> iterator(NameEntryFilter filter) {
    return new NameEntryIterator(filter);
  }

  // TODO(stuartg): do this in the encoding enums
  private static String getEncodingName(int platformId, int encodingId) {
    String encodingName = null;
    switch (Font.PlatformId.valueOf(platformId)) {
      case Unicode:
        encodingName = "UTF-16BE";
        break;
      case Macintosh:
        switch (Font.MacintoshEncodingId.valueOf(encodingId)) {
          case Roman:
            encodingName = "MacRoman";
            break;
          case Japanese:
            encodingName = "Shift_JIS";
            break;
          case ChineseTraditional:
            encodingName = "Big5";
            break;
          case Korean:
            encodingName = "EUC-KR";
            break;
          case Arabic:
            encodingName = "MacArabic";
            break;
          case Hebrew:
            encodingName = "MacHebrew";
            break;
          case Greek:
            encodingName = "MacGreek";
            break;
          case Russian:
            encodingName = "MacCyrillic";
            break;
          case RSymbol:
            encodingName = "MacSymbol";
            break;
          case Devanagari:
            break;
          case Gurmukhi:
            break;
          case Gujarati:
            break;
          case Oriya:
            break;
          case Bengali:
            break;
          case Tamil:
            break;
          case Telugu:
            break;
          case Kannada:
            break;
          case Malayalam:
            break;
          case Sinhalese:
            break;
          case Burmese:
            break;
          case Khmer:
            break;
          case Thai:
            encodingName = "MacThai";
            break;
          case Laotian:
            break;
          case Georgian:
            // TODO: ??? is it?
            encodingName = "MacCyrillic";
            break;
          case Armenian:
            break;
          case ChineseSimplified:
            encodingName = "EUC-CN";
            break;
          case Tibetan:
            break;
          case Mongolian:
            // TODO: ??? is it?
            encodingName = "MacCyrillic";
            break;
          case Geez:
            break;
          case Slavic:
            // TODO: ??? is it?
            encodingName = "MacCyrillic";
            break;
          case Vietnamese:
            break;
          case Sindhi:
            break;
          case Uninterpreted:
            break;
        }
        break;
      case ISO:
        break;
      case Windows:
        switch (Font.WindowsEncodingId.valueOf(encodingId)) {
          case Symbol:
            encodingName = "UTF-16BE";
            break;
          case UnicodeUCS2:
            encodingName = "UTF-16BE";
            break;
          case ShiftJIS:
            encodingName = "windows-932";
            break;
          case PRC:
            encodingName = "windows-936";
            break;
          case Big5:
            encodingName = "windows-950";
            break;
          case Wansung:
            encodingName = "windows-949";
            break;
          case Johab:
            encodingName = "ms1361";
            break;
          case UnicodeUCS4:
            encodingName = "UCS-4";
            break;
        }
        break;
      case Custom:
        break;
      default:
        break;
    }
    return encodingName;
  }

  // TODO: caching of charsets?
  private static Charset getCharset(int platformId, int encodingId) {
    String encodingName = getEncodingName(platformId, encodingId);
    if (encodingName == null) {
      return null;
    }
    Charset charset = null;
    try {
      charset = Charset.forName(encodingName);
    } catch (UnsupportedCharsetException e) {
      return null;
    }
    return charset;
  }

  // TODO(stuartg):
  // do the conversion by hand to detect conversion failures (i.e. no character in the encoding)
  private static byte[] convertToNameBytes(String name, int platformId, int encodingId) {
    Charset cs = getCharset(platformId, encodingId);
    if (cs == null) {
      return null;
    }
    ByteBuffer bb = cs.encode(name);
    return bb.array();
  }

  private static String convertFromNameBytes(byte[] nameBytes, int platformId, int encodingId) {
    return convertFromNameBytes(ByteBuffer.wrap(nameBytes), platformId, encodingId);
  }

  private static String convertFromNameBytes(ByteBuffer nameBytes, int platformId, int encodingId) {
    Charset cs = getCharset(platformId, encodingId);
    if (cs == null) {
      return Integer.toHexString(platformId);
    }
    CharBuffer cb = cs.decode(nameBytes);
    return cb.toString();
  }

  public static class Builder extends SubTableContainerTable.Builder<NameTable> {

    private Map<NameEntryId, NameEntryBuilder> nameEntryMap;

    public static Builder createBuilder(Header header, WritableFontData data) {
      return new Builder(header, data);
    }

    protected Builder(Header header, WritableFontData data) {
      super(header, data);
    }

    protected Builder(Header header, ReadableFontData data) {
      super(header, data);
    }

    private void initialize(ReadableFontData data) {
      this.nameEntryMap = new TreeMap<>();

      if (data != null) {
        NameTable table = new NameTable(header(), data);

        for (NameEntry nameEntry : table) {
          NameEntryBuilder nameEntryBuilder = new NameEntryBuilder(nameEntry);
          nameEntryMap.put(nameEntryBuilder.getNameEntryId(), nameEntryBuilder);
        }
      }
    }

    private Map<NameEntryId, NameEntryBuilder> getNameBuilders() {
      if (nameEntryMap == null) {
        initialize(super.internalReadData());
      }
      super.setModelChanged();
      return nameEntryMap;
    }

    /** Revert the name builders for the name table to the last version that came from data. */
    public void revertNames() {
      this.nameEntryMap = null;
      setModelChanged(false);
    }

    public int builderCount() {
      return getNameBuilders().size();
    }

    /** Clear the name builders for the name table. */
    public void clear() {
      getNameBuilders().clear();
    }

    public boolean has(int platformId, int encodingId, int languageId, int nameId) {
      NameEntryId probe = new NameEntryId(platformId, encodingId, languageId, nameId);
      return getNameBuilders().containsKey(probe);
    }

    public NameEntryBuilder nameBuilder(
        int platformId, int encodingId, int languageId, int nameId) {
      NameEntryId probe = new NameEntryId(platformId, encodingId, languageId, nameId);
      NameEntryBuilder builder = getNameBuilders().get(probe);
      if (builder == null) {
        builder = new NameEntryBuilder(probe);
        getNameBuilders().put(probe, builder);
      }
      return builder;
    }

    public boolean remove(int platformId, int encodingId, int languageId, int nameId) {
      NameEntryId probe = new NameEntryId(platformId, encodingId, languageId, nameId);
      return (getNameBuilders().remove(probe) != null);
    }

    // subclass API implementation

    @Override
    protected NameTable subBuildTable(ReadableFontData data) {
      return new NameTable(header(), data);
    }

    @Override
    protected void subDataSet() {
      this.nameEntryMap = null;
      super.setModelChanged(false);
    }

    @Override
    protected int subDataSizeToSerialize() {
      if (nameEntryMap == null || nameEntryMap.size() == 0) {
        return 0;
      }

      int size = HeaderOffsets.nameRecordStart + nameEntryMap.size() * NameRecord.SIZE;
      for (Map.Entry<NameEntryId, NameEntryBuilder> entry : nameEntryMap.entrySet()) {
        size += entry.getValue().nameAsBytes().length;
      }
      return size;
    }

    @Override
    protected boolean subReadyToSerialize() {
      return nameEntryMap != null && nameEntryMap.size() != 0;
    }

    @Override
    protected int subSerialize(WritableFontData newData) {
      int stringTableStartOffset =
          HeaderOffsets.nameRecordStart + nameEntryMap.size() * NameRecord.SIZE;

      // header
      newData.writeUShort(HeaderOffsets.format, 0);
      newData.writeUShort(HeaderOffsets.count, nameEntryMap.size());
      newData.writeUShort(HeaderOffsets.stringOffset, stringTableStartOffset);
      int offset = HeaderOffsets.nameRecordStart;
      int stringOffset = 0;
      for (Map.Entry<NameEntryId, NameEntryBuilder> entry : nameEntryMap.entrySet()) {
        NameEntryId id = entry.getKey();
        NameEntryBuilder builder = entry.getValue();

        // lookup table
        newData.writeUShort(offset + NameRecord.platformId, id.getPlatformId());
        newData.writeUShort(offset + NameRecord.encodingId, id.getEncodingId());
        newData.writeUShort(offset + NameRecord.languageId, id.getLanguageId());
        newData.writeUShort(offset + NameRecord.nameId, id.getNameId());
        newData.writeUShort(offset + NameRecord.stringLength, builder.nameAsBytes().length);
        newData.writeUShort(offset + NameRecord.stringOffset, stringOffset);
        offset += NameRecord.SIZE;
        // string table
        byte[] nameBytes = builder.nameAsBytes();
        if (nameBytes.length > 0) {
          stringOffset +=
              newData.writeBytes(stringOffset + stringTableStartOffset, builder.nameAsBytes());
        }
      }
      return stringOffset + stringTableStartOffset;
    }
  }
}
