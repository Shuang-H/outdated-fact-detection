import java.util.*;

public class Rule {
	public ArrayList<Atom> body;
	int atomNum;
	HashMap<Integer, Integer> countMap;
	public double confidence = -1;

	
	public Rule(){
		body = new ArrayList<Atom>();
		atomNum = 0;
		countMap = new HashMap<Integer, Integer>();
	}
	
	public void addAtom(Atom atom){
		body.add(atom);
		atomNum++;
		
		if(countMap.containsKey(atom.variable1)){
			int count = countMap.get(atom.variable1) + 1;
			countMap.put(atom.variable1, count);
		}else{
			countMap.put(atom.variable1, 1);
		}
		if(countMap.containsKey(atom.variable2)){
			int count = countMap.get(atom.variable2) + 1;
			countMap.put(atom.variable2, count);
		}else{
			countMap.put(atom.variable2, 1);
		}
	}
	
	public void remove(){
		if(body.size() > 0){
			Atom atom = body.get(body.size()-1); 
			body.remove(body.size()-1);
			atomNum--;
			
			int count = countMap.get(atom.variable1) - 1;
			countMap.put(atom.variable1, count);
			count = countMap.get(atom.variable2) - 1;
			countMap.put(atom.variable2, count);
		}
	}

	public void setConfidence(double confidence){
		this.confidence = confidence;
	}
	
	public boolean isClose(){
		for(Map.Entry<Integer, Integer> entry : countMap.entrySet()){
			if(entry.getValue() < 2 && entry.getValue() > 0) return false;
		}
		return true;
	}
	
	public String toString(){
		String str = "";
		if(atomNum == 0){
			return str;		
		}
		for(int i = 1; i < atomNum; i++){
			str = str + body.get(i).toString() + "\t";
		}
		str = str + body.get(0).toString();
		return str;
	}
	
	public int hashCode(){ 
		int code = 0;
		for(int i = 1; i < body.size(); i++){
			code = code * body.get(i).hashCode();
		}
		return code * 31 + body.get(0).hashCode();
	}
	
	public boolean equals(Object obj){
		if(obj instanceof Rule){
			Rule rule = (Rule) obj;
			if(this.hashCode() == rule.hashCode()) return true;
		}
		return false;
	}

}
