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

#ifndef TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_FONT_DATA_TABLE_H_
#define TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_FONT_DATA_TABLE_H_

#include "sfntly/data/readable_font_data.h"
#include "sfntly/font_data_table_builder_container.h"
#include "sfntly/port/refcount.h"

namespace sfntly {

class FontDataTable : virtual public RefCount {
 public:
  explicit FontDataTable(ReadableFontData* data);
  virtual ~FontDataTable();

  // Get the readable font data for this table.
  ReadableFontData* readFontData();

  // Get the length of the data for this table in bytes. This is the full
  // allocated length of the data and may or may not include any padding.
  virtual int32_t length();

  // Get the number of bytes of padding used in the table. The padding bytes are
  // used to align the table length to a 4 byte boundary.
  virtual int32_t padding();

  // Return the number of bytes of non-padded data in the table. If the padding
  // is unknown or unknowable then the total number of bytes of data in the
  // tables is returned.
  virtual int32_t dataLength();

  virtual int32_t serialize(OutputStream* os);

 public:
  // Note: original version is abstract Builder<T extends FontDataTable>
  //       C++ template is not designed that way so plain class is chosen.
  class Builder : virtual public RefCount {
   protected:
    explicit Builder(FontDataTableBuilderContainer* container);
    Builder(FontDataTableBuilderContainer* container, WritableFontData* data);
    Builder(FontDataTableBuilderContainer* container, ReadableFontData* data);
    virtual ~Builder();

    void init(FontDataTableBuilderContainer* container);

   public:
    // Get a snapshot copy of the internal data of the builder.
    // This causes any internal data structures to be serialized to a new data
    // object. This data object belongs to the caller and must be properly
    // disposed of. No changes are made to the builder and any changes to the
    // data directly do not affect the internal state. To do that a subsequent
    // call must be made to {@link #setData(WritableFontData)}.
    // @return a copy of the internal data of the builder
    WritableFontData* data();
    virtual void setData(ReadableFontData* data);

   private:
    void internalSetData(WritableFontData* data, bool data_changed);
    void internalSetData(ReadableFontData* data, bool data_changed);

   public:  // Note: changed from protected to avoid accessibility error in C++
    virtual FontDataTable* build();
    virtual bool readyToBuild();
    virtual ReadableFontData* internalReadData();
    virtual WritableFontData* internalWriteData();
    virtual WritableFontData* internalNewData(int32_t size);
    virtual bool dataChanged();
    virtual bool modelChanged();
    virtual bool setModelChanged();
    virtual bool setModelChanged(bool changed);

   protected:  // subclass API
    virtual void notifyPostTableBuild(FontDataTable* table);
    virtual int32_t subSerialize(WritableFontData* new_data) = 0;
    virtual bool subReadyToSerialize() = 0;
    virtual int32_t subDataSizeToSerialize() = 0;
    virtual void subDataSet() = 0;
    virtual CALLER_ATTACH FontDataTable*
        subBuildTable(ReadableFontData* data) = 0;

   private:
    FontDataTableBuilderContainer* container_;  // avoid circular ref-counting
    WritableFontDataPtr w_data_;
    ReadableFontDataPtr r_data_;
    bool model_changed_;
    bool data_changed_;
  };

 protected:
  ReadableFontDataPtr data_;
};
typedef Ptr<FontDataTable> FontDataTablePtr;

}  // namespace sfntly

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_FONT_DATA_TABLE_H_
