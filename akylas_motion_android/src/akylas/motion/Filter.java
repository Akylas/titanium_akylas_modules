package akylas.motion;

public class Filter {
	private float[] mLastVector = new float[3];
	public void filter(float[] inVector, float[] outVector) {
		 float[] lastVector = mLastVector;
		 for (int i = 0; i < lastVector.length; i++) {
		  float value = 0.075f * inVector[i] + (1 - 0.075f)
		    * lastVector[i];
		  outVector[i] = value;
		  lastVector[i] = value;
		 }
		}

}
