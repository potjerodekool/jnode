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

import java.awt.AWTEvent;
import java.awt.BufferCapabilities.FlipContents;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.event.FocusEvent.Cause;
import java.awt.peer.CanvasPeer;
import java.awt.peer.ComponentPeer;

import javax.swing.JComponent;

/**
 * AWT canvas peer implemented as a {@link javax.swing.JComponent}.
 */

final class SwingCanvasPeer extends SwingComponentPeer<Canvas, SwingCanvas> implements CanvasPeer {

    public SwingCanvasPeer(SwingToolkit toolkit, Canvas canvas) {
        super(toolkit, canvas, new SwingCanvas(canvas));
        SwingToolkit.add(canvas, peerComponent);
        SwingToolkit.copyAwtProperties(canvas, peerComponent);
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
	public GraphicsConfiguration getAppropriateGraphicsConfiguration(GraphicsConfiguration arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}

final class SwingCanvas extends JComponent implements ISwingPeer<Canvas> {
    private static final long serialVersionUID = 1L;
    private final Canvas awtComponent;

    public SwingCanvas(Canvas awtComponent) {
        this.awtComponent = awtComponent;
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
     */
    public Canvas getAWTComponent() {
        return awtComponent;
    }

    /**
     * Pass an event onto the AWT component.
     *
     * @see java.awt.Component#processEvent(java.awt.AWTEvent)
     */
    protected final void processEvent(AWTEvent event) {
        awtComponent.dispatchEvent(SwingToolkit.convertEvent(event, awtComponent));
    }

    /**
     * Process an event within this swingpeer
     *
     * @param event
     */
    public final void processAWTEvent(AWTEvent event) {
        super.processEvent(event);
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#validatePeerOnly()
     */
    public final void validatePeerOnly() {
        super.validate();
    }
}
