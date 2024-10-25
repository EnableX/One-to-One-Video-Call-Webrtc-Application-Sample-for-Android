# 1- to-1 RTC: Android App with EnableX Android Toolkit

Explore a Sample Android App Demonstrating EnableX Platform Video APIs and Android Toolkit for 1-to-1 Real-Time Communication (RTC)

Uncover the power of the EnableX platform's Video APIs (https://developer.enablex.io/docs/references/apis/video-api/index/) and Android Toolkit (https://developer.enablex.io/docs/references/sdks/video-sdk/android-sdk/index/) through this illustrative Android application. This app equips developers with the tools to fast-track their app development journey, allowing them to host the application on their own Android devices.

With this app, you can effortlessly create virtual rooms on-the-fly within the EnableX platform using REST calls. It harnesses Room credentials, such as Room ID, to seamlessly connect as a mobile client to the virtual Room. These Room credentials can also be conveniently shared with others, enabling collaborative real-time communication (RTC) sessions in the same virtual Room.


## 1. How to get started

### 1.1 Pre-Requisites

#### 1.1.1 App Id and App Key 

* Register with EnableX [https://www.enablex.io/free-trial/] 
* Login to the EnableX Portal
* Create your Application Key
* Get your App ID and App Key delivered to your email


#### 1.1.2 Sample Android Client 

* Clone or download this repository [https://github.com/EnableX/One-to-One-Video-Call-Webrtc-Application-Sample-for-Android.git] 


#### 1.1.3 Test Application Server

You need to set up an Application Server to provision Web Service API for your Android Application to communicate enabling Video Session. 

To help you try our Android Application quickly, without having to set up Application Server, this Application is shipped pre-configured to work in a "try" mode with EnableX hosted Application Server i.e. https://demo.enablex.io. 

Our Application Server restricts a single Session Durations to 10 minutes and allows 1 moderator and not more than 3 participants in a Session.

Once you tried EnableX Android Sample Application, you may need to set up your own  Application Server and verify your Application to work with your Application Server. Refer to point 2 for more details on this.


#### 1.1.4 Configure Android Client 

* Open the App
* Go to WebConstants and change the following:
``` 
 /* To try the App with Enablex Hosted Service you need to set the kTry = true When you setup your own Application Service, set kTry = false */
     
     public  static  final  boolean kTry = true;
     
 /* Your Web Service Host URL. Keet the defined host when kTry = true */
 
     String kBaseURL = "https://demo.enablex.io/"
     
 /* Your Application Credential required to try with EnableX Hosted Service
     When you setup your own Application Service, remove these */
     
     String kAppId = ""  
     String kAppkey = ""  
 ```


### 1.2 Test

#### 1.2.1 Open the App

* Open the App in your Device. You get a form to enter the credentials i.e. Name & Room Id.
* You need to create a Room by clicking the "Create Room" button.
* Once the Room Id is created, you can use it and share with others to connect to the Virtual Room to carry out an RTC Session either as a Moderator or a Participant (Choose applicable Role in the Form).

Note: Only one user with Moderator Role allowed to connect to a Virtual Room while trying with EnableX Hosted Service. Your Own Application Server may allow up to 5 Moderators.
  
 Note:- In case of emulator/simulator your local stream will not create. It will create only on real device. 
  
## 2. Set up Your Own Application Server

You may need to set up your own Application Server after you tried the Sample Application with EnableX hosted Server. We have different variants of Application Server Sample Code. Pick the one in your preferred language and follow instructions given in respective README.md file.

* NodeJS: [https://github.com/EnableX/Video-Conferencing-Open-Source-Web-Application-Sample.git]
* PHP: [https://github.com/EnableX/Group-Video-Call-Conferencing-Sample-Application-in-PHP]

Note the following:
* You need to use App ID and App Key to run this Service.
* Your Android Client End Point needs to connect to this Service to create Virtual Room and Create Token to join the session.
* Application Server is created using EnableX Server API, a Rest API Service helps in provisioning, session access and post-session reporting.  

To know more about Server API, go to:
https://developer.enablex.io/docs/guides/video-guide/sample-codes/video-calling-app/#demo-application-server


## 3. Android Toolkit

This Sample Applcation uses EnableX Android Toolkit to communicate with EnableX Servers to initiate and manage Real Time Communications. Please update your Application with latest version of EnableX Android Toolkit as and when a new release is avaialble.   

* Documentation: https://developer.enablex.io/docs/references/sdks/video-sdk/android-sdk/index/
* Download Toolkit: https://developer.enablex.io/docs/references/sdks/video-sdk/android-sdk/index/

## 4. Demo

EnableX provides hosted Demo Application Server of different use-case for you to try out.

1. Try a quick Video Call: https://demo.enablex.io/
2. Sign up for a free trial https://www.enablex.io/free-trial/
