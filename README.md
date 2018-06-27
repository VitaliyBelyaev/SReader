# SReader
The app which builds graphics using values from accelerometer and gyroscope

- The app has a service, that starts as foreground service on app launch
- This service recieve data from acceletometer and gyroscope and invoke C++ method through JNI to find averege for each sensor
- C++ method take as input array of size N, find min and max values, then calculate average without that min and max values
- Every average value saves in EntriesRepository, so even when app is not visible or closed, sensors data still collects
- MainActivity has two ChartViews for each sensor
- Sensors data for the charts getting from EntriesRepository, so we always have valide data
- Stop button at the toolbar stops service and close app
