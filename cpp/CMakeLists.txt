########################################################################
# CMake build script for Google Typography sfntly.
cmake_minimum_required(VERSION 2.6.2)
set(CMAKE_LEGACY_CYGWIN_WIN32 0)
project(sfntly)
# Uncomment the line below to simulate Chrome compilation flags
#  add_definitions(-DSFNTLY_NO_EXCEPTION -Wall -Werror -fno-exceptions)
  set(LIBRARY_OUTPUT_PATH ${CMAKE_CURRENT_BINARY_DIR}/lib)
  set(EXECUTABLE_OUTPUT_PATH ${CMAKE_CURRENT_BINARY_DIR}/bin)
  include_directories(${CMAKE_CURRENT_SOURCE_DIR}/src ext/gtest/include ext/gtest)
  add_library(sfntly 
    src/sfntly/cmap_table.cc
    src/sfntly/data/byte_array.cc
    src/sfntly/data/font_data.cc
    src/sfntly/data/font_input_stream.cc
    src/sfntly/data/font_output_stream.cc
    src/sfntly/data/growable_memory_byte_array.cc
    src/sfntly/data/memory_byte_array.cc
    src/sfntly/data/readable_font_data.cc
    src/sfntly/data/writable_font_data.cc
    src/sfntly/font.cc
    src/sfntly/font_data_table.cc
    src/sfntly/font_factory.cc
    src/sfntly/font_header_table.cc
    src/sfntly/glyph_table.cc
    src/sfntly/horizontal_header_table.cc
    src/sfntly/horizontal_metrics_table.cc
    src/sfntly/loca_table.cc
    src/sfntly/maximum_profile_table.cc
    src/sfntly/name_table.cc
    src/sfntly/os2_table.cc
    src/sfntly/port/memory_output_stream.cc
    src/sfntly/subtable.cc
    src/sfntly/table.cc
    src/sfntly/tag.cc
    src/sfntly/tools/subsetter.cc
    src/sfntly/tools/subsetter/glyph_table_subsetter.cc
    src/sfntly/tools/subsetter/table_subsetter_impl.cc)
  add_executable(subsetter
    src/sample/subsetter/main.cc
    src/sample/subsetter/subset_util.cc)
  target_link_libraries(subsetter sfntly)
  add_executable(unit_test
    src/test/byte_array_test.cc
    src/test/endian_test.cc
    src/test/font_data_test.cc
    src/test/main.cc
    src/test/open_type_data_test.cc
    src/test/otf_basic_editing_test.cc
    src/test/smart_pointer_test.cc
    ext/gtest/src/gtest-all.cc
    ext/gtest/src/gtest_main.cc)
  target_link_libraries(unit_test sfntly pthread)
  