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
package dsa.model.data;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

public class CharacterTypes {

  private static CharacterTypes instance = new CharacterTypes();

  public static CharacterTypes getInstance() {
    return instance;
  }

  public CharacterType getType(String typeName) {
    return characterTypes.get(typeName);
  }

  public TreeModel getAllTypes(boolean female) {
    TreeNode root = createTree(female ? femaleRoot : maleRoot);
    return new DefaultTreeModel(root, true);
  }

  private MutableTreeNode createTree(Node node) {
    if (node instanceof Type) {
      return new DefaultMutableTreeNode(node.getName(), false);
    }
    else {
      DefaultMutableTreeNode n = new DefaultMutableTreeNode(node.getName());
      for (Node x : ((Directory) node).getNodes()) {
        n.add(createTree(x));
      }
      return n;
    }
  }

  private CharacterTypes() {
  }

  private static class Node {
    private final String name;

    public String getName() {
      return name;
    }

    public Node(String name) {
      this.name = name;
    }
  }

  private static class Directory extends Node {
    public Directory(String name) {
      super(name);
    }

    private final java.util.ArrayList<Node> subNodes = new java.util.ArrayList<Node>();

    public void addNode(Node node) {
      subNodes.add(node);
    }

    public java.util.List<Node> getNodes() {
      return java.util.Collections.unmodifiableList(subNodes);
    }

    public void sort() {
      java.util.Collections.sort(subNodes, new NodeComparator());
      for (Node n : subNodes) {
        if (n instanceof Directory) {
          ((Directory) n).sort();
        }
      }
    }

    private static class NodeComparator implements Comparator<Node> {
      public int compare(Node o1, Node o2) {
        if (o1 instanceof Directory && !(o2 instanceof Directory)) {
          return -1;
        }
        else if (o2 instanceof Directory && !(o1 instanceof Directory)) {
          return 1;
        }
        else
          return o1.getName().compareTo(o2.getName());
      }

    }
  }

  private class Type extends Node {
    private final CharacterType type;

    public CharacterType getType() {
      return type;
    }

    public Type(String name, CharacterType type) {
      super(name);
      this.type = type;
    }
  }

  private Directory maleRoot = null;

  private Directory femaleRoot = null;

  private HashMap<String, CharacterType> characterTypes = new HashMap<String, CharacterType>();

  public void parseFiles(String directory) throws IOException {
    maleRoot = new Directory("Helden");
    femaleRoot = new Directory("Heldinnen");
    File file = new File(directory);
    if (!file.exists() || !file.isDirectory()) return;
    parseDirectory(file, maleRoot, femaleRoot);
    maleRoot.sort();
    femaleRoot.sort();
  }

  private void parseDirectory(File directory, Directory male, Directory female)
      throws IOException {
    File[] subFiles = directory.listFiles();
    for (File file : subFiles) {
      if (file.isDirectory()) {
        Directory m = new Directory(file.getName());
        Directory f = new Directory(file.getName());
        parseDirectory(file, m, f);
        male.addNode(m);
        female.addNode(f);
      }
      else if (file.getName().endsWith(".p42")) {
        CharacterType ct = new CharacterType(file);
        if (ct.isMalePossible()) {
          male.addNode(new Type(ct.getMaleName(), ct));
          characterTypes.put(ct.getMaleName(), ct);
        }
        if (ct.isFemalePossible()) {
          female.addNode(new Type(ct.getFemaleName(), ct));
          characterTypes.put(ct.getFemaleName(), ct);
        }
      }
    }
  }

}
