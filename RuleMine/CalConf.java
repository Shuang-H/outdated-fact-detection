import java.util.*;

public class CalConf {
	HashMap<Integer, ArrayList<Triple>> relationMap_R;
	HashMap<String, HashSet<Integer>> entityRelMap_R;
	HashMap<Integer, ArrayList<Triple>> relationMap_K;
	HashMap<String, HashSet<Integer>> entityRelMap_K;
	HashMap<String, Integer> entity_ID;
	HashMap<String, Integer> relation_ID;
	ArrayList<String> entityList;
	ArrayList<String> relationList;
	
	public CalConf(HashMap<Integer, ArrayList<Triple>> relationMap_R, HashMap<String, HashSet<Integer>> entityRelMap_R,
			HashMap<Integer, ArrayList<Triple>> relationMap_K, HashMap<String, HashSet<Integer>> entityRelMap_K,
			HashMap<String, Integer> entity_ID, HashMap<String, Integer> relation_ID, ArrayList<String> entityList, ArrayList<String> relationList){
		this.relationMap_R = relationMap_R;
		this.entityRelMap_R = entityRelMap_R;
		this.relationMap_K = relationMap_K;
		this.entityRelMap_K = entityRelMap_K;
		this.entity_ID = entity_ID;
		this.relation_ID = relation_ID;
		this.entityList = entityList;
		this.relationList = relationList;
	}
	
	public ArrayList<HashMap<Integer, Integer>> findCorrTriple(Atom atom){
		ArrayList<HashMap<Integer, Integer>> resultList = new ArrayList<HashMap<Integer, Integer>>();
		if(!relation_ID.containsKey(atom.relation)) return resultList;
		int relationID = relation_ID.get(atom.relation);
		ArrayList<Triple> tripleList;
		if(atom.source.equals("r")){
			if(!relationMap_R.containsKey(relationID)) return resultList;
			tripleList = relationMap_R.get(relationID);
		}else{
			if(!relationMap_K.containsKey(relationID)) return resultList;
			tripleList = relationMap_K.get(relationID);
		}
		for(Triple triple : tripleList){
			HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>();
			hashMap.put(atom.variable1, triple.entityID);
			hashMap.put(atom.variable2, triple.propID);
			resultList.add(hashMap);
		}
		return resultList;
	}
	
	public ArrayList<HashMap<Integer, Integer>> expandCorrTriple(Atom atom, ArrayList<HashMap<Integer, Integer>> currResults){
		ArrayList<HashMap<Integer, Integer>> resultList = new ArrayList<HashMap<Integer, Integer>>();
		if(currResults.isEmpty() || !relation_ID.containsKey(atom.relation)) return resultList;
		boolean contain1 = currResults.get(0).containsKey(atom.variable1);
		boolean contain2 = currResults.get(0).containsKey(atom.variable2);
		for(HashMap<Integer, Integer> hashMap : currResults){
			if(contain1 && contain2){
				String key = hashMap.get(atom.variable1) + "_" + relation_ID.get(atom.relation) + "_0";
				if(atom.source.equals("r") && entityRelMap_R.containsKey(key)){
					if(entityRelMap_R.get(key).contains(hashMap.get(atom.variable2))){
						resultList.add(hashMap);
					}
				}
				if(atom.source.equals("k") && entityRelMap_K.containsKey(key)){
					if(entityRelMap_K.get(key).contains(hashMap.get(atom.variable2))){
						resultList.add(hashMap);
					}
				}
			}else if(contain1){
				String key = hashMap.get(atom.variable1) + "_" + relation_ID.get(atom.relation) + "_0";
				if(atom.source.equals("r") && entityRelMap_R.containsKey(key)){
					for(Integer value : entityRelMap_R.get(key)){
						hashMap.put(atom.variable2, value);
						resultList.add(hashMap);
					}
				}
				if(atom.source.equals("k") && entityRelMap_K.containsKey(key)){
					for(Integer value : entityRelMap_K.get(key)){
						hashMap.put(atom.variable2, value);
						resultList.add(hashMap);
					}
				}
			}else if(contain2){
				String key = relation_ID.get(atom.relation) + "_" + hashMap.get(atom.variable2) + "_1";
				if(atom.source.equals("r") && entityRelMap_R.containsKey(key)){
					for(Integer value : entityRelMap_R.get(key)){
						hashMap.put(atom.variable1, value);
						resultList.add(hashMap);
					}
				}
				if(atom.source.equals("k") && entityRelMap_K.containsKey(key)){
					for(Integer value : entityRelMap_K.get(key)){
						hashMap.put(atom.variable1, value);
						resultList.add(hashMap);
					}
				}
			}else{
				int relationID = relation_ID.get(atom.relation);
				ArrayList<Triple> tripleList;
				if(atom.source.equals("r")){
					tripleList = relationMap_R.get(relationID);
				}else{
					tripleList = relationMap_K.get(relationID);
				}
				for(Triple triple : tripleList){
					HashMap<Integer, Integer> hashMap1 = new HashMap<Integer, Integer>();
					hashMap1.put(atom.variable1, triple.entityID);
					hashMap1.put(atom.variable2, triple.propID);
					for(Map.Entry<Integer, Integer> entry : hashMap.entrySet()){
						hashMap1.put(entry.getKey(), entry.getValue());
					}
					resultList.add(hashMap1);
				}
			}
			if(resultList.size() > Integer.MAX_VALUE/2) break;
		}
		return resultList;
	}
	
