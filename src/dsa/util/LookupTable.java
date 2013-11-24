/*
    Copyright (c) 2006 [Joerg Ruedenauer]
  
    This file is part of Heldenverwaltung.

    Heldenverwaltung is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Heldenverwaltung is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package dsa.util;

import java.util.LinkedList;

abstract class Node {
  public abstract boolean isLeaf();

  public abstract Node nextNode(Character ch);

  public abstract String getItem(boolean remove);

  public abstract void appendNode(Node node, char ch);
}

class LeafNode extends Node {
  public boolean isLeaf() {
    return true;
  }

  public Node nextNode(Character ch) {
    return null;
  }

  public String getItem(boolean remove) {
    if (remove && removeAtGet)
      return items.poll();
    else
      return items.peek();
  }

  public void appendNode(Node node, char ch) {
  }

  public void addItem(String item) {
    items.addLast(item);
  }

  public LeafNode(String item, boolean printOnlyOnce) {
    this.items = new LinkedList<String>();
    addItem(item);
    this.removeAtGet = printOnlyOnce;
  }

  private LinkedList<String> items;

  private boolean removeAtGet;
}

class IntermediateNode extends Node {
  public boolean isLeaf() {
    return false;
  }

  public Node nextNode(Character ch) {
    return nextNodes.get(ch);
  }

  public String getItem(boolean remove) {
    return null;
  }

  public void appendNode(Node node, char ch) {
    nextNodes.put(new Character(ch), node);
  }

  public IntermediateNode() {
    nextNodes = new java.util.HashMap<Character, Node>();
  }

  private java.util.HashMap<Character, Node> nextNodes;
}

public class LookupTable {

  public LookupTable() {
    startNode = new IntermediateNode();
    triggerKey = null;
  }

  public LookupTable(char triggerKey) {
    startNode = new IntermediateNode();
    Node firstNode = new IntermediateNode();
    startNode.appendNode(firstNode, triggerKey);
    this.triggerKey = new Character(triggerKey);
  }

  public void clear() {
    startNode = new IntermediateNode();
  }

  public enum AddItemResult {
    OK, KeyDuplicate, KeyStartIsKey, KeyIncludesTriggerKey
  }

  public AddItemResult addItem(String key, String item) {
    return addItem(key, item, false);
  }

  public AddItemResult addItem(String key, String item, boolean printOnlyOnce) {
    if (triggerKey != null && key.indexOf(triggerKey.charValue()) != -1) {
      return AddItemResult.KeyIncludesTriggerKey;
    }
    Node currentNode = startNode;
    if (triggerKey != null) currentNode = startNode.nextNode(triggerKey);
    for (int i = 0; i < key.length() - 1; ++i) {
      if (currentNode.isLeaf()) return AddItemResult.KeyStartIsKey;
      char ch = key.charAt(i);
      Node nextNode = currentNode.nextNode(new Character(ch));
      if (nextNode == null) {
        nextNode = new IntermediateNode();
        currentNode.appendNode(nextNode, ch);
      }
      currentNode = nextNode;
    }
    if (currentNode.isLeaf()) return AddItemResult.KeyStartIsKey;
    char ch = key.charAt(key.length() - 1);
    Node nextNode = currentNode.nextNode(new Character(ch));
    if (nextNode != null) {
      if (nextNode.isLeaf()) {
        ((LeafNode) nextNode).addItem(item);
        return AddItemResult.KeyDuplicate;
      }
      else
        return AddItemResult.KeyStartIsKey;
    }
    else {
      nextNode = new LeafNode(item, printOnlyOnce);
      currentNode.appendNode(nextNode, ch);
      return AddItemResult.OK;
    }
  }

  public interface LookupPerformer {
    void restart();

    enum NextCharResult {
      Miss, Hit, Continue
    };

    NextCharResult nextChar(Character ch);

    String getItem();
  }

  private class LookupPerformerImpl implements LookupPerformer {

    public void restart() {
      currentNode = LookupTable.this.startNode;
    }

    public NextCharResult nextChar(Character ch) {
      if (currentNode == null) return NextCharResult.Miss;
      currentNode = currentNode.nextNode(ch);
      if (currentNode == null)
        return NextCharResult.Miss;
      else if (currentNode.isLeaf())
        return NextCharResult.Hit;
      else
        return NextCharResult.Continue;
    }

    public String getItem() {
      if (currentNode != null)
        return currentNode.getItem(true);
      else
        return null;
    }

    public LookupPerformerImpl() {
      restart();
    }

    private Node currentNode;
  }

  public LookupPerformer GetLookupPerformer() {
    return new LookupPerformerImpl();
  }

  private Node startNode;

  private Character triggerKey;
}