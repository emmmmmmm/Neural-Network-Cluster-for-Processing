class NetworkCluster {
  Network[] cluster;
  Network outputNetwork;
  float[][][] trainingData;
  float[][][] trainingTarget;
  public float minScore = 00; // percent
  float learningRate = 1e-3;
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
    void initAr(float[]ar, int val){
      for(int i=0;i<ar.length;i++)ar[i]=val;
    }
    void initAr(boolean[]ar, boolean val){
      for(int i=0;i<ar.length;i++)ar[i]=val;
    }
    //--------------------------------
    boolean isSimilar(float[] p1, float[] p2){
      float sim = 0;
      float thisSim = 0;
      for(int i=0;i<p1.length;i++){
        thisSim = 100.0 - abs(percentChange(p1[i], p2[i]));
        sim+=thisSim;

      }
      sim/=p1.length;
      //  println(sim/p1.length);
      if(sim < minScore)  return true;
      else                return false;
    }
    //--------------------------------
    //--------------------------------
    float percentChange(float a, float b) {
      float aa,bb;
      aa = a<b? a:b;
      bb = a<b? b:a;
      if (aa==bb) return 00;
      else if (aa==0) return 100;
      else     return 100.0* (bb-aa)/abs(aa);
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
    float[][] forward(float[] input) {
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
