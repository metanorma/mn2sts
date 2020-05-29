#!make
SHELL ?= /bin/bash

JAR_VERSION := $(shell mvn -q -Dexec.executable="echo" -Dexec.args='$${project.version}' --non-recursive exec:exec -DforceStdout)
#JAR_VERSION := 1.0
JAR_FILE := mn2sts-$(JAR_VERSION).jar

SRCDIR := src/test/resources
SRCFILE := $(SRCDIR)/iso-tc154-8601-1-en.xml

DESTDIR := documents
DESTXML := $(DESTDIR)/iso-tc154-8601-1-en.sts.xml
DESTHTML := $(DESTDIR)/iso-tc154-8601-1-en.sts.html

STS2HTMLXSL := https://www.iso.org/schema/isosts/resources/isosts2html_standalone.xsl

all: target/$(JAR_FILE)

target/$(JAR_FILE):
	mvn --settings settings.xml -DskipTests clean package shade:shade

test: target/$(JAR_FILE)
	mvn --settings settings.xml test surefire-report:report

deploy:
	mvn --settings settings.xml -Dmaven.test.skip=true clean deploy shade:shade

mn2sts: target/$(JAR_FILE) | documents
	java -jar target/$(JAR_FILE) --xml-file-in $(SRCFILE) --xml-file-out $(DESTXML)

mn2stsXSD: documents
	java -jar target/$(JAR_FILE) --xml-file-in $(SRCFILE) --xml-file-out $(DESTXML) --check-type xsd-niso

mn2stsDTD_NISO: target/$(JAR_FILE) | documents 
	java -jar target/$(JAR_FILE) --xml-file-in $(SRCFILE) --xml-file-out $(DESTXML) --check-type dtd-niso

mn2stsDTD_ISO: target/$(JAR_FILE) | documents
	java -jar target/$(JAR_FILE) --xml-file-in $(SRCFILE) --xml-file-out $(DESTXML) --check-type dtd-iso

saxon.jar:
	curl -sSL https://repo1.maven.org/maven2/net/sf/saxon/Saxon-HE/10.1/Saxon-HE-10.1.jar -o saxon.jar

documents.html: mn2sts saxon.jar	
	java -jar saxon.jar -s:$(DESTXML) -xsl:$(STS2HTMLXSL) -o:$(DESTHTML)

documents:
	mkdir -p $@

clean:
	mvn clean

publish: published
published: documents.html
	mkdir published && \
	cp -a documents $@/

.PHONY: all clean test deploy version publish target/$(JAR_FILE)
