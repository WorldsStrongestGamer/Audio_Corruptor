# Audio-Corruptor
Really poorly written program that corrupts mp3 files! Thought I would upload this because I guess sometime somewhere somebody MIGHT have a use for a program like this. Abides by no proper coding conventions and comments were an afterthought, good luck!

How to run it:
Unlike every other stupid git hub project that tells you to type 15+ things into a command line interface but NOT what command line interface/interfaces to use (please be more cortiuous guys, some of us are not as smart). Just open my projects lib folder and run the jar file titled “AudioCorruptor”


Hi, here are the instructions on how to use please be gentle with the code it has been tested like once ever:  

  1. Choose an input file location (must be mp3)asdf 
  2. Choose an output file that already exists or go to a folder you want to place the corrupted mp3 in, write the name of the file you want to create, and then click open 
     (I was too lazy to just rename the button to create or save so just trust me)
  3. Choose to corrupt the entire file or selected segments of the file
  4. Enter in all the settings you want. Just note a couple of things:

        a) Never put zero into the add bytes thingy it gets REALLY angry

        b) When you enter the start times make sure that you are typing in SECONDS and that you don't put a start time that conflicts with another start time
	   (if segment 3 is from 10 seconds to 20 seconds make DAMN sure that segment 2 is ends BEFORE 10 seconds)

        c) Make sure your segments are in order of start and end time. (example: segment 3 should not start at 0 seconds, segment one should) 

        d) When you click fade corruption you are saying I want to have a fade in and out. The times you put in (in seconds) should be the time it takes to fade in
	   and out (example 5 seconds to fully fade in then 5 seconds to fully fade out)
  5. Click update to save the changes you made
  6. AND BOOM click run, and it might work if you are lucky. Make sure to give it a bit of time to render

Most important points restated:
Please do not set add bytes to zero and please make sure segments are in order of start and end times and DO NOT OVERLAP

Creator: James Cocja

Published: June 2020


