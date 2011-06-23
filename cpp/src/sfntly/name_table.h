/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// TODO(arthurhsu): IMPLEMENT: not really used and tested, need cleanup
#ifndef TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_NAME_TABLE_H_
#define TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_NAME_TABLE_H_

#include <map>
#include <utility>

#include "sfntly/table.h"

namespace sfntly {

struct UnicodeLanguageId {
  static const int32_t kUnknown;
  static const int32_t kAll;
};

// Macinstosh Language IDs (platform ID = 1)
struct MacintoshLanguageId {
  static const int32_t kUnknown;
  static const int32_t kEnglish;
  static const int32_t kFrench;
  static const int32_t kGerman;
  static const int32_t kItalian;
  static const int32_t kDutch;
  static const int32_t kSwedish;
  static const int32_t kSpanish;
  static const int32_t kDanish;
  static const int32_t kPortuguese;
  static const int32_t kNorwegian;
  static const int32_t kHebrew;
  static const int32_t kJapanese;
  static const int32_t kArabic;
  static const int32_t kFinnish;
  static const int32_t kGreek;
  static const int32_t kIcelandic;
  static const int32_t kMaltese;
  static const int32_t kTurkish;
  static const int32_t kCroatian;
  static const int32_t kChinese_Traditional;
  static const int32_t kUrdu;
  static const int32_t kHindi;
  static const int32_t kThai;
  static const int32_t kKorean;
  static const int32_t kLithuanian;
  static const int32_t kPolish;
  static const int32_t kHungarian;
  static const int32_t kEstonian;
  static const int32_t kLatvian;
  static const int32_t kSami;
  static const int32_t kFaroese;
  static const int32_t kFarsiPersian;
  static const int32_t kRussian;
  static const int32_t kChinese_Simplified;
  static const int32_t kFlemish;
  static const int32_t kIrishGaelic;
  static const int32_t kAlbanian;
  static const int32_t kRomanian;
  static const int32_t kCzech;
  static const int32_t kSlovak;
  static const int32_t kSlovenian;
  static const int32_t kYiddish;
  static const int32_t kSerbian;
  static const int32_t kMacedonian;
  static const int32_t kBulgarian;
  static const int32_t kUkrainian;
  static const int32_t kByelorussian;
  static const int32_t kUzbek;
  static const int32_t kKazakh;
  static const int32_t kAzerbaijani_Cyrillic;
  static const int32_t kAzerbaijani_Arabic;
  static const int32_t kArmenian;
  static const int32_t kGeorgian;
  static const int32_t kMoldavian;
  static const int32_t kKirghiz;
  static const int32_t kTajiki;
  static const int32_t kTurkmen;
  static const int32_t kMongolian_Mongolian;
  static const int32_t kMongolian_Cyrillic;
  static const int32_t kPashto;
  static const int32_t kKurdish;
  static const int32_t kKashmiri;
  static const int32_t kSindhi;
  static const int32_t kTibetan;
  static const int32_t kNepali;
  static const int32_t kSanskrit;
  static const int32_t kMarathi;
  static const int32_t kBengali;
  static const int32_t kAssamese;
  static const int32_t kGujarati;
  static const int32_t kPunjabi;
  static const int32_t kOriya;
  static const int32_t kMalayalam;
  static const int32_t kKannada;
  static const int32_t kTamil;
  static const int32_t kTelugu;
  static const int32_t kSinhalese;
  static const int32_t kBurmese;
  static const int32_t kKhmer;
  static const int32_t kLao;
  static const int32_t kVietnamese;
  static const int32_t kIndonesian;
  static const int32_t kTagalong;
  static const int32_t kMalay_Roman;
  static const int32_t kMalay_Arabic;
  static const int32_t kAmharic;
  static const int32_t kTigrinya;
  static const int32_t kGalla;
  static const int32_t kSomali;
  static const int32_t kSwahili;
  static const int32_t kKinyarwandaRuanda;
  static const int32_t kRundi;
  static const int32_t kNyanjaChewa;
  static const int32_t kMalagasy;
  static const int32_t kEsperanto;
  static const int32_t kWelsh;
  static const int32_t kBasque;
  static const int32_t kCatalan;
  static const int32_t kLatin;
  static const int32_t kQuenchua;
  static const int32_t kGuarani;
  static const int32_t kAymara;
  static const int32_t kTatar;
  static const int32_t kUighur;
  static const int32_t kDzongkha;
  static const int32_t kJavanese_Roman;
  static const int32_t kSundanese_Roman;
  static const int32_t kGalician;
  static const int32_t kAfrikaans;
  static const int32_t kBreton;
  static const int32_t kInuktitut;
  static const int32_t kScottishGaelic;
  static const int32_t kManxGaelic;
  static const int32_t kIrishGaelic_WithDotAbove;
  static const int32_t kTongan;
  static const int32_t kGreek_Polytonic;
  static const int32_t kGreenlandic;
  static const int32_t kAzerbaijani_Roman;
};

// Windows Language IDs (platformID = 3)
struct WindowsLanguageId {
  static const int32_t kUnknown;
  static const int32_t kAfrikaans_SouthAfrica;
  static const int32_t kAlbanian_Albania;
  static const int32_t kAlsatian_France;
  static const int32_t kAmharic_Ethiopia;
  static const int32_t kArabic_Algeria;
  static const int32_t kArabic_Bahrain;
  static const int32_t kArabic_Egypt;
  static const int32_t kArabic_Iraq;
  static const int32_t kArabic_Jordan;
  static const int32_t kArabic_Kuwait;
  static const int32_t kArabic_Lebanon;
  static const int32_t kArabic_Libya;
  static const int32_t kArabic_Morocco;
  static const int32_t kArabic_Oman;
  static const int32_t kArabic_Qatar;
  static const int32_t kArabic_SaudiArabia;
  static const int32_t kArabic_Syria;
  static const int32_t kArabic_Tunisia;
  static const int32_t kArabic_UAE;
  static const int32_t kArabic_Yemen;
  static const int32_t kArmenian_Armenia;
  static const int32_t kAssamese_India;
  static const int32_t kAzeri_Cyrillic_Azerbaijan;
  static const int32_t kAzeri_Latin_Azerbaijan;
  static const int32_t kBashkir_Russia;
  static const int32_t kBasque_Basque;
  static const int32_t kBelarusian_Belarus;
  static const int32_t kBengali_Bangladesh;
  static const int32_t kBengali_India;
  static const int32_t kBosnian_Cyrillic_BosniaAndHerzegovina;
  static const int32_t kBosnian_Latin_BosniaAndHerzegovina;
  static const int32_t kBreton_France;
  static const int32_t kBulgarian_Bulgaria;
  static const int32_t kCatalan_Catalan;
  static const int32_t kChinese_HongKongSAR;
  static const int32_t kChinese_MacaoSAR;
  static const int32_t kChinese_PeoplesRepublicOfChina;
  static const int32_t kChinese_Singapore;
  static const int32_t kChinese_Taiwan;
  static const int32_t kCorsican_France;
  static const int32_t kCroatian_Croatia;
  static const int32_t kCroatian_Latin_BosniaAndHerzegovina;
  static const int32_t kCzech_CzechRepublic;
  static const int32_t kDanish_Denmark;
  static const int32_t kDari_Afghanistan;
  static const int32_t kDivehi_Maldives;
  static const int32_t kDutch_Belgium;
  static const int32_t kDutch_Netherlands;
  static const int32_t kEnglish_Australia;
  static const int32_t kEnglish_Belize;
  static const int32_t kEnglish_Canada;
  static const int32_t kEnglish_Caribbean;
  static const int32_t kEnglish_India;
  static const int32_t kEnglish_Ireland;
  static const int32_t kEnglish_Jamaica;
  static const int32_t kEnglish_Malaysia;
  static const int32_t kEnglish_NewZealand;
  static const int32_t kEnglish_RepublicOfThePhilippines;
  static const int32_t kEnglish_Singapore;
  static const int32_t kEnglish_SouthAfrica;
  static const int32_t kEnglish_TrinidadAndTobago;
  static const int32_t kEnglish_UnitedKingdom;
  static const int32_t kEnglish_UnitedStates;
  static const int32_t kEnglish_Zimbabwe;
  static const int32_t kEstonian_Estonia;
  static const int32_t kFaroese_FaroeIslands;
  static const int32_t kFilipino_Philippines;
  static const int32_t kFinnish_Finland;
  static const int32_t kFrench_Belgium;
  static const int32_t kFrench_Canada;
  static const int32_t kFrench_France;
  static const int32_t kFrench_Luxembourg;
  static const int32_t kFrench_PrincipalityOfMonoco;
  static const int32_t kFrench_Switzerland;
  static const int32_t kFrisian_Netherlands;
  static const int32_t kGalician_Galician;
  static const int32_t kGeorgian_Georgia;
  static const int32_t kGerman_Austria;
  static const int32_t kGerman_Germany;
  static const int32_t kGerman_Liechtenstein;
  static const int32_t kGerman_Luxembourg;
  static const int32_t kGerman_Switzerland;
  static const int32_t kGreek_Greece;
  static const int32_t kGreenlandic_Greenland;
  static const int32_t kGujarati_India;
  static const int32_t kHausa_Latin_Nigeria;
  static const int32_t kHebrew_Israel;
  static const int32_t kHindi_India;
  static const int32_t kHungarian_Hungary;
  static const int32_t kIcelandic_Iceland;
  static const int32_t kIgbo_Nigeria;
  static const int32_t kIndonesian_Indonesia;
  static const int32_t kInuktitut_Canada;
  static const int32_t kInuktitut_Latin_Canada;
  static const int32_t kIrish_Ireland;
  static const int32_t kisiXhosa_SouthAfrica;
  static const int32_t kisiZulu_SouthAfrica;
  static const int32_t kItalian_Italy;
  static const int32_t kItalian_Switzerland;
  static const int32_t kJapanese_Japan;
  static const int32_t kKannada_India;
  static const int32_t kKazakh_Kazakhstan;
  static const int32_t kKhmer_Cambodia;
  static const int32_t kKiche_Guatemala;
  static const int32_t kKinyarwanda_Rwanda;
  static const int32_t kKiswahili_Kenya;
  static const int32_t kKonkani_India;
  static const int32_t kKorean_Korea;
  static const int32_t kKyrgyz_Kyrgyzstan;
  static const int32_t kLao_LaoPDR;
  static const int32_t kLatvian_Latvia;
  static const int32_t kLithuanian_Lithuania;
  static const int32_t kLowerSorbian_Germany;
  static const int32_t kLuxembourgish_Luxembourg;
  static const int32_t kMacedonian_FYROM_FormerYugoslavRepublicOfMacedonia;
  static const int32_t kMalay_BruneiDarussalam;
  static const int32_t kMalay_Malaysia;
  static const int32_t kMalayalam_India;
  static const int32_t kMaltese_Malta;
  static const int32_t kMaori_NewZealand;
  static const int32_t kMapudungun_Chile;
  static const int32_t kMarathi_India;
  static const int32_t kMohawk_Mohawk;
  static const int32_t kMongolian_Cyrillic_Mongolia;
  static const int32_t kMongolian_Traditional_PeoplesRepublicOfChina;
  static const int32_t kNepali_Nepal;
  static const int32_t kNorwegian_Bokmal_Norway;
  static const int32_t kNorwegian_Nynorsk_Norway;
  static const int32_t kOccitan_France;
  static const int32_t kOriya_India;
  static const int32_t kPashto_Afghanistan;
  static const int32_t kPolish_Poland;
  static const int32_t kPortuguese_Brazil;
  static const int32_t kPortuguese_Portugal;
  static const int32_t kPunjabi_India;
  static const int32_t kQuechua_Bolivia;
  static const int32_t kQuechua_Ecuador;
  static const int32_t kQuechua_Peru;
  static const int32_t kRomanian_Romania;
  static const int32_t kRomansh_Switzerland;
  static const int32_t kRussian_Russia;
  static const int32_t kSami_Inari_Finland;
  static const int32_t kSami_Lule_Norway;
  static const int32_t kSami_Lule_Sweden;
  static const int32_t kSami_Northern_Finland;
  static const int32_t kSami_Northern_Norway;
  static const int32_t kSami_Northern_Sweden;
  static const int32_t kSami_Skolt_Finland;
  static const int32_t kSami_Southern_Norway;
  static const int32_t kSami_Southern_Sweden;
  static const int32_t kSanskrit_India;
  static const int32_t kSerbian_Cyrillic_BosniaAndHerzegovina;
  static const int32_t kSerbian_Cyrillic_Serbia;
  static const int32_t kSerbian_Latin_BosniaAndHerzegovina;
  static const int32_t kSerbian_Latin_Serbia;
  static const int32_t kSesothoSaLeboa_SouthAfrica;
  static const int32_t kSetswana_SouthAfrica;
  static const int32_t kSinhala_SriLanka;
  static const int32_t kSlovak_Slovakia;
  static const int32_t kSlovenian_Slovenia;
  static const int32_t kSpanish_Argentina;
  static const int32_t kSpanish_Bolivia;
  static const int32_t kSpanish_Chile;
  static const int32_t kSpanish_Colombia;
  static const int32_t kSpanish_CostaRica;
  static const int32_t kSpanish_DominicanRepublic;
  static const int32_t kSpanish_Ecuador;
  static const int32_t kSpanish_ElSalvador;
  static const int32_t kSpanish_Guatemala;
  static const int32_t kSpanish_Honduras;
  static const int32_t kSpanish_Mexico;
  static const int32_t kSpanish_Nicaragua;
  static const int32_t kSpanish_Panama;
  static const int32_t kSpanish_Paraguay;
  static const int32_t kSpanish_Peru;
  static const int32_t kSpanish_PuertoRico;
  static const int32_t kSpanish_ModernSort_Spain;
  static const int32_t kSpanish_TraditionalSort_Spain;
  static const int32_t kSpanish_UnitedStates;
  static const int32_t kSpanish_Uruguay;
  static const int32_t kSpanish_Venezuela;
  static const int32_t kSweden_Finland;
  static const int32_t kSwedish_Sweden;
  static const int32_t kSyriac_Syria;
  static const int32_t kTajik_Cyrillic_Tajikistan;
  static const int32_t kTamazight_Latin_Algeria;
  static const int32_t kTamil_India;
  static const int32_t kTatar_Russia;
  static const int32_t kTelugu_India;
  static const int32_t kThai_Thailand;
  static const int32_t kTibetan_PRC;
  static const int32_t kTurkish_Turkey;
  static const int32_t kTurkmen_Turkmenistan;
  static const int32_t kUighur_PRC;
  static const int32_t kUkrainian_Ukraine;
  static const int32_t kUpperSorbian_Germany;
  static const int32_t kUrdu_IslamicRepublicOfPakistan;
  static const int32_t kUzbek_Cyrillic_Uzbekistan;
  static const int32_t kUzbek_Latin_Uzbekistan;
  static const int32_t kVietnamese_Vietnam;
  static const int32_t kWelsh_UnitedKingdom;
  static const int32_t kWolof_Senegal;
  static const int32_t kYakut_Russia;
  static const int32_t kYi_PRC;
  static const int32_t kYoruba_Nigeria;
};

class NameTable : public Table, public RefCounted<NameTable> {
 private:
  struct Offset {
    static const int32_t kFormat;
    static const int32_t kCount;
    static const int32_t kStringOffset;
    static const int32_t kNameRecordStart;

