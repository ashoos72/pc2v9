// Copyright (C) 1989-2019 PC2 Development Team: John Clevenger, Douglas Lane, Samir Ashoo, and Troy Boudreau.
package edu.csus.ecs.pc2.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;

/**
 * This class is a stand-alone frame that implements a "click me" mouse game.
 * 
 * @author john clevenger
 * 
 *         TODO: Add to Admin panel? add count of failed clicks add "score" == percent of success vs. failed clicks Enforce minimum size on chase panel restart game on "I Give Up/ No" improved
 *         (harder) success algorithm -- move clickme on window shown? -- differentiate between mouse MOVE and DRAG? difficulty levels
 *
 */
public class ClickMeFrame extends JFrame implements ActionListener, ComponentListener, MouseListener, MouseMotionListener, WindowListener {

    private static final long serialVersionUID = 1L;

    // the message on the Success label in the top panel
    private static final String SUCCESS_MESSAGE = "Successful clicks = ";

    // the count of successful "clickme's" that will go into the SuccessMessage
    private int successCount = 0;

    private JPanel clickMeContentPane = null;

    private JLabel clickMeLabel = null;

    private JButton quitButton = null;

    private JLabel successLabel = null;

    private static final int MAX_MOVE = 500;

    private double oldDist = java.lang.Double.MAX_VALUE;

    private JPanel scorePanel;

    private JLabel scoreLabel;

    private JPanel buttonPanel;

    private JPanel chasePanel;

    private JButton replayButton;

    /**
     * Constructs a ClickMe frame with the specified title.
     * 
     * @param title
     *            - the String to be displayed in the frame's title bar.
     */
    public ClickMeFrame(String title) {
        super(title);
        initialize();
    }

    /**
     * Constructs a ClickMe frame with a default title.
     */
    public ClickMeFrame() {
        this("ClickMe");
    }

    /**
     * Initialize the class.
     */
    private void initialize() {
        try {
            setName("ClickMe");
            setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            setSize(616, 404);
            setVisible(true);
            setTitle("Click Me If You Can");
            setContentPane(getClickMeContentPane());
            initConnections();
            FrameUtilities.centerFrame(this);
        } catch (java.lang.Throwable ivjExc) {
            handleException(ivjExc);
        }
    }

    /**
     * Returns the ClickMe JFrame's ContentPane, which contains a score panel at the top, a "chase" panel (on which the ClickMe label moves around) in the center, and a button panel at the bottom.
     * 
     * @return a JPanel to be used as the frame's ContentPane
     */
    private JPanel getClickMeContentPane() {
        if (clickMeContentPane == null) {
            try {
                clickMeContentPane = new JPanel();
                clickMeContentPane.setName("ClickMeContentPane");
                clickMeContentPane.setBorder(new EtchedBorder());
                clickMeContentPane.setLayout(new BorderLayout(0, 0));
                clickMeContentPane.add(getScorePanel(), BorderLayout.NORTH);
                clickMeContentPane.add(getChasePanel(), BorderLayout.CENTER);
                clickMeContentPane.add(getButtonPanel(), BorderLayout.SOUTH);
                clickMeContentPane.setMinimumSize(new Dimension(200, 200));

                // clickMeContentPane.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                // clickMeContentPane.add(getScorePanel());
                // clickMeContentPane.add(getChasePanel());
                // clickMeContentPane.add(getButtonPanel());

            } catch (java.lang.Throwable ivjExc) {
                handleException(ivjExc);
            }
        }
        return clickMeContentPane;
    }

