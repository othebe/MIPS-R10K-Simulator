package register;

import java.util.HashMap;
import java.util.Iterator;

public class ActiveList {
	private static final int SIZE = 32;
	
	private HashMap<Register, Register> registerMap;
	
	public ActiveList() {
		this.registerMap = new HashMap<Register, Register>();
	}
	
	public boolean canAdd() {
		return (this.registerMap.size() < SIZE);
	}
	
	public void addMapping(Register logical, Register physical) {
		registerMap.put(logical, physical);
	}
	
	public void removeMapping(Register logical) {
		registerMap.remove(logical);
	}
	
	public Register getRenamed(Register logical) {
		return registerMap.get(logical);
	}
	
	public Register getOriginal(Register physical) {
		Register logical = null;
		
		Iterator<Register> iterator = registerMap.keySet().iterator();
		while (iterator.hasNext()) {
			Register register = iterator.next();
			if (registerMap.get(register) == physical) {
				logical = register;
				break;
			}
		}
		
		return logical;
	}
}
