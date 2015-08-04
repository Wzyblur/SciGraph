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
package edu.sdsc.scigraph.owlapi.loader.processor;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.ReadableIndex;

import com.google.common.collect.ImmutableMap;

import edu.sdsc.scigraph.frames.CommonProperties;
import edu.sdsc.scigraph.owlapi.OwlRelationships;

public class CategoryProcessor implements GraphProcessor {

  private static int BATCH_SIZE = 10_000;
  
  private static final Logger logger = Logger.getLogger(CategoryProcessor.class.getName());

  private final GraphDatabaseService graphDb;

  private final Map<String, String> categories;

  @Inject
  public CategoryProcessor(GraphDatabaseService graphDb, 
      @Named("owl.categories") Map<String, String> categories) {
    this.graphDb = graphDb;
    this.categories = ImmutableMap.copyOf(categories);
  }

  @Override
  public void process() throws Exception {
    logger.info("Processing categories");
    for (Entry<String, String> category : categories.entrySet()) {
      Set<Node> roots = new HashSet<>();
      try (Transaction tx = graphDb.beginTx()) {
        ReadableIndex<Node> nodeIndex = graphDb.index().getNodeAutoIndexer().getAutoIndex();
        Node root = nodeIndex.get(CommonProperties.URI, category.getKey()).getSingle();
        if (null == root) {
          logger.warning("Failed to locate " + category.getKey() + " while processing categories");
          continue;
        }
        roots.add(root);
        for (Relationship equiv: root.getRelationships(OwlRelationships.OWL_EQUIVALENT_CLASS)) {
          roots.add(equiv.getOtherNode(root));
        }
        tx.success();
      }
      if (roots.isEmpty()) {
        logger.warning("Failed to locate " + category.getKey() + " while processing categories");
      } else {
        for (Node root: roots) {
          logger.info("Processing category: " + category);
          // TODO: Actually multi-thread this...
          CategoryProcessorThread processor = new CategoryProcessorThread(graphDb, root, category.getValue(), BATCH_SIZE);
          long count = processor.call();
          logger.info("Processsed " + count + " nodes for " + category);
        }
      }
    }
  }

}
