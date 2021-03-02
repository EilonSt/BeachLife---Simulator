package Beach;
import Beach.BeachLifeMain.Weather;

import java.util.Random;

import javax.swing.JTextArea;


public class Scenarios {
	static Constants constants = new Constants();
	static boolean done_scenario2 = false;
	static int next_drown_scenario3 = -1;
	static int[] drown_order_scenario3 = new int[]{3, 1, 0, 2};
	static int level_scenario4 = 0;
	static int unstable_weather_scenario5=0;
	static int[][] drowning_positions = new int[][] {{4, 2}, {6,3}, {3, 1}, {7, 1}};
	static int[][] ready_to_drown_pos = new int[][] {{4, 3}, {6,4}, {2, 1}, {8, 1}};
	static Visitor[] visitors = BeachLifeMain.visitors;
	static JTextArea outputArea = BeachLifeMain.outputArea;
	

	public static boolean Scenario1_Tsunami() {
		BeachLifeMain.risk_of_drowning = constants.RISK_OF_DROWNING_HEAVY_STORM;
		BeachLifeMain.weather = Weather.STORM;
		outputArea.setText("A Tsunami is coming! \n Everyone needs to get out of the water!\n");
		BeachLifeMain.weather_change_count=0;
		BeachLifeMain.Tsunami=true;
		if(BeachLifeMain.lifeguard1_TsunamiWarning || BeachLifeMain.lifeguard2_TsunamiWarning) {//
			BeachLifeMain.warnedTsunami=true;
		}

		for(int i=0;i<constants.V;i++) {
			Visitor visitor = visitors[i];	
			if(BeachLifeMain.warnedTsunami) {
				if(visitor.state.toString()=="SAFE" && visitor.y<constants.Y-1) {
					visitor.move(visitors, -1, constants.Y-1, false);
					BeachLifeMain.updateVisitorInputs(i);
				}
				else if(visitor.state.toString()=="SAFE") {
					visitor.move(visitors, -1, constants.Y-1, false);
					BeachLifeMain.updateVisitorInputs(i);
					BeachLifeMain.flags[i]=false;
				}
				else {
					BeachLifeMain.visitor_auto_moves(i);
				}
			}
			else {
				BeachLifeMain.visitor_auto_moves(i);
			}

		}
		if(!BeachLifeMain.array_boolean_check(BeachLifeMain.flags)) {
			return true;
		}
		else {
			return false;
		}		
	}


