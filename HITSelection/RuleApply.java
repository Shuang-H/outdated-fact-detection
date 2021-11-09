import java.io.*;
import java.util.*;

import RuleMine.Atom;
import RuleMine.Rule;


public class RuleApply {
	static HashMap<String, ArrayList<String>> relationMap = new HashMap<String, ArrayList<String>>();
	static HashMap<String, HashSet<String>> entityRelMap = new HashMap<String, HashSet<String>>();
	static ArrayList<Rule> ruleList = new ArrayList<Rule>();
	static HashSet<String> relationSet = new HashSet<String>();
	
	public static ArrayList<Rule> readAMIERule(String ruleFile){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(ruleFile));
			String line = null;
			while((line = reader.readLine()) != null){
				String[] splits = line.toLowerCase().split("\t");
				Rule rule = new Rule();
				double confidence = Double.valueOf(splits[splits.length-1]);
				rule.setConfidence(confidence);
				
				HashMap<String, Integer> indexMap = new HashMap<String, Integer>();
				for(int i = splits.length-2; i >= 0; i--){
					String[] splits1 = splits[i].split(" ");
					String relation = splits1[1].substring(1,splits1[1].length()-1);
					if(!indexMap.containsKey(splits1[0])) indexMap.put(splits1[0], indexMap.size()+1);
					if(!indexMap.containsKey(splits1[2])) indexMap.put(splits1[2], indexMap.size()+1);
					Atom atom = new Atom(indexMap.get(splits1[0]), relation, indexMap.get(splits1[2]));
					rule.addAtom(atom);
					relationSet.add(relation);
				}
				ruleList.add(rule);
				System.out.println(rule.toString()+"\t"+rule.confidence);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ruleList;
	}
	
	public static ArrayList<Rule> readRule(String ruleFile){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(ruleFile));
			String line = null;
			while((line = reader.readLine()) != null){
				String[] splits = line.toLowerCase().split("\t");
				Rule rule = new Rule();
				double confidence = Double.valueOf(splits[splits.length-1]);
				rule.setConfidence(confidence);
				for(int i = splits.length-2; i >= 0; i--){
					String[] splits1 = splits[i].split(" "); 
					String relation = splits1[1].substring(1,splits1[1].length()-1);
					Atom atom = new Atom(Integer.parseInt(splits1[0]), relation, Integer.parseInt(splits1[2]), splits1[3]);
					rule.addAtom(atom);
					relationSet.add(relation);
				}
				ruleList.add(rule);
				System.out.println(rule.toString()+"\t"+rule.confidence);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ruleList;
	}
	
