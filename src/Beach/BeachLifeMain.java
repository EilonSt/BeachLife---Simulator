package Beach;


import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import tau.smlab.syntech.controller.executor.ControllerExecutor;
import tau.smlab.syntech.controller.jit.BasicJitController;

public class BeachLifeMain extends JPanel{
	
	enum Weather{
		SUN,
		CLOUDS,
		STORM
	}

	enum FlagColor{
		WHITE,
		RED,
		BLACK
	}
	
	private static final long serialVersionUID = 1L;
	ControllerExecutor executor;
	
	static Constants constants = new Constants();
	Images img = new Images();
	static JTextArea outputArea = new JTextArea("Output for scenarios and events will be displayed here\n", 0, 20);
	
	//Weather
	static Weather weather=Weather.SUN;
	static int weather_change_count = 0; // Counter that ensures that the weather is stable for at least a short period of time
	
	//Scenarios and Buttons
	static boolean semiAuto = false;
	static boolean doScenario = false;
	static String scenario;
	static boolean ScenarioRunning = false;
	static String[] missions = new String[4];
	static boolean[] flags = new boolean[]{true, true, true, true};

	//Lifeguards
	static Lifeguard[] lifeguards= new Lifeguard[constants.L];
	int[] Prev_LG_y = new int[constants.L];
	short[] lifeguards_dir = new short[constants.L];
	
	//Flags
	FlagColor flag1Color = FlagColor.WHITE;
	FlagColor flag2Color = FlagColor.WHITE; 
	
	//Tsunami
	static boolean lifeguard1_TsunamiWarning;
	static boolean lifeguard2_TsunamiWarning;
	static boolean Tsunami;
	static boolean warnedTsunami=false;
	static Map<String,String> inputs = new HashMap<String, String>();
	
	//Images
	int background_img_size = constants.X*constants.IMG_DIM;
	static short[] onBoat = {0,0};
	
	// visitors
	boolean v0_and_v2_together = false;
	boolean v1_and_v3_together = false;
	static Visitor[] visitors = new Visitor[constants.V];
	static int risk_of_drowning = constants.RISK_OF_DROWNING; // 1,...,10 changes according to weather (higher for bad weather, lower for good weather)

