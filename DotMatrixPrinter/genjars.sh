#!/bin/sh

# Generate printaddon jars and sign the applet
# keystore and key password is: passwd
cp -rf classes/* bin/
#src/dk/apaq/vaadin/addon/printservice/gwt/public/printapplet_pol.jar
cd bin/
#jar cfm ../src/dk/apaq/vaadin/addon/printservice/gwt/public/printapplet_pol.jar ../AMANIFEST.MF org/vaadin/applet/* org/vaadin/applet/client/ui/* dk/apaq/vaadin/addon/printapplet/* org/vaadin/applet/sample/* org/json/*
jar cf ../src/dk/apaq/vaadin/addon/printservice/gwt/public/printapplet_pol.jar org/vaadin/applet/* org/vaadin/applet/client/ui/* dk/apaq/vaadin/addon/printapplet/* org/vaadin/applet/sample/* org/json/*
jar ufm ../src/dk/apaq/vaadin/addon/printservice/gwt/public/printapplet_pol.jar ../APP_MANIFEST.MF
cd ../
jarsigner -keystore ./.keystore src/dk/apaq/vaadin/addon/printservice/gwt/public/printapplet_pol.jar Pol
