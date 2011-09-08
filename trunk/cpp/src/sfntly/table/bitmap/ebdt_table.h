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

#ifndef SFNTLY_CPP_SRC_SFNTLY_TABLE_BITMAP_EBDT_TABLE_H_
#define SFNTLY_CPP_SRC_SFNTLY_TABLE_BITMAP_EBDT_TABLE_H_

#include "sfntly/table/bitmap/bitmap_glyph.h"
#include "sfntly/table/subtable_container_table.h"

namespace sfntly {

class EbdtTable : public SubTableContainerTable,
                  public RefCounted<EbdtTable> {
 public:
  class Builder : public SubTableContainerTable::Builder,
                  public RefCounted<Builder> {
   public:
    // Constructor scope altered to public because C++ does not allow base
    // class to instantiate derived class with protected constructors.
    Builder(Header* header, WritableFontData* data);
    Builder(Header* header, ReadableFontData* data);
    virtual ~Builder();

    virtual int32_t SubSerialize(WritableFontData* new_data);
    virtual bool SubReadyToSerialize();
    virtual int32_t SubDataSizeToSerialize();
    virtual void SubDataSet();
    virtual CALLER_ATTACH FontDataTable* SubBuildTable(ReadableFontData* data);

    static CALLER_ATTACH Builder* CreateBuilder(Header* header,
                                                WritableFontData* data);
  };

  virtual ~EbdtTable();
  int32_t Version();
  CALLER_ATTACH BitmapGlyph* Glyph(int32_t offset,
                                   int32_t length,
                                   int32_t format);

 protected:
  EbdtTable(Header* header, ReadableFontData* data);

 private:
  struct Offset {
    enum {
      kVersion = 0,
    };
  };
};
typedef Ptr<EbdtTable> EbdtTablePtr;

}  // namespace sfntly

#endif  // SFNTLY_CPP_SRC_SFNTLY_TABLE_BITMAP_EBDT_TABLE_H_
