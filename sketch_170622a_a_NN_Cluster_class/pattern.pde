
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
  void setValues(float[][] ar){
    values = ar;
    dim1 = ar.length;
    dim2 = ar[0].length;
  }
  void setResult(float[] res){
    result = res;
  }
  //------------------------------------------
  float getScore(float [][] ar) {
    float score = 0;
    float sim   = 0;

    for (int i=0; i<values.length; i++) {
      for (int j=0; j<values[i].length; j++) {
        sim = 100.0 - abs(percentChange(values[i][j], ar[i][j]));
        if (sim < minSimilarity)return 0; // why?
        score+=sim;
      }
    }
    return score/(dim1*dim2);
  }
  //------------------------------------------
  float getScore(Pattern currentPat) {
    float score = 0;
    float sim   = 0;

    for (int i=0; i<values.length; i++) {
      for (int j=0; j<values[i].length; j++) {
        sim = 100.0 - abs(percentChange(values[i][j], currentPat.values[i][j]));
        if (sim < minSimilarity) return 0; // why?
        score+=sim;
      }
    }
    return score/(dim1*dim2);
  }
  //------------------------------------------
  float[] getResult() {return result;  }
  //------------------------------------------
  float[][] getValues() {return values;  }
  //------------------------------------------
  float percentChange(float a, float b){
    return 100.0* (b-a)/a;
  }
}
