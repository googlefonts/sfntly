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

#include "gtest/gtest.h"
#include "sfntly/font.h"
#include "sfntly/horizontal_metrics_table.h"
#include "test/serialization_test.h"

namespace sfntly {

const int32_t HMTX_ENTRIES_COUNT = 197;
const int32_t HMTX_LSB_COUNT = 0;

struct HmtxEntry {
  int32_t advance_width_;
  int32_t lsb_;

  HmtxEntry(int32_t advance_width, int32_t lsb)
      : advance_width_(advance_width), lsb_(lsb) {}
};

const HmtxEntry HMTX_ENTRIES[] = {
    HmtxEntry(32768, 0),  // 0
    HmtxEntry(0, 0),  // 1
    HmtxEntry(682, 0),  // 2
    HmtxEntry(616, 0),  // 3
    HmtxEntry(421, 103),  // 4
    HmtxEntry(690, 129),  // 5
    HmtxEntry(1589, 129),  // 6
    HmtxEntry(1017, 25),  // 7
    HmtxEntry(1402, 104),  // 8
    HmtxEntry(1241, 100),  // 9
    HmtxEntry(382, 129),  // 10
    HmtxEntry(548, 41),  // 11
    HmtxEntry(536, 90),  // 12
    HmtxEntry(913, 33),  // 13
    HmtxEntry(894, 78),  // 14
    HmtxEntry(399, 103),  // 15
    HmtxEntry(866, 109),  // 16
    HmtxEntry(405, 103),  // 17
    HmtxEntry(808, -23),  // 18
    HmtxEntry(1136, 100),  // 19
    HmtxEntry(1136, 424),  // 20
    HmtxEntry(1136, 109),  // 21
    HmtxEntry(1126, 113),  // 22
    HmtxEntry(1136, 88),  // 23
    HmtxEntry(1136, 135),  // 24
    HmtxEntry(1136, 129),  // 25
    HmtxEntry(1136, 111),  // 26
    HmtxEntry(1136, 104),  // 27
    HmtxEntry(1136, 143),  // 28
    HmtxEntry(423, 113),  // 29
    HmtxEntry(423, 113),  // 30
    HmtxEntry(843, 84),  // 31
    HmtxEntry(907, 66),  // 32
    HmtxEntry(856, 92),  // 33
    HmtxEntry(985, 57),  // 34
    HmtxEntry(1624, 41),  // 35
    HmtxEntry(1302, 72),  // 36
    HmtxEntry(1179, 121),  // 37
    HmtxEntry(1220, 100),  // 38
    HmtxEntry(1220, 121),  // 39
    HmtxEntry(1142, 121),  // 40
    HmtxEntry(1146, 121),  // 41
    HmtxEntry(1363, 100),  // 42
    HmtxEntry(1198, 121),  // 43
    HmtxEntry(444, 154),  // 44
    HmtxEntry(1140, 113),  // 45
    HmtxEntry(1239, 121),  // 46
    HmtxEntry(1105, 121),  // 47
    HmtxEntry(1562, 121),  // 48
    HmtxEntry(1271, 121),  // 49
    HmtxEntry(1333, 92),  // 50
    HmtxEntry(1099, 121),  // 51
    HmtxEntry(1370, 92),  // 52
    HmtxEntry(1101, 121),  // 53
    HmtxEntry(1079, 45),  // 54
    HmtxEntry(1243, 88),  // 55
    HmtxEntry(1265, 121),  // 56
    HmtxEntry(1245, 80),  // 57
    HmtxEntry(1576, 84),  // 58
    HmtxEntry(1214, 74),  // 59
    HmtxEntry(1179, 76),  // 60
    HmtxEntry(1325, 100),  // 61
    HmtxEntry(626, 131),  // 62
    HmtxEntry(811, -29),  // 63
    HmtxEntry(638, 129),  // 64
    HmtxEntry(927, 133),  // 65
    HmtxEntry(1241, 88),  // 66
    HmtxEntry(516, 96),  // 67
    HmtxEntry(993, 84),  // 68
    HmtxEntry(1044, 129),  // 69
    HmtxEntry(921, 76),  // 70
    HmtxEntry(1044, 73),  // 71
    HmtxEntry(1013, 98),  // 72
    HmtxEntry(716, 125),  // 73
    HmtxEntry(1021, 73),  // 74
    HmtxEntry(1064, 129),  // 75
    HmtxEntry(430, 131),  // 76
    HmtxEntry(468, -119),  // 77
    HmtxEntry(944, 129),  // 78
    HmtxEntry(430, 150),  // 79
    HmtxEntry(1533, 129),  // 80
    HmtxEntry(1064, 129),  // 81
    HmtxEntry(1054, 102),  // 82
    HmtxEntry(1044, 129),  // 83
    HmtxEntry(1048, 73),  // 84
    HmtxEntry(686, 129),  // 85
    HmtxEntry(921, 96),  // 86
    HmtxEntry(696, 102),  // 87
    HmtxEntry(1064, 120),  // 88
    HmtxEntry(1024, 82),  // 89
    HmtxEntry(1374, 68),  // 90
    HmtxEntry(901, 53),  // 91
    HmtxEntry(1064, 120),  // 92
    HmtxEntry(962, 90),  // 93
    HmtxEntry(747, 90),  // 94
    HmtxEntry(368, 125),  // 95
    HmtxEntry(747, 133),  // 96
    HmtxEntry(735, 63),  // 97
    HmtxEntry(309, 28),  // 98
    HmtxEntry(808, 80),  // 99
    HmtxEntry(1120, 84),  // 100
    HmtxEntry(856, 63),  // 101
    HmtxEntry(1110, 74),  // 102
    HmtxEntry(305, 90),  // 103
    HmtxEntry(686, 43),  // 104
    HmtxEntry(794, 76),  // 105
    HmtxEntry(942, 41),  // 106
    HmtxEntry(872, 59),  // 107
    HmtxEntry(1026, 154),  // 108
    HmtxEntry(950, 109),  // 109
    HmtxEntry(1189, 94),  // 110
    HmtxEntry(706, 63),  // 111
    HmtxEntry(598, 49),  // 112
    HmtxEntry(983, 150),  // 113
    HmtxEntry(550, 97),  // 114
    HmtxEntry(1003, 154),  // 115
    HmtxEntry(1150, 121),  // 116
    HmtxEntry(438, 142),  // 117
    HmtxEntry(473, 117),  // 118
    HmtxEntry(933, 98),  // 119
    HmtxEntry(954, 18),  // 120
    HmtxEntry(1292, 82),  // 121
    HmtxEntry(1292, 82),  // 122
    HmtxEntry(1292, 82),  // 123
    HmtxEntry(1292, 82),  // 124
    HmtxEntry(1292, 82),  // 125
    HmtxEntry(1292, 82),  // 126
    HmtxEntry(1388, 83),  // 127
    HmtxEntry(1220, 120),  // 128
    HmtxEntry(1146, 131),  // 129
    HmtxEntry(1146, 131),  // 130
    HmtxEntry(1146, 131),  // 131
    HmtxEntry(1146, 131),  // 132
    HmtxEntry(440, -53),  // 133
    HmtxEntry(440, 164),  // 134
    HmtxEntry(440, -103),  // 135
    HmtxEntry(440, -90),  // 136
    HmtxEntry(3969, 144),  // 137
    HmtxEntry(1292, 131),  // 138
    HmtxEntry(1329, 102),  // 139
    HmtxEntry(1329, 102),  // 140
    HmtxEntry(1329, 102),  // 141
    HmtxEntry(1329, 102),  // 142
    HmtxEntry(1329, 102),  // 143
    HmtxEntry(833, 108),  // 144
    HmtxEntry(2416, 96),  // 145
    HmtxEntry(1298, 131),  // 146
    HmtxEntry(1298, 131),  // 147
    HmtxEntry(1298, 131),  // 148
    HmtxEntry(1298, 131),  // 149
    HmtxEntry(1179, 86),  // 150
    HmtxEntry(1329, 145),  // 151
    HmtxEntry(1212, 92),  // 152
    HmtxEntry(1036, 94),  // 153
    HmtxEntry(1036, 94),  // 154
    HmtxEntry(1036, 94),  // 155
    HmtxEntry(1036, 94),  // 156
    HmtxEntry(1036, 94),  // 157
    HmtxEntry(1036, 94),  // 158
    HmtxEntry(1486, 84),  // 159
    HmtxEntry(1011, 86),  // 160
    HmtxEntry(1046, 108),  // 161
    HmtxEntry(1046, 108),  // 162
    HmtxEntry(1046, 108),  // 163
    HmtxEntry(1046, 108),  // 164
    HmtxEntry(428, -75),  // 165
    HmtxEntry(421, 150),  // 166
    HmtxEntry(888, 115),  // 167
    HmtxEntry(667, 8),  // 168
    HmtxEntry(1030, 82),  // 169
    HmtxEntry(1044, 139),  // 170
    HmtxEntry(1060, 112),  // 171
    HmtxEntry(1062, 112),  // 172
    HmtxEntry(1062, 112),  // 173
    HmtxEntry(1062, 112),  // 174
    HmtxEntry(1060, 112),  // 175
    HmtxEntry(911, 119),  // 176
    HmtxEntry(1024, 86),  // 177
    HmtxEntry(1032, 130),  // 178
    HmtxEntry(1064, 130),  // 179
    HmtxEntry(1064, 130),  // 180
    HmtxEntry(1064, 130),  // 181
    HmtxEntry(1064, 130),  // 182
    HmtxEntry(1003, 129),  // 183
    HmtxEntry(1064, 130),  // 184
    HmtxEntry(1087, 170),  // 185
    HmtxEntry(1001, 170),  // 186
    HmtxEntry(1290, 150),  // 187
    HmtxEntry(385, 83),  // 188
    HmtxEntry(385, 95),  // 189
    HmtxEntry(385, 99),  // 190
    HmtxEntry(385, 106),  // 191
    HmtxEntry(677, 81),  // 192
    HmtxEntry(677, 97),  // 193
    HmtxEntry(688, 113),  // 194
    HmtxEntry(688, 91),  // 195
    HmtxEntry(1089, 135),  // 196

    HmtxEntry(32768, 0)  // 197: no such element, used to check the logic.
};

static bool VerifyHMTX(Table* table) {
  HorizontalMetricsTablePtr hmtx = down_cast<HorizontalMetricsTable*>(table);
  if (hmtx == NULL) {
    return false;
  }

  EXPECT_EQ(hmtx->NumberOfHMetrics(), HMTX_ENTRIES_COUNT);
  EXPECT_EQ(hmtx->NumberOfLSBs(), HMTX_LSB_COUNT);

  for (int32_t i = 0; i < HMTX_ENTRIES_COUNT + 1; ++i) {
    EXPECT_EQ(hmtx->AdvanceWidth(i), HMTX_ENTRIES[i].advance_width_);
    EXPECT_EQ(hmtx->LeftSideBearing(i), HMTX_ENTRIES[i].lsb_);
  }

  return true;
}

bool VerifyHMTX(Table* original, Table* target) {
  EXPECT_TRUE(VerifyHMTX(original));
  EXPECT_TRUE(VerifyHMTX(target));
  return true;
}

}  // namespace sfntly
