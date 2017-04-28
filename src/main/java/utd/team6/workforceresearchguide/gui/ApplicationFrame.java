/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utd.team6.workforceresearchguide.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import utd.team6.workforceresearchguide.gui.repscan.RepositoryScanDialog;
import utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException;
import javax.swing.ListCellRenderer;
import utd.team6.workforceresearchguide.lucene.ReadSessionNotStartedException;
import utd.team6.workforceresearchguide.main.ApplicationController;
import utd.team6.workforceresearchguide.main.DocumentData;

/**
 *
 * @author Michael
 */
public final class ApplicationFrame extends javax.swing.JFrame {

    private static final String NEW_GROUP_TEXT = "New Group";

    private static final String LUCENE_FILE_PATH = "_lucene_files_";
    private static final String DATABASE_PATH = "lucene.db";

    /**
     * This is the key used to store the repository path in the properties file.
     */
    public static final String REPOSITORY_PATH_KEY = "repository";

    private static final Color SEARCH_BAR_INACTIVE_COLOR = new Color(102, 102, 102);
    private static final Color SEARCH_BAR_ACTIVE_COLOR = new Color(0, 0, 0);
    private static final String SEARCH_BAR_INACTIVE_TEXT = "Search";

    private static final String PROPERTIES_PATH = "WorkforceResearchGuide.properties";

    private final Properties properties;

    private String repPath;
    private List<DocumentData> results;

    private final ApplicationController app;

    HashSet<String> existingTagFilters;
    HashSet<String> appliedTagFilters;

    /**
     * Creates new form ApplicationFrame
     */
    public ApplicationFrame() {
        initComponents();

        //Load in the properties file
        properties = new Properties();

        app = new ApplicationController(LUCENE_FILE_PATH, DATABASE_PATH);

        existingTagFilters = new HashSet<>();
        appliedTagFilters = new HashSet<>();
    }

