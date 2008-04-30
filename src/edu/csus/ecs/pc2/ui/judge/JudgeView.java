package edu.csus.ecs.pc2.ui.judge;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import edu.csus.ecs.pc2.VersionInfo;
import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.model.ContestTime;
import edu.csus.ecs.pc2.core.model.ContestTimeEvent;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.model.IContestTimeListener;
import edu.csus.ecs.pc2.ui.ClarificationsPane;
import edu.csus.ecs.pc2.ui.FrameUtilities;
import edu.csus.ecs.pc2.ui.JPanePlugin;
import edu.csus.ecs.pc2.ui.LogWindow;
import edu.csus.ecs.pc2.ui.OptionsPanel;
import edu.csus.ecs.pc2.ui.RunsPanel;
import edu.csus.ecs.pc2.ui.SubmissionBiffPane;
import edu.csus.ecs.pc2.ui.SubmitRunPane;
import edu.csus.ecs.pc2.ui.UIPlugin;

/**
 * Judge GUI.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class JudgeView extends JFrame implements UIPlugin {

    /**
     * 
     */
    private static final long serialVersionUID = 5365837218548110171L;

    private IInternalContest contest;

    @SuppressWarnings("unused")
    private IInternalController controller;

    private JTabbedPane mainTabbedPane = null;

    private LogWindow logWindow = null;

    private JPanel messagePane = null;

    private JPanel centerPane = null;

    private JPanel mainPane = null;

    private JLabel messageLabel = null;

    private JPanel exitPane = null;

    private JButton exitButton = null;

    private JLabel timeLabel = null;

    private JPanel northPane = null;

    private JPanel judgeBiffPane = null;
    
    private static boolean alreadyJudgingRun = false;

    public JudgeView() {
        super();
        initialize();
    }

    private void initialize() {
        this.setSize(new java.awt.Dimension(800, 515));
        this.setContentPane(getMainPane());
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("PC^2 Judge - Not Logged In ");
        FrameUtilities.centerFrame(this);
        setVisible(true);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                promptAndExit();
            }
        });

    }

    protected void promptAndExit() {
        int result = FrameUtilities.yesNoCancelDialog("Are you sure you want to exit PC^2?", "Exit PC^2");

        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    /**
     * This method initializes mainTabbedPane
     * 
     * @return javax.swing.JTabbedPane
     */
    private JTabbedPane getMainTabbedPane() {
        if (mainTabbedPane == null) {
            mainTabbedPane = new JTabbedPane();
        }
        return mainTabbedPane;
    }

    protected void addUIPlugin(JTabbedPane tabbedPane, String tabTitle, JPanePlugin plugin) {

        plugin.setContestAndController(contest, controller);
        tabbedPane.add(plugin, tabTitle);
    }

    private void setFrameTitle(final boolean contestStarted) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (contestStarted) {
                    setTitle("PC^2 Judge " + contest.getTitle() + " [STARTED] Build " + new VersionInfo().getBuildNumber());
                    timeLabel.setText("");
                } else {
                    setTitle("PC^2 Judge " + contest.getTitle() + " [STOPPED] Build " + new VersionInfo().getBuildNumber());
                    timeLabel.setText("STOPPED");
                }
            }
        });
        FrameUtilities.regularCursor(this);
    }

    public void setContestAndController(IInternalContest inContest, IInternalController inController) {
        this.contest = inContest;
        this.controller = inController;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (logWindow == null) {
                    logWindow = new LogWindow();
                }
                logWindow.setContestAndController(contest, controller);
                logWindow.setTitle("Log " + contest.getClientId().toString());
        
                contest.addContestTimeListener(new ContestTimeListenerImplementation());
        
                setFrameTitle(contest.getContestTime().isContestRunning());
                showMessage("");
        
                RunsPanel newRunsPane = new RunsPanel();
                newRunsPane.setShowNewRunsOnly(true);
                newRunsPane.setMakeSoundOnOneRun(true);
                addUIPlugin(getMainTabbedPane(), "New Runs", newRunsPane);
        
                RunsPanel runsPanel = new RunsPanel();
                addUIPlugin(getMainTabbedPane(), "All Runs", runsPanel);
        
                ClarificationsPane newClarificationsPane = new ClarificationsPane();
                newClarificationsPane.setShowNewClarificationsOnly(true);
                addUIPlugin(getMainTabbedPane(), "New Clars", newClarificationsPane);
        
                ClarificationsPane clarificationsPane = new ClarificationsPane();
                addUIPlugin(getMainTabbedPane(), "All clarifications", clarificationsPane);
        
                SubmitRunPane submitRunPane = new SubmitRunPane();
                addUIPlugin(getMainTabbedPane(), "Test Run", submitRunPane);
        
                OptionsPanel optionsPanel = new OptionsPanel();
                addUIPlugin(getMainTabbedPane(), "Options", optionsPanel);
                optionsPanel.setLogWindow(logWindow);
                
                SubmissionBiffPane submissionBiffPane = new SubmissionBiffPane();
                getJudgeBiffPane().add(submissionBiffPane, java.awt.BorderLayout.CENTER);
                submissionBiffPane.setContestAndController(contest, controller);
            }
        });

    }

    public String getPluginTitle() {
        return "Judge Main GUI";
    }

    protected void showLog(boolean showLogWindow) {
        logWindow.setVisible(showLogWindow);
    }

    /**
     * This method initializes messagePane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getMessagePane() {
        if (messagePane == null) {
            timeLabel = new JLabel();
            timeLabel.setText("STOPPED ");
            messageLabel = new JLabel();
            messageLabel.setText("JLabel");
            messageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            messagePane = new JPanel();
            messagePane.setLayout(new BorderLayout());
            messagePane.add(timeLabel, java.awt.BorderLayout.WEST);
            messagePane.add(messageLabel, java.awt.BorderLayout.CENTER);
            messagePane.add(getExitPane(), java.awt.BorderLayout.EAST);
        }
        return messagePane;
    }

    /**
     * This method initializes centerPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getCenterPane() {
        if (centerPane == null) {
            centerPane = new JPanel();
            centerPane.setLayout(new BorderLayout());
            centerPane.add(getMainTabbedPane(), java.awt.BorderLayout.CENTER);
        }
        return centerPane;
    }

    /**
     * This method initializes mainPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getMainPane() {
        if (mainPane == null) {
            mainPane = new JPanel();
            mainPane.setLayout(new BorderLayout());
            mainPane.add(getCenterPane(), java.awt.BorderLayout.CENTER);
            mainPane.add(getNorthPane(), java.awt.BorderLayout.NORTH);
        }
        return mainPane;
    }

    /**
     * This method initializes exitPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getExitPane() {
        if (exitPane == null) {
            exitPane = new JPanel();
            exitPane.add(getExitButton(), null);
        }
        return exitPane;
    }

    /**
     * This method initializes exitButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getExitButton() {
        if (exitButton == null) {
            exitButton = new JButton();
            exitButton.setText("Exit");
            exitButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    promptAndExit();
                }
            });
        }
        return exitButton;
    }

    private void showMessage(final String string) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                messageLabel.setText(string);
                messageLabel.setToolTipText(string);
            }
        });

    }

    protected boolean isThisSite(int siteNumber) {
        return contest.getSiteNumber() == siteNumber;
    }

    /**
     * 
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */

    class ContestTimeListenerImplementation implements IContestTimeListener {

        public void contestTimeAdded(ContestTimeEvent event) {
            contestTimeChanged(event);
        }

        public void contestTimeRemoved(ContestTimeEvent event) {
            contestTimeChanged(event);
        }

        public void contestTimeChanged(ContestTimeEvent event) {
            ContestTime contestTime = event.getContestTime();
            if (isThisSite(contestTime.getSiteNumber())) {
                setFrameTitle(contestTime.isContestRunning());
            }
        }

        public void contestStarted(ContestTimeEvent event) {
            contestTimeChanged(event);
        }

        public void contestStopped(ContestTimeEvent event) {
            contestTimeChanged(event);
        }

    }

    /**
     * This method initializes northPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getNorthPane() {
        if (northPane == null) {
            northPane = new JPanel();
            northPane.setLayout(new BorderLayout());
            northPane.setPreferredSize(new java.awt.Dimension(65,65));
            northPane.add(getMessagePane(), java.awt.BorderLayout.NORTH);
            northPane.add(getJudgeBiffPane(), java.awt.BorderLayout.CENTER);
        }
        return northPane;
    }

    /**
     * This method initializes judgeBiffPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJudgeBiffPane() {
        if (judgeBiffPane == null) {
            judgeBiffPane = new JPanel();
            judgeBiffPane.setLayout(new BorderLayout());
            judgeBiffPane.setPreferredSize(new java.awt.Dimension(35, 35));
        }
        return judgeBiffPane;
    }

    public static boolean isAlreadyJudgingRun() {
        return alreadyJudgingRun;
    }

    public static void setAlreadyJudgingRun(boolean alreadyJudgingRun) {
        JudgeView.alreadyJudgingRun = alreadyJudgingRun;
    }

} // @jve:decl-index=0:visual-constraint="10,10"
