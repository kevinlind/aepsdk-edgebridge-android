# Migrating from Analytics mobile extension to the Edge Network using the Edge Bridge extension <!-- omit in toc -->

## Table of Contents <!-- omit in toc -->
- [Overview](#overview)
  - [Environment](#environment)
  - [Prerequisites](#prerequisites)
- [Adobe Experience Platform setup](#adobe-experience-platform-setup)
  - [1. Set up mobile property](#1-set-up-mobile-property)
  - [2. Configure a Rule to forward PII events to Edge Network](#2-configure-a-rule-to-forward-pii-events-to-edge-network)
- [Client-side implementation](#client-side-implementation)
  - [1. Get a copy of the files (code and tutorial app)](#1-get-a-copy-of-the-files-code-and-tutorial-app)
  - [2. Install Edge Bridge using dependency manager (Gradle)](#2-install-edge-bridge-using-dependency-manager-gradle)
  - [3. Update tutorial app code to enable Edge Bridge functionality](#3-update-tutorial-app-code-to-enable-edge-bridge-functionality)
  - [4. Run app](#4-run-app)
  - [5. `trackAction`/`trackState` implementation examples](#5-trackactiontrackstate-implementation-examples)
- [Initial validation with Assurance](#initial-validation-with-assurance)
  - [1. Set up the Assurance session](#1-set-up-the-assurance-session)
  - [2. Connect the app to the Assurance session](#2-connect-the-app-to-the-assurance-session)
  - [3. Event transactions view - check for EdgeBridge events](#3-event-transactions-view---check-for-edgebridge-events)
- [Data Prep mapping](#data-prep-mapping)
- [Final validation using Assurance](#final-validation-using-assurance)

## Overview
This hands-on tutorial provides end-to-end instructions on how to migrate from sending data to Analytics to sending data to the Edge Network using the Edge Bridge mobile extension.

```mermaid
graph LR;
    step1(1<br/>Existing Adobe Analytics app) -->
    step2(2<br/>Adobe Experience Platform<br/>Update server-side configuration) --> 
    step3(3<br/>Edge Bridge<br/>Send event data to the Edge Network & Analytics) --> 
    step4(4<br/>Assurance<br/>Verify event data formats) -->
    step5(5<br/>Data mapper<br/>Map data to XDM - Edge network data format) -->
    step6(6<br/>Assurance<br/>Verify trackAction/trackState to XDM conversion)
```

### Environment
- Android Studio version which supports Gradle plugin 7.2 and a working Android simulator.

### Prerequisites
- The tutorial app for this exercise already includes the Edge extensions. If you want to learn more about this, check out the [Edge tutorial](https://github.com/adobe/aepsdk-edge-ios/tree/main/Documentation/Tutorials).
- A timestamp enabled report suite is configured for mobile data collection.
- A tag (also known as mobile property) is configured in Data Collection UI which has Adobe Analytics extension installed and configured.

## Adobe Experience Platform setup
Before any app changes we need to set up some configuration items on the Adobe Experience Platform (AEP) side. The end goal of this section is to create a mobile property that controls the configuration settings for the various AEP extensions used in this tutorial.

### 1. Set up mobile property  
If you don't have an existing mobile property, see the [instructions on how to set up a new property](https://github.com/adobe/aepsdk-edge-ios/blob/tutorial-send-event/Documentation/Tutorials/edge-send-event-tutorial.md#1-create-a-schema).

The following AEP extension configurations should be installed:  

<details>
  <summary> Adobe Analytics </summary><p>

Open the **Catalog** and install the `Adobe Analytics` extension configuration.

<img src="../assets/edge-bridge-tutorial/aep-setup/mobile-property-analytics-catalog.png" alt="Catalog search for Adobe Experience Platform Edge Network" width="1100"/>  

In the extension configuration settings window, set the report suite ID (**1**) for each environment to the one for this tutorial. Then click `Save` (**2**)

<img src="../assets/edge-bridge-tutorial/aep-setup/mobile-property-analytics-settings.png" alt="Edge extension settings" width="1100"/>  

</p></details>

<details>
  <summary> AEP Assurance </summary><p>

Open the **Catalog** and install the `AEP Assurance` extension configuration.

<img src="../assets/edge-bridge-tutorial/aep-setup/mobile-property-assurance-catalog.png" alt="Catalog search for Adobe Experience Platform Edge Network" width="1100"/>  

</p></details>

<details>
  <summary> Adobe Experience Platform Edge Network </summary><p>

Go back to the `Catalog` and install the `Adobe Experience Platform Edge Network` extension configuration.

<img src="../assets/edge-bridge-tutorial/aep-setup/mobile-property-edge-catalog.png" alt="Catalog search for Adobe Experience Platform Edge Network" width="1100"/>  

In the extension configuration settings window, set the datastream for each environment (**1**) to the one created for this tutorial. Then click `Save` (**2**)

<img src="../assets/edge-bridge-tutorial/aep-setup/mobile-property-edge-settings.png" alt="Edge extension settings" width="1100"/>  

</p></details>

<details>
  <summary> Identity </summary><p>

Open the `Catalog` and install the `Identity` extension configuration. There are no settings for this extension.

<img src="../assets/edge-bridge-tutorial/aep-setup/mobile-property-identity-catalog.png" alt="Catalog search for Identity" width="1100"/>  

</p></details>

<details>
  <summary> Consent </summary><p>

Open the `Catalog` and install the `Consent` extension configuration.

<img src="../assets/edge-bridge-tutorial/aep-setup/mobile-property-consent-catalog.png" alt="Catalog search for Consent" width="1100"/>  

In the extension configuration settings window, the `Default Consent Level` should be set to `Yes` by default (**1**); for the tutorial app this setting is fine as-is, however when using this configuration in production apps, it should reflect the requirements of the company's actual data collection policy for the app.

<img src="../assets/edge-bridge-tutorial/aep-setup/mobile-property-consent-settings.png" alt="Consent extension settings" width="1100"/>  

</p></details>

The following cards should be visible after all the extensions are installed:

<img src="../assets/edge-bridge-tutorial/aep-setup/mobile-property-all-extensions.png" alt="All installed extensions" width="1100"/>  

### 2. Configure a Rule to forward PII events to Edge Network 
The collectPII API for Analytics does not send events to the Edge Network by default, and needs a rule to be configured in order to forward these events.

#### Create a rule <!-- omit in toc -->
1. On the Rules tab, select **Create New Rule**.
   - If your property already has rules, the button will be in the top right of the screen.
2. Give your rule an easily recognizable name (**1**) in your list of rules. In this example, the rule is named "Forward PII events to Edge Network".
3. Under the **EVENTS** section, select **Add** (**2**).

<img src="../assets/edge-bridge-tutorial/aep-setup/analytics-rule-1.png" alt="Analytics rule 1" width="1100"/>  

#### Define the event <!-- omit in toc -->

1. From the **Extension** dropdown list (**1**), select **Mobile Core**.
2. From the **Event Type** dropdown list (**2**), select **Collect PII**.
3. Select **Keep Changes** (**3**).

<img src="../assets/edge-bridge-tutorial/aep-setup/analytics-rule-2.png" alt="Analytics rule 2" width="1100"/>  

#### Define the action <!-- omit in toc -->
1. Under the Actions section, select **+ Add** (**1**).

2. From the **Extension** dropdown list (**1**), select **Adobe Analytics**.
3. From the **Action Type** dropdown list (**2**), select **Track**.
4. Name the **Action** field (**3**) "collect_pii", in the right-side window.
5. Select the **+** (**4**) next to **Context Data** and set the **Key** to "ruleKey" and **Value** to "ruleValue" (**5**).
6. Select **Keep Changes** (**6**).

<img src="../assets/edge-bridge-tutorial/aep-setup/analytics-rule-3.png" alt="Analytics rule 3" width="1100"/>  

#### Save the rule and rebuild your property <!-- omit in toc -->
1. After you complete your configuration, verify that your rule looks like the following:
2. Select **Save** (**1**).

<img src="../assets/edge-bridge-tutorial/aep-setup/analytics-rule-4.png" alt="Analytics rule 4" width="1100"/>  

## Client-side implementation
### 1. Get a copy of the files (code and tutorial app)
1. Open the code repository: https://github.com/adobe/aepsdk-edgebridge-android
2. Click **Code** in the top right
3. In the window that opens, click **Download ZIP**; by default it should land in your **Downloads** folder.
   - Optionally, move the ZIP to your **Documents** folder
4. Unzip the archived file by double clicking it.
5. Navigate inside the unarchived file, then to Documentation -> tutorials -> EdgeBridgeTutorialAppStart.
6. Right click **settings.gradle** and select **Open With -> Other....**
7. Pick **Android Studio** and click **Open**

### 2. Install Edge Bridge using dependency manager (Gradle)
The next task is to add the necessary dependencies that will enable the Edge Bridge extension to function.

1. In Android Studio, click the dropdown chevron next to **Gradle Scripts** in the left-side navigation panel to open the folder
2. Double click file **build.gradle** (Module: EdgeBridgeTutorialAppStart.app); note that there are two `build.gradle` files, the one we want is the module level one that ends with `.app`.

Inside this file, you will see a code block for this tutorial that is greyed out, because it is block commented out. It's marked by the header and footer:
`Edge Bridge Tutorial - code section (n/m)`
Where `n` is the current section and `m` is the total number of sections in the file.

To activate the code, simply add a forward slash `/` at the front of the **header**:
```java
/* Edge Bridge Tutorial - code section (1/2)
```
To:
```java
//* Edge Bridge Tutorial - code section (1/2)
```

Next, remove the Analytics dependency by removing a forward slash `/` at the front of the **header**:
```java
//* Edge Bridge Tutorial - remove section (2/2)
```
To:
```java
/* Edge Bridge Tutorial - remove section (2/2)
```

With the block uncommented, you should see a blue ribbon appear at the top of the code view window, with text like: "Gradle files have changed since last project sync. A project sync may be necessary for the IDE to work properly."

1. Click ***Sync Now***.

Gradle will use the configuration settings we just activated to install all of the Edge Bridge extensions we want for our tutorial app, and allow us to use their features within the app's code.

> **Warning**  
> After this step, there are no Adobe Experience Cloud Solution extensions in the app. At this point, the import and registration for the Identity extension can also be removed since the Identity for Edge Network extension takes care of the identity functionality for the Edge extensions.
> If your application still uses Adobe Experience Cloud Solution extensions, such as Adobe Target, Adobe Campaign, etc. (find the full list [here](https://aep-sdks.gitbook.io/docs/)) you should ignore the steps below for removing AEPIdentity and continue to use the extension.

### 3. Update tutorial app code to enable Edge Bridge functionality
There is one file that needs to be updated to enable the Edge Bridge extension:

1. Click dropdown chevrons to expand the folders: **java** -> **com.adobe.marketing.mobile** (the first one which should not be highlighted in green) -> **tutorial**.
2. Double click to open the **MainApp** file, which should be under the **tutorial** folder.
3. First update the `ENVIRONMENT_FILE_ID` value to the mobile property ID published in the first section.
   - See how to get your mobile property ID in the instructions for [getting the mobile property ID](https://github.com/adobe/aepsdk-edge-ios/blob/dev/Documentation/Tutorials/edge-send-event-tutorial.md#getting-the-mobile-property-id-).

#### Add the Edge Bridge extension <!-- omit in toc -->
Inside you will see code blocks for this tutorial that are greyed out, because they are commented out. They are marked by the header and footer `Edge Bridge Tutorial - code section n/m` (where `n` is the current section and `m` is the total number of sections in the file).

To uncomment the section and activate the code, simply add a forward slash at the front of the header:
```java
/* Edge Bridge Tutorial - code section (1/2)
```
To:
```java
//* Edge Bridge Tutorial - code section (1/2)
```
Find the next `Edge Bridge Tutorial - code section (2/2)` and uncomment by adding a forward slash to register the Edge Bridge extension with the Mobile SDK.

#### Remove Analytics and Identity <!-- omit in toc -->
Inside you will see code blocks for this tutorial marked by a header and footer `EdgeBridge Tutorial - remove section (n/m)` (where `n` is the current section and `m` is the total number of sections in the file).

Simply delete everything between the header and footer.

The first `Edge Bridge Tutorial - remove section (3/4)`, removes the import statements for Analytics and Identity while the second, `Edge Bridge Tutorial - remove section (4/4)`, removes the registration code for Analytics and Identity.

For details on the various Edge extensions used, see the [table of related projects](../../README.md#related-projects).

### 4. Run app   
In Android Studio
1. Set the app target (1) to **app** (if not already).
2. Choose which destination device (2) to run it on.
3. Click the green play button (3).

You should see your application running on the device you selected, with logs being displayed in the debug console in Android Studio.

> **Note**
> If the log console area is not shown by default, activate it by selecting:
> View -> Tool Windows -> Logcat


### 5. `trackAction`/`trackState` implementation examples   
With Edge Bridge extension successfully installed and registered, you can make the regular Analytics `trackAction` and `trackState` calls, which will be captured by Edge Bridge extension and sent to the Edge Network.

Check `ContentView.swift` for implementation examples of both APIs. You can see the data payloads that are to be sent with the calls.

## Initial validation with Assurance
Assurance is the AEP tool for inspecting all events that Adobe extensions send out, in real time. It will allow us to see the flow of events, including the EdgeBridge conversion of `trackAction`/`trackState`.

### 1. Set up the Assurance session 
To create a new Assurance session and connect to it, see the instructions on [setting up an Assurance session](https://github.com/adobe/aepsdk-edge-ios/blob/dev/Documentation/Tutorials/edge-send-event-tutorial.md#1-set-up-the-assurance-session), using the base URL value:
```
edgebridgetutorialapp://main
```

### 2. Connect the app to the Assurance session  
To connect to Assurance, we will use the session link method:

1. Copy the session link; you can click the icon of a double overlapping box to the right of the link to copy it.
  - If using a physical device, it may be helpful to have a way to send this link to the device (ex: email, text, etc.). Alternatively, you can use the camera on your physical device to scan the QR code.
2. Open the tutorial app and tap the 3 dot menu (**1**) in the top right. Select **Connect to Assurance**.

<img src="../assets/edge-bridge-tutorial/assurance-validation/android-app-assurance-menu.png" alt="Assurance menu Android" width="400"/>

3. Paste the Assurance session link copied from step 1 into the text field and tap the Connect button.

<img src="../assets/edge-bridge-tutorial/assurance-validation/android-app-assurance-connection.png" alt="Assurance connection Android" width="400"/>

4. App should open and show the Assurance PIN screen to authenticate the session connection; enter the PIN from the session details and tap **Connect (1)**.

<img src="../assets/edge-bridge-tutorial/assurance-validation/android-app-assurance-pin.png" alt="Android Assurance PIN" width="400"/>


### 3. Event transactions view - check for EdgeBridge events  
#### `trackAction`/`trackState` events <!-- omit in toc -->
In order to see EdgeBridge events, in the connected app instance:
1. Trigger a `trackAction` and/or `trackState` within the app which the Edge Bridge extension will convert into Edge events. This event will be captured by the Assurance extension and shown in the web session viewer.

<img src="../assets/edge-bridge-tutorial/assurance-validation/android-app-track-buttons.png" alt="Simulator track buttons" width="400"/>

2. Click the `AnalyticsTrack` event (**1**) in the events table to see the event details in the right side window
3. Click the `RAW EVENT` dropdown (**2**) in the event details window to see the event data payload.
4. Verify that the `contextdata` matches what was sent by the Analytics `trackAction`/`trackState` API.

<img src="../assets/edge-bridge-tutorial/assurance-validation/assurance-analytics-track-event.png" alt="Assurance Analytics track event" width="800"/>

5. Now click the `Edge Bridge Request` event (**1**) in the events table
6. Click the `RAW EVENT` dropdown (**2**) in the event details window; notice the slight differences in the payload structure as a result of the `Edge Bridge Request` event conforming to the format of an Edge event.

<img src="../assets/edge-bridge-tutorial/assurance-validation/assurance-edge-bridge-track-event.jpg" alt="Assurance Edge Bridge track event" width="800"/>

Notice the differences in event data structure and format between the two types of events: Analytics (left) vs Edge (right) via Edge Bridge extension
The top level EventType is converted from a `generic.track` to `edge` (that is, Analytics generic track event -> Edge event) (**1**). The Edge Bridge extension also populates the standard XDM field for event type (`eventType`) in the event data payload. Also notice that the `contextdata` has moved from directly under `EventData` to under the generic Edge XDM `data` property (**2**).

<img src="../assets/edge-bridge-tutorial/assurance-validation/analytics-edge-bridge-conversion.jpg" alt="Comparison of event data between analytics and edge bridge events" width="1100"/>

> **Note**
> The two new top level properties `xdm` and `data` are standard Edge event properties that are part of the Edge platform's XDM schema-based system for event data organization that enables powerful, customizable data processing. However, because the `contextdata` is not yet mapped to an XDM schema, it is not in a usable form for the Edge platform. We will solve this issue by mapping the event data to an XDM schema in the next section.

#### Trigger rule-based `trackAction` events <!-- omit in toc -->
Rules-based trackAction/trackState events are also converted to Edge events by the Edge Bridge extension. Select the **Trigger Rule** button (**1**) to trigger a rule that creates a trackAction event.

<img src="../assets/edge-bridge-tutorial/assurance-validation/android-app-trigger-rule-button.png" alt="Simulator trigger rule button" width="400"/>

Just like the `trackAction`/`trackState` events above, the Edge Bridge extension will convert the PII trackAction event into an Edge event.

## Data Prep mapping

<details>
  <summary> Data Prep background</summary><p>

Data Prep is an Adobe Experience Platform service which maps and transforms data to the [Experience Data Model (XDM)](https://experienceleague.adobe.com/docs/experience-platform/xdm/home.html).  Data Prep is configured from a Platform enabled [datastream](https://experienceleague.adobe.com/docs/experience-platform/edge/datastreams/overview.html) to map source data from the Edge Bridge mobile extension to the Platform Edge Network.

This guide covers how to map data sent from the Edge Bridge within the Data Collection UI.

For a quick overview of the capabilities of Data Prep, watch the following [video](https://experienceleague.adobe.com/docs/platform-learn/data-collection/edge-network/data-prep.html).

> **Note**
> The following documentation provides a comprehensive overview of the Data Prep capabilities:
> - [Data Prep overview](https://experienceleague.adobe.com/docs/experience-platform/data-prep/home.html)
> - [Data Prep mapping functions](https://experienceleague.adobe.com/docs/experience-platform/data-prep/functions.html)
> - [Handling data formats with Data Prep](https://experienceleague.adobe.com/docs/experience-platform/data-prep/data-handling.html)
>

</p></details>

To open the data prep mapper: 
1. Select **Datastreams** (**1**) in the left-side navigation panel.
2. Select your datastream (**2**).

<img src="../assets/edge-bridge-tutorial/data-prep/datastreams-mapper-nav-1.png" alt="Datastream mapper navigation" width="1100"/>  

3. Select **Edit Mapping** (**1**) in the right-side navigation panel.  

<img src="../assets/edge-bridge-tutorial/data-prep/datastreams-mapper-nav-2.png" alt="Datastream mapper navigation 2" width="1100"/>

Currently, the data mapper UI only allows for one JSON payload to be mapped per datastream. This means for a given datastream, all of the potential event payloads need to be merged so that they can be mapped at once. Note that Data Prep requires that the **data** and **xdm** objects are top-level objects in the provided JSON source.

The properties from both `trackAction` and `trackState` events from the tutorial app need to be combined into a single JSON. For simplicity, the merged data structure has been provided below:

```json
{
  "xdm": {
    "eventType": "analytics.track",
    "timestamp": "2022-08-19T20:55:12.320Z"
  },
  "data": {
    "contextdata": {
      "product.add.event": "1",
      "product.view.event": "1",
      "product.id": "12345",
      "product.name": "wide_brim_sunhat",
      "product.units": "1"
    },
    "action": "add_to_cart",
    "state": "hats/sunhat/wide_brim_sunhat_id12345"
  }
}

```

1. Copy and paste the JSON data into the datastreams JSON input box (**1**).
2. Verify the uploaded JSON matches what is displayed in the `Preview sample data` section (**2**) and click `Next` (**3**).

<details>
  <summary> Getting the JSON data from Assurance </summary><p>

1. Navigate back to your Assurance session for the Edge Bridge app and select the `Edge Bridge Request` event (**1**)
2. Open the `RAW EVENT` dropdown and click and drag to select the `ACPExtensionEventData` value as shown, then copy the selected value (right click the highlighted selection and choose `Copy`, or use the copy keyboard shortcut `CMD + C`)  

<img src="../assets/edge-bridge-tutorial/data-prep/assurance-edgebridge-mapping-data.png" alt="Assurance Edge Bridge mapping data" width="1100"/>  

> **Note**
> To merge events, you would look for properties under `data` and `contextdata` that are unique between events and include them in the final data payload.

</p></details>

<img src="../assets/edge-bridge-tutorial/data-prep/datastreams-json-paste.png" alt="Datastream JSON paste" width="1100"/>  

> **Note**
> XDM source fields are automatically mapped if the same field appears in the target schema. For example, the fields _xdm.\_id_ and _xdm.timestamp_ are required fields in a time-series XDM schema so you will notice they are automatically mapped from the source data to the target schema and do not require a mapping entry.

> **Note**
> The Edge Bridge extension automatically sets an _xdm.eventType_ value of _analytics.track_. However, the value may be changed by adding a new mapping row in Data Prep by setting the **Target Field** to "eventType".

3. Click the `Add new mapping` button (**1**).

<img src="../assets/edge-bridge-tutorial/data-prep/datastreams-start-mapping.png" alt="Datastream start mapping" width="1100"/>  

4. A new entry for mapping will appear in the window; click the arrow button (**1**) next to the field `Select source field`.

<img src="../assets/edge-bridge-tutorial/data-prep/datastreams-mapping-json.png" alt="Datastream mapping" width="1100"/>  

5. In the JSON property viewer window, click the dropdown arrows next to `data` (**1**) and `contextdata` (**2**).
6. Then select the first property to map, `product.add.event` (**3**) and click `Select` (**4**).

<img src="../assets/edge-bridge-tutorial/data-prep/datastreams-mapping-json-property.png" alt="Datastream select property" width="1100"/>  

Notice that in the property viewer, you can see the data hierarchy, where `data` is at the top, `contextdata` is one level down, and `product.add.event` is one level below that. This is nested data, which is a way to organize data in the JSON format.

> **Info**
> The data mapper interprets the `.` character as nesting, which means if there are `.` characters in a property name that are not meant to be nesting, namely the ones in our current example: `product.add.event`, we need to escape this behavior by adding backslashes `\` before the `.` (**1**).

7. Add backslashes `\` before the `.` characters as shown below (**1**).

Now, we need to map this JSON property from the Edge Bridge event to its matching property in the XDM schema.

8. Click the schema icon (**2**) to open the XDM property viewer window.

<img src="../assets/edge-bridge-tutorial/data-prep/datastreams-mapping-xdm.png" alt="Datastream mapping XDM" width="1100"/>  

9. In the XDM property viewer window, click the dropdown arrows next to `commerce` (**1**) and `productListAdds` (**2**).
10. Then select the `value` property (**3**) and click `Select` (**4**).

<img src="../assets/edge-bridge-tutorial/data-prep/datastreams-mapping-xdm-property.png" alt="Datastream mapping XDM property" width="1100"/>  

11. Repeat this process, adding new mappings for all of the other properties on the JSON data side (except for the `timestamp` property which is handled automatically by Edge), finalizing the mappings like this:

| JSON Property                           | XDM Property                       | trackAction        | trackState         |
| --------------------------------------- | ---------------------------------- | ------------------ | ------------------ |
| data.contextdata.product\\.add\\.event  | commerce.productListAdds.value     | :white_check_mark: |                    |
| data.contextdata.product\\.view\\.event | commerce.productListViews.value    |                    | :white_check_mark: |
| data.contextdata.product\\.id           | productListItems.SKU               | :white_check_mark: | :white_check_mark: |
| data.contextdata.product\\.name         | productListItems.name              | :white_check_mark: | :white_check_mark: |
| data.contextdata.product\\.units        | productListItems.quantity          | :white_check_mark: |                    |

12. After completing all the mappings, click **Save**.

## Final validation using Assurance
Now that the mapping is set up in the datastream, we have the full pathway of data:

```mermaid
graph LR;
    step1(App<br/>Analytics trackAction/trackState) --> step2(App<br/>Edge Bridge conversion to Edge event) --> step3(Edge Network<br/>Datastream translation of payload from contextdata to Edge XDM) --> step4(Edge Network<br/>Routing XDM data using datastream to Analytics);
```

By using the Event Transactions view in the left-side navigation panel, the logical flow of events from `trackAction` event -> data mapping -> Analytics can be seen.
<img src="../assets/edge-bridge-tutorial/assurance-validation/assurance-event-transactions.png" alt="Assurance Event Transactions" width="1100"/>  

Back in the Events view, the **mapper:xdm-event** (**1**) shows the result of the mapping from the Edge Bridge event's generic **data** -> **contextdata** structure into the XDM schema format (**2**).
<img src="../assets/edge-bridge-tutorial/assurance-validation/assurance-mapper-event.png" alt="Assurance mapper event" width="1100"/>  

