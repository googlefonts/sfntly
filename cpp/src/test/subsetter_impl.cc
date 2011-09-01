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
// File is originally from Chromium third_party/sfntly/src/subsetter.
// Use as test case in sfntly so that problems can be caught in upstream early.

#include "test/subsetter_impl.h"

#include <string.h>

#include <map>
#include <set>

#include "sfntly/table/core/name_table.h"
#include "sfntly/table/truetype/glyph_table.h"
#include "sfntly/table/truetype/loca_table.h"
#include "sfntly/tag.h"
#include "sfntly/data/memory_byte_array.h"
#include "sfntly/port/memory_input_stream.h"
#include "sfntly/port/memory_output_stream.h"

namespace sfntly {

void ConstructName(UChar* name_part, UnicodeString* name, int32_t name_id) {
  switch(name_id) {
    case NameId::kFullFontName:
      *name = name_part;
      break;
    case NameId::kFontFamilyName:
    case NameId::kPreferredFamily:
    case NameId::kWWSFamilyName: {
      UnicodeString original = *name;
      *name = name_part;
      *name += original;
      break;
    }
    case NameId::kFontSubfamilyName:
    case NameId::kPreferredSubfamily:
    case NameId::kWWSSubfamilyName:
      *name += name_part;
      break;
    default:
      // This name part is not used to construct font name (e.g. copyright).
      // Simply ignore it.
      break;
  }
}

int32_t HashCode(int32_t platform_id, int32_t encoding_id, int32_t language_id,
                 int32_t name_id) {
  int32_t result = platform_id << 24 | encoding_id << 16 | language_id << 8;
  if (name_id == NameId::kFullFontName) {
    result |= 0xff;
  } else if (name_id == NameId::kPreferredFamily ||
             name_id == NameId::kPreferredSubfamily) {
    result |= 0xf;
  } else if (name_id == NameId::kWWSFamilyName ||
             name_id == NameId::kWWSSubfamilyName) {
    result |= 1;
  }
  return result;
}

SubsetterImpl::SubsetterImpl() {
}

SubsetterImpl::~SubsetterImpl() {
}

bool SubsetterImpl::LoadFont(const char* font_name,
                             const unsigned char* original_font,
                             size_t font_size) {
  MemoryInputStream mis;
  mis.Attach(original_font, font_size);
  if (factory_ == NULL) {
    factory_.Attach(FontFactory::GetInstance());
  }

  FontArray font_array;
  factory_->LoadFonts(&mis, &font_array);
  font_ = FindFont(font_name, font_array);
  if (font_ == NULL) {
    return false;
  }

  return true;
}

int SubsetterImpl::SubsetFont(const unsigned int* glyph_ids,
                              size_t glyph_count,
                              unsigned char** output_buffer) {
  if (factory_ == NULL || font_ == NULL) {
    return -1;
  }

  IntegerSet glyph_id_processed;
  if (!ResolveCompositeGlyphs(glyph_ids, glyph_count, &glyph_id_processed) ||
      glyph_id_processed.empty()) {
    return 0;
  }

  FontPtr new_font;
  new_font.Attach(Subset(glyph_id_processed));
  if (new_font == NULL) {
    return 0;
  }

  MemoryOutputStream output_stream;
  factory_->SerializeFont(new_font, &output_stream);
  int length = static_cast<int>(output_stream.Size());
  if (length > 0) {
    *output_buffer = new unsigned char[length];
    memcpy(*output_buffer, output_stream.Get(), length);
  }

  return length;
}

Font* SubsetterImpl::FindFont(const char* font_name,
                              const FontArray& font_array) {
  if (font_array.empty() || font_array[0] == NULL) {
    return NULL;
  }

  if (font_name && strlen(font_name)) {
    for (FontArray::const_iterator b = font_array.begin(), e = font_array.end();
         b != e; ++b) {
      if (HasName(font_name, (*b).p_)) {
        return (*b).p_;
      }
    }
  }

  return font_array[0].p_;
}

bool SubsetterImpl::HasName(const char* font_name, Font* font) {
  UnicodeString font_string = UnicodeString::fromUTF8(font_name);
  if (font_string.isEmpty())
    return false;
  UnicodeString regular_suffix = UnicodeString::fromUTF8(" Regular");
  UnicodeString alt_font_string = font_string;
  alt_font_string += regular_suffix;

  typedef std::map<int32_t, UnicodeString> NameMap;
  NameMap names;
  NameTablePtr name_table = down_cast<NameTable*>(font->GetTable(Tag::name));
  if (name_table == NULL) {
    return false;
  }

  for (int32_t i = 0; i < name_table->NameCount(); ++i) {
    switch(name_table->NameId(i)) {
      case NameId::kFontFamilyName:
      case NameId::kFontSubfamilyName:
      case NameId::kFullFontName:
      case NameId::kPreferredFamily:
      case NameId::kPreferredSubfamily:
      case NameId::kWWSFamilyName:
      case NameId::kWWSSubfamilyName: {
        int32_t hash_code = HashCode(name_table->PlatformId(i),
                                     name_table->EncodingId(i),
                                     name_table->LanguageId(i),
                                     name_table->NameId(i));
        UChar* name_part = name_table->Name(i);
        ConstructName(name_part, &(names[hash_code]), name_table->NameId(i));
        delete[] name_part;
        break;
      }
      default:
        break;
    }
  }

  if (!names.empty()) {
    for (NameMap::iterator b = names.begin(), e = names.end(); b != e; ++b) {
      if (b->second.caseCompare(font_string, 0) == 0 ||
          b->second.caseCompare(alt_font_string, 0) == 0) {
        return true;
      }
    }
  }
  return false;
}

bool SubsetterImpl::ResolveCompositeGlyphs(const unsigned int* glyph_ids,
                                           size_t glyph_count,
                                           IntegerSet* glyph_id_processed) {
  if (glyph_ids == NULL || glyph_count == 0 || glyph_id_processed == NULL) {
    return false;
  }

  // Find glyf and loca table.
  GlyphTablePtr glyph_table =
      down_cast<GlyphTable*>(font_->GetTable(Tag::glyf));
  LocaTablePtr loca_table = down_cast<LocaTable*>(font_->GetTable(Tag::loca));
  if (glyph_table == NULL || loca_table == NULL) {
    // The font is invalid.
    return false;
  }

  // Sort and uniquify glyph ids.
  IntegerSet glyph_id_remaining;
  glyph_id_remaining.insert(0);  // Always include glyph id 0.
  for (size_t i = 0; i < glyph_count; ++i) {
    glyph_id_remaining.insert(glyph_ids[i]);
  }

  // Identify if any given glyph id maps to a composite glyph.  If so, include
  // the glyphs referenced by that composite glyph.
  while (!glyph_id_remaining.empty()) {
    IntegerSet comp_glyph_id;
    for (IntegerSet::iterator i = glyph_id_remaining.begin(),
                              e = glyph_id_remaining.end(); i != e; ++i) {
      if (*i < 0 || *i >= loca_table->num_glyphs()) {
        // Invalid glyph id, ignore.
        continue;
      }

      int32_t length = loca_table->GlyphLength(*i);
      if (length == 0) {
        // Empty glyph, ignore.
        continue;
      }
      int32_t offset = loca_table->GlyphOffset(*i);

      GlyphPtr glyph;
      glyph.Attach(glyph_table->GetGlyph(offset, length));
      if (glyph == NULL) {
        // Error finding glyph, ignore.
        continue;
      }

      if (glyph->GlyphType() == GlyphType::kComposite) {
        Ptr<GlyphTable::CompositeGlyph> comp_glyph =
            down_cast<GlyphTable::CompositeGlyph*>(glyph.p_);
        for (int32_t j = 0; j < comp_glyph->NumGlyphs(); ++j) {
          int32_t glyph_id = comp_glyph->GlyphIndex(j);
          if (glyph_id_processed->find(glyph_id) == glyph_id_processed->end() &&
              glyph_id_remaining.find(glyph_id) == glyph_id_remaining.end()) {
            comp_glyph_id.insert(comp_glyph->GlyphIndex(j));
          }
        }
      }

      glyph_id_processed->insert(*i);
    }

    glyph_id_remaining.clear();
    glyph_id_remaining = comp_glyph_id;
  }

  return true;
}

CALLER_ATTACH Font* SubsetterImpl::Subset(const IntegerSet& glyph_ids) {
  // The tables are already checked in ResolveCompositeGlyphs().
  GlyphTablePtr glyph_table =
      down_cast<GlyphTable*>(font_->GetTable(Tag::glyf));
  LocaTablePtr loca_table = down_cast<LocaTable*>(font_->GetTable(Tag::loca));

  // Setup font builders we need.
  FontBuilderPtr font_builder;
  font_builder.Attach(factory_->NewFontBuilder());

  GlyphTableBuilderPtr glyph_table_builder =
      down_cast<GlyphTable::Builder*>(font_builder->NewTableBuilder(Tag::glyf));
  LocaTableBuilderPtr loca_table_builder =
      down_cast<LocaTable::Builder*>(font_builder->NewTableBuilder(Tag::loca));
  if (glyph_table_builder == NULL || loca_table_builder == NULL) {
    // Out of memory.
    return NULL;
  }

  // Extract glyphs and setup loca list.
  IntegerList loca_list;
  loca_list.resize(loca_table->num_glyphs());
  loca_list.push_back(0);
  int32_t last_glyph_id = 0;
  int32_t last_offset = 0;
  GlyphTable::GlyphBuilderList* glyph_builders =
      glyph_table_builder->GlyphBuilders();
  for (IntegerSet::const_iterator i = glyph_ids.begin(), e = glyph_ids.end();
                                  i != e; ++i) {
    int32_t length = loca_table->GlyphLength(*i);
    int32_t offset = loca_table->GlyphOffset(*i);

    GlyphPtr glyph;
    glyph.Attach(glyph_table->GetGlyph(offset, length));

    // Add glyph to new glyf table.
    ReadableFontDataPtr data = glyph->ReadFontData();
    WritableFontDataPtr copy_data;
    copy_data.Attach(WritableFontData::CreateWritableFontData(data->Length()));
    data->CopyTo(copy_data);
    GlyphBuilderPtr glyph_builder;
    glyph_builder.Attach(glyph_table_builder->GlyphBuilder(copy_data));
    glyph_builders->push_back(glyph_builder);

    // Configure loca list.
    for (int32_t j = last_glyph_id + 1; j <= *i; ++j) {
      loca_list[j] = last_offset;
    }
    last_offset += length;
    loca_list[*i + 1] = last_offset;
    last_glyph_id = *i;
  }
  for (int32_t j = last_glyph_id + 1; j <= loca_table->num_glyphs(); ++j) {
    loca_list[j] = last_offset;
  }
  loca_table_builder->SetLocaList(&loca_list);

  // Setup remaining builders.
  for (TableMap::const_iterator i = font_->GetTableMap()->begin(),
                                e = font_->GetTableMap()->end(); i != e; ++i) {
    // We already build the builder for glyph and loca.
    if (i->first != Tag::glyf && i->first != Tag::loca) {
      font_builder->NewTableBuilder(i->first, i->second->ReadFontData());
    }
  }

  return font_builder->Build();
}

}  // namespace sfntly