	public static void readKBData(){
		//String KBFile = "/root/Dataset/DBpedia/DBpedia 3.4/mappingbased_properties_en.nt";
		String KBFile = "/root/Dataset/Yago/Yago 1.1.0/yagoInfobox.nt";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(KBFile));
			String line = null;
			while((line = reader.readLine()) != null){
				line = line.toLowerCase();
				/*
				String[] splits = line.split(" ");
				String entity = splits[0].substring(splits[0].lastIndexOf("/")+1, splits[0].indexOf(">"));
				String relation = splits[1].substring(splits[1].lastIndexOf("/")+1, splits[1].indexOf(">"));
				String propValue = line.substring(line.indexOf(relation)+relation.length()+1);
				if(propValue.contains("\"") && !propValue.contains("<http://dbpedia.org/resource/")){
					propValue = propValue.substring(propValue.indexOf("\"")+1, propValue.lastIndexOf("\""));
				}else{
					propValue = propValue.substring(propValue.lastIndexOf("/")+1, propValue.lastIndexOf(">"));
				}
				*/
				String[] splits = line.split("\t");
				String entity = splits[0].substring(splits[0].indexOf("<")+1, splits[0].indexOf(">"));
				String relation = splits[1].substring(splits[1].indexOf("<")+1, splits[1].indexOf(">"));
				String propValue = splits[2].substring(splits[2].indexOf("<")+1, splits[2].indexOf(">"));
				
				String triple = entity+"\t"+relation+"\t"+propValue;
				if(relationSet.contains(relation) && triple.split("\t").length == 3){		
					if(relationMap.containsKey(relation)){
						relationMap.get(relation).add(triple);
					}else{
						ArrayList<String> list = new ArrayList<String>();
						list.add(triple);
						relationMap.put(relation, list);
					}
					
					String key = entity+" "+relation;
					if(entityRelMap.containsKey(key)){
						entityRelMap.get(key).add(propValue);
					}else{
						HashSet<String> set = new HashSet<String>();
						set.add(propValue);
						entityRelMap.put(key, set);
					}				
					key = relation+" "+propValue;
					if(entityRelMap.containsKey(key)){
						entityRelMap.get(key).add(entity);
					}else{
						HashSet<String> set = new HashSet<String>();
						set.add(entity);
						entityRelMap.put(key, set);
					}
				}
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public static ArrayList<HashMap<Integer, String>> findCorrTriple(Atom atom){
		ArrayList<HashMap<Integer, String>> resultList = new ArrayList<HashMap<Integer, String>>();
		ArrayList<String> tripleList;
		if(!relationMap.containsKey(atom.relation)) return resultList;
		tripleList = relationMap.get(atom.relation);
		
		for(String triple : tripleList){
			String[] splits = triple.split("\t"); 
			if(splits.length == 3){
				HashMap<Integer, String> hashMap = new HashMap<Integer, String>();
				hashMap.put(atom.variable1, splits[0]);
				hashMap.put(atom.variable2, splits[2]);
				resultList.add(hashMap);
			}
		}
		return resultList;
	}
	
	public static ArrayList<HashMap<Integer, String>> expandCorrTriple(Atom atom, ArrayList<HashMap<Integer, String>> currResults){
		ArrayList<HashMap<Integer, String>> resultList = new ArrayList<HashMap<Integer, String>>();
		if(currResults.isEmpty()) return resultList;
		boolean contain1 = currResults.get(0).containsKey(atom.variable1);
		boolean contain2 = currResults.get(0).containsKey(atom.variable2);
		for(HashMap<Integer, String> hashMap : currResults){
			if(contain1 && contain2){
				String key = hashMap.get(atom.variable1) + " " + atom.relation;
				if(entityRelMap.containsKey(key)){
					if(entityRelMap.get(key).contains(hashMap.get(atom.variable2))){
						resultList.add(hashMap);
					}
				}
			}else if(contain1){
				String key = hashMap.get(atom.variable1) + " " + atom.relation;
				if(entityRelMap.containsKey(key)){
					for(String value : entityRelMap.get(key)){
						hashMap.put(atom.variable2, value);
						resultList.add(hashMap);
					}
				}
			}else if(contain2){
				String key = atom.relation + " " + hashMap.get(atom.variable2);
				if(entityRelMap.containsKey(key)){
					for(String value : entityRelMap.get(key)){
						hashMap.put(atom.variable1, value);
						resultList.add(hashMap);
					}
				}
			}else{
				ArrayList<String> tripleList = relationMap.get(atom.relation);
				for(String triple : tripleList){
					String[] splits = triple.split("\t");
					HashMap<Integer, String> hashMap1 = new HashMap<Integer, String>();
					hashMap1.put(atom.variable1, splits[0]);
					hashMap1.put(atom.variable2, splits[2]);
					for(Map.Entry<Integer, String> entry : hashMap.entrySet()){
						hashMap1.put(entry.getKey(), entry.getValue());
					}
					resultList.add(hashMap1);
				}
			}
		}
		return resultList;
	}
	
	public static ArrayList<String> apply(){
		ArrayList<String> list = new ArrayList<String>();
		for(Rule rule : ruleList){
			Atom head = rule.body.get(0), firstAtom = rule.body.get(1);
			ArrayList<HashMap<Integer, String>> resultList = findCorrTriple(firstAtom);	
			for(int i = 2; i < rule.body.size(); i++){
				resultList = expandCorrTriple(rule.body.get(i), resultList);
			}
			for(HashMap<Integer, String> hashMap : resultList){
				String insta = "";
				for(int i = 0; i < rule.body.size(); i++){
					Atom atom = rule.body.get(i);
					String triple = hashMap.get(atom.variable1)+" "+atom.relation+" "+hashMap.get(atom.variable2);
					insta  = insta + triple + "\t";
				}
				list.add(insta);
				System.out.println(insta);
			}
		}
		return list;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RuleApply.readAMIERule("Rule_data/AMIE_Yago.txt");
		//RuleApply.readRule("Rule_data/Rule_DBpedia.txt");
		RuleApply.readKBData();
		RuleApply.apply();
	}

}
