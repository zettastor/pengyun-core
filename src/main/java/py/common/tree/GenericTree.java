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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.ByteArrayParser;
import py.common.ToByteArray;
import py.common.Utils;

/**
 * A generic n-ary tree.
 *
 * @param <K> the content of tree node
 */
public class GenericTree<K> {
  private static final Logger logger = LoggerFactory.getLogger(GenericTree.class);

  private TreeNode<K> root;

  public GenericTree(K rootContent) {
    root = new TreeNode<>(rootContent);
  }

  private GenericTree(TreeNode<K> rootContent) {
    root = rootContent;
  }

  public static <K> GenericTree<K> parseFrom(byte[] byteArray, ByteArrayParser<K> parserK) {
    TreeNode<K> rootNode = null;
    TreeNode<K> parentNode = null;
    ByteBuffer buffer = ByteBuffer.wrap(byteArray);
    while (buffer.hasRemaining()) {
      int length = buffer.getInt();
      logger.debug("content length {}", length);
      if (length == 0) {
        break;
      }
      if (length == -1) { // if we get a back node
        Validate.notNull(parentNode);
        parentNode = parentNode.parent;
      } else {
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        K content = parserK.parse(bytes);
        if (parentNode == null) {
          parentNode = new TreeNode<>(content);
          rootNode = parentNode;
          logger.debug("root {}", rootNode.content);
        } else {
          logger.debug("{} -> {}", parentNode.content, content);
          parentNode = parentNode.addChild(content);
        }
      }
    }
    return new GenericTree<>(rootNode);
  }

  public TreeNode<K> getRoot() {
    return root;
  }

  public void remove(TreeNode<K> node) {
    node.removeMyself();
  }

  public TreeNode<K> insert(K newContent, TreeNode<K> parent) {
    return parent.addChild(newContent);
  }

  public List<TreeNode<K>> asList() {
    List<TreeNode<K>> nodeList = new LinkedList<>();
    Stack<TreeNode<K>> nodeStack = new Stack<>();
    nodeStack.push(root);

    while (!nodeStack.isEmpty()) {
      TreeNode<K> node = nodeStack.pop();
      nodeList.add(node);
      if (!node.isLeafNode()) {
        for (TreeNode<K> child : node.children) {
          nodeStack.push(child);
        }
      }
    }
    return nodeList;
  }

  public byte[] toByteArray(ToByteArray<K> toByteArray) {
    ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
    appendByteArrayFromTree(byteArray, root, toByteArray);
    return byteArray.toByteArray();
  }

  private void appendByteArrayFromTree(ByteArrayOutputStream byteArray, TreeNode<K> rootNode,
      ToByteArray<K> toByteArray) {
    byte[] rootValue = toByteArray.toByteArray(rootNode.content);
    try {
      byteArray.write(Utils.integerToBytes(rootValue.length));
      byteArray.write(rootValue);
    } catch (IOException e) {
      throw new RuntimeException();
    }

    if (rootNode.children() != null && !rootNode.children().isEmpty()) {
      for (TreeNode<K> child : rootNode.children()) {
        appendByteArrayFromTree(byteArray, child, toByteArray);
        try {
          byteArray.write(Utils.integerToBytes(-1));
        } catch (IOException e) {
          throw new RuntimeException();
        }
      }
    }
  }

  /**
   * The node of the n-ary tree.
   */
  public static class TreeNode<K> {
    /**
     * The content of this node.
     */
    private K content;
    /**
     * The parent of this node.
     */
    private TreeNode<K> parent;
    /**
     * The children list of this node.
     */
    private List<TreeNode<K>> children;
    /**
     * Is root node.
     */
    private boolean isRoot;

    TreeNode(K content) {
      this.content = content;
      this.isRoot = true;
    }

    public K content() {
      return content;
    }

    public TreeNode<K> parent() {
      return parent;
    }

    public List<TreeNode<K>> children() {
      return children;
    }

    public boolean isLeafNode() {
      return children == null || children.isEmpty();
    }

    public boolean isRootNode() {
      return isRoot;
    }

    private TreeNode<K> addChild(K newContent) {
      TreeNode<K> child = new TreeNode<>(newContent);
      addChildNode(child);
      return child;
    }

    private void addChildNode(TreeNode<K> childNode) {
      if (children == null) {
        children = new LinkedList<>();
      }
      childNode.isRoot = false;
      childNode.parent = this;
      children.add(childNode);
    }

    private void removeChildNode(TreeNode<K> childNode) {
      if (children != null && children.contains(childNode)) {
        children.remove(childNode);
      }
    }

    private void removeMyself() {
      parent.removeChildNode(this);
      if (children != null && !children.isEmpty()) {
        for (TreeNode<K> child : children) {
          parent.addChildNode(child);
        }
      }
    }

    @Override
    public String toString() {
      return "TreeNode{" + ", isRoot=" + isRoot + "content=" + content + ", childrenSize=" + (
          children == null ? 0 : children.size()) + '}';
    }
  }
}
