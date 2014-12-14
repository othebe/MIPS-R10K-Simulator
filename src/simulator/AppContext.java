package simulator;
import instructionQueue.AddressQueue;
import instructionQueue.FloatingQueue;
import instructionQueue.InstructionQueue;
import instructionQueue.IntegerQueue;
import register.FreeList;
import register.ActiveList;
import register.RegisterList;

/**
 * Hold context for the simulator application.
 * @author Ozzy
 *
 */
public class AppContext {
	// Time logger.
	public TimeLogger timeLogger;
	
	// Register list.
	public RegisterList registerList;
	
	// Free list.
	public FreeList freeList;
	
	// Active list.
	public ActiveList activeList;
	
	// Integer queue.
	public IntegerQueue integerQueue;
	
	// Floating queue.
	public FloatingQueue floatingQueue;
	
	// Address queue.
	public AddressQueue addressQueue;
	
	// Fetcher.
	public Fetcher fetcher;
	
	// Decoder.
	public Decoder decoder;
	
	// Graduator.
	public Graduator graduator;
	
	public AppContext(String filename) {
		this.timeLogger = new TimeLogger(this);
		this.registerList = new RegisterList(this);
		this.freeList = new FreeList(this);
		this.activeList = new ActiveList();
		this.addressQueue = new AddressQueue(this);
		this.integerQueue = new IntegerQueue(this);
		this.floatingQueue = new FloatingQueue(this);
		this.fetcher = new Fetcher(this, filename);
		this.decoder = new Decoder(this);
		this.graduator = new Graduator(this);
	}
}
