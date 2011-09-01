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
#include "sfntly/table/truetype/loca_table.h"
#include "test/serialization_test.h"

namespace sfntly {

const int32_t LOCA_NUM_LOCAS = 198;
const int32_t LOCAS[] = {
    0x00000,  // 0
    0x00000,  // 1
    0x00000,  // 2
    0x00000,  // 3
    0x00000,  // 4
    0x00060,  // 5
    0x000E0,  // 6
    0x0014C,  // 7
    0x001FC,  // 8
    0x002C8,  // 9
    0x00358,  // 10
    0x0039C,  // 11
    0x003D4,  // 12
    0x0040C,  // 13
    0x004B4,  // 14
    0x004E4,  // 15
    0x00530,  // 16
    0x0054C,  // 17
    0x00584,  // 18
    0x005A0,  // 19
    0x0061C,  // 20
    0x0063C,  // 21
    0x006C4,  // 22
    0x0074C,  // 23
    0x0078C,  // 24
    0x00804,  // 25
    0x00888,  // 26
    0x008AC,  // 27
    0x0096C,  // 28
    0x009F0,  // 29
    0x00A08,  // 30
    0x00A20,  // 31
    0x00A48,  // 32
    0x00A70,  // 33
    0x00A98,  // 34
    0x00B44,  // 35
    0x00C40,  // 36
    0x00C78,  // 37
    0x00D24,  // 38
    0x00D9C,  // 39
    0x00DF8,  // 40
    0x00E28,  // 41
    0x00E54,  // 42
    0x00EDC,  // 43
    0x00F0C,  // 44
    0x00F24,  // 45
    0x00F6C,  // 46
    0x00FA4,  // 47
    0x00FC4,  // 48
    0x00FFC,  // 49
    0x01028,  // 50
    0x010BC,  // 51
    0x01114,  // 52
    0x011C0,  // 53
    0x0121C,  // 54
    0x012C0,  // 55
    0x012E8,  // 56
    0x0133C,  // 57
    0x01364,  // 58
    0x013A0,  // 59
    0x013E0,  // 60
    0x01410,  // 61
    0x0143C,  // 62
    0x01460,  // 63
    0x01480,  // 64
    0x014A4,  // 65
    0x014C8,  // 66
    0x014E0,  // 67
    0x0152C,  // 68
    0x015AC,  // 69
    0x01618,  // 70
    0x01680,  // 71
    0x016EC,  // 72
    0x0175C,  // 73
    0x017B0,  // 74
    0x01848,  // 75
    0x01898,  // 76
    0x018DC,  // 77
    0x01948,  // 78
    0x01980,  // 79
    0x019B0,  // 80
    0x01A28,  // 81
    0x01A74,  // 82
    0x01AE0,  // 83
    0x01B4C,  // 84
    0x01BC0,  // 85
    0x01BFC,  // 86
    0x01C94,  // 87
    0x01CCC,  // 88
    0x01D1C,  // 89
    0x01D48,  // 90
    0x01D84,  // 91
    0x01DBC,  // 92
    0x01E2C,  // 93
    0x01E54,  // 94
    0x01F04,  // 95
    0x01F1C,  // 96
    0x01FCC,  // 97
    0x02028,  // 98
    0x02044,  // 99
    0x020AC,  // 100
    0x02128,  // 101
    0x021B8,  // 102
    0x0220C,  // 103
    0x02234,  // 104
    0x022E0,  // 105
    0x02348,  // 106
    0x023F4,  // 107
    0x02430,  // 108
    0x02450,  // 109
    0x02464,  // 110
    0x02540,  // 111
    0x0255C,  // 112
    0x025C0,  // 113
    0x025DC,  // 114
    0x02628,  // 115
    0x02680,  // 116
    0x0270C,  // 117
    0x02720,  // 118
    0x02768,  // 119
    0x027A4,  // 120
    0x027B8,  // 121
    0x027D0,  // 122
    0x027E8,  // 123
    0x02800,  // 124
    0x02818,  // 125
    0x02830,  // 126
    0x02848,  // 127
    0x02898,  // 128
    0x028B0,  // 129
    0x028C8,  // 130
    0x028E0,  // 131
    0x028F8,  // 132
    0x02910,  // 133
    0x02928,  // 134
    0x02940,  // 135
    0x02958,  // 136
    0x02970,  // 137
    0x029D8,  // 138
    0x029F0,  // 139
    0x02A08,  // 140
    0x02A20,  // 141
    0x02A38,  // 142
    0x02A50,  // 143
    0x02A68,  // 144
    0x02A84,  // 145
    0x02B30,  // 146
    0x02B48,  // 147
    0x02B60,  // 148
    0x02B78,  // 149
    0x02B90,  // 150
    0x02BA8,  // 151
    0x02C00,  // 152
    0x02CCC,  // 153
    0x02CE4,  // 154
    0x02CFC,  // 155
    0x02D14,  // 156
    0x02D2C,  // 157
    0x02D44,  // 158
    0x02D5C,  // 159
    0x02E6C,  // 160
    0x02E84,  // 161
    0x02E9C,  // 162
    0x02EB4,  // 163
    0x02ECC,  // 164
    0x02EE4,  // 165
    0x02F3C,  // 166
    0x02F94,  // 167
    0x02FC8,  // 168
    0x0303C,  // 169
    0x030FC,  // 170
    0x03114,  // 171
    0x0312C,  // 172
    0x03144,  // 173
    0x0315C,  // 174
    0x03174,  // 175
    0x0318C,  // 176
    0x031A4,  // 177
    0x03240,  // 178
    0x03258,  // 179
    0x03270,  // 180
    0x03288,  // 181
    0x032A0,  // 182
    0x032B8,  // 183
    0x0333C,  // 184
    0x03354,  // 185
    0x03370,  // 186
    0x0338C,  // 187
    0x033A8,  // 188
    0x033BC,  // 189
    0x033D0,  // 190
    0x033E4,  // 191
    0x033F4,  // 192
    0x0340C,  // 193
    0x03424,  // 194
    0x03438,  // 195
    0x03448,  // 196
    0x034D4   // 197
};

static bool VerifyLOCA(Table* table) {
  LocaTablePtr loca = down_cast<LocaTable*>(table);
  if (loca == NULL) {
    return false;
  }

  EXPECT_EQ(loca->NumLocas(), LOCA_NUM_LOCAS);
  EXPECT_EQ(loca->num_glyphs(), LOCA_NUM_LOCAS - 1);

  for (int32_t i = 0; i < LOCA_NUM_LOCAS - 1; ++i) {
    EXPECT_EQ(loca->GlyphOffset(i), LOCAS[i]);
    EXPECT_EQ(loca->GlyphLength(i), LOCAS[i + 1] - LOCAS[i]);
  }
  return true;
}

bool VerifyLOCA(Table* original, Table* target) {
  EXPECT_TRUE(VerifyLOCA(original));
  EXPECT_TRUE(VerifyLOCA(target));
  return true;
}

}  // namespace sfntly
