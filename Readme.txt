
Source Code 
The application is split into several projects to allow for easy testing and re-use of the components. To build the EATreasureHunt App, you need: 

Android3DEmys – This project implements an OpenGl rendered model of the EMYS head and was produced during the LIREC project.
HwuDialogSystem – This is the lightweight framework to handle dialogue scripts written in interpreted Java using BeanShell. 
XMLParser – This project reads the routes and feedbacks scripts, then translates them into Route and FeedbackTemplate objects.
EATreasureHunt – This is the EATreasureHunt App which uses all the above projects. 

Note that the code all builds with no errors, or warnings. If you see some, check them out!

Setting up the build paths 
This should be done for you if you’re using copies of the entire project. To import the entire project, 
•	Go to import -> General -> Existing Projects into Workspace. 
•	Click Next and browse to the existing (this) EATreasureHunt folder.
•	All 4 projects: Android3DEmys, HWUDialogSystem, XMLParser and EATreasureHunt should appear. 
•	Check all of them and check Copy projects into workspace. 
•	Then click Finish. The projects should be imported now with the appropriate paths.
•	To run the application, right click on the EATreasureHunt project -> Run As -> Android Application
In case you are not using the entire project, here’s how things should be setup:
Android3DEmys 
Right click the project -> Properties -> Android -> Library, “is Library” should be checked.
HwuDialogSystem 
Right click the project -> Properties -> Java Build Path -> Libraries -> Add External Jar -> add bsh-core-2.0b4.jar
XMLParser 
Right click the project -> Properties -> Android -> Library, “is Library” should be checked.
EATreasureHunt 
Right click the project -> Android -> Library, make sure that Android3DEmys and XMLParser are listed. If not just click on Add -> select the library and press OK. Then in the Java Build Path -> Projects, make sure the HwuDialogSystem is listed, else, add the project. Also under Libraries, make sure that bsh-core-2.0b4.jar is added. In Order and Export, make sure that they are both checked.

The app is available on EATreasureHunt\bin\EATreasureHunt.apk



