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

#include "sfntly/maximum_profile_table.h"

namespace sfntly {
/******************************************************************************
 * MaximumProfileTable class
 ******************************************************************************/
MaximumProfileTable::MaximumProfileTable(Header* header,
                                         ReadableFontData* data) :
    Table(header, data) {
}

MaximumProfileTable::~MaximumProfileTable() {}

int32_t MaximumProfileTable::version() {
  return data_->readFixed(Offset::kVersion);
}

int32_t MaximumProfileTable::numGlyphs() {
  return data_->readUShort(Offset::kNumGlyphs);
}

int32_t MaximumProfileTable::maxPoints() {
  return data_->readUShort(Offset::kMaxPoints);
}

int32_t MaximumProfileTable::maxContours() {
  return data_->readUShort(Offset::kMaxContours);
}

int32_t MaximumProfileTable::maxCompositePoints() {
  return data_->readUShort(Offset::kMaxCompositePoints);
}

int32_t MaximumProfileTable::maxZones() {
  return data_->readUShort(Offset::kMaxZones);
}

int32_t MaximumProfileTable::maxTwilightPoints() {
  return data_->readUShort(Offset::kMaxTwilightPoints);
}

int32_t MaximumProfileTable::maxStorage() {
  return data_->readUShort(Offset::kMaxStorage);
}

int32_t MaximumProfileTable::maxFunctionDefs() {
  return data_->readUShort(Offset::kMaxFunctionDefs);
}

int32_t MaximumProfileTable::maxStackElements() {
  return data_->readUShort(Offset::kMaxStackElements);
}

int32_t MaximumProfileTable::maxSizeOfInstructions() {
  return data_->readUShort(Offset::kMaxSizeOfInstructions);
}

int32_t MaximumProfileTable::maxComponentElements() {
  return data_->readUShort(Offset::kMaxComponentElements);
}

int32_t MaximumProfileTable::maxComponentDepth() {
  return data_->readUShort(Offset::kMaxComponentDepth);
}

/******************************************************************************
 * MaximumProfileTable::Builder class
 ******************************************************************************/
MaximumProfileTable::Builder::Builder(
    FontDataTableBuilderContainer* font_builder, Header* header,
    WritableFontData* data) :
    Table::TableBasedTableBuilder(font_builder, header, data) {
}

MaximumProfileTable::Builder::Builder(
    FontDataTableBuilderContainer* font_builder, Header* header,
    ReadableFontData* data) :
    Table::TableBasedTableBuilder(font_builder, header, data) {
}

MaximumProfileTable::Builder::~Builder() {}

CALLER_ATTACH FontDataTable* MaximumProfileTable::Builder::subBuildTable(
    ReadableFontData* data) {
  FontDataTablePtr table = new MaximumProfileTable(header(), data);
  return table.detach();
}

int32_t MaximumProfileTable::Builder::version() {
  return internalReadData()->readUShort(Offset::kVersion);
}

void MaximumProfileTable::Builder::setVersion(int32_t version) {
  internalWriteData()->writeUShort(Offset::kVersion, version);
}

int32_t MaximumProfileTable::Builder::numGlyphs() {
  return internalReadData()->readUShort(Offset::kNumGlyphs);
}

void MaximumProfileTable::Builder::setNumGlyphs(int32_t num_glyphs) {
  internalWriteData()->writeUShort(Offset::kNumGlyphs, num_glyphs);
}

int32_t MaximumProfileTable::Builder::maxPoints() {
  return internalReadData()->readUShort(Offset::kMaxPoints);
}

void MaximumProfileTable::Builder::setMaxPoints(int32_t max_points) {
  internalWriteData()->writeUShort(Offset::kMaxPoints, max_points);
}

int32_t MaximumProfileTable::Builder::maxContours() {
  return internalReadData()->readUShort(Offset::kMaxContours);
}

void MaximumProfileTable::Builder::setMaxContours(int32_t max_contours) {
  internalWriteData()->writeUShort(Offset::kMaxContours, max_contours);
}

int32_t MaximumProfileTable::Builder::maxCompositePoints() {
  return internalReadData()->readUShort(Offset::kMaxCompositePoints);
}

void MaximumProfileTable::Builder::setMaxCompositePoints(
    int32_t max_composite_points) {
  internalWriteData()->writeUShort(Offset::kMaxCompositePoints,
                                   max_composite_points);
}

int32_t MaximumProfileTable::Builder::maxZones() {
  return internalReadData()->readUShort(Offset::kMaxZones);
}

void MaximumProfileTable::Builder::setMaxZones(int32_t max_zones) {
  internalWriteData()->writeUShort(Offset::kMaxZones, max_zones);
}

int32_t MaximumProfileTable::Builder::maxTwilightPoints() {
  return internalReadData()->readUShort(Offset::kMaxTwilightPoints);
}

void MaximumProfileTable::Builder::setMaxTwilightPoints(
    int32_t max_twilight_points) {
  internalWriteData()->writeUShort(Offset::kMaxTwilightPoints,
                                   max_twilight_points);
}

int32_t MaximumProfileTable::Builder::maxStorage() {
  return internalReadData()->readUShort(Offset::kMaxStorage);
}

void MaximumProfileTable::Builder::setMaxStorage(int32_t max_storage) {
  internalWriteData()->writeUShort(Offset::kMaxStorage, max_storage);
}

int32_t MaximumProfileTable::Builder::maxFunctionDefs() {
  return internalReadData()->readUShort(Offset::kMaxFunctionDefs);
}

void MaximumProfileTable::Builder::setMaxFunctionDefs(
    int32_t max_function_defs) {
  internalWriteData()->writeUShort(Offset::kMaxFunctionDefs, max_function_defs);
}

int32_t MaximumProfileTable::Builder::maxStackElements() {
  return internalReadData()->readUShort(Offset::kMaxStackElements);
}

void MaximumProfileTable::Builder::setMaxStackElements(
    int32_t max_stack_elements) {
  internalWriteData()->writeUShort(Offset::kMaxStackElements,
                                   max_stack_elements);
}

int32_t MaximumProfileTable::Builder::maxSizeOfInstructions() {
  return internalReadData()->readUShort(Offset::kMaxSizeOfInstructions);
}

void MaximumProfileTable::Builder::setMaxSizeOfInstructions(
    int32_t max_size_of_instructions) {
  internalWriteData()->writeUShort(Offset::kMaxSizeOfInstructions,
                                   max_size_of_instructions);
}

int32_t MaximumProfileTable::Builder::maxComponentElements() {
  return internalReadData()->readUShort(Offset::kMaxComponentElements);
}

void MaximumProfileTable::Builder::setMaxComponentElements(
    int32_t max_component_elements) {
  internalWriteData()->writeUShort(Offset::kMaxComponentElements,
                                   max_component_elements);
}

int32_t MaximumProfileTable::Builder::maxComponentDepth() {
  return internalReadData()->readUShort(Offset::kMaxComponentDepth);
}

void MaximumProfileTable::Builder::setMaxComponentDepth(
    int32_t max_component_depth) {
  internalWriteData()->writeUShort(Offset::kMaxComponentDepth,
                                   max_component_depth);
}

}  // namespace sfntly
