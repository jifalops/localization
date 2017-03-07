# Localization App
An app for collecting data and using interpreted results to do ranging and localization.


## Main functions
1. Sampling of raw data at known distances.
    * Signal strength using WiFi, Bluetooth, and Bluetooth low energy.
    * Time of flight using Bluetooth.
2. Submit raw samples to remote database.
3. Receive interpreted results from the database which explain to the app how to estimate range.
  * Neural network weights and metrics - The number of inputs, hidden neurons, and the weights.
4. Use the interpreted results to perform ranging on new ranging samples.
5. Perform localization using ranging results.


## Use cases
There are a few different ways to use the app summarized below.

### Collect samples
Collect raw samples from devices at known distances.
This can be done at any time to add to the database.
If interpreted results have been received from the remote database,
the app will attempt ranging at the same time.

Note: Raw samples must have a clear line-of-sight between devices.

### Ranging
Perform ranging on new ranging samples from devices at unknown distances in real time.
Localization will not be attempted here.

### Localization
Uses ranging to put devices onto a shared coordinate system.

### Testing
The functional building blocks of the app have their own demo areas for testing.
* WiFi scanning
* Device to device communication using Network Service Discovery


## Developer notes
The app uses Firebase as a remote database. The project name is localization-7535f.

### Screens
1. MainActivity - Allows to user to choose which use case they are using the app for.
   It also shows some status information.
2. SampleCollectionActivity - Collect raw samples using RSS or TOF.
  * RssSamplingActivity - Sample RSS from 2.4GHz WiFi, 5GHz WiFi,
    Bluetooth, and Bluetooth LE simultaneously.
  * TofSamplingActivity - Sample TOF from Bluetooth.
3. RangingActivity - Apply interpreted results to new incoming data.
  * RssRangingActivity - Test the current ranging algorithms for RSS using WiFi and BT.
  * TofRangingActivity - Test the current ranging algorithm for TOF using BT.
4. LocalizationActivity - Have devices share ranging information and see them 
   on a 2D coordinate system. This is only available using RSS.
5. DemoActivity - Try individual demos of the app's functional building blocks.


### Local storage
Most data will need to be stored locally. Anything to be sent to or received from 
the database will need to be persisted. Settings/preferences will also need to be saved.

#### Raw samples
When collecting raw samples they are stored as they come in. After the user sends
samples to the remote database and they are received, those samples are purged
from storage.

##### RSS sample fields
These apply to 2.4GHz WiFi, 5GHz WiFi, Bluetooth, and Bluetooth LE.
* mac1 - Local MAC address
* mac2 - Remote MAC address
* rss - Received signal strength in dBm
* freq - Frequency in MHz (roughly 2400 or 5000)
* width - Channel width in MHz, either 20, 40, 80, or 160.
* dist - Actual distance in meters

Note that NN training sets that include freq and width will likely over-fit
unless a substantially large and diverse set of samples are collected.

##### TOF sample fields
These apply to Bluetooth measurements at both the HCI/snoop level and Java level.
* mac1 - Local MAC address
* mac2 - Remote MAC address
* tof - Time of flight in nanoseconds
* dist - Actual distance in meters

#### Interpreted results (NN fields)
* samples - The number of raw samples which make a range sample.
* method - How to combine raw samples into a ranging sample.
  Must be "median" or "mean".
* drop - The number of raw samples to drop from both the high and low end.
  If samples is 3 and drop is 1, 5 raw samples will be taken to make 1 ranging sample.
* inputs - How many inputs the NN uses. Either 1 or 3, depending on whether 
  frequency and channel width should be included.
* hidden - The number of hidden neurons to use in the NN.
* weights - The array of weights to use for calculating range using the NN.
  The NN is MLP so the number of weights should be equal to 
  `hidden * (inputs + outputs + 1) + outputs`. The number of outputs 
  is always 1, so if using 3 inputs and 2 hidden, there should be 11 weights.
 
Note that the inputs and outputs of the NN must be scaled. RSS ranges 
from -120 dBm to 0 dBm, and would be plugged into the NN as -1 and 1 respectively.
Outputs can range from 0 meters to 100 meters, scaled to 0 and 1 respectively.


#### User preferences
* Debug mode

#### File names
All files except user preferences are stored in the app's storage on the SD card.
Raw samples and NN settings are stored as .csv files without quotes.

Raw samples 
* rss_wifi4g.csv
* rss_wifi5g.csv
* rss_bt.csv
* rss_btle.csv
* tof_bt_hci.csv
* tof_bt_java.csv

Neural network settings (samples, method, drop, inputs, hidden, weights...)
* nn_rss_wifi4g.csv
* nn_rss_wifi5g.csv
* nn_rss_bt.csv
* nn_rss_btle.csv
* nn_tof_bt_hci.csv
* nn_tof_bt_java.csv


### Old repos
Time of flight using Bluetooth has been added to this project from the repository 
at https://github.com/jifalops/flat. The results it produced were particularly bad
so no attempt was made to make it fully functional here. Who would have thought 
consumer devices are not really equipped to handle measuring things that move 
at the speed of light?

This repo is basically a refactoring of https://github.com/jifalops/wsnlocalize
that gets rid of the parts involved with interpreted the RSS data (training a neural
network, etc.). Those parts are now done outside of the app and the app simply
uses the results. A main goal is simplifying that repo and getting rid of the fluff.

### A note about localization
It is very difficult to manage a bunch of sockets for ad hoc device communication.
The piece are here and it _kind of_ works experimentally, but it will probably take
a lot longer for it to become reliable. As such, the app doesn't really accomplish
the localization part yet. It's mostly about ranging.
