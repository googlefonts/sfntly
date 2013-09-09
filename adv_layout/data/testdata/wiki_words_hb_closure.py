#!/usr/bin/python

import subprocess
import os
import tempfile

FONT_LANGS = '/tmp/font-languages.txt'
HB_CLOSURE_BIN = '/usr/local/google/home/cibu/behdad-opensource-staging-area.pyotlss/closure-gsub-text.py'
WORD_PATH = '/usr/local/google/home/cibu/sfntly/adv_layout/data/testdata/wiki_words'
OUT_DIR = '/usr/local/google/home/cibu/sfntly/adv_layout/data/testdata/wiki_words_hb_closure'
DIR_FILTER = '' # containing string; ex: windows8/gadug
MAX = 500


def getLangs():
  return [file for file in os.listdir(WORD_PATH) if not file.startswith('.') and not file.endswith('~') ]

def getLangLinesMap():
  map = {}
  for lang in getLangs():
    f = open(WORD_PATH + '/' + lang)
    map[lang] = f.readlines()
    f.close()
  return map

def composeInput(map, langs, count):
  lines = []
  for lang in langs:
    lines += map[lang][:count]
  return lines

def splitOutput(lines, langs, count):
  map = {}
  i = 0
  for lang in langs:
    map[lang] = lines[i:i+count]
    i += count
  return map

def writeLangOut(map, osFont, lang):
  f = open(OUT_DIR + '/' + osFont + '/' + lang, 'w') 
  for line in map[lang]:
    f.write("%s\n" % line)
  f.close()

def hbInputOutput(font, input):
  tmpFile = tempfile.NamedTemporaryFile(delete=False)
  tmpFile.write(''.join(input))
  tmpFile.close()
  
  p = subprocess.Popen([HB_CLOSURE_BIN, font, tmpFile.name], stdout=subprocess.PIPE)
  lines = p.communicate()[0].split('\n')
  os.unlink(tmpFile.name)
  return lines

def fontLangMap():
  map = {}
  fontLangFile = open(FONT_LANGS)
  for line in fontLangFile.read().splitlines():
    row = line.split(',')
    fontPath = row[0]
    langs = row[1:]
    if not DIR_FILTER or DIR_FILTER in fontPath:
      map[fontPath] = langs
  return map
  
def createOutputDir(osFontPart):
  outputDir = OUT_DIR + '/' + osFontPart
  print 'Creating ' + outputDir
  try:
    os.makedirs(outputDir)
  except:
    pass

#
# Main
#

testlines = getLangLinesMap()
fontLangs = fontLangMap()

for (fontPath, langs) in sorted(fontLangs.items()):
  if len(langs) is 0:
    continue

  count = int(float(MAX) / len(langs))
  
  input = composeInput(testlines, langs, count)
  output = hbInputOutput(fontPath, input)
  outputMap = splitOutput(output, langs, count)

  osFontPart = '/'.join(fontPath.split('/')[-2:])
  createOutputDir(osFontPart)
  for lang in langs:
    writeLangOut(outputMap, osFontPart, lang)
