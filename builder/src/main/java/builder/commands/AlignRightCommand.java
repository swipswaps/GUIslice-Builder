/**
 *
 * The MIT License
 *
 * Copyright 2018-2020 Paul Conti
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package builder.commands;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import builder.controller.Controller;
import builder.mementos.AlignPropertyMemento;
import builder.models.WidgetModel;
import builder.prefs.GeneralEditor;
import builder.views.PagePane;
import builder.widgets.Widget;

/**
 * The Class AlignRightCommand will
 * align the selected widget horizontally by the right most widget.
 * 
 * @author Paul Conti
 * 
 */
public class AlignRightCommand extends Command {
  
  /** The page that holds the selected widgets. */
  private PagePane page;
  
  /** The group list contains the models of all the selected widgets that will be aligned. */
  private List<WidgetModel> groupList = new ArrayList<WidgetModel>();
  
  /** The right most X value. */
  private int rightX = -1;   // x axis value of right most widget
  
  /** bAlreadyAligned indicates that the widgets are already aligned. */
  private boolean bAlreadyAligned;
  
  /**
   * Instantiates a new align right command.
   *
   * @param page
   *          the <code>page</code> is the object that holds the widgets
   */
  public AlignRightCommand(PagePane page) {
    this.page = page;
  }
  
  /**
   * Align will setup the align right command for later execution
   * and creates the required Memento object for undo/redo.
   *
   * @return <code>true</code>, if successful
   */
  public boolean align() {
    int margin = GeneralEditor.getInstance().getMargins();
    int width = Controller.getProjectModel().getWidth();
    margin = width - margin;
    // pass one finds right-most widget (highest X value) + width
    List<Widget> list = page.getSelectedList();
    int offsetX=0;
    for (Widget w : list) {
      WidgetModel m = w.getModel();
      offsetX = m.getX()+m.getWidth();
      if (offsetX > rightX) {
        rightX = offsetX;
      }
    }
    // test for already aligned and, if so, assume move to left margin
    // unless already there
    bAlreadyAligned = true;
    for (Widget w : list) {
      WidgetModel m = w.getModel();
      offsetX = m.getX()+m.getWidth();
      if (offsetX != rightX) {
        bAlreadyAligned = false;
        break;
      }
    }
    if (bAlreadyAligned) {
      if (rightX == margin) { // error out, can't move any further
        JOptionPane.showMessageDialog(null, 
            "The widgets are already aligned.",
            "Warning",
            JOptionPane.WARNING_MESSAGE);
      } else {
        rightX = margin;
      }
    }
    // This pass will add to groupList selected widgets that will use the new X value
    for (Widget w : list) {
      WidgetModel m = w.getModel();
      offsetX = m.getX()+m.getWidth();
      if(offsetX <= rightX) {
        groupList.add(m);
      }   
    }
    // did we find any matches?
    if (groupList.size() < 1) {
      JOptionPane.showMessageDialog(null, 
          "This command requires at least one widget be selected.",
          "Warning",
          JOptionPane.WARNING_MESSAGE);
      return false;
    }
    memento = new AlignPropertyMemento(page, groupList, WidgetModel.PROP_X);
    return true;  // success
  }

  /**
   * execute - will actually run the command
   *
   * @see builder.commands.Command#execute()
   */
  @Override
  public void execute() {
    int offsetX = 0;
    for(WidgetModel m : groupList) {
      offsetX = rightX - m.getWidth();
      m.changeValueAt(Integer.valueOf(offsetX), WidgetModel.PROP_X);
    }
  }

  /**
   * toString - converts command to string for debugging
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String myEnums = "";
    WidgetModel m = null;
    for(int i=0; i<groupList.size(); i++) {
      m = groupList.get(i);
      if (i > 0) myEnums = myEnums + ",";
      myEnums = myEnums + m.getEnum();
    }
    return String.format("Align Right: %s x=%d",myEnums,rightX);
  }

}
