# GoogleFitExample

Sample android application that uses the Google Fit API

Development environment:
<br><b>IDE:</b> AndroidStudio
<br><b>Language:</b> Java
<br><b>Android SDK:</b> 19+

##Summary
Sample application that uses the basic of the Google Fit API.
It used the:
<br> - ConfiAPI: to create custom data types, 
<br> - HistoryAPI: to realize bulk operations in the fit store, like insert, delete and read,
<br> - RecordingAPI: to create subscriptions to sync with the cloud.

##Structure of the app
####HistoryDataActivity:
Use APIs: HistoryAPI and ReadingAPI
<br> Insert a STEP data type with a specific date range and use the recording to create a subscription and send to the cloud
<br>
####CustomDataActivity:
Uses APIs: HistoryAPI and ConfigAPI.
<br> It create a custom “TYPE” data type with a “value” field using the ConfigAPI.
<br> After that we save to the google fit storage using the same HistoryAPI.
<br>
####GoogleFitManager:
Class responsible create the google fit client, verify the authorization and request
the authorization dialog if it was not previously authorized.
