# What is PointMark?

PointMark is a tool developed by CSAFE, to manually mark interest and false positive
points on Shoeprint images, and export them in JSON format as data for evaluation.

PointMark is a plugin for [ImageJ2][ij2] aka
[FIJI][fiji]; the jar is accessible via Fiji's Plugins menu.

## What is the purpose of this repo?

The purpose of this repo is to provide a streamlined method of marking and saving
interest points on a ShoePrint image to be evaluated.

This repo contains:

- scripts to download ImageJ (for different OS/arch combos)
- scripts to download the necessary plugins used in PointMark
- configuration files for ImageJ plugins

Our Github Actions runner provides the PointMark plugin as a
Java JAR, which can be added to Fiji's Plugins Folder for use.

[ij2]: https://imagej.net/software/imagej2/
[fiji]: https://imagej.net/software/fiji/
[actionbar]: https://imagej.net/plugins/action-bar
