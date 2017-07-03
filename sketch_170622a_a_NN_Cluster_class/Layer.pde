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
  void forward(float[] in) {
    for (int i=0; i<cells.length; i++) {
      Sy[i] = cells[i].forward(in);
    }
  }
  //------------------------------
  void backward(float[] dy) {
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
  void update(float lr) {
    for (int i=0; i<cells.length; i++)
      cells[i].update(lr);
  }
  //------------------------------
  void resetStates() {
    for (int i=0; i<cells.length; i++)
      cells[i].resetStates();
  }
}
