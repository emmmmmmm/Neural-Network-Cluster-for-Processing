NetworkCluster NC;

void setup(){
  frameRate(100000);
  size(1200,600);
  NC = new NetworkCluster();
  NC.setLearningRate(1e-3);
//  buildData();
  importGestures();
//  importData() ;
  NC.setUpNetworkCluster(trainingInput,trainingTarget);
  println(NC.trainingData.length+ " / "+NC.trainingData[0].length+" / "+NC.trainingData[0][0].length+" ("+NC.trainingTarget[0][0].length+")");

}
void draw(){
  float err = NC.learn();
  frame.setTitle((int)frameRate+" cps / error: "+err);

  if(frameCount%10!=0)return;
  float[][] results;




  background(255);
  for(int i=0;i<NC.cluster.length;i++){
    NC.cluster[i].displayError();
  }
  stroke(0);
  line(0,height/4,width,height/4);
  line(0,height/4*3,width,height/4*3);
  fill(255,150);
  noStroke();
  rect(0,0,width,height);

  pushMatrix();
  strokeCap(RECT);
  strokeWeight(2);

  int testIndex =0;
  testIndex = round( map(mouseX,0,width,0,evalInput.length-1));
  results = NC.forward(evalInput[testIndex]);
  fill(0);
  stroke(0);
  text("currentIndex: "+testIndex, 20,20);
  //drawInputs:
  translate(50,height/2);
  stroke(0,0,255);
  for(int i=0;i<evalInput[testIndex].length;i++){
    line(0, 0, 0, map(evalInput[testIndex][i], 2,-2,-height/2,height/2));
    translate(2,0);
  }

  translate(5,0);
  // draw targets
  stroke(0,255,0);
  for(int i=0;i<evalTarget[testIndex].length;i++){
    line(0, 0, 0, map(evalTarget[testIndex][i], 2,-2,-height/2,height/2));
    translate(2,0);
  }
  // draw Outputs;

  // draw Outputs;
  translate(20,0);

  stroke(0);

  for(int i=0;i<results.length;i++){
    translate(5,0);
    for(int j=0;j<results[0].length;j++){
      translate(2,0);
      line(0,0,0,map(results[i][j],2,-2,-height/2,height/2));

    }
  }
  popMatrix();
}
void keyPressed(){
  if(key=='+')
  NC.setLearningRate(NC.learningRate*10);
  if(key == '-')
  NC.setLearningRate(NC.learningRate/10);

  println("learningRate: "+NC.learningRate);

}
