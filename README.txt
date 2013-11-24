Über Heldenverwaltung
---------------------

Heldenverwaltung ist ein Werkzeug, um Helden (und Heldengruppen) für das Rollenspielsystem DSA 3 zu verwalten.

Lizenz
------

Heldenverwaltung ist Open Source unter der General Public Licence. Siehe licence.txt oder lizenz-deutsch.txt.

Versionshistorie
----------------

Siehe ChangeLog.txt.

Bauen
-----

Es werden außer den Sourcen auch die Libraries (jar-Files) aus dem libraries-Verzeichnis der Binärinstallation der Heldenverwaltung benötigt. Diese als Referenz angeben und alle java-Dateien übersetzen, und zwar in zwei verschiedene Pakete, etwa so (Linux):

javac -source 1.5 -target 1.5 -d classes -cp libraries/synthetica.jar `find src/de -name "*.java"`
pushd libraries
cp -R ../images/de de
mv ../classes/de/javasoft/plaf/synthetica/SyntheticaWalnutLookAndFeel.class de/javasoft/plaf/synthetica/
jar cf syntheticaWalnut.jar de
rm -rf de
rm -rf classes/de
popd

javac -source 1.5 -target 1.5 -d classes -cp libraries/jdic.jar:[alle anderen libraries]:libraries/syntheticaWalnut.jar `find src/dsa -name "*.java"`
cp Manifest classes/
cd classes
jar cfm heldenverwaltung.jar Manifest dsa
