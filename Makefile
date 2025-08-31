GRADLE_CMD ?= ./gradlew
ADB_CMD ?= adb

.DEFAULT_GOAL := help
.PHONY: help
help:
	@grep -E '^[a-zA-Z_-][a-zA-Z_\-\.]+:.*$$' $(MAKEFILE_LIST) | awk -F ':' '{ print $$1 }'

.PHONY: clean
clean:
	@$(GRADLE_CMD) clean

.PHONY: build
build:
	@$(GRADLE_CMD) buildLibs

.PHONY: test
test:
	@$(GRADLE_CMD) allTests

.PHONY: fmt
fmt:
	@$(GRADLE_CMD) spotlessApply

.PHONY: kover
kover:
	@$(GRADLE_CMD) koverHtmlReport

.PHONY: dokka
dokka:
	@$(GRADLE_CMD) dokkaGeneratePublicationHtml

.PHONY: publish
publish:
	@$(GRADLE_CMD) publish

.PHONY: publish.local
publish.local:
	@$(GRADLE_CMD) publishToMavenLocal


## ----- Sample App ----- ##

.PHONY: sample.android.run
sample.android.run:
	@$(GRADLE_CMD) installDebug
	@$(ADB_CMD) shell am start -n soil.kmp/soil.kmp.MainActivity

.PHONY: sample.desktop.run
sample.desktop.run:
	@$(GRADLE_CMD) runDistributable

.PHONY: sample.desktop.hotRun
sample.desktop.hotRun:
	@$(GRADLE_CMD) hotRunDesktop --auto


.PHONY: sample.wasm.dist
sample.wasm.dist:
	@$(GRADLE_CMD) wasmJsBrowserDistribution

.PHONY: sample.wasm.run
sample.wasm.run:
	@$(GRADLE_CMD) wasmJsBrowserDevelopmentRun