	public void run() throws Exception{
		executor = new ControllerExecutor(new BasicJitController(), "out");
		img.load();
		Random rand = new Random();
		for (int i=0;i<constants.V;i++) {
			visitors[i] = new Visitor(i);
		}		
		setInitialValues();

		//Initial values
		for (int i=0;i<constants.V;i++) {
			updateVisitorInputs(i);
		}
		inputs.put("weather", weather.toString());
		inputs.put("Tsunami",Boolean.toString(Tsunami));
				
		executor.initState(inputs);
		Map<String, String> sysValues = executor.getCurrOutputs();
		
		//Get system initial values
		for(int i =0;i<constants.L;i++) {
			int lifeguard_x = Integer.parseInt(sysValues.get("lifeguard"+(i+1)+"[0]"));
			int lifeguard_y = Integer.parseInt(sysValues.get("lifeguard"+(i+1)+"[1]"));
			lifeguards[i] = new Lifeguard(lifeguard_x, lifeguard_y);
		}
		flag1Color= getFlagColor(sysValues.get("flag1"));
		flag2Color= getFlagColor(sysValues.get("flag2"));
		lifeguard1_TsunamiWarning=Boolean.parseBoolean(sysValues.get("lifeguard1_TsunamiWarning"));
		lifeguard2_TsunamiWarning=Boolean.parseBoolean(sysValues.get("lifeguard2_TsunamiWarning")); 
		
		paint(this.getGraphics());
		Thread.sleep(700);
		
		while (true) {
			weather_change_count++;
			if (weather_change_count > constants.WEATHER_CHANGE_ITERATIONS) { // Allow weather change
				int rand_weather = rand.nextInt(constants.RISK_OF_WEATHER_CHANGE);
				if (rand_weather < constants.RISK_OF_HEAVY_STORM) {
					if (weather != Weather.STORM) {
						risk_of_drowning = constants.RISK_OF_DROWNING_HEAVY_STORM;
						weather = Weather.STORM;
						weather_change_count = 0;	
					}
				}
				else if(rand_weather < constants.RISK_OF_LIGHT_STORM) {
					if (weather != Weather.CLOUDS) {
						risk_of_drowning = constants.RISK_OF_DROWNING_LIGHT_STORM;
						weather = Weather.CLOUDS;
						weather_change_count = 0;	
					}
				}
				else {
					if (weather != Weather.SUN) {
						risk_of_drowning = constants.RISK_OF_DROWNING;
						weather = Weather.SUN;
						weather_change_count = 0;	
					}
				}
			}				
			Tsunami=false;
			if(semiAuto){
				Semi_auto.semi_auto();
			} 
			else if (doScenario) {
				for(int i=0;i<constants.V;i++) {
					visitors[i].aboutToDrown = false;
				}
				switch(scenario) {
				case "Tsunami":
					boolean done;
					done = Scenarios.Scenario1_Tsunami();
					if (done) {
						Tsunami_Animation(sysValues,this.getGraphics());
						doScenario=false;
						ScenarioRunning=false;
						outputArea.setText("Everyone is safe now.\n Scenario is done!");
						warnedTsunami=false; //
						for (int j=0; j<BeachLifeMain.flags.length; j++) {
							BeachLifeMain.flags[j] = true;
						}
					}
					break;
				case "Everyone drowns at the same time":
					Scenarios.Scenario2_Everyone_drowns();
					break;
				case "Rotational Drowning":
					Scenarios.Scenario3_Rotational_Drowning();
					break;
				case "Exactly 2 drown on the same side":
					Scenarios.Scenario4_Exactly_2_Drowning();
					break;
				case "Unstable weather":
					Scenarios.Scenario5_Unstable_Weather();
					break;
				}
			}
			else { // auto-run
				for(int i=0;i<constants.V;i++) {
					visitor_auto_moves(i);
				}
			} 
			inputs.put("weather", weather.toString());
			inputs.put("Tsunami",Boolean.toString(Tsunami));
			
			executor.updateState(inputs);
			sysValues = executor.getCurrOutputs();
						
			for (int i=0; i<constants.L; i++) {
				Prev_LG_y[i] = lifeguards[i].y;
				lifeguards[i].x = Integer.parseInt(sysValues.get("lifeguard"+(i+1)+"[0]"));
				lifeguards[i].y = Integer.parseInt(sysValues.get("lifeguard"+(i+1)+"[1]"));
			}
			for (int i=0; i<constants.L; i++) {				
				if (Prev_LG_y[i] - lifeguards[i].y == -1) {
					lifeguards_dir[i] = 2;
				}
				else if (Prev_LG_y[i] - lifeguards[i].y == 1) {
					lifeguards_dir[i] = 1;
				}
			}			
			lifeguard1_TsunamiWarning=Boolean.parseBoolean(sysValues.get("lifeguard1_TsunamiWarning"));
			lifeguard2_TsunamiWarning=Boolean.parseBoolean(sysValues.get("lifeguard2_TsunamiWarning"));
			flag1Color= getFlagColor(sysValues.get("flag1"));
			flag2Color= getFlagColor(sysValues.get("flag2"));
			
			paint(this.getGraphics());
			Thread.sleep(700);
		}
	}
		
		public static void visitor_auto_moves(int i) {
			Visitor visitor = visitors[i];
			if(visitor.aboutToDrown()) {
				visitor.setStateToDrowning();
			}
			else if (visitor.isDrowning()) {
				//Check if the lifeguard has reached the drowning visitor
				for (int j=0; j<constants.L; j++) {
					if (i%constants.L == j && lifeguards[j].x== visitor.x && lifeguards[j].y == visitor.y) {
						lifeguards[j].is_saving = true;
						onBoat[j] += 1;
						visitor.setSavedBy(j);
						visitor.setStateToSaved();
					}
				}
			}
			else { //Visitor isn't drowning
				if (visitor.changedToSafe) { //Visitor stays in same spot one iteration
					visitor.changedToSafe = false;
					updateVisitorInputs(i);
					return;
				}				
				// visitor has just reached the sand with the saving lg
				else if (visitor.isBeingSaved() && visitor.onSand()) {
					//visitor can stop following lifeguard
					visitor.setStateToSafe();
					visitor.changedToSafe = true;
					for (int j=0; j<constants.L; j++) {
						if (visitor.savedBy() == j) {
							onBoat[j] -= 1;
							visitor.move(visitors,lifeguards[j].x,lifeguards[j].y,true);
							break;
						}
					}
					lifeguards[visitor.savedBy()].is_saving = false;
					updateVisitorInputs(i);
					
					return;
				}
				else if (visitor.isBeingSaved()) {
					//visitor must follow lifeguard
					for (int j=0; j<constants.L; j++) {
						if (visitor.savedBy() == j) {
							visitor.move(visitors,lifeguards[j].x, lifeguards[j].y,true);
							break;
						}
					}
					updateVisitorInputs(i);
					return;
				}
				visitor.move(visitors,-1,-1,false);
				visitor.randomDrown(risk_of_drowning);						
			}
			updateVisitorInputs(i);
		}
		
