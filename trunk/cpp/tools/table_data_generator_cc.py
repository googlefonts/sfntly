#!/usr/bin/python
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

"""Contains abstract table data generator for C++ structs."""

from string import Template

not_implemented_message = """Cannot instantiate abstract base class
TableDataGenerator"""


class TableDataGenerator(object):
  """Abstract base class for any table data generator that outputs C++ files."""

  def __init__(self, test_struct_name):
    self.test_struct_name = test_struct_name

  def TestStructs(self):
    raise NotImplementedError(not_implemented_message)

  def TestStructName(self):
    return self.test_struct_name

  def TestStructDecl(self):
    return Template('extern ' + self.TestStructName() + ' $var_name;\n')

  def TestStructDef(self):
    return Template(self.TestStructName() + ' $var_name = $var_contents;\n')

  def TestArrayName(self):
    return 'kAll' + self.TestStructName() + 's'

  def Generate(self, file_name, font):
    raise NotImplementedError(not_implemented_message)
