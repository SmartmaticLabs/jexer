/*
 * Jexer - Java Text User Interface
 *
 * The MIT License (MIT)
 *
 * Copyright (C) 2017 Kevin Lamonte
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * @author Kevin Lamonte [kevin.lamonte@gmail.com]
 * @version 1
 */
package jexer;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import jexer.TApplication;
import jexer.TEditorWidget;
import jexer.THScroller;
import jexer.TScrollableWindow;
import jexer.TVScroller;
import jexer.TWidget;
import jexer.bits.CellAttributes;
import jexer.bits.GraphicsChars;
import jexer.event.TCommandEvent;
import jexer.event.TKeypressEvent;
import jexer.event.TMouseEvent;
import jexer.event.TResizeEvent;
import static jexer.TCommand.*;
import static jexer.TKeypress.*;

/**
 * TEditorWindow is a basic text file editor.
 */
public class TEditorWindow extends TScrollableWindow {

    /**
     * Hang onto my TEditor so I can resize it with the window.
     */
    private TEditorWidget editField;

    /**
     * The fully-qualified name of the file being edited.
     */
    private String filename = "";

    /**
     * Setup other fields after the editor is created.
     */
    private void setupAfterEditor() {
        hScroller = new THScroller(this, 17, getHeight() - 2, getWidth() - 20);
        vScroller = new TVScroller(this, getWidth() - 2, 0, getHeight() - 2);
        setMinimumWindowWidth(25);
        setMinimumWindowHeight(10);
        setTopValue(1);
        setBottomValue(editField.getMaximumRowNumber());
        setLeftValue(1);
        setRightValue(editField.getMaximumColumnNumber());

        statusBar = newStatusBar("Editor");
        statusBar.addShortcutKeypress(kbF1, cmHelp, "Help");
        statusBar.addShortcutKeypress(kbF2, cmSave, "Save");
        statusBar.addShortcutKeypress(kbF3, cmOpen, "Open");
        statusBar.addShortcutKeypress(kbF10, cmMenu, "Menu");
    }

    /**
     * Public constructor sets window title.
     *
     * @param parent the main application
     * @param title the window title
     */
    public TEditorWindow(final TApplication parent, final String title) {

        super(parent, title, 0, 0, parent.getScreen().getWidth(),
            parent.getScreen().getHeight() - 2, RESIZABLE);

        editField = addEditor("", 0, 0, getWidth() - 2, getHeight() - 2);
        setupAfterEditor();
    }

    /**
     * Public constructor sets window title and contents.
     *
     * @param parent the main application
     * @param title the window title, usually a filename
     * @param contents the data for the editing window, usually the file data
     */
    public TEditorWindow(final TApplication parent, final String title,
        final String contents) {

        super(parent, title, 0, 0, parent.getScreen().getWidth(),
            parent.getScreen().getHeight() - 2, RESIZABLE);

        filename = title;
        editField = addEditor(contents, 0, 0, getWidth() - 2, getHeight() - 2);
        setupAfterEditor();
    }

    /**
     * Public constructor opens a file.
     *
     * @param parent the main application
     * @param file the file to open
     * @throws IOException if a java.io operation throws
     */
    public TEditorWindow(final TApplication parent,
        final File file) throws IOException {

        super(parent, file.getName(), 0, 0, parent.getScreen().getWidth(),
            parent.getScreen().getHeight() - 2, RESIZABLE);

        filename = file.getName();
        String contents = readFileData(file);
        editField = addEditor(contents, 0, 0, getWidth() - 2, getHeight() - 2);
        setupAfterEditor();
    }

    /**
     * Public constructor.
     *
     * @param parent the main application
     */
    public TEditorWindow(final TApplication parent) {
        this(parent, "New Text Document");
    }

