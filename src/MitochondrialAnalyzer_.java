
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.RowFilter;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import amira.AmiraMeshDecoder;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.ChannelSplitter;
import ij.plugin.ImagesToStack;
import ij.plugin.PlugIn;
import ij.plugin.RoiEnlarger;
import ij.plugin.Scaler;
import ij.plugin.ZProjector;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.filter.UnsharpMask;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.StackStatistics;
import sc.fiji.analyzeSkeleton.AnalyzeSkeleton_;
import sc.fiji.analyzeSkeleton.Edge;
import sc.fiji.analyzeSkeleton.Graph;
import sc.fiji.analyzeSkeleton.Point;
import sc.fiji.analyzeSkeleton.SkeletonResult;
import sc.fiji.analyzeSkeleton.Vertex;
import util.opencsv.CSVReader;
import mpicbg.ij.clahe.*;

public class MitochondrialAnalyzer_ implements PlugIn, Measurements {

	public String MITOCHONDRIALANALYZER_IMAGES_PATH = "images_path";
	public String MITOCHONDRIALANALYZER_SAVE_PATH = "save_path";
	public String MITOCHONDRIALANALYZER_PS_PATH = "ps_path";
	public Preferences prefImages = Preferences.userRoot();
	public Preferences prefSave = Preferences.userRoot();
	public Preferences prefPS = Preferences.userRoot();
	static File[] listOfFiles;
	private JRadioButton csvFileB, excelFileB;
	static int l;
	static JTextArea taskOutput;
	public List<String> tablehead1ing1 = new ArrayList<String>();
	private int sumNumberOfBranches, sumNumberOfJunctions, sumNumberOfEndPoints, sumNumberOfJunctionVoxels,
			sumNumberOfSlabs, sumNumberOfTriplePoints, sumNumberOfQuadruplePoints, sumAreaPix, sumAreaNM;
	private double sumAverageBranchLength, sumMaximumBranchLength;
	List<String> cellID, cellX, cellZ;
	List<Double> idsT, areasPixT, areasNMT, branchesT, junctT, endT, junctVoxT, slabsT, ablT, tripleT, quadT, mblT,
			sumBranches, sumJunctions, sumEnd, sumJuncVox, sumSlabVox, averageAv, sumTriple, sumQuadrupl, sumMax,
			sumBranchesT, sumJunctionsT, sumEndT, sumJuncVoxT, sumSlabVoxT, averageAvT, sumTripleT, sumQuadruplT,
			sumMaxT;
	ResultsTable rt1, rt2;
	String[] head1 = { "ID", "# Branches", "# Junctions", "# End-point voxels", "# Junction voxels", "# Slab voxels",
			"Average Branch Length", "# Triple points", "# Quadruple points", "Maximum Branch Length" };
	String[] head2 = { "ID", "Vol(pix^3)", "Vol(nm^3)" };

