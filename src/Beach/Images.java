package Beach;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import javax.swing.JFrame;

public class Images extends JFrame{
	Constants constants = new Constants();
	int n = constants.V;
	
	//Background and Weather
	BufferedImage background_img, background_storm;
	BufferedImage[] Tsunami_backgrounds = new BufferedImage[6];
	BufferedImage lg1_TsunamiWarning, lg2_TsunamiWarning;
	BufferedImage clouds,sun,storm;
	
	//Lifeguards
	BufferedImage lifeguard_w_d;
	BufferedImage lifeguard_w_u;
	BufferedImage lifeguard_img;
	
	//Visitors
	BufferedImage[][] lg_visitors_together =  new BufferedImage[2][1];
	BufferedImage[][] visitor_walk = new BufferedImage[n][9]; //first index is the visitor number, second is the direction
	BufferedImage[][] visitor_swim = new BufferedImage[n][9]; //first index is the visitor number, second is the direction
	BufferedImage[][] visitor_boat = new BufferedImage[n][9]; //first index is the visitor number, second is the direction
	BufferedImage[] visitor_drown = new BufferedImage[n];

	//Flags
	BufferedImage flag_r_w;
	BufferedImage flag_l_w;
	BufferedImage flag_r_r;
	BufferedImage flag_l_r;
	BufferedImage flag_r_b;
	BufferedImage flag_l_b;

	//Boats
	BufferedImage boat_v_u;
	BufferedImage boat_lg_u;
	BufferedImage boat_lg_d;
	BufferedImage boat_v_dr;
	BufferedImage boat_v_dl;
	BufferedImage boat_v_ur;
	BufferedImage boat_v_ul;
	BufferedImage boat_v_r;
	BufferedImage boat_v_l;
	BufferedImage[][] lg_boat_2vis = new BufferedImage[2][1];

	/*Loads images from img folder*/
	public void load() throws Exception{
		//Visitors 
		for(int i=0;i<n;i++) {
			int tmp = i % 4;
			String image_path = "img/Visitors/visitor" + tmp + "_drown" + ".png";
			visitor_drown[i] = ImageIO.read(new File(image_path));
			for(int j=0;j<9;j++) { //8 different moving directions + in place.
				image_path = "img/Visitors/visitor" + tmp + "_w" + j + ".png";
				visitor_walk[i][j] = ImageIO.read(new File(image_path));
				image_path = "img/Visitors/visitor" + tmp + "_s" + j + ".png";
				visitor_swim[i][j] = ImageIO.read(new File(image_path));
				image_path = "img/Boats/visitor" + tmp + "_b3" + ".png";
				visitor_boat[i][j] = ImageIO.read(new File(image_path));
			}
		}
		lg_visitors_together[0][0] = ImageIO.read(new File("img/Visitors/lg1_2v_w3.png"));
		lg_visitors_together[1][0] = ImageIO.read(new File("img/Visitors/lg2_2v_w3.png"));
		
		//Backgrounds and weather
		background_img = ImageIO.read(new File("img/Background/background_sun.jpg"));
		background_storm = ImageIO.read(new File("img/Background/background_storm.jpg"));
		clouds = ImageIO.read(new File("img/Weather/clouds.png"));
		sun = ImageIO.read(new File("img/Weather/sun.png"));
		storm = ImageIO.read(new File("img/Weather/storm.png"));
		
		//Tsunami
		Tsunami_backgrounds[0] = background_storm;
		for(int i=1;i<Tsunami_backgrounds.length;i++) {
			String img_path = "img/Background/bg_Tsunami_stage" + i + ".jpg";
			Tsunami_backgrounds[i] = ImageIO.read(new File(img_path));
		}
		lg1_TsunamiWarning = ImageIO.read(new File("img/Weather/lg1_TsunamiWarning.png"));
		lg2_TsunamiWarning = ImageIO.read(new File("img/Weather/lg2_TsunamiWarning.png"));

		//Lifeguards
		lifeguard_w_d = ImageIO.read(new File("img/Lifeguard/lg_w_d.png"));
		lifeguard_w_u = ImageIO.read(new File("img/Lifeguard/lg_w_u.png"));
		lifeguard_img = lifeguard_w_u;

		//Flags
		flag_r_w = ImageIO.read(new File("img/Flags/flag_r_w.png"));
		flag_l_w = ImageIO.read(new File("img/Flags/flag_l_w.png"));
		flag_r_r = ImageIO.read(new File("img/Flags/flag_r_r.png"));
		flag_l_r = ImageIO.read(new File("img/Flags/flag_l_r.png"));
		flag_r_b = ImageIO.read(new File("img/Flags/flag_r_b.png"));
		flag_l_b = ImageIO.read(new File("img/Flags/flag_l_b.png"));
		
		//Boats
		boat_lg_u = ImageIO.read(new File("img/Lifeguard/boat_lg_u.png"));
		boat_lg_d = ImageIO.read(new File("img/Lifeguard/boat_lg_d.png"));
		lg_boat_2vis[0][0] = ImageIO.read(new File("img/Boats/lg1_2v_b3.png"));
		lg_boat_2vis[1][0] = ImageIO.read(new File("img/Boats/lg2_2v_b3.png"));
		
		
	}
	
	
	public BufferedImage visitorImage(int visitorNumber,boolean walking,boolean swimming, boolean drowning, boolean onBoat,int visitorDirection) {
		if (swimming) {
			return visitor_swim[visitorNumber][visitorDirection];
		}
		else if(drowning) {
			return visitor_drown[visitorNumber];
		}
		else if(walking) {
			return visitor_walk[visitorNumber][visitorDirection];
		}
		else if(onBoat) {
			return visitor_boat[visitorNumber][visitorDirection];
		}
		return visitor_walk[visitorNumber][0];
	}
	
	
	
}
