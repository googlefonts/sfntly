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

#include "sfntly/font_data_table.h"
#include "sfntly/data/font_output_stream.h"

namespace sfntly {

/******************************************************************************
 * FontDataTable class
 ******************************************************************************/

FontDataTable::FontDataTable(ReadableFontData* data) {
  data_ = data;
}

FontDataTable::~FontDataTable() {}

ReadableFontData* FontDataTable::readFontData() {
  return data_;
}

int32_t FontDataTable::length() {
  return data_->length();
}

int32_t FontDataTable::padding() {
  return -1;
}

int32_t FontDataTable::dataLength() {
  int32_t paddings = padding();
  return (paddings == -1) ? length() : length() - paddings;
}

int32_t FontDataTable::serialize(OutputStream* os) {
  return data_->copyTo(os);
}

/******************************************************************************
 * FontDataTable::Builder class
 ******************************************************************************/
void FontDataTable::Builder::init(FontDataTableBuilderContainer* container) {
  container_ = container;
  model_changed_ = false;
  data_changed_ = false;
}

FontDataTable::Builder::Builder(FontDataTableBuilderContainer* container) {
  init(container);
}

FontDataTable::Builder::Builder(FontDataTableBuilderContainer* container,
                                WritableFontData* data) {
  init(container);
  w_data_ = data;
}

FontDataTable::Builder::Builder(FontDataTableBuilderContainer* container,
                                ReadableFontData* data) {
  init(container);
  r_data_ = data;
}

FontDataTable::Builder::~Builder() {
}

WritableFontData* FontDataTable::Builder::data() {
  WritableFontDataPtr new_data;
  if (model_changed_) {
    if (!subReadyToSerialize()) {
      return NULL;
    }
    int32_t size = subDataSizeToSerialize();
    new_data.attach(container_->getNewData(size));
    subSerialize(new_data);
  } else {
    ReadableFontDataPtr data = internalReadData();
    new_data.attach(container_->getNewData(data != NULL ? data->length() : 0));
    data->copyTo(new_data);
  }
  return new_data.detach();
}

void FontDataTable::Builder::setData(ReadableFontData* data) {
  internalSetData(data, true);
}

void FontDataTable::Builder::internalSetData(WritableFontData* data,
                                             bool data_changed) {
  w_data_ = data;
  r_data_ = NULL;
  if (data_changed) {
    data_changed_ = true;
    subDataSet();
  }
}

void FontDataTable::Builder::internalSetData(ReadableFontData* data,
                                             bool data_changed) {
  w_data_ = NULL;
  r_data_ = data;
  if (data_changed) {
    data_changed_ = true;
    subDataSet();
  }
}

CALLER_ATTACH FontDataTable* FontDataTable::Builder::build() {
  ReadableFontDataPtr data = internalReadData();
  if (model_changed_) {
    // Let subclass serialize from model.
    if (!subReadyToSerialize()) {
      return NULL;
    }
    int32_t size = subDataSizeToSerialize();
    WritableFontDataPtr new_data;
    new_data.attach(container_->getNewData(size));
    subSerialize(new_data);
    data = new_data;
  }
  FontDataTablePtr table = subBuildTable(data);
  notifyPostTableBuild(table);
  return table;
}

bool FontDataTable::Builder::readyToBuild() {
  return true;
}

ReadableFontData* FontDataTable::Builder::internalReadData() {
  return (r_data_ != NULL) ? r_data_.p_ :
                             static_cast<ReadableFontData*>(w_data_.p_);
}

WritableFontData* FontDataTable::Builder::internalWriteData() {
  if (w_data_ == NULL) {
    WritableFontDataPtr new_data;
    new_data.attach(container_->getNewData(r_data_->length()));
    r_data_->copyTo(new_data);
    internalSetData(new_data, false);
  }
  return w_data_.p_;
}

WritableFontData* FontDataTable::Builder::internalNewData(int32_t size) {
  return container_->getNewData(size);
}

bool FontDataTable::Builder::dataChanged() {
  return data_changed_;
}

bool FontDataTable::Builder::modelChanged() {
  return model_changed_;
}

bool FontDataTable::Builder::setModelChanged() {
  return setModelChanged(true);
}

bool FontDataTable::Builder::setModelChanged(bool changed) {
  bool old = model_changed_;
  model_changed_ = changed;
  return old;
}

void FontDataTable::Builder::notifyPostTableBuild(FontDataTable* table) {
  // default: NOP
  UNREFERENCED_PARAMETER(table);
}

}  // namespace sfntly
