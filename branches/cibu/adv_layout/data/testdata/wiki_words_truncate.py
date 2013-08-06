#!/usr/bin/python

import math
import sys
import os

def truncateFile(filePath, count):
#  print filePath
  with open(filePath, "r+") as f:
    for x in xrange(count):
        f.readline()
    f.truncate()

WORD_PATH = '/usr/local/google/home/cibu/sfntly/adv_layout/data/testdata/wiki_words'
OUT_DIR   = '/usr/local/google/home/cibu/sfntly/adv_layout/data/testdata/wiki_words_hb_closure'
MAX_WORD_COUNT = 5000

for file in os.listdir(WORD_PATH):
  if file.startswith('.'): #.svn ignore
    continue
  truncateFile(WORD_PATH + '/' + file, MAX_WORD_COUNT)

for folder, subs, files in os.walk(OUT_DIR):
  if not folder.endswith('.ttf'):
    continue
  if files is None or len(files) is 0:
    print sys.stderr.write(folder + "\n")
    continue
  count = int(math.ceil(float(MAX_WORD_COUNT)/len(files))) 
  for file in files:
    truncateFile(folder + '/' + file, count)