	/*
	public double[] compute(Rule rule){
		double[] result = new double[2];//support_confidence
		Atom firstAtom = rule.body.get(1), head = rule.body.get(0);
		ArrayList<HashMap<Integer, Integer>> denominatorTriples = findCorrTriple(firstAtom);
				
		if(rule.atomNum > 2){
			for(int i = 2; i < rule.body.size(); i++){
				denominatorTriples = expandCorrTriple(rule.body.get(i), denominatorTriples);
			}
		}
		int denominator = denominatorTriples.size();
		ArrayList<HashMap<Integer, Integer>> numeratorTriples = expandCorrTriple(head, denominatorTriples);
		//result[0] = numeratorTriples.size();//support
		
		HashSet<String> hashSet = new HashSet<String>();
		for(HashMap<Integer, Integer> hashMap : numeratorTriples){
			hashSet.add(hashMap.get(head.variable1)+"_"+hashMap.get(head.variable2));
		}
		
		result[0] = (double) hashSet.size() / relationMap_R.get(relation_ID.get(head.relation)).size();//head coverage
		result[1] = (double) numeratorTriples.size() / (double) denominator;
		numeratorTriples.clear();
		return result;
	}
	*/
	
	HashMap<String, Double> hashMap = new HashMap<String, Double>();
	
	public double[] compute(Rule rule, double coverThreshold){
		double[] result = new double[2];//support_confidence
		Atom firstAtom = rule.body.get(1), head = rule.body.get(0);
		ArrayList<HashMap<Integer, Integer>> denominatorTriples = findCorrTriple(firstAtom);//firstAtom
				
		if(rule.atomNum > 2){
			if(hashMap.containsKey(head.relation+"_"+firstAtom.relation)) {
				double sup = hashMap.get(head.relation+"_"+firstAtom.relation);
				if(sup < coverThreshold) {
					result[0] = 0; result[1] = 0;
					return result;
				}
			}
			for(int i = 2; i < rule.body.size(); i++){
				denominatorTriples = expandCorrTriple(rule.body.get(i), denominatorTriples);//firstAtom+secondAtom
			}
		}
		
		int denominator = denominatorTriples.size();
		ArrayList<HashMap<Integer, Integer>> numeratorTriples = expandCorrTriple(head, denominatorTriples);
		//result[0] = numeratorTriples.size();//support
		
		HashSet<String> hashSet = new HashSet<String>();
		for(HashMap<Integer, Integer> hashMap : numeratorTriples){
			hashSet.add(hashMap.get(head.variable1)+"_"+hashMap.get(head.variable2));
		}
		
		result[0] = (double) hashSet.size() / relationMap_R.get(relation_ID.get(head.relation)).size();//head coverage
		result[1] = (double) numeratorTriples.size() / (double) denominator;
		numeratorTriples.clear();
		
		if(rule.atomNum == 2) {
			hashMap.put(head.relation+"_"+firstAtom.relation, result[0]);
			hashMap.put(firstAtom.relation+"_"+head.relation, result[0]);
		}
		
		return result;
	}
}
