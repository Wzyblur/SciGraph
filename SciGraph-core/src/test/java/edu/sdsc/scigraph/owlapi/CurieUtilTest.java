/**
 * Copyright (C) 2014 The SciGraph authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.sdsc.scigraph.owlapi;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;

public class CurieUtilTest {

  CurieUtil util;

  @Before
  public void setup() {
    Map<String, String> map = new HashMap<>();
    map.put("http://example.org/a_", "A");
    map.put("http://example.org/A_", "A");
    map.put("http://example.org/B_", "B");
    util = new CurieUtil(map);
  }

  @Test
  public void testGetCuriePrefixes() {
    assertThat(util.getPrefixes(), hasItems("A", "B"));
  }
  
  @Test
  public void testGetFullUri() {
    assertThat(util.getFullUri("A:foo"), containsInAnyOrder("http://example.org/a_foo", "http://example.org/A_foo"));
  }

  @Test
  public void testUnknownCurie() {
    assertThat(util.getFullUri("NONE:foo"), is(empty()));
  }

  @Test
  public void testGetCurie() {
    assertThat(util.getCurie("http://example.org/a_foo"), is(Optional.of("A:foo")));
  }

  @Test
  public void testUnknownUri() {
    assertThat(util.getFullUri("http://example.org/none_foo"), is(empty()));
  }

}
