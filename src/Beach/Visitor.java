package Beach;


import java.util.Random;

public class Visitor{
	
	enum Direction{
		INPLACE(0),
		LEFT(1),
		RIGHT(2),
		DOWN(3),
		DOWNLEFT(4),
		DOWNRIGHT(5),
		UP(6),
		UPLEFT(7),
		UPRIGHT(8);
		
		private int direction_num;
		
		Direction(int direction_number){
			this.direction_num = direction_number;
		}
		
		public int getDirectionNumber() {
			return direction_num;
		}
	}
	
	enum State{
		SAFE,
		SAVED,
		DROWNING
	}
	
	Random rand;
	int x;
	int y;
	private int savedBy;
	int visitor_number;
	Direction dir;
	State state;
	Constants constants;
	
	boolean aboutToDrown;
	boolean changedToSafe;
	boolean newVisitor;
	
	// visitor builder
	public Visitor(int v_number) {
		super();
		rand = new Random();
		x = 0;
		y = 0;
		visitor_number = v_number;
		dir = Direction.INPLACE;
		state = State.SAFE;
		aboutToDrown = false;
		constants = new Constants();
		changedToSafe = false;
		newVisitor = true;
	}
	
	public void setSavedBy(int lg_number) {
		savedBy = lg_number;
	}
	
	public int savedBy() {
		return savedBy;
	}
	
	public void setStateToDrowning() {
		state = State.DROWNING;
		aboutToDrown = false;
		dir = Direction.INPLACE;
	}
	
	public void setStateToSaved() {
		state = State.SAVED;
	}
	
	public void setStateToSafe() {
		state = State.SAFE;
	}
	
	public boolean isDrowning() {
		if (state == State.DROWNING) {
			return true;
		}
		return false;
	}
	
	public boolean isBeingSaved() {
		if (state == State.SAVED) {
			return true;
		}
		return false;
	}
	
	public boolean onSand() {
		if (y >= constants.SHORE_Y) {
			return true;
		}
		return false;
	}
	
	public boolean aboutToDrown() {
		return aboutToDrown;
	}
	
	public Direction direction() {
		return dir;
	}
	
	public String state() {
		return state.toString();
	}
	
	public boolean inTower1(int x,int y) {
		if (x == constants.TOWERS_X[0] && y == constants.TOWERS_Y) {
			return true;
		}
		return false;
	}
	
	public boolean inTower2(int x,int y) {
		if (x == constants.TOWERS_X[1] && y == constants.TOWERS_Y) {
			return true;
		}
		return false;
	}
	
	public boolean inLadder1(int x,int y) {
		if (x == constants.LADDERS_X[0] && y == constants.LADDERS_Y) {
			return true;
		}
		return false;
	}
	
	public boolean inLadder2(int x,int y) {
		if (x == constants.LADDERS_X[1] && y == constants.LADDERS_Y) {
			return true;
		}
		return false;
	}
	
	public boolean inSea() {
		if (y < constants.SHORE_Y - 1) {
			return true;
		}
		return false;
	}
	

	public void randomDrown(int currentRiskOfDrowning) {
		if ((this.visitor_number%2 == 0) &&  (this.state == State.SAFE)) {
			for (int j=0; j<constants.DROWNING_POSITIONS1.length; j++) {
				if(this.x == constants.DROWNING_POSITIONS1[j][0] && this.y == constants.DROWNING_POSITIONS1[j][1]) {
					int rand_drown = rand.nextInt(10);
					if (rand_drown <= currentRiskOfDrowning) {
						aboutToDrown = true;
					}
				}
			}
		}
		if ((this.visitor_number%2 == 1) &&  (this.state == State.SAFE)) {
			for (int j=0; j<constants.DROWNING_POSITIONS2.length; j++) {
				if(this.x == constants.DROWNING_POSITIONS2[j][0] && this.y == constants.DROWNING_POSITIONS2[j][1]) {
					int rand_drown = rand.nextInt(10);
					if (rand_drown <= currentRiskOfDrowning) {
						aboutToDrown = true;
					}
				}
			}
		}
	}
	
	public int directionNumber() {
		return dir.getDirectionNumber();
	}
	
	private boolean isTargetLocationValid(int t_x, int t_y) {
		if (inTower1(t_x,t_y) || inTower2(t_x,t_y) || inLadder1(t_x,t_y) || inLadder2(t_x,t_y)) {
			return false;
		}
		else if (t_x >= constants.X || t_x < 0) {
			return false;
		}
		else if (t_y >= constants.Y || t_y < 0) {
			return false;
		}
		return true;
	}
	
	private boolean isTargetLocationEmpty(Visitor[] visitors, int t_x, int t_y) {
		for (int j=0; j<constants.V; j++) {
			if (j==visitor_number) {
				continue;
			}
			if (t_x == visitors[j].x && t_y == visitors[j].y) {
				return false;
			}
		}
		return true;
	}
	
	private void setDirectionAccordingToMove(int delta_x,int delta_y) {
		if (delta_y == 0) {// no change in y
			if (delta_x == 0) {
				dir = Direction.INPLACE;
			}
			else if (delta_x < 0) {// next_x < x
				dir = Direction.LEFT;
			}
			else { //  next_x > x
				dir = Direction.RIGHT;
			}
		}
		else if (delta_y > 0) { // next_y > y
			if (delta_x == 0) {
				dir = Direction.DOWN;
			}
			else if (delta_x < 0) { // next_x < x
				dir = Direction.DOWNLEFT;
			}
			else { // next_x > x
				dir = Direction.DOWNRIGHT;
			}
		}
		else { // next_y < y
			if (delta_x == 0) {
				dir = Direction.UP;
			}
			else if (delta_x < 0) {
				dir = Direction.UPLEFT;
			}
			else { 
				dir = Direction.UPRIGHT;
			}
		}
	}
	
