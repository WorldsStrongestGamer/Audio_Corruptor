/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dukinuki;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Header;

/**
 *
 * @author jcocj
 */
public class DukiNukiGui extends javax.swing.JFrame {
    
    private final JFileChooser openFileChooser;
    private File inFile, outFile = null;
    private boolean isSegmented = false;
    private int soloEffectChoice = 0,soloPower = 1,numOfSegments = 1, index = 0;
    private final DefaultListModel<String> listModel;
    
    //all the arraylists to store information about each segment selected back to DukiNuki propper
    private final ArrayList<Double> startTimes = new ArrayList<>();
    private final ArrayList<Double> fadeIn = new ArrayList<>();
    private final ArrayList<Double> endTimes = new ArrayList<>();
    private final ArrayList<Double> fadeOut = new ArrayList<>();
    private final ArrayList<Boolean> isFaded = new ArrayList<>();
    private final ArrayList<Integer> effectChoice = new ArrayList<>();
    private final ArrayList<Integer> power = new ArrayList<>();
    
    private DukiNuki dukiNuki = null;
    /**
     * Creates new form DukiNukiGui
     */
    public DukiNukiGui() {
        initComponents();
        
        openFileChooser = new JFileChooser();
        openFileChooser.setFileFilter(new FileNameExtensionFilter("MP3 files", "mp3"));
        
        //sets the corrupt all file and random byte button to be selected by default
        corruptFileBtn.setSelected(true);
        effectRandom.setSelected(true);
        
        //since the corrupt entire file button is selected by default I run its code once on startup
        setIsSegmented(false);
        
        
        //prevents window from being resized
        this.setResizable(false);
        
        //sets default byteSpinner value to -1
        //addByteSpinner.setValue(-1);
        
        //calls class for class stuff
        dukiNuki = new DukiNuki();
        listModel = new DefaultListModel<>();
        listModel.addElement("Segment 1");
        segmentList.setModel(listModel);
        
        startTimes.add(0, 0.00);
        fadeIn.add(0, 0.00);
        endTimes.add(0, 0.00);
        fadeOut.add(0, 0.00);
        isFaded.add(0, false);
        effectChoice.add(0, 0);
        power.add(0,1);
    }
    
    //getters and setters doing their thing
    public File getInFile() {
        return inFile;
    }

    public void setInFile(File inFile) {
        this.inFile = inFile;
    }

    public File getOutFile() {
        return outFile;
    }
    
    public void setOutFile(File outFile) {
        this.outFile = outFile;
    }
    
    public void setNumOfSegments(int i){
        this.numOfSegments = i;
    }
    
    public void incNumOfSegments(int i){
        this.numOfSegments = this.numOfSegments + i;
    }
    
    public int getNumOfSegments(){
        return numOfSegments;
    }

    public boolean isIsSegmented() {
        return isSegmented;
    }

    public void setIsSegmented(boolean isSegmented) {
        this.isSegmented = isSegmented;
    }

    public int getSoloEffectChoice() {
        return soloEffectChoice;
    }

    public void setSoloEffectChoice(int soloEffectChoice) {
        this.soloEffectChoice = soloEffectChoice;
    }
    
    
    
