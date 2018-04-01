curl \
-F "status=2" \
-F "notify=0" \
-F "ipa=@AppName/build/outputs/apk/debug/app-debug.apk" \
-H "X-HockeyAppToken: $HockeyAppToken" \
https://rink.hockeyapp.net/api/2/apps/$AndroidAppId_Dev/app_versions/upload