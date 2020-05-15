WORKING_PATH="./"
APP_Project="AgoraLive"
APP_TARGET=$1

echo "WORKING_PATH: ${WORKING_PATH}"
echo "APP_TARGET: ${APP_TARGET}"

cd ${WORKING_PATH}
echo `pwd`

rm -f *.ipa
rm -rf *.app
rm -f *.zip
rm -rf dSYMs
rm -rf *.dSYM
rm -f *dSYMs.zip
rm -rf *.xcarchive

BUILD_DATE=`date +%Y-%m-%d-%H.%M.%S`
ArchivePath=${APP_TARGET}-${BUILD_DATE}.xcarchive

TARGET_FILE=""
if [ ! -f "Podfile" ];then
TARGET_FILE="${APP_Project}.xcodeproj"
xcodebuild clean -project ${TARGET_FILE} -scheme "${APP_TARGET}" -configuration Release
xcodebuild -project ${TARGET_FILE} -scheme "${APP_TARGET}" -archivePath ${ArchivePath} archive
xcodebuild -exportArchive -exportOptionsPlist exportPlist.plist -archivePath ${ArchivePath} -exportPath .
else
pod install
TARGET_FILE="${APP_Project}.xcworkspace"
xcodebuild clean -workspace ${TARGET_FILE} -scheme "${APP_TARGET}" -configuration Release
xcodebuild -workspace ${TARGET_FILE} -scheme "${APP_TARGET}" -archivePath ${ArchivePath} archive
xcodebuild -exportArchive -exportOptionsPlist exportPlist.plist -archivePath ${ArchivePath} -exportPath .
fi

mkdir app
mv *.ipa app && mv *.xcarchive app
zip -q -r app.zip app

cp pugongying.sh app