	public static void Scenario2_Everyone_drowns() {
		outputArea.setText("All the visitors are going to drown at the same time!\n");
		if (done_scenario2) {
			for (int i =0; i<constants.V; i++) {
				visitors[i].setStateToDrowning();
				BeachLifeMain.updateVisitorInputs(i);
			}
			outputArea.setText(outputArea.getText()+"Every one drowns scenario Done!\n");
			BeachLifeMain.doScenario = false;
			BeachLifeMain.ScenarioRunning = false;
		}
		else {
			int ready_count = 0;
			for (int i=0; i<constants.V; i++) {
				if (visitors[i].state != Visitor.State.SAFE)
					BeachLifeMain.visitor_auto_moves(i);
				else if (visitors[i].changedToSafe == true) {
					visitors[i].changedToSafe = false;
					BeachLifeMain.updateVisitorInputs(i);
				}
				else if(visitors[i].x==ready_to_drown_pos[i][0] && visitors[i].y==ready_to_drown_pos[i][1]) {
					ready_count++;
					visitors[i].dir = Visitor.Direction.INPLACE;
				}
				else {
					visitors[i].move(visitors, ready_to_drown_pos[i][0], ready_to_drown_pos[i][1], false);
					BeachLifeMain.updateVisitorInputs(i);
				}
			}
			if (ready_count==constants.V) {
				for (int i=0; i<constants.V; i++) {
					visitors[i].move(visitors, drowning_positions[i][0], drowning_positions[i][1], false);
					BeachLifeMain.updateVisitorInputs(i);
					done_scenario2 = true;
					
				}
		}
		}
	}
	
	
	public static void Scenario3_Rotational_Drowning() {
		outputArea.setText("Every one will be drown one after each other\n");
		if (next_drown_scenario3 != -1) {
			if (next_drown_scenario3==0) {
				visitors[drown_order_scenario3[0]].move(visitors, drowning_positions[drown_order_scenario3[0]][0], drowning_positions[drown_order_scenario3[0]][1], false);
				BeachLifeMain.updateVisitorInputs(drown_order_scenario3[0]);
			}
			else if(next_drown_scenario3==constants.V) {
				visitors[drown_order_scenario3[next_drown_scenario3-1]].setStateToDrowning();
				BeachLifeMain.updateVisitorInputs(drown_order_scenario3[next_drown_scenario3-1]);
			}
			else {
				visitors[drown_order_scenario3[next_drown_scenario3]].move(visitors, drowning_positions[drown_order_scenario3[next_drown_scenario3]][0], drowning_positions[drown_order_scenario3[next_drown_scenario3]][1], false);
				visitors[drown_order_scenario3[next_drown_scenario3-1]].setStateToDrowning();
				BeachLifeMain.updateVisitorInputs(drown_order_scenario3[next_drown_scenario3-1]);
				BeachLifeMain.updateVisitorInputs(drown_order_scenario3[next_drown_scenario3]);
			}
			
				if(next_drown_scenario3==constants.V) {
					BeachLifeMain.doScenario = false;
					BeachLifeMain.ScenarioRunning = false;
					outputArea.setText(outputArea.getText()+"Every one drowns one after each other scenario is Done!\n");
				}
					
			next_drown_scenario3++;
			}
		else {
			int ready_count = 0;
			for (int i=0; i<constants.V; i++) {
				if (visitors[i].state != Visitor.State.SAFE)
					BeachLifeMain.visitor_auto_moves(i);
				else if (visitors[i].changedToSafe == true) {
					visitors[i].changedToSafe = false;
					BeachLifeMain.updateVisitorInputs(i);
				}
				else if(visitors[i].x==ready_to_drown_pos[i][0] && visitors[i].y==ready_to_drown_pos[i][1]) {
					visitors[i].dir = Visitor.Direction.INPLACE;
					ready_count++;
					BeachLifeMain.updateVisitorInputs(i);
				}
				else {
					visitors[i].move(visitors, ready_to_drown_pos[i][0], ready_to_drown_pos[i][1], false);
					BeachLifeMain.updateVisitorInputs(i);
				}
			}
			if (ready_count==constants.V) {
				next_drown_scenario3++;

		}
		}
	}

	
	public static void Scenario4_Exactly_2_Drowning () {
		outputArea.setText("In each side 2 visitors will be drowning \n");
		if (level_scenario4 == 0) {
			int safe_cnt=0;
			for(int i=0; i<constants.V; i++) {
				if (visitors[i].state != Visitor.State.SAFE)
					BeachLifeMain.visitor_auto_moves(i);
				else if (visitors[i].changedToSafe == true) {
					visitors[i].changedToSafe = false;
					BeachLifeMain.updateVisitorInputs(i);
				}
				else {
					if(visitors[i].y>6) {
						safe_cnt++;
						visitors[i].move(visitors, -1, -1, false);
					}
					else {
						if (visitors[i].y == 6)
							safe_cnt++;
						visitors[i].move(visitors, -1, 7, false);
					}
					BeachLifeMain.updateVisitorInputs(i);
				}
			}
			if (safe_cnt==constants.V) 
				level_scenario4 = 1;
		}
		else if (level_scenario4 == 1) {
			int ready_cnt=0;
			for(int i=0; i<constants.V; i++){
				if (i%2==1) {
					if(visitors[i].x==ready_to_drown_pos[i][0] && visitors[i].y==ready_to_drown_pos[i][1]) {
						visitors[i].dir = Visitor.Direction.INPLACE;
						ready_cnt++;
					}
					else
						visitors[i].move(visitors, ready_to_drown_pos[i][0], ready_to_drown_pos[i][1], false);
					BeachLifeMain.updateVisitorInputs(i);

				}
				else {
					if(visitors[i].y>6) {
						visitors[i].move(visitors, -1, -1, false);
					}
					else {
						visitors[i].move(visitors, -1, 7, false);
					}
					BeachLifeMain.updateVisitorInputs(i);
				}
				
			}
			if (ready_cnt*2==constants.V) {
				for (int i=1; i<constants.V; i+=2) {
					visitors[i].move(visitors, drowning_positions[i][0], drowning_positions[i][1], false);
					BeachLifeMain.updateVisitorInputs(i);
				}
				level_scenario4 = 2;
		}
		}
		
		else if (level_scenario4 == 2) {
			for (int i=0; i<constants.V; i++) {
				if (i%2==1) {
					visitors[i].setStateToDrowning();
					BeachLifeMain.updateVisitorInputs(i);
				}
				else {
					if(visitors[i].y>6) {
						visitors[i].move(visitors, -1, -1, false);
					}
					else {
						visitors[i].move(visitors, -1, 7, false);
					}
					BeachLifeMain.updateVisitorInputs(i);
				}
				}
			level_scenario4 = 3;
		}
		else if (level_scenario4 == 3) {
			int safe_right=0;
			for(int i=0; i<constants.V; i++) {
				if (i%2==1) {
					if (visitors[i].state != Visitor.State.SAFE)
						BeachLifeMain.visitor_auto_moves(i);
					else if (visitors[i].changedToSafe == true) {
						visitors[i].changedToSafe = false;
						BeachLifeMain.updateVisitorInputs(i);
					}
					else {
						safe_right++;
						if(visitors[i].y>6) {
							visitors[i].move(visitors, -1, -1, false);
						}
						else {
							visitors[i].move(visitors, -1, 7, false);
						}
						BeachLifeMain.updateVisitorInputs(i);
					}
				}
				else {
					if(visitors[i].y>6) {
						visitors[i].move(visitors, -1, -1, false);
					}
					else {
						visitors[i].move(visitors, -1, 7, false);
					}
					BeachLifeMain.updateVisitorInputs(i);
				}
				
			}
			if (safe_right*2==constants.V) 
				level_scenario4 = 4;
		}
		else if (level_scenario4 == 4) {
			int ready_cnt=0;
			for(int i=0; i<constants.V; i++){
				if (i%2 ==1) {
					if(visitors[i].y>6) {
						visitors[i].move(visitors, -1, -1, false);
					}
					else {
						visitors[i].move(visitors, -1, 7, false);
					}
					BeachLifeMain.updateVisitorInputs(i);
				}
				else {
					if(visitors[i].x==ready_to_drown_pos[i][0] && visitors[i].y==ready_to_drown_pos[i][1]) {
						ready_cnt++;
						visitors[i].dir = Visitor.Direction.INPLACE;
					}
					else 
						visitors[i].move(visitors, ready_to_drown_pos[i][0], ready_to_drown_pos[i][1], false);
					BeachLifeMain.updateVisitorInputs(i);

				}
			}
			if (ready_cnt*2==constants.V) {
				for (int i=0; i<constants.V; i+=2) {
					visitors[i].move(visitors, drowning_positions[i][0], drowning_positions[i][1], false);
					BeachLifeMain.updateVisitorInputs(i);
				}
				level_scenario4 = 5;
				}
		}
		else if (level_scenario4 == 5) {
			for (int i=0; i<constants.V; i+=2) {
				visitors[i].setStateToDrowning();
				BeachLifeMain.updateVisitorInputs(i);
			}
			BeachLifeMain.doScenario = false;
			BeachLifeMain.ScenarioRunning = false;
			outputArea.setText(outputArea.getText()+"Two visitors drown at each side scenario Done!\n");
		}
	}	


