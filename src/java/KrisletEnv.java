// Environment code for project rideau_robocup

import jason.asSyntax.*;
import jason.environment.*;
//import sun.tools.jstat.Literal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.*;
import java.util.regex.Pattern;
import java.util.ArrayList; 
import java.util.Set;

public class KrisletEnv extends Environment {

    private Logger logger = Logger.getLogger("rideau_robocup."+KrisletEnv.class.getName());

    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
    	super.init(args);
    	
    	try {
			host = InetAddress.getByName("localhost");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	port = 6000;
    	team = new String("Krislet-Rideau");
    	
    	mAgents = new HashMap<String, Krislet>();
    	mPlayers = new HashMap<Integer, String>(); 
    	
    	
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
    	Krislet krislet =null;

    	/*Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
    	for (Thread i : threadSet) {
    		logger.info("THREAD: " + i.getName());
    	}*/
    	
        System.out.println("Agent： "+agName);
        
    	if (!mAgents.containsKey(agName)) {
			try {
				krislet = new Krislet(host, port, team);
				mAgents.put(agName, krislet);
				mPlayers.put(krislet.m_number, agName); 
				krislet.start();
				krislet.setName(agName+"_SENSOR");
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			addPercept(agName, Literal.parseLiteral("before_kick_off"));
    	}
    	else
    	{
    		krislet = mAgents.get(agName);
    	}
    	
    	System.out.println("Sensor the environment ");
    	checkEnv(krislet, agName);		//Percepts the Environment and alters agent's beliefs.
    	
    	Memory memory = krislet.m_memory;
    	ObjectInfo object = null;
    	if (memory != null)
    		object = memory.getObject("ball");
    	
		if (action.getFunctor().equals("move")){
			logger.info("PlayerMovedOnGround");
			krislet.move( -Math.random()*52.5 , 34 - Math.random()*68.0 );
		}
		else if (action.getFunctor().equals("turn_fixed_angle")) {
			logger.info("TURNFIXEDANGLE"); 
			krislet.turn(40);
			memory.waitForNewInfo();
		}
		else if (action.getFunctor().equals("turnToBall")) {
			logger.info("TURNTOBALL"); 
			if (object != null)
				krislet.turn(object.m_direction);
			memory.waitForNewInfo();
		}
		else if (action.getFunctor().equals("dash")) {
			logger.info("DASH"); 
			if (object != null)
				krislet.dash(10*object.m_distance);
		}
		else if (action.getFunctor().equals("kickToGoal")) {
			logger.info("KICKTOGOAL"); 
			// TBD
			object = memory.getObject("goal r");
			if (object != null)
				krislet.kick(100, object.m_direction);
		}
		else if (action.getFunctor().equals("play")){
			logger.info("PERCEPT");
			checkEnv(krislet, agName);
		}
		else if (action.getFunctor().equals("kickToPlayer")) {
			logger.info("KICKTOPLAYER");
			object = memory.getObject("player");
			if(memory != null)
				krislet.kick(100, object.m_direction);
		}
		else {
			logger.info("executing: " + action + ", but not implemented");
			return false;
		}

    	return true;
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }

    
    public void checkEnv(Krislet m_krislet, String agName)
	{
		ObjectInfo object;
		Brain brain = m_krislet.getBrain();
		Memory memory = brain.getMemory();
		String playMode = brain.getPlayMode();
		char mySide = brain.getMyside();
		
		m_krislet.m_memory = memory;
		/*if (m_krislet.m_before_kick_off == false) {
			// first put it somewhere on my side
			logger.info("entered if..."); 
			if(Pattern.matches("^before_kick_off.*", playMode))
				logger.info("before kick off...");
				//m_krislet.move( -Math.random()*52.5 , 34 - Math.random()*68.0 );
				addPercept(agName, Literal.parseLiteral("before_kick_off"));
			m_krislet.m_before_kick_off = true;
		}*/
		
		if(m_krislet.m_kicked_off == false) {
			
			logger.info(m_krislet.getBrain().m_playMode);
			if(m_krislet.getBrain().m_playMode.equals("kickOff")) {
				logger.info("entered if");
				addPercept(Literal.parseLiteral("kickOff"));
				m_krislet.m_kicked_off = true; }
		}
	
		object = memory.getObject("ball");
		if( object == null ) {
			/*
			logger.info("turn"); 
			// If you don't know where is ball then find it
			m_krislet.turn(40);
			m_memory.waitForNewInfo(); */
			clearPercepts(agName);
			addPercept(agName, Literal.parseLiteral("ball_not_visible"));
			addPercept(agName, Literal.parseLiteral("far_to_ball"));
			addPercept(agName, Literal.parseLiteral("not_face_to_ball"));
			addPercept(agName, Literal.parseLiteral("no_goal"));
			
		    }
		else if( object.m_distance > 1.0 ) {
			clearPercepts(agName);
			addPercept(agName, Literal.parseLiteral("ball_visible"));
			addPercept(agName, Literal.parseLiteral("far_to_ball"));
			// If ball is too far then
			// turn to ball or 
			// if we have correct direction then go to ball
			if( object.m_direction != 0 ) {
			    //m_krislet.turn(object.m_direction);
				addPercept(agName, Literal.parseLiteral("not_face_to_ball"));
			}
			else {
			    //m_krislet.dash(10*object.m_distance);
				addPercept(agName, Literal.parseLiteral("face_to_ball"));
			}
			
			addPercept(agName, Literal.parseLiteral("no_goal"));
		}
		else 
		{
			clearPercepts(agName);
			addPercept(agName, Literal.parseLiteral("ball_visible"));
			addPercept(agName, Literal.parseLiteral("close_to_ball"));
			addPercept(agName, Literal.parseLiteral("face_to_ball"));
			// We know where is ball and we can kick it
			// so look for goal
			if( mySide == 'l' )
			    object = memory.getObject("goal r");
			else
			    object = memory.getObject("goal l");
	
			if( object == null )
			    {
				//m_krislet.turn(40);
				//m_memory.waitForNewInfo();
				addPercept(agName, Literal.parseLiteral("no_goal"));
			    }
			else {
			    //m_krislet.kick(100, object.m_direction);
				addPercept(agName, Literal.parseLiteral("have_goal"));
			}
		}
		
		//-----------------------------------------------------------------------------------------
		
		object = memory.getObject("player");
		if(object == null)
			addPercept(agName, Literal.parseLiteral("playerNotVisible"));
		else{
			PlayerInfo player = (PlayerInfo)object; 
			if (player.m_uniformName != 0) {
				logger.info("I see a player");
				String percept = "playerVisible" + "(" + mPlayers.get(player.m_uniformName) + ")"; 
				addPercept(agName, Literal.parseLiteral(percept));
				}
			else
				addPercept(agName, Literal.parseLiteral("playerNotVisible"));	
		}
		// sleep one step to ensure that we will not send
		// two commands in one cycle.
		try{
		    Thread.sleep(/*2**/SoccerParams.simulator_step);
		}catch(Exception e){} 	
	}
    
    public ArrayList<Float> getObjectInfo(String objectType, Krislet m_krislet){
    	ArrayList<Float> objectInfo = new ArrayList<Float>(); 
    	ObjectInfo object; 
    	Memory m_memory = m_krislet.getBrain().getMemory(); 
    	
    	object = m_memory.getObject(objectType); 
    	if(object != null) {
    		if(objectType == "goal") {
    			char m_side = 'l'; 
    			if(m_side == 'l')
    				object = m_memory.getObject("goal r");
    			else 
    				object = m_memory.getObject("goal l");
        		objectInfo.add(object.m_distance); 
        		objectInfo.add(object.m_direction); 
    		}
    		else {
        		objectInfo.add(object.m_distance); 
        		objectInfo.add(object.m_direction); 
    		}
    	}
    	else { //for some reason can't get the object so randomize
    		objectInfo.add((float) (Math.random()*52.5));
    		objectInfo.add((float) (Math.random()*52.5)); 
    	}
    	
    	return objectInfo; 
    }
    
	private  InetAddress	host;
	private  int	port;
	private  String	team;
	Map<String, Krislet> mAgents;
	Map<Integer, String> mPlayers; 
}