    // format 1 - offset from the end of the name records
    static const int32_t kLangTagCount;
    static const int32_t kLangTagRecord;

    static const int32_t kNameRecordSize;
    // Name Records
    static const int32_t kNameRecordPlatformId;
    static const int32_t kNameRecordEncodingId;
    static const int32_t kNameRecordLanguageId;
    static const int32_t kNameRecordNameId;
    static const int32_t kNameRecordStringLength;
    static const int32_t kNameRecordStringOffset;
  };

  struct NameId {
    static const int32_t kUnknown;
    static const int32_t kCopyrightNotice;
    static const int32_t kFontFamilyName;
    static const int32_t kFontSubfamilyName;
    static const int32_t kUniqueFontIdentifier;
    static const int32_t kFullFontName;
    static const int32_t kVersionString;
    static const int32_t kPostscriptName;
    static const int32_t kTrademark;
    static const int32_t kManufacturerName;
    static const int32_t kDesigner;
    static const int32_t kDescription;
    static const int32_t kVendorURL;
    static const int32_t kDesignerURL;
    static const int32_t kLicenseDescription;
    static const int32_t kLicenseInfoURL;
    static const int32_t kReserved15;
    static const int32_t kPreferredFamily;
    static const int32_t kPreferredSubfamily;
    static const int32_t kCompatibleFullName;
    static const int32_t kSampleText;
    static const int32_t kPostscriptCID;
    static const int32_t kWWSFamilyName;
    static const int32_t kWWSSubfamilyName;
  };

