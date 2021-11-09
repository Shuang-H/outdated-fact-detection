import java.io.*;
import java.util.*;


public class MultiMiner {
	HashMap<Integer, ArrayList<Triple>> invertedLists_R;//from revision history
	HashMap<Integer, ArrayList<Triple>> relationMap_R;
	HashMap<String, ArrayList<Integer>> findRelationMap_R;//entityID1_entityID2, relationID
	HashMap<String, HashSet<Integer>> entityRelMap_R;
	HashMap<Integer, ArrayList<Triple>> invertedLists_K;//from the knowledge base
	HashMap<Integer, ArrayList<Triple>> relationMap_K;
	HashMap<String, ArrayList<Integer>> findRelationMap_K;//entityID1_entityID2, relationID
	HashMap<String, HashSet<Integer>> entityRelMap_K;
	CalConf cal;
	double coverThreshold, confThreshold;
	HashSet<Rule> ruleSet;
	HashMap<String, Integer> entity_ID;//index
	HashMap<String, Integer> relation_ID;
	ArrayList<String> entityList, relationList;
	int threadNum;
	
	public MultiMiner(double coverThreshold, double confThreshold, int threadNum){
		invertedLists_R = new HashMap<Integer, ArrayList<Triple>>();
		relationMap_R = new HashMap<Integer, ArrayList<Triple>>();
		entityRelMap_R = new HashMap<String, HashSet<Integer>>();
		findRelationMap_R = new HashMap<String, ArrayList<Integer>>();
		invertedLists_K = new HashMap<Integer, ArrayList<Triple>>();
		relationMap_K = new HashMap<Integer, ArrayList<Triple>>();
		entityRelMap_K = new HashMap<String, HashSet<Integer>>();
		findRelationMap_K = new HashMap<String, ArrayList<Integer>>();
		this.coverThreshold = coverThreshold;
		this.confThreshold = confThreshold;
		ruleSet = new HashSet<Rule>();
		entity_ID = new HashMap<String, Integer>();
		relation_ID = new HashMap<String, Integer>();
		entityList = new ArrayList<String>();
		relationList = new ArrayList<String>();
		this.threadNum = threadNum;
	}