	@Override
	public void run(String arg0) {
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			// If Nimbus is not available, you can set the GUI to another look and feel.
		}
		BatchModeGUI();

	}

	public void BatchModeGUI() {

		JFrame frameInitial = new JFrame("Mitochondrial-Analyzer:  Batch-Mode");
		JRadioButton batchAnalysisButton = new JRadioButton(" Batch-Mode for Mitochondrial Analysis");
		batchAnalysisButton.setSelected(true);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(batchAnalysisButton);
		JPanel firstPanel = new JPanel();
		firstPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		firstPanel.add(batchAnalysisButton);
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
		buttonsPanel.add(Box.createVerticalStrut(8));
		buttonsPanel.add(Box.createHorizontalStrut(8));
		buttonsPanel.add(firstPanel);
		TextField textImages = (TextField) new TextField(20);
		TextField textSave = (TextField) new TextField(20);
		TextField textPS = (TextField) new TextField(20);
		textImages.setText(prefImages.get(MITOCHONDRIALANALYZER_IMAGES_PATH, ""));
		textSave.setText(prefSave.get(MITOCHONDRIALANALYZER_SAVE_PATH, ""));
		textPS.setText(prefPS.get(MITOCHONDRIALANALYZER_PS_PATH, ""));
		ImageIcon iconImages = createImageIcon("images/browse.png");
		JButton buttonImages = new JButton("");
		JButton buttonSave = new JButton("");
		JButton buttonPS = new JButton("");
		Icon iconImagesCell = new ImageIcon(iconImages.getImage().getScaledInstance(20, 22, Image.SCALE_SMOOTH));
		buttonImages.setIcon(iconImagesCell);
		buttonSave.setIcon(iconImagesCell);
		buttonPS.setIcon(iconImagesCell);
		DirectoryListener_ listenerImages = new DirectoryListener_("Browse for images to analyze", textImages,
				JFileChooser.FILES_AND_DIRECTORIES);
		DirectoryListener_ listenerSave = new DirectoryListener_("Browse for directory to save results", textSave,
				JFileChooser.FILES_AND_DIRECTORIES);
		DirectoryListener_ listenerPS = new DirectoryListener_("Browse for pixel size data ", textPS,
				JFileChooser.FILES_AND_DIRECTORIES);
		buttonImages.addActionListener(listenerImages);
		buttonSave.addActionListener(listenerSave);
		buttonPS.addActionListener(listenerPS);
		JPanel panelImages = new JPanel();
		JPanel panelSave = new JPanel();
		JPanel panelExport = new JPanel();
		JPanel panelPS = new JPanel();
		panelImages.setLayout(new FlowLayout(FlowLayout.LEFT));
		panelSave.setLayout(new FlowLayout(FlowLayout.LEFT));
		panelExport.setLayout(new FlowLayout(FlowLayout.LEFT));
		panelPS.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel directImages = new JLabel("     ⊳   Directoy where Images to be analyzed are :   ");
		JLabel directSave = new JLabel("     ⊳   Directoy to Export Results :   ");
		JLabel directPS = new JLabel("     ⊳   Directoy where pixel-size data :   ");
		directImages.setFont(new Font("Helvetica", Font.BOLD, 12));
		directSave.setFont(new Font("Helvetica", Font.BOLD, 12));
		directPS.setFont(new Font("Helvetica", Font.BOLD, 12));
		panelImages.add(directImages);
		panelImages.add(textImages);
		panelImages.add(buttonImages);
		panelSave.add(directSave);
		panelSave.add(textSave);
		panelSave.add(buttonSave);
		panelPS.add(directPS);
		panelPS.add(textPS);
		panelPS.add(buttonPS);
		JButton okButton = new JButton("");
		ImageIcon iconOk = createImageIcon("images/ok.png");
		Icon okCell = new ImageIcon(iconOk.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
		okButton.setIcon(okCell);
		okButton.setToolTipText("Click this button to get Batch-Mode Analysis.");
		JButton cancelButton = new JButton("");
		ImageIcon iconCancel = createImageIcon("images/cancel.png");
		Icon cancelCell = new ImageIcon(iconCancel.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
		cancelButton.setIcon(cancelCell);
		cancelButton.setToolTipText("Click this button to cancel Batch-Mode Analysis.");
		JPanel panelOptions = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel panelBox = new JPanel();
		panelBox.setLayout(new BoxLayout(panelBox, BoxLayout.Y_AXIS));
		csvFileB = new JRadioButton(".CSV file", true);
		excelFileB = new JRadioButton("EXCEL file");
		ButtonGroup bgroup = new ButtonGroup();
		bgroup.add(csvFileB);
		bgroup.add(excelFileB);
		panelBox.add(panelExport);
		panelBox.add(excelFileB);
		panelBox.add(csvFileB);
		panelOptions.add(panelBox);
		JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
		Dimension dime = separator.getPreferredSize();
		dime.height = panelBox.getPreferredSize().height * 3;
		separator.setPreferredSize(dime);
		panelOptions.add(separator);
		JPanel mainPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		mainPanel1.add(panelOptions);
		JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		panelButtons.add(okButton);
		panelButtons.add(cancelButton);
		buttonsPanel.add(Box.createVerticalStrut(5));
		buttonsPanel.add(Box.createVerticalStrut(5));
		buttonsPanel.add(panelImages);
		buttonsPanel.add(Box.createVerticalStrut(5));
		buttonsPanel.add(panelSave);
		buttonsPanel.add(Box.createVerticalStrut(5));
		buttonsPanel.add(panelPS);
		buttonsPanel.add(Box.createVerticalStrut(5));
		buttonsPanel.add(panelButtons);

		frameInitial.setSize(725, 450);
		frameInitial.add(buttonsPanel);
		frameInitial.setLocationRelativeTo(null);
		frameInitial.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frameInitial.setVisible(true);

		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				frameInitial.dispatchEvent(new WindowEvent(frameInitial, WindowEvent.WINDOW_CLOSING));
				Thread mainProcess = new Thread(new Runnable() {

					public void run() {
						// ProgressBarDemo frame = new ProgressBarDemo();
						// frame.createAndShowGUI();
						File imageFolder = new File(textImages.getText());
						listOfFiles = imageFolder.listFiles();
						final String[] imageTitles = new String[listOfFiles.length];
						final int MAX = listOfFiles.length;
						final JFrame frame = new JFrame("Analyzing...");

						// creates progress bar
						final JProgressBar pb = new JProgressBar();
						pb.setMinimum(0);
						pb.setMaximum(MAX);
						pb.setStringPainted(true);
						taskOutput = new JTextArea(5, 20);
						taskOutput.setMargin(new Insets(5, 5, 5, 5));
						taskOutput.setEditable(false);
						JPanel panel = new JPanel();
						panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
						panel.add(pb);
						panel.add(Box.createVerticalStrut(5));
						panel.add(new JScrollPane(taskOutput), BorderLayout.CENTER);
						panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

						frame.getContentPane().add(panel);
						frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
						frame.setSize(400, 220);
						frame.setVisible(true);

						prefImages.put(MITOCHONDRIALANALYZER_IMAGES_PATH, textImages.getText());
						prefSave.put(MITOCHONDRIALANALYZER_SAVE_PATH, textSave.getText());
						prefPS.put(MITOCHONDRIALANALYZER_PS_PATH, textPS.getText());

						for (int i = 0; i < listOfFiles.length; i++) {
							File directImage = new File(textSave.getText() + File.separator
									+ listOfFiles[i].getName().replaceAll(".am", ""));

							if (!directImage.exists()) {

								boolean result = false;

								try {
									directImage.mkdir();
									result = true;
								} catch (SecurityException se) {
									// handle it
								}

							}
							if (rt1 != null)
								rt1.reset();
							rt1 = new ResultsTable();
							if (rt2 != null)
								rt2.reset();
							rt2 = new ResultsTable();

							idsT = new ArrayList<Double>();
							areasPixT = new ArrayList<Double>();
							areasNMT = new ArrayList<Double>();
							branchesT = new ArrayList<Double>();
							junctT = new ArrayList<Double>();
							endT = new ArrayList<Double>();
							junctVoxT = new ArrayList<Double>();
							slabsT = new ArrayList<Double>();
							ablT = new ArrayList<Double>();
							tripleT = new ArrayList<Double>();
							quadT = new ArrayList<Double>();
							mblT = new ArrayList<Double>();
							// update progressbar
							if (listOfFiles[i].isFile())
								imageTitles[i] = listOfFiles[i].getName();

							// if (imageTitles[i].contains("._") == Boolean.TRUE)
							// imageTitles[i].replaceFirst("._", "");

							// ImagePlus imp = new ImagePlus(textImages.getText() + File.separator +
							// imageTitles[i]);

							final int currentValue = i + 1;
							try {
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										pb.setValue(currentValue);
										taskOutput.append(
												String.format("Processing Image - %s%%%% -Completed : %f of task.\n",
														imageTitles[currentValue - 1],
														(double) (currentValue * 100.0) / listOfFiles.length));

									}
								});
								java.lang.Thread.sleep(100);
							} catch (InterruptedException e) {
								JOptionPane.showMessageDialog(frame, e.getMessage());
							}
							ImagePlus imp = null;
							if (listOfFiles[i].getName().contains(".am") == Boolean.TRUE) {
								AmiraMeshDecoder decoder = new AmiraMeshDecoder();
								decoder.open(textImages.getText() + File.separator + listOfFiles[i].getName());
								imp = new ImagePlus(listOfFiles[i].getName().replaceAll(".am", ""), decoder.getStack());
							} else {
								imp = new ImagePlus(textImages.getText() + File.separator + listOfFiles[i].getName());
							}
							AnalyzeSkeleton_ skel = new AnalyzeSkeleton_();
							Scaler scaler = new Scaler();

							// IJ.saveAsTiff(imp,
							// "/home/anaacayuela/Ana_pruebas_imageJ/javi_conesa/labelizada");
							ImagePlus impCopy = imp.duplicate();
							// ImagePlus impOrig = imp.duplicate();
							StackStatistics ss = new StackStatistics(imp);
							long values[] = ss.getHistogram();
							List<Double> areas = new ArrayList<Double>();
							List<String> ids = new ArrayList<String>();
							List<String> indexThreshold = new ArrayList<String>();
							for (int j = 2; j <= values.length - 1; j++) {
								if (values[j] != 0) {
									areas.add((double) values[j]);
									ids.add(String.valueOf(j));
									indexThreshold.add(String.valueOf(j));

								}
							}

							Object[] columnnames;
							CSVReader CSVFileReader = null;
							try {
								CSVFileReader = new CSVReader(new FileReader(textPS.getText()));
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							List myEntries = null;
							try {
								myEntries = CSVFileReader.readAll();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							columnnames = (String[]) myEntries.get(0);
							DefaultTableModel tableModel = new DefaultTableModel(columnnames, myEntries.size() - 1);
							int rowcount = tableModel.getRowCount();
							for (int x = 0; x < rowcount + 1; x++) {
								int columnnumber = 0;

								if (x > 0) {
									for (String thiscellvalue : (String[]) myEntries.get(x)) {
										tableModel.setValueAt(thiscellvalue, x - 1, columnnumber);
										columnnumber++;
									}
								}
							}

							JTable myTable = new JTable(tableModel);

							cellID = new ArrayList<String>();
							cellX = new ArrayList<String>();
							cellZ = new ArrayList<String>();

							for (int r = 0; r < myTable.getRowCount(); r++) {
								cellID.add(myTable.getValueAt(r, 0).toString());
								cellX.add(myTable.getValueAt(r, 1).toString());
								cellZ.add(myTable.getValueAt(r, 3).toString());
							}
							int index = 0;
							for (int r = 0; r < cellID.size(); r++)
								if (listOfFiles[i].getName().replaceAll("_filt_ali8bits2.Labels.am", "")
										.contains(cellID.get(r)))
									index = r;
//
//							ImagePlus[] imps = new ImagePlus[areas.size()];
//							for (int y = 0; y < imps.length; y++)
//								imps[y] = new ImagePlus(listOfFiles[i].getName().replaceAll(".am", ""),
//										decoder.getStack());
							for (int j = 2; j <= values.length - 1; j++) {
								rt2.incrementCounter();
								rt2.addValue(head2[0], String.valueOf(j));
								rt2.addValue(head2[1], String.valueOf(values[j]));
								rt2.addValue(head2[2],
										String.valueOf((values[j] * Math.pow(Double.valueOf(cellX.get(index)), 2))
												* Double.valueOf(cellZ.get(index))));
							}
							try {
								rt2.saveAs((textSave.getText() + File.separator + directImage.getName() + File.separator
										+ "AnalyzeVolumes_"
										+ listOfFiles[i].getName().replaceAll("_filt_ali8bits2.Labels.am", "")
										+ ".csv"));
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							ImagePlus impResults, impResults0;
							sumBranchesT = new ArrayList<Double>();
							sumJunctionsT = new ArrayList<Double>();
							sumEndT = new ArrayList<Double>();
							sumJuncVoxT = new ArrayList<Double>();
							sumSlabVoxT = new ArrayList<Double>();
							averageAvT = new ArrayList<Double>();
							sumTripleT = new ArrayList<Double>();
							sumQuadruplT = new ArrayList<Double>();
							sumMaxT = new ArrayList<Double>();
							for (int j = 0; j < indexThreshold.size(); j++) {
								sumBranches = new ArrayList<Double>();
								sumJunctions = new ArrayList<Double>();
								sumEnd = new ArrayList<Double>();
								sumJuncVox = new ArrayList<Double>();
								sumSlabVox = new ArrayList<Double>();
								averageAv = new ArrayList<Double>();
								sumTriple = new ArrayList<Double>();
								sumQuadrupl = new ArrayList<Double>();
								sumMax = new ArrayList<Double>();

								impResults = impCopy.duplicate();
								// impResults.show();
								if (rt1 != null)
									rt1.reset();
								rt1 = new ResultsTable();

								// IJ.log(Double.valueOf(indexThreshold.get(j)) + "----------los indices");
								IJ.setAutoThreshold(impResults, "Default dark");
								IJ.setRawThreshold(impResults, Double.valueOf(indexThreshold.get(j)),
										Double.valueOf(indexThreshold.get(j)), null);
								Prefs.blackBackground = false;
								IJ.run(impResults, "Convert to Mask", "method=Default background=Dark");
								// IJ.log(impResults.getNSlices() + "---slices");
								ImagePlus impResults2 = ZProjector.run(impResults.duplicate(), "max");
								IJ.run(impResults2, "Create Selection", "");
								Roi roi = impResults2.getRoi();
								impResults.setRoi(roi);
								ImagePlus impResultsEnd = scaler.resize(impResults, roi.getBounds().width,
										roi.getBounds().height, impResults.getNSlices(), "bilinear");
								ImagePlus[] slices = stack2images(impResultsEnd);

								// IJ.run(impResultsEnd, "Invert LUT", "");
								for (int s = 0; s < slices.length; s++) {
									IJ.run(slices[s], "Create Selection", "");
									Roi roiSlice = slices[s].getRoi();
									if (roiSlice != null) {
										Roi rois[] = new ShapeRoi(roiSlice).getRois();
										for (int r = 0; r < rois.length; r++)
											if (rois[r].getStatistics().area <= 5) {
												IJ.run(slices[s], "Invert LUT", "");
												slices[s].setRoi(rois[r]);
												IJ.run(slices[s], "Clear", "slice");
												IJ.run(slices[s], "Invert LUT", "");

											}
									}

								}
								ImagePlus stack = ImagesToStack.run(slices);
								// stack.show();
								impResults0 = scaler.resize(stack, (int) (stack.getWidth()
										* (Double.valueOf(cellX.get(index)) / Double.valueOf(cellZ.get(index)))),
										(int) (stack.getHeight() * (Double.valueOf(cellX.get(index))
												/ Double.valueOf(cellZ.get(index)))),
										stack.getNSlices(), "bilinear");
								IJ.setRawThreshold(impResults0, 1, 255, null);
								IJ.run(impResults0, "Convert to Mask", "method=Default background=Dark");

								// IJ.log(cellX.get(index) + "---------pasaaaa" + "------" + index);
								IJ.run(impResults0, "Skeletonize (2D/3D)", "");

								skel.setup("", impResults0);
								SkeletonResult skelResult = null;
								skelResult = skel.run(AnalyzeSkeleton_.SHORTEST_BRANCH, false, false, null, true,
										false);

								int numOfTrees = skelResult.getNumOfTrees();
								int[] numberOfBranches = skelResult.getBranches();
								int[] numberOfJunctions = skelResult.getJunctions();
								int[] numberOfEndPoints = skelResult.getEndPoints();
								int[] numberOfJunctionVoxels = skelResult.getJunctionVoxels();
								int[] numberOfSlabs = skelResult.getSlabs();
								double[] averageBranchLength = skelResult.getAverageBranchLength();
								int[] numberOfTriplePoints = skelResult.getTriples();
								int[] numberOfQuadruplePoints = skelResult.getQuadruples();
								double[] maximumBranchLength = skelResult.getMaximumBranchLength();

								if (numOfTrees == 1) {
									idsT.add(Double.valueOf(ids.get(j)));
									areasPixT.add(areas.get(j));
									areasNMT.add((areas.get(j) * Math.pow(Double.valueOf(cellX.get(index)), 2))
											* Double.valueOf(cellZ.get(index)));

								}
								if (numOfTrees > 1)
									for (int x = 0; x < numOfTrees; x++) {
										idsT.add(Double.valueOf(ids.get(j)));
										areasPixT.add(areas.get(j));
										areasNMT.add((areas.get(j) * Math.pow(Double.valueOf(cellX.get(index)), 2))
												* Double.valueOf(cellZ.get(index)));

									}

								for (int x = 0; x < numOfTrees; x++) {
									// IJ.log("pasa por los treeeeeeeeeees");
									rt1.incrementCounter();
									branchesT.add(Double.valueOf(numberOfBranches[x]));
									junctT.add(Double.valueOf(numberOfJunctions[x]));
									endT.add(Double.valueOf(numberOfEndPoints[x]));
									junctVoxT.add(Double.valueOf(numberOfJunctionVoxels[x]));
									slabsT.add(Double.valueOf(numberOfSlabs[x]));
									ablT.add(Double.valueOf(averageBranchLength[x]));
									tripleT.add(Double.valueOf(numberOfTriplePoints[x]));
									quadT.add(Double.valueOf(numberOfQuadruplePoints[x]));
									mblT.add(Double.valueOf(maximumBranchLength[x]));
									sumBranches.add(Double.valueOf(numberOfBranches[x]));
									sumJunctions.add(Double.valueOf(numberOfJunctions[x]));
									sumEnd.add(Double.valueOf(numberOfEndPoints[x]));
									sumJuncVox.add(Double.valueOf(numberOfJunctionVoxels[x]));
									sumSlabVox.add(Double.valueOf(numberOfSlabs[x]));
									averageAv.add(Double.valueOf(averageBranchLength[x]));
									sumTriple.add(Double.valueOf(numberOfTriplePoints[x]));
									sumQuadrupl.add(Double.valueOf(numberOfQuadruplePoints[x]));
									sumMax.add(Double.valueOf(maximumBranchLength[x]));

								}

								sumBranchesT.add(sumBranches.stream().mapToDouble(a -> a).sum());
								sumJunctionsT.add(sumJunctions.stream().mapToDouble(a -> a).sum());
								sumEndT.add(sumEnd.stream().mapToDouble(a -> a).sum());

								sumJuncVoxT.add(sumJuncVox.stream().mapToDouble(a -> a).sum());

								sumSlabVoxT.add(sumSlabVox.stream().mapToDouble(a -> a).sum());
								if (averageAv.size() > 1)
									averageAvT.add(averageAv.stream().mapToDouble(a -> a).average().getAsDouble());
								if (averageAv.size() == 1)
									averageAvT.add(averageAv.get(0));

								sumTripleT.add(sumTriple.stream().mapToDouble(a -> a).sum());

								sumQuadruplT.add(sumQuadrupl.stream().mapToDouble(a -> a).sum());

								sumMaxT.add(sumMax.stream().mapToDouble(a -> a).sum());

							}

							for (int x = 0; x < idsT.size(); x++) {
								rt1.incrementCounter();
								rt1.addValue(head1[0], idsT.get(x));
								rt1.addValue(head1[1], String.valueOf(branchesT.get(x)));
								rt1.addValue(head1[2], String.valueOf(junctT.get(x)));
								rt1.addValue(head1[3], String.valueOf(endT.get(x)));
								rt1.addValue(head1[4], String.valueOf(junctVoxT.get(x)));
								rt1.addValue(head1[5], String.valueOf(slabsT.get(x)));
								rt1.addValue(head1[6], String.valueOf(ablT.get(x)));
								rt1.addValue(head1[7], String.valueOf(tripleT.get(x)));
								rt1.addValue(head1[8], String.valueOf(quadT.get(x)));
								rt1.addValue(head1[9], String.valueOf(mblT.get(x)));

							}
							try {
								rt1.saveAs((textSave.getText() + File.separator + directImage.getName() + File.separator
										+ "AnalyzeSkeleton_"
										+ listOfFiles[i].getName().replaceAll("_filt_ali8bits2.Labels.am", "")
										+ ".csv"));
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							// rt1.showRowNumbers(true);

						}

						taskOutput.append("Done!");
						frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));

					}
				});
				mainProcess.start();

			}
		});

		cancelButton.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent evt) {
				frameInitial.dispatchEvent(new WindowEvent(frameInitial, WindowEvent.WINDOW_CLOSING));
			}
		});

	}

	public static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = MitochondrialAnalyzer_.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			return null;
		}
	}

	static <T> T[] append(T[] arr, T element) {
		final int N = arr.length;
		arr = Arrays.copyOf(arr, N + 1);
		arr[N] = element;
		return arr;
	}

	public static ImagePlus[] stack2images(ImagePlus imp) {
		String sLabel = imp.getTitle();
		String sImLabel = "";
		ImageStack stack = imp.getStack();

		int sz = stack.getSize();
		int currentSlice = imp.getCurrentSlice(); // to reset ***

		DecimalFormat df = new DecimalFormat("0000"); // for title
		ImagePlus[] arrayOfImages = new ImagePlus[imp.getStack().getSize()];
		for (int n = 1; n <= sz; ++n) {
			imp.setSlice(n); // activate next slice ***

			// Get current image processor from stack. What ever is
			// used here should do a COPY pixels from old processor to
			// new. For instance, ImageProcessor.crop() returns copy.
			ImageProcessor ip = imp.getProcessor(); // ***
			ImageProcessor newip = ip.createProcessor(ip.getWidth(), ip.getHeight());
			newip.setPixels(ip.getPixelsCopy());

			// Create a suitable label, using the slice label if possible
			sImLabel = imp.getStack().getSliceLabel(n);
			if (sImLabel == null || sImLabel.length() < 1) {
				sImLabel = "slice" + df.format(n) + "_" + sLabel;
			}
			// Create new image corresponding to this slice.
			ImagePlus im = new ImagePlus(sImLabel, newip);
			im.setCalibration(imp.getCalibration());
			arrayOfImages[n - 1] = im;

			// Show this image.
			// imp.show();
		}
		// Reset original stack state.
		imp.setSlice(currentSlice); // ***
		if (imp.isProcessor()) {
			ImageProcessor ip = imp.getProcessor();
			ip.setPixels(ip.getPixels()); // ***
		}
		imp.setSlice(currentSlice);
		return arrayOfImages;
	}
}
