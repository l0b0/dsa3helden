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

abstract class AbstractNode {
  public abstract boolean isLeaf();

  public abstract AbstractNode nextNode(Character ch);

  public abstract String getItem(boolean remove);
  
  public abstract boolean isNumberItem();

  public abstract void appendNode(AbstractNode node, char ch);
}

class LeafNode extends AbstractNode {
  public boolean isLeaf() {
    return true;
  }

  public AbstractNode nextNode(Character ch) {
    return null;
  }

  public String getItem(boolean remove) {
    if (remove && removeAtGet)
      return items.poll();
    else
      return items.peek();
  }
  
  public boolean isNumberItem() {
	  return isNumber;
  }

  public void appendNode(AbstractNode node, char ch) {
    // leaf nodes do not append!
    assert(false);
  }

  public final void addItem(String item, boolean isNumber) {
    items.addLast(item);
    this.isNumber = isNumber;
  }
  
  public LeafNode(String item, boolean printOnlyOnce, boolean isNumber) {
    super();
    this.items = new LinkedList<String>();
    addItem(item, isNumber);
    this.removeAtGet = printOnlyOnce;
  }  

  private final LinkedList<String> items;
  
  private boolean isNumber;

  private final boolean removeAtGet;
}

class IntermediateNode extends AbstractNode {
  public boolean isLeaf() {
    return false;
  }

  public AbstractNode nextNode(Character ch) {
    return nextNodes.get(ch);
  }

  public String getItem(boolean remove) {
    return null;
  }
  
  public boolean isNumberItem() {
	  return false;
  }

  public void appendNode(AbstractNode node, char ch) {
    nextNodes.put(new Character(ch), node);
  }

  public IntermediateNode() {
    super();
    nextNodes = new java.util.HashMap<Character, AbstractNode>();
  }

  private final java.util.HashMap<Character, AbstractNode> nextNodes;
}

public class LookupTable {

  public LookupTable() {
    startNode = new IntermediateNode();
    triggerKey = null;
  }

  public LookupTable(char triggerKey) {
    startNode = new IntermediateNode();
    AbstractNode firstNode = new IntermediateNode();
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
  
  public AddItemResult addItem(String key, int item) {
    return addItem(key, item, false);
  }

  public AddItemResult addItem(String key, String item, boolean printOnlyOnce) {
    return addItem(key, item, printOnlyOnce, false);
  }
  
  public AddItemResult addItem(String key, int item, boolean printOnlyOnce) {
    return addItem(key, "" + item, printOnlyOnce, true);
  }
  
  public AddItemResult addItem(String key, float item) {
    return addItem(key, item, false);
  }
  
  public AddItemResult addItem(String key, float item, boolean printOnlyOnce) {
    return addItem(key, "" + item, printOnlyOnce, true);
  }
  
  private AddItemResult addItem(String key, String item, boolean printOnlyOnce, boolean isNumber) {
    if (triggerKey != null && key.indexOf(triggerKey.charValue()) != -1) {
      return AddItemResult.KeyIncludesTriggerKey;
    }
    AbstractNode currentNode = startNode;
    if (triggerKey != null) currentNode = startNode.nextNode(triggerKey);
    for (int i = 0; i < key.length() - 1; ++i) {
      if (currentNode.isLeaf()) return AddItemResult.KeyStartIsKey;
      char ch = key.charAt(i);
      AbstractNode nextNode = currentNode.nextNode(new Character(ch));
      if (nextNode == null) {
        nextNode = new IntermediateNode();
        currentNode.appendNode(nextNode, ch);
      }
      currentNode = nextNode;
    }
    if (currentNode.isLeaf()) return AddItemResult.KeyStartIsKey;
    char ch = key.charAt(key.length() - 1);
    AbstractNode nextNode = currentNode.nextNode(new Character(ch));
    if (nextNode != null) {
      if (nextNode.isLeaf()) {
        ((LeafNode) nextNode).addItem(item, isNumber);
        return AddItemResult.KeyDuplicate;
      }
      else
        return AddItemResult.KeyStartIsKey;
    }
    else {
      nextNode = new LeafNode(item, printOnlyOnce, isNumber);
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
    boolean isNumberItem();
  }

  private class LookupPerformerImpl implements LookupPerformer {

    public final void restart() {
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
    
    public boolean isNumberItem() {
      if (currentNode != null) 
        return currentNode.isNumberItem();
      else
        return false;
    }

    public LookupPerformerImpl() {
      restart();
    }

    private AbstractNode currentNode;
  }

  public LookupPerformer getLookupPerformer() {
    return new LookupPerformerImpl();
  }

  private AbstractNode startNode;

  private final Character triggerKey;
}
