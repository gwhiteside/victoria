package net.georgewhiteside.victoria;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;

public class ContextMenuMouseListener extends MouseAdapter
{
	private JPopupMenu popupMenu;
	private JTextComponent textComponent;
	
	private Action cutAction;
	private Action copyAction;
	private Action pasteAction;
	private Action selectAllAction;
	
	public ContextMenuMouseListener() {
		popupMenu = new JPopupMenu();
		
		cutAction = new DefaultEditorKit.CutAction();
		copyAction = new DefaultEditorKit.CopyAction();
		pasteAction = new DefaultEditorKit.PasteAction();
		selectAllAction = new SelectAllAction();
		
		cutAction.putValue(Action.NAME, "Cut");
		copyAction.putValue(Action.NAME, "Copy");
		pasteAction.putValue(Action.NAME, "Paste");
		selectAllAction.putValue(Action.NAME, "Select All");
		
		popupMenu.add(cutAction);
		popupMenu.add(copyAction);
		popupMenu.add(pasteAction);
		popupMenu.addSeparator();
		popupMenu.add(selectAllAction);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getModifiers() == InputEvent.BUTTON3_MASK) {
			if(e.getSource() instanceof JTextComponent == false) {
				return;
			}
			
			textComponent = (JTextComponent) e.getSource();
			textComponent.requestFocusInWindow();
			
			boolean enabled = textComponent.isEnabled();
			boolean editable = textComponent.isEditable();
			boolean empty = textComponent.getText() == null || textComponent.getText().equals("");
			boolean marked = textComponent.getSelectedText() != null;
			boolean pasteAvailable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).isDataFlavorSupported(DataFlavor.stringFlavor);
			
			cutAction.setEnabled(enabled && editable && marked);
			copyAction.setEnabled(enabled && marked);
			pasteAction.setEnabled(enabled && editable && pasteAvailable);
			selectAllAction.setEnabled(enabled && !empty);
			
			popupMenu.show(e.getComponent(), e.getX(), e.getY() - popupMenu.getSize().height);
		}
	}
	
	// lifted wholesale from DefaultEditorKit
	static class SelectAllAction extends TextAction {
        SelectAllAction() {
            super(DefaultEditorKit.selectAllAction);
        }

        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                Document doc = target.getDocument();
                target.setCaretPosition(0);
                target.moveCaretPosition(doc.getLength());
            }
        }
    }
}
