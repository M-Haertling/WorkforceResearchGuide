/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utd.team6.workforceresearchguide.gui.repscan;

import java.io.File;
import javax.swing.JFileChooser;
import utd.team6.workforceresearchguide.main.issues.MovedFileIssue;

/**
 *
 * @author Michael
 */
public class MovedFileIssuePanel extends javax.swing.JPanel {

    MovedFileIssue issue;
    JFileChooser chooser;
    
    /**
     * Creates new form DoubleFileIssuePanel
     * @param issue
     */
    public MovedFileIssuePanel(MovedFileIssue issue) {
        initComponents();
        this.issue = issue;
        this.oldFilePathLabel.setText(issue.getMissingFile().getPath());
        this.newFilePathLabel.setText(issue.getNewFile().getPath());
        notifyLabel.setVisible(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        infoButton = new javax.swing.JButton();
        oldFilePathLabel = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        newFilePathLabel = new javax.swing.JLabel();
        relocateButton = new javax.swing.JButton();
        relocateCheckBox = new javax.swing.JCheckBox();
        notifyLabel = new javax.swing.JLabel();

        infoButton.setText("Info");

        oldFilePathLabel.setText("old/file/path");

        jButton1.setText("Info");

        newFilePathLabel.setText("new/file/path");

        relocateButton.setText("Change Relocation Path");
        relocateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                relocateButtonActionPerformed(evt);
            }
        });

        relocateCheckBox.setText("Relocate");

        notifyLabel.setForeground(java.awt.Color.red);
        notifyLabel.setText("The suggested file will be added to the system.");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(notifyLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(newFilePathLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(oldFilePathLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(infoButton, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(relocateCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(relocateButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(infoButton)
                    .addComponent(oldFilePathLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(newFilePathLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(notifyLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(relocateButton)
                    .addComponent(relocateCheckBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void relocateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_relocateButtonActionPerformed
        if(chooser == null){
            chooser = new JFileChooser();
            chooser.setSelectedFile(new File(issue.getNewFile().getPath()));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        }
        int result = chooser.showDialog(this, "Set File");
        if(result == JFileChooser.APPROVE_OPTION && !newFilePathLabel.getText().equals(chooser.getSelectedFile().getAbsolutePath())){
            newFilePathLabel.setText(chooser.getSelectedFile().getAbsolutePath());
            notifyLabel.setVisible(true);
        }
    }//GEN-LAST:event_relocateButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton infoButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel newFilePathLabel;
    private javax.swing.JLabel notifyLabel;
    private javax.swing.JLabel oldFilePathLabel;
    private javax.swing.JButton relocateButton;
    private javax.swing.JCheckBox relocateCheckBox;
    // End of variables declaration//GEN-END:variables
}