 public:
  class NameEntryBuilder;
  class NameEntry : public RefCounted<NameEntry> {
   public:
    NameEntry();
    NameEntry(int32_t platform_id, int32_t encoding_id, int32_t language_id,
              int32_t name_id, const ByteVector& name_bytes);
    virtual void init(int32_t platform_id, int32_t encoding_id,
                      int32_t language_id, int32_t name_id,
                      const ByteVector* name_bytes);
    virtual ~NameEntry();
    virtual int32_t platformId();
    virtual int32_t encodingId();
    virtual int32_t languageId();
    virtual int32_t nameId();
    virtual bool operator==(const NameEntry& obj);
    virtual int hashCode();
    virtual int compareTo(const NameEntry& obj);
    virtual int32_t nameBytesLength();  // C++ port only
    virtual ByteVector* nameBytes();

   protected:
    int32_t platform_id_;
    int32_t encoding_id_;
    int32_t language_id_;
    int32_t name_id_;
    int32_t length_;
    ByteVector name_bytes_;

    friend class NameEntryBuilder;
  };

  class NameEntryBuilder : public NameEntry {
   public:
    NameEntryBuilder();
    NameEntryBuilder(int32_t platform_id, int32_t encoding_id,
                     int32_t language_id, int32_t name_id,
                     const ByteVector& name_bytes);
    NameEntryBuilder(int32_t platform_id, int32_t encoding_id,
                     int32_t language_id, int32_t name_id);
    explicit NameEntryBuilder(NameEntry* entry);
    virtual ~NameEntryBuilder();
  };
  typedef Ptr<NameEntryBuilder> NameEntryBuilderPtr;
  typedef std::map<NameEntryBuilderPtr, NameEntryBuilderPtr> NameEntryMap;
  typedef std::pair<NameEntryBuilderPtr, NameEntryBuilderPtr> NameEntryMapEntry;

