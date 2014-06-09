/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.util;

import java.util.Stack;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Utility class for dealing with XML DOM elements.
 *
 *
 * @author Mikkel Heisterberg, lekkim@lsdoc.org
 */
public class ElementUtil {

   /**
    * Constructs a XPath query to the supplied node.
    *
    * @param n
    * @return
    */
   public static String getXPath(Node n) {
      // abort early
      if (null == n) return null;

      // declarations
      Node parent = null;
      Stack hierarchy = new Stack();
      StringBuffer buffer = new StringBuffer();

      // push element on stack
      hierarchy.push(n);

      parent = n.getParentNode();
      while (null != parent && parent.getNodeType() != Node.DOCUMENT_NODE) {
         // push on stack
         hierarchy.push(parent);

         // get parent of parent
         parent = parent.getParentNode();
      }

      // construct xpath
      Object obj = null;
      while (!hierarchy.isEmpty() && null != (obj = hierarchy.pop())) {
         Node node = (Node) obj;
         boolean handled = false;

         // only consider elements
         if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element e = (Element) node;

            // is this the root element?
            if (buffer.length() == 0) {
               // root element - simply append element name
               buffer.append(node.getLocalName());
            } else {
               // child element - append slash and element name
               buffer.append("/");
               buffer.append(node.getLocalName());

               if (node.hasAttributes()) {
                  // see if the element has a name or id attribute
                  if (e.hasAttribute("id")) {
                     // id attribute found - use that
                     buffer.append("[@id='" + e.getAttribute("id") + "']");
                     handled = true;
                  } else if (e.hasAttribute("name")) {
                     // name attribute found - use that
                     buffer.append("[@name='" + e.getAttribute("name") + "']");
                     handled = true;
                  }
               }

               if (!handled) {
                  // no known attribute we could use - get sibling index
                  int prev_siblings = 1;
                  Node prev_sibling = node.getPreviousSibling();
                  while (null != prev_sibling) {
                     if (prev_sibling.getNodeType() == node.getNodeType()) {
                        if (prev_sibling.getLocalName().equalsIgnoreCase(node.getLocalName())) {
                           prev_siblings++;
                        }
                     }
                     prev_sibling = prev_sibling.getPreviousSibling();
                  }
                  buffer.append("[" + prev_siblings + "]");
               }
            }
         }
      }

      // return buffer
      return buffer.toString();
   }
}

