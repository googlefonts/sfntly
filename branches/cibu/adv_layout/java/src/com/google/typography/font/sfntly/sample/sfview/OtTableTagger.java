// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.sample.sfview;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.FontDataTable;
import com.google.typography.font.sfntly.table.opentype.CoverageTable;
import com.google.typography.font.sfntly.table.opentype.CoverageTableNew;
import com.google.typography.font.sfntly.table.opentype.FeatureListTable;
import com.google.typography.font.sfntly.table.opentype.FeatureTable;
import com.google.typography.font.sfntly.table.opentype.GSubTable;
import com.google.typography.font.sfntly.table.opentype.GsubLookupContextual;
import com.google.typography.font.sfntly.table.opentype.GsubLookupLigature;
import com.google.typography.font.sfntly.table.opentype.GsubLookupSingle;
import com.google.typography.font.sfntly.table.opentype.GsubLookupTable;
import com.google.typography.font.sfntly.table.opentype.LangSysTable;
import com.google.typography.font.sfntly.table.opentype.LigatureSubst;
import com.google.typography.font.sfntly.table.opentype.LookupListTable;
import com.google.typography.font.sfntly.table.opentype.LookupTableNew;
import com.google.typography.font.sfntly.table.opentype.NullTable;
import com.google.typography.font.sfntly.table.opentype.ScriptListTable;
import com.google.typography.font.sfntly.table.opentype.ScriptTable;
import com.google.typography.font.sfntly.table.opentype.SingleSubst;
import com.google.typography.font.sfntly.table.opentype.SubstSubtable;
import com.google.typography.font.sfntly.table.opentype.TaggedData;
import com.google.typography.font.sfntly.table.opentype.TaggedData.FieldType;
import com.google.typography.font.sfntly.table.opentype.coveragetable.InnerArrayFmt1;
import com.google.typography.font.sfntly.table.opentype.coveragetable.InnerArrayFmt2;
import com.google.typography.font.sfntly.table.opentype.ligaturesubst.Ligature;
import com.google.typography.font.sfntly.table.opentype.ligaturesubst.LigatureSet;
import com.google.typography.font.sfntly.table.opentype.singlesubst.HeaderFmt1;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author dougfelt@google.com (Doug Felt)
 */
public class OtTableTagger {
  private final TaggedData td;
  private final Map<Class<? extends FontDataTable>, TagMethod> tagMethodRegistry;

  public OtTableTagger(TaggedData tdata) {
    this.td = tdata;
    this.tagMethodRegistry = new HashMap<Class<? extends FontDataTable>, TagMethod>();

    registerTagMethods();
  }

  public void tag(GSubTable gsub) {
    tagTable(gsub.scriptList());
    tagTable(gsub.featureList());
    tagTable(gsub.lookupList());
  }

  public void tagTable(FontDataTable table) {
    if (table == null) {
      return;
    }
    ReadableFontData data = table.readFontData();
    if (data == null) {
      return;
    }
    TagMethod tm = getTagMethod(table);
    if (tm == null) {
      td.pushRange(table.getClass().getSimpleName(), data);
    } else {
      td.pushRange(tm.tableLabel(table), data);
      tm.tag(table);
    }
    td.popRange();
  }

  abstract class TagMethod {
    protected final Class<? extends FontDataTable> clzz;

    TagMethod(Class<? extends FontDataTable> clzz) {
      this.clzz = clzz;
    }

    String tableLabel(FontDataTable table) {
      Class<?> clzz = table.getClass();
      Class<?> encl = clzz.getEnclosingClass();
      if (encl == null) {
        return clzz.getSimpleName();
      }
      return encl.getSimpleName() + "." + clzz.getSimpleName();
    }

    abstract void tag(FontDataTable table);
  }

