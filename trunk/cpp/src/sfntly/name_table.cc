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

#include "sfntly/name_table.h"

namespace sfntly {
/******************************************************************************
 * Constants
 ******************************************************************************/
const int32_t NameTable::Offset::kFormat = 0;
const int32_t NameTable::Offset::kCount = 2;
const int32_t NameTable::Offset::kStringOffset = 4;
const int32_t NameTable::Offset::kNameRecordStart = 6;
const int32_t NameTable::Offset::kLangTagCount = 0;
const int32_t NameTable::Offset::kLangTagRecord = 2;
const int32_t NameTable::Offset::kNameRecordSize = 12;
const int32_t NameTable::Offset::kNameRecordPlatformId = 0;
const int32_t NameTable::Offset::kNameRecordEncodingId = 2;
const int32_t NameTable::Offset::kNameRecordLanguageId = 4;
const int32_t NameTable::Offset::kNameRecordNameId = 6;
const int32_t NameTable::Offset::kNameRecordStringLength = 8;
const int32_t NameTable::Offset::kNameRecordStringOffset = 10;

const int32_t NameTable::NameId::kUnknown = -1;
const int32_t NameTable::NameId::kCopyrightNotice = 0;
const int32_t NameTable::NameId::kFontFamilyName = 1;
const int32_t NameTable::NameId::kFontSubfamilyName = 2;
const int32_t NameTable::NameId::kUniqueFontIdentifier = 3;
const int32_t NameTable::NameId::kFullFontName = 4;
const int32_t NameTable::NameId::kVersionString = 5;
const int32_t NameTable::NameId::kPostscriptName = 6;
const int32_t NameTable::NameId::kTrademark = 7;
const int32_t NameTable::NameId::kManufacturerName = 8;
const int32_t NameTable::NameId::kDesigner = 9;
const int32_t NameTable::NameId::kDescription = 10;
const int32_t NameTable::NameId::kVendorURL = 11;
const int32_t NameTable::NameId::kDesignerURL = 12;
const int32_t NameTable::NameId::kLicenseDescription = 13;
const int32_t NameTable::NameId::kLicenseInfoURL = 14;
const int32_t NameTable::NameId::kReserved15 = 15;
const int32_t NameTable::NameId::kPreferredFamily = 16;
const int32_t NameTable::NameId::kPreferredSubfamily = 17;
const int32_t NameTable::NameId::kCompatibleFullName = 18;
const int32_t NameTable::NameId::kSampleText = 19;
const int32_t NameTable::NameId::kPostscriptCID = 20;
const int32_t NameTable::NameId::kWWSFamilyName = 21;
const int32_t NameTable::NameId::kWWSSubfamilyName = 22;

const int32_t UnicodeLanguageId::kUnknown = -1;
const int32_t UnicodeLanguageId::kAll = 0;

const int32_t MacintoshLanguageId::kUnknown = -1;
const int32_t MacintoshLanguageId::kEnglish = 0;
const int32_t MacintoshLanguageId::kFrench = 1;
const int32_t MacintoshLanguageId::kGerman = 2;
const int32_t MacintoshLanguageId::kItalian = 3;
const int32_t MacintoshLanguageId::kDutch = 4;
const int32_t MacintoshLanguageId::kSwedish = 5;
const int32_t MacintoshLanguageId::kSpanish = 6;
const int32_t MacintoshLanguageId::kDanish = 7;
const int32_t MacintoshLanguageId::kPortuguese = 8;
const int32_t MacintoshLanguageId::kNorwegian = 9;
const int32_t MacintoshLanguageId::kHebrew = 10;
const int32_t MacintoshLanguageId::kJapanese = 11;
const int32_t MacintoshLanguageId::kArabic = 12;
const int32_t MacintoshLanguageId::kFinnish = 13;
const int32_t MacintoshLanguageId::kGreek = 14;
const int32_t MacintoshLanguageId::kIcelandic = 15;
const int32_t MacintoshLanguageId::kMaltese = 16;
const int32_t MacintoshLanguageId::kTurkish = 17;
const int32_t MacintoshLanguageId::kCroatian = 18;
const int32_t MacintoshLanguageId::kChinese_Traditional = 19;
const int32_t MacintoshLanguageId::kUrdu = 20;
const int32_t MacintoshLanguageId::kHindi = 21;
const int32_t MacintoshLanguageId::kThai = 22;
const int32_t MacintoshLanguageId::kKorean = 23;
const int32_t MacintoshLanguageId::kLithuanian = 24;
const int32_t MacintoshLanguageId::kPolish = 25;
const int32_t MacintoshLanguageId::kHungarian = 26;
const int32_t MacintoshLanguageId::kEstonian = 27;
const int32_t MacintoshLanguageId::kLatvian = 28;
const int32_t MacintoshLanguageId::kSami = 29;
const int32_t MacintoshLanguageId::kFaroese = 30;
const int32_t MacintoshLanguageId::kFarsiPersian = 31;
const int32_t MacintoshLanguageId::kRussian = 32;
const int32_t MacintoshLanguageId::kChinese_Simplified = 33;
const int32_t MacintoshLanguageId::kFlemish = 34;
const int32_t MacintoshLanguageId::kIrishGaelic = 35;
const int32_t MacintoshLanguageId::kAlbanian = 36;
const int32_t MacintoshLanguageId::kRomanian = 37;
const int32_t MacintoshLanguageId::kCzech = 38;
const int32_t MacintoshLanguageId::kSlovak = 39;
const int32_t MacintoshLanguageId::kSlovenian = 40;
const int32_t MacintoshLanguageId::kYiddish = 41;
const int32_t MacintoshLanguageId::kSerbian = 42;
const int32_t MacintoshLanguageId::kMacedonian = 43;
const int32_t MacintoshLanguageId::kBulgarian = 44;
const int32_t MacintoshLanguageId::kUkrainian = 45;
const int32_t MacintoshLanguageId::kByelorussian = 46;
const int32_t MacintoshLanguageId::kUzbek = 47;
const int32_t MacintoshLanguageId::kKazakh = 48;
const int32_t MacintoshLanguageId::kAzerbaijani_Cyrillic = 49;
const int32_t MacintoshLanguageId::kAzerbaijani_Arabic = 50;
const int32_t MacintoshLanguageId::kArmenian = 51;
const int32_t MacintoshLanguageId::kGeorgian = 52;
const int32_t MacintoshLanguageId::kMoldavian = 53;
const int32_t MacintoshLanguageId::kKirghiz = 54;
const int32_t MacintoshLanguageId::kTajiki = 55;
const int32_t MacintoshLanguageId::kTurkmen = 56;
const int32_t MacintoshLanguageId::kMongolian_Mongolian = 57;
const int32_t MacintoshLanguageId::kMongolian_Cyrillic = 58;
const int32_t MacintoshLanguageId::kPashto = 59;
const int32_t MacintoshLanguageId::kKurdish = 60;
const int32_t MacintoshLanguageId::kKashmiri = 61;
const int32_t MacintoshLanguageId::kSindhi = 62;
const int32_t MacintoshLanguageId::kTibetan = 63;
const int32_t MacintoshLanguageId::kNepali = 64;
const int32_t MacintoshLanguageId::kSanskrit = 65;
const int32_t MacintoshLanguageId::kMarathi = 66;
const int32_t MacintoshLanguageId::kBengali = 67;
const int32_t MacintoshLanguageId::kAssamese = 68;
const int32_t MacintoshLanguageId::kGujarati = 69;
const int32_t MacintoshLanguageId::kPunjabi = 70;
const int32_t MacintoshLanguageId::kOriya = 71;
const int32_t MacintoshLanguageId::kMalayalam = 72;
const int32_t MacintoshLanguageId::kKannada = 73;
const int32_t MacintoshLanguageId::kTamil = 74;
const int32_t MacintoshLanguageId::kTelugu = 75;
const int32_t MacintoshLanguageId::kSinhalese = 76;
const int32_t MacintoshLanguageId::kBurmese = 77;
const int32_t MacintoshLanguageId::kKhmer = 78;
const int32_t MacintoshLanguageId::kLao = 79;
const int32_t MacintoshLanguageId::kVietnamese = 80;
const int32_t MacintoshLanguageId::kIndonesian = 81;
const int32_t MacintoshLanguageId::kTagalong = 82;
const int32_t MacintoshLanguageId::kMalay_Roman = 83;
const int32_t MacintoshLanguageId::kMalay_Arabic = 84;
const int32_t MacintoshLanguageId::kAmharic = 85;
const int32_t MacintoshLanguageId::kTigrinya = 86;
const int32_t MacintoshLanguageId::kGalla = 87;
const int32_t MacintoshLanguageId::kSomali = 88;
const int32_t MacintoshLanguageId::kSwahili = 89;
const int32_t MacintoshLanguageId::kKinyarwandaRuanda = 90;
const int32_t MacintoshLanguageId::kRundi = 91;
const int32_t MacintoshLanguageId::kNyanjaChewa = 92;
const int32_t MacintoshLanguageId::kMalagasy = 93;
const int32_t MacintoshLanguageId::kEsperanto = 94;
const int32_t MacintoshLanguageId::kWelsh = 128;
const int32_t MacintoshLanguageId::kBasque = 129;
const int32_t MacintoshLanguageId::kCatalan = 130;
const int32_t MacintoshLanguageId::kLatin = 131;
const int32_t MacintoshLanguageId::kQuenchua = 132;
const int32_t MacintoshLanguageId::kGuarani = 133;
const int32_t MacintoshLanguageId::kAymara = 134;
const int32_t MacintoshLanguageId::kTatar = 135;
const int32_t MacintoshLanguageId::kUighur = 136;
const int32_t MacintoshLanguageId::kDzongkha = 137;
const int32_t MacintoshLanguageId::kJavanese_Roman = 138;
const int32_t MacintoshLanguageId::kSundanese_Roman = 139;
const int32_t MacintoshLanguageId::kGalician = 140;
const int32_t MacintoshLanguageId::kAfrikaans = 141;
const int32_t MacintoshLanguageId::kBreton = 142;
const int32_t MacintoshLanguageId::kInuktitut = 143;
const int32_t MacintoshLanguageId::kScottishGaelic = 144;
const int32_t MacintoshLanguageId::kManxGaelic = 145;
const int32_t MacintoshLanguageId::kIrishGaelic_WithDotAbove = 146;
const int32_t MacintoshLanguageId::kTongan = 147;
const int32_t MacintoshLanguageId::kGreek_Polytonic = 148;
const int32_t MacintoshLanguageId::kGreenlandic = 149;
const int32_t MacintoshLanguageId::kAzerbaijani_Roman = 150;

const int32_t WindowsLanguageId::kUnknown = -1;
const int32_t WindowsLanguageId::kAfrikaans_SouthAfrica = 0x0436;
const int32_t WindowsLanguageId::kAlbanian_Albania = 0x041C;
const int32_t WindowsLanguageId::kAlsatian_France = 0x0484;
const int32_t WindowsLanguageId::kAmharic_Ethiopia = 0x045E;
const int32_t WindowsLanguageId::kArabic_Algeria = 0x1401;
const int32_t WindowsLanguageId::kArabic_Bahrain = 0x3C01;
const int32_t WindowsLanguageId::kArabic_Egypt = 0x0C01;
const int32_t WindowsLanguageId::kArabic_Iraq = 0x0801;
const int32_t WindowsLanguageId::kArabic_Jordan = 0x2C01;
const int32_t WindowsLanguageId::kArabic_Kuwait = 0x3401;
const int32_t WindowsLanguageId::kArabic_Lebanon = 0x3001;
const int32_t WindowsLanguageId::kArabic_Libya = 0x1001;
const int32_t WindowsLanguageId::kArabic_Morocco = 0x1801;
const int32_t WindowsLanguageId::kArabic_Oman = 0x2001;
const int32_t WindowsLanguageId::kArabic_Qatar = 0x4001;
const int32_t WindowsLanguageId::kArabic_SaudiArabia = 0x0401;
const int32_t WindowsLanguageId::kArabic_Syria = 0x2801;
const int32_t WindowsLanguageId::kArabic_Tunisia = 0x1C01;
const int32_t WindowsLanguageId::kArabic_UAE = 0x3801;
const int32_t WindowsLanguageId::kArabic_Yemen = 0x2401;
const int32_t WindowsLanguageId::kArmenian_Armenia = 0x042B;
const int32_t WindowsLanguageId::kAssamese_India = 0x044D;
const int32_t WindowsLanguageId::kAzeri_Cyrillic_Azerbaijan = 0x082C;
const int32_t WindowsLanguageId::kAzeri_Latin_Azerbaijan = 0x042C;
const int32_t WindowsLanguageId::kBashkir_Russia = 0x046D;
const int32_t WindowsLanguageId::kBasque_Basque = 0x042D;
const int32_t WindowsLanguageId::kBelarusian_Belarus = 0x0423;
const int32_t WindowsLanguageId::kBengali_Bangladesh = 0x0845;
const int32_t WindowsLanguageId::kBengali_India = 0x0445;
const int32_t WindowsLanguageId::kBosnian_Cyrillic_BosniaAndHerzegovina = 0x201A;
const int32_t WindowsLanguageId::kBosnian_Latin_BosniaAndHerzegovina = 0x141A;
const int32_t WindowsLanguageId::kBreton_France = 0x047E;
const int32_t WindowsLanguageId::kBulgarian_Bulgaria = 0x0402;
const int32_t WindowsLanguageId::kCatalan_Catalan = 0x0403;
const int32_t WindowsLanguageId::kChinese_HongKongSAR = 0x0C04;
const int32_t WindowsLanguageId::kChinese_MacaoSAR = 0x1404;
const int32_t WindowsLanguageId::kChinese_PeoplesRepublicOfChina = 0x0804;
const int32_t WindowsLanguageId::kChinese_Singapore = 0x1004;
const int32_t WindowsLanguageId::kChinese_Taiwan = 0x0404;
const int32_t WindowsLanguageId::kCorsican_France = 0x0483;
const int32_t WindowsLanguageId::kCroatian_Croatia = 0x041A;
const int32_t WindowsLanguageId::kCroatian_Latin_BosniaAndHerzegovina = 0x101A;
const int32_t WindowsLanguageId::kCzech_CzechRepublic = 0x0405;
const int32_t WindowsLanguageId::kDanish_Denmark = 0x0406;
const int32_t WindowsLanguageId::kDari_Afghanistan = 0x048C;
const int32_t WindowsLanguageId::kDivehi_Maldives = 0x0465;
const int32_t WindowsLanguageId::kDutch_Belgium = 0x0813;
const int32_t WindowsLanguageId::kDutch_Netherlands = 0x0413;
const int32_t WindowsLanguageId::kEnglish_Australia = 0x0C09;
const int32_t WindowsLanguageId::kEnglish_Belize = 0x2809;
const int32_t WindowsLanguageId::kEnglish_Canada = 0x1009;
const int32_t WindowsLanguageId::kEnglish_Caribbean = 0x2409;
const int32_t WindowsLanguageId::kEnglish_India = 0x4009;
const int32_t WindowsLanguageId::kEnglish_Ireland = 0x1809;
const int32_t WindowsLanguageId::kEnglish_Jamaica = 0x2009;
const int32_t WindowsLanguageId::kEnglish_Malaysia = 0x4409;
const int32_t WindowsLanguageId::kEnglish_NewZealand = 0x1409;
const int32_t WindowsLanguageId::kEnglish_RepublicOfThePhilippines = 0x3409;
const int32_t WindowsLanguageId::kEnglish_Singapore = 0x4809;
const int32_t WindowsLanguageId::kEnglish_SouthAfrica = 0x1C09;
const int32_t WindowsLanguageId::kEnglish_TrinidadAndTobago = 0x2C09;
const int32_t WindowsLanguageId::kEnglish_UnitedKingdom = 0x0809;
const int32_t WindowsLanguageId::kEnglish_UnitedStates = 0x0409;
const int32_t WindowsLanguageId::kEnglish_Zimbabwe = 0x3009;
const int32_t WindowsLanguageId::kEstonian_Estonia = 0x0425;
const int32_t WindowsLanguageId::kFaroese_FaroeIslands = 0x0438;
const int32_t WindowsLanguageId::kFilipino_Philippines = 0x0464;
const int32_t WindowsLanguageId::kFinnish_Finland = 0x040B;
const int32_t WindowsLanguageId::kFrench_Belgium = 0x080C;
const int32_t WindowsLanguageId::kFrench_Canada = 0x0C0C;
const int32_t WindowsLanguageId::kFrench_France = 0x040C;
const int32_t WindowsLanguageId::kFrench_Luxembourg = 0x140c;
const int32_t WindowsLanguageId::kFrench_PrincipalityOfMonoco = 0x180C;
const int32_t WindowsLanguageId::kFrench_Switzerland = 0x100C;
const int32_t WindowsLanguageId::kFrisian_Netherlands = 0x0462;
const int32_t WindowsLanguageId::kGalician_Galician = 0x0456;
const int32_t WindowsLanguageId::kGeorgian_Georgia = 0x0437;
const int32_t WindowsLanguageId::kGerman_Austria = 0x0C07;
const int32_t WindowsLanguageId::kGerman_Germany = 0x0407;
const int32_t WindowsLanguageId::kGerman_Liechtenstein = 0x1407;
const int32_t WindowsLanguageId::kGerman_Luxembourg = 0x1007;
const int32_t WindowsLanguageId::kGerman_Switzerland = 0x0807;
const int32_t WindowsLanguageId::kGreek_Greece = 0x0408;
const int32_t WindowsLanguageId::kGreenlandic_Greenland = 0x046F;
const int32_t WindowsLanguageId::kGujarati_India = 0x0447;
const int32_t WindowsLanguageId::kHausa_Latin_Nigeria = 0x0468;
const int32_t WindowsLanguageId::kHebrew_Israel = 0x040D;
const int32_t WindowsLanguageId::kHindi_India = 0x0439;
const int32_t WindowsLanguageId::kHungarian_Hungary = 0x040E;
const int32_t WindowsLanguageId::kIcelandic_Iceland = 0x040F;
const int32_t WindowsLanguageId::kIgbo_Nigeria = 0x0470;
const int32_t WindowsLanguageId::kIndonesian_Indonesia = 0x0421;
const int32_t WindowsLanguageId::kInuktitut_Canada = 0x045D;
const int32_t WindowsLanguageId::kInuktitut_Latin_Canada = 0x085D;
const int32_t WindowsLanguageId::kIrish_Ireland = 0x083C;
const int32_t WindowsLanguageId::kisiXhosa_SouthAfrica = 0x0434;
const int32_t WindowsLanguageId::kisiZulu_SouthAfrica = 0x0435;
const int32_t WindowsLanguageId::kItalian_Italy = 0x0410;
const int32_t WindowsLanguageId::kItalian_Switzerland = 0x0810;
const int32_t WindowsLanguageId::kJapanese_Japan = 0x0411;
const int32_t WindowsLanguageId::kKannada_India = 0x044B;
const int32_t WindowsLanguageId::kKazakh_Kazakhstan = 0x043F;
const int32_t WindowsLanguageId::kKhmer_Cambodia = 0x0453;
const int32_t WindowsLanguageId::kKiche_Guatemala = 0x0486;
const int32_t WindowsLanguageId::kKinyarwanda_Rwanda = 0x0487;
const int32_t WindowsLanguageId::kKiswahili_Kenya = 0x0441;
const int32_t WindowsLanguageId::kKonkani_India = 0x0457;
const int32_t WindowsLanguageId::kKorean_Korea = 0x0412;
const int32_t WindowsLanguageId::kKyrgyz_Kyrgyzstan = 0x0440;
const int32_t WindowsLanguageId::kLao_LaoPDR = 0x0454;
const int32_t WindowsLanguageId::kLatvian_Latvia = 0x0426;
const int32_t WindowsLanguageId::kLithuanian_Lithuania = 0x0427;
const int32_t WindowsLanguageId::kLowerSorbian_Germany = 0x082E;
const int32_t WindowsLanguageId::kLuxembourgish_Luxembourg = 0x046E;
const int32_t WindowsLanguageId::kMacedonian_FYROM_FormerYugoslavRepublicOfMacedonia = 0x042F;
const int32_t WindowsLanguageId::kMalay_BruneiDarussalam = 0x083E;
const int32_t WindowsLanguageId::kMalay_Malaysia = 0x043E;
const int32_t WindowsLanguageId::kMalayalam_India = 0x044C;
const int32_t WindowsLanguageId::kMaltese_Malta = 0x043A;
const int32_t WindowsLanguageId::kMaori_NewZealand = 0x0481;
const int32_t WindowsLanguageId::kMapudungun_Chile = 0x047A;
const int32_t WindowsLanguageId::kMarathi_India = 0x044E;
const int32_t WindowsLanguageId::kMohawk_Mohawk = 0x047C;
const int32_t WindowsLanguageId::kMongolian_Cyrillic_Mongolia = 0x0450;
const int32_t WindowsLanguageId::kMongolian_Traditional_PeoplesRepublicOfChina = 0x0850;
const int32_t WindowsLanguageId::kNepali_Nepal = 0x0461;
const int32_t WindowsLanguageId::kNorwegian_Bokmal_Norway = 0x0414;
const int32_t WindowsLanguageId::kNorwegian_Nynorsk_Norway = 0x0814;
const int32_t WindowsLanguageId::kOccitan_France = 0x0482;
const int32_t WindowsLanguageId::kOriya_India = 0x0448;
const int32_t WindowsLanguageId::kPashto_Afghanistan = 0x0463;
const int32_t WindowsLanguageId::kPolish_Poland = 0x0415;
const int32_t WindowsLanguageId::kPortuguese_Brazil = 0x0416;
const int32_t WindowsLanguageId::kPortuguese_Portugal = 0x0816;
const int32_t WindowsLanguageId::kPunjabi_India = 0x0446;
const int32_t WindowsLanguageId::kQuechua_Bolivia = 0x046B;
const int32_t WindowsLanguageId::kQuechua_Ecuador = 0x086B;
const int32_t WindowsLanguageId::kQuechua_Peru = 0x0C6B;
const int32_t WindowsLanguageId::kRomanian_Romania = 0x0418;
const int32_t WindowsLanguageId::kRomansh_Switzerland = 0x0417;
const int32_t WindowsLanguageId::kRussian_Russia = 0x0419;
const int32_t WindowsLanguageId::kSami_Inari_Finland = 0x243B;
const int32_t WindowsLanguageId::kSami_Lule_Norway = 0x103B;
const int32_t WindowsLanguageId::kSami_Lule_Sweden = 0x143B;
const int32_t WindowsLanguageId::kSami_Northern_Finland = 0x0C3B;
const int32_t WindowsLanguageId::kSami_Northern_Norway = 0x043B;
const int32_t WindowsLanguageId::kSami_Northern_Sweden = 0x083B;
const int32_t WindowsLanguageId::kSami_Skolt_Finland = 0x203B;
const int32_t WindowsLanguageId::kSami_Southern_Norway = 0x183B;
const int32_t WindowsLanguageId::kSami_Southern_Sweden = 0x1C3B;
const int32_t WindowsLanguageId::kSanskrit_India = 0x044F;
const int32_t WindowsLanguageId::kSerbian_Cyrillic_BosniaAndHerzegovina = 0x1C1A;
const int32_t WindowsLanguageId::kSerbian_Cyrillic_Serbia = 0x0C1A;
const int32_t WindowsLanguageId::kSerbian_Latin_BosniaAndHerzegovina = 0x181A;
const int32_t WindowsLanguageId::kSerbian_Latin_Serbia = 0x081A;
const int32_t WindowsLanguageId::kSesothoSaLeboa_SouthAfrica = 0x046C;
const int32_t WindowsLanguageId::kSetswana_SouthAfrica = 0x0432;
const int32_t WindowsLanguageId::kSinhala_SriLanka = 0x045B;
const int32_t WindowsLanguageId::kSlovak_Slovakia = 0x041B;
const int32_t WindowsLanguageId::kSlovenian_Slovenia = 0x0424;
const int32_t WindowsLanguageId::kSpanish_Argentina = 0x2C0A;
const int32_t WindowsLanguageId::kSpanish_Bolivia = 0x400A;
const int32_t WindowsLanguageId::kSpanish_Chile = 0x340A;
const int32_t WindowsLanguageId::kSpanish_Colombia = 0x240A;
const int32_t WindowsLanguageId::kSpanish_CostaRica = 0x140A;
const int32_t WindowsLanguageId::kSpanish_DominicanRepublic = 0x1C0A;
const int32_t WindowsLanguageId::kSpanish_Ecuador = 0x300A;
const int32_t WindowsLanguageId::kSpanish_ElSalvador = 0x440A;
const int32_t WindowsLanguageId::kSpanish_Guatemala = 0x100A;
const int32_t WindowsLanguageId::kSpanish_Honduras = 0x480A;
const int32_t WindowsLanguageId::kSpanish_Mexico = 0x080A;
const int32_t WindowsLanguageId::kSpanish_Nicaragua = 0x4C0A;
const int32_t WindowsLanguageId::kSpanish_Panama = 0x180A;
const int32_t WindowsLanguageId::kSpanish_Paraguay = 0x3C0A;
const int32_t WindowsLanguageId::kSpanish_Peru = 0x280A;
const int32_t WindowsLanguageId::kSpanish_PuertoRico = 0x500A;
const int32_t WindowsLanguageId::kSpanish_ModernSort_Spain = 0x0C0A;
const int32_t WindowsLanguageId::kSpanish_TraditionalSort_Spain = 0x040A;
const int32_t WindowsLanguageId::kSpanish_UnitedStates = 0x540A;
const int32_t WindowsLanguageId::kSpanish_Uruguay = 0x380A;
const int32_t WindowsLanguageId::kSpanish_Venezuela = 0x200A;
const int32_t WindowsLanguageId::kSweden_Finland = 0x081D;
const int32_t WindowsLanguageId::kSwedish_Sweden = 0x041D;
const int32_t WindowsLanguageId::kSyriac_Syria = 0x045A;
const int32_t WindowsLanguageId::kTajik_Cyrillic_Tajikistan = 0x0428;
const int32_t WindowsLanguageId::kTamazight_Latin_Algeria = 0x085F;
const int32_t WindowsLanguageId::kTamil_India = 0x0449;
const int32_t WindowsLanguageId::kTatar_Russia = 0x0444;
const int32_t WindowsLanguageId::kTelugu_India = 0x044A;
const int32_t WindowsLanguageId::kThai_Thailand = 0x041E;
const int32_t WindowsLanguageId::kTibetan_PRC = 0x0451;
const int32_t WindowsLanguageId::kTurkish_Turkey = 0x041F;
const int32_t WindowsLanguageId::kTurkmen_Turkmenistan = 0x0442;
const int32_t WindowsLanguageId::kUighur_PRC = 0x0480;
const int32_t WindowsLanguageId::kUkrainian_Ukraine = 0x0422;
const int32_t WindowsLanguageId::kUpperSorbian_Germany = 0x042E;
const int32_t WindowsLanguageId::kUrdu_IslamicRepublicOfPakistan = 0x0420;
const int32_t WindowsLanguageId::kUzbek_Cyrillic_Uzbekistan = 0x0843;
const int32_t WindowsLanguageId::kUzbek_Latin_Uzbekistan = 0x0443;
const int32_t WindowsLanguageId::kVietnamese_Vietnam = 0x042A;
const int32_t WindowsLanguageId::kWelsh_UnitedKingdom = 0x0452;
const int32_t WindowsLanguageId::kWolof_Senegal = 0x0448;
const int32_t WindowsLanguageId::kYakut_Russia = 0x0485;
const int32_t WindowsLanguageId::kYi_PRC = 0x0478;
const int32_t WindowsLanguageId::kYoruba_Nigeria = 0x046A;

/******************************************************************************
 * NameTable class
 ******************************************************************************/
NameTable::NameTable(Header* header, ReadableFontData* data)
    : Table(header, data) {}

NameTable::~NameTable() {}

int32_t NameTable::format() {
  return data_->readUShort(Offset::kFormat);
}

int32_t NameTable::nameCount() {
  return data_->readUShort(Offset::kCount);
}

int32_t NameTable::stringOffset() {
  return data_->readUShort(Offset::kStringOffset);
}

int32_t NameTable::offsetForNameRecord(int32_t index) {
  return Offset::kNameRecordStart + index * Offset::kNameRecordSize;
}

int32_t NameTable::platformId(int32_t index) {
  return data_->readUShort(Offset::kNameRecordPlatformId +
                           offsetForNameRecord(index));
}

int32_t NameTable::encodingId(int32_t index) {
  return data_->readUShort(Offset::kNameRecordEncodingId +
                           offsetForNameRecord(index));
}

int32_t NameTable::languageId(int32_t index) {
  return data_->readUShort(Offset::kNameRecordLanguageId +
                           offsetForNameRecord(index));
}

int32_t NameTable::nameId(int32_t index) {
  return data_->readUShort(Offset::kNameRecordNameId +
                           offsetForNameRecord(index));
}

int32_t NameTable::nameLength(int32_t index) {
  return data_->readUShort(Offset::kNameRecordStringLength +
                           offsetForNameRecord(index));
}

int32_t NameTable::nameOffset(int32_t index) {
  return data_->readUShort(Offset::kNameRecordStringOffset +
                           offsetForNameRecord(index) + stringOffset());
}

void NameTable::nameAsBytes(int32_t index, ByteVector* b) {
  assert(b);
  int32_t length = nameLength(index);
  b->clear();
  b->resize(length);
  data_->readBytes(nameOffset(index), b, 0, length);
}

CALLER_ATTACH NameTable::NameEntry* NameTable::nameEntry(int32_t index) {
  ByteVector b;
  nameAsBytes(index, &b);
  NameEntryPtr instance = new NameEntry(platformId(index), encodingId(index),
                                        languageId(index), nameId(index), b);
  return instance.detach();
}

/******************************************************************************
 * NameTable::NameEntry class
 ******************************************************************************/
void NameTable::NameEntry::init(int32_t platform_id, int32_t encoding_id,
                                int32_t language_id, int32_t name_id,
                                const ByteVector* name_bytes) {
  platform_id_ = platform_id;
  encoding_id_ = encoding_id;
  language_id_ = language_id;
  name_id_ = name_id;
  if (name_bytes)
    name_bytes_ = *name_bytes;
}

NameTable::NameEntry::NameEntry() {
  init(0, 0, 0, 0, NULL);
}

NameTable::NameEntry::NameEntry(int32_t platform_id, int32_t encoding_id,
                                int32_t language_id, int32_t name_id,
                                const ByteVector& name_bytes) {
  init(platform_id, encoding_id, language_id, name_id, &name_bytes);
}

NameTable::NameEntry::~NameEntry() {}
int32_t NameTable::NameEntry::platformId() { return platform_id_; }
int32_t NameTable::NameEntry::encodingId() { return encoding_id_; }
int32_t NameTable::NameEntry::languageId() { return language_id_; }
int32_t NameTable::NameEntry::nameId() { return name_id_; }
int32_t NameTable::NameEntry::nameBytesLength() { return name_bytes_.size(); }
ByteVector* NameTable::NameEntry::nameBytes() { return &name_bytes_; }

bool NameTable::NameEntry::operator==(const NameEntry& obj) {
  return (encoding_id_ == obj.encoding_id_ &&
          language_id_ == obj.language_id_ &&
          platform_id_ == obj.platform_id_ &&
          name_id_ == obj.name_id_);
}

int NameTable::NameEntry::hashCode() {
  return ((encoding_id_ & 0x3f) << 26) | ((name_id_ & 0x3f) << 16) |
         ((platform_id_ & 0x0f) << 12) | (language_id_ & 0xff);
}

int NameTable::NameEntry::compareTo(const NameEntry& o) {
  if (platform_id_ != o.platform_id_) {
    return platform_id_ - o.platform_id_;
  }
  if (encoding_id_ != o.encoding_id_) {
    return encoding_id_ - o.encoding_id_;
  }
  if (language_id_ != o.language_id_) {
    return language_id_ - o.language_id_;
  }
  return name_id_ - o.name_id_;
}

/******************************************************************************
 * NameTable::NameEntryBuilder class
 ******************************************************************************/
NameTable::NameEntryBuilder::NameEntryBuilder() {
  init(0, 0, 0, 0, NULL);
}

NameTable::NameEntryBuilder::NameEntryBuilder(
    int32_t platform_id, int32_t encoding_id, int32_t language_id,
    int32_t name_id, const ByteVector& name_bytes) {
  init(platform_id, encoding_id, language_id, name_id, &name_bytes);
}

NameTable::NameEntryBuilder::NameEntryBuilder(
    int32_t platform_id, int32_t encoding_id, int32_t language_id,
    int32_t name_id) {
  init(platform_id, encoding_id, language_id, name_id, NULL);
}

NameTable::NameEntryBuilder::NameEntryBuilder(NameEntry* b) {
  init(b->platform_id_, b->encoding_id_, b->language_id_, b->name_id_,
       b->nameBytes());
}

NameTable::NameEntryBuilder::~NameEntryBuilder() {}

/******************************************************************************
 * NameTable::NameEntryIterator class
 ******************************************************************************/
NameTable::NameEntryIterator::NameEntryIterator(NameTable* table,
                                                NameEntryFilter* filter) :
    table_(table), filter_(filter), name_index_(0) {
}

bool NameTable::NameEntryIterator::hasNext() {
  if (!filter_) {
    if (name_index_ < table_->nameCount()) {
      return true;
    }
    return false;
  }
  for (; name_index_ < table_->nameCount(); ++name_index_) {
    if (filter_->accept(table_->platformId(name_index_),
                        table_->encodingId(name_index_),
                        table_->languageId(name_index_),
                        table_->nameId(name_index_))) {
      return true;
    }
  }
  return false;
}

NameTable::NameEntry* NameTable::NameEntryIterator::next() {
  if (!hasNext())
    return NULL;
  return table_->nameEntry(name_index_++);
}

/******************************************************************************
 * NameTable::Builder class
 ******************************************************************************/
NameTable::Builder::Builder(FontDataTableBuilderContainer* font_builder,
                            Header* header, WritableFontData* data) :
    Table::ArrayElementTableBuilder(font_builder, header, data) {
}

NameTable::Builder::Builder(FontDataTableBuilderContainer* font_builder,
                            Header* header, ReadableFontData* data) :
    Table::ArrayElementTableBuilder(font_builder, header, data) {
}

void NameTable::Builder::initialize(ReadableFontData* data) {
  if (data) {
    NameTablePtr table = new NameTable(header(), data);
    NameEntryIterator name_iter(table, NULL);
    while (name_iter.hasNext()) {
      NameEntryPtr name_entry(name_iter.next());
      NameEntryBuilderPtr name_entry_builder = new NameEntryBuilder(name_entry);
      name_entry_map_.insert(NameEntryMapEntry(name_entry_builder,
                                               name_entry_builder));
    }
  }
}

int32_t NameTable::Builder::subSerialize(WritableFontData* new_data) {
  int32_t string_table_start_offset =
      NameTable::Offset::kNameRecordStart + name_entry_map_.size() *
      NameTable::Offset::kNameRecordSize;

  // header
  new_data->writeUShort(NameTable::Offset::kFormat, 0);
  new_data->writeUShort(NameTable::Offset::kCount, name_entry_map_.size());
  new_data->writeUShort(NameTable::Offset::kStringOffset,
                        string_table_start_offset);
  int32_t name_record_offset = NameTable::Offset::kNameRecordStart;
  int32_t string_offset = 0;
  for (NameEntryMap::iterator b = name_entry_map_.begin(),
                              end = name_entry_map_.end(); b != end; ++b) {
    new_data->writeUShort(NameTable::Offset::kNameRecordPlatformId,
                          b->first->platformId());
    new_data->writeUShort(NameTable::Offset::kNameRecordEncodingId,
                          b->first->encodingId());
    new_data->writeUShort(NameTable::Offset::kNameRecordLanguageId,
                          b->first->languageId());
    new_data->writeUShort(NameTable::Offset::kNameRecordNameId,
                          b->first->nameId());
    new_data->writeUShort(NameTable::Offset::kNameRecordStringLength,
                          b->first->nameBytesLength());
    new_data->writeUShort(NameTable::Offset::kNameRecordStringOffset,
                          string_offset);
    name_record_offset += NameTable::Offset::kNameRecordSize;
    string_offset += new_data->writeBytes(
        string_offset + string_table_start_offset, b->first->nameBytes());
  }

  return string_offset + string_table_start_offset;
}

bool NameTable::Builder::subReadyToSerialize() {
  return !name_entry_map_.empty();
}

int32_t NameTable::Builder::subDataSizeToSerialize() {
  if (name_entry_map_.empty()) {
    return 0;
  }

  int32_t size = NameTable::Offset::kNameRecordStart + name_entry_map_.size() *
                 NameTable::Offset::kNameRecordSize;
  for (NameEntryMap::iterator b = name_entry_map_.begin(),
                              end = name_entry_map_.end(); b != end; ++b) {
    size += b->first->nameBytesLength();
  }
  return size;
}

void NameTable::Builder::subDataSet() {
  name_entry_map_.clear();
  setModelChanged(false);
}

CALLER_ATTACH FontDataTable* NameTable::Builder::subBuildTable(
    ReadableFontData* data) {
  FontDataTablePtr table = new NameTable(header(), data);
  return table.detach();
}

}  // namespace sfntly
