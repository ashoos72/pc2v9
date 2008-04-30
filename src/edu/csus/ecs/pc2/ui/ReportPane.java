package edu.csus.ecs.pc2.ui;

import java.awt.BorderLayout;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import edu.csus.ecs.pc2.VersionInfo;
import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.log.Log;
import edu.csus.ecs.pc2.core.model.Account;
import edu.csus.ecs.pc2.core.model.Filter;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.report.AccountPermissionReport;
import edu.csus.ecs.pc2.core.report.AccountsReport;
import edu.csus.ecs.pc2.core.report.AllReports;
import edu.csus.ecs.pc2.core.report.BalloonSettingsReport;
import edu.csus.ecs.pc2.core.report.BalloonSummaryReport;
import edu.csus.ecs.pc2.core.report.ClarificationsReport;
import edu.csus.ecs.pc2.core.report.ClientSettingsReport;
import edu.csus.ecs.pc2.core.report.ContestAnalysisReport;
import edu.csus.ecs.pc2.core.report.ContestReport;
import edu.csus.ecs.pc2.core.report.EvaluationReport;
import edu.csus.ecs.pc2.core.report.FastestSolvedReport;
import edu.csus.ecs.pc2.core.report.GroupsReport;
import edu.csus.ecs.pc2.core.report.IReport;
import edu.csus.ecs.pc2.core.report.InternalDumpReport;
import edu.csus.ecs.pc2.core.report.JudgementReport;
import edu.csus.ecs.pc2.core.report.LanguagesReport;
import edu.csus.ecs.pc2.core.report.ListRunLanguages;
import edu.csus.ecs.pc2.core.report.LoginReport;
import edu.csus.ecs.pc2.core.report.OldRunsReport;
import edu.csus.ecs.pc2.core.report.ProblemsReport;
import edu.csus.ecs.pc2.core.report.RunsByTeamReport;
import edu.csus.ecs.pc2.core.report.RunsReport;
import edu.csus.ecs.pc2.core.report.SolutionsByProblemReport;
import edu.csus.ecs.pc2.core.report.StandingsReport;

/**
 * Report Pane, allows picking and viewing reports.
 * 
 * @author pc2@ecs.csus.edu
 */

// $HeadURL$
// $Id$
public class ReportPane extends JPanePlugin {

    /**
     * 
     */
    private static final long serialVersionUID = -5165297328068331675L;

    private JPanel jPanel = null;

    private JPanel buttonPane = null;

    private JPanel mainPane = null;

    private JButton viewReportButton = null;

    private JCheckBox thisSiteCheckBox = null;

    private JPanel reportChoicePane = null;

    private JComboBox reportsComboBox = null;

    private JLabel messageLabel = null;

    /**
     * List of reports.
     */
    private IReport[] listOfReports;

    private Log log;

    private String reportDirectory = "reports";

    private JCheckBox thisClientFilterButton = null;
    
    public String getReportDirectory() {
        return reportDirectory;
    }

    /**
     * This method can change the directory that the reports will be written to.
     * The default is "reports".
     * 
     * @param reportDirectory what directory to write the reports to
     */
    public void setReportDirectory(String reportDirectory) {
        this.reportDirectory = reportDirectory;
    }

    /**
     * This method initializes
     * 
     */
    public ReportPane() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.setLayout(new BorderLayout());
        this.setSize(new java.awt.Dimension(505, 251));
        this.add(getJPanel(), java.awt.BorderLayout.NORTH);
        this.add(getButtonPane(), java.awt.BorderLayout.SOUTH);
        this.add(getMainPane(), java.awt.BorderLayout.CENTER);

        // populate list of reports
        listOfReports = new IReport[23];
        int repNo = 0;
        listOfReports[repNo++] = new AccountsReport();
        listOfReports[repNo++] = new BalloonSummaryReport();
        
        listOfReports[repNo++] = new AllReports();
        listOfReports[repNo++] = new ContestReport();
        
        listOfReports[repNo++] = new ContestAnalysisReport();
        listOfReports[repNo++] = new SolutionsByProblemReport();
        listOfReports[repNo++] = new ListRunLanguages();
        listOfReports[repNo++] = new FastestSolvedReport();
        
        listOfReports[repNo++] = new StandingsReport();
        listOfReports[repNo++] = new LoginReport();
        
        listOfReports[repNo++] = new RunsReport();
        listOfReports[repNo++] = new ClarificationsReport();
        listOfReports[repNo++] = new ProblemsReport();
        listOfReports[repNo++] = new LanguagesReport();
        
        listOfReports[repNo++] = new JudgementReport();
        listOfReports[repNo++] = new RunsByTeamReport();
        listOfReports[repNo++] = new BalloonSettingsReport();
        listOfReports[repNo++] = new ClientSettingsReport();
        listOfReports[repNo++] = new GroupsReport();
        
        listOfReports[repNo++] = new EvaluationReport();
        