		private void checkVisitorsSameLocation() {
			v0_and_v2_together = false;
			v1_and_v3_together = false;
			if (visitors[0] == null || visitors[1] == null || visitors[2] == null || visitors[3] == null){
				return;
			}
			if (visitors[0].x == visitors[2].x && visitors[0].y == visitors[2].y) {
				if(visitors[0].isBeingSaved() && visitors[2].isBeingSaved()) {
					v0_and_v2_together = true;
				}
				else if(visitors[0].onSand()) {// Both visitors are on the same spot on the shore
					v0_and_v2_together = true;
				}
			}
			if (visitors[1].x == visitors[3].x && visitors[1].y == visitors[3].y) {
				if(visitors[1].isBeingSaved() && visitors[3].isBeingSaved()) {
					v1_and_v3_together = true;
				}
				else if(visitors[1].onSand()) {// Both visitors are on the same spot on the shore
					v1_and_v3_together = true;
				}
			}
		}
		
		private void paint_flags(Graphics g) {
			BufferedImage flagImgRight;
			BufferedImage flagImgLeft;
			
			if (flag1Color == FlagColor.WHITE) {
				flagImgLeft = img.flag_l_w;
			}
			else if(flag1Color == FlagColor.RED) {
				flagImgLeft = img.flag_l_r;	
			}
			else {
				flagImgLeft = img.flag_l_b;	
			}
			if (flag2Color == FlagColor.WHITE) {
				flagImgRight = img.flag_r_w;
			}
			else if(flag2Color == FlagColor.RED) {
				flagImgRight = img.flag_r_r;
			}
			else {
				flagImgRight = img.flag_r_b;
			}

			g.drawImage(flagImgRight, (int)(7.5*constants.IMG_DIM), (int)(7.75*constants.IMG_DIM), constants.IMG_DIM,constants.IMG_DIM, this);
			g.drawImage(flagImgLeft, (int)(1.5*constants.IMG_DIM), (int)(7.75*constants.IMG_DIM), constants.IMG_DIM,constants.IMG_DIM, this);
		}
		
		private void paint_weather(Graphics g) {
			switch (weather) {
				case SUN:
					g.drawImage(img.sun, 0, 0, background_img_size,background_img_size, this);
					break;
				case CLOUDS:
					g.drawImage(img.clouds, 0, 0, background_img_size,background_img_size, this);
					break;
				case STORM:
					g.drawImage(img.storm, 0, 0, background_img_size,background_img_size, this);
			}
		}
		
		private void paint_background(Graphics g) {
			switch (weather) {
				case STORM:
					g.drawImage(img.background_storm, 0, 0, background_img_size,background_img_size, this);
					break;
				default:
					g.drawImage(img.background_img, 0, 0, background_img_size,background_img_size, this);
			}
		}
		
		private void paint_visitors(Graphics g) {
			checkVisitorsSameLocation();
			if (v0_and_v2_together) {
				if(visitors[0].isBeingSaved()) {
					int savingLifeguardNum = visitors[0].savedBy();
					if (lifeguards[savingLifeguardNum].y >= constants.SHORE_Y) {
						g.drawImage(img.lg_visitors_together[0][0],visitors[0].x*constants.IMG_DIM,visitors[0].y*constants.IMG_DIM,constants.IMG_DIM,constants.IMG_DIM,null);
					}
					//else, lifeguard is painted with visitors on boat
				}
				else {
					g.drawImage(img.lg_visitors_together[0][0],visitors[0].x*constants.IMG_DIM,visitors[0].y*constants.IMG_DIM,constants.IMG_DIM,constants.IMG_DIM,null);
				}
			}
			else { //draw visitors 0 and 2 separately
				paint_visitor(g,visitors[0],0);
				paint_visitor(g,visitors[2],2);
			}
			
			if (v1_and_v3_together) {
				if(visitors[1].isBeingSaved()) {
					int savingLifeguardNum = visitors[1].savedBy();
					if (lifeguards[savingLifeguardNum].y >= constants.SHORE_Y) {
						g.drawImage(img.lg_visitors_together[1][0],visitors[1].x*constants.IMG_DIM,visitors[1].y*constants.IMG_DIM,constants.IMG_DIM,constants.IMG_DIM,null);
					}
					//else, lifeguard is painted with visitors on boat
				}
				else {
					g.drawImage(img.lg_visitors_together[1][0],visitors[1].x*constants.IMG_DIM,visitors[1].y*constants.IMG_DIM,constants.IMG_DIM,constants.IMG_DIM,null);
				}
			}
			else { //draw visitors 1 and 3 separately
				paint_visitor(g,visitors[1],1);
				paint_visitor(g,visitors[3],3);
			}
		}
		
