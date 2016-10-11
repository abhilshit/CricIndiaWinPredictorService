# CricIndiaWinPredictorService

A Spring Boot Project that wraps the Cricket India Match Win Predictor as a REST Web Service. 

This project is created as part of my Techgig Webinar on Predictive Analytics with R and Java.
 
 http://www.techgig.com/webinar/Predictive-Analytics-using-R-and-Java-964
 
 It Predicts whether India will win a Cricket Match depending on scores stats of Virat Kohli. 
 It uses Neural Network based prediction model which can be found under the ./model directory.

 To execute this project follow the below given steps
 
  1. git clone the project
  2. gradle clean build
  3. The above command should generate cricindia-win-predictor-0.0.1-SNAPSHOT.jar under build/libs directory
  4. execute this jar using java -jar cricindia-win-predictor-0.0.1-SNAPSHOT.jar
  
  
 These PMML Models under /model directory were exported from R. 
  
 Cricket statistics data was obtained in YAML format from http://cricsheet.org.
  