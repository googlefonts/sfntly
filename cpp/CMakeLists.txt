########################################################################
# CMake build script for Google Typography sfntly.
cmake_minimum_required(VERSION 2.6.2)
set(CMAKE_CONFIGURATION_TYPES "Debug;Release" CACHE STRING "good configs" FORCE)
set(CMAKE_LEGACY_CYGWIN_WIN32 0)

project(sfntly)

# For gcc, make the default be debug build and valgrind friendly.
if(CMAKE_COMPILER_IS_GNUCXX)
  if(NOT CMAKE_BUILD_TYPE)
    set(CMAKE_BUILD_TYPE Debug)
    add_definitions("-DDEBUG -D_DEBUG -g -fno-inline -fno-omit-frame-pointer -fno-builtin")
  endif(NOT CMAKE_BUILD_TYPE)
endif(CMAKE_COMPILER_IS_GNUCXX)

  set(LIBRARY_OUTPUT_PATH ${CMAKE_CURRENT_BINARY_DIR}/lib)
  set(EXECUTABLE_OUTPUT_PATH ${CMAKE_CURRENT_BINARY_DIR}/bin)
  include_directories(${CMAKE_CURRENT_SOURCE_DIR}/src ext/gtest/include ext/gtest)

# The following simulates Chrome compilation flags
  add_definitions(-DSFNTLY_NO_EXCEPTION)
if(CMAKE_COMPILER_IS_GNUCXX)
  add_definitions(-D__wur=__attribute__\(\(warn_unused_result\)\) -Wall -Werror -fno-exceptions)
endif(CMAKE_COMPILER_IS_GNUCXX)

# VC specific flags
if(MSVC10 OR MSVC90)
  set (CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} /Zi /nologo /W4 /WX /O2 /Ob2 /Oy /GF /Gm- /MT /GS /Gy /fp:precise /Zc:wchar_t /Zc:forScope /GR-") 
  set (CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} /Zi /nologo /W4 /WX /Od /Oy- /RTC1 /MTd /GS /Gy /fp:precise /Zc:wchar_t /Zc:forScope /GR-")
  add_definitions(-D_UNICODE -DUNICODE)
  include_directories(${CMAKE_CURRENT_SOURCE_DIR}/ext/icu/include)
  link_directories(${CMAKE_CURRENT_SOURCE_DIR}/ext/icu/lib)
endif(MSVC10 OR MSVC90)

  file(GLOB_RECURSE SFNTLY_FILES src/sfntly/*.h src/sfntly/*.cc)
  add_library(sfntly ${SFNTLY_FILES})
  file(GLOB_RECURSE SIMPLE_SUBSETTER src/sample/subsetter/*.h src/sample/subsetter/*.cc)
  add_executable(subsetter ${SIMPLE_SUBSETTER})
  target_link_libraries(subsetter sfntly icuuc)
  file(GLOB TEST_CASES src/test/*.cc)
  add_executable(unit_test
    ${TEST_CASES}
    ext/gtest/src/gtest-all.cc
    ext/gtest/src/gtest_main.cc)
  target_link_libraries(unit_test sfntly icuuc)
if(CMAKE_COMPILER_IS_GNUCXX)
  target_link_libraries(unit_test pthread)
endif(CMAKE_COMPILER_IS_GNUCXX)
  