    //Checks to see if file is correct filetype
    public static boolean isCorrectExtension(String ex, File file) {
        String extension = ex;
        String fileName = file.getName();
        int i = fileName.lastIndexOf('.');
        if (i >= 0) { extension = fileName.substring(i+1); }
        System.out.println(extension);
        if("mp3".equals(extension))
            return true;
        else
            return false;
    }
    //converters for arraylists to arrays
    public static int[] convertIntegers(List<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        Iterator<Integer> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++){
            ret[i] = iterator.next().intValue();
        }
        return ret;
    }
    public static double[] convertDoubles(List<Double> doubles){
       double[] ret = new double[doubles.size()];
        Iterator<Double> iterator = doubles.iterator();
        for (int i = 0; i < ret.length; i++){
            ret[i] = iterator.next().doubleValue();
        }
        return ret; 
    }
    public static boolean[] convertBooleans(List<Boolean> booleans){
       boolean[] ret = new boolean[booleans.size()];
        Iterator<Boolean> iterator = booleans.iterator();
        for (int i = 0; i < ret.length; i++){
            ret[i] = iterator.next().booleanValue();
        }
        return ret; 
    }    
    //Gets and returns duration in minutes
    public static float getDuration2(File audio) throws UnsupportedAudioFileException, IOException, BitstreamException{
        Header h= null;
        FileInputStream file = null;
        try {
            file = new FileInputStream(audio);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DukiNuki.class.getName()).log(Level.SEVERE, null, ex);
        }
        Bitstream bitstream = new  Bitstream(file);
        try {
            h = bitstream.readFrame();

        } catch (BitstreamException ex) {
            Logger.getLogger(DukiNuki.class.getName()).log(Level.SEVERE, null, ex);
        }
        int size = h.calculate_framesize();
            //System.out.println(size);
        float ms_per_frame = h.ms_per_frame();
           // System.out.println(ms_per_frame);
        int maxSize = h.max_number_of_frames(10000);
        float t = h.total_ms(size);
        long tn = 0;
        try {
            tn = file.getChannel().size();
        } catch (IOException ex) {
            Logger.getLogger(DukiNuki.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println("Chanel: " + file.getChannel().size());
        int min = h.min_number_of_frames(500);
        file.close();
        bitstream.close();
        return (h.total_ms((int) tn)/1000)/60;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnGroupSegmentChoice = new javax.swing.ButtonGroup();
        btnGroupEffects = new javax.swing.ButtonGroup();
        fileSelectPanel = new javax.swing.JPanel();
        inputFileTextLabel = new javax.swing.JLabel();
        outputFileTextLabel = new javax.swing.JLabel();
        fileBrowserOpenInput = new javax.swing.JButton();
        fileBrowserOpenOutput = new javax.swing.JButton();
        inputFileMessageLabel = new javax.swing.JLabel();
        outputFileMessageLabel = new javax.swing.JLabel();
        settingsPane = new javax.swing.JPanel();
        entireFilePowerSlider = new javax.swing.JSlider();
        totalAudioLabel = new javax.swing.JLabel();
        currentlyEditingLabel = new javax.swing.JLabel();
        powerLabel = new javax.swing.JLabel();
        effectLabel = new javax.swing.JLabel();
        effectRandom = new javax.swing.JRadioButton();
        effectFlip = new javax.swing.JRadioButton();
        effectAdd = new javax.swing.JRadioButton();
        addByteSpinner = new javax.swing.JSpinner();
        addByteWarningLabel = new javax.swing.JLabel();
        startTimeLabel = new javax.swing.JLabel();
        endTimeLabel = new javax.swing.JLabel();
        fadeCorruptionBtn = new javax.swing.JCheckBox();
        fadeInTimeLabel = new javax.swing.JLabel();
        fadeOutTimeLabel = new javax.swing.JLabel();
        runBtn = new javax.swing.JButton();
        runBtnWarning = new javax.swing.JLabel();
        updateBtn = new javax.swing.JButton();
        startTimeSpinner = new javax.swing.JSpinner();
        endTimeSpinner = new javax.swing.JSpinner();
        fadeInSpinner = new javax.swing.JSpinner();
        fadeOutSpinner = new javax.swing.JSpinner();
        titleLabel = new javax.swing.JLabel();
        theListThingPanel = new javax.swing.JPanel();
        segmentListScroller = new javax.swing.JScrollPane();
        segmentList = new javax.swing.JList<>();
        corruptFileBtn = new javax.swing.JRadioButton();
        corruptSegmentBtn = new javax.swing.JRadioButton();
        decreaseSpliceBtn = new javax.swing.JButton();
        increaseSpliceBtn = new javax.swing.JButton();
        maxValueLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        fileSelectPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        inputFileTextLabel.setText("Input File Location");

        outputFileTextLabel.setText("Output File Location");

        fileBrowserOpenInput.setText("Select File");
        fileBrowserOpenInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileBrowserOpenInputActionPerformed(evt);
            }
        });

        fileBrowserOpenOutput.setText("Select File");
        fileBrowserOpenOutput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileBrowserOpenOutputActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout fileSelectPanelLayout = new javax.swing.GroupLayout(fileSelectPanel);
        fileSelectPanel.setLayout(fileSelectPanelLayout);
        fileSelectPanelLayout.setHorizontalGroup(
            fileSelectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fileSelectPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(fileSelectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(inputFileTextLabel)
                    .addComponent(fileBrowserOpenInput)
                    .addComponent(fileBrowserOpenOutput)
                    .addComponent(outputFileTextLabel)
                    .addGroup(fileSelectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(inputFileMessageLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                        .addComponent(outputFileMessageLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        fileSelectPanelLayout.setVerticalGroup(
            fileSelectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fileSelectPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(inputFileTextLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fileBrowserOpenInput)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(inputFileMessageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(outputFileTextLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fileBrowserOpenOutput)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(outputFileMessageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        settingsPane.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        entireFilePowerSlider.setMajorTickSpacing(1);
        entireFilePowerSlider.setMaximum(10);
        entireFilePowerSlider.setMinimum(1);
        entireFilePowerSlider.setPaintLabels(true);
        entireFilePowerSlider.setPaintTicks(true);
        entireFilePowerSlider.setSnapToTicks(true);
        entireFilePowerSlider.setValue(1);
        entireFilePowerSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                entireFilePowerSliderStateChanged(evt);
            }
        });

        totalAudioLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        totalAudioLabel.setText("Total Audio Length: N/A");

        currentlyEditingLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        currentlyEditingLabel.setText("Currently Editing: Entire File");

        powerLabel.setText("Power");

        effectLabel.setText("Effect");

        btnGroupEffects.add(effectRandom);
        effectRandom.setText("Random Bytes");
        effectRandom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                effectRandomActionPerformed(evt);
            }
        });

        btnGroupEffects.add(effectFlip);
        effectFlip.setText("Flip Bytes");
        effectFlip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                effectFlipActionPerformed(evt);
            }
        });

        btnGroupEffects.add(effectAdd);
        effectAdd.setText("Add Byte");
        effectAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                effectAddActionPerformed(evt);
            }
        });

        addByteSpinner.setModel(new javax.swing.SpinnerNumberModel(0, -128, 127, 1));
        addByteSpinner.setEnabled(false);
        addByteSpinner.setValue(-1);
        addByteSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                addByteSpinnerStateChanged(evt);
            }
        });
        addByteSpinner.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                addByteSpinnerPropertyChange(evt);
            }
        });

        addByteWarningLabel.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        addByteWarningLabel.setText("Add Byte Cannot Be Set to 0 (Min Value -128, Max Value 127)");

        startTimeLabel.setText("Start Time");
        startTimeLabel.setEnabled(false);

        endTimeLabel.setText("End Time");
        endTimeLabel.setEnabled(false);

        fadeCorruptionBtn.setText("Fade Corruption");
        fadeCorruptionBtn.setEnabled(false);
        fadeCorruptionBtn.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fadeCorruptionBtnStateChanged(evt);
            }
        });
        fadeCorruptionBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fadeCorruptionBtnActionPerformed(evt);
            }
        });

        fadeInTimeLabel.setText("Fade In Time");
        fadeInTimeLabel.setEnabled(false);

        fadeOutTimeLabel.setText("Fade Out Time");
        fadeOutTimeLabel.setEnabled(false);

        runBtn.setText("Run");
        runBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runBtnActionPerformed(evt);
            }
        });

        updateBtn.setText("Update");
        updateBtn.setEnabled(false);
        updateBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateBtnActionPerformed(evt);
            }
        });

        startTimeSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, null, 0.1d));
        startTimeSpinner.setEnabled(false);
        startTimeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                startTimeSpinnerStateChanged(evt);
            }
        });

        endTimeSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, null, 0.1d));
        endTimeSpinner.setEnabled(false);
        endTimeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                endTimeSpinnerStateChanged(evt);
            }
        });

        fadeInSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, null, 0.1d));
        fadeInSpinner.setEnabled(false);

        fadeOutSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, null, 0.1d));
        fadeOutSpinner.setEnabled(false);

        javax.swing.GroupLayout settingsPaneLayout = new javax.swing.GroupLayout(settingsPane);
        settingsPane.setLayout(settingsPaneLayout);
        settingsPaneLayout.setHorizontalGroup(
            settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(settingsPaneLayout.createSequentialGroup()
                        .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addByteWarningLabel)
                            .addGroup(settingsPaneLayout.createSequentialGroup()
                                .addComponent(effectAdd)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(addByteSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(effectFlip))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(runBtnWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(settingsPaneLayout.createSequentialGroup()
                                .addComponent(updateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(46, 46, 46)
                                .addComponent(runBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(powerLabel)
                    .addGroup(settingsPaneLayout.createSequentialGroup()
                        .addComponent(currentlyEditingLabel)
                        .addGap(18, 18, 18)
                        .addComponent(totalAudioLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(settingsPaneLayout.createSequentialGroup()
                        .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(effectLabel)
                            .addComponent(entireFilePowerSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(effectRandom))
                        .addGap(18, 18, 18)
                        .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(startTimeLabel)
                            .addComponent(endTimeLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(endTimeSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                            .addComponent(startTimeSpinner))
                        .addGap(18, 18, 18)
                        .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(settingsPaneLayout.createSequentialGroup()
                                .addComponent(fadeInTimeLabel)
                                .addGap(18, 18, 18)
                                .addComponent(fadeInSpinner))
                            .addComponent(fadeCorruptionBtn)
                            .addGroup(settingsPaneLayout.createSequentialGroup()
                                .addComponent(fadeOutTimeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(fadeOutSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        settingsPaneLayout.setVerticalGroup(
            settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(currentlyEditingLabel)
                    .addComponent(totalAudioLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(settingsPaneLayout.createSequentialGroup()
                        .addComponent(powerLabel)
                        .addGap(9, 9, 9)
                        .addComponent(entireFilePowerSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(effectLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(effectRandom)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(effectFlip)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(effectAdd)
                            .addComponent(addByteSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(settingsPaneLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(fadeCorruptionBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(startTimeLabel)
                                .addComponent(startTimeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(fadeInTimeLabel)
                                .addComponent(fadeInSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(6, 6, 6)
                        .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(endTimeLabel)
                            .addComponent(fadeOutTimeLabel)
                            .addComponent(endTimeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fadeOutSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 44, Short.MAX_VALUE)
                        .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(runBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(updateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addByteWarningLabel)
                    .addComponent(runBtnWarning))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        titleLabel.setFont(new java.awt.Font("Comic Sans MS", 1, 24)); // NOI18N
        titleLabel.setForeground(new java.awt.Color(100, 149, 237));
        titleLabel.setText("Mp3 Blaster");

        theListThingPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        segmentList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        segmentList.setEnabled(false);
        segmentList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                segmentListValueChanged(evt);
            }
        });
        segmentListScroller.setViewportView(segmentList);

        btnGroupSegmentChoice.add(corruptFileBtn);
        corruptFileBtn.setText("Corrupt Entire File");
        corruptFileBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                corruptFileBtnActionPerformed(evt);
            }
        });

        btnGroupSegmentChoice.add(corruptSegmentBtn);
        corruptSegmentBtn.setText("Corrupt Segments of File");
        corruptSegmentBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                corruptSegmentBtnActionPerformed(evt);
            }
        });

        decreaseSpliceBtn.setText("-");
        decreaseSpliceBtn.setEnabled(false);
        decreaseSpliceBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                decreaseSpliceBtnActionPerformed(evt);
            }
        });

        increaseSpliceBtn.setText("+");
        increaseSpliceBtn.setEnabled(false);
        increaseSpliceBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                increaseSpliceBtnActionPerformed(evt);
            }
        });

        maxValueLabel.setForeground(java.awt.Color.gray);
        maxValueLabel.setText("(Max List Size of 5)");

        javax.swing.GroupLayout theListThingPanelLayout = new javax.swing.GroupLayout(theListThingPanel);
        theListThingPanel.setLayout(theListThingPanelLayout);
        theListThingPanelLayout.setHorizontalGroup(
            theListThingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(theListThingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(theListThingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(theListThingPanelLayout.createSequentialGroup()
                        .addGroup(theListThingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(corruptSegmentBtn)
                            .addComponent(corruptFileBtn))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(theListThingPanelLayout.createSequentialGroup()
                        .addComponent(segmentListScroller, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(theListThingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(theListThingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(decreaseSpliceBtn, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(increaseSpliceBtn, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(maxValueLabel))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        theListThingPanelLayout.setVerticalGroup(
            theListThingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, theListThingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(corruptFileBtn)
                .addGap(8, 8, 8)
                .addComponent(corruptSegmentBtn)
                .addGroup(theListThingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(theListThingPanelLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(increaseSpliceBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(decreaseSpliceBtn)
                        .addGap(8, 8, 8)
                        .addComponent(maxValueLabel)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(theListThingPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(segmentListScroller, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(settingsPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fileSelectPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(titleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(theListThingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(titleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fileSelectPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(theListThingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(7, 7, 7)
                .addComponent(settingsPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
//opens the file browser tests if file is the correct extension and exists then acts accordingly
    private void fileBrowserOpenInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileBrowserOpenInputActionPerformed
        int returnValue = openFileChooser.showOpenDialog(this);
        //sets a decimalformat (df) to round the mp3 duration
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        //boolean that prevents selected file message if try catch catches an error
        boolean isKosher = true;
        //if the file exists and is the correct file type then it will set inFile to the selected file otherwise it will post an error
        if(returnValue == JFileChooser.APPROVE_OPTION){
            if(isCorrectExtension("mp3", openFileChooser.getSelectedFile()) == true){
                //System.out.println(getInFile().toString());
                setInFile(openFileChooser.getSelectedFile());
                //tires to read file and determin length will catch errors
                try {
                    totalAudioLabel.setText("Total Audio Length: " + df.format(getDuration2(getInFile())));
                } 
                catch (UnsupportedAudioFileException ex) {
                    Logger.getLogger(DukiNukiGui.class.getName()).log(Level.SEVERE, null, ex);
                    inputFileMessageLabel.setText("ERROR! Unsupported File Type!");
                    isKosher = false;
                } 
                catch (IOException ex) {
                    Logger.getLogger(DukiNukiGui.class.getName()).log(Level.SEVERE, null, ex);
                    inputFileMessageLabel.setText("ERROR! IOExecption!");
                    isKosher = false;
                } 
                catch (BitstreamException ex) {
                    Logger.getLogger(DukiNukiGui.class.getName()).log(Level.SEVERE, null, ex);
                    inputFileMessageLabel.setText("ERROR! BitstreamExceptionm!");
                    isKosher = false;
                }
                if(isKosher){
                    inputFileMessageLabel.setText("Selected file: " + getInFile().getName());
                }
            }
            else{
                inputFileMessageLabel.setText("Incorrect file type! Must be Mp3!");
            }
        }
        else{
            inputFileMessageLabel.setText("No file chosen!");
        }
    }//GEN-LAST:event_fileBrowserOpenInputActionPerformed

    private void fileBrowserOpenOutputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileBrowserOpenOutputActionPerformed
        int returnValue = openFileChooser.showOpenDialog(this);
        //if the file exists and is the correct file type then it will set inFile to the selected file otherwise it will post an error
        if(returnValue == JFileChooser.APPROVE_OPTION){
            if(isCorrectExtension("mp3", openFileChooser.getSelectedFile()) == true){
                setOutFile(openFileChooser.getSelectedFile());
                System.out.println(getOutFile().toString());
                outputFileMessageLabel.setText("Selected file: " + getOutFile().getName());
            }
            else{
                outputFileMessageLabel.setText("Incorrect file type! Must be Mp3!");
            }
        }
        else{
            outputFileMessageLabel.setText("No file chosen!");
        }
    }//GEN-LAST:event_fileBrowserOpenOutputActionPerformed

    private void increaseSpliceBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_increaseSpliceBtnActionPerformed
        //if the current number of segments is less then 5 (to prevent overflow) then increment the number of segments by 1
        if(numOfSegments < 5){
            incNumOfSegments(1);
            listModel.addElement("Segment " + numOfSegments);
            segmentList.setModel(listModel);
            
            //adds new segment and fills arrays at that segment with default values
            startTimes.add((numOfSegments - 1), 0.00);
            fadeIn.add((numOfSegments - 1), 0.00);
            endTimes.add((numOfSegments - 1), 0.00);
            fadeOut.add((numOfSegments - 1), 0.00);
            isFaded.add((numOfSegments - 1), false);
            effectChoice.add((numOfSegments - 1), 0);
            power.add((numOfSegments - 1),1);
        }
    }//GEN-LAST:event_increaseSpliceBtnActionPerformed

    private void corruptFileBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_corruptFileBtnActionPerformed
        //disables all buttons and text that pertains to segments/splices
        increaseSpliceBtn.setEnabled(false);
        decreaseSpliceBtn.setEnabled(false);
        segmentList.setEnabled(false);
        maxValueLabel.setForeground(Color.gray);
        startTimeLabel.setEnabled(false);
        startTimeSpinner.setEnabled(false);
        endTimeSpinner.setEnabled(false);
        endTimeLabel.setEnabled(false);
        fadeCorruptionBtn.setEnabled(false);
        
        //sets currently editing text to let user know they are messing with the whole file
        currentlyEditingLabel.setText("Currently Editing: Entire File");
        
        //sets DukiNuki to run the solo version of the corruptor on activation
        isSegmented = false;
        dukiNuki.setIsSolo(true);
        
        entireFilePowerSlider.setValue(soloPower);
            
            //if else statements to ensure that correct effect choice button is selected
            if(soloEffectChoice == 0){
                effectRandom.setSelected(true);
                addByteSpinner.setValue(0);
                addByteSpinner.setEnabled(false);
            }
            else if(soloEffectChoice == 128){
                effectFlip.setSelected(true);
                addByteSpinner.setValue(0);
                addByteSpinner.setEnabled(false);
            }
            else{
                effectAdd.setSelected(true);
                addByteSpinner.setEnabled(true);
                addByteSpinner.setValue(soloEffectChoice);
            }
            fadeInTimeLabel.setEnabled(false);
            fadeOutTimeLabel.setEnabled(false);
            fadeInSpinner.setEnabled(false);
            fadeOutSpinner.setEnabled(false);
            
            updateBtn.setEnabled(false);
    }//GEN-LAST:event_corruptFileBtnActionPerformed

    private void corruptSegmentBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_corruptSegmentBtnActionPerformed
        //enables all buttons and text that pertains to segments/splices
        increaseSpliceBtn.setEnabled(true);
        decreaseSpliceBtn.setEnabled(true);
        segmentList.setEnabled(true);
        maxValueLabel.setForeground(Color.black);
        startTimeLabel.setEnabled(true);
        startTimeSpinner.setEnabled(true);
        endTimeSpinner.setEnabled(true);
        endTimeLabel.setEnabled(true);
        fadeCorruptionBtn.setEnabled(true);
        
        //automatically selects last index and changes currently editing label to reflect that
        segmentList.setSelectedIndex(index);
        if(index != -1){
            //changes currently editing label to reflect current index
            currentlyEditingLabel.setText("Currently Editing: Segment " + (index + 1));
            
            //sets all fields/settings back to what the selection had last
            startTimeSpinner.setValue(startTimes.get(index));
            endTimeSpinner.setValue(endTimes.get(index));
           
            entireFilePowerSlider.setValue(power.get(index));
            System.out.println("power" + power.get(index));
            //if else statements to ensure that correct effect choice button is selected
            if(effectChoice.get(index) == 0){
                effectRandom.setSelected(true);
                addByteSpinner.setValue(0);
                addByteSpinner.setEnabled(false);
            }
            else if(effectChoice.get(index) == 128){
                effectFlip.setSelected(true);
                addByteSpinner.setValue(0);
                addByteSpinner.setEnabled(false);
            }
            else{
                effectAdd.setSelected(true);
                addByteSpinner.setEnabled(true);
                addByteSpinner.setValue(effectChoice.get(index));
            }
            if(isFaded.get(index) == true){
                fadeCorruptionBtn.setSelected(true);
                fadeInTimeLabel.setEnabled(true);
                fadeOutTimeLabel.setEnabled(true);
                fadeInSpinner.setEnabled(true);
                fadeOutSpinner.setEnabled(true);
            }
            else{
                fadeCorruptionBtn.setSelected(false);
                fadeInTimeLabel.setEnabled(false);
                fadeOutTimeLabel.setEnabled(false);
                fadeInSpinner.setEnabled(false);
                fadeOutSpinner.setEnabled(false);                
            }
            
        }
        //only happens if none of the segments are selected
        else{
            currentlyEditingLabel.setText("Currently Editing: Nothing");
        }
        updateBtn.setEnabled(false);
        
        //sets DukiNuki to run the non solo version (has segments) upon activation
        isSegmented = true;
        dukiNuki.setIsSolo(false);
    }//GEN-LAST:event_corruptSegmentBtnActionPerformed

    private void decreaseSpliceBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_decreaseSpliceBtnActionPerformed
        //if the current number of segments greater then 1 (to prevent overflow) then decrement the number of segments by 1
        if(numOfSegments > 1){
            startTimes.remove((numOfSegments - 1));
            fadeIn.remove((numOfSegments - 1));
            endTimes.remove((numOfSegments - 1));
            fadeOut.remove((numOfSegments - 1));
            isFaded.remove((numOfSegments - 1));
            effectChoice.remove((numOfSegments - 1));
            power.remove((numOfSegments - 1));
            incNumOfSegments(-1);
            listModel.remove(numOfSegments);
        }
    }//GEN-LAST:event_decreaseSpliceBtnActionPerformed

    private void runBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runBtnActionPerformed
        if(isSegmented == false){
            if(inFile == null){
                runBtnWarning.setText("No input file detected");
            }
            else if(outFile == null){
                runBtnWarning.setText("No output file detected");
            }
            else if(effectAdd.isSelected() && 0 == (int) addByteSpinner.getValue()){
                runBtnWarning.setText("Add Bytes Cannot Be Set to Zero");
            }
            else{
                try {
                    dukiNuki.setIsSolo(true);
                    dukiNuki.setSoloEffectChoice(soloEffectChoice);
                    dukiNuki.setSoloPower(soloPower);
                    dukiNuki.doEverythingElse(inFile, outFile);
                } catch (BitstreamException ex) {
                    Logger.getLogger(DukiNukiGui.class.getName()).log(Level.SEVERE, null, ex);
                    runBtnWarning.setText("Woah! Bitstream Error");
                } catch (IOException ex) {
                    Logger.getLogger(DukiNukiGui.class.getName()).log(Level.SEVERE, null, ex);
                    runBtnWarning.setText("Damn Bro. We got an IOExecption");
                } catch (UnsupportedAudioFileException ex) {
                    Logger.getLogger(DukiNukiGui.class.getName()).log(Level.SEVERE, null, ex);
                    runBtnWarning.setText("How did this even happen? Unsupported Audio?");
                }
            }
        }
        else{
            if(inFile == null){
                runBtnWarning.setText("No input file detected");
            }
            else if(outFile == null){
                runBtnWarning.setText("No output file detected");
            }
            else if(effectAdd.isSelected() && 0 == (int) addByteSpinner.getValue()){
                runBtnWarning.setText("Add Bytes Cannot Be Set to Zero");
            }
            else{
                try {
                    //sets all required things for dukiNuki then blasts ass
                    dukiNuki.setEffectChoice(convertIntegers(effectChoice));
                    dukiNuki.setPower(convertIntegers(power));
                    dukiNuki.setStartTimes(convertDoubles(startTimes));
                    dukiNuki.setEndTimes(convertDoubles(endTimes));
                    dukiNuki.setIsFaded(convertBooleans(isFaded));
                    dukiNuki.setFadeIn(convertDoubles(fadeIn));
                    dukiNuki.setFadeOut(convertDoubles(fadeOut));
                    dukiNuki.setIsSolo(false);
                    dukiNuki.doEverythingElse(inFile, outFile);
                } catch (BitstreamException ex) {
                    Logger.getLogger(DukiNukiGui.class.getName()).log(Level.SEVERE, null, ex);
                    runBtnWarning.setText("Woah! Bitstream Error");
                } catch (IOException ex) {
                    Logger.getLogger(DukiNukiGui.class.getName()).log(Level.SEVERE, null, ex);
                    runBtnWarning.setText("Damn Bro. We got an IOExecption");
                } catch (UnsupportedAudioFileException ex) {
                    Logger.getLogger(DukiNukiGui.class.getName()).log(Level.SEVERE, null, ex);
                    runBtnWarning.setText("How did this even happen? Unsupported Audio?");
                }
        }
    }//GEN-LAST:event_runBtnActionPerformed
    }
    private void addByteSpinnerPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_addByteSpinnerPropertyChange

    }//GEN-LAST:event_addByteSpinnerPropertyChange

    private void effectAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_effectAddActionPerformed
        addByteSpinner.setEnabled(true);
        
    }//GEN-LAST:event_effectAddActionPerformed

    private void effectRandomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_effectRandomActionPerformed
        addByteSpinner.setEnabled(false);
        
        //checks to see if user has selected entire file for editing and either updates the solo effect choice or the array of effect choices accordingly
        if(isSegmented == false){
            dukiNuki.setSoloEffectChoice(0);
        }
        else if(index != -1){
            effectChoice.set(index, 0);
        }
    }//GEN-LAST:event_effectRandomActionPerformed

    private void effectFlipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_effectFlipActionPerformed
        addByteSpinner.setEnabled(false);
        //checks to see if user has selected entire file for editing and either updates the solo effect choice or the array of effect choices accordingly
        if(isSegmented == false){
            dukiNuki.setSoloEffectChoice(128);
        }
        else if(index != -1){
            effectChoice.set(index, 128);
        }
    }//GEN-LAST:event_effectFlipActionPerformed

    private void addByteSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_addByteSpinnerStateChanged
        //lets the user know to update by enableing the update btn
        if(index != -1){
            updateBtn.setEnabled(true);
        }
    }//GEN-LAST:event_addByteSpinnerStateChanged

    private void entireFilePowerSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_entireFilePowerSliderStateChanged
        if(index != -1){
            updateBtn.setEnabled(true);
        }
    }//GEN-LAST:event_entireFilePowerSliderStateChanged

    private void segmentListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_segmentListValueChanged
        index = segmentList.getSelectedIndex();
        System.out.println("Index! " + index);
        if(index != -1){
            //changes currently editing label to reflect current index
            currentlyEditingLabel.setText("Currently Editing: Segment " + (index + 1));
            
            //sets all fields/settings back to what the selection had last
            startTimeSpinner.setValue((double)startTimes.get(index));
            endTimeSpinner.setValue((double)endTimes.get(index));
            entireFilePowerSlider.setValue(power.get(index));
            fadeInSpinner.setValue(fadeIn.get(index));
            fadeOutSpinner.setValue(fadeOut.get(index));
            
            System.out.println("power" + power.get(index));
            //if else statements to ensure that correct effect choice button is selected
            if(effectChoice.get(index) == 0){
                effectRandom.setSelected(true);
                addByteSpinner.setValue(0);
                addByteSpinner.setEnabled(false);
            }
            else if(effectChoice.get(index) == 128){
                effectFlip.setSelected(true);
                addByteSpinner.setValue(0);
                addByteSpinner.setEnabled(false);
            }
            else{
                effectAdd.setSelected(true);
                addByteSpinner.setEnabled(true);
                addByteSpinner.setValue(effectChoice.get(index));
            }
            if(isFaded.get(index)){
                fadeInTimeLabel.setEnabled(true);
                fadeOutTimeLabel.setEnabled(true);
                fadeInSpinner.setEnabled(true);
                fadeOutSpinner.setEnabled(true);
                fadeCorruptionBtn.setSelected(true);
            }
            else{
                fadeInTimeLabel.setEnabled(false);
                fadeOutTimeLabel.setEnabled(false);
                fadeInSpinner.setEnabled(false);
                fadeOutSpinner.setEnabled(false);
                fadeCorruptionBtn.setSelected(false);
            }
        }
        //only happens if none of the segments are selected
        else{
            currentlyEditingLabel.setText("Currently Editing: Nothing");
        }
        updateBtn.setEnabled(false);
    }//GEN-LAST:event_segmentListValueChanged

    private void fadeCorruptionBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fadeCorruptionBtnActionPerformed
        //enables fade in and out fields to be edited
        
    }//GEN-LAST:event_fadeCorruptionBtnActionPerformed

    private void updateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateBtnActionPerformed
        //updates all edited values
        System.out.println("changed");
        if(isSegmented == false){
            soloPower = (int) entireFilePowerSlider.getValue();
        }
        else if(index != -1){
            power.set(index, entireFilePowerSlider.getValue());
        }
        if(isSegmented == false){
            soloEffectChoice = (int) addByteSpinner.getValue();
        }
        else if(index != -1){
            effectChoice.set(index, (int) addByteSpinner.getValue());
        }
        if(isSegmented == true){
            startTimes.set(index,(double)startTimeSpinner.getValue());
            endTimes.set(index, (double)endTimeSpinner.getValue());
            if(isFaded.get(index) == true){
                fadeIn.set(index, (double)fadeInSpinner.getValue());
                fadeOut.set(index, (double)fadeInSpinner.getValue());
                System.out.println("Fade In: " + fadeIn.get(index));
                System.out.println("Fade Out: " + fadeOut.get(index));
            }
        }
        if(isSegmented == true){
        //prints out currently updated values to console    
            System.out.println("Power: "+power.get(index));
            System.out.println("EffectChoice: " + effectChoice.get(index));
            System.out.println("StartTime: " + startTimes.get(index));
            System.out.println("EndTimes: " + endTimes.get(index));
            System.out.println("FadeIn: " + fadeIn.get(index));
            System.out.println("FadeOut: " + fadeOut.get(index));
            System.out.println("IsFaded: " + isFaded.get(index));
        }
        else{
            System.out.println("Power: "+soloPower);
            System.out.println("EffectChoice: " + soloEffectChoice);
        }
        updateBtn.setEnabled(false);
    }//GEN-LAST:event_updateBtnActionPerformed

    private void startTimeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_startTimeSpinnerStateChanged
        if(index != -1){
            updateBtn.setEnabled(true);
        }
    }//GEN-LAST:event_startTimeSpinnerStateChanged

    private void endTimeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_endTimeSpinnerStateChanged
        if(index != -1){
            updateBtn.setEnabled(true);
        }
    }//GEN-LAST:event_endTimeSpinnerStateChanged

    private void fadeCorruptionBtnStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fadeCorruptionBtnStateChanged
        //enables/disables sections of the gui pertaining to fade in and out
        if(fadeCorruptionBtn.isSelected() == true){
            fadeInTimeLabel.setEnabled(true);
            fadeOutTimeLabel.setEnabled(true);
            fadeInSpinner.setEnabled(true);
            fadeOutSpinner.setEnabled(true);
            isFaded.set(index,true);
        }
        else{
            fadeInTimeLabel.setEnabled(false);
            fadeOutTimeLabel.setEnabled(false);
            fadeInSpinner.setEnabled(false);
            fadeOutSpinner.setEnabled(false);
            isFaded.set(index,false);
        }
        updateBtn.setEnabled(true);
    }//GEN-LAST:event_fadeCorruptionBtnStateChanged
    

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
        * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
        */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DukiNukiGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DukiNukiGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DukiNukiGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DukiNukiGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DukiNukiGui().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner addByteSpinner;
    private javax.swing.JLabel addByteWarningLabel;
    private javax.swing.ButtonGroup btnGroupEffects;
    private javax.swing.ButtonGroup btnGroupSegmentChoice;
    private javax.swing.JRadioButton corruptFileBtn;
    private javax.swing.JRadioButton corruptSegmentBtn;
    private javax.swing.JLabel currentlyEditingLabel;
    private javax.swing.JButton decreaseSpliceBtn;
    private javax.swing.JRadioButton effectAdd;
    private javax.swing.JRadioButton effectFlip;
    private javax.swing.JLabel effectLabel;
    private javax.swing.JRadioButton effectRandom;
    private javax.swing.JLabel endTimeLabel;
    private javax.swing.JSpinner endTimeSpinner;
    private javax.swing.JSlider entireFilePowerSlider;
    private javax.swing.JCheckBox fadeCorruptionBtn;
    private javax.swing.JSpinner fadeInSpinner;
    private javax.swing.JLabel fadeInTimeLabel;
    private javax.swing.JSpinner fadeOutSpinner;
    private javax.swing.JLabel fadeOutTimeLabel;
    private javax.swing.JButton fileBrowserOpenInput;
    private javax.swing.JButton fileBrowserOpenOutput;
    private javax.swing.JPanel fileSelectPanel;
    private javax.swing.JButton increaseSpliceBtn;
    private javax.swing.JLabel inputFileMessageLabel;
    private javax.swing.JLabel inputFileTextLabel;
    private javax.swing.JLabel maxValueLabel;
    private javax.swing.JLabel outputFileMessageLabel;
    private javax.swing.JLabel outputFileTextLabel;
    private javax.swing.JLabel powerLabel;
    private javax.swing.JButton runBtn;
    private javax.swing.JLabel runBtnWarning;
    private javax.swing.JList<String> segmentList;
    private javax.swing.JScrollPane segmentListScroller;
    private javax.swing.JPanel settingsPane;
    private javax.swing.JLabel startTimeLabel;
    private javax.swing.JSpinner startTimeSpinner;
    private javax.swing.JPanel theListThingPanel;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JLabel totalAudioLabel;
    private javax.swing.JButton updateBtn;
    // End of variables declaration//GEN-END:variables
}
