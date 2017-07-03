import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class sketch_170622a_a_NN_Cluster_class extends PApplet {

NetworkCluster NC;

public void setup(){
  frameRate(100000);
  size(1200,600);
  NC = new NetworkCluster();
  NC.setLearningRate(1e-3f);
//  buildData();
  importGestures();
//  importData() ;
  NC.setUpNetworkCluster(trainingInput,trainingTarget);
  println(NC.trainingData.length+ " / "+NC.trainingData[0].length+" / "+NC.trainingData[0][0].length+" ("+NC.trainingTarget[0][0].length+")");

}
public void draw(){
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
public void keyPressed(){
  if(key=='+')
  NC.setLearningRate(NC.learningRate*10);
  if(key == '-')
  NC.setLearningRate(NC.learningRate/10);

  println("learningRate: "+NC.learningRate);

}
class SimpleCell {
  float[] Wx;
  float Bx;
  float Sy;
  float[] Sx;
  float[] dWx;
  float dBx;
  float[] dSx;
  //------------------------------
  SimpleCell(int in) {
    Wx= new float[in];
    dWx = new float[in];
    Sx = new float[in];
    dSx = new float[in];
    for (int i=0; i<Wx.length; i++)
      Wx[i] = random(0.5f,0.99f)*random(100)>50?-1:1;
  }
  //------------------------------
  public float forward(float[] in) {

    Sx = in;
    Sy = 0;
    for (int i=0; i<in.length; i++)
      Sy+=(Wx[i])*in[i];
    Sy+=Bx;
    Sy= softSign(Sy);
    return Sy;
  }
  public void resetStates(){}

  //------------------------------
  public float[] backward(float dSy) {
    dSy =deSoftSign(Sy)*dSy; // softsign
    dBx+=dSy;
    dSx = new float[dSx.length];
    for (int i=0; i<Wx.length; i++) {
      dWx[i] += Sx[i]*dSy;
      dSx[i] += Wx[i]*dSy;
    }
    return dSx;
  }
  //------------------------------
  public void update(float lr) {
    for (int i=0; i<Wx.length; i++) {
      Wx[i]+=dWx[i]*lr;
    }
    Bx+=dBx*lr;
    dWx=new float[dWx.length];
    dBx=0;
  }
  //------------------------------
  public float softSign(float x) {
    return 2.0f * x / (1.0f + abs(x));
  }
  //------------------------------
  public float deSoftSign(float x){
    return  (2.0f / sq(1.0f+abs(x)));
  }
}
class NetworkCluster {
  Network[] cluster;
  Network outputNetwork;
  float[][][] trainingData;
  float[][][] trainingTarget;
  public float minScore = 00; // percent
  float learningRate = 1e-3f;
  NetworkCluster() {
    cluster = new Network[0];
    trainingData = new float[0][0][0];
    trainingTarget = new float[0][0][0];

    //  outputNetwork = new Netork();
  }
  //--------------------------------
  // add a new network to the cluster-AR, args: float ar with inputs
  // all clusters need the same amount of inputs, because every cluster will get the same input!
  // think about bulding an input-cluster aswell, to downsample the inputs? (maybe pre-train with an hourglass?)
  private void addCluster(int[] layers) {

    cluster = (Network[]) expand(cluster, cluster.length+1);
    cluster[cluster.length-1] = new Network();
    for (int i=0; i<layers.length-1; i++) {
      cluster[cluster.length-1].addLayer(layers[i], layers[i+1]);
    }
    cluster[cluster.length-1].batchSize = 10;
    //    println(cluster[cluster.length-1].layers[0].cells.length);
  }
  //--------------------------------
  // after all clusters are added, attach an outputNetwork to the network!
  // single layer... does that suffice? mh deeplearning capabilities can be added later on!
  private void addOutputLayer(int numOutputs) {
    int numInputs=0;
    for (int i=0; i<cluster.length; i++)
    numInputs += cluster[i].layers[cluster[i].layers.length-1].cells.length;
    outputNetwork = new Network();
    outputNetwork.addLayer(numInputs, numOutputs);
  }
  //--------------------------------
  // function to prepare the network!
  // inputs: trainingData, trainingTargets
  // does:
  // find similar patterns in input data
  // split them into similar-data-sets
  // create clusters for every datasets
  // create an output layers
  // done.

  // something is still wrong here... :/
  public void setUpNetworkCluster(float[][] inputData, float[][] outputData){
    float[]tmpOutput = new float[inputData[0].length];
    initAr(tmpOutput,-1);
    boolean[] sorted = new boolean[inputData.length];
    initAr(sorted,false); // list to take track of already sorted elements
    // test if it is a new sample:
    for(int i=0;i<inputData.length;i++){

      // if it's a new sample, create a new entry!
      if(!sorted[i]){
        trainingData = (float[][][]) expand(trainingData, trainingData.length+1);
        trainingData[trainingData.length-1] = new float[1][0];
        trainingData[trainingData.length-1][0] = inputData[i];

        trainingTarget = (float[][][]) expand(trainingTarget,trainingTarget.length+1);
        trainingTarget[trainingTarget.length-1] = new float[1][0];
        trainingTarget[trainingTarget.length-1][0] = outputData[i];
        sorted[i]=true;


        // add data to current sample:

        for(int j=0;j<inputData.length;j++){
          if(i!=j){
            trainingData[trainingData.length-1] = (float[][]) expand(trainingData[trainingData.length-1],trainingData[trainingData.length-1].length+1);
            trainingData[trainingData.length-1][trainingData[trainingData.length-1].length-1] = inputData[j];

            trainingTarget[trainingTarget.length-1] = (float[][]) expand(trainingTarget[trainingTarget.length-1],trainingTarget[trainingTarget.length-1].length+1);
            trainingTarget[trainingTarget.length-1][trainingTarget[trainingTarget.length-1].length-1] = tmpOutput; //outputData[j];

            if(!sorted[j] && isSimilar(inputData[i],inputData[j])){
              trainingTarget[trainingTarget.length-1][trainingTarget[trainingTarget.length-1].length-1] = outputData[j];
              sorted[j]=true;
            }
            else{
              // remove if not similar:
              if(false){
                trainingData[trainingData.length-1] = (float[][]) shorten(trainingData[trainingData.length-1]);
                trainingTarget[trainingTarget.length-1] = (float[][])shorten(trainingTarget[trainingTarget.length-1]);
              }
            }
          }
        }
      }
    }
    // add cluster for each trainingData-Set
    for(int i=0;i<trainingData.length;i++){
      int[] layers = {
        trainingData[i][0].length,
        130,
        trainingTarget[i][0].length
        }; //  trainingTarget[i][].length //!?
        addCluster(layers);
      }
      addOutputLayer(trainingTarget[0][0].length);
    }
    //--------------------------------
    public void initAr(float[]ar, int val){
      for(int i=0;i<ar.length;i++)ar[i]=val;
    }
    public void initAr(boolean[]ar, boolean val){
      for(int i=0;i<ar.length;i++)ar[i]=val;
    }
    //--------------------------------
    public boolean isSimilar(float[] p1, float[] p2){
      float sim = 0;
      float thisSim = 0;
      for(int i=0;i<p1.length;i++){
        thisSim = 100.0f - abs(percentChange(p1[i], p2[i]));
        sim+=thisSim;

      }
      sim/=p1.length;
      //  println(sim/p1.length);
      if(sim < minScore)  return true;
      else                return false;
    }
    //--------------------------------
    //--------------------------------
    public float percentChange(float a, float b) {
      float aa,bb;
      aa = a<b? a:b;
      bb = a<b? b:a;
      if (aa==bb) return 00;
      else if (aa==0) return 100;
      else     return 100.0f* (bb-aa)/abs(aa);
    }

    //--------------------------------
    // now that's where things get tricky...^^
    // maybe it'd be good if i'd be able to train every cluster individually?
    // and only when all clusters are trained train the outputCluster?

    // prepare datasets for each Network in cluster
    // train individual networks
    // train output network

    public float learn(){
      float error = 0;
      for(int i=0;i<cluster.length;i++){
        error+=cluster[i].learn(trainingData[i],trainingTarget[i]); // umm, i nee to keep the trainingTagets aswell...!!!!
      }
      //  error+= outputLayer.learn(/*outputs from cluster*/,trainingTarget[i]);
      // if there's an outputlayer: go through that aswell!


      // for every cluster:
      // forward
      // get error
      // backward
      // update
      // ??
      // profit xD

      return error/=(cluster.length);
    }
    //--------------------------------
    // push input through all parallel clusters and see what happenes^^
    public float[][] forward(float[] input) {
      float[][] ret = new float[cluster.length][0];
      for(int i=0;i<cluster.length;i++){
        ret[i]=cluster[i].forward(input); // umm, i nee to keep the trainingTagets aswell...!!!!
      }
      return ret;
    }
    //--------------------------------
    public void setLearningRate(float lr){
      learningRate = lr;
      for(int i=0;i<cluster.length;i++)
      cluster[i].setLearningRate(lr);
      //outputLayer.learningRate = lr;
    }
    //--------------------------------
  }
float trainingInput[][], trainingTarget[][], evalInput[][], evalTarget[][];

String[] vocab;
public void importGestures(){
  String lines[] = loadStrings("pointClouds.txt");
  lines = sort(lines);
  String[][] entries = new String[lines.length][0];
  String[] labels = new String[lines.length];
  for (int i=0; i<lines.length; i++) {
    entries[i] = split(lines[i], ",");
    labels[i] = entries[i][0].toUpperCase();
    entries[i] = subset(entries[i],1);
  }
  // remove stroke-index from input-data...!
  for(int i=0;i<entries.length;i++){
    for(int j=3;j<entries[i].length;j+=2){
      // arrayCopy(src, srcPosition, dst, dstPosition, length)
      arrayCopy(entries[i],j,entries[i],j-1,entries[i].length-j);
      entries[i] = (String[]) shorten(entries[i]);

    }
    entries[i]=shorten(entries[i]);
  }

  buildVocab(labels);
  trainingInput = new float[entries.length][0];
  trainingTarget = new float[entries.length][vocab.length];
  for (int i=0; i<entries.length; i++) {
    initAr(trainingTarget[i],0);
    trainingTarget[i][getFromVocab(labels[i])] = 1;
    trainingInput[i] = stringToFloat(entries[i]);
  }
  //-------
  // eval-dataset:

  lines = loadStrings("pointClouds_eval.txt");
  lines = sort(lines);
  entries = new String[lines.length][0];
  labels = new String[lines.length];
  for (int i=0; i<lines.length; i++) {
    entries[i] = split(lines[i], ",");
    labels[i] = entries[i][0].toUpperCase();
    entries[i] = subset(entries[i],1);

  }

  // remove stroke-index from input-data...!
  for(int i=0;i<entries.length;i++){
    for(int j=3;j<entries[i].length;j+=2){
      // arrayCopy(src, srcPosition, dst, dstPosition, length)
      arrayCopy(entries[i],j,entries[i],j-1,entries[i].length-j);
      entries[i] = (String[]) shorten(entries[i]);

    }
    entries[i]=shorten(entries[i]);
  }


  evalInput = new float[entries.length][0];
  evalTarget = new float[entries.length][vocab.length];
  for (int i=0; i<entries.length; i++) {
    initAr(evalTarget[i],0);
    evalTarget[i][getFromVocab(labels[i])] = 1;
    evalInput[i] = stringToFloat(entries[i]);
  }

}
//------------------------------------------
public float[] stringToFloat(String[] ar){
  float[] ret = new float[ar.length];
  for(int i=0;i<ret.length;i++){
    ret[i] = Float.parseFloat(ar[i])*2;

  }
  return ret;
}
//------------------------------------------
public void buildVocab(String[] labels) {
  vocab = new String[0];
  labels = sort(labels);
  for (int i=0; i<labels.length; i++) {
    if (getFromVocab(labels[i])==-1)
    vocab = append(vocab, labels[i]);
  }
  print("vocabulary: ");
  println(vocab);
}

//------------------------------------------
public int getFromVocab(String input) {
  for (int i=0; i<vocab.length; i++)
  if (vocab[i].equals(input)) return i;
  return -1;
}
//------------------------------------------
public String getFromVocab(int input){
  return vocab[input];
}
public String getFromVocab(float[] input){
  for(int i=0;i<input.length;i++)
  if(input[i]==1)return vocab[i];
  return "";
}


//------------------------------------------
public void buildData() {
  int numSamples = 60;
  int numInputs = 3;
  int numOutputs = 2;
  float[][] inputData,targetData;
  inputData = new float[numSamples][numInputs];
  targetData = new float[numSamples][numOutputs];

  for (int i=0; i<inputData.length; i++) {
    for (int j=0; j<inputData[i].length; j++) {
      inputData[i][j] = round(random(10))/10.0f;
    }
  }
  for (int i=0; i<targetData.length; i++) {
    for(int j=0;j<targetData[i].length;j++){
      targetData[i][j] = round(random(1))/1.0f; //0.01f+(random(1)); //cos(i*.2);
    }
  }


  trainingInput = inputData;
  trainingTarget = targetData;
  /*
  for (int i=0; i<inputData.length; i++) {
  for (int j=0; j<inputData[i].length; j++) {
  inputData[i][j] = 0.01f+(random(1)); //sin(i*.1); // inputData[i-1][j]+random(-0.2, 0.2);
}
}
for (int i=0; i<targetData.length; i++) {
for(int j=0;j<targetData[i].length;j++){
targetData[i][j] = 1; //(random(1));//cos(i*.2);
}
}
*/
evalInput = inputData;
evalTarget = targetData;

//  evalInput = trainingInput;
//  evalTarget = trainingTarget;

/*
// XOR
inputData[0][0] = 0;
inputData[0][1] = 0;
inputData[1][0] = 0;
inputData[1][1] = 1;
inputData[2][0] = 1;
inputData[2][1] = 0;
inputData[3][0] = 1;
inputData[3][1] = 1;
targetData[0][0] = 1;
targetData[1][0] = 0;
targetData[2][0] = 0;
targetData[3][0] = 1;
*/
}
public void initAr(float[]ar, int val){
  for(int i=0;i<ar.length;i++)ar[i]=val;
}
public void initAr(boolean[]ar, boolean val){
  for(int i=0;i<ar.length;i++)ar[i]=val;
}
//-----------------------------
// simple fully connected layer
class Layer {
  //BaseCell[] cells;
  SimpleCell[] cells;
  float[] Sy;
  float[] dSx;
  //------------------------------
  Layer(int in, int out) {
    Sy = new float[out];
    dSx = new float[in];
    cells = new SimpleCell[out];

    for (int i=0; i<out; i++) {
      cells[i] = new SimpleCell(in);
    }
  }
  //------------------------------
  public void forward(float[] in) {
    for (int i=0; i<cells.length; i++) {
      Sy[i] = cells[i].forward(in);
    }
  }
  //------------------------------
  public void backward(float[] dy) {
    dSx = new float[dSx.length];
    float[] tmp = new float[dSx.length];
    for (int i=0; i<cells.length; i++) {
      tmp = cells[i].backward(dy[i]);
      for (int j=0; j<tmp.length; j++) {
        dSx[j]+=tmp[j];
      }
    }
  }
  //------------------------------
  public void update(float lr) {
    for (int i=0; i<cells.length; i++)
      cells[i].update(lr);
  }
  //------------------------------
  public void resetStates() {
    for (int i=0; i<cells.length; i++)
      cells[i].resetStates();
  }
}

//===================================================
class PatternMatcher {
  int patternLength1, patternLength2;
  int futureOffset;
  float minScore = 10;
  Pattern[] pattern;
  boolean cleanup = false;
  //------------------------------------------
  PatternMatcher(int pl1, int pl2, int fo) {
    pattern = new Pattern[0];
    patternLength1 = pl1;
    patternLength2 = pl2;
    futureOffset = fo;
  }
  //------------------------------------------
  // should i filter all the duplicate patterns? -> group all similar pattern into one?
  // that would speed up things enormously, but might worsen the results? -> test!
  public void learn(float[][] ar) {
    float[][] pat = new float[patternLength1][patternLength2];
    float[] res = new float[patternLength2];
    for (int i=patternLength1; i<ar.length-futureOffset; i++) {
      pattern = (Pattern[]) expand(pattern, pattern.length+1);
      pattern[pattern.length-1] = new Pattern();
      pattern[pattern.length-1].setValues(getPattern(ar, i));
      pattern[pattern.length-1].setResult(getResult(ar, i));
    }
    if (cleanup)cleanup();
    println(pattern.length+" pattern learned");
  }
  //------------------------------------------
  public void cleanup() {
    // i should somehow adjust/keep/take into account the target of the other patterns, no? ... -,-
    int index = 0, i=0;
    int removed = 0;
    float currentScore;
    float testScore;
    Pattern testPattern;
    float[][] results = new float[0][0];
    float currentResult=0;
    int right, wrong;
    boolean remove;
    println(pattern.length);
    while (index<pattern.length-1) {

      i=index+1;
      testPattern = pattern[index];
      results = new float[0][0];
      currentResult=0;
      right = 0;
      wrong = 0;
      while (i<pattern.length) {
        currentScore = testPattern.getScore(pattern[i]);
        if (currentScore>minScore) {
          // add result to results-ar
          results = (float[][]) expand(results, results.length+1);
          results[results.length-1] = pattern[i].result;
          // similar -> remove!
          arrayCopy(pattern, i, pattern, i-1, pattern.length-i);
          pattern = (Pattern[])shorten(pattern);
          //println("duplicate pattern removed");
          removed++;
        }
        i++;
      }
      // update expected result (there's probably a smarter way to do this)
      // would be good: first: test if they go to the same direction most of the time
      // if not: remove the pattern all together! (only keep high probability pattern!)
      remove = false;
      if (results.length>0) {
        for (int k=0; k<patternLength2; k++) {
          for (int j=0; j<results.length; j++) {
            if (results[j][k]>0) right++;
            else                wrong++;
            currentResult+=results[j][k];
          }
          // only act if more than half of the instances point in the same direction!
          if (right/2>wrong) {
            pattern[index].result[k]+=currentResult;
            pattern[index].result[k] /= (results.length+1);
          } else {
            remove=true;
          }
        }
      }
      // is this actually correct!?
      if (remove && index<pattern.length) {
        //arrayCopy(src, srcPosition, dst, dstPosition, length)
        arrayCopy(pattern, index+1, pattern, index, pattern.length-(index+1));
        pattern = (Pattern[])shorten(pattern);
        index--;
        //println("pattern discarded");
        removed++;
      }
      index++;
    }
    println(removed+" pattern removed");
  }
  //------------------------------------------
  public void removeEntryFromPatternAr(Pattern[] ar, int index) {
    arrayCopy(ar, index+1, ar, index, ar.length-(index+1));
    ar = (Pattern[])shorten(ar);
  }
  //------------------------------------------
  public float[][] getPattern(float[][] ar, int index) {
    float[][] ret = new float[patternLength1][patternLength2];
    for (int i=0; i<patternLength1; i++) {
      for (int j=0; j<patternLength2; j++) {
        //ret[i][j] = percentChange(ar[index-patternLength1+i][j], ar[index-patternLength1+i+1][j]);
        ret[i][j] = ar[index-patternLength1+i+1][j]-ar[index-patternLength1+i][j];
      }
    }
    return ret;
  }
  //------------------------------------------
  public float[] getResult(float[][] ar, int index) {
    float[] ret = new float[patternLength2];
    for (int j=0; j<patternLength2; j++)
      //  ret[j] = percentChange(ar[index][j], ar[index+futureOffset][j]);
      ret[j] = ar[index+futureOffset][j]-ar[index][j];
    return ret;
  }
  public boolean isSimilar(float[] p1, float[]p2){
    float sim = 0;
    for(int i=0;i<p1.length;i++){
      sim = 100.0f - abs(percentChange(p1[i],p2[i]));
    }
    if(sim > minScore) return true;
    else               return false;
  }
  //------------------------------------------
  public Pattern[] testPattern(float[][] ar, int index) {
    index+=patternLength1;
    float avgScore = 0;
    float[] prediction =new float[patternLength2];
    float thisScore=0;
    int weight = 0;

    float[][] testPattern = getPattern(ar, index); //percentChange(subset(ar,index-patternLength,patternLength+1));


    Pattern[] matchingPatternAr = new Pattern[0];
    for (int i=0; i<pattern.length; i++) {
      thisScore=pattern[i].getScore(testPattern);
      if (thisScore > minScore) {
        //prediction += pattern[i].getResult();
        weight++;
        // add pattern to found-pattern ar!
        matchingPatternAr = (Pattern[]) expand(matchingPatternAr, matchingPatternAr.length+1);
        matchingPatternAr[matchingPatternAr.length-1] = pattern[i];
      }
    }
    return matchingPatternAr;
  }

  //------------------------------------------
  public float getTarget(Pattern[] p) {
    float ret = 0;
    for (int i=0; i<p.length; i++)
      for (int j=0; j<p[i].dim2; j++)
        ret+= p[i].result[j];
    return ret/p.length;
  }
  public float percentChange(float a, float b){
    return 100.0f* (b-a)/a;
  }
}

//------------------------------
class Network {
  Layer[] layers;
  float lr = 1e-4f;
  float[] errorList=new float[0];
  int batchSize = 10;
  //------------------------------
  Network() {
    layers = new Layer[0];
  }
  //------------------------------
  public void resetStates(){
    for(int i=0;i<layers.length;i++)
    layers[i].resetStates();
  }
  //------------------------------
  public float learn(float[][] in, float[][] out) {
    float[] ret = new float[1];
    float[] err = new float[out[0].length];
    resetStates();
//    memory = new float[memory.length];
    float absErr = 0;

    for (int i=0; i<in.length; i++) {
      ret = forward(in[i]);

      for (int j=0; j<ret.length; j++) {
        err[j] = out[i][j]-ret[j];
        absErr+=abs(err[j]);
      //  err[j]*=abs(err[j]); // error squared!
      //  err[j]*=abs(err[j]); // error squared! (again...!)
      }
      backward(err);
      if(batchSize!=0 && i%batchSize==0) update(); // batchlearn
    }
    update();
    if (frameCount%100==0) {
      errorList = append(errorList, absErr/in.length);
    }
    return absErr/in.length;
  }

  //------------------------------
  public void push(float[] ar, float val) {
    for (int i=0; i<ar.length-1; i++) {
      ar[i+1]=ar[i];
    }
    ar[0]=val;
  }

  //------------------------------
  public float[] forward(float[] in) {
  //  push(memory, in[0]);
    layers[0].forward(in);
    for (int i=1; i<layers.length; i++) {
      layers[i].forward(layers[i-1].Sy);
    }
    return layers[layers.length-1].Sy;
  }
  //------------------------------
  public void backward(float[] err) {
    layers[layers.length-1].backward(err);
    for (int i=layers.length-2; i>=0; i--) {
      layers[i].backward(layers[i+1].dSx);
    }
  }
  //------------------------------
  public void update() {
    for (int i=0; i<layers.length; i++)
      layers[i].update(lr);
  }
  //------------------------------
  public void addLayer(int in, int out) {
    layers =  (Layer[]) expand(layers, layers.length+1);
    layers[layers.length-1] = new Layer(in, out);
  }
  public void setLearningRate(float _lr){lr = _lr;}
  //------------------------------
  public void displayError() {
    stroke(200, 0, 0);
    strokeWeight(1);
    for (int i=0; i<this.errorList.length-1; i++)
      line(
      map(i, 0, errorList.length-1, 0, width),
      map(errorList[i], 0, max(errorList), height, 0),
      map(i+1, 0, errorList.length-1, 0, width),
      map(errorList[i+1], 0, max(errorList), height, 0)
        );
  }
  //------------------------------
}

//===================================================
class Pattern {
  float[][] values;
  float result[];
  float minSimilarity = 0;
  int dim1, dim2;
  int right=0,wrong=0;
  //------------------------------------------
  Pattern(float[][] ar, float f[]) {
    setValues(ar);
    setResult(f);
  }
  Pattern(){}
  public void setValues(float[][] ar){
    values = ar;
    dim1 = ar.length;
    dim2 = ar[0].length;
  }
  public void setResult(float[] res){
    result = res;
  }
  //------------------------------------------
  public float getScore(float [][] ar) {
    float score = 0;
    float sim   = 0;

    for (int i=0; i<values.length; i++) {
      for (int j=0; j<values[i].length; j++) {
        sim = 100.0f - abs(percentChange(values[i][j], ar[i][j]));
        if (sim < minSimilarity)return 0; // why?
        score+=sim;
      }
    }
    return score/(dim1*dim2);
  }
  //------------------------------------------
  public float getScore(Pattern currentPat) {
    float score = 0;
    float sim   = 0;

    for (int i=0; i<values.length; i++) {
      for (int j=0; j<values[i].length; j++) {
        sim = 100.0f - abs(percentChange(values[i][j], currentPat.values[i][j]));
        if (sim < minSimilarity) return 0; // why?
        score+=sim;
      }
    }
    return score/(dim1*dim2);
  }
  //------------------------------------------
  public float[] getResult() {return result;  }
  //------------------------------------------
  public float[][] getValues() {return values;  }
  //------------------------------------------
  public float percentChange(float a, float b){
    return 100.0f* (b-a)/a;
  }
}





/*


public void setUpNetworkCluster(float[][] inputData, float[][] outputData){

  int  i=0, j=0;
  while(inputData.length>0){
    // add new entry to trainingData;
    trainingData = (float[][][]) expand(trainingData, trainingData.length+1);
    trainingData[trainingData.length-1] = new float[1][0];
    trainingData[trainingData.length-1][0] = inputData[i];

    trainingTarget = (float[][][]) expand(trainingTarget,trainingTarget.length+1);
    trainingTarget[trainingTarget.length-1] = new float[1][0];
    trainingTarget[trainingTarget.length-1][0] = outputData[i];

    println(inputData.length);
    arrayCopy(inputData, 0+1, inputData, 0, inputData.length-(0+1));
    inputData = (float[][])shorten(inputData);
    arrayCopy(outputData, 0+1, outputData, 0, outputData.length-(0+1));
    outputData = (float[][])shorten(outputData);
    j = 0;

    println("-------------");
    println(trainingData[trainingData.length-1][0] );
    println("-------------");
    while(j<inputData.length){
      println(inputData[j]);
      if(isSimilar(trainingData[trainingData.length-1][0],inputData[j])){
        trainingData[trainingData.length-1] = (float[][]) expand(trainingData[trainingData.length-1],trainingData[trainingData.length-1].length+1);
        trainingData[trainingData.length-1][trainingData[trainingData.length-1].length-1] = inputData[j];

        trainingTarget[trainingTarget.length-1] = (float[][]) expand(trainingTarget[trainingTarget.length-1],trainingTarget[trainingTarget.length-1].length+1);
        trainingTarget[trainingTarget.length-1][trainingTarget[trainingTarget.length-1].length-1] = outputData[j];
        // remove entry from inputData AR:

        arrayCopy(inputData, j+1, inputData, j, inputData.length-(j+1));
        inputData = (float[][])shorten(inputData);

        arrayCopy(outputData, j+1, outputData, j, outputData.length-(j+1));
        outputData = (float[][])shorten(outputData);
         j--; //??
      }
      else{
        // add input with "wrong"-Output! // makes sense? not sure yet...^^
        // makes sense, but doesnt work like this, because  i removed some entries from this ar already... -,-
        // need a "working" copy, but even then this is a rather tricky task...^^
        if(true){
          trainingData[trainingData.length-1] = (float[][]) expand(trainingData[trainingData.length-1],trainingData[trainingData.length-1].length+1);
          // add to current trainingData entry
          trainingData[trainingData.length-1][trainingData[trainingData.length-1].length-1] = inputData[j];

          trainingTarget[trainingTarget.length-1] = (float[][]) expand(trainingTarget[trainingTarget.length-1],trainingTarget[trainingTarget.length-1].length+1);

          // add to current trainingtarget entry
          float[]tmpOutput = new float[trainingTarget[trainingTarget.length-1][0].length];
          for(int t=0;t<tmpOutput.length;t++)tmpOutput[t] = -1;
          trainingTarget[trainingTarget.length-1][trainingTarget[trainingTarget.length-1].length-1] = tmpOutput;
          // remove entry from inputData AR:
        }


      }
      j++;

    }
  //  i++;
    // shorten inputdata-ar
    //arrayCopy(inputData, 0+1, inputData, 0, inputData.length-(0+1));
  //  inputData = (float[][])shorten(inputData);
  }

  println("############");
  for(i=0;i<trainingData.length;i++){
    for(j=0;j<trainingData[i].length;j++){
      println(trainingData[i][j]);
      println();
    }
    println("-----");
  }



  // add cluster for each trainingData-Set
  for( i=0;i<trainingData.length;i++){
    int[] layers ={trainingData[i][0].length,50, trainingTarget[i][0].length}; //  trainingTarget[i][].length //!?
    addCluster(layers);
  }
  println("cluster length: "+cluster.length);
}
*/
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "sketch_170622a_a_NN_Cluster_class" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
