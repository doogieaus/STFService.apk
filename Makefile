all:
	JAVA_HOME=/opt/android-studio/jre ./gradlew assembleDebug
	cp ./app/build/outputs/apk/debug/app-debug.apk ../stf/vendor/STFService/STFService.apk
	cp ./app/src/main/proto/wire.proto ../stf/vendor/STFService/wire.proto
