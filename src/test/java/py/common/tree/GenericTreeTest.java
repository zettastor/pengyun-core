/*
 * Copyright (c) 2022-2022. PengYunNetWork
 *
 * This program is free software: you can use, redistribute, and/or modify it
 * under the terms of the GNU Affero General Public License, version 3 or later ("AGPL"),
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  You should have received a copy of the GNU Affero General Public License along with
 *  this program. If not, see <http://www.gnu.org/licenses/>.
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