package util;

import random.Sfmt;
import static shared.Constants.*;

public class Calculator {
	public static int eGreedy(Sfmt rnd, double epsilon) {
		int A;
        double randNum = rnd.NextUnif();
        if (randNum <= epsilon) {
        	//eの確率
			A = rnd.NextInt(2);
        } else {
        	//(1-e)の確率
        	A = 0;
        }
        return A;
	}
}
