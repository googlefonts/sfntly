#!/usr/bin/env python
# Copyright 2011 Google Inc. All Rights Reserved.

# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at

# http://www.apache.org/licenses/LICENSE-2.0

# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limit

"""Script to generate C++ files needed for Google Test from XML font data."""

import os
import argparse
from argparse import ArgumentParser
from cmap_data_generator_cc import CMapDataGenerator
from test_data_generator_cc import TestDataGenerator
from utils import FixPath
from utils import GetFontList


def main():
  parser = ArgumentParser(description="""Generates CMap table test data
from XML description files.""",
                          formatter_class=
                          argparse.ArgumentDefaultsHelpFormatter)
  parser.add_argument('--destination',
                      default='.',
                      help='ouput folder for the generated files')
  parser.add_argument('--source',
                      default='../data/fonts',
                      help='input folder for the XML files')
  parser.add_argument('--name',
                      default='cmap_test_data',
                      help='base name of the generated test files')
  parser.add_argument('--fonts',
                      default='',
                      help="""space separated list of XML files to use as
source data""")
  parser.add_argument('--font_dir',
                      default='.',
                      help="""base directory of the fonts in the generated
source""")
  parser.add_argument('--num_cmaps',
                      type=int,
                      default=2,
                      help="""maximum number of cmaps for each font;
this is a constant because values might differ across XML files but we need a
fixed size array in C++""")
  parser.add_argument('--num_mappings',
                      default=10,
                      type=int,
                      help="""maximum number of mappings for each fonts;
this is a fixed constant because values might differ across XML but we need a
fixed size array in C++""")
  args = parser.parse_args()

  try:
    os.stat(args.destination)
  except OSError:
    os.mkdir(args.destination)

  args.destination = FixPath(args.destination)
  args.source = FixPath(args.source)
  args.font_dir = FixPath(args.font_dir)
  if args.font_dir == '.':
    args.font_dir = ''

  if not args.fonts:
    args.fonts = [path
                  for path in
                  GetFontList(args.source, ['.ttf.xml', '.ttc.xml', '.otf.xml'])
                  if path.find('src') == -1 and path.find('archive') == -1]

  elif type(args.fonts) != 'list':
    args.fonts = [args.fonts]

  data_generator = TestDataGenerator(args.destination + args.name,
                                     args.font_dir,
                                     args.fonts,
                                     CMapDataGenerator(args.num_cmaps,
                                                       args.num_mappings,
                                                       [0, 4]))
  data_generator.Generate()

if __name__ == '__main__':
  main()