		private void paint_visitor(Graphics g,Visitor visitor,int visitorNum) {
			if (visitor == null) {
				return;
			}
			short visitor_dir = (short)visitor.directionNumber();
			if(visitor.isBeingSaved()) {
				int SavingLifeguardNumber = visitor.savedBy();
				if (lifeguards[SavingLifeguardNumber].y >= constants.SHORE_Y) {
					g.drawImage(img.visitor_walk[visitorNum][visitor_dir],visitor.x*constants.IMG_DIM,visitor.y*constants.IMG_DIM,constants.IMG_DIM,constants.IMG_DIM,null);
				}
				//else, lifeguard is painted with visitor on boat
			}
			else if(visitor.isDrowning()) { //visitor is drowning
				g.drawImage(img.visitor_drown[visitorNum],visitor.x*constants.IMG_DIM,visitor.y*constants.IMG_DIM,constants.IMG_DIM,constants.IMG_DIM,null);	
			}
			else if(visitor.y >= constants.SHORE_Y - 1) { //visitor is walking
				g.drawImage(img.visitor_walk[visitorNum][visitor_dir],visitor.x*constants.IMG_DIM,visitor.y*constants.IMG_DIM,constants.IMG_DIM,constants.IMG_DIM,null);
			}
			else { //visitor is swimming
				g.drawImage(img.visitor_swim[visitorNum][visitor_dir],visitor.x*constants.IMG_DIM,visitor.y*constants.IMG_DIM,constants.IMG_DIM,constants.IMG_DIM,null);
			}
		}
		
		public void paint_lifeguard(Graphics g,int LifeguardNum) {
			Lifeguard lifeguard = lifeguards[LifeguardNum];
			if (lifeguard.is_saving && lifeguard.y < constants.SHORE_Y) { //Lifeguard is currently with one or more visitors on his boat
				if(onBoat[LifeguardNum] == 2) {
					g.drawImage(img.lg_boat_2vis[LifeguardNum][0],lifeguard.x*constants.IMG_DIM,lifeguard.y*constants.IMG_DIM,constants.IMG_DIM,constants.IMG_DIM,null);
					lifeguards[LifeguardNum].is_saving_both_visitors = true;
				}
				else {//Lifeguard is saving exactly one visitor. check which one
					for(int v=0;v<constants.V;v++) {
						if (visitors[v].isBeingSaved() && (visitors[v].savedBy() == LifeguardNum)) {
							short visitor_dir = (short)visitors[v].directionNumber();
							g.drawImage(img.visitor_boat[v][visitor_dir],lifeguard.x*constants.IMG_DIM,lifeguard.y*constants.IMG_DIM,constants.IMG_DIM,constants.IMG_DIM,null);
							break;
						}
					}
				}
			}
			else { //Lifeguard is not with a visitor on his boat
				if (lifeguards[LifeguardNum].x == constants.TOWERS_X[LifeguardNum] && lifeguards[LifeguardNum].y == constants.TOWERS_Y) {
					// Lifeguard is in tower, no need to paint him
					return;
				}
				else if (lifeguards[LifeguardNum].y < constants.SHORE_Y) { // Lifeguard is in water, paint with boat
					if (lifeguards_dir[LifeguardNum] == 1) {
						img.lifeguard_img = img.boat_lg_u;
					}
					else {
						img.lifeguard_img = img.boat_lg_d;
					}
					g.drawImage(img.lifeguard_img, lifeguards[LifeguardNum].x*constants.IMG_DIM, lifeguards[LifeguardNum].y*constants.IMG_DIM, constants.IMG_DIM, constants.IMG_DIM, null);
				}
				else {
					if (lifeguards_dir[LifeguardNum] == 1) { // Lifeguard is on shore, paint walking
						img.lifeguard_img = img.lifeguard_w_u;
					}
					else {
						img.lifeguard_img = img.lifeguard_w_d;
					}
					g.drawImage(img.lifeguard_img, lifeguards[LifeguardNum].x*constants.IMG_DIM, lifeguards[LifeguardNum].y*constants.IMG_DIM, constants.IMG_DIM, constants.IMG_DIM, null);
				}
			}
		}
		
