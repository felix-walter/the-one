javac -sourcepath src -d target -extdirs lib/ src/core/*.java src/movement/*.java src/report/*.java src/routing/*.java src/gui/*.java src/input/*.java src/applications/*.java src/interfaces/*.java

mkdir target/gui/buttonGraphics
copy src/gui/buttonGraphics/* target/gui/buttonGraphics/