	public void ReadFile(){
		String reviseHistoryFile = "/home/haoshuang/Dataset/DBpedia/active.tsv";
		//String reviseHistoryFile = "/home/haoshuang/Dataset/Yago/active.tsv";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(reviseHistoryFile));
			String line = null;
			while((line = reader.readLine()) != null){
				
				String[] splits = line.split(" ");
				String entity = "<" + splits[0].substring(splits[0].lastIndexOf("/")+1, splits[0].lastIndexOf(">")) + ">";
				String relation = "<" + splits[1].substring(splits[1].lastIndexOf("/")+1, splits[1].indexOf(">")) + ">";
				String propValue = line.substring(line.indexOf(relation)+relation.length()+1);
				if(propValue.contains("\"") && !propValue.contains("<http://dbpedia.org/resource/")){
					propValue = propValue.substring(propValue.indexOf("\"")+1, propValue.lastIndexOf("\""));
				}else{
					propValue = propValue.substring(propValue.lastIndexOf("/")+1, propValue.lastIndexOf(">"));
				}
				propValue = "<" + propValue + ">";
				
				//String[] splits = line.split("\t");
				//String entity = splits[0], relation = splits[1], propValue = splits[2];
				
				if(!entity_ID.containsKey(entity)){
					entity_ID.put(entity, entity_ID.size());
					entityList.add(entity);
				}
				if(!entity_ID.containsKey(propValue)){
					entity_ID.put(propValue, entity_ID.size());
					entityList.add(propValue);
				}
				if(!relation_ID.containsKey(relation)){
					relation_ID.put(relation, relation_ID.size());
					relationList.add(relation);
				}
				int entityID = entity_ID.get(entity);
				int relationID = relation_ID.get(relation);
				int propID = entity_ID.get(propValue);
				Triple triple = new Triple(entityID, relationID, propID, "r");
				
				if(relationMap_R.containsKey(relationID)){
					relationMap_R.get(relationID).add(triple);
				}else{
					ArrayList<Triple> list = new ArrayList<Triple>();
					list.add(triple);
					relationMap_R.put(relationID, list);
				}
				
				if(invertedLists_R.containsKey(entityID)){
					invertedLists_R.get(entityID).add(triple);
				}else{
					ArrayList<Triple> list = new ArrayList<Triple>();
					list.add(triple);
					invertedLists_R.put(entityID, list);
				}
				if(invertedLists_R.containsKey(propID)){
					invertedLists_R.get(propID).add(triple);
				}else{
					ArrayList<Triple> list = new ArrayList<Triple>();
					list.add(triple);
					invertedLists_R.put(propID, list);
				}
				
				String key = entityID+"_"+relationID+"_0";
				if(entityRelMap_R.containsKey(key)){
					entityRelMap_R.get(key).add(propID);
				}else{
					HashSet<Integer> set = new HashSet<Integer>();
					set.add(propID);
					entityRelMap_R.put(key, set);
				}
				
				key = relationID+"_"+propID+"_1";
				if(entityRelMap_R.containsKey(key)){
					entityRelMap_R.get(key).add(entityID);
				}else{
					HashSet<Integer> set = new HashSet<Integer>();
					set.add(entityID);
					entityRelMap_R.put(key, set);
				}
				
				key = entityID + "_" + propID;
				if(findRelationMap_R.containsKey(key)){
					findRelationMap_R.get(key).add(relationID);
				}else{
					ArrayList<Integer> list = new ArrayList<Integer>();
					list.add(relationID);
					findRelationMap_R.put(key, list);
				}
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		String KBFile = "/home/haoshuang/Dataset/DBpedia/stable.tsv";
		//String KBFile = "/home/haoshuang/Dataset/Yago/stable.tsv";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(KBFile));
			String line = null;
			while((line = reader.readLine()) != null){
				
				String[] splits = line.toLowerCase().split(" ");
				String entity = "<" + splits[0].substring(splits[0].lastIndexOf("/")+1, splits[0].lastIndexOf(">")) + ">";
				String relation = "<" + splits[1].substring(splits[1].lastIndexOf("/")+1, splits[1].indexOf(">")) + ">";
				String propValue = line.substring(line.indexOf(relation)+relation.length()+1);
				if(propValue.contains("\"") && !propValue.contains("<http://dbpedia.org/resource/")){
					propValue = propValue.substring(propValue.indexOf("\"")+1, propValue.lastIndexOf("\""));
				}else{
					propValue = propValue.substring(propValue.lastIndexOf("/")+1, propValue.lastIndexOf(">"));
				}
				propValue = "<" + propValue + ">";
				
				//String[] splits = line.split("\t");
				//String entity = splits[0], relation = splits[1], propValue = splits[2];
				
				if(!entity_ID.containsKey(entity)){
					entity_ID.put(entity, entity_ID.size());
					entityList.add(entity);
				}
				if(!entity_ID.containsKey(propValue)){
					entity_ID.put(propValue, entity_ID.size());
					entityList.add(propValue);
				}
				if(!relation_ID.containsKey(relation)){
					relation_ID.put(relation, relation_ID.size());
					relationList.add(relation);
				}
				int entityID = entity_ID.get(entity);
				int relationID = relation_ID.get(relation);
				int propID = entity_ID.get(propValue);
				Triple triple = new Triple(entityID, relationID, propID, "k");
				
				if(relationMap_K.containsKey(relationID)){
					relationMap_K.get(relationID).add(triple);
				}else{
					ArrayList<Triple> list = new ArrayList<Triple>();
					list.add(triple);
					relationMap_K.put(relationID, list);
				}
				
				if(invertedLists_K.containsKey(entityID)){
					invertedLists_K.get(entityID).add(triple);
				}else{
					ArrayList<Triple> list = new ArrayList<Triple>();
					list.add(triple);
					invertedLists_K.put(entityID, list);
				}
				if(invertedLists_K.containsKey(propID)){
					invertedLists_K.get(propID).add(triple);
				}else{
					ArrayList<Triple> list = new ArrayList<Triple>();
					list.add(triple);
					invertedLists_K.put(propID, list);
				}
				
				String key = entityID+"_"+relationID+"_0";
				if(entityRelMap_K.containsKey(key)){
					entityRelMap_K.get(key).add(propID);
				}else{
					HashSet<Integer> set = new HashSet<Integer>();
					set.add(propID);
					entityRelMap_K.put(key, set);
				}
				
				key = relationID+"_"+propID+"_1";
				if(entityRelMap_K.containsKey(key)){
					entityRelMap_K.get(key).add(entityID);
				}else{
					HashSet<Integer> set = new HashSet<Integer>();
					set.add(entityID);
					entityRelMap_K.put(key, set);
				}
				
				key = entityID + "_" + propID;
				if(findRelationMap_K.containsKey(key)){
					findRelationMap_K.get(key).add(relationID);
				}else{
					ArrayList<Integer> list = new ArrayList<Integer>();
					list.add(relationID);
					findRelationMap_K.put(key, list);
				}
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cal = new CalConf(relationMap_R, entityRelMap_R, relationMap_K, entityRelMap_K, entity_ID, relation_ID, entityList, relationList);	
	}
	
	public synchronized void startMiner() {
		long startTime=System.currentTimeMillis(); 
		
		ArrayList<Thread> threadList = new ArrayList<Thread>();
		int dataNum = relationMap_R.size();
		int quotient = dataNum / threadNum, remainder = dataNum % threadNum;
		for(int i = 0; i < threadNum-1; i++){
			int start = i * quotient;
			int end = (i+1) * quotient;
			Miner miner = new Miner(start, end, i, coverThreshold, confThreshold, invertedLists_R, invertedLists_K, relationMap_R, relationMap_K, findRelationMap_R, findRelationMap_K, entity_ID, relation_ID, entityList, relationList, ruleSet, cal);
			Thread thread = new Thread(miner);
			thread.start();
			threadList.add(thread);
		}
		int start = (threadNum-1) * quotient;
		int end = start + quotient + remainder;
		Miner miner = new Miner(start, end, threadNum-1, coverThreshold, confThreshold, invertedLists_R, invertedLists_K, relationMap_R, relationMap_K, findRelationMap_R, findRelationMap_K, entity_ID, relation_ID, entityList, relationList, ruleSet, cal);
		Thread thread = new Thread(miner);
		thread.start();
		threadList.add(thread);
		
		for(int i = 0; i < threadList.size(); i++){
			try {
				threadList.get(i).join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		long endTime=System.currentTimeMillis();
		System.out.println("Running Time: " + (endTime-startTime) + "ms");
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("rule.txt"));
			for(Rule rule : ruleSet){
				writer.write(rule.toString() + "\t" + rule.confidence);
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Preprocess...");
		MultiMiner multiMiner = new MultiMiner(0.3, 0.5, 8);
		multiMiner.ReadFile();
		
		System.out.println("Start Mining...");
		multiMiner.startMiner();

	}


}
