package org.nlogo.deltatick.dnd;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 8/11/12
 * Time: 10:59 PM
 * To change this template use File | Settings | File Templates.
 */

public class AgentInput extends JTextField {
    public AgentInput(Component parent) {
        super();

        setBorder(javax.swing.BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(parent.getBackground().darker()),
                javax.swing.BorderFactory.createEmptyBorder(1, 2, 0, 0)
        ));
        setBackground( Color.white );
        setFont(new java.awt.Font("Courier New", 1, 12));
        setSize(this.getWidth(), 10);
        this.setToolTipText("Who?");
}
}
