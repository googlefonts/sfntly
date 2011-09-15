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

"""Module that generates C++ font test data from a font using the XML format."""

import xml.dom.minidom as minidom


class FontDataGeneratorCC(object):
  """Generates font test stricts as C++ files.

  Uses a list of |table_data_generators| and
  uses the font described by |xml_path|.
  """

  def __init__(self, table_data_generators, xml_path):
    self.table_data_generators = table_data_generators
    self.xml_path = xml_path

  def Generate(self):
    doc = minidom.getDOMImplementation().parseString(
        open(self.xml_path, 'r').read())
    font_path = doc.getElementsByTagName('font').getAttribute('path')
    for (name, table_data_generator) in self.table_data_generators:
      name += '_table'
      table_element = doc.getElementsByTagName(name)[0]
      table_data_generator.Generate(font_path, table_element)
    return doc
