# MitochondrialAnalyzer_
This is a Java-based plugin for quantification of mitochondrial characteristics and subsequent morphology operational under either ImageJ or Fiji. 
## Table of Contents  
- [Overview of Procedure](#overview-of-procedure)
- [Installation](#installation)
- [References](#references)
-
<a name="overview-of-procedure"></a>
## Overview of Procedure
  The approach for quantification of mitochondrial characteristics and subsequent morphology was performed using a Java-based “MitochondrialAnalyzer” plugin operational under either ImageJ or Fiji. The basic workflow is to reach those paths in which the files to be analyzed are located and in which the outputs will be generated. Furthermore, user may choose the directory in which the file (“.csv”) containing information related to voxel size for each cell is saved.  Once done, press the ok button to start the process. User does not need to select any specific parameters to perform the analysis as they are set by default. Due to this plugin includes several ImageJ’s plugins for image preprocessing and analysis, this tool is able to extract, if required, each serie from “.lif” file as a single TIFF multichannel z-stack. Then Amira label files will be accessed to isolate each mitochondria as an independent instance to be analyzed for each cell. The labels associated with each mitochondria are shown in the same color chosen in Amira being each one stored in one of the available 255 channels (8 bit image). Thereafter, a “StackStatistics” object from a Amira stack is created, using 256 histogram bins and the entire pixel value range to calculate uncalibrated (raw) area for each mitochondria along with the associated id. Once done, a “.csv” file called “AnalyzeVolumes” will be generated for each cell saving the information described above. Then each stack will be thresholded to isolate each mitochondria setting the lower and upper levels under the id number associated. Then a “ZProjector” object of the entire isolated mitochondria stack will be done using the method “Maximum Intensity” in order to highlight non-background pixels. At this point, a selection surrounding mitochondria projection will be drawn and the stack scaled in accordance with voxel size. To get a more accurate analysis, those structures in projection whose area smaller than 5 pixels will be filtered out since their signal is irrelevant. For further skeleton analysis, skeletonization is first applied generating a skeleton map through removing pixels from edges of structures. Subsequently, a “AnalyzeSkeleton” object is created for skeleton analysis of each mitochondria within each cell calculating for each skeleton the number of branches (slab segments, connecting end-points, end-points and junctions), the number of voxels of every type (end-point, slab and junction voxels), the number of actual junctions, the number of both triple and quadruple points and finally, the average and maximum length of branches. All this information detailed above is saved as “AnalyzeSkelton” in the directory corresponding to each cell.

<p align="center">
  <img width="800" height="450" src="https://user-images.githubusercontent.com/83207172/157080170-b1b9844f-e7bf-477c-94eb-b91686ee62e7.png">
</p>

<a name="installation"></a>
## Installation

The ***MitochondrialAnalyzer*** plugin may be installed in Fiji or ImageJ by following these steps:

1. In the event of not having ImageJ or Fiji already installed, please navigate through [https://imagej.nih.gov/ij/download.html](https://imagej.nih.gov/ij/download.html), download it and then, install it on a computer with Java pre-installed in either Windows, Mac OS or Linux systems.
2.  Once done, download the plugin JAR file named as [MitochondrialAnalyzer_.jar](https://github.com/QuantitativeImageAnalysisUnitCNB/MitochondrialAnalyzer_/blob/master/MitochondrialAnalyzer_.jar) from repository.
3.  Move this file into the `ImageJ/Fiji "plugins" subfolder`, or differently, by dragging and dropping into the `ImageJ/Fiji main window` or optionally, running through menu bar `"Plugins"` **→** `"Install"` **→**  `‘Path to File’`. Then restart either ImageJ or Fiji and it is about time to start using "MitochondrialAnalyzer".
<a name="references"></a>
## References
<a id="1">[1]</a> 
Arganda-Carreras, I., Fernández-González, R., Muñoz-Barrutia, A., & Ortiz-De-Solorzano, C. (2010). 
3D reconstruction of histological sections: Application to mammary gland tissue. 
Microscopy Research and Technique, 73(11), 1019–1029. [![DOI:10.1038/nmeth.2019](http://img.shields.io/badge/DOI-10.1101/2021.01.08.425840-B31B1B.svg)](https://doi.org/10.1002/jemt.20829)


