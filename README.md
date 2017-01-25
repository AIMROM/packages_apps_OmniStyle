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
day_header - display image on this day and hour - will overrule hour header
random_header - choose a random image from the image list
list_header - sequential iterate over the image list
change_interval - change interval in minutes - default interval is one hour
only valid in random or sequential mode

```xml
<daylight_headers>
    <hour_header hour="21" image="night" />
    <day_header day="25" month="12" image="christmas" />
    <day_header day="1" month="1" hour="0" image="new_year_1" />
    <day_header day="1" month="1" hour="12" image="new_year_2" />
</daylight_headers>

<daylight_headers>
    <random_header image="nature01" />
    <random_header image="nature02" />
    <random_header image="nature03" />
</daylight_headers>

<daylight_headers>
    <change_interval minutes="10" />
    <list_header image="nature01" />
    <list_header image="nature02" />
    <list_header image="nature03" />
</daylight_headers>

```
Label will be the name your pack will appear when selecting