	public static void Scenario5_Unstable_Weather() {
		BeachLifeMain.weather_change_count=0;
		Random w_rand = new Random();
		int w_change=w_rand.nextInt(1);
		for(int i=0;i<constants.V;i++) {
			BeachLifeMain.visitor_auto_moves(i);
		}
		if(unstable_weather_scenario5%3==0) {
			switch (BeachLifeMain.weather) {
			case STORM:
				if(w_change==0) {
					BeachLifeMain.risk_of_drowning = constants.RISK_OF_DROWNING;
					BeachLifeMain.weather = Weather.SUN;
        			outputArea.setText( "Weather is unstable  \nWeather changed to Sunny  \n");
				}
				else {
					BeachLifeMain.risk_of_drowning = constants.RISK_OF_DROWNING_LIGHT_STORM;
					BeachLifeMain.weather = Weather.CLOUDS;	       
					outputArea.setText( "Weather is unstable  \nWeather changed to Cloudy  \n");
				}
				break;
			case SUN:
				if(w_change==0) {
					BeachLifeMain.risk_of_drowning = constants.RISK_OF_DROWNING_LIGHT_STORM;
					BeachLifeMain.weather = Weather.CLOUDS;	
					outputArea.setText( "Weather is unstable  \nWeather changed to Cloudy  \n");
				}
				else {
					BeachLifeMain.risk_of_drowning = constants.RISK_OF_DROWNING_HEAVY_STORM;
					BeachLifeMain.weather = Weather.STORM;
					outputArea.setText( "Weather is unstable  \nWeather changed to Storm  \n");
				}
				break;
				
			case CLOUDS:
				if(w_change==0) {
					BeachLifeMain.risk_of_drowning = constants.RISK_OF_DROWNING_HEAVY_STORM;
					BeachLifeMain.weather = Weather.STORM;
					outputArea.setText( "Weather is unstable  \nWeather changed to Storm  \n");
				}
				else {
					BeachLifeMain.risk_of_drowning = constants.RISK_OF_DROWNING;
					BeachLifeMain.weather = Weather.SUN;
        			outputArea.setText( "Weather is unstable  \nWeather changed to Sunny  \n");
				}
				break;
			
			}
		}
		unstable_weather_scenario5++;
		if(unstable_weather_scenario5==15) {
			outputArea.setText("Weather is stable again \n Scenario is Done! \n");
			unstable_weather_scenario5=0;
			BeachLifeMain.ScenarioRunning=false;
			BeachLifeMain.doScenario=false;
		}
	}

}
