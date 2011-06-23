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

#include "sfntly/tag.h"
#include "sfntly/glyph_table.h"
#include "sfntly/loca_table.h"
#include "sfntly/tools/subsetter/subsetter.h"
#include "sfntly/tools/subsetter/glyph_table_subsetter.h"
#include "sfntly/port/exception_type.h"

namespace sfntly {

const int32_t kGlyphTableSubsetterTags[2] = {Tag::glyf, Tag::loca};

GlyphTableSubsetter::GlyphTableSubsetter()
    : TableSubsetterImpl(kGlyphTableSubsetterTags, 2) {
}

GlyphTableSubsetter::~GlyphTableSubsetter() {}

bool GlyphTableSubsetter::subset(Subsetter* subsetter, Font* font,
                                 Font::Builder* font_builder) {
  assert(font);
  assert(subsetter);
  assert(font_builder);

  IntegerList* permutation_table = subsetter->glyphPermutationTable();
  if (!permutation_table || permutation_table->empty())
    return false;

  GlyphTablePtr glyph_table = down_cast<GlyphTable*>(font->table(Tag::glyf));
  LocaTablePtr loca_table = down_cast<LocaTable*>(font->table(Tag::loca));
  if (glyph_table == NULL || loca_table == NULL) {
    throw RuntimeException("Font to subset is not valid.");
  }

  GlyphTableBuilderPtr glyph_table_builder;
  glyph_table_builder.attach(down_cast<GlyphTable::Builder*>(
       font_builder->newTableBuilder(Tag::glyf)));
  LocaTableBuilderPtr loca_table_builder;
  loca_table_builder.attach(down_cast<LocaTable::Builder*>(
       font_builder->newTableBuilder(Tag::loca)));
  if (glyph_table_builder == NULL || loca_table_builder == NULL) {
    throw RuntimeException("Builder for subset is not valid.");
  }
  GlyphTable::GlyphBuilderList* glyph_builders =
      glyph_table_builder->glyphBuilders();
  for (IntegerList::iterator old_glyph_id = permutation_table->begin(),
                             old_glyph_id_end = permutation_table->end();
                             old_glyph_id != old_glyph_id_end; ++old_glyph_id) {
    int old_offset = loca_table->glyphOffset(*old_glyph_id);
    int old_length = loca_table->glyphLength(*old_glyph_id);
    GlyphPtr glyph;
    glyph.attach(glyph_table->glyph(old_offset, old_length));
    ReadableFontDataPtr data = glyph->readFontData();
    WritableFontDataPtr copy_data;
    copy_data.attach(font_builder->getNewData(data->length()));
    data->copyTo(copy_data);
    GlyphBuilderPtr glyph_builder;
    glyph_builder.attach(glyph_table_builder->glyphBuilder(copy_data));
    glyph_builders->push_back(glyph_builder);
  }
  IntegerList loca_list;
  glyph_table_builder->generateLocaList(&loca_list);
  loca_table_builder->setLocaList(&loca_list);
  return true;
}

}  // namespace sfntly