    /**
     * Read file data into a string.
     *
     * @param file the file to open
     * @return the file contents
     * @throws IOException if a java.io operation throws
     */
    private String readFileData(final File file) throws IOException {
        StringBuilder fileContents = new StringBuilder();
        Scanner scanner = new Scanner(file);
        String EOL = System.getProperty("line.separator");

        try {
            while (scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine() + EOL);
            }
            return fileContents.toString();
        } finally {
            scanner.close();
        }
    }

    /**
     * Read file data into a string.
     *
     * @param filename the file to open
     * @return the file contents
     * @throws IOException if a java.io operation throws
     */
    private String readFileData(final String filename) throws IOException {
        return readFileData(new File(filename));
    }

    /**
     * Draw the window.
     */
    @Override
    public void draw() {
        // Draw as normal.
        super.draw();

        // Add the row:col on the bottom row
        CellAttributes borderColor = getBorder();
        String location = String.format(" %d:%d ",
            editField.getEditingRowNumber(),
            editField.getEditingColumnNumber());
        int colon = location.indexOf(':');
        putStringXY(10 - colon, getHeight() - 1, location, borderColor);

        if (editField.isDirty()) {
            putCharXY(2, getHeight() - 1, GraphicsChars.OCTOSTAR, borderColor);
        }
    }

    /**
     * Check if a mouse press/release/motion event coordinate is over the
     * editor.
     *
     * @param mouse a mouse-based event
     * @return whether or not the mouse is on the editor
     */
    private final boolean mouseOnEditor(final TMouseEvent mouse) {
        if ((mouse.getAbsoluteX() >= getAbsoluteX() + 1)
            && (mouse.getAbsoluteX() <  getAbsoluteX() + getWidth() - 1)
            && (mouse.getAbsoluteY() >= getAbsoluteY() + 1)
            && (mouse.getAbsoluteY() <  getAbsoluteY() + getHeight() - 1)
        ) {
            return true;
        }
        return false;
    }

    /**
     * Handle mouse press events.
     *
     * @param mouse mouse button press event
     */
    @Override
    public void onMouseDown(final TMouseEvent mouse) {
        // Use TWidget's code to pass the event to the children.
        super.onMouseDown(mouse);

        if (mouseOnEditor(mouse)) {
            // The editor might have changed, update the scollbars.
            setBottomValue(editField.getMaximumRowNumber());
            setVerticalValue(editField.getEditingRowNumber());
            setRightValue(editField.getMaximumColumnNumber());
            setHorizontalValue(editField.getEditingColumnNumber());
        } else {
            if (mouse.isMouseWheelUp() || mouse.isMouseWheelDown()) {
                // Vertical scrollbar actions
                editField.setEditingRowNumber(getVerticalValue());
            }
        }
    }

    /**
     * Handle mouse release events.
     *
     * @param mouse mouse button release event
     */
    @Override
    public void onMouseUp(final TMouseEvent mouse) {
        // Use TWidget's code to pass the event to the children.
        super.onMouseUp(mouse);

        if (mouse.isMouse1() && mouseOnVerticalScroller(mouse)) {
            // Clicked on vertical scrollbar
            editField.setEditingRowNumber(getVerticalValue());
        }

        // TODO: horizontal scrolling
    }

    /**
     * Method that subclasses can override to handle mouse movements.
     *
     * @param mouse mouse motion event
     */
    @Override
    public void onMouseMotion(final TMouseEvent mouse) {
        // Use TWidget's code to pass the event to the children.
        super.onMouseMotion(mouse);

        if (mouseOnEditor(mouse) && mouse.isMouse1()) {
            // The editor might have changed, update the scollbars.
            setBottomValue(editField.getMaximumRowNumber());
            setVerticalValue(editField.getEditingRowNumber());
            setRightValue(editField.getMaximumColumnNumber());
            setHorizontalValue(editField.getEditingColumnNumber());
        } else {
            if (mouse.isMouse1() && mouseOnVerticalScroller(mouse)) {
                // Clicked/dragged on vertical scrollbar
                editField.setEditingRowNumber(getVerticalValue());
            }

            // TODO: horizontal scrolling
        }

    }

    /**
     * Handle keystrokes.
     *
     * @param keypress keystroke event
     */
    @Override
    public void onKeypress(final TKeypressEvent keypress) {
        // Use TWidget's code to pass the event to the children.
        super.onKeypress(keypress);

        // The editor might have changed, update the scollbars.
        setBottomValue(editField.getMaximumRowNumber());
        setVerticalValue(editField.getEditingRowNumber());
        setRightValue(editField.getMaximumColumnNumber());
        setHorizontalValue(editField.getEditingColumnNumber());
    }

    /**
     * Handle window/screen resize events.
     *
     * @param event resize event
     */
    @Override
    public void onResize(final TResizeEvent event) {
        if (event.getType() == TResizeEvent.Type.WIDGET) {
            // Resize the text field
            TResizeEvent editSize = new TResizeEvent(TResizeEvent.Type.WIDGET,
                event.getWidth() - 2, event.getHeight() - 2);
            editField.onResize(editSize);

            // Have TScrollableWindow handle the scrollbars
            super.onResize(event);
            return;
        }

        // Pass to children instead
        for (TWidget widget: getChildren()) {
            widget.onResize(event);
        }
    }

    /**
     * Method that subclasses can override to handle posted command events.
     *
     * @param command command event
     */
    @Override
    public void onCommand(final TCommandEvent command) {
        if (command.equals(cmOpen)) {
            try {
                String filename = fileOpenBox(".");
                if (filename != null) {
                    try {
                        String contents = readFileData(filename);
                        new TEditorWindow(getApplication(), filename, contents);
                    } catch (IOException e) {
                        messageBox("Error", "Error reading file: " +
                            e.getMessage());
                    }
                }
            } catch (IOException e) {
                messageBox("Error", "Error opening file dialog: " +
                    e.getMessage());
            }
            return;
        }

        if (command.equals(cmSave)) {
            if (filename.length() > 0) {
                try {
                    editField.saveToFilename(filename);
                } catch (IOException e) {
                    messageBox("Error", "Error saving file: " + e.getMessage());
                }
            }
            return;
        }

        // Didn't handle it, let children get it instead
        super.onCommand(command);
    }

}