		private void paint_tsunami_warnings(Graphics g) {
			if (lifeguard1_TsunamiWarning) {
				g.drawImage(img.lg1_TsunamiWarning,(constants.LADDERS_X[0]-1)*constants.IMG_DIM,(constants.LADDERS_Y)*constants.IMG_DIM,constants.IMG_DIM,constants.IMG_DIM,null);
			}
			if (lifeguard2_TsunamiWarning) {
				g.drawImage(img.lg2_TsunamiWarning,(constants.LADDERS_X[1]+1)*constants.IMG_DIM,(constants.LADDERS_Y)*constants.IMG_DIM,constants.IMG_DIM,constants.IMG_DIM,null);
			}
			
		}
		
		public void paint(Graphics g) {
			paint_background(g);	
			paint_flags(g);
			paint_visitors(g);
			
			// paint lifeguards
			for (int i=0; i<constants.L; i++) {
				if(lifeguards[i]==null) {
					continue;
				}
				paint_lifeguard(g,i);
			}
			
			paint_tsunami_warnings(g);
			paint_weather(g);
		}

		public void paint_tsunami(int j,Graphics g) {
			g.drawImage(img.Tsunami_backgrounds[j], 0, 0, background_img_size,background_img_size, this);
			paint_visitors(g);
			// paint lifeguards
			for (int i=0; i<constants.L; i++) {
				if(lifeguards[i]==null) {
					continue;
				}
				paint_lifeguard(g,i);
			}
			paint_flags(g);
			paint_tsunami_warnings(g);
			paint_weather(g);
		}
		
		public void Tsunami_Animation(Map<String, String> sysValues,Graphics g) throws Exception {
			executor.updateState(inputs);
			sysValues = executor.getCurrOutputs();
			paint(g);
			Thread.sleep(700);
			for(int j=1;j<6;j++) {
				for(int v=0;v<constants.V;v++) {
					Visitor visitor = visitors[v];
					visitor.move(visitors, -1, constants.Y-1, false);
					updateVisitorInputs(v);
				}
				executor.updateState(inputs);
				sysValues = executor.getCurrOutputs();
							
				for (int i=0; i<constants.L; i++) {
					Prev_LG_y[i] = lifeguards[i].y;
					lifeguards[i].x = Integer.parseInt(sysValues.get("lifeguard"+(i+1)+"[0]"));
					lifeguards[i].y = Integer.parseInt(sysValues.get("lifeguard"+(i+1)+"[1]"));
				}
				for (int i=0; i<constants.L; i++) {
					
					if (Prev_LG_y[i] - lifeguards[i].y == -1) {
						lifeguards_dir[i] = 2;
					}
					else if (Prev_LG_y[i] - lifeguards[i].y == 1) {
						lifeguards_dir[i] = 1;
					}
				}
				
				lifeguard1_TsunamiWarning=Boolean.parseBoolean(sysValues.get("lifeguard1_TsunamiWarning")); //
				lifeguard2_TsunamiWarning=Boolean.parseBoolean(sysValues.get("lifeguard2_TsunamiWarning")); //
				flag1Color= getFlagColor(sysValues.get("flag1"));
				flag2Color= getFlagColor(sysValues.get("flag2"));
				paint_tsunami(j,g);
				Thread.sleep(700);

			}
			for(int j=4;j>=0;j--) {
				for(int v=0;v<constants.V;v++) {
					Visitor visitor = visitors[v];
					visitor.move(visitors, -1, constants.Y-1, false);
					updateVisitorInputs(v);
				}
				executor.updateState(inputs);
				sysValues = executor.getCurrOutputs();
							
				for (int i=0; i<constants.L; i++) {
					Prev_LG_y[i] = lifeguards[i].y;
					lifeguards[i].x = Integer.parseInt(sysValues.get("lifeguard"+(i+1)+"[0]"));
					lifeguards[i].y = Integer.parseInt(sysValues.get("lifeguard"+(i+1)+"[1]"));
				}
				for (int i=0; i<constants.L; i++) {
					
					if (Prev_LG_y[i] - lifeguards[i].y == -1) {
						lifeguards_dir[i] = 2;
					}
					else if (Prev_LG_y[i] - lifeguards[i].y == 1) {
						lifeguards_dir[i] = 1;
					}
				}
				
				lifeguard1_TsunamiWarning=Boolean.parseBoolean(sysValues.get("lifeguard1_TsunamiWarning"));
				lifeguard2_TsunamiWarning=Boolean.parseBoolean(sysValues.get("lifeguard2_TsunamiWarning"));
				flag1Color= getFlagColor(sysValues.get("flag1"));
				flag2Color= getFlagColor(sysValues.get("flag2"));
				paint_tsunami(j,g);
				Thread.sleep(700);

			}
		}
		
		
		public static JPanel createHeadLinePanel()
		{
			JPanel headPanel = new JPanel(); 
			JLabel headLineLabel = new JLabel("<html><span style='font-size:20px'>Beach Life Simulator</span></html>");
	        headPanel.add(headLineLabel);
			return headPanel;
		}
		
