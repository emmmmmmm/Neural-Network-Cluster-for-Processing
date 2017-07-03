
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
  void learn(float[][] ar) {
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
  void cleanup() {
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
  void removeEntryFromPatternAr(Pattern[] ar, int index) {
    arrayCopy(ar, index+1, ar, index, ar.length-(index+1));
    ar = (Pattern[])shorten(ar);
  }
  //------------------------------------------
  float[][] getPattern(float[][] ar, int index) {
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
  float[] getResult(float[][] ar, int index) {
    float[] ret = new float[patternLength2];
    for (int j=0; j<patternLength2; j++)
      //  ret[j] = percentChange(ar[index][j], ar[index+futureOffset][j]);
      ret[j] = ar[index+futureOffset][j]-ar[index][j];
    return ret;
  }
  boolean isSimilar(float[] p1, float[]p2){
    float sim = 0;
    for(int i=0;i<p1.length;i++){
      sim = 100.0 - abs(percentChange(p1[i],p2[i]));
    }
    if(sim > minScore) return true;
    else               return false;
  }
  //------------------------------------------
  Pattern[] testPattern(float[][] ar, int index) {
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
  float getTarget(Pattern[] p) {
    float ret = 0;
    for (int i=0; i<p.length; i++)
      for (int j=0; j<p[i].dim2; j++)
        ret+= p[i].result[j];
    return ret/p.length;
  }
  float percentChange(float a, float b){
    return 100.0* (b-a)/a;
  }
}