        listOfReports[repNo++] = new OldRunsReport();
        listOfReports[repNo++] = new AccountPermissionReport();
        listOfReports[repNo++] = new InternalDumpReport();
        
    }

    public void setContestAndController(IInternalContest inContest, IInternalController inController) {
        super.setContestAndController(inContest, inController);

        this.log = getController().getLog();
        refreshGUI();
    }

    protected void refreshGUI() {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                refreshReportComboBox();
            }
        });
    }

    private void refreshReportComboBox() {

        getReportsComboBox().removeAllItems();

        for (IReport report : listOfReports) {
            getReportsComboBox().addItem(report.getReportTitle());
        }
        
        getReportsComboBox().setSelectedIndex(0);

    }

    @Override
    public String getPluginTitle() {
        return "Reports Pane";
    }

    /**
     * This method initializes jPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            messageLabel = new JLabel();
            messageLabel.setText("");
            messageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jPanel = new JPanel();
            jPanel.setLayout(new BorderLayout());
            jPanel.setPreferredSize(new java.awt.Dimension(30, 30));
            jPanel.add(messageLabel, java.awt.BorderLayout.CENTER);
        }
        return jPanel;
    }

    /**
     * This method initializes buttonPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getButtonPane() {
        if (buttonPane == null) {
            buttonPane = new JPanel();
            buttonPane.setPreferredSize(new java.awt.Dimension(45, 45));
            buttonPane.add(getViewReportButton(), null);
        }
        return buttonPane;
    }

    /**
     * This method initializes mainPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getMainPane() {
        if (mainPane == null) {
            mainPane = new JPanel();
            mainPane.setLayout(null);
            mainPane.add(getThisSiteCheckBox(), null);
            mainPane.add(getReportChoicePane(), null);
            mainPane.add(getThisClientFilterButton(), null);
        }
        return mainPane;
    }

    /**
     * This method initializes viewReportButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getViewReportButton() {
        if (viewReportButton == null) {
            viewReportButton = new JButton();
            viewReportButton.setText("View Report");
            viewReportButton.setMnemonic(java.awt.event.KeyEvent.VK_V);
            viewReportButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    generateSelectedReport();
                }
            });
        }
        return viewReportButton;
    }

    private String getFileName() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM.dd.SSS");
        // "yyMMdd HHmmss.SSS");
        return "report." + simpleDateFormat.format(new Date()) + ".txt";

    }
    
    private void viewFile (String filename, String title){
        MultipleFileViewer multipleFileViewer = new MultipleFileViewer(log);
        multipleFileViewer.addFilePane(title, filename);
        multipleFileViewer.setTitle("PC^2 Report (Build "+new VersionInfo().getBuildNumber()+")");
        FrameUtilities.centerFrameFullScreenHeight(multipleFileViewer);
        multipleFileViewer.setVisible(true);
    }

    protected void generateSelectedReport() {

        // TODO code populate filter.
        Filter filter = new Filter();
        
        // Site filter
        if (getThisSiteCheckBox().isSelected()){
            filter.setSiteNumber(getContest().getSiteNumber());
            filter.setThisSiteOnly(true);
        }
        
        // Client filter
        if (getThisClientFilterButton().isSelected()){
            Account account = getContest().getAccount(getContest().getClientId());
            if (account != null){
                filter.addAccount(account);
            }
        }

        try {
            
            String filename = getFileName();
            File reportDirectoryFile = new File(getReportDirectory());
            if (reportDirectoryFile.exists()) {
                if (reportDirectoryFile.isDirectory()) {
                    filename = reportDirectoryFile.getCanonicalPath() + File.separator + filename;
                }
            } else {
                if (reportDirectoryFile.mkdirs()) {
                    filename = reportDirectoryFile.getCanonicalPath() + File.separator + filename;
                }
            }
            IReport selectedReport = null;
            
            String selectedReportTitle = (String) getReportsComboBox().getSelectedItem();
            for (IReport report : listOfReports) {
                if (selectedReportTitle.equals(report.getReportTitle())) {
                    selectedReport = report;
                }
            }
            
            selectedReport.setContestAndController(getContest(), getController());
            selectedReport.createReportFile(filename, filter);
            viewFile (filename, selectedReport.getReportTitle());

        } catch (Exception e) {
            log.log(Log.WARNING, "Exception logged ", e);
            showMessage("Unable to output report, check logs");
        }

    }

    /**
     * show message to user
     * @param string
     */
    private void showMessage(final String string) {
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                messageLabel.setText(string);
            }
        });
        
    }

    /**
     * This method initializes thisSiteCheckBox
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getThisSiteCheckBox() {
        if (thisSiteCheckBox == null) {
            thisSiteCheckBox = new JCheckBox();
            thisSiteCheckBox.setBounds(new java.awt.Rectangle(30, 87, 165, 21));
            thisSiteCheckBox.setMnemonic(java.awt.event.KeyEvent.VK_F);
            thisSiteCheckBox.setText("Filter for this site only");
        }
        return thisSiteCheckBox;
    }

    /**
     * This method initializes reportChoicePane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getReportChoicePane() {
        if (reportChoicePane == null) {
            reportChoicePane = new JPanel();
            reportChoicePane.setLayout(new BorderLayout());
            reportChoicePane.setBounds(new java.awt.Rectangle(31, 16, 445, 53));
            reportChoicePane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Reports", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                    javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
            reportChoicePane.add(getReportsComboBox(), java.awt.BorderLayout.CENTER);
        }
        return reportChoicePane;
    }

    /**
     * This method initializes reportsComboBox
     * 
     * @return javax.swing.JComboBox
     */
    private JComboBox getReportsComboBox() {
        if (reportsComboBox == null) {
            reportsComboBox = new JComboBox();
        }
        return reportsComboBox;
    }

    /**
     * This method initializes thisClientFilterButton
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getThisClientFilterButton() {
        if (thisClientFilterButton == null) {
            thisClientFilterButton = new JCheckBox();
            thisClientFilterButton.setBounds(new java.awt.Rectangle(30,121,213,21));
            thisClientFilterButton.setMnemonic(java.awt.event.KeyEvent.VK_C);
            thisClientFilterButton.setText("Filter for this client only");
        }
        return thisClientFilterButton;
    }

} // @jve:decl-index=0:visual-constraint="10,10"
