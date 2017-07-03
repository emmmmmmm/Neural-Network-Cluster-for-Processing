
//------------------------------
class Network {
  Layer[] layers;
  float lr = 1e-4;
  float[] errorList=new float[0];
  int batchSize = 10;
  //------------------------------
  Network() {
    layers = new Layer[0];
  }
  //------------------------------
  void resetStates(){
    for(int i=0;i<layers.length;i++)
    layers[i].resetStates();
  }
  //------------------------------
  float learn(float[][] in, float[][] out) {
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
  void push(float[] ar, float val) {
    for (int i=0; i<ar.length-1; i++) {
      ar[i+1]=ar[i];
    }
    ar[0]=val;
  }

  //------------------------------
  float[] forward(float[] in) {
  //  push(memory, in[0]);
    layers[0].forward(in);
    for (int i=1; i<layers.length; i++) {
      layers[i].forward(layers[i-1].Sy);
    }
    return layers[layers.length-1].Sy;
  }
  //------------------------------
  void backward(float[] err) {
    layers[layers.length-1].backward(err);
    for (int i=layers.length-2; i>=0; i--) {
      layers[i].backward(layers[i+1].dSx);
    }
  }
  //------------------------------
  void update() {
    for (int i=0; i<layers.length; i++)
      layers[i].update(lr);
  }
  //------------------------------
  void addLayer(int in, int out) {
    layers =  (Layer[]) expand(layers, layers.length+1);
    layers[layers.length-1] = new Layer(in, out);
  }
  void setLearningRate(float _lr){lr = _lr;}
  //------------------------------
  void displayError() {
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
