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

import java.awt.BufferCapabilities.FlipContents;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
import java.awt.event.FocusEvent.Cause;
import java.awt.peer.ComponentPeer;
import java.awt.peer.DialogPeer;
import java.util.List;

/**
 * AWT dialog peer implemented as a {@link javax.swing.JInternalFrame}.
 *
 * @author Levente S\u00e1ntha
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class SwingDialogPeer extends SwingBaseWindowPeer<Dialog, SwingDialog>
    implements DialogPeer, ISwingContainerPeer {

    public SwingDialogPeer(SwingToolkit toolkit, Dialog target) {
        super(toolkit, target, new SwingDialog(target, target.getTitle()));
        setTitle(target.getTitle());
        setResizable(target.isResizable());
        peerComponent.setIconifiable(false);
        peerComponent.setMaximizable(true);
        peerComponent.setClosable(true);
        peerComponent.setTitle(target.getTitle());
        addToDesktop();
    }

    /**
     * @see org.jnode.awt.swingpeers.SwingBaseWindowPeer#setTitle(java.lang.String)
     */
    public void setTitle(String title) {
        if (!targetComponent.isUndecorated()) {
            super.setTitle(title);
        }
    }

    public void blockWindows(List<Window> windows) {
        //TODO implement it
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
}

final class SwingDialog extends SwingBaseWindow<Dialog, SwingDialog> {
    private static final long serialVersionUID = 1L;

    public SwingDialog(Dialog target, String title) {
        super(target, title);
    }
}