    /**
     * Handles ActionEvents on the Quit button by forwarding them to {@link ClickMeFrame#quitButton}.
     */
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == getQuitButton()) {
            this.quitButtonActionPerformed();
        }
    }

    /**
     * Handles delegated MouseDragged events by forwarding them to {@link ClickMeFrame#moveIt(MouseEvent)}.
     * 
     * @param mouseEvent
     *            - the MouseEvent that was generated by a mouse drag
     */
    public void clickMeChasePanelMouseDragged(MouseEvent mouseEvent) {
        moveIt(mouseEvent);
        return;
    }

    /**
     * Handles delegated MouseMoved events by forwarding them to {@link ClickMeFrame#moveIt(MouseEvent)}.
     * 
     * @param mouseEvent
     *            - the MouseEvent that was generated by a mouse move
     */
    public void clickMeChasePanelMouseMoved(MouseEvent mouseEvent) {
        moveIt(mouseEvent);
        return;
    }

    public void clickMeDlgComponentResized(ComponentEvent componentEvent) {

        // move label to (new) center of Chase Panel

        int midX = this.getChasePanel().getWidth() / 2;
        int midY = this.getChasePanel().getHeight() / 2;

        getClickMeLabel().setLocation(midX, midY);

        return;
    }

    /**
     * Handles the WindowClosing event by delegating it to the Quit Button action handler.
     */
    public void clickMeWindowClosing() {
        quitButtonActionPerformed();
        return;
    }

    /**
     * Handles a successful mouse click on the ClickMe label (the objective of the game) by changing the label text.
     * 
     * @param e
     */
    public void clickMeLabelMouseClicked(MouseEvent e) {
        this.getClickMeLabel().setVisible(false);
        this.getSuccessLabel().setVisible(true);
        successCount++;
        getScoreLabel().setText(SUCCESS_MESSAGE + successCount);
        this.repaint();
        return;
    }

    /**
     * Asks the user if they really want to quit, and disposes the frame if so.
     */
    public void quitButtonActionPerformed() {
        int result = javax.swing.JOptionPane.showConfirmDialog(this, "Awww... Are you sure you want to give up?", "Nice Try...", javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE);
        if (result == javax.swing.JOptionPane.YES_OPTION) {
            this.dispose();
        }
        return;
    }

    /**
     * ComponentHidden events are currently ignored.
     * 
     * @param e
     *            java.awt.event.ComponentEvent
     */
    public void componentHidden(ComponentEvent e) {
    }

    /**
     * ComponentMoved events are currently ignored.
     * 
     * @param e
     *            java.awt.event.ComponentEvent
     */
    public void componentMoved(ComponentEvent e) {
    }

    /**
     * Handles ComponentResized events for the frame by delegating them to {@link ClickMeFrame#clickMeDlgComponentResized(ComponentEvent)}.
     * 
     * @param e
     *            java.awt.event.ComponentEvent
     */
    public void componentResized(java.awt.event.ComponentEvent e) {
        if (e.getSource() == this) {
            this.clickMeDlgComponentResized(e);
        }
    }

    /**
     * Handles ComponentShown events for the frame.
     * 
     * @param e
     *            java.awt.event.ComponentEvent
     */
    public void componentShown(java.awt.event.ComponentEvent e) {
        // shouldn't we do something when the frame is shown, in case the user is switching
        // between frames to try to solve the puzzle?
        System.out.println("ComponentShown event fired...");
    }

    /**
     * Return the ClickMe Label.
     * 
     * @return javax.swing.JLabel
     */
    private JLabel getClickMeLabel() {
        if (clickMeLabel == null) {
            try {
                clickMeLabel = new JLabel();
                clickMeLabel.setName("ClickMeLabel");
                clickMeLabel.setPreferredSize(new Dimension(58, 30));
                clickMeLabel.setBorder(new EtchedBorder());
                clickMeLabel.setText("CLICK ME");
                clickMeLabel.setMinimumSize(new Dimension(58, 30));
            } catch (java.lang.Throwable ivjExc) {
                handleException(ivjExc);
            }
        }
        return clickMeLabel;
    }

    /**
     * Returns the point at the center of the ClickMe label.
     * 
     * @return java.awt.Point - the label center point
     */
    public java.awt.Point getClickMeLabelMidpoint() {

        int labelX = getClickMeLabel().getX();
        int labelY = getClickMeLabel().getY();

        int labelWidth = getClickMeLabel().getWidth();
        int labelHeight = getClickMeLabel().getHeight();

        int labelMidX = labelX + (labelWidth / 2);
        int labelMidY = labelY + (labelHeight / 2);

        return new java.awt.Point(labelMidX, labelMidY);

    }

    /**
     * Return the ClickMeQuitButton property value.
     * 
     * @return javax.swing.JButton
     */
    private javax.swing.JButton getQuitButton() {
        if (quitButton == null) {
            try {
                quitButton = new javax.swing.JButton();
                quitButton.setName("ClickMeQuitButton");
                quitButton.setText("I Give Up");
            } catch (java.lang.Throwable ivjExc) {
                handleException(ivjExc);
            }
        }
        return quitButton;
    }

    /**
     * Returns the distance between the specified X,Y point and the ClickMe label center
     * 
     * @param inX
     *            ,inY - the x/y coordinates of a point
     * 
     * @return the distance between the specified point and the ClickMe label center
     */
    public double getDistance(int inX, int inY) {

        Point midPoint = getClickMeLabelMidpoint();

        int labelMidX = midPoint.x;
        int labelMidY = midPoint.y;

        return Math.sqrt(Math.pow((labelMidX - inX), 2) + java.lang.Math.pow((labelMidY - inY), 2));

    }

    /**
     * Return the SuccessLabel property value.
     * 
     * @return javax.swing.JLabel
     */
    private JLabel getSuccessLabel() {
        if (successLabel == null) {
            successLabel = new javax.swing.JLabel();
            successLabel.setName("SuccessLabel");
            successLabel.setText("CONGRATULATIONS!!");
            successLabel.setVisible(false);
            successLabel.setForeground(new java.awt.Color(255, 0, 0));
            successLabel.setFont(new java.awt.Font("dialog", 1, 18));
            successLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        }
        return successLabel;
    }

    /**
     * Initializes connections
     */
    private void initConnections() throws java.lang.Exception {
        getQuitButton().addActionListener(this);
        getChasePanel().addMouseMotionListener(this);
        getClickMeLabel().addMouseListener(this);
        this.addComponentListener(this);
        this.addWindowListener(this);
    }

    private void handleException(Throwable t) {
        System.err.println("Exception in ClickMe frame: " + t);
    }

    /**
     * Handles MouseClicked on the ClickMe label by forwarding it to {@link #clickMeLabelMouseClicked()}; ignores all other mouse clicks.
     * 
     * @param e
     *            the MouseEvent generated by the mouse click
     */
    public void mouseClicked(MouseEvent e) {

        if (e.getSource() == getClickMeLabel()) {
            this.clickMeLabelMouseClicked(e);
        }
    }

    /**
     * Handles MouseDragged events by checking if the Drag was on the Chase Panel and if so delegating handling to {@link #clickMeChasePanelMouseDragged()}.
     * 
     * @param e
     *            the MouseEvent generated by a mouse drag
     */
    public void mouseDragged(java.awt.event.MouseEvent e) {
        if (e.getSource() == getChasePanel()) {
            clickMeChasePanelMouseDragged(e);
        }
    }

    /**
     * MouseEntered is currently ignored.
     * 
     * @param e
     *            java.awt.event.MouseEvent
     */
    public void mouseEntered(java.awt.event.MouseEvent e) {
    }

    /**
     * MouseExited is currently ignored.
     * 
     * @param e
     *            java.awt.event.MouseEvent
     */
    public void mouseExited(java.awt.event.MouseEvent e) {
    }

    /**
     * Handles mouse moved by checking the source of the mouse movement event and if was movement on the Chase Panel delegating handling to
     * {@link ClickMeFrame#clickMeChasePanelMouseMoved(MouseEvent)}.
     * 
     * @param e
     *            - the MouseEvent which was generated by the mouse being moved
     */
    public void mouseMoved(MouseEvent e) {
        if (e.getSource() == getChasePanel()) {
            clickMeChasePanelMouseMoved(e);
        }
    }

    /**
     * MousePressed events are ignored.
     */
    public void mousePressed(MouseEvent e) {
    }

    /**
     * MouseReleased events are ignored.
     */
    public void mouseReleased(MouseEvent e) {
    }

    /**
     * Invoked to move the ClickMe label away from the mouse.
     * 
     * @param mouseEvent
     *            - a mouse event which triggered the need to move the label
     */
    public void moveIt(MouseEvent mouseEvent) {

        java.awt.Point newLoc = null;

        // get the distance between the mouse and the label center
        double curDist = getDistance(mouseEvent.getX(), mouseEvent.getY());

        // determine the maximum distance possible given the current screen (panel size)
        int panelHeight = this.getChasePanel().getHeight();
        int panelWidth = this.getChasePanel().getWidth();

        double maxDist = java.lang.Math.sqrt(java.lang.Math.pow(panelHeight, 2) + java.lang.Math.pow(panelWidth, 2));

        // move the label if the mouse is "close" to it (and moving closer)
        if (((curDist < (maxDist / 2)) && (curDist < oldDist)) || (getClickMeLabel().contains(mouseEvent.getX(), mouseEvent.getY()))) {

            // determine how much to move the label based on how "close" the mouse is
            // (the closer the mouse, the larger the move amount)
            if (curDist == 0.0) {
                curDist = 1.0;
            } // avoid division by zero
            int moveAmount = (int) ((1.0 / curDist) * MAX_MOVE);

            // get the mouse and label-midpoint X,Y coordinates
            int mouseX = mouseEvent.getX();
            int mouseY = mouseEvent.getY();
            java.awt.Point midPoint = getClickMeLabelMidpoint();
            int labelMidX = midPoint.x;
            int labelMidY = midPoint.y;
            int labelXloc = getClickMeLabel().getX();
            int labelYloc = getClickMeLabel().getY();

            // determine a nominal (intended) new location for the label
            if ((mouseX <= labelMidX) && (mouseY <= labelMidY)) {
                // mouse is left and above label; move label right and down
                newLoc = new java.awt.Point(labelXloc + moveAmount, labelYloc + moveAmount);
            } else if ((mouseX <= labelMidX) && (mouseY > labelMidY)) {
                // mouse is left and below label; move label right and up
                newLoc = new java.awt.Point(labelXloc + moveAmount, labelYloc - moveAmount);
            } else if ((mouseX > labelMidX) && (mouseY <= labelMidY)) {
                // mouse is right and above label; move label left and down
                newLoc = new java.awt.Point(labelXloc - moveAmount, labelYloc + moveAmount);
            } else {
                // mouse is right and below label; move label left and up
                newLoc = new java.awt.Point(labelXloc - moveAmount, labelYloc - moveAmount);
            }

            // check if moving would put the label past an edge
            if ((newLoc.x < 15) || (newLoc.x > (panelWidth - getClickMeLabel().getWidth() - 15)) || (newLoc.y < 15) || (newLoc.y > (panelHeight - getClickMeLabel().getHeight() - 15))) {
                // close to edge; move only if mouse is 'real close'
                if (curDist < (getClickMeLabel().getWidth() + 10)) {

                    // at edge and mouse too close; move to random location
                    double rx = java.lang.Math.random();
                    double ry = java.lang.Math.random();

                    newLoc.x = (int) ((panelWidth - getClickMeLabel().getWidth()) * rx);
                    newLoc.y = (int) ((panelHeight - getClickMeLabel().getHeight()) * ry);
                    getClickMeLabel().setLocation(newLoc);
                    curDist = getDistance(mouseEvent.getX(), mouseEvent.getY());
                }
            } else {

                // not near an edge; move label
                getClickMeLabel().setLocation(newLoc);
                curDist = getDistance(mouseEvent.getX(), mouseEvent.getY());
            }
        }

        // save the current distance for next time
        oldDist = curDist;

        return;
    }

    /**
     * Methods to handle events for the WindowListener interface.
     */
    public void windowActivated(java.awt.event.WindowEvent e) {
        this.clickMeWindowActivated(e);
    }

    private void clickMeWindowActivated(WindowEvent e) {
        // add code here to move the label so hiding/restoring the window isn't as obvious
        // a winning strategy.
        // Move the label to a new position which can be determined algorithmically if a
        // user is observant enough (e.g. the center of one of four quadrants chosen
        // based on the current clock time or something...
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        this.clickMeWindowClosing();
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    /**
     * main entry point - starts the part when it is run as an application
     * 
     * @param args
     *            java.lang.String[]
     */
    public static void main(java.lang.String[] args) {
        try {
            JFrame frame = new ClickMeFrame();
            frame.setVisible(true);
        } catch (Throwable exception) {
            System.err.println("Exception occurred in main() while creating a new ClickMe frame");
            exception.printStackTrace(System.out);
        }
    }

    private JPanel getScorePanel() {
        if (scorePanel == null) {
            scorePanel = new JPanel();
            scorePanel.setBorder(new EtchedBorder());
            scorePanel.add(getScoreLabel());
            scorePanel.setMinimumSize(new Dimension(100, 100));
        }
        return scorePanel;
    }

    private JLabel getScoreLabel() {
        if (scoreLabel == null) {
            scoreLabel = new JLabel(SUCCESS_MESSAGE + successCount);
        }
        return scoreLabel;
    }

    private JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel();
            buttonPanel.setBorder(new EtchedBorder());
            buttonPanel.add(getQuitButton());
            buttonPanel.add(getReplayButton());
        }
        return buttonPanel;
    }

    private JPanel getChasePanel() {
        if (chasePanel == null) {
            chasePanel = new JPanel();
            chasePanel.setBorder(new LineBorder(Color.blue, 2, true));
            chasePanel.add(getClickMeLabel());
            chasePanel.add(getSuccessLabel());
            chasePanel.setMinimumSize(new Dimension(100, 100));
        }
        return chasePanel;
    }

    private JButton getReplayButton() {
        if (replayButton == null) {
            try {
                replayButton = new JButton();
                replayButton.setName("ReplayButton");
                replayButton.setText("Replay");
                replayButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // System.out.println ("Replay pushed");
                        getSuccessLabel().setVisible(false);
                        getClickMeLabel().setVisible(true);
                    }
                });
            } catch (Throwable t) {
                handleException(t);
            }
        }
        return replayButton;
    }
}
