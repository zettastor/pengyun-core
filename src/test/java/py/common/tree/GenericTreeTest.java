/**
* Copyright (C) 2013-2024 Nanjing Pengyun Network Technology Co., Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/ 

package py.common.tree;

import java.nio.ByteBuffer;
import java.util.Iterator;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Level;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import py.test.TestBase;

public class GenericTreeTest extends TestBase {
  @Test
  public void basicTest() {
    setLogLevel(Level.DEBUG);
    GenericTree<Integer> intTree = new GenericTree<>(0);
    GenericTree.TreeNode<Integer> rootNode = intTree.getRoot();

    GenericTree.TreeNode<Integer> node1 = intTree.insert(1, rootNode);
    GenericTree.TreeNode<Integer> node2 = intTree.insert(2, rootNode);
    GenericTree.TreeNode<Integer> node3 = intTree.insert(3, rootNode);

    GenericTree.TreeNode<Integer> node14 = intTree.insert(4, node1);
    GenericTree.TreeNode<Integer> node25 = intTree.insert(5, node2);
    GenericTree.TreeNode<Integer> node256 = intTree.insert(6, node25);

    GenericTree.TreeNode<Integer> node17 = intTree.insert(7, node1);
    Validate.isTrue(node14.parent() == node1);

    Validate.isTrue(node256.parent() == node25);
    Validate.isTrue(node25.parent() == node2);

    Validate
        .isTrue(node1.children().containsAll(Sets.<GenericTree.TreeNode>newSet(node14, node17)));

    byte[] byteArray = intTree
        .toByteArray(integer -> ByteBuffer.allocate(4).putInt(integer).array());

    logger.warn("byte array {}", byteArray);
    GenericTree<Integer> parsedTree = GenericTree
        .parseFrom(byteArray, bytes -> ByteBuffer.wrap(bytes).getInt());
    GenericTree.TreeNode parsedRootNode = parsedTree.getRoot();

    Validate.isTrue(parsedRootNode.content().equals(rootNode.content()));

    Iterator<GenericTree.TreeNode<Integer>> it1 = intTree.asList().iterator();
    Iterator<GenericTree.TreeNode<Integer>> it2 = parsedTree.asList().iterator();

    while (it1.hasNext() || it2.hasNext()) {
      Validate.isTrue(it1.next().content().equals(it2.next().content()));
    }
  }

}