Pointmark
=====================================

This is a a plugin for importing sets of "valid" and "invalid" points for an image in Fiji in order to edit and export them.

How To Set Up
------
1. Make sure Fiji (ImageJ2) is installed
2. Add jar to Fiji.app/plugins directory
3. Open an image within ImageJ
4. Navigate to Plugins menu, if the second step was followed correctly then "Coordinate Controller" should appear upon scrolling down
5. Select Coordinate Controller to boot up the plugin

User Manual
------
1. Import:
    +   Click on the Import button in Coordinate Controller's log menu and select a JSON file to import onto the open image.
    +   Correct format for JSON has "valid" and "invalid" headers storing arrays of size 2, the first value being the x-coordinate and the second the y-coordinate. Can be doubles (decimals) or integers.
2. Export:
    +   Click on the Export button in Coordinate Controller's log menu and select a JSON file to export points into.
    +   WARNING: Previous contents of the JSON will be overwritten upon an export.
3. Adding a Point:
    +   Select the Multi-point Tool in ImageJ
    +   Choose either the valid or invalid point set
    +   Click the image on where you would like to mark a point
4. Deleting a Point:
    +   Make sure the set of the point you would like to delete is selected (it will be a cyan/light blue color)
    +   Hold Alt + click on the point to delete
5. Swapping a Point:
    +   Make sure the set of the point you would like to swap is selected (it will be a cyan/light blue color)
    +   Hold Q + click on the point to swap it to the other point set (it should change color)

NOTE: The ability to move points is disabled for this plugin in order to reduce errors.
