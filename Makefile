#  vim:ts=4:sts=4:sw=4:noet
#
#  Author: Nho Luong
#  Date: 2015-10-06 14:02:35 +0100 (Tue, 06 Oct 2015)
#
#  https://github.com/nholuongut/lib-java
#
#  License: see accompanying Nho Luong LICENSE file
#
#  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help improve or steer this or other code I publish
#
#  https://www.linkedin.com/in/nholuong
#

ifneq ("$(wildcard bash-tools/Makefile.in)", "")
	include bash-tools/Makefile.in
endif

# breaks bootstrapping on Alpine
#SHELL := /usr/bin/env bash

REPO := nholuongut/lib-java

CODE_FILES := $(shell find . -name '*.java')

.PHONY: build
build: init
	$(MAKE) gradle

.PHONY: init
init:
	git submodule update --init --recursive

# used by CI
.PHONY: random-build
random-build:
	# Travis does't have SBT in java builds
	@x=$$(bash-tools/bin/random_select.sh build mvn gradle); echo $(MAKE) $$x; $(MAKE) $$x

.PHONY: maven
maven: mvn
	@:

.PHONY: mvn
mvn: init
	@echo ==========================
	@echo Java Library - Maven Build
	@echo ==========================
	@$(MAKE) printenv
	$(MAKE) system-packages
	./mvnw clean install
	@#ln -sfv target/nholuongut-utils-*.jar nholuongut-utils.jar

# don't use SBT - it will bundle Scala ballooning the jar size
.PHONY: sbt
sbt: init
	@echo ========================
	@echo Java Library - SBT Build
	@echo ========================
	@$(MAKE) printenv
	$(MAKE) system-packages
	@#                 .m2     .ivy
	sbt clean assembly publish publishLocal
	@#ln -sfv target/scala-*/nholuongut-utils-assembly-*.jar nholuongut-utils.jar

.PHONY: gradle
gradle: init
	@echo ===========================
	@echo Java Library - Gradle Build
	@echo ===========================
	@$(MAKE) printenv
	$(MAKE) system-packages
	@#              .m2     .ivy
	./gradlew clean install uploadArchives
	@#ln -sfv build/libs/nholuongut-utils-*.jar nholuongut-utils.jar

# for testing
.PHONY: all
all:
	$(MAKE) mvn
	$(MAKE) gradle
	$(MAKE) sbt

.PHONY: clean
clean:
	./mvnw clean || :
	sbt clean || :
	./gradlew clean || :
	@#rm -vf nholuongut-utils.jar

.PHONY: deep-clean
deep-clean:
	$(MAKE) clean
	rm -rf .gradle ~/.gradle/{caches,native,wrapper} ~/.m2/{repository,wrapper} ~/.ivy2 ~/.sbt/boot

.PHONY: p
p:
	$(MAKE) package
.PHONY: package
package:
	./mvnw package

.PHONY: test
test: unittest
	bash-tools/checks/check_all.sh

.PHONY: unittest
unittest:
	./mvnw test

.PHONY: tld
tld:
	wget -O src/main/resources/tlds-alpha-by-domain.txt http://data.iana.org/TLD/tlds-alpha-by-domain.txt

.PHONY: gradle-sonar
gradle-sonar:
	@# calls compileJava
	./gradlew sonarqube

.PHONY: mvn-sonar
mvn-sonar:
	./mvnw sonar:sonar

.PHONY: findbugs
findbugs:
	./mvnw compile
	./mvnw findbugs:findbugs
	./mvnw findbugs:gui

.PHONY: gradle-versioneye
gradle-versioneye:
	@# in gradle.properties now
	@#./gradlew -P versioneye.projectid=57616cdb0a82b20053182c74 versionEyeUpdate
	./gradlew versionEyeUpdate

.PHONY: mvn-versioneye
mvn-versioneye:
	./mvnw versioneye:update

.PHONY: sbt-versioneye
sbt-versioneye:
	sbt versioneye:updateProject

.PHONY: versioneye
versioneye:
	$(MAKE) mvn-versioneye
	$(MAKE) gradle-versioneye
	$(MAKE) sbt-versioneye
