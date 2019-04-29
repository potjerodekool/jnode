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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Panel;
import java.awt.event.FocusEvent.Cause;
import java.awt.peer.ComponentPeer;
import java.awt.peer.PanelPeer;
import javax.swing.JPanel;

/**
 * AWT panel peer implemented as a {@link javax.swing.JPanel}.
 *
 * @author Levente S\u00e1ntha
 */

final class SwingPanelPeer extends SwingContainerPeer<Panel, SwingPanel>
    implements PanelPeer, ISwingContainerPeer {

    //
    // Construction
    //

    public SwingPanelPeer(SwingToolkit toolkit, Panel panel) {
        super(toolkit, panel, new SwingPanel(panel));
        final SwingPanel jPanel = (SwingPanel) peerComponent;
        SwingToolkit.add(panel, jPanel);
        SwingToolkit.copyAwtProperties(panel, jPanel);
        peerComponent.setLayout(new SwingContainerLayout(panel, this));
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

final class SwingPanel extends JPanel implements ISwingPeer<Panel> {
    private static final long serialVersionUID = 1L;
    private final Panel awtComponent;

    public SwingPanel(Panel awtComponent) {
        this.awtComponent = awtComponent;
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
     */
    public Panel getAWTComponent() {
        return awtComponent;
    }

    /**
     * @see javax.swing.JComponent#paintChildren(java.awt.Graphics)
     */
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        //SwingToolkit.paintLightWeightChildren(awtComponent, g, 0, 0);
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
