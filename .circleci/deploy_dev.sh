curl \
-F "status=2" \
-F "notify=0" \
-F "ipa=app/build/outputs/apk/debug/app-debug.apk" \
-H "X-HockeyAppToken: $HockeyAppToken" \
https://rink.hockeyapp.net/api/2/apps/upload