  class NameEntryFilter {
   public:
    virtual bool accept(int32_t platform_id, int32_t encoding_id,
                        int32_t language_id, int32_t name_id) = 0;
    // Make gcc -Wnon-virtual-dtor happy.
    virtual ~NameEntryFilter() {}
  };

  class Builder : public Table::ArrayElementTableBuilder {
   public:
    // Constructor scope altered to public because C++ does not allow base
    // class to instantiate derived class with protected constructors.
    Builder(FontDataTableBuilderContainer* font_builder, Header* header,
            WritableFontData* data);
    Builder(FontDataTableBuilderContainer* font_builder, Header* header,
            ReadableFontData* data);

    virtual int32_t subSerialize(WritableFontData* new_data);
    virtual bool subReadyToSerialize();
    virtual int32_t subDataSizeToSerialize();
    virtual void subDataSet();
    virtual CALLER_ATTACH FontDataTable* subBuildTable(ReadableFontData* data);

   private:
    void initialize(ReadableFontData* data);

   private:
    NameEntryMap name_entry_map_;
  };

  class NameEntryIterator {
   public:
    // If filter is NULL, filter through all tables.
    NameEntryIterator(NameTable* table, NameEntryFilter* filter);
    bool hasNext();
    NameEntry* next();

   private:
    NameTable* table_;  // use dumb pointer since it's a composition object
    int32_t name_index_;
    NameEntryFilter* filter_;
  };

 private:
  NameTable(Header* header, ReadableFontData* data);

 public:
  virtual ~NameTable();
  virtual int32_t format();
  virtual int32_t nameCount();
  virtual int32_t platformId(int32_t index);
  virtual int32_t encodingId(int32_t index);
  virtual int32_t languageId(int32_t index);
  virtual int32_t nameId(int32_t index);
  virtual void nameAsBytes(int32_t index, ByteVector* b);
  virtual CALLER_ATTACH NameEntry* nameEntry(int32_t index);

 private:
  int32_t stringOffset();
  int32_t offsetForNameRecord(int32_t index);
  int32_t nameLength(int32_t index);
  int32_t nameOffset(int32_t index);
};
typedef Ptr<NameTable> NameTablePtr;
typedef Ptr<NameTable::NameEntry> NameEntryPtr;

}  // namespace sfntly

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_NAME_TABLE_H_
