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

#include <string.h>

#include "sfntly/font_factory.h"
#include "sfntly/tag.h"
#include "sfntly/data/memory_byte_array.h"
#include "sfntly/data/growable_memory_byte_array.h"

namespace sfntly {

const int32_t FontFactory::Offset::kTTCTag = 0;
const int32_t FontFactory::Offset::kVersion = 4;
const int32_t FontFactory::Offset::kNumFonts = 8;
const int32_t FontFactory::Offset::kOffsetTable = 12;
const int32_t FontFactory::Offset::kulDsigTag = 0;
const int32_t FontFactory::Offset::kulDsigLength = 4;
const int32_t FontFactory::Offset::kulDsigOffset = 8;

FontFactory::FontFactory() : fingerprint_(false) {
}

FontFactory::~FontFactory() {
}

CALLER_ATTACH FontFactory* FontFactory::getInstance() {
  FontFactoryPtr instance = new FontFactory();
  return instance.detach();
}

void FontFactory::fingerprintFont(bool fingerprint) {
  fingerprint_ = fingerprint;
}

bool FontFactory::fingerprintFont() {
  return fingerprint_;
}

void FontFactory::loadFonts(InputStream* is, FontArray* output) {
  assert(output);
  PushbackInputStream* pbis = down_cast<PushbackInputStream*>(is);
  if (isCollection(pbis)) {
    loadCollection(pbis, output);
    return;
  }
  FontPtr font = loadSingleOTF(pbis);
  if (font) {
    output->push_back(font);
  }
}

void FontFactory::loadFontsForBuilding(InputStream* is,
                                       FontBuilderArray* output) {
  PushbackInputStream* pbis = down_cast<PushbackInputStream*>(is);
  if (isCollection(pbis)) {
    loadCollectionForBuilding(pbis, output);
    return;
  }
  FontBuilderPtr builder;
  builder.attach(loadSingleOTFForBuilding(pbis));
  if (builder) {
    output->push_back(builder);
  }
}

Font* FontFactory::loadSingleOTF(InputStream* is) {
  FontBuilderPtr builder;
  builder.attach(loadSingleOTFForBuilding(is));
  return builder->build();
}

void FontFactory::loadCollection(InputStream* is, FontArray* output) {
  FontBuilderArray ba;
  loadCollectionForBuilding(is, &ba);
  output->reserve(ba.size());
  for (FontBuilderArray::iterator builder = ba.begin(), builders_end = ba.end();
                                  builder != builders_end; ++builder) {
      FontPtr font;
      font.attach((*builder)->build());
      output->push_back(font);
  }
}

Font::Builder* FontFactory::loadSingleOTFForBuilding(InputStream* is) {
  // UNIMPLEMENTED: SHA-1 hash checking via Java DigestStream
  Font::Builder* builder = Font::Builder::getOTFBuilder(this, is);
  // UNIMPLEMENTED: setDigest
  return builder;
}

void FontFactory::loadCollectionForBuilding(InputStream* is,
                                            FontBuilderArray* builders) {
  ByteArrayPtr ba = new GrowableMemoryByteArray();
  ba->copyFrom(is);
  loadCollectionForBuilding(ba, builders);
}

bool FontFactory::isCollection(PushbackInputStream* pbis) {
  ByteVector tag(4);
  pbis->read(&tag);
  pbis->unread(&tag);
  return Tag::ttcf == generate_tag(tag[0], tag[1], tag[2], tag[3]);
}

void FontFactory::loadFonts(ByteArray* ba, FontArray* output) {
  if (isCollection(ba)) {
    loadCollection(ba, output);
    return;
  }
  FontPtr font;
  font.attach(loadSingleOTF(ba));
  if (font) {
    output->push_back(font);
  }
}

void FontFactory::loadFontsForBuilding(ByteArray* ba,
                                       FontBuilderArray* output) {
  if (isCollection(ba)) {
    loadCollectionForBuilding(ba, output);
    return;
  }
  FontBuilderPtr builder;
  builder.attach(loadSingleOTFForBuilding(ba, 0));
  if (builder) {
    output->push_back(builder);
  }
}

Font* FontFactory::loadSingleOTF(ByteArray* ba) {
  FontBuilderPtr builder;
  builder.attach(loadSingleOTFForBuilding(ba, 0));
  return builder->build();
}

void FontFactory::loadCollection(ByteArray* ba, FontArray* output) {
  FontBuilderArray builders;
  loadCollectionForBuilding(ba, &builders);
  output->reserve(ba->size());
  for (FontBuilderArray::iterator
       builder = builders.begin(), builders_end = builders.end();
       builder != builders_end; ++builder) {
    FontPtr font;
    font.attach((*builder)->build());
    output->push_back(font);
  }
}

Font::Builder* FontFactory::loadSingleOTFForBuilding(ByteArray* ba,
    int32_t offset_to_offset_table) {
  // UNIMPLEMENTED: SHA-1 hash checking via Java DigestStream
  Font::Builder* builder =
      Font::Builder::getOTFBuilder(this, ba, offset_to_offset_table);
  // UNIMPLEMENTED: setDigest
  return builder;
}

void FontFactory::loadCollectionForBuilding(ByteArray* ba,
                                            FontBuilderArray* builders) {
  ReadableFontDataPtr rfd = new ReadableFontData(ba);
  int32_t ttc_tag = rfd->readULongAsInt(Offset::kTTCTag);
  UNREFERENCED_PARAMETER(ttc_tag);
  int32_t version = rfd->readFixed(Offset::kVersion);
  UNREFERENCED_PARAMETER(version);
  int32_t num_fonts = rfd->readULongAsInt(Offset::kNumFonts);

  builders->reserve(num_fonts);
  int32_t offset_table_offset = Offset::kOffsetTable;
  for (int32_t font_number = 0; font_number < num_fonts;
       font_number++, offset_table_offset += DataSize::kULONG) {
    int32_t offset = rfd->readULongAsInt(offset_table_offset);
    FontBuilderPtr builder;
    builder.attach(loadSingleOTFForBuilding(ba, offset));
    builders->push_back(builder);
  }
}

bool FontFactory::isCollection(ByteArray* ba) {
  return Tag::ttcf ==
         generate_tag(ba->get(0), ba->get(1), ba->get(2), ba->get(3));
}

CALLER_ATTACH WritableFontData* FontFactory::getNewData(int32_t capacity) {
  // UNIMPLMENTED: if (capacity > 0) { this.getNewFixedData(capacity); }
  // seems a no-op
  return getNewGrowableData(capacity);
}

CALLER_ATTACH WritableFontData* FontFactory::getNewFixedData(int32_t capacity) {
  ByteArrayPtr buffer;
  buffer.attach(getNewArray(capacity));
  WritableFontDataPtr new_fixed_data = new WritableFontData(buffer);
  return new_fixed_data.detach();
}

CALLER_ATTACH WritableFontData* FontFactory::getNewGrowableData(
    int32_t capacity) {
  ByteArrayPtr buffer;
  buffer.attach(getNewGrowableArray(capacity));
  WritableFontDataPtr new_growable_data = new WritableFontData(buffer);
  return new_growable_data.detach();
}

CALLER_ATTACH WritableFontData* FontFactory::getNewGrowableData(
    ReadableFontData* src_data) {
  WritableFontDataPtr data;
  data.attach(getNewGrowableData(src_data->length()));
  src_data->copyTo(data);
  return data.detach();
}

CALLER_ATTACH ByteArray* FontFactory::getNewArray(int32_t length) {
  ByteArrayPtr new_fixed_array = new MemoryByteArray(length);
  return new_fixed_array.detach();
}

CALLER_ATTACH ByteArray* FontFactory::getNewGrowableArray(int32_t length) {
  UNREFERENCED_PARAMETER(length);
  ByteArrayPtr new_growable_array = new GrowableMemoryByteArray();
  return new_growable_array.detach();
}

void FontFactory::serializeFont(Font* font, OutputStream* os) {
  font->serialize(os, &table_ordering_);
}

void FontFactory::setSerializationTableOrdering(
    const IntegerList& table_ordering) {
  table_ordering_ = table_ordering;
}

CALLER_ATTACH Font::Builder* FontFactory::newFontBuilder() {
  return Font::Builder::getOTFBuilder(this);
}

}  // namespace sfntly
