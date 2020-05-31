#!make
SHELL ?= /bin/bash

JAR_VERSION := $(shell mvn -q -Dexec.executable="echo" -Dexec.args='$${project.version}' --non-recursive exec:exec -DforceStdout)
#JAR_VERSION := 1.0
JAR_FILE := mn2sts-$(JAR_VERSION).jar

SRCDIR := src/test/resources
# Can change to
# SRCFILE := $(SRCDIR)/iso-tc154-8601-1-en.xml
SRCFILE := $(SRCDIR)/iso-rice-en.cd.xml

DESTDIR := documents
DESTXML := $(patsubst src/test/resources/%,documents/%,$(SRCFILE))
DESTHTML := $(patsubst %.xml,%.sts.html,$(DESTXML))

SAXON_URL := https://repo1.maven.org/maven2/net/sf/saxon/Saxon-HE/10.1/Saxon-HE-10.1.jar
STS2HTMLXSL := https://www.iso.org/schema/isosts/resources/isosts2html_standalone.xsl

all: target/$(JAR_FILE)

src/test/resources/iso-tc154-8601-1-en.xml: tests/iso-8601-1/documents/iso-tc154-8601-1-en.xml
	cp $< $@

tests/iso-8601-1/documents/iso-tc154-8601-1-en.xml:
	pushd tests/iso-8601-1; \
	$(MAKE) clean all; \
	popd

src/test/resources/iso-rice-en.cd.xml: tests/mn-samples-iso/documents/international-standard/rice-en.cd.xml
	cp tests/mn-samples-iso/documents/international-standard/rice-en.cd.xml $@

tests/mn-samples-iso/documents/international-standard/rice-en.cd.xml:

target/$(JAR_FILE):
	mvn --settings settings.xml -DskipTests clean package shade:shade

test: target/$(JAR_FILE)
	mvn --settings settings.xml test surefire-report:report

deploy:
	mvn --settings settings.xml -Dmaven.test.skip=true clean deploy shade:shade

documents/%.xml: target/$(JAR_FILE) src/test/resources/iso-rice-en.cd.xml | documents
	java -jar target/$(JAR_FILE) --xml-file-in $(SRCFILE) --xml-file-out $(DESTXML)

mn2stsDTD_NISO: | documents
	java -jar target/$(JAR_FILE) --xml-file-in $(SRCFILE) --xml-file-out $(DESTXML) --check-type dtd-niso

mn2stsDTD_ISO: | documents
	java -jar target/$(JAR_FILE) --xml-file-in $(SRCFILE) --xml-file-out $(DESTXML) --check-type dtd-iso

saxon.jar:
	curl -sSL $(SAXON_URL) -o $@

documents.html: documents/iso-rice-en.cd.xml saxon.jar
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
