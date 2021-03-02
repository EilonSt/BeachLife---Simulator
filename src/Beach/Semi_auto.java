package Beach;
import javax.swing.JTextArea;

public class Semi_auto {
	static Visitor[] visitors = BeachLifeMain.visitors;
	static Constants constants = new Constants();
	static JTextArea outputArea = BeachLifeMain.outputArea;
	static int[][] drowning_positions = new int[][] {{4, 2}, {6,3}, {3, 1}, {7, 1}};
	public static void semi_auto() {
		for (int i=0; i<BeachLifeMain.flags.length; i++) {
			Visitor visitor = visitors[i];
			if (BeachLifeMain.flags[i]==false) {
				BeachLifeMain.visitor_auto_moves(i);
				continue;
			}
			switch (BeachLifeMain.missions[i]) {
			case "DROWNING":
				boolean prevflag3=BeachLifeMain.flags[i];
				if (visitor.state.toString() == "DROWNING") {
					
					BeachLifeMain.flags[i] = false;
					if(prevflag3) {
						switch(i) {
						case 0:
							outputArea.setText(outputArea.getText()+"Greg is " +BeachLifeMain.missions[i]+ "\n");
							break;
						case 1:
							outputArea.setText(outputArea.getText()+"Bob is " +BeachLifeMain.missions[i]+ "\n");
							break;
						case 2:
							outputArea.setText(outputArea.getText()+"Peter is " +BeachLifeMain.missions[i]+ "\n");
							break;
						case 3:
							outputArea.setText(outputArea.getText()+"Rachel is " +BeachLifeMain.missions[i]+ "\n");
							break;
						}
						}
					BeachLifeMain.visitor_auto_moves(i);
					
					break;
				}
				else if (visitor.state.toString() == "SAVED") {
					BeachLifeMain.visitor_auto_moves(i);
					break;
				}
				else {
					if (visitor.changedToSafe == true) {
						visitor.changedToSafe = false;
					}
					else if (visitor.x == drowning_positions[i][0] && visitor.y == drowning_positions[i][1]) {
						visitor.setStateToDrowning();
						BeachLifeMain.flags[i] = false;
						switch(i) {
						case 0:
							outputArea.setText(outputArea.getText()+"Greg is " +BeachLifeMain.missions[i]+ "\n");
							break;
						case 1:
							outputArea.setText(outputArea.getText()+"Bob is " +BeachLifeMain.missions[i]+ "\n");
							break;
						case 2:
							outputArea.setText(outputArea.getText()+"Peter is " +BeachLifeMain.missions[i]+ "\n");
							break;
						case 3:
							outputArea.setText(outputArea.getText()+"Rachel is " +BeachLifeMain.missions[i]+ "\n");
							break;
						}
						break;
				}
					else {
						visitor.move(visitors,drowning_positions[i][0],drowning_positions[i][1],false);
						break;
					}
					
				}
			case "IN SEA":
				boolean prevflag=BeachLifeMain.flags[i];
				if (visitor.y < constants.SHORE_Y-1) {
					BeachLifeMain.flags[i] = false;
					if(prevflag) {
						switch(i) {
						case 0:
							outputArea.setText(outputArea.getText()+"Greg is " +BeachLifeMain.missions[i]+ "\n");
							break;
						case 1:
							outputArea.setText(outputArea.getText()+"Bob is " +BeachLifeMain.missions[i]+ "\n");
							break;
						case 2:
							outputArea.setText(outputArea.getText()+"Peter is " +BeachLifeMain.missions[i]+ "\n");
							break;
						case 3:
							outputArea.setText(outputArea.getText()+"Rachel is " +BeachLifeMain.missions[i]+ "\n");
							break;
						}
					}
					BeachLifeMain.visitor_auto_moves(i);
					break;
				}
				if (visitor.state.toString() == "SAVED") {
					BeachLifeMain.visitor_auto_moves(i);
					break;
				}
				else {
					visitor.move(visitors,-1,0,false);
					break;
				}
			case "IN SHORE":
				boolean prevflag2=BeachLifeMain.flags[i];
				if (visitor.y >= constants.SHORE_Y + 1) {
					BeachLifeMain.flags[i] = false;
					if(prevflag2) {
						switch(i) {
						case 0:
							outputArea.setText(outputArea.getText()+"Greg is " +BeachLifeMain.missions[i]+ "\n");
							break;
						case 1:
							outputArea.setText(outputArea.getText()+"Bob is " +BeachLifeMain.missions[i]+ "\n");
							break;
						case 2:
							outputArea.setText(outputArea.getText()+"Peter is " +BeachLifeMain.missions[i]+ "\n");
							break;
						case 3:
							outputArea.setText(outputArea.getText()+"Rachel is " +BeachLifeMain.missions[i]+ "\n");
							break;
						}
					}
					BeachLifeMain.visitor_auto_moves(i);
					break;
				}
				visitor.aboutToDrown = false; //INBAL BUG FIX
				if (visitor.state.toString() == "SAFE") {
					visitor.move(visitors,-1,9,false);
					break;
				}
				BeachLifeMain.visitor_auto_moves(i);
				break;
			case "-----":
				BeachLifeMain.visitor_auto_moves(i);
				BeachLifeMain.flags[i] = false;
				break;
				
				
			}
			BeachLifeMain.updateVisitorInputs(i);
		}
				
		
		if(!BeachLifeMain.array_boolean_check(BeachLifeMain.flags)) {
			BeachLifeMain.semiAuto=false;
			BeachLifeMain.ScenarioRunning=false;
			outputArea.setText(outputArea.getText()+"Manual scenario is Done! \n");
			for (int j=0; j<BeachLifeMain.flags.length; j++) {
				BeachLifeMain.flags[j] = true;
			}
		}
	}

}
