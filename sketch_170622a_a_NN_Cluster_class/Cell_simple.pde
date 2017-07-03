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
      Wx[i] = random(0.5,0.99)*random(100)>50?-1:1;
  }
  //------------------------------
  float forward(float[] in) {

    Sx = in;
    Sy = 0;
    for (int i=0; i<in.length; i++)
      Sy+=(Wx[i])*in[i];
    Sy+=Bx;
    Sy= softSign(Sy);
    return Sy;
  }
  void resetStates(){}

  //------------------------------
  float[] backward(float dSy) {
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
  void update(float lr) {
    for (int i=0; i<Wx.length; i++) {
      Wx[i]+=dWx[i]*lr;
    }
    Bx+=dBx*lr;
    dWx=new float[dWx.length];
    dBx=0;
  }
  //------------------------------
  float softSign(float x) {
    return 2.0 * x / (1.0 + abs(x));
  }
  //------------------------------
  float deSoftSign(float x){
    return  (2.0 / sq(1.0+abs(x)));
  }
}
