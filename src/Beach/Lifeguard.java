package Beach;

public class Lifeguard {
	
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
	int x;
	int y;
	Direction dir;
	boolean is_saving;
	boolean is_saving_both_visitors;
	
	// Builder
	public Lifeguard(int start_x, int start_y) {
		x = start_x;
		y = start_y;
		dir = Direction.INPLACE;
		is_saving = false;
		is_saving_both_visitors = false;
	}
}
