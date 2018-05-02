# deploy script for the web front-end

# This file is responsible for preprocessing all TypeScript files, making sure
# all dependencies are up-to-date, and copying all necessary files into the
# web deploy directory.

# This is the resource folder where maven expects to find our files
TARGETFOLDER=../backend/src/main/resources

# This is the folder that we used with the Spark.staticFileLocation command
WEBFOLDERNAME=web

# These are all of the singletons in the program
SINGLETONS=(ElementList EditEntryForm NewEntryForm Navbar)

# step 1: make sure we have someplace to put everything.  We will delete the
#         old folder tree, and then make it from scratch
rm -rf $TARGETFOLDER
mkdir $TARGETFOLDER
mkdir $TARGETFOLDER/$WEBFOLDERNAME

# step 2: update our npm dependencies
npm update

# step 3: copy jQuery, Handlebars, and Bootstrap files
cp node_modules/jquery/dist/jquery.min.js $TARGETFOLDER/$WEBFOLDERNAME
cp node_modules/handlebars/dist/handlebars.min.js $TARGETFOLDER/$WEBFOLDERNAME
cp node_modules/bootstrap/dist/js/bootstrap.min.js $TARGETFOLDER/$WEBFOLDERNAME
cp node_modules/bootstrap/dist/css/bootstrap.min.css $TARGETFOLDER/$WEBFOLDERNAME
cp -R node_modules/bootstrap/dist/fonts $TARGETFOLDER/$WEBFOLDERNAME

# step 4: compile TypeScript files
cp allProjectsPage.js $TARGETFOLDER/$WEBFOLDERNAME/
cp filePage.js $TARGETFOLDER/$WEBFOLDERNAME/
cp projectPage.js $TARGETFOLDER/$WEBFOLDERNAME/

# step 5: copy css files
cp style.css $TARGETFOLDER/$WEBFOLDERNAME/style.css
for s in ${SINGLETONS[@]}
do
cat css/$s.css >> $TARGETFOLDER/$WEBFOLDERNAME/style.css
done

# step 6: compile handlebars templates to the deploy folder
for s in ${SINGLETONS[@]}
do
node_modules/handlebars/bin/handlebars hb/$s.hb >> $TARGETFOLDER/$WEBFOLDERNAME/templates.js
done

# step 7: copy the main HTML shell
cp allProjectsPage.html $TARGETFOLDER/$WEBFOLDERNAME
cp filePage.html $TARGETFOLDER/$WEBFOLDERNAME
cp projectPage.html $TARGETFOLDER/$WEBFOLDERNAME
cp projectPag2e.html $TARGETFOLDER/$WEBFOLDERNAME
