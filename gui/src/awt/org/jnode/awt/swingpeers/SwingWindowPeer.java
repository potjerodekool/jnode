/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.awt.swingpeers;

import java.awt.BorderLayout;
import java.awt.BufferCapabilities.FlipContents;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
import java.awt.event.FocusEvent.Cause;
import java.awt.peer.ComponentPeer;
import java.awt.peer.WindowPeer;
import javax.swing.JRootPane;
import javax.swing.plaf.InternalFrameUI;
import javax.swing.plaf.basic.BasicInternalFrameUI;

/**
 * AWT window peer implemented as a {@link javax.swing.JInternalFrame}.
 *
 * @author Levente S\u00e1ntha
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class SwingWindowPeer extends SwingBaseWindowPeer<Window, SwingWindow>
    implements WindowPeer {

    public SwingWindowPeer(SwingToolkit toolkit, Window window) {
        super(toolkit, window, new SwingWindow(window));
        addToDesktop();
    }

	@Override
	public void flip(int arg0, int arg1, int arg2, int arg3, FlipContents arg4) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean requestFocus(Component arg0, boolean arg1, boolean arg2, long arg3, Cause arg4) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setZOrder(ComponentPeer arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean updateGraphicsData(GraphicsConfiguration arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void repositionSecurityWarning() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOpacity(float arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOpaque(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateAlwaysOnTopState() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateWindow() {
		// TODO Auto-generated method stub
		
	}
}

final class SwingWindow extends SwingBaseWindow<Window, SwingWindow> {
    private static final long serialVersionUID = 1L;

    public SwingWindow(Window target) {
        super(target);
        setResizable(false);
        setIconifiable(false);
        setMaximizable(false);
        setClosable(false);
        InternalFrameUI ui1 = getUI();
        if (ui1 instanceof BasicInternalFrameUI) {
            //removing upper decoration
            ((BasicInternalFrameUI) ui1).setNorthPane(null);
        } else {
            throw new RuntimeException("Unknown UI: " + ui1);
        }
    }

    /**
     * @see javax.swing.JComponent#updateUI()
     */
    public void updateUI() {
        setMinimumSize(null);
        setBorder(null);
        super.updateUI();
        setMinimumSize(null);

        final JRootPane rootpane = getRootPane();
        setBorder(SwingToolkit.EMPTY_BORDER);
        removeAll();
        setLayout(new BorderLayout());
        //add(rootpane, BorderLayout.CENTER);
        setRootPane(rootpane);
        getLayout().layoutContainer(this);

        this.setOpaque(false);
        rootpane.setOpaque(false);
        getLayeredPane().setOpaque(false);
    }
}
