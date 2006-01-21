/* DefaultStyledDocument.java --
   Copyright (C) 2004, 2005 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package javax.swing.text;

import java.awt.Color;
import java.awt.Font;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;

/**
 * The default implementation of {@link StyledDocument}.
 *
 * The document is modeled as an {@link Element} tree, which has
 * a {@link SectionElement} as single root, which has one or more
 * {@link AbstractDocument.BranchElement}s as paragraph nodes
 * and each paragraph node having one or more
 * {@link AbstractDocument.LeafElement}s as content nodes.
 *
 * @author Michael Koch (konqueror@gmx.de)
 * @author Roman Kennke (roman@kennke.org)
 */
public class DefaultStyledDocument extends AbstractDocument
  implements StyledDocument
{
  
  /**
   * An {@link UndoableEdit} that can undo attribute changes to an element.
   *
   * @author Roman Kennke (kennke@aicas.com)
   */
  public static class AttributeUndoableEdit
    extends AbstractUndoableEdit
  {
    /**
     * A copy of the old attributes.
     */
    protected AttributeSet copy;

    /**
     * The new attributes.
     */
    protected AttributeSet newAttributes;

    /**
     * If the new attributes replaced the old attributes or if they only were
     * added to them.
     */
    protected boolean isReplacing;

    /**
     * The element that has changed.
     */
    protected Element element;

    /**
     * Creates a new <code>AttributeUndoableEdit</code>.
     *
     * @param el the element that changes attributes
     * @param newAtts the new attributes
     * @param replacing if the new attributes replace the old or only append to
     *        them
     */
    public AttributeUndoableEdit(Element el, AttributeSet newAtts,
                                 boolean replacing)
    {
      element = el;
      newAttributes = newAtts;
      isReplacing = replacing;
      copy = el.getAttributes().copyAttributes();
    }

    /**
     * Undos the attribute change. The <code>copy</code> field is set as
     * attributes on <code>element</code>.
     */
    public void undo()
    {
      super.undo();
      AttributeSet atts = element.getAttributes();
      if (atts instanceof MutableAttributeSet)
        {
          MutableAttributeSet mutable = (MutableAttributeSet) atts;
          mutable.removeAttributes(atts);
          mutable.addAttributes(copy);
        }
    }

    /**
     * Redos an attribute change. This adds <code>newAttributes</code> to the
     * <code>element</code>'s attribute set, possibly clearing all attributes
     * if <code>isReplacing</code> is true.
     */
    public void redo()
    {
      super.undo();
      AttributeSet atts = element.getAttributes();
      if (atts instanceof MutableAttributeSet)
        {
          MutableAttributeSet mutable = (MutableAttributeSet) atts;
          if (isReplacing)
            mutable.removeAttributes(atts);
          mutable.addAttributes(newAttributes);
        }
    }
  }

  /**
   * Carries specification information for new {@link Element}s that should
   * be created in {@link ElementBuffer}. This allows the parsing process
   * to be decoupled from the <code>Element</code> creation process.
   */
  public static class ElementSpec
  {
    /**
     * This indicates a start tag. This is a possible value for
     * {@link #getType}.
     */
    public static final short StartTagType = 1;

    /**
     * This indicates an end tag. This is a possible value for
     * {@link #getType}.
     */
    public static final short EndTagType = 2;

    /**
     * This indicates a content element. This is a possible value for
     * {@link #getType}.
     */
    public static final short ContentType = 3;

    /**
     * This indicates that the data associated with this spec should be joined
     * with what precedes it. This is a possible value for
     * {@link #getDirection}.
     */
    public static final short JoinPreviousDirection = 4;

    /**
     * This indicates that the data associated with this spec should be joined
     * with what follows it. This is a possible value for
     * {@link #getDirection}.
     */
    public static final short JoinNextDirection = 5;

    /**
     * This indicates that the data associated with this spec should be used
     * to create a new element. This is a possible value for
     * {@link #getDirection}.
     */
    public static final short OriginateDirection = 6;

    /**
     * This indicates that the data associated with this spec should be joined
     * to the fractured element. This is a possible value for
     * {@link #getDirection}.
     */
    public static final short JoinFractureDirection = 7;

    /**
     * The type of the tag.
     */
    short type;

    /**
     * The direction of the tag.
     */
    short direction;

    /**
     * The offset of the content.
     */
    int offset;

    /**
     * The length of the content.
     */
    int length;

    /**
     * The actual content.
     */
    char[] content;

    /**
     * The attributes for the tag.
     */
    AttributeSet attributes;

    /**
     * Creates a new <code>ElementSpec</code> with no content, length or
     * offset. This is most useful for start and end tags.
     *
     * @param a the attributes for the element to be created
     * @param type the type of the tag
     */
    public ElementSpec(AttributeSet a, short type)
    {
      this(a, type, 0);
    }

    /**
     * Creates a new <code>ElementSpec</code> that specifies the length but
     * not the offset of an element. Such <code>ElementSpec</code>s are
     * processed sequentially from a known starting point.
     *
     * @param a the attributes for the element to be created
     * @param type the type of the tag
     * @param len the length of the element
     */
    public ElementSpec(AttributeSet a, short type, int len)
    {
      this(a, type, null, 0, len);
    }
 
    /**
     * Creates a new <code>ElementSpec</code> with document content.
     *
     * @param a the attributes for the element to be created
     * @param type the type of the tag
     * @param txt the actual content
     * @param offs the offset into the <code>txt</code> array
     * @param len the length of the element
     */
    public ElementSpec(AttributeSet a, short type, char[] txt, int offs,
                       int len)
    {
      attributes = a;
      this.type = type;
      offset = offs;
      length = len;
      content = txt;
      direction = OriginateDirection;
    }

    /**
     * Sets the type of the element.
     *
     * @param type the type of the element to be set
     */
    public void setType(short type)
    {
      this.type = type;
    }

    /**
     * Returns the type of the element.
     *
     * @return the type of the element
     */
    public short getType()
    {
      return type;
    }

    /**
     * Sets the direction of the element.
     *
     * @param dir the direction of the element to be set
     */
    public void setDirection(short dir)
    {
      direction = dir;
    }

    /**
     * Returns the direction of the element.
     *
     * @return the direction of the element
     */
    public short getDirection()
    {
      return direction;
    }

    /**
     * Returns the attributes of the element.
     *
     * @return the attributes of the element
     */
    public AttributeSet getAttributes()
    {
      return attributes;
    }

    /**
     * Returns the actual content of the element.
     *
     * @return the actual content of the element
     */
    public char[] getArray()
    {
      return content;
    }

    /**
     * Returns the offset of the content.
     *
     * @return the offset of the content
     */
    public int getOffset()
    {
      return offset;
    }

    /**
     * Returns the length of the content.
     *
     * @return the length of the content
     */
    public int getLength()
    {
      return length;
    }

    /**
     * Returns a String representation of this <code>ElementSpec</code>
     * describing the type, direction and length of this
     * <code>ElementSpec</code>.
     *
     * @return a String representation of this <code>ElementSpec</code>
     */
    public String toString()
    {
      StringBuilder b = new StringBuilder();
      switch (type)
        {
        case StartTagType:
          b.append("StartTag");
          break;
        case EndTagType:
          b.append("EndTag");
          break;
        case ContentType:
          b.append("Content");
          break;
        default:
          b.append("??");
          break;
        }

      b.append(':');

      switch (direction)
        {
        case JoinPreviousDirection:
          b.append("JoinPrevious");
          break;
        case JoinNextDirection:
          b.append("JoinNext");
          break;
        case OriginateDirection:
          b.append("Originate");
          break;
        case JoinFractureDirection:
          b.append("Fracture");
          break;
        default:
          b.append("??");
          break;
        }

      b.append(':');
      b.append(length);

      return b.toString();
    }
  }

  /**
   * Performs all <em>structural</code> changes to the <code>Element</code>
   * hierarchy.  This class was implemented with much help from the document:
   * http://java.sun.com/products/jfc/tsc/articles/text/element_buffer/index.html.
   */
  public class ElementBuffer implements Serializable
  {
    /** The serialization UID (compatible with JDK1.5). */
    private static final long serialVersionUID = 1688745877691146623L;

    /** The root element of the hierarchy. */
    private Element root;

    /** Holds the offset for structural changes. */
    private int offset;

    /** Holds the length of structural changes. */
    private int length;

    /** Holds the end offset for structural changes. **/
    private int endOffset;

    /**
     * The current position in the element tree. This is used for bulk inserts
     * using ElementSpecs.
     */
    private Stack elementStack;

    /**
     * The ElementChange that describes the latest changes.
     */
    DefaultDocumentEvent documentEvent;

    /**
     * Creates a new <code>ElementBuffer</code> for the specified
     * <code>root</code> element.
     *
     * @param root the root element for this <code>ElementBuffer</code>
     */
    public ElementBuffer(Element root)
    {
      this.root = root;
      elementStack = new Stack();
    }

    /**
     * Returns the root element of this <code>ElementBuffer</code>.
     *
     * @return the root element of this <code>ElementBuffer</code>
     */
    public Element getRootElement()
    {
      return root;
    }

    /**
     * Updates the element structure of the document in response to removal of
     * content. It removes the affected {@link Element}s from the document
     * structure.
     *
     * This method sets some internal parameters and delegates the work
     * to {@link #removeUpdate}.
     *
     * @param offs the offset from which content is remove
     * @param len the length of the removed content
     * @param ev the document event that records the changes
     */
    public void remove(int offs, int len, DefaultDocumentEvent ev)
    {
      offset = offs;
      length = len;
      documentEvent = ev;
      removeUpdate();
    }

    /**
     * Updates the element structure of the document in response to removal of
     * content. It removes the affected {@link Element}s from the document
     * structure.
     */
    protected void removeUpdate()
    {
      int startParagraph = root.getElementIndex(offset);
      int endParagraph = root.getElementIndex(offset + length);
      Element[] empty = new Element[0];
      int removeStart = -1;
      int removeEnd = -1;
      for (int i = startParagraph;  i < endParagraph; i++)
        {
          BranchElement paragraph = (BranchElement) root.getElement(i);
          int contentStart = paragraph.getElementIndex(offset);
          int contentEnd = paragraph.getElementIndex(offset + length);
          if (contentStart == paragraph.getStartOffset()
              && contentEnd == paragraph.getEndOffset())
            {
              // In this case we only need to remove the whole paragraph. We
              // do this in one go after this loop and only record the indices
              // here.
              if (removeStart == -1)
                {
                  removeStart = i;
                  removeEnd = i;
                }
              else
                removeEnd = i;
            }
          else
            {
              // In this case we remove a couple of child elements from this
              // paragraph.
              int removeLen = contentEnd - contentStart;
              Element[] removed = new Element[removeLen];
              for (int j = contentStart; j < contentEnd; j++)
                removed[j] = paragraph.getElement(j);
              Edit edit = getEditForParagraphAndIndex(paragraph, contentStart);
              edit.addRemovedElements(removed);
            }
        }
      // Now we remove paragraphs from the root that have been tagged for
      // removal.
      if (removeStart != -1)
        {
          int removeLen = removeEnd - removeStart;
          Element[] removed = new Element[removeLen];
          for (int i = removeStart; i < removeEnd; i++)
            removed[i] = root.getElement(i);
          Edit edit = getEditForParagraphAndIndex((BranchElement)root, removeStart);
          edit.addRemovedElements(removed);
        }
    }

    /**
     * Modifies the element structure so that the specified interval starts
     * and ends at an element boundary. Content and paragraph elements
     * are split and created as necessary.
     *
     * This also updates the <code>DefaultDocumentEvent</code> to reflect the
     * structural changes.
     *
     * The bulk work is delegated to {@link #changeUpdate()}.
     *
     * @param offset the start index of the interval to be changed
     * @param length the length of the interval to be changed
     * @param ev the <code>DefaultDocumentEvent</code> describing the change
     */
    public void change(int offset, int length, DefaultDocumentEvent ev)
    {
      this.offset = offset;
      this.length = length;
      documentEvent = ev;
      changeUpdate();
    }

    /**
     * Performs the actual work for {@link #change}.
     * The elements at the interval boundaries are split up (if necessary)
     * so that the interval boundaries are located at element boundaries.
     */
    protected void changeUpdate()
    {
      // Split up the element at the start offset if necessary.
      Element el = getCharacterElement(offset);
      Element[] res = split(el, offset, 0);
      BranchElement par = (BranchElement) el.getParentElement();
      if (res[1] != null)
        {
          int index = par.getElementIndex(offset);
          Element[] removed;
          Element[] added;
          if (res[0] == null)
            {
              removed = new Element[0];
              added = new Element[]{ res[1] };
              index++;
            }
          else
            {
              removed = new Element[]{ el };
              added = new Element[]{ res[0], res[1] };
            }
          Edit edit = getEditForParagraphAndIndex(par, index);
          edit.addRemovedElements(removed);
          edit.addAddedElements(added);
        }

      int endOffset = offset + length;
      el = getCharacterElement(endOffset);
      res = split(el, endOffset, 0);
      par = (BranchElement) el.getParentElement();
      if (res[1] != null)
        {
          int index = par.getElementIndex(offset);
          Element[] removed;
          Element[] added;
          if (res[1] == null)
            {
              removed = new Element[0];
              added = new Element[]{ res[1] };
            }
          else
            {
              removed = new Element[]{ el };
              added = new Element[]{ res[0], res[1] };
            }
          Edit edit = getEditForParagraphAndIndex(par, index);
          edit.addRemovedElements(removed);
          edit.addAddedElements(added);
        }
    }

    /**
     * Splits an element if <code>offset</code> is not already at its boundary.
     *
     * @param el the Element to possibly split
     * @param offset the offset at which to possibly split
     * @param space the amount of space to create between the splitted parts
     *
     * @return An array of elements which represent the split result. This
     *         array has two elements, the two parts of the split. The first
     *         element might be null, which means that the element which should
     *         be splitted can remain in place. The second element might also
     *         be null, which means that the offset is already at an element
     *         boundary and the element doesn't need to be splitted.
     *          
     */
    private Element[] split(Element el, int offset, int space)
    {
      // If we are at an element boundary, then return an empty array.
      if ((offset == el.getStartOffset() || offset == el.getEndOffset())
          && space == 0 && el.isLeaf())
        return new Element[2];

      // If the element is an instance of BranchElement, then we recursivly
      // call this method to perform the split.
      Element[] res = new Element[2];
      if (el instanceof BranchElement)
        {
          int index = el.getElementIndex(offset);
          Element child = el.getElement(index);
          Element[] result = split(child, offset, space);
          Element[] removed;
          Element[] added;
          Element[] newAdded;

          int count = el.getElementCount();
          if (!(result[1] == null))
            {
              // This is the case when we can keep the first element.
              if (result[0] == null)
                {
                  removed = new Element[count - index - 1];
                  newAdded = new Element[count - index - 1];
                  added = new Element[]{};
		}
              // This is the case when we may not keep the first element.
              else
                {
                  removed = new Element[count - index];
                  newAdded = new Element[count - index];
                  added = new Element[]{result[0]};
                }
              newAdded[0] = result[1];
              for (int i = index; i < count; i++)
                {
                  Element el2 = el.getElement(i);
                  int ind = i - count + removed.length;
                  removed[ind] = el2;
                  if (ind != 0)
                    newAdded[ind] = el2;
                }

              Edit edit = getEditForParagraphAndIndex((BranchElement)el, index);
              edit.addRemovedElements(removed);
              edit.addAddedElements(added);
              BranchElement newPar =
                (BranchElement) createBranchElement(el.getParentElement(),
                                                    el.getAttributes());
              Edit edit2 = getEditForParagraphAndIndex(newPar, 0);
              edit2.addAddedElements(newAdded);
              res = new Element[]{ null, newPar };
	}
      else
            {
              removed = new Element[count - index];
              for (int i = index; i < count; ++i)
                removed[i - index] = el.getElement(i);
              added = new Element[0];
              Edit edit = getEditForParagraphAndIndex((BranchElement)el, index);
              edit.addRemovedElements(removed);
              edit.addAddedElements(added);
              BranchElement newPar =
                (BranchElement) createBranchElement(el.getParentElement(),
                                                    el.getAttributes());
              Edit edit2 = getEditForParagraphAndIndex(newPar, 0);
              edit2.addAddedElements(removed);
              res = new Element[]{ null, newPar };
            }
        }
      else if (el instanceof LeafElement)
        {
          BranchElement par = (BranchElement) el.getParentElement();
          Element el1 = createLeafElement(par, el.getAttributes(),
                                          el.getStartOffset(), offset);
          Element el2 = createLeafElement(par, el.getAttributes(),
                                          offset + space, el.getEndOffset());
          res = new Element[]{ el1, el2 };
        }
      return res;
    }

    /**
     * Inserts new <code>Element</code> in the document at the specified
     * position.
     *
     * Most of the work is done by {@link #insertUpdate}, after some fields
     * have been prepared for it.
     *
     * @param offset the location in the document at which the content is
     *        inserted
     * @param length the length of the inserted content
     * @param data the element specifications for the content to be inserted
     * @param ev the document event that is updated to reflect the structural
     *        changes
     */
    public void insert(int offset, int length, ElementSpec[] data,
                       DefaultDocumentEvent ev)
    {
      if (length == 0)
        return;
      this.offset = offset;
      this.length = length;
      this.endOffset = offset + length;
      documentEvent = ev;
      // Push the root and the paragraph at offset onto the element stack.
      edits.clear();
      elementStack.clear();
      Element current = root;
      int index;
      while (!current.isLeaf())
        {
          index = current.getElementIndex(offset);
          elementStack.push(current);
          current = current.getElement(index);
        }
      insertUpdate(data);
      
      // This for loop applies all the changes that were made and updates the 
      // DocumentEvent.
      int size = edits.size();
      for (int i = 0; i < size; i++)
        {
          Edit curr = (Edit) edits.get(i);
          BranchElement e = (BranchElement) curr.e;
          Element[] removed = curr.getRemovedElements();
          Element[] added = curr.getAddedElements();
          e.replace(curr.index, removed.length, added);
          addEdit(e, curr.index, removed, added);
        }
    }

    /**
     * Performs the actual structural change for {@link #insert}. This
     * creates a bunch of {@link Edit}s as specified by <code>data</code>
     * and saves them in the edits Vector to be inserted at the end of the 
     * {@link #insert} method.
     *
     * @param data the element specifications for the elements to be inserte
     */
    protected void insertUpdate(ElementSpec[] data)
    {
      int i = 0;
      if (data[0].getType() == ElementSpec.ContentType)
        {
          // If the first tag is content we must treat it separately to allow
          // for joining properly to previous Elements and to ensure that
          // no extra LeafElements are erroneously inserted.
          i = 1;
          insertFirstContentTag(data);
        }
      else
        createFracture(data);
      
      // Handle each ElementSpec individually.
      for (; i < data.length; i++)
        {
          BranchElement paragraph = (BranchElement) elementStack.peek();
          switch (data[i].getType())
            {
            case ElementSpec.StartTagType:
              switch (data[i].getDirection())
                {
                case ElementSpec.JoinFractureDirection:
                  // Fracture the tree and ensure the appropriate element
                  // is on top of the stack.
                  insertFracture(data[i]);
              break;
                case ElementSpec.JoinNextDirection:
                  // Push the next paragraph element onto the stack so 
                  // future insertions are added to it.
                  int index = paragraph.getElementIndex(offset);
                  elementStack.push(paragraph.getElement(index));
                  break;
                case ElementSpec.OriginateDirection:
                  // Create a new paragraph and push it onto the stack.
                  Element current = (Element) elementStack.peek();
                  Element newParagraph =
                    insertParagraph((BranchElement) current, offset);
                  elementStack.push(newParagraph);
              break;
            default:
              break;
            }
              break;
            case ElementSpec.EndTagType:
          elementStack.pop();
              break;
            case ElementSpec.ContentType:
              insertContentTag(data[i]);
              break;
            }
        }
    }

    /**
     * This method fractures the child at offset.
     * @param data the ElementSpecs used for the entire insertion
     */
    private void createFracture(ElementSpec[] data)
    {
      // FIXME: This method is not complete.  We must handle the elementStack
      // properly and make sure the appropriate Elements are pushed onto the 
      // top of the stack so future inserts go to the appropriate paragraph.
      BranchElement paragraph = (BranchElement)elementStack.peek();
      int index = paragraph.getElementIndex(offset);
      Element child = paragraph.getElement(index);
      Edit edit = getEditForParagraphAndIndex(paragraph, index);
      if (offset != 0)
        {
          Element newEl1 = createLeafElement(paragraph, child.getAttributes(), child.getStartOffset(), offset);
          edit.addAddedElement(newEl1);
        }
      edit.addRemovedElement(child);
    }
    
    private Element insertParagraph(BranchElement par, int offset)
    {
      Element current = par.getElement(par.getElementIndex(offset));
      Element[] res = split(current, offset, 0);
      int index = par.getElementIndex(offset);
      Element ret;
      if (res[1] != null)
        {
          Element[] removed;
          Element[] added;
          if (res[0] == null)
            {
              removed = new Element[0];
              if (res[1] instanceof BranchElement)
                {
                  added = new Element[]{ res[1] };
                  ret = res[1];
                }
              else
                {
                  ret = createBranchElement(par, null);
                  added = new Element[]{ ret, res[1] };
                }
              index++;
            }
          else
            {
              removed = new Element[]{ current };
              if (res[1] instanceof BranchElement)
                {
                  ret = res[1];
                  added = new Element[]{ res[0], res[1] };
                }
              else
                {
                  ret = createBranchElement(par, null);
                  added = new Element[]{ res[0], ret, res[1] };
                }
            }
          Edit edit = getEditForParagraphAndIndex(par, index);
          edit.addRemovedElements(removed);
          edit.addAddedElements(added);
        }
      else
        {
          ret = createBranchElement(par, null);
          Edit edit = getEditForParagraphAndIndex(par, index);
          edit.addAddedElement(ret);
        }
      return ret;
    }

    private void insertFirstContentTag(ElementSpec[] data)
    {      
      // FIXME: This method is not complete.  It needs to properly recreate the
      // leaves when the spec's direction is JoinPreviousDirection.
      ElementSpec first = data[0];
      BranchElement paragraph = (BranchElement) elementStack.peek();
      int index = paragraph.getElementIndex(offset);
      Element current = paragraph.getElement(index);
      int newEndOffset = offset + first.length;
      Edit edit = getEditForParagraphAndIndex(paragraph, index);
      switch (first.getDirection())
        {
        case ElementSpec.JoinPreviousDirection:
          if (current.getEndOffset() != newEndOffset)
            {
              Element newEl1 = createLeafElement(paragraph, current.getAttributes(), current.getStartOffset(), newEndOffset);              
              edit.addRemovedElement(current);
              edit.addAddedElement(newEl1);
              if (current.getEndOffset() != newEndOffset)
                {
                  // This means all the leaves that were there previously need
                  // to be recreated after all the new Elements are inserted.
                }
            }
          break;
        case ElementSpec.JoinNextDirection:
          if (offset != 0)
            {
              Element next = paragraph.getElement(index + 1);
              Element[] removed = new Element[] { current, next };
              Element newEl1 = createLeafElement(paragraph, current.getAttributes(), current.getStartOffset(), offset);
              Element[] added = new Element[2];
              added[0] = newEl1;
              if (data.length == 1)
                added[1] = createLeafElement(paragraph, next.getAttributes(), offset, next.getEndOffset());
              else
                added[1] = createLeafElement(paragraph, next.getAttributes(), offset, newEndOffset);
              edit.addRemovedElements(removed);
              edit.addAddedElements(added);
            }
          break;
        case ElementSpec.OriginateDirection:
          if (current.getStartOffset() != offset)
            {
              Element newEl1 = createLeafElement(paragraph, current.getAttributes(), current.getStartOffset(), offset);
              edit.addAddedElement(newEl1);
            }
          Element newEl2 = createLeafElement(paragraph, first.getAttributes(), offset, newEndOffset);
          edit.addRemovedElement(current);
          edit.addAddedElement(newEl2);
          if (current.getEndOffset() != endOffset && (data.length == 1))
            {
              // This means all the leaves that were there previously need
              // to be recreated after all the new Elements are inserted.
              Element newCurrent = createLeafElement(paragraph, current.getAttributes(), newEndOffset, current.getEndOffset());
              edit.addAddedElement(newCurrent);
            }            
          break;
        default:
          break;
        }
      offset = newEndOffset; 
    }
    
    /**
     * Inserts a fracture into the document structure.
     * 
     * @param tag - the element spec.
     */
    private void insertFracture(ElementSpec tag)
    {
      // FIXME: This method may be incomplete.  We must make sure the 
      // appropriate edits were added and the correct paragraph element
      // is pushed onto the top of the elementStack so future inserts go 
      // to the right paragraph.
      
      // This is the parent of the paragraph about to be fractured.  We will
      // create a new child of this parent.
      BranchElement parent = (BranchElement) elementStack.peek();
      int parentIndex = parent.getElementIndex(offset);
      
      // This is the old paragraph.  We must remove all its children that 
      // occur after offset and move them to a new paragraph.  We must
      // also recreate its child that occurs at offset to have the proper
      // end offset.  The remainder of this child will also go in the new
      // paragraph.
      BranchElement previous = (BranchElement) parent.getElement(parentIndex);
      
      // This is the new paragraph.
      BranchElement newBranch = 
        (BranchElement) createBranchElement(parent, previous.getAttributes());
      
      
      // The steps we must take to properly fracture are:
      // 1. Recreate the LeafElement at offset to have the correct end offset.
      // 2. Create a new LeafElement with the remainder of the LeafElement in 
      //    #1 ==> this is whatever was in that LeafElement to the right of the
      //    inserted newline.
      // 3. Find the paragraph at offset and remove all its children that 
      //    occur _after_ offset.  These will be moved to the newly created
      //    paragraph.
      // 4. Move the LeafElement created in #2 and all the LeafElements removed
      //    in #3 to the newly created paragraph.
      // 5. Add the new paragraph to the parent.
      int previousIndex = previous.getElementIndex(offset);
      int numReplaced = previous.getElementCount() - previousIndex;
      Element previousLeaf = previous.getElement(previousIndex);
      AttributeSet prevLeafAtts = previous.getAttributes();
      
      // This recreates the child at offset to have the proper end offset.  
      // (Step 1).
      Element newPreviousLeaf = 
        createLeafElement(previous, 
                          prevLeafAtts, previousLeaf.getStartOffset(), 
                          offset);
      // This creates the new child, which is the remainder of the old child.  
      // (Step 2).
      
      Element firstLeafInNewBranch = 
        createLeafElement(newBranch, prevLeafAtts, 
                          offset, previousLeaf.getEndOffset());
      
      // Now we move the new LeafElement and all the old children that occurred
      // after the offset to the new paragraph.  (Step 4).
      Element[] newLeaves = new Element[numReplaced];
      newLeaves[0] = firstLeafInNewBranch;
      for (int i = 1; i < numReplaced; i++)
        newLeaves[i] = previous.getElement(previousIndex + i);
      newBranch.replace(0, 0, newLeaves);
            
      // Now we remove the children after the offset from the previous 
      // paragraph. (Step 3).
      int removeSize = previous.getElementCount() - previousIndex;
      Element[] remove = new Element[removeSize];
      for (int j = 0; j < removeSize; j++)
        remove[j] = previous.getElement(previousIndex + j);
      Edit edit = getEditForParagraphAndIndex(previous, previousIndex);
      edit.addRemovedElements(remove);
      
      // Finally we add the new paragraph to the parent. (Step 5).
      int index = parentIndex + 1;
      Edit edit2 = getEditForParagraphAndIndex(parent, index);
      edit2.addAddedElement(newBranch);
      elementStack.push(newBranch);
    }
    
    /**
     * Inserts a content element into the document structure.
     *
     * @param tag the element spec
     */
    private void insertContentTag(ElementSpec tag)
    {
      int len = tag.getLength();
      int dir = tag.getDirection();
      AttributeSet tagAtts = tag.getAttributes();
      if (dir == ElementSpec.JoinPreviousDirection)
        {
          // The mauve tests to this class show that a JoinPrevious insertion
          // does not add any edits to the document event. To me this means
          // that nothing is done here. The previous element naturally should
          // expand so that it covers the new characters.
        }
      else if (dir == ElementSpec.JoinNextDirection)
        {
          // FIXME:
          // Have to handle JoinNext differently depending on whether
          // or not it comes after a fracture.  If comes after a fracture, 
          // the insertFracture method takes care of everything and nothing
          // needs to be done here.  Otherwise, we need to adjust the
          // Element structure.  For now, I check if the elementStack's 
          // top Element is the immediate parent of the LeafElement at
          // offset - if so, we did not come immediately after a 
          // fracture.  This seems awkward and should probably be improved.
          // We may be doing too much in insertFracture because we are 
          // adjusting the offsets, the correct thing to do may be to 
          // create a new branch element and push it on to element stack
          // and then this method here can be more general.

          BranchElement paragraph = (BranchElement) elementStack.peek();
          int index = paragraph.getElementIndex(offset);
          Element target = paragraph.getElement(index);
          if (target.isLeaf() && paragraph.getElementCount() > (index + 1))
            {
              Element next = paragraph.getElement(index + 1);
          Element newEl1 = createLeafElement(paragraph,
                                                 target.getAttributes(),
                                                 target.getStartOffset(),
                                             offset);
          Element newEl2 = createLeafElement(paragraph,
                                                 next.getAttributes(), offset,
                                           next.getEndOffset());
              Edit edit = getEditForParagraphAndIndex(paragraph, index);
              edit.addRemovedElement(target);
              edit.addRemovedElement(next);
              edit.addAddedElement (newEl1);
              edit.addAddedElement (newEl2);
        }
        }
      else if (dir == ElementSpec.OriginateDirection)
        {
          BranchElement paragraph = (BranchElement) elementStack.peek();
          int index = paragraph.getElementIndex(offset);
          Element current = paragraph.getElement(index);

          Element[] added;
          Element[] removed = new Element[] {current};
          Element[] splitRes = split(current, offset, length);
          if (splitRes[0] == null)
            {
              added = new Element[2];
              added[0] = createLeafElement(paragraph, tagAtts,
                                           offset, endOffset);
              added[1] = splitRes[1];
              removed = new Element[0];
              index++;
            }
          else if (current.getStartOffset() == offset)
            {
              // This is if the new insertion happens immediately before 
              // the <code>current</code> Element.  In this case there are 2 
              // resulting Elements.              
              added = new Element[2];
              added[0] = createLeafElement(paragraph, tagAtts, offset,
                                           endOffset);
              added[1] = splitRes[1];
            }
          else if (current.getEndOffset() == endOffset)
            {
              // This is if the new insertion happens right at the end of 
              // the <code>current</code> Element.  In this case there are 
              // 2 resulting Elements.
              added = new Element[2];
              added[0] = splitRes[0];
              added[1] = createLeafElement(paragraph, tagAtts, offset,
                                           endOffset);
            }
          else
            {
              // This is if the new insertion is in the middle of the 
              // <code>current</code> Element.  In this case 
              // there will be 3 resulting Elements.
              added = new Element[3];
              added[0] = splitRes[0];
              added[1] = createLeafElement(paragraph, tagAtts, offset,
                                           endOffset);
              added[2] = splitRes[1];
            }
          Edit edit = getEditForParagraphAndIndex(paragraph, index);
          edit.addRemovedElements(removed);
          edit.addAddedElements(added);
        }
      offset += len;
    }
    
    /**
     * Creates a copy of the element <code>clonee</code> that has the parent
     * <code>parent</code>.
     * @param parent the parent of the newly created Element
     * @param clonee the Element to clone
     * @return the cloned Element
     */
    public Element clone (Element parent, Element clonee)
    {
      // If the Element we want to clone is a leaf, then simply copy it
      if (clonee.isLeaf())
        return createLeafElement(parent, clonee.getAttributes(),
                                 clonee.getStartOffset(), clonee.getEndOffset());
      
      // Otherwise create a new BranchElement with the desired parent and 
      // the clonee's attributes
      BranchElement result = (BranchElement) createBranchElement(parent, clonee.getAttributes());
      
      // And clone all the of clonee's children
      Element[] children = new Element[clonee.getElementCount()];
      for (int i = 0; i < children.length; i++)
        children[i] = clone(result, clonee.getElement(i));
      
      // Make the cloned children the children of the BranchElement
      result.replace(0, 0, children);
      return result;
    }

    /**
     * Adds an ElementChange for a given element modification to the document
     * event. If there already is an ElementChange registered for this element,
     * this method tries to merge the ElementChanges together. However, this
     * is only possible if the indices of the new and old ElementChange are
     * equal.
     *
     * @param e the element
     * @param i the index of the change
     * @param removed the removed elements, or <code>null</code>
     * @param added the added elements, or <code>null</code>
     */
    private void addEdit(Element e, int i, Element[] removed, Element[] added)
    {
      // Perform sanity check first.
      DocumentEvent.ElementChange ec = documentEvent.getChange(e);

      // Merge the existing stuff with the new stuff.
      Element[] oldAdded = ec == null ? null: ec.getChildrenAdded();
      Element[] newAdded;
      if (oldAdded != null && added != null)
        {
          if (ec.getIndex() <= i)
            {
              int index = i - ec.getIndex();
              // Merge adds together.
              newAdded = new Element[oldAdded.length + added.length];
              System.arraycopy(oldAdded, 0, newAdded, 0, index);
              System.arraycopy(added, 0, newAdded, index, added.length);
              System.arraycopy(oldAdded, index, newAdded, index + added.length,
                               oldAdded.length - index);
              i = ec.getIndex();
            }
          else
            throw new AssertionError("Not yet implemented case.");
        }
      else if (added != null)
        newAdded = added;
      else if (oldAdded != null)
        newAdded = oldAdded;
      else
        newAdded = new Element[0];

      Element[] oldRemoved = ec == null ? null: ec.getChildrenRemoved();
      Element[] newRemoved;
      if (oldRemoved != null && removed != null)
        {
          if (ec.getIndex() <= i)
            {
              int index = i - ec.getIndex();
              // Merge removes together.
              newRemoved = new Element[oldRemoved.length + removed.length];
              System.arraycopy(oldAdded, 0, newRemoved, 0, index);
              System.arraycopy(removed, 0, newRemoved, index, removed.length);
              System.arraycopy(oldRemoved, index, newRemoved,
                               index + removed.length,
                               oldRemoved.length - index);
              i = ec.getIndex();
            }
          else
            throw new AssertionError("Not yet implemented case.");
        }
      else if (removed != null)
        newRemoved = removed;
      else if (oldRemoved != null)
        newRemoved = oldRemoved;
      else
        newRemoved = new Element[0];

      // Replace the existing edit for the element with the merged.
      documentEvent.addEdit(new ElementEdit(e, i, newRemoved, newAdded));
    }
    
    /** 
     * Instance of all editing information for an object in the Vector.  This
     * class is used to add information to the DocumentEvent associated with
     * an insertion/removal/change as well as to store the changes that need 
     * to be made so they can be made all at the same (appropriate) time.
     */
    class Edit
    {
      /** The element to edit . */
      Element e;
      
      /** The index of the change. */
      int index;
      
      /** The removed elements. */
      Vector removed = new Vector();
      
      /** The added elements. */
      Vector added = new Vector();
      
      /**
       * Return an array containing the Elements that have been removed
       * from the paragraph associated with this Edit.
       * @return an array of removed Elements
       */
      public Element[] getRemovedElements()
      {
        int size = removed.size();
        Element[] removedElements = new Element[size];
        for (int i = 0; i < size; i++)
          removedElements[i] = (Element) removed.elementAt(i);
        return removedElements;
      }
      
      /**
       * Return an array containing the Elements that have been added to the 
       * paragraph associated with this Edit.
       * @return an array of added Elements
       */
      public Element[] getAddedElements()
      {
        int size = added.size();
        Element[] addedElements = new Element[size];
        for (int i = 0; i < size; i++)
          addedElements[i] = (Element) added.elementAt(i);
        return addedElements;
      }
      
      /**
       * Adds one Element to the vector of removed Elements.
       * @param e the Element to add
       */
      public void addRemovedElement (Element e)
      {
        if (!removed.contains(e))
          removed.add(e);
      }
      
      /**
       * Adds each Element in the given array to the vector of 
       * removed Elements
       * @param e the array containing the Elements to be added
       */
      public void addRemovedElements (Element[] e)
      {
        if (e == null || e.length == 0)
          return;
        for (int i = 0; i < e.length; i++)
          {
            if (!removed.contains(e[i]))
              removed.add(e[i]);
          }
      }
      
      /**
       * Adds one Element to the vector of added Elements.
       * @param e the Element to add
       */
      public void addAddedElement (Element e)
      {
        if (!added.contains(e))
          added.add(e);
      }
      
      /**
       * Adds each Element in the given array to the vector of
       * added Elements.
       * @param e the array containing the Elements to be added
       */
      public void addAddedElements (Element[] e)
      {
        if (e == null || e.length == 0)
          return;
        for (int i = 0; i < e.length; i++)
          {
            if (!added.contains(e[i]))
              added.add(e[i]);
          }
      }
      
      /**
       * Creates a new Edit object with the given parameters
       * @param e the paragraph Element associated with this Edit
       * @param i the index within the paragraph where changes are started
       * @param removed an array containing Elements that should be removed from
       * the paragraph Element
       * @param added an array containing Elements that should be added to the
       * paragraph Element
       */
      public Edit(Element e, int i, Element[] removed, Element[] added)
      {
        this.e = e;
        this.index = i;
        addRemovedElements(removed);
        addAddedElements(added);
      }    
    }

    /**
     * This method looks through the Vector of Edits to see if there is already
     * an Edit object associated with the given paragraph.  If there is,  
     * then we return it.  Otherwise we create a new Edit object, add it to the 
     * vector, and return it.  
     * 
     * Note: this method is package private to avoid accessors.
     * 
     * @param index the index associated with the Edit we want
     * @param para the paragraph associated with the Edit we want
     * @return the found or created Edit object
     */
    private Edit getEditForParagraphAndIndex (BranchElement para, int index)
    {
      Edit curr;
      int size = edits.size();
      for (int i = 0; i < size; i++)
        {
           curr = (Edit)edits.elementAt(i);
           if (curr.e.equals(para) && curr.index == index)
             return curr;
        }
      curr = new Edit(para, index, null, null);
      edits.add(curr);
      return curr;
    }
  }

  /**
   * An element type for sections. This is a simple BranchElement with
   * a unique name.
   */
  protected class SectionElement extends BranchElement
  {
    /**
     * Creates a new SectionElement.
     */
    public SectionElement()
    {
      super(null, null);
    }

    /**
     * Returns the name of the element. This method always returns
     * &quot;section&quot;.
     *
     * @return the name of the element
     */
    public String getName()
    {
      return SectionElementName;
    }
  }

  /**
   * Receives notification when any of the document's style changes and calls
   * {@link DefaultStyledDocument#styleChanged(Style)}.
   *
   * @author Roman Kennke (kennke@aicas.com)
   */
  private class StyleChangeListener
    implements ChangeListener
  {

    /**
     * Receives notification when any of the document's style changes and calls
     * {@link DefaultStyledDocument#styleChanged(Style)}.
     *
     * @param event the change event
     */
    public void stateChanged(ChangeEvent event)
    {
      Style style = (Style) event.getSource();
      styleChanged(style);
    }
  }

  /** The serialization UID (compatible with JDK1.5). */
  private static final long serialVersionUID = 940485415728614849L;

  /**
   * The default size to use for new content buffers.
   */
  public static final int BUFFER_SIZE_DEFAULT = 4096;

  /**
   * The <code>EditorBuffer</code> that is used to manage to
   * <code>Element</code> hierarchy.
   */
  protected DefaultStyledDocument.ElementBuffer buffer;

  /**
   * Listens for changes on this document's styles and notifies styleChanged().
   */
  private StyleChangeListener styleChangeListener;

  /** Vector that contains all the edits. */
  Vector edits = new Vector();

  /**
   * Creates a new <code>DefaultStyledDocument</code>.
   */
  public DefaultStyledDocument()
  {
    this(new GapContent(BUFFER_SIZE_DEFAULT), new StyleContext());
  }

  /**
   * Creates a new <code>DefaultStyledDocument</code> that uses the
   * specified {@link StyleContext}.
   *
   * @param context the <code>StyleContext</code> to use
   */
  public DefaultStyledDocument(StyleContext context)
  {
    this(new GapContent(BUFFER_SIZE_DEFAULT), context);
  }

  /**
   * Creates a new <code>DefaultStyledDocument</code> that uses the
   * specified {@link StyleContext} and {@link Content} buffer.
   *
   * @param content the <code>Content</code> buffer to use
   * @param context the <code>StyleContext</code> to use
   */
  public DefaultStyledDocument(AbstractDocument.Content content,
			       StyleContext context)
  {
    super(content, context);
    buffer = new ElementBuffer(createDefaultRoot());
    setLogicalStyle(0, context.getStyle(StyleContext.DEFAULT_STYLE));
  }

  /**
   * Adds a style into the style hierarchy. Unspecified style attributes
   * can be resolved in the <code>parent</code> style, if one is specified.
   *
   * While it is legal to add nameless styles (<code>nm == null</code),
   * you must be aware that the client application is then responsible
   * for managing the style hierarchy, since unnamed styles cannot be
   * looked up by their name.
   *
   * @param nm the name of the style or <code>null</code> if the style should
   *           be unnamed
   * @param parent the parent in which unspecified style attributes are
   *           resolved, or <code>null</code> if that is not necessary
   *
   * @return the newly created <code>Style</code>
   */
  public Style addStyle(String nm, Style parent)
  {
    StyleContext context = (StyleContext) getAttributeContext();
    Style newStyle = context.addStyle(nm, parent);

    // Register change listener.
    if (styleChangeListener == null)
      styleChangeListener = new StyleChangeListener();
    newStyle.addChangeListener(styleChangeListener);

    return newStyle;
  }

  /**
   * Create the default root element for this kind of <code>Document</code>.
   *
   * @return the default root element for this kind of <code>Document</code>
   */
  protected AbstractDocument.AbstractElement createDefaultRoot()
  {
    Element[] tmp;
    SectionElement section = new SectionElement();

    BranchElement paragraph = new BranchElement(section, null);
    tmp = new Element[1];
    tmp[0] = paragraph;
    section.replace(0, 0, tmp);

    LeafElement leaf = new LeafElement(paragraph, null, 0, 1);
    tmp = new Element[1];
    tmp[0] = leaf;
    paragraph.replace(0, 0, tmp);

    return section;
  }

  /**
   * Returns the <code>Element</code> that corresponds to the character
   * at the specified position.
   *
   * @param position the position of which we query the corresponding
   *        <code>Element</code>
   *
   * @return the <code>Element</code> that corresponds to the character
   *         at the specified position
   */
  public Element getCharacterElement(int position)
  {
    Element element = getDefaultRootElement();

    while (!element.isLeaf())
      {
	int index = element.getElementIndex(position);
	element = element.getElement(index);
      }
    
    return element;
  }

  /**
   * Extracts a background color from a set of attributes.
   *
   * @param attributes the attributes from which to get a background color
   *
   * @return the background color that correspond to the attributes
   */
  public Color getBackground(AttributeSet attributes)
  {
    StyleContext context = (StyleContext) getAttributeContext();
    return context.getBackground(attributes);
  }

  /**
   * Returns the default root element.
   *
   * @return the default root element
   */
  public Element getDefaultRootElement()
  {
    return buffer.getRootElement();
  }

  /**
   * Extracts a font from a set of attributes.
   *
   * @param attributes the attributes from which to get a font
   *
   * @return the font that correspond to the attributes
   */
  public Font getFont(AttributeSet attributes)
  {
    StyleContext context = (StyleContext) getAttributeContext();
    return context.getFont(attributes);
  }
  
  /**
   * Extracts a foreground color from a set of attributes.
   *
   * @param attributes the attributes from which to get a foreground color
   *
   * @return the foreground color that correspond to the attributes
   */
  public Color getForeground(AttributeSet attributes)
  {
    StyleContext context = (StyleContext) getAttributeContext();
    return context.getForeground(attributes);
  }

  /**
   * Returns the logical <code>Style</code> for the specified position.
   *
   * @param position the position from which to query to logical style
   *
   * @return the logical <code>Style</code> for the specified position
   */
  public Style getLogicalStyle(int position)
  {
    Element paragraph = getParagraphElement(position);
    AttributeSet attributes = paragraph.getAttributes();
    AttributeSet a = attributes.getResolveParent();
    // If the resolve parent is not of type Style, we return null.
    if (a instanceof Style)
      return (Style) a;
    return null;
  }

  /**
   * Returns the paragraph element for the specified position.
   * If the position is outside the bounds of the document's root element,
   * then the closest element is returned. That is the last paragraph if
   * <code>position >= endIndex</code> or the first paragraph if
   * <code>position < startIndex</code>.
   *
   * @param position the position for which to query the paragraph element
   *
   * @return the paragraph element for the specified position
   */
  public Element getParagraphElement(int position)
  {
    BranchElement root = (BranchElement) getDefaultRootElement();
    int start = root.getStartOffset();
    int end = root.getEndOffset();
    if (position >= end)
      position = end - 1;
    else if (position < start)
      position = start;

    Element par = root.positionToElement(position);

    assert par != null : "The paragraph element must not be null";
    return par;
  }

  /**
   * Looks up and returns a named <code>Style</code>.
   *
   * @param nm the name of the <code>Style</code>
   *
   * @return the found <code>Style</code> of <code>null</code> if no such
   *         <code>Style</code> exists
   */
  public Style getStyle(String nm)
  {
    StyleContext context = (StyleContext) getAttributeContext();
    return context.getStyle(nm);
  }

  /**
   * Removes a named <code>Style</code> from the style hierarchy.
   *
   * @param nm the name of the <code>Style</code> to be removed
   */
  public void removeStyle(String nm)
  {
    StyleContext context = (StyleContext) getAttributeContext();
    context.removeStyle(nm);
  }

  /**
   * Sets text attributes for the fragment specified by <code>offset</code>
   * and <code>length</code>.
   *
   * @param offset the start offset of the fragment
   * @param length the length of the fragment
   * @param attributes the text attributes to set
   * @param replace if <code>true</code>, the attributes of the current
   *     selection are overridden, otherwise they are merged
   */
  public void setCharacterAttributes(int offset, int length,
				     AttributeSet attributes,
				     boolean replace)
  {
    // Exit early if length is 0, so no DocumentEvent is created or fired.
    if (length == 0)
      return;
    try
      {
        // Must obtain a write lock for this method.  writeLock() and
        // writeUnlock() should always be in try/finally block to make
        // sure that locking happens in a balanced manner.
        writeLock();
    DefaultDocumentEvent ev =
          new DefaultDocumentEvent(
                                   offset, 
                                   length, 
			       DocumentEvent.EventType.CHANGE);

        // Modify the element structure so that the interval begins at an
        // element
    // start and ends at an element end.
    buffer.change(offset, length, ev);

    Element root = getDefaultRootElement();
    // Visit all paragraph elements within the specified interval
        int end = offset + length;
        Element curr;
        for (int pos = offset; pos < end; )
          {
            // Get the CharacterElement at offset pos.
            curr = getCharacterElement(pos);
            if (pos == curr.getEndOffset())
              break;

            MutableAttributeSet a = (MutableAttributeSet) curr.getAttributes();
            ev.addEdit(new AttributeUndoableEdit(curr, attributes, replace));
            // If replace is true, remove all the old attributes.
		if (replace)
              a.removeAttributes(a);
            // Add all the new attributes.
            a.addAttributes(attributes);
            // Increment pos so we can check the next CharacterElement.
            pos = curr.getEndOffset();
	      }
        fireChangedUpdate(ev);
        fireUndoableEditUpdate(new UndoableEditEvent(this, ev));
	  }
    finally
      {
        writeUnlock();
      }
  }
  
  /**
   * Sets the logical style for the paragraph at the specified position.
   *
   * @param position the position at which the logical style is added
   * @param style the style to set for the current paragraph
   */
  public void setLogicalStyle(int position, Style style)
  {
    Element el = getParagraphElement(position);
    // getParagraphElement doesn't return null but subclasses might so
    // we check for null here.
    if (el == null)
      return;
    try
    {
      writeLock();    
    if (el instanceof AbstractElement)
      {
        AbstractElement ael = (AbstractElement) el;
        ael.setResolveParent(style);
          int start = el.getStartOffset();
          int end = el.getEndOffset();
          DefaultDocumentEvent ev = 
            new DefaultDocumentEvent (start, 
                                      end - start, 
                                      DocumentEvent.EventType.CHANGE);
          fireChangedUpdate(ev);
          fireUndoableEditUpdate(new UndoableEditEvent(this, ev));
      }
    else
        throw new 
        AssertionError("paragraph elements are expected to be"
                       + "instances of AbstractDocument.AbstractElement");
    }
    finally
    {
      writeUnlock();
    }
  }

  /**
   * Sets text attributes for the paragraph at the specified fragment.
   *
   * @param offset the beginning of the fragment
   * @param length the length of the fragment
   * @param attributes the text attributes to set
   * @param replace if <code>true</code>, the attributes of the current
   *     selection are overridden, otherwise they are merged
   */
  public void setParagraphAttributes(int offset, int length,
                                     AttributeSet attributes,
                                     boolean replace)
  {
    try
      {
        // Must obtain a write lock for this method.  writeLock() and
        // writeUnlock() should always be in try/finally blocks to make
        // sure that locking occurs in a balanced manner.
        writeLock();
        
        // Create a DocumentEvent to use for changedUpdate().
        DefaultDocumentEvent ev = 
          new DefaultDocumentEvent (
                                    offset, 
                                    length, 
                                    DocumentEvent.EventType.CHANGE);
        
        // Have to iterate through all the _paragraph_ elements that are
        // contained or partially contained in the interval
        // (offset, offset + length).
        Element rootElement = getDefaultRootElement();
        int startElement = rootElement.getElementIndex(offset);
        int endElement = rootElement.getElementIndex(offset + length - 1);
        if (endElement < startElement)
          endElement = startElement;
        
        for (int i = startElement; i <= endElement; i++)
          {
            Element par = rootElement.getElement(i);
            MutableAttributeSet a = (MutableAttributeSet) par.getAttributes();
            // Add the change to the DocumentEvent.
            ev.addEdit(new AttributeUndoableEdit(par, attributes, replace));
            // If replace is true remove the old attributes.
        if (replace)
              a.removeAttributes(a);
            // Add the new attributes.
            a.addAttributes(attributes);
          }
        fireChangedUpdate(ev);
        fireUndoableEditUpdate(new UndoableEditEvent(this, ev));
      }
    finally
      {
        writeUnlock();
      }
  }

  /**
   * Called in response to content insert actions. This is used to
   * update the element structure.
   *
   * @param ev the <code>DocumentEvent</code> describing the change
   * @param attr the attributes for the change
   */
  protected void insertUpdate(DefaultDocumentEvent ev, AttributeSet attr)
  {
    super.insertUpdate(ev, attr);
    // If the attribute set is null, use an empty attribute set.
    if (attr == null)
      attr = SimpleAttributeSet.EMPTY;
    int offset = ev.getOffset();
    int length = ev.getLength();
    int endOffset = offset + length;
    AttributeSet paragraphAttributes = 
      getParagraphElement(endOffset).getAttributes();
    Segment txt = new Segment();
    try
      {
        getText(offset, length, txt);
      }
    catch (BadLocationException ex)
      {
        AssertionError ae = new AssertionError("Unexpected bad location");
        ae.initCause(ex);
        throw ae;
      }

    int len = 0;
    Vector specs = new Vector();
    ElementSpec finalStartTag = null;
    short finalStartDirection = ElementSpec.OriginateDirection;
    boolean prevCharWasNewline = false;
    Element prev = getCharacterElement(offset);
    Element next = getCharacterElement(endOffset);
    Element prevParagraph = getParagraphElement(offset);
    Element paragraph = getParagraphElement(endOffset);

    int segmentEnd = txt.offset + txt.count;
    
    // Check to see if we're inserting immediately after a newline.
    if (offset > 0)
      {
        try
        {
          String s = getText(offset - 1, 1);
          if (s.equals("\n"))
            {
              finalStartDirection = 
                handleInsertAfterNewline(specs, offset, endOffset,
                                         prevParagraph,
                                         paragraph,
                                         paragraphAttributes);
              
              prevCharWasNewline = true;
              // Find the final start tag from the ones just created.
              for (int i = 0; i < specs.size(); i++)
                if (((ElementSpec) specs.get(i)).getType() 
                    == ElementSpec.StartTagType)
                  finalStartTag = (ElementSpec)specs.get(i);
            }
        }
        catch (BadLocationException ble)
        {          
          // This shouldn't happen.
          AssertionError ae = new AssertionError();
          ae.initCause(ble);
          throw ae;
        }        
      }

        
    for (int i = txt.offset; i < segmentEnd; ++i)
      {
        len++;
        if (txt.array[i] == '\n')
          {
            // Add the ElementSpec for the content.
            specs.add(new ElementSpec(attr, ElementSpec.ContentType, len));            

            // Add ElementSpecs for the newline.
            specs.add(new ElementSpec(null, ElementSpec.EndTagType));
            finalStartTag = new ElementSpec(paragraphAttributes,
                                                   ElementSpec.StartTagType);
            specs.add(finalStartTag);
            len = 0;
          }
      }

    // Create last element if last character hasn't been a newline.
    if (len > 0)
      specs.add(new ElementSpec(attr, ElementSpec.ContentType, len));      

    // Set the direction of the last spec of type StartTagType.  
    // If we are inserting after a newline then this value comes from 
    // handleInsertAfterNewline.
    if (finalStartTag != null)
      {        
        if (prevCharWasNewline)
          finalStartTag.setDirection(finalStartDirection);
        else if (prevParagraph.getEndOffset() != endOffset)
                  finalStartTag.setDirection(ElementSpec.JoinFractureDirection);
        else
          {
            // If there is an element AFTER this one, then set the 
            // direction to JoinNextDirection.
            Element parent = prevParagraph.getParentElement();
            int index = parent.getElementIndex(offset);
            if (index + 1 < parent.getElementCount()
                && !parent.getElement(index + 1).isLeaf())
              finalStartTag.setDirection(ElementSpec.JoinNextDirection);
          }
      }
    
    // If we are at the last index, then check if we could probably be
    // joined with the next element.
    // This means:
    //  - we must be a ContentTag
    //  - if there is a next Element, we must have the same attributes
    //  - if there is no next Element, but one will be created,
    //    we must have the same attributes as the higher-level run.
    ElementSpec last = (ElementSpec) specs.lastElement();
    if (last.getType() == ElementSpec.ContentType)
      {
        Element currentRun = 
          prevParagraph.getElement(prevParagraph.getElementIndex(offset));
        if (currentRun.getEndOffset() == endOffset)
          {
            if (endOffset < getLength() && next.getAttributes().isEqual(attr)
                && last.getType() == ElementSpec.ContentType)
      last.setDirection(ElementSpec.JoinNextDirection);    
          }
        else
          {
            if (finalStartTag != null
                && finalStartTag.getDirection() == 
                  ElementSpec.JoinFractureDirection
                && currentRun.getAttributes().isEqual(attr))
              {
                last.setDirection(ElementSpec.JoinNextDirection);
              }
          }
      }
    
        // If we are at the first new element, then check if it could be
        // joined with the previous element.
    ElementSpec first = (ElementSpec) specs.firstElement();
    if (prev.getAttributes().isEqual(attr)
        && first.getType() == ElementSpec.ContentType)
      first.setDirection(ElementSpec.JoinPreviousDirection);

    ElementSpec[] elSpecs =
      (ElementSpec[]) specs.toArray(new ElementSpec[specs.size()]);

    buffer.insert(offset, length, elSpecs, ev);
  }  

  /**
   * A helper method to set up the ElementSpec buffer for the special
   * case of an insertion occurring immediately after a newline.
   * @param specs the ElementSpec buffer to initialize.
   */
  short handleInsertAfterNewline(Vector specs, int offset, int endOffset,
                                Element prevParagraph, Element paragraph,
                                AttributeSet a)
  {
    if (prevParagraph.getParentElement() == paragraph.getParentElement())
      {
        specs.add(new ElementSpec(a, ElementSpec.EndTagType));
        specs.add(new ElementSpec(a, ElementSpec.StartTagType));
        if (prevParagraph.getEndOffset() != endOffset)
          return ElementSpec.JoinFractureDirection;
        // If there is an Element after this one, use JoinNextDirection.
        Element parent = paragraph.getParentElement();
        if (parent.getElementCount() > parent.getElementIndex(offset) + 1)
          return ElementSpec.JoinNextDirection;
      }
    else
      {
        // TODO: What to do here?
      }
    return ElementSpec.OriginateDirection;
  }
  
  /**
   * Updates the document structure in response to text removal. This is
   * forwarded to the {@link ElementBuffer} of this document. Any changes to
   * the document structure are added to the specified document event and
   * sent to registered listeners.
   *
   * @param ev the document event that records the changes to the document
   */
  protected void removeUpdate(DefaultDocumentEvent ev)
  {
    super.removeUpdate(ev);
    buffer.remove(ev.getOffset(), ev.getLength(), ev);
  }

  /**
   * Returns an enumeration of all style names.
   *
   * @return an enumeration of all style names
   */
  public Enumeration getStyleNames()
  {
    StyleContext context = (StyleContext) getAttributeContext();
    return context.getStyleNames();
  }

  /**
   * Called when any of this document's styles changes.
   *
   * @param style the style that changed
   */
  protected void styleChanged(Style style)
  {
    // Nothing to do here. This is intended to be overridden by subclasses.
  }

  /**
   * Inserts a bulk of structured content at once.
   *
   * @param offset the offset at which the content should be inserted
   * @param data the actual content spec to be inserted
   */
  protected void insert(int offset, ElementSpec[] data)
    throws BadLocationException
  {
    if (data == null || data.length == 0)
      return;
    try
      {
        // writeLock() and writeUnlock() should always be in a try/finally
        // block so that locking balance is guaranteed even if some 
        // exception is thrown.
    writeLock();
        
        // First we collect the content to be inserted.
        StringBuffer contentBuffer = new StringBuffer();
    for (int i = 0; i < data.length; i++)
      {
            // Collect all inserts into one so we can get the correct
            // ElementEdit
        ElementSpec spec = data[i];
        if (spec.getArray() != null && spec.getLength() > 0)
              contentBuffer.append(spec.getArray(), spec.getOffset(),
                                             spec.getLength());
      }

        int length = contentBuffer.length();

        // If there was no content inserted then exit early.
        if (length == 0)
          return;

        UndoableEdit edit = content.insertString(offset,
                                                 contentBuffer.toString());

        // Create the DocumentEvent with the ElementEdit added
        DefaultDocumentEvent ev = 
          new DefaultDocumentEvent(offset,
                                   length,
                                               DocumentEvent.EventType.INSERT);
        ev.addEdit(edit);

        // Finally we must update the document structure and fire the insert
        // update event.
        buffer.insert(offset, length, data, ev);
    fireInsertUpdate(ev);
        fireUndoableEditUpdate(new UndoableEditEvent(this, ev));
      }
    finally
      {
    writeUnlock();
  }
  }

  /**
   * Initializes the <code>DefaultStyledDocument</code> with the specified
   * data.
   *
   * @param data the specification of the content with which the document is
   *        initialized
   */
  protected void create(ElementSpec[] data)
  {
    try
      {
        // Clear content.
        content.remove(0, content.length());
        // Clear buffer and root element.
        buffer = new ElementBuffer(createDefaultRoot());
        // Insert the data.
        insert(0, data);
      }
    catch (BadLocationException ex)
      {
        AssertionError err = new AssertionError("Unexpected bad location");
        err.initCause(ex);
        throw err;
      }
  }
}
