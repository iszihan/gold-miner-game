import acm.graphics.*;     // GOval, GRect, etc.
import acm.program.*;      // GraphicsProgram
import acm.util.*;         // RandomGenerator
 
import java.applet.*;       // AudioClip
import java.awt.*;         // Color
import java.awt.event.*;  // Event handlers
import java.util.Vector;  //Vector

import javax.swing.JButton; //restart button

public class GoldMiner extends GraphicsProgram {
	public static final int ROCKSIZE= 15; //Radius of the rocks
	public static final int GOLDSIZE= 30; //Radius of the golds
	public static final int INCR = 5;  //The amount of increment
	public static final int CLAW = 50;  //The initial size of claw
	protected GOval collider=null;  //The Object that the claw is touching
	protected int periods;  // Helps to keep track of the rotate direction of claw
	protected int clawsize = CLAW; //the size of claw
	protected GLine claw; //claw object
	protected double angle; // The rotate angle of the claw
	public int state;   //State=0: claw rotates   state=1: claw extends  state=2: claw retrieves
	protected Vector<GOval> golds = new Vector<GOval>(); //vector to store golds
	protected Vector<GOval> stones = new Vector<GOval>(); //vector to store stones
	protected long starttime; 
	protected double timelimit;
	protected double timer;
	protected boolean completed; //The game is completed if the time is up or the score is over 700.
	protected int score; //The current score
	protected GLabel timerLabel = new GLabel("Time: " + timelimit);
	protected GLabel scoreLabel = new GLabel("Score: " + score);
	protected GLabel instruction = new GLabel("Click the screen first!\nPress downward key to extend the claw and HAVE FUN!!:D");
	protected GLabel nLabel;
	protected Font timelabel = new Font("timelabel",Font.PLAIN,24);
	protected Font labelscore = new Font("gamelabel",Font.PLAIN,21);
	protected Font instfont = new Font("inst",Font.BOLD,13);
	protected Font labels = new Font("gamelabel",Font.BOLD,34);
	protected AudioClip clink; //Sound played when the claw hits stones or golds.
	protected GImage earth; //Background picture
	protected GImage goldminer; //Goldminer picture
	protected JButton restart; //Click this button to restart the  game

	
// Randomly create 7 golds without overlap
public void createGold(RandomGenerator g){
	
	while(golds.size()<7){
		int gx = g.nextInt(800-2*GOLDSIZE);
		while(checkOverlapGold(gx)||checkOverlapStone(gx)){
			gx = g.nextInt(800-2*GOLDSIZE);
		}	
		int gy = g.nextInt((int)goldminer.getHeight()+CLAW,getHeight()-2*GOLDSIZE);
		int size = g.nextInt(10,GOLDSIZE);
		GOval temp = new GOval(gx,gy,2*size,2*size);
		temp.setColor(Color.yellow);
		temp.setFilled(true);
		golds.add(temp);
	}
	for(int i=0; i<golds.size();i++){
		add(golds.get(i));
	}
	
}


//Randomly create 7 stones without overlap
protected void createStones(RandomGenerator g){
	while(stones.size()<7){
		int gx = g.nextInt(0,getWidth()-2*ROCKSIZE);
		while(checkOverlapGold(gx) || checkOverlapStone(gx)){
			System.out.println(gx);
			gx = g.nextInt(0,getWidth()-2*ROCKSIZE);}
		int gy = g.nextInt((int)goldminer.getHeight()+CLAW,getHeight()-2*ROCKSIZE);
		GOval temp = new GOval(gx,gy,2*ROCKSIZE,2*ROCKSIZE);
		temp.setColor(Color.GRAY);
		temp.setFilled(true);
		stones.add(temp);	
	}
	for(int i =0; i< stones.size();i++){
		add(stones.get(i));
	}
}

//The method returns true when the x-value is too close to one of the existing stones 
private boolean checkOverlapStone(int gx) {
	if(stones.size()==0)//when no stone is created yet
		return false;
	else{
		for(int i=0; i<stones.size();i++){
			if(Math.abs(gx-stones.get(i).getX())<1.5*ROCKSIZE)
				return true;
			}
		}
		return false;
}


//The method returns true when the x-value is too close to one of the existing golds 
private boolean checkOverlapGold(int gx) {
	if(golds.size()==0)// when no gold is created yet
		return false;
	else{
		for(int i=0; i<golds.size();i++){
			if(Math.abs(gx-golds.get(i).getX())<1.5*GOLDSIZE)
				return true;
			}
		}
		return false;
	}

//This method creates the claw in the game
protected void createClaw(){
	if(claw!=null){//restart
		angle= Math.PI/3;
		clawsize= CLAW;
		periods=0;
		remove(claw);
	}
	claw = new GLine(getWidth()/2,goldminer.getHeight(),(getWidth()/2)-clawsize*Math.cos(angle),
			clawsize*Math.sin(angle)+goldminer.getHeight());
	add(claw);
	
}

//This method rotates the claw
protected void rotateClaw(){
	keepRotating();
	if(angle>Math.PI||angle<0){
		periods+=1;
	} 
}

//This method keeps the claw rotating
protected void keepRotating(){
	remove(claw);
	if(periods%2==0){
	angle+=Math.PI/180.0;
	}else{
	angle-=Math.PI/180.0;	
	}
	claw=new GLine(getWidth()/2,goldminer.getHeight(),getWidth()/2-clawsize*Math.cos(angle),
			clawsize*Math.sin(angle)+goldminer.getHeight());
	add(claw);
	
}

//This method retrieves the claw.
protected void retrieveClaw(){
	if(collider!=null){//when the claw reached an object
		retrieveOval();
	}
	if(clawsize > CLAW){
	remove(claw);
	clawsize -= INCR;
	claw = new GLine(getWidth()/2,goldminer.getHeight(),(getWidth()/2)-clawsize*Math.cos(angle),
			goldminer.getHeight()+clawsize*Math.sin(angle));
	add(claw);
	}else if(clawsize <= CLAW){
		state = 0;//returns to rotating mode
		clawsize = CLAW; //reset the claw size
	}
}

//This method retrieves the object if the claw hits stones or golds
protected void retrieveOval(){
	if(clawsize>CLAW){
	collider.move(INCR*Math.cos(angle),-1*INCR*Math.sin(angle));
	}else{
	if(golds.contains(collider)){
		golds.remove(collider);
		score+=100;
		if(score >= 700){
			createScore();
			createLabel("You win!");
		}
	}else{
		stones.remove(collider);
		score+=20;
		if(score >= 700){
			createScore();
			createLabel("You win!");
		}
		
	}
	remove(collider);
	
	}
	
}

public void extendClaw(){
	//extends the claw by INCR while checking if the claw reached stone or gold 
	remove(claw);
	clawsize += INCR;
	claw = new GLine(getWidth()/2,goldminer.getHeight(),(getWidth()/2)-clawsize*Math.cos(angle),
			clawsize*Math.sin(angle)+goldminer.getHeight());
	add(claw);
	collider=checkCollision();
	if(collider!=null){
		clink.play();
		state=2;
	}
	isWall();
}


protected void isWall(){
	//check if the claw reaches the wall
	GPoint end= claw.getEndPoint();
	if(end.getX()<=0||end.getX()>=getWidth()||end.getY()>=getHeight()
			||end.getY()<=0
			){
		state = 2;
	}
}

protected GOval checkCollision(){
	//returns the stone or gold if the claw reaches it
	GPoint end = claw.getEndPoint();
	for(int i =0; i<golds.size();i++){
		if(golds.get(i).contains(end)){
			return golds.get(i);
	}
	}
	for(int i=0; i<stones.size();i++){
		if(stones.get(i).contains(end)){
			return stones.get(i);
	}
	}
	return null;
}



public void keyPressed(KeyEvent e){
	//when the downward key is pressed, switches to extending mode 
	int keycode = e.getKeyCode();
	if(keycode == KeyEvent.VK_DOWN){
		if(state == 0)
			state = 1;
		else{};
	}
	
}


protected void createScore(){
	//initialized the score label
	if(scoreLabel!=null)
		remove(scoreLabel);
	scoreLabel = new GLabel("Score:" + score);
	scoreLabel.setFont(timelabel);
	add(scoreLabel,getWidth()-scoreLabel.getWidth(),scoreLabel.getAscent());

}

protected void createTimer(){
	//initializes timer label 
	if(timerLabel != null){
		remove(timerLabel);
	}
	if(nLabel != null){
		remove(nLabel);
	}
	timelimit = 45;
	timerLabel = new GLabel("Time:" + timelimit + "s");
	timerLabel.setFont(timelabel);
	add(timerLabel,0,timerLabel.getAscent());
}


protected void timerRun(){
	//runs the timer for a specified time range as specified in the timelimit variable. 
	timer = timelimit - (System.currentTimeMillis() - starttime)/1000;
	if(timer >= 0.0){
		remove(timerLabel);
		timerLabel = new GLabel("Time:" + timer + "s");
		timerLabel.setFont(timelabel);
		add(timerLabel,0,timerLabel.getAscent());
		} else if(timer < 0.0){
			createLabel("Time out!");
}
}


protected void createLabel(String str){
	//creates Label with designated string 
	nLabel= new GLabel(str);
	nLabel.setFont(labels);
	add(nLabel,getWidth()/2-nLabel.getWidth()/2,getHeight()/2);
}



protected void createEarth(){
	//imports the background image
	earth=new GImage("earth85.png");
	earth.setSize(800,466);
	add(earth,0,0);
}

protected void createGoldMiner(){
	//imports the gold miner image
	 goldminer= new GImage("miner.png");
	 goldminer.setSize(100,100);
	 add(goldminer,getWidth()/2-7,0);
	}



public void gameStart(){
	//keeps the game running
	timerRun();
	createScore();
	if(state == 0){
		rotateClaw();
	}else if(state == 1 ){
		extendClaw();	
		}else if(state == 2){
			retrieveClaw();
		}
}




public void actionPerformed(ActionEvent e){
	//after restart button is pressed 
	if(timer <= 0.0 ||( timer>0.0 && score>= 700)){ //only effective when the game is completed
		if(e.getActionCommand()=="restart"){
			completed = false;
			clearAll();
			init();
		}
	}
	
	
}
public void clearAll(){
	//clear the canvas
	removeAll();
	remove(nLabel);
	remove(claw);
	stones.clear();
	golds.clear();
}

public void init(){
	// initialize the canvas 
	RandomGenerator g = RandomGenerator.getInstance();
	createEarth();
	createGoldMiner();
	createGold(g);
	createStones(g);
	createClaw();
	createTimer();
	restart = new JButton("restart");
	add(restart,NORTH);
	instruction.setFont(instfont);;
	add(instruction,getWidth()/5,instruction.getAscent());
	addActionListeners();
	score = 0;
	state = 0;
	completed = false;
	starttime = System.currentTimeMillis();
}

public void run(){
	setSize(800,466);
	addKeyListeners();
	clink=getAudioClip(getCodeBase(), "CLINK.au"); 
	while(true){
		System.out.println(completed); 
		if(completed == false){
			gameStart();
			pause(10);
		}
		if(timer < 0.0){ //when time run out 
			completed = true; 
		}
		if(score >= 700){ // when score reaches the goal
			completed = true; 
		}
	}
    } 
	}