		public static JPanel createEventsPanel(BeachLifeMain beach)
		{
			JPanel eventsPanel 						= new JPanel(new BorderLayout());
			JPanel eventsPanelTop 					= new JPanel(new BorderLayout());
			JPanel eventsPanelTopwithLabel			= new JPanel(new BorderLayout());
			JPanel eventsPanelButtom			  	= new JPanel(new BorderLayout());
			JPanel eventsPanelButtomWeather		 	= new JPanel(new BorderLayout());
			JPanel weatherModePanelWithLabel		= createWeatherModesPanel();
			JPanel headLinePanel 					= new JPanel();
			JPanel GregPanel 						= new JPanel();
			JPanel BobPanel 						= new JPanel();
			JPanel PeterPanel 						= new JPanel();
			JPanel RachelPanel                       = new JPanel();
			JPanel buttonPanel 						= new JPanel();
			
			String[] visitors_states 	= { "-----", "IN SHORE", "IN SEA", "DROWNING"};
			
			JComboBox<String> visitors_statesListGreg 	= new JComboBox<>(visitors_states);
			JComboBox<String> visitors_statesListBob 	= new JComboBox<>(visitors_states);
			JComboBox<String> visitors_statesListPeter 	= new JComboBox<>(visitors_states);
			JComboBox<String> visitors_statesListRachel 	= new JComboBox<>(visitors_states);
			
			//Greg - green swimsuit, Bob - blue swimsuit, Peter - purple swimsuit, Rachel - red bikini
			JLabel GregLabel = new JLabel("<html><span style='font-size:10px'>Greg: </span></html>");
			JLabel BobLabel = new JLabel("<html><span style='font-size:10px'>Bob: </span></html>");
			JLabel PeterLabel = new JLabel("<html><span style='font-size:10px'>Peter: </span></html>");
			JLabel RachelLabel = new JLabel("<html><span style='font-size:10px'>Rachel: </span></html>");
			JLabel headLineLabel = new JLabel("<html><span style='font-size:14px'>Set Visitors States </span></html>");
			
			GregPanel .add(GregLabel);
			GregPanel .add(visitors_statesListGreg);
			
			BobPanel .add(BobLabel);
			BobPanel .add(visitors_statesListBob);
			
			PeterPanel .add(PeterLabel);
			PeterPanel .add(visitors_statesListPeter);
			
			RachelPanel .add(RachelLabel);
			RachelPanel .add(visitors_statesListRachel);
			
			headLinePanel.add(headLineLabel);
			
			JButton eventOkButton = new JButton("Start");  
	        eventOkButton.addActionListener(new ActionListener(){  
	        	public void actionPerformed(ActionEvent e) { 
	        	
	        		sendVsitorsToLocations(visitors_statesListGreg.getSelectedItem().toString(), visitors_statesListBob.getSelectedItem().toString(), visitors_statesListPeter.getSelectedItem().toString(),visitors_statesListRachel.getSelectedItem().toString());
	        		
	        	}  
	        });
	        buttonPanel.add(eventOkButton);
	        
	        eventsPanelTop.add(GregPanel, BorderLayout.NORTH);
	        eventsPanelTop.add(BobPanel, BorderLayout.SOUTH);
	        
	        eventsPanelTopwithLabel.add(headLinePanel, BorderLayout.NORTH);
	        eventsPanelTopwithLabel.add(eventsPanelTop, BorderLayout.SOUTH);
	        
	        eventsPanelButtom.add(PeterPanel, BorderLayout.NORTH);
	        eventsPanelButtom.add(RachelPanel, BorderLayout.CENTER);
	        eventsPanelButtom.add(buttonPanel, BorderLayout.SOUTH);
	        
	        eventsPanelButtomWeather.add(eventsPanelButtom, BorderLayout.NORTH);
	        eventsPanelButtomWeather.add(weatherModePanelWithLabel, BorderLayout.SOUTH);
	        
	        eventsPanel.add(eventsPanelTopwithLabel, BorderLayout.NORTH);
	        eventsPanel.add(eventsPanelButtomWeather, BorderLayout.SOUTH);
	        
	        return eventsPanel; 
		}
		
