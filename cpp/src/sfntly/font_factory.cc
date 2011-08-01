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

#include "sfntly/font_factory.h"

#include <string.h>

#include "sfntly/tag.h"
#include "sfntly/data/memory_byte_array.h"
#include "sfntly/data/growable_memory_byte_array.h"

namespace sfntly {

FontFactory::~FontFactory() {
}

CALLER_ATTACH FontFactory* FontFactory::GetInstance() {
  FontFactoryPtr instance = new FontFactory();
  return instance.Detach();
}

void FontFactory::FingerprintFont(bool fingerprint) {
  fingerprint_ = fingerprint;
}

bool FontFactory::FingerprintFont() {
  return fingerprint_;
}

void FontFactory::LoadFonts(InputStream* is, FontArray* output) {
  assert(output);
  PushbackInputStream* pbis = down_cast<PushbackInputStream*>(is);
  if (IsCollection(pbis)) {
    LoadCollection(pbis, output);
    return;
  }
  FontPtr font = LoadSingleOTF(pbis);
  if (font) {
    output->push_back(font);
  }
}

void FontFactory::LoadFonts(ByteArray* ba, FontArray* output) {
  if (IsCollection(ba)) {
    LoadCollection(ba, output);
    return;
  }
  FontPtr font;
  font.Attach(LoadSingleOTF(ba));
  if (font) {
    output->push_back(font);
  }
}

void FontFactory::LoadFontsForBuilding(InputStream* is,
                                       FontBuilderArray* output) {
  PushbackInputStream* pbis = down_cast<PushbackInputStream*>(is);
  if (IsCollection(pbis)) {
    LoadCollectionForBuilding(pbis, output);
    return;
  }
  FontBuilderPtr builder;
  builder.Attach(LoadSingleOTFForBuilding(pbis));
  if (builder) {
    output->push_back(builder);
  }
}

void FontFactory::LoadFontsForBuilding(ByteArray* ba,
                                       FontBuilderArray* output) {
  if (IsCollection(ba)) {
    LoadCollectionForBuilding(ba, output);
    return;
  }
  FontBuilderPtr builder;
  builder.Attach(LoadSingleOTFForBuilding(ba, 0));
  if (builder) {
    output->push_back(builder);
  }
}

CALLER_ATTACH WritableFontData* FontFactory::GetNewData(int32_t capacity) {
  // UNIMPLMENTED: if (capacity > 0) { this.GetNewFixedData(capacity); }
  // Seems a no-op.
  return GetNewGrowableData(capacity);
}

CALLER_ATTACH WritableFontData* FontFactory::GetNewFixedData(int32_t capacity) {
  ByteArrayPtr buffer;
  buffer.Attach(GetNewArray(capacity));
  WritableFontDataPtr new_fixed_data = new WritableFontData(buffer);
  return new_fixed_data.Detach();
}

CALLER_ATTACH WritableFontData* FontFactory::GetNewGrowableData(
    int32_t capacity) {
  ByteArrayPtr buffer;
  buffer.Attach(GetNewGrowableArray(capacity));
  WritableFontDataPtr new_growable_data = new WritableFontData(buffer);
  return new_growable_data.Detach();
}

CALLER_ATTACH WritableFontData* FontFactory::GetNewGrowableData(
    ReadableFontData* src_data) {
  WritableFontDataPtr data;
  data.Attach(GetNewGrowableData(src_data->Length()));
  src_data->CopyTo(data);
  return data.Detach();
}

CALLER_ATTACH ByteArray* FontFactory::GetNewArray(int32_t length) {
  ByteArrayPtr new_fixed_array = new MemoryByteArray(length);
  return new_fixed_array.Detach();
}

CALLER_ATTACH ByteArray* FontFactory::GetNewGrowableArray(int32_t length) {
  UNREFERENCED_PARAMETER(length);
  ByteArrayPtr new_growable_array = new GrowableMemoryByteArray();
  return new_growable_array.Detach();
}

void FontFactory::SerializeFont(Font* font, OutputStream* os) {
  font->Serialize(os, &table_ordering_);
}

void FontFactory::SetSerializationTableOrdering(
    const IntegerList& table_ordering) {
  table_ordering_ = table_ordering;
}

CALLER_ATTACH Font::Builder* FontFactory::NewFontBuilder() {
  return Font::Builder::GetOTFBuilder(this);
}

CALLER_ATTACH Font* FontFactory::LoadSingleOTF(InputStream* is) {
  FontBuilderPtr builder;
  builder.Attach(LoadSingleOTFForBuilding(is));
  return builder->Build();
}

CALLER_ATTACH Font* FontFactory::LoadSingleOTF(ByteArray* ba) {
  FontBuilderPtr builder;
  builder.Attach(LoadSingleOTFForBuilding(ba, 0));
  return builder->Build();
}

void FontFactory::LoadCollection(InputStream* is, FontArray* output) {
  FontBuilderArray ba;
  LoadCollectionForBuilding(is, &ba);
  output->reserve(ba.size());
  for (FontBuilderArray::iterator builder = ba.begin(), builders_end = ba.end();
                                  builder != builders_end; ++builder) {
      FontPtr font;
      font.Attach((*builder)->Build());
      output->push_back(font);
  }
}

void FontFactory::LoadCollection(ByteArray* ba, FontArray* output) {
  FontBuilderArray builders;
  LoadCollectionForBuilding(ba, &builders);
  output->reserve(ba->Size());
  for (FontBuilderArray::iterator
           builder = builders.begin(), builders_end = builders.end();
       builder != builders_end; ++builder) {
    FontPtr font;
    font.Attach((*builder)->Build());
    output->push_back(font);
  }
}

CALLER_ATTACH Font::Builder*
    FontFactory::LoadSingleOTFForBuilding(InputStream* is) {
  // UNIMPLEMENTED: SHA-1 hash checking via Java DigestStream
  Font::Builder* builder = Font::Builder::GetOTFBuilder(this, is);
  // UNIMPLEMENTED: setDigest
  return builder;
}

CALLER_ATTACH Font::Builder*
    FontFactory::LoadSingleOTFForBuilding(ByteArray* ba,
                                          int32_t offset_to_offset_table) {
  // UNIMPLEMENTED: SHA-1 hash checking via Java DigestStream
  Font::Builder* builder =
      Font::Builder::GetOTFBuilder(this, ba, offset_to_offset_table);
  // UNIMPLEMENTED: setDigest
  return builder;
}

void FontFactory::LoadCollectionForBuilding(InputStream* is,
                                            FontBuilderArray* builders) {
  ByteArrayPtr ba = new GrowableMemoryByteArray();
  ba->CopyFrom(is);
  LoadCollectionForBuilding(ba, builders);
}

void FontFactory::LoadCollectionForBuilding(ByteArray* ba,
                                            FontBuilderArray* builders) {
  ReadableFontDataPtr rfd = new ReadableFontData(ba);
  int32_t ttc_tag = rfd->ReadULongAsInt(Offset::kTTCTag);
  UNREFERENCED_PARAMETER(ttc_tag);
  int32_t version = rfd->ReadFixed(Offset::kVersion);
  UNREFERENCED_PARAMETER(version);
  int32_t num_fonts = rfd->ReadULongAsInt(Offset::kNumFonts);

  builders->reserve(num_fonts);
  int32_t offset_table_offset = Offset::kOffsetTable;
  for (int32_t font_number = 0; font_number < num_fonts;
       font_number++, offset_table_offset += DataSize::kULONG) {
    int32_t offset = rfd->ReadULongAsInt(offset_table_offset);
    FontBuilderPtr builder;
    builder.Attach(LoadSingleOTFForBuilding(ba, offset));
    builders->push_back(builder);
  }
}

bool FontFactory::IsCollection(PushbackInputStream* pbis) {
  ByteVector tag(4);
  pbis->Read(&tag);
  pbis->Unread(&tag);
  return Tag::ttcf == GenerateTag(tag[0], tag[1], tag[2], tag[3]);
}

bool FontFactory::IsCollection(ByteArray* ba) {
  return Tag::ttcf ==
         GenerateTag(ba->Get(0), ba->Get(1), ba->Get(2), ba->Get(3));
}

FontFactory::FontFactory()
    : fingerprint_(false) {
}

}  // namespace sfntly
