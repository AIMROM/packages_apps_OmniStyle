Condition icon packs support
-----
Activity
```xml
<action android:name="org.omnirom.WeatherIconPack" />
```
activity name is used to defined the prefix for the image names
```xml
<tag>_<condition id>.png
```
```xml
		<activity
			android:name=".<tag>"
			android:label="<label>" >
			<intent-filter>
				<action android:name="org.omnirom.WeatherIconPack" />

				<category android:name="android.intent.category.DEFAULT" />
			</intent-filter>
		</activity>
```
Label will be the name your pack will appear when selecting

Daylight header images
-----
Activity
activity name is used to defined name of the config file
```xml
<action android:name="org.omnirom.DaylightHeaderPack1" />
```
```xml
		<activity
			android:name=".<tag>"
			android:label="<label>" >
			<intent-filter>
				<action android:name="org.omnirom.DaylightHeaderPack1" />

				<category android:name="android.intent.category.DEFAULT" />
			</intent-filter>
		</activity>
```
Config file
```xml
assets/<tag>.xml
```

hour_header - display image if hour is larger then value
day_header - display image on this day - will overrule hour header
random_header - choose a random image every hour

```xml
<daylight_headers>
    <hour_header hour="21" image="notifhead_night" />
    <day_header day="25" month="12" image="notifhead_christmas" />
</daylight_headers>

<daylight_headers>
    <random_header image="nature01" />
    <random_header image="nature02" />
    <random_header image="nature03" />
</daylight_headers>
```
Label will be the name your pack will appear when selecting
