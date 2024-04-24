GRADLE_CMD ?= ./gradlew
ADB_CMD ?= adb

.PHONY: clean
clean:
	@$(GRADLE_CMD) clean

.PHONY: build
build:
	@$(GRADLE_CMD) assemble

.PHONY: dist.wasm
dist.wasm:
	@$(GRADLE_CMD) wasmJsBrowserDistribution

.PHONY: fmt
fmt:
	@$(GRADLE_CMD) spotlessApply

.PHONY: publish
publish:
	@$(GRADLE_CMD) publish

.PHONY: publish.local
publish.local:
	@$(GRADLE_CMD) publishToMavenLocal


## ----- Sample App ----- ##

.PHONY: play.wasm
play.wasm:
	@$(GRADLE_CMD) wasmJsBrowserDevelopmentRun

.PHONY: play.desktop
play.desktop:
	@$(GRADLE_CMD) runDistributable

.PHONY: play.android
play.android:
	@$(GRADLE_CMD) installDebug
	@$(ADB_CMD) shell am start -n soil.kmp/soil.kmp.MainActivity
