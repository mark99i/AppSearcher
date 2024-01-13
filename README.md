# AppSearcher
In MIUI 13, Xiaomi began using Google Assistant instead of a convenient search application, I did not find an analogue and solved this problem in this way

This small application is designed to be installed as a desktop search, allows you to search and launch other applications.

In addition to the basic functionality, this application:
- Save the last 5 running applications from it as quick access icons (just like in MIUI 12)
- Search for the entered text on the Internet, using Yandex (by default) or Google.
- Search in contacts
- Uses cache for fast open

## Installation
Download and install APK from [Releases](../../releases)

**Global MIUI ROMs**  
Need connect ADB shell (or run in local terminal) and disable preinstalled applications: 
- MI Browser 
- Google Search
```
pm disable-user --user 0 com.mi.globalbrowser
pm disable-user --user 0 com.google.android.googlequicksearchbox
```

**Xiaomi.EU ROMs**  
Go to Settings - Home screen - Search Provider - select AppSearcher

## Notes:
- The first launch, as well as the cache reload when enabling or disabling options, will take longer than a normal launch
- Pressing Enter on the keyboard opens the first item in the list
- Long press on the quick launch icon removes the element
- If you haven't found a recently installed app or a recently added contact, click "Reload cache"

## Screenshot
<img src='https://github.com/mark99i/AppSearcher/raw/dev/photo_2022-03-12_03-00-02.jpg' width=200>

## Settings / Switches:
| Option        | Description                                                                                                                                                                                                                                                                                                                                           | 
|---------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Contacts      | Search among contacts (requires permission when first turned on)                                                                                                                                                                                                                                                                                      |
| Use cache     | Using internal storage to speed up startup. Synchronization with real data (the list of applications/contacts) occurs when the option is enabled and when the 'Reload cache' element is clicked, in other cases you can see outdated data (lack of recently installed applications/contacts or the presence of already deleted applications/contacts) |
| System apps   | Show system applications in the list                                                                                                                                                                                                                                                                                                                  |
| Google        | By default, clicking on the 'Search in the Intenet' element opens a Yandex search. Enabling this option changes the search engine to Google                                                                                                                                                                                                           |
| Recently apps | Show or hide the panel of recently launched applications (the last 5 pieces, as well as in the MIUI12 search)                                                                                                                                                                                                                                         |