		public static JPanel createControlPanel(BeachLifeMain beach)
		{
			JPanel controlPanel 					= new JPanel(new BorderLayout());
			JPanel controlPanelHeadAndEvents 		= new JPanel(new BorderLayout());
			JPanel controlPanelScenarions		 	= new JPanel(new BorderLayout());
			JPanel outputPanelAndLabel			 	= new JPanel(new BorderLayout());
			JPanel headPanel 						= createHeadLinePanel(); 
			JPanel eventsPanel 						= createEventsPanel(beach); 
			JPanel scenariosPanel 					= createScenariosPanel(beach); 
			JPanel outputPanel						= new JPanel();
			JPanel outputLabelPanel					= new JPanel();
			JLabel outputLabel = new JLabel("<html><span style='font-size:14px'>Output </span></html>");
			
			outputLabelPanel.add(outputLabel);
			outputPanel.add(outputArea);
			outputPanelAndLabel.add(outputLabelPanel, BorderLayout.NORTH);
			outputPanelAndLabel.add(outputPanel, BorderLayout.CENTER);

			controlPanelHeadAndEvents.add(headPanel, BorderLayout.NORTH);
	        controlPanelHeadAndEvents.add(eventsPanel, BorderLayout.SOUTH);
	        
	        controlPanelScenarions.add(scenariosPanel, BorderLayout.NORTH);
	        controlPanelScenarions.add(outputPanelAndLabel, BorderLayout.CENTER);
	        
	        controlPanel.add(controlPanelHeadAndEvents, BorderLayout.NORTH);
	        controlPanel.add(controlPanelScenarions, BorderLayout.CENTER);
			
			return controlPanel;
		}
		
		public static JSplitPane createMainPanel(BeachLifeMain beach)
		{
			JPanel controlPanel = createControlPanel(beach);
			JSplitPane splitPanel = new JSplitPane(SwingConstants.VERTICAL, beach, controlPanel);
			splitPanel.setDividerLocation(800); 
			return splitPanel;
		}
		
		public static JPanel createWeatherModesPanel()
		{
			String[] modesNames = { "Sunny", "Clouds", "Storm"};
			
			JPanel weatherModePanelWithLabel 	= new JPanel(new BorderLayout());
			JPanel weatherModePanel			= new JPanel();
			JPanel weatherModeLabelPanel		= new JPanel();
			
			JLabel flagModeLabel = new JLabel("<html><span style='font-size:14px'>Set Weather </span></html>");
	        
			JComboBox<String> modesList	= new JComboBox<>(modesNames);
			
			JButton modeOkButton = new JButton("Apply");  
	        modeOkButton.addActionListener(new ActionListener(){  
	        	public void actionPerformed(ActionEvent e) {
	        		if(ScenarioRunning)
	        			return;
				String new_mode = modesList.getSelectedItem().toString();
	        		if (new_mode == "Sunny") {
	        			outputArea.setText("Weather changed to Sunny \n");
	        			risk_of_drowning = constants.RISK_OF_DROWNING;
	        			weather = Weather.SUN;

	        		}
	        		if (new_mode == "Clouds") {
	        			outputArea.setText("Weather changed to Cloudy \n");
						risk_of_drowning = constants.RISK_OF_DROWNING_LIGHT_STORM;
						weather = Weather.CLOUDS;	        			
	        		}
	        		if (new_mode == "Storm") {
	        			outputArea.setText("Weather changed to Storm \n");
	        			risk_of_drowning = constants.RISK_OF_DROWNING_HEAVY_STORM;
						weather = Weather.STORM;
	        		}
	        		weather_change_count = 0;
	            } 
	        });
	        
	        weatherModeLabelPanel.add(flagModeLabel);
	        weatherModePanel.add(modesList);
	        weatherModePanel.add(modeOkButton);
	        
	        weatherModePanelWithLabel.add(weatherModeLabelPanel, BorderLayout.NORTH);
	        weatherModePanelWithLabel.add(weatherModePanel, BorderLayout.SOUTH);
			
			return weatherModePanelWithLabel;
		}

