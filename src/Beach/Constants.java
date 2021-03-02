package Beach;


public class Constants{
	final int V=4; //max number of visitors
	final int L=2; // max number of life-guards;
	final int X=10;
	final int Y=X;
	final int IMG_DIM = 80;
	final int SHORE_Y = Y/2;
	final int HALF_X = X/2;
	final int HALF_Y = Y/2;
	final int RISK_OF_LIGHT_STORM = 3; // 1,...,20
	final int RISK_OF_HEAVY_STORM = 1; // 1,...,20  
	final int TOWERS_Y = (X/10)*8;//(int) Math.floor(0.8 * y);
	final int[] TOWERS_X = new int[]{(X/10)*2, (X/10)*7};
	final int LADDERS_Y=TOWERS_Y-1;
	final int[] LADDERS_X = new int[]{TOWERS_X[0], TOWERS_X[1]};
	final int[][] DROWNING_POSITIONS1 = new int[][] {{3,1}, {4,2}};
	final int[][] DROWNING_POSITIONS2 = new int[][] {{7,1}, {6,3}};
	final int RISK_OF_DROWNING = 3;	
	final int RISK_OF_DROWNING_LIGHT_STORM = 5;	
	final int RISK_OF_DROWNING_HEAVY_STORM = 8;
	final int RISK_OF_WEATHER_CHANGE = 20;
	final int WEATHER_CHANGE_ITERATIONS = 15;
	Weather weather;
	FlagColor flagColor;
	

	
	enum Weather{
		SUN,
		CLOUDS,
		STORM
	}
	// 0 = white, 1 = red, 2 = black 
	enum FlagColor{
		WHITE,
		RED,
		BLACK
	}
}
	

			
			
			
			
			