	private boolean belowTower1() {
		if (x==constants.TOWERS_X[0] && y==constants.Y-1) {
			return true;
		}
		return false;
	}
	
	private boolean belowTower2() {
		if (x==constants.TOWERS_X[1] && y==constants.Y-1) {
			return true;
		}
		return false;
	}
	
	public int[] moveTowardsSpecificLocation(Visitor[] visitors,int t_x,int t_y) {
		int[] moves = {0,0};
		if (y >= constants.LADDERS_Y) { //visitor is near towers
			if (this.belowTower1() || this.belowTower2()) {
				moves[0] = 1; //move to right of tower
			}
			else {
				//move upwards
				if (isTargetLocationEmpty(visitors, x, y-1)) {
					moves[1] = -1;
				}
			}
		}
		else { //visitor is above towers and ladders
			int delta_x = t_x - x;
			int delta_y = t_y - y;
			if (delta_x < 0) {
				moves[0] = -1;
			}
			else if(delta_x > 0) {
				moves[0] = 1;
			}
			if (delta_y < 0) {
				moves[1] = -1;
			}
			else if(delta_y > 0) {
				moves[1] = 1;
			}
		}
		return moves;
		
	}
	public int[] possible_move_perm() {
		int rand_move=rand.nextInt(6);
		int [] ret_arr=new int [3];
		switch(rand_move) {
			case 0:
				ret_arr[0]=0;
				ret_arr[1]=1;
				ret_arr[2]=-1;
				return ret_arr;
			case 1:
				ret_arr[0]=0;
				ret_arr[1]=-1;
				ret_arr[2]=1;
				return ret_arr;
			case 2:
				ret_arr[0]=1;
				ret_arr[1]=0;
				ret_arr[2]=-1;
				return ret_arr;
			case 3:
				ret_arr[0]=1;
				ret_arr[1]=-1;
				ret_arr[2]=0;
				return ret_arr;
			case 4:
				ret_arr[0]=-1;
				ret_arr[1]=0;
				ret_arr[2]=1;
				return ret_arr;
			case 5:
				ret_arr[0]=-1;
				ret_arr[1]=1;
				ret_arr[2]=0;
				return ret_arr;
		}
		return ret_arr;
	}
	
	/*Receives target x and target y. If one of them equals -1 it means that it's next value can be anything of distance 1 or 0*/
	public void move(Visitor[] visitors, int t_x, int t_y, boolean followingLifeguard) {
		int[] moves = {0,0};
		boolean targetLocationValid;
		boolean targetLocationEmpty;
		
		// If new visitor, set location to t_x,t_y
		if (newVisitor) {
			newVisitor = false;
			if(isTargetLocationValid(t_x, t_y)) {
				x = t_x;
				y = t_y;
			}
			else {
				x = 0;
				y = 0;
			}
		}
		
		else if(followingLifeguard) {
			// no need to check if next spot is empty, move anyways
			moves[0] = t_x - x;
			moves[1] = t_y - y;
		}
		
		else if (t_x != -1 && t_y != -1) {// specific location, not following lg
			moves = this.moveTowardsSpecificLocation(visitors, t_x, t_y);
		}

		else if (t_x == -1 && t_y == -1) {// random move
			int rand_x = rand.nextInt(3) - 1;
			int rand_y = rand.nextInt(3) - 1;
			t_x = x + rand_x;
			t_y = y + rand_y;
			targetLocationValid = this.isTargetLocationValid(t_x,t_y);
			if (targetLocationValid) {
				targetLocationEmpty = this.isTargetLocationEmpty(visitors,t_x,t_y);
				if (targetLocationEmpty) {
					moves[0] = t_x - x;
					moves[1] = t_y - y; 
				}
			}
		}

		else if(t_y != -1) {// y is important, x doesn't matter
			// If target is further than one step, update it to a target of distance 1
			if (Math.abs(t_y - y) > 1) {
				if (t_y > y) {
					t_y = y+1;
				}
				else {
					t_y = y-1;
				}
			}
			
			int[] possible_moves = possible_move_perm();
			for (int i=0; i <3;i++) {
				int possible_x = x+possible_moves[i];
				targetLocationValid = this.isTargetLocationValid(possible_x,t_y);
				if (targetLocationValid) {
					targetLocationEmpty = this.isTargetLocationEmpty(visitors,possible_x,t_y);
					if (targetLocationEmpty) {
						moves[0] = possible_x - x;
						moves[1] = t_y - y;
						break;
					}
				}
			}
		}
		else { // x is important, y doesn't matter
			if (Math.abs(t_x - x) > 1) {
				if (t_x > x) {
					t_x = x+1;
				}
				else {
					t_x = x-1;
				}
			}
			int[] possible_moves = possible_move_perm();
			for (int i=0; i < 3;i++) {
				int possible_y = y+possible_moves[i];
				targetLocationValid = this.isTargetLocationValid(t_x,possible_y);
				if (targetLocationValid) {
					targetLocationEmpty = this.isTargetLocationEmpty(visitors,t_x,possible_y);
					if (targetLocationEmpty) {
						moves[0] = t_x - x;
						moves[1] = possible_y - y; 
						break;
					}
				}
			}
		}
		this.setDirectionAccordingToMove(moves[0],moves[1]);
		this.x += moves[0];
		this.y += moves[1];
	}
}