		public static JPanel createScenariosPanel(BeachLifeMain beach)
		{
			String[] scenarioNames 	= { "Tsunami", "Everyone drowns at the same time", "Rotational Drowning", "Exactly 2 drown on the same side", "Unstable weather"};
			
			JPanel headLinePanel 	 = new JPanel();
			JPanel scenariosPanel 	 = new JPanel(); 
			JPanel scenariosMainPanel = new JPanel(new BorderLayout());
			
			JLabel headLineLabel = new JLabel("<html><span style='font-size:14px'>Scenarios </span></html>");
			
			headLinePanel.add(headLineLabel);
			
			JComboBox<String> scenatioList 	= new JComboBox<>(scenarioNames);
			
			JButton ScenariosButton = new JButton("Start");  
	        ScenariosButton.addActionListener(new ActionListener(){  
	        	public void actionPerformed(ActionEvent e) {  
	        		if(ScenarioRunning)
	        			return;
	        		doScenario=true;
	        		ScenarioRunning = true;
	        		scenario = scenatioList.getSelectedItem().toString();
	        		if (scenario == "Everyone drowns at the same time")
	        			Scenarios.done_scenario2 = false;
	        		if (scenario == "Rotational Drowning")
	        			Scenarios.next_drown_scenario3 = -1;
	        		if (scenario == "Exactly 2 drown on the same side") 
	        			Scenarios.level_scenario4 = 0;
	        		
	            }  
	        });
	        
	        scenariosPanel.add(scenatioList);
	        scenariosPanel.add(ScenariosButton);
	        
	        scenariosMainPanel.add(headLinePanel, BorderLayout.NORTH);
	        scenariosMainPanel.add(scenariosPanel, BorderLayout.CENTER);
	        
			return scenariosMainPanel;
		}
		
		
		static boolean array_boolean_check(boolean[] arr) {
			for (int i=0; i<arr.length; i++) {
				if (arr[i]==true) {
					return true;
				}
			}
			return false;
		}

		public static void sendVsitorsToLocations(String Greg, String Bob, String Peter, String Rachel) {
			if(ScenarioRunning || (Greg == "-----" && Bob == "-----" && Peter == "-----" && Rachel == "-----")) {
				return;
			}
			ScenarioRunning = true;
			outputArea.setText("Start moving  visitors: \n");
			if(Greg != "-----")
				outputArea.setText(outputArea.getText() + "Greg will be " + Greg + "\n");
			if(Bob != "-----")
				outputArea.setText(outputArea.getText() + "Bob will be " + Bob + "\n");
			if(Peter!= "-----")
				outputArea.setText(outputArea.getText() + "Peter will be " + Peter + "\n");
			if(Rachel!= "-----")
				outputArea.setText(outputArea.getText() + "Rachel will be " + Rachel + "\n");
			semiAuto=true;
			missions[0] = Greg;
			missions[1] = Bob;
			missions[2] = Peter;
			missions[3] = Rachel;
		}
		
		public static FlagColor getFlagColor(String flagcolor) throws Exception
		{
				FlagColor color=FlagColor.valueOf(flagcolor);
				return color;
		}
		
		public void setInitialValues() {
			for(int i=0;i<constants.V;i++) {
				Visitor visitor = visitors[i];
				if (i==0) {
					visitor.move(visitors,3,1,false);
					visitor.aboutToDrown = true;
				}
				else if(i==2) {
					visitor.move(visitors,1,9,false);
				}
				else if (i == 1) {
					visitor.move(visitors,7,1,false);
				}
				else {
					visitor.move(visitors,6,3,false);
				}
				
			}
		}
		
		public static void updateVisitorInputs(int i) {
			inputs.put("visitor["+i+"][0]", Integer.toString(visitors[i].x));
			inputs.put("visitor["+i+"][1]", Integer.toString(visitors[i].y));
			inputs.put("state["+i+"]", visitors[i].state.toString());
		}
		
		public static void main(String args[]) throws Exception {
			JFrame Beach = new JFrame("Beach Life Simulator");
			BeachLifeMain beach = new BeachLifeMain();
			Beach.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			Beach.setSize(1200, 850);
			Beach.setContentPane(createMainPanel(beach));
			Beach.setVisible(true);
			beach.run();
		}


	}
	
