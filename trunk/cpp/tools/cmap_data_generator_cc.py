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

"""Generates C++ and H files for the CMap table from the XML descriptions."""

from string import join
from string import Template
import xml.dom.minidom as minidom
from table_data_generator_cc import TableDataGenerator


class CMapDataGenerator(TableDataGenerator):
  """Generate CMap table data from an XML.

  Can change the number of CMaps in the final structure,
  the number of mappings copied and the supported CMap formats
  """
  test_cmap_struct = Template("""struct ProtoCMap {
  int32_t length;
  int32_t format;
  int32_t platform_id;
  int32_t encoding_id;
  int32_t chars[$num_mappings];
  int32_t glyph_ids[$num_mappings];
};
""")
  test_struct = Template("""struct $test_struct_name {
  const char* name;
  ProtoCMap cmaps[$num_cmaps];
};
""")

  def __init__(self, num_cmaps, num_mappings, supported_formats):
    super(CMapDataGenerator, self).__init__('TestCMap')
    self.num_cmaps = num_cmaps
    self.num_mappings = num_mappings
    self.supported_formats = supported_formats

  def TestStructs(self):
    return [self.test_cmap_struct.substitute(num_mappings=self.num_mappings),
            self.test_struct.substitute(test_struct_name=
                                        self.TestStructName(),
                                        num_cmaps=self.num_cmaps)]

  def Generate(self, prefix_dir, font_xml):
    """Generates the intialization for a CMap aggregate struct."""
    if prefix_dir == './':
      prefix_dir = ''
    doc = minidom.parseString(open(font_xml, 'r').read())
    table = doc.getElementsByTagName('cmap_table')[0]
    cmap_infos = []
    for cmap in table.getElementsByTagName('cmap')[:self.num_cmaps]:
      cmap_format = int(cmap.getAttribute('format'))
      if cmap_format not in self.supported_formats:
        print '  Unsupported CMapFormat ', cmap_format, '. Skipping.'
        continue
      cmap_infos.append(self.CMapInfoToString(self.GetCMapInfo(cmap)))
    path = (prefix_dir +
            doc.getElementsByTagName('font_test_data')[0].getAttribute('path'))
    return ('{\n"' + path +
            '",\n{\n' + join(cmap_infos, ',\n') + '\n}\n}')

  def GetCMapInfo(self, cmap):

    length = int(cmap.getAttribute('byte_length'))
    cmap_format = int(cmap.getAttribute('format'))
    platform_id = int(cmap.getAttribute('platform_id'))
    encoding_id = int(cmap.getAttribute('encoding_id'))
    chars_to_glyph_ids = [(int(mapping.getAttribute('char'), 16),
                           int(mapping.getAttribute('gid')))
                          for mapping in cmap.getElementsByTagName('map')]
    return (length, cmap_format, platform_id, encoding_id,
            dict(chars_to_glyph_ids[:self.num_mappings]))

  def CMapInfoToString(self, info_tuple):
    """Convert CMapInfo tuple to C++ struct."""

    def FormatString(x):
      return '{' + x + '},  '

    def FormatInt(x):
      return str(x) + ',  '
    length = info_tuple[0]
    cmap_format = info_tuple[1]
    platform_id = info_tuple[2]
    encoding_id = info_tuple[3]
    chars_to_glyph_ids = info_tuple[4]
    result = '{\n' + (FormatInt(length) + '// length\n' +
                      FormatInt(cmap_format) + '// format\n' +
                      FormatInt(platform_id) + '// platform id\n' +
                      FormatInt(encoding_id)) + '// encoding id\n'
    keys = map(str, chars_to_glyph_ids.keys())
    values = map(str, chars_to_glyph_ids.values())
    result += FormatString(join(keys, ', ')) + '// chars\n'
    result += FormatString(join(values, ', ')) + '// glyph ids\n}'
    return result