  private void register(TagMethod m) {
    tagMethodRegistry.put(m.clzz, m);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void register(TagMethod m, Class... clzzs) {
    tagMethodRegistry.put(m.clzz, m);
    for (Class<? extends FontDataTable> clzz : clzzs) {
      tagMethodRegistry.put(clzz, m);
    }
  }

  void registerTagMethods() {
    register(new TagMethod(ScriptListTable.class) {
      @Override
      public void tag(FontDataTable fdt) {
        ScriptListTable table = (ScriptListTable) fdt;
        int scriptCount = td.tagRangeField(FieldType.SHORT, "script count");
        for (int i = 0; i < scriptCount; ++i) {
          td.tagRangeField(FieldType.TAG, null);
          td.tagRangeField(FieldType.OFFSET, null);
        }
        for (ScriptTable st : table) {
          tagTable(st);
        }
      }
    });

    register(new TagMethod(ScriptTable.class) {
      @Override
      public void tag(FontDataTable fdt) {
        ScriptTable table = (ScriptTable) fdt;
        td.tagRangeField(FieldType.OFFSET_NONZERO, "default lang sys");
        int langCount = td.tagRangeField(FieldType.SHORT, "language count");
        for (int i = 0; i < langCount; ++i) {
          td.tagRangeField(FieldType.TAG, null);
          td.tagRangeField(FieldType.OFFSET, null);
        }
        for (LangSysTable lst : table) {
          tagTable(lst);
        }
        tagTable(table.defaultLangSysTable());
      }
    });

    register(new TagMethod(LangSysTable.class) {
      @Override
      public void tag(FontDataTable fdt) {
        LangSysTable table = (LangSysTable) fdt;
        td.tagRangeField(FieldType.SHORT_IGNORED, "lookup order");
        td.tagRangeField(FieldType.SHORT_IGNORED_FFFF, "required feature");
        td.tagRangeField(FieldType.SHORT, "feature count");
        for (int i = 0; i < table.recordList.count(); ++i) {
          td.tagRangeField(FieldType.SHORT, null);
        }
      }
    });

    register(new TagMethod(FeatureListTable.class) {
      @Override
      public void tag(FontDataTable fdt) {
        FeatureListTable table = (FeatureListTable) fdt;
        int featureCount = td.tagRangeField(FieldType.SHORT, "feature count");
        for (int i = 0; i < featureCount; ++i) {
          td.tagRangeField(FieldType.TAG, null);
          td.tagRangeField(FieldType.OFFSET, null);
        }
        for (FeatureTable ft : table) {
          tagTable(ft);
        }
      }
    });

    register(new TagMethod(FeatureTable.class) {
      @Override
      public void tag(FontDataTable fdt) {
        FeatureTable table = (FeatureTable) fdt;
        td.tagRangeField(FieldType.OFFSET_NONZERO, "feature params");
        td.tagRangeField(FieldType.SHORT, "lookup count");
        for (int i = 0; i < table.recordList.count(); ++i) {
          td.tagRangeField(FieldType.SHORT, null);
        }
      }
    });

    register(new TagMethod(LookupListTable.class) {
      @Override
      public void tag(FontDataTable fdt) {
        LookupListTable table = (LookupListTable) fdt;
        int lookupCount = td.tagRangeField(FieldType.SHORT, "lookup count");
        for (int i = 0; i < lookupCount; ++i) {
          td.tagRangeField(FieldType.OFFSET, null);
        }
        for (int i = 0; i < lookupCount; ++i) {
          LookupTableNew lookup = table.subTableAt(i);
          if (lookup != null) {
            tagTable(lookup);
          }
        }
      }
    });

    register(new TagMethod(LookupTableNew.class) {
      @Override
      public void tag(FontDataTable fdt) {
        LookupTableNew table = (LookupTableNew) fdt;
        td.tagRangeField(FieldType.SHORT, "lookup type");
        td.tagRangeField(FieldType.SHORT, "lookup flags");
        int subTableCount = td.tagRangeField(FieldType.SHORT, "subtable count");
        for (int i = 0; i < subTableCount; ++i) {
          td.tagRangeField(FieldType.OFFSET, null);
        }
        // TODO(cibu): introduce this back.
        // if (table.useMarkFilteringSet()) {
        // td.tagRangeField(FieldType.SHORT, "mark filtering set");
        // }
        for (int i = 0; i < subTableCount; ++i) {
          SubstSubtable subTable = table.subTableAt(i);
          tagTable(subTable);
        }
      }
    }, GsubLookupTable.class, GsubLookupSingle.class, GsubLookupLigature.class);

    register(new TagMethod(LigatureSubst.class) {
      @Override
      public void tag(FontDataTable fdt) {
        LigatureSubst table = (LigatureSubst) fdt;
        td.tagRangeField(FieldType.SHORT, "subst format");
        td.tagRangeField(FieldType.OFFSET_NONZERO, "coverage offset");
        tagTable(table.coverage());
        td.tagRangeField(FieldType.SHORT, "subtable count");

        int subTableCount = table.recordList().count();
        for (int i = 0; i < subTableCount; ++i) {
          td.tagRangeField(FieldType.OFFSET, null);
        }

        for (int i = 0; i < subTableCount; ++i) {
          LigatureSet subTable = table.subTableAt(i);
          tagTable(subTable);
        }
      }
    });

    register(new TagMethod(LigatureSet.class) {
      @Override
      public void tag(FontDataTable fdt) {
        LigatureSet table = (LigatureSet) fdt;
        td.tagRangeField(FieldType.SHORT, "lookup count");
        for (int i = 0; i < table.recordList.count(); ++i) {
          td.tagRangeField(FieldType.OFFSET, null);
        }
        for (int i = 0; i < table.recordList.count(); ++i) {
          Ligature lookup = table.subTableAt(i);
          if (lookup != null) {
            tagTable(lookup);
          }
        }
      }
    });

    register(new TagMethod(Ligature.class) {
      @Override
      public void tag(FontDataTable fdt) {
        Ligature table = (Ligature) fdt;
        td.tagRangeField(FieldType.SHORT, "lig glyph");
        td.tagRangeField(FieldType.SHORT, "glyph count + 1");
        for (int i = 0; i < table.recordList.count(); ++i) {
          td.tagRangeField(FieldType.SHORT, null);
        }
      }
    });

    register(new TagMethod(SingleSubst.class) {
      @Override
      public void tag(FontDataTable fdt) {
        SingleSubst table = (SingleSubst) fdt;
        td.tagRangeField(FieldType.SHORT, "format");
        switch (table.format) {
        case 1:
          HeaderFmt1 tableFmt1 = table.fmt1;
          td.tagRangeField(FieldType.OFFSET_NONZERO, "coverage offset");
          tagTable(tableFmt1.coverage);
          td.tagRangeField(FieldType.SHORT, "delta glyph id");
          break;
        case 2:
          com.google.typography.font.sfntly.table.opentype.singlesubst.InnerArrayFmt2 tableFmt2 = table.fmt2;
          td.tagRangeField(FieldType.OFFSET_NONZERO, "coverage offset");
          tagTable(tableFmt2.coverage);
          td.tagRangeField(FieldType.SHORT, "glyph count");
          for (int i = 0; i < tableFmt2.recordList.count(); ++i) {
            td.tagRangeField(FieldType.SHORT, null);
          }
          break;
        }
      }
    });

    register(new TagMethod(NullTable.class) {
      @Override
      public void tag(FontDataTable fdt) {
      }
    });

    register(new TagMethod(GsubLookupSingle.Fmt1.class) {
      @Override
      public void tag(FontDataTable fdt) {
        GsubLookupSingle.Fmt1 table = (GsubLookupSingle.Fmt1) fdt;
        td.tagRangeField(FieldType.SHORT, "format");
        td.tagRangeField(FieldType.OFFSET, "coverage");
        tagTable(table.coverage());
        td.tagRangeField(FieldType.SHORT, "delta");
      }
    });

    register(new TagMethod(GsubLookupSingle.Fmt2.class) {
      @Override
      public void tag(FontDataTable fdt) {
        GsubLookupSingle.Fmt2 table = (GsubLookupSingle.Fmt2) fdt;
        td.tagRangeField(FieldType.SHORT, "format");
        td.tagRangeField(FieldType.OFFSET, "coverage");
        tagTable(table.coverage());
        int glyphCount = td.tagRangeField(FieldType.SHORT, "glyph count");
        for (int i = 0; i < glyphCount; ++i) {
          td.tagRangeField(FieldType.SHORT, String.valueOf(i + 1));
        }
      }
    });

    register(new TagMethod(CoverageTable.Fmt1.class) {
      @Override
      public void tag(FontDataTable fdt) {
        CoverageTable.Fmt1 table = (CoverageTable.Fmt1) fdt;
        td.tagRangeField(FieldType.SHORT, "format");
        int glyphCount = td.tagRangeField(FieldType.SHORT, "glyph count");
        for (int i = 0; i < glyphCount; ++i) {
          td.tagRangeField(FieldType.SHORT, String.valueOf(i + 1));
        }
      }
    });

    register(new TagMethod(CoverageTable.Fmt2.class) {
      @Override
      public void tag(FontDataTable fdt) {
        CoverageTable.Fmt2 table = (CoverageTable.Fmt2) fdt;
        td.tagRangeField(FieldType.SHORT, "format");
        int rangeCount = td.tagRangeField(FieldType.SHORT, "range count");
        for (int i = 0; i < rangeCount; ++i) {
          td.tagRangeField(FieldType.SHORT, "start");
          td.tagRangeField(FieldType.SHORT, "end");
          td.tagRangeField(FieldType.SHORT, "offset");
        }
      }
    });

    register(new TagMethod(CoverageTableNew.class) {
      @Override
      public void tag(FontDataTable fdt) {
        CoverageTableNew table = (CoverageTableNew) fdt;
        td.tagRangeField(FieldType.SHORT, "format");
        if (table.header.format == 1) {
          InnerArrayFmt1 tableFmt1 = (InnerArrayFmt1) table.array;
          td.tagRangeField(FieldType.SHORT, "glyph count");
          for (int i = 0; i < tableFmt1.recordList.count(); ++i) {
            td.tagRangeField(FieldType.SHORT, String.valueOf(i + 1));
          }
        }
        if (table.header.format == 2) {
          InnerArrayFmt2 tableFmt2 = (InnerArrayFmt2) table.array;
          td.tagRangeField(FieldType.SHORT, "range count");
          for (int i = 0; i < tableFmt2.recordList.count(); ++i) {
            td.tagRangeField(FieldType.SHORT, "start");
            td.tagRangeField(FieldType.SHORT, "end");
            td.tagRangeField(FieldType.SHORT, "offset");
          }
        }
      }
    });

    register(new TagMethod(GsubLookupLigature.LigatureSubTable.class) {
      @Override
      public void tag(FontDataTable fdt) {
        GsubLookupLigature.LigatureSubTable table = (GsubLookupLigature.LigatureSubTable) fdt;
        td.tagRangeField(FieldType.SHORT, "format");
        td.tagRangeField(FieldType.OFFSET, "coverage");
        tagTable(table.coverage());
        int setCount = td.tagRangeField(FieldType.SHORT, "set count");
        for (int i = 0; i < setCount; ++i) {
          int setBase = td.tagRangeField(FieldType.OFFSET, String.valueOf(i));
          td.pushRangeAtOffset("LigatureSet", setBase);
          int ligCount = td.tagRangeField(FieldType.SHORT, "ligature table count");
          for (int j = 0; j < ligCount; ++j) {
            int ligBase = td.tagRangeField(FieldType.OFFSET, String.valueOf(j));
            td.pushRangeAtOffset("LigatureTable", ligBase);
            td.tagRangeField(FieldType.SHORT, "lig glyph");
            int cmpCount = td.tagRangeField(FieldType.SHORT, "component count");
            for (int k = 1; k < cmpCount; ++k) {
              td.tagRangeField(FieldType.SHORT, String.valueOf(k));
            }
            td.popRange();
          }
          td.popRange();
        }
      }
    });

    register(new TagMethod(GsubLookupContextual.Fmt1.class) {
      @Override
      public void tag(FontDataTable fdt) {
        GsubLookupContextual.Fmt1 table = (GsubLookupContextual.Fmt1) fdt;
        td.tagRangeField(FieldType.SHORT, "format");
        td.tagRangeField(FieldType.OFFSET, "coverage");
        tagTable(table.coverage());
      }
    });

  }

  private static final Comparator<Class<? extends FontDataTable>> CLASS_NAME_COMPARATOR =
    new Comparator<Class<? extends FontDataTable>>() {
    @Override
    public int compare(Class<? extends FontDataTable> o1, Class<? extends FontDataTable> o2) {
      return o1.getName().compareTo(o2.getName());
    }
    };

  private static Set<Class<? extends FontDataTable>>
      missedClasses = new TreeSet<Class<? extends FontDataTable>>(CLASS_NAME_COMPARATOR);

  private TagMethod getTagMethod(FontDataTable table) {
    Class<? extends FontDataTable> clzz = table.getClass();
    TagMethod tm = tagMethodRegistry.get(clzz);
    if (tm == null) {
      if (!missedClasses.contains(clzz)) {
        missedClasses.add(clzz);
        System.out.println("unregistered class: " + clzz.getName());
      }
    }
    return tm;
  }
}
