#!make
SHELL ?= /bin/bash

JAR_VERSION := $(shell mvn -q -Dexec.executable="echo" -Dexec.args='$${project.version}' --non-recursive exec:exec -DforceStdout)
#JAR_VERSION := 1.0
JAR_FILE := mn2sts-$(JAR_VERSION).jar

SRCDIR := src/test/resources
# Can change to
# SRCFILE := $(SRCDIR)/iso-tc154-8601-1-en.xml
SRCFILE := $(SRCDIR)/iso-rice-en.cd.mn.xml $(SRCDIR)/iso-tc154-8601-1-en.mn.xml

DESTDIR := documents
DESTSTSXML := $(patsubst %.mn.xml,%.sts.xml,$(patsubst src/test/resources/%,documents/%,$(SRCFILE)))
DESTSTSHTML := $(patsubst %.xml,%.html,$(DESTSTSXML))

SAXON_URL := https://repo1.maven.org/maven2/net/sf/saxon/Saxon-HE/10.1/Saxon-HE-10.1.jar
STS2HTMLXSL := https://www.iso.org/schema/isosts/resources/isosts2html_standalone.xsl

all: documents.html

src/test/resources/iso-tc154-8601-1-en.mn.xml: tests/iso-8601-1/documents/iso-tc154-8601-1-en.xml
	cp $< $@

tests/iso-8601-1/documents/iso-tc154-8601-1-en.xml:
	cd tests/iso-8601-1; \
	$(MAKE) all; \
	cd ../..

src/test/resources/iso-rice-en.cd.mn.xml: tests/mn-samples-iso/documents/international-standard/rice-en.cd.xml
	cp $< $@

tests/mn-samples-iso/documents/international-standard/rice-en.cd.xml:

documents/%.mn.xml: src/test/resources/%.mn.xml
	cp $< $@

target/$(JAR_FILE):
	mvn --settings settings.xml -DskipTests clean package shade:shade

test: documents/%.mn.xml target/$(JAR_FILE)
	mvn -DinputXML=$< --settings settings.xml test surefire-report:report

deploy:
	mvn --settings settings.xml -Dmaven.test.skip=true clean deploy shade:shade

documents/%.sts.html: documents/%.sts.xml saxon.jar
	java -jar saxon.jar -s:$< -xsl:$(STS2HTMLXSL) -o:$@

documents/%.sts.xml: documents/%.mn.xml target/$(JAR_FILE) | documents
	java -jar target/$(JAR_FILE) --xml-file-in $< --xml-file-out $@

mn2stsDTD_NISO: target/$(JAR_FILE) $(DESTSTSXML) | documents
	for file in $(filter-out $<,$^); do \
	java -jar $< --xml-file-in $${file} --check-type dtd-niso; \
	done

mn2stsDTD_ISO: target/$(JAR_FILE) $(DESTSTSXML) | documents
	for file in $(filter-out $<,$^); do \
	java -jar $< --xml-file-in $${file} --check-type dtd-iso; \
	done

saxon.jar:
	curl -sSL $(SAXON_URL) -o $@

documents.rxl: $(DESTSTSHTML) | bundle
	bundle exec relaton concatenate \
	  -t "mn2sts samples" \
		-g "Metanorma" \
		documents $@

bundle:
	bundle

documents.html: documents.rxl
	bundle exec relaton xml2html documents.rxl

documents:
	mkdir -p $@

clean:
	mvn clean; \
	rm -rf documents

publish: published
published: documents.html
	mkdir published && \
	cp -a documents $@/

.PHONY: all clean test deploy version publish
