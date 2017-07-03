float trainingInput[][], trainingTarget[][], evalInput[][], evalTarget[][];

String[] vocab;
void importGestures(){
  String lines[] = loadStrings("data/pointClouds.txt");
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

  lines = loadStrings("data/pointClouds_eval.txt");
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
float[] stringToFloat(String[] ar){
  float[] ret = new float[ar.length];
  for(int i=0;i<ret.length;i++){
    ret[i] = Float.parseFloat(ar[i])*2;

  }
  return ret;
}
//------------------------------------------
void buildVocab(String[] labels) {
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
int getFromVocab(String input) {
  for (int i=0; i<vocab.length; i++)
  if (vocab[i].equals(input)) return i;
  return -1;
}
//------------------------------------------
String getFromVocab(int input){
  return vocab[input];
}
String getFromVocab(float[] input){
  for(int i=0;i<input.length;i++)
  if(input[i]==1)return vocab[i];
  return "";
}


//------------------------------------------
void buildData() {
  int numSamples = 60;
  int numInputs = 3;
  int numOutputs = 2;
  float[][] inputData,targetData;
  inputData = new float[numSamples][numInputs];
  targetData = new float[numSamples][numOutputs];

  for (int i=0; i<inputData.length; i++) {
    for (int j=0; j<inputData[i].length; j++) {
      inputData[i][j] = round(random(10))/10.0;
    }
  }
  for (int i=0; i<targetData.length; i++) {
    for(int j=0;j<targetData[i].length;j++){
      targetData[i][j] = round(random(1))/1.0; //0.01f+(random(1)); //cos(i*.2);
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
void initAr(float[]ar, int val){
  for(int i=0;i<ar.length;i++)ar[i]=val;
}
void initAr(boolean[]ar, boolean val){
  for(int i=0;i<ar.length;i++)ar[i]=val;
}