    /**
     * Loads the necessary parts of the application. This is called after the
     * frame is instantiated.
     */
    public void load() {
        //Load the properties file, if it exists
        try {
            File f = new File(PROPERTIES_PATH);
            if (!f.exists()) {
                f.createNewFile();
            }
            InputStream stream = new FileInputStream(f);
            properties.load(stream);
        } catch (IOException ex) {
            Logger.getLogger(ApplicationFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Get the repository path
        loadRepositoryPath();
        if (repPath == null) {
            beginUserRepositorySelection();
        }
        //Make sure the database tables are created
        app.updateDatabaseSchema();

        //Load the groups
        ArrayList<String> groups = app.getGroups();
        groupComboBox.addItem(NEW_GROUP_TEXT);
        for (String group : groups) {
            groupComboBox.addItem(group);
        }
    }

    /**
     * Updates a property of the property file.
     *
     * @param key
     * @param value
     */
    public void updateProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * Saves all properties to the property file.
     */
    public void saveProperties() {
        try {
            properties.store(new FileOutputStream(PROPERTIES_PATH), null);
        } catch (IOException ex) {
            Logger.getLogger(ApplicationFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Sets the repPath variable and the properties file.
     *
     * @param path
     */
    public void setRepositoryPath(String path) {
        repPath = path;
        updateProperty(REPOSITORY_PATH_KEY, path);
    }

    /**
     * Sets the repPath variable to match what is in the properties file.
     */
    public void loadRepositoryPath() {
        repPath = (String) properties.getProperty("repository");
    }

    /**
     * Opens a dialog for handling repository scanning.
     */
    public void scanRepository() {
        RepositoryScanDialog rsd = new RepositoryScanDialog(app.getInfoFactory(), app.generateFileSyncManager(repPath));
        rsd.setLocationRelativeTo(this);
        try {
            rsd.showDialog();
        } catch (ConnectionNotStartedException ex) {
            Logger.getLogger(ApplicationFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Shows the necessary dialogs to allow the user to select a repository.
     */
    public void beginUserRepositorySelection() {
        //This must be a fresh application, a new path needs to be set
        RepositoryChooser repChooser = new RepositoryChooser(repPath);
        repChooser.setLocationRelativeTo(this);
        String path = repChooser.showDialog();
        if (path == null && repPath == null) {
            JOptionPane.showMessageDialog(this, "A repository must be selected.\nThe application will now close.");
            System.exit(0);
        } else if (path != null && !path.equals(repPath)) {
            setRepositoryPath(path);
            int result = JOptionPane.showOptionDialog(this, "Would you like to scan the repository and sync it with the system?", "Scan Repository", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
            this.saveProperties();
            if (result == JOptionPane.YES_OPTION) {
                //Scan the repository
                scanRepository();
            }
        }

    }
    
    public void addDocumentDiaplay(DocumentDisplay disp){
        resultPanel.add(disp);
    }
    
    public void updateResultDisplay(HashMap<Integer,DocumentDisplay> map){
        //Grab the value set
        
        //Sort the value set
        
        //Re-add the values
        resultPanel.removeAll();
        
    }

    /**
     * This function is called when the user initiates a search.
     */
    public void startSearch() {

    }

    public void clearAllTagFilters() {
        existingTagFilters.clear();
        appliedTagFilters.clear();
    }

    public void addTagFilter(String tag) {
        boolean inserted = existingTagFilters.add(tag);
        if (inserted) {
            //Add to the panel
            final JToggleButton button = new JToggleButton(tag);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (button.isSelected()) {
                        //Apply the filter
                        appliedTagFilters.add(button.getText());
                    } else {
                        //Remove the filter
                        appliedTagFilters.remove(button.getText());
                    }
                }
            });
            tagFilterPanel.add(button);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jScrollPane3 = new javax.swing.JScrollPane();
        jSplitPane3 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        searchBar = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane2 = new javax.swing.JScrollPane();
        resultPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        groupComboBox = new javax.swing.JComboBox<>();
        jScrollPane5 = new javax.swing.JScrollPane();
        groupPanel = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        tagFilterPanel = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenu = new javax.swing.JMenu();
        openFileMenuItem = new javax.swing.JMenuItem();
        openGroupMenuItem = new javax.swing.JMenuItem();
        openTagMenuItem = new javax.swing.JMenuItem();
        scanRepositoryMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        propertiesMenu = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Workforce Research Guide");

        jSplitPane1.setDividerLocation(150);

        jSplitPane2.setDividerLocation(500);

        jLabel1.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        jLabel1.setText("Document Data");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane3)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane2.setRightComponent(jPanel4);

        jSplitPane3.setDividerLocation(500);
        jSplitPane3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        searchBar.setForeground(new java.awt.Color(102, 102, 102));
        searchBar.setText("Search");
        searchBar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchBarActionPerformed(evt);
            }
        });
        searchBar.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                searchBarFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                searchBarFocusLost(evt);
            }
        });

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        javax.swing.GroupLayout resultPanelLayout = new javax.swing.GroupLayout(resultPanel);
        resultPanel.setLayout(resultPanelLayout);
        resultPanelLayout.setHorizontalGroup(
            resultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 470, Short.MAX_VALUE)
        );
        resultPanelLayout.setVerticalGroup(
            resultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 425, Short.MAX_VALUE)
        );

        jScrollPane2.setViewportView(resultPanel);

        cancelButton.setText("x");
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(searchBar, javax.swing.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addComponent(jSeparator1))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(searchBar)
                    .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane3.setTopComponent(jPanel2);

        groupComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                groupComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout groupPanelLayout = new javax.swing.GroupLayout(groupPanel);
        groupPanel.setLayout(groupPanelLayout);
        groupPanelLayout.setHorizontalGroup(
            groupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 495, Short.MAX_VALUE)
        );
        groupPanelLayout.setVerticalGroup(
            groupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 200, Short.MAX_VALUE)
        );

        jScrollPane5.setViewportView(groupPanel);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(groupComboBox, 0, 497, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(groupComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane3.setRightComponent(jPanel3);

        jSplitPane2.setLeftComponent(jSplitPane3);

        jSplitPane1.setRightComponent(jSplitPane2);

        jLabel2.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        jLabel2.setText("Filter By Tag");

        tagFilterPanel.setLayout(new javax.swing.BoxLayout(tagFilterPanel, javax.swing.BoxLayout.Y_AXIS));
        jScrollPane1.setViewportView(tagFilterPanel);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                    .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 665, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel5);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 881, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );

        fileMenu.setText("File");

        openMenu.setText("Open");

        openFileMenuItem.setText("File");
        openMenu.add(openFileMenuItem);

        openGroupMenuItem.setText("Group");
        openMenu.add(openGroupMenuItem);

        openTagMenuItem.setText("Tag");
        openMenu.add(openTagMenuItem);

        fileMenu.add(openMenu);

        scanRepositoryMenuItem.setText("Scan Repository");
        scanRepositoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scanRepositoryMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(scanRepositoryMenuItem);

        jMenuBar1.add(fileMenu);

        editMenu.setText("Edit");

        jMenuItem3.setText("Documents");
        editMenu.add(jMenuItem3);

        jMenuItem4.setText("Tags");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        editMenu.add(jMenuItem4);

        jMenuItem5.setText("Groups");
        editMenu.add(jMenuItem5);

        jMenuBar1.add(editMenu);

        propertiesMenu.setText("Properties");

        jMenuItem2.setText("Repository");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        propertiesMenu.add(jMenuItem2);

        jMenuItem6.setText("Sync Options");
        propertiesMenu.add(jMenuItem6);

        jMenuBar1.add(propertiesMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void searchBarFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_searchBarFocusGained
        if (searchBar.getForeground().equals(SEARCH_BAR_INACTIVE_COLOR)) {
            searchBar.setForeground(SEARCH_BAR_ACTIVE_COLOR);
            searchBar.setText("");
        }
    }//GEN-LAST:event_searchBarFocusGained

    private void searchBarFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_searchBarFocusLost
        if (searchBar.getForeground().equals(SEARCH_BAR_ACTIVE_COLOR) && searchBar.getText().isEmpty()) {
            searchBar.setForeground(SEARCH_BAR_INACTIVE_COLOR);
            searchBar.setText(SEARCH_BAR_INACTIVE_TEXT);
        }
    }//GEN-LAST:event_searchBarFocusLost

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        //Cancel the search

        //Disable the cancel button
        cancelButton.setEnabled(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void searchBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchBarActionPerformed
        //Check if there is any content
        String query = searchBar.getText();
        if (!query.isEmpty()) {
            //Enable the cancelation buttion
            cancelButton.setEnabled(true);
            try {
                //Start the search
                app.beginSearch(query);
            } catch (IOException | ReadSessionNotStartedException ex) {
                Logger.getLogger(ApplicationFrame.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }//GEN-LAST:event_searchBarActionPerformed

    /**
     * Get the search results of query and display in JList
     *
     * @param query
     */
    private void generateSearchResults(String query) throws InterruptedException {

        //TO DO: get search results in array form??
        //3. Clicking on JList item should return the index# of the
        //   DocumentData.
        //4. Selected JList documents should be stored in another List and
        //   placed on the bottom panel to keep running List of relevant
        //   search documents for later viewing.
        //5. Unselecting a JList item should remove that item from secondary
        //   List.
        //temporary pause waiting for search to finish
        //while (app.searchRunning()) {
        //    TimeUnit.SECONDS.sleep(1);
        //}
        
        results = app.getDocResults();
        String[] resultsList = new String[results.size()];
        int i = 0;
        for (DocumentData data : results) {
            resultsList[i] = (i + 1) + ". \t" + data.getName() + "\t"
                    + data.getResultScore();
            i++;
        }
        displaySearchResults(resultsList);

    }

    private void displaySearchResults(final String[] newResults) {
        
    }
    
    

    /**
     * Handles a change in the group selection.
     */
    public void groupChanged() {
        if(groupComboBox.getSelectedItem().equals(NEW_GROUP_TEXT)){
            groupPanel.removeAll();
            return;
        }
        //Load the group documents
        ArrayList<String> docs = app.getGroupDocuments((String)groupComboBox.getSelectedItem());
        for (String doc : docs) {
            DocumentDisplay display = new DocumentDisplay(new DocumentData(doc),null);
        }
    }

    /**
     * Handles adding a document to the specified group.
     *
     * @param group
     */
    public void addDocumentToGroup(String docPath) {
        String selection = (String) groupComboBox.getSelectedItem();
        if (selection.equals(NEW_GROUP_TEXT)) {
            //Create a new group
            String groupName = JOptionPane.showInputDialog(this, "Name the new group.");
            if (groupName != null && !groupName.isEmpty()) {
                return;
            }
            app.addGroup(groupName);
            app.addDocumentToGroup(groupName, docPath);
            //Add the group to the dropdown
            groupComboBox.addItem(groupName);
            groupComboBox.setSelectedItem(groupName);
        } else {
            //Add to the selected group
            app.addDocumentToGroup((String)groupComboBox.getSelectedItem(), docPath);
        }
        //Add the document to the panel
        groupChanged();
    }

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        beginUserRepositorySelection();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void scanRepositoryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scanRepositoryMenuItemActionPerformed
        scanRepository();
    }//GEN-LAST:event_scanRepositoryMenuItemActionPerformed

    private void groupComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_groupComboBoxActionPerformed
        groupChanged();
    }//GEN-LAST:event_groupComboBoxActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ApplicationFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ApplicationFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ApplicationFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ApplicationFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                ApplicationFrame f = new ApplicationFrame();
                f.setVisible(true);
                f.load();
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JComboBox<String> groupComboBox;
    private javax.swing.JPanel groupPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JMenuItem openFileMenuItem;
    private javax.swing.JMenuItem openGroupMenuItem;
    private javax.swing.JMenu openMenu;
    private javax.swing.JMenuItem openTagMenuItem;
    private javax.swing.JMenu propertiesMenu;
    private javax.swing.JPanel resultPanel;
    private javax.swing.JMenuItem scanRepositoryMenuItem;
    private javax.swing.JTextField searchBar;
    private javax.swing.JPanel tagFilterPanel;
    // End of variables declaration//GEN-END:variables
}
