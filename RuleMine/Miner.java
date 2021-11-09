import java.util.*;


public class Miner implements Runnable{
	HashMap<Integer, ArrayList<Triple>> invertedLists_R;//from revision history
	HashMap<Integer, ArrayList<Triple>> relationMap_R;
	HashMap<String, ArrayList<Integer>> findRelationMap_R;//entityID1_entityID2, relationID
	HashMap<Integer, ArrayList<Triple>> invertedLists_K;//from the knowledge base
	HashMap<Integer, ArrayList<Triple>> relationMap_K;
	HashMap<String, ArrayList<Integer>> findRelationMap_K;//entityID1_entityID2, relationID
	HashMap<String, Integer> entity_ID;//index
	HashMap<String, Integer> relation_ID;
	ArrayList<String> entityList, relationList;
	HashSet<Rule> ruleSet;
	double coverThreshold, confThreshold;
	int start, end, threadID;
	CalConf cal;
	
	public Miner(int start, int end, int threadID, double coverThreshold, double confThreshold, HashMap<Integer, ArrayList<Triple>> invertedLists_R, HashMap<Integer, ArrayList<Triple>> invertedLists_K, HashMap<Integer, ArrayList<Triple>> relationMap_R, HashMap<Integer, ArrayList<Triple>> relationMap_K, 
			HashMap<String, ArrayList<Integer>> findRelationMap_R, HashMap<String, ArrayList<Integer>> findRelationMap_K, 
			HashMap<String, Integer> entity_ID, HashMap<String, Integer> relation_ID, ArrayList<String> entityList, 
			ArrayList<String> relationList, HashSet<Rule> ruleSet, CalConf cal){
		this.invertedLists_R = invertedLists_R;
		this.invertedLists_K = invertedLists_K;
		this.relationMap_R = relationMap_R;
		this.relationMap_K = relationMap_K;
		this.findRelationMap_R = findRelationMap_R;
		this.findRelationMap_K = findRelationMap_K;
		this.entity_ID = entity_ID;
		this.relation_ID = relation_ID;
		this.entityList = entityList;
		this.relationList = relationList;
		this.ruleSet = ruleSet;
		this.coverThreshold = coverThreshold;
		this.confThreshold = confThreshold;
		this.start = start;
		this.end = end;
		this.threadID = threadID;
		this.cal = cal;
	}
	
	public void FindAllTwoAtomRule(Atom head){
		Rule rule = new Rule();
		rule.addAtom(head);
		int relationID = relation_ID.get(head.relation);
		ArrayList<Triple> tripleList = relationMap_R.get(relationID);
		HashSet<Atom> nextAtoms = new HashSet<Atom>();
		for(Triple triple : tripleList){
			String enPair = triple.entityID + "_" + triple.propID;
			ArrayList<Integer> list = findRelationMap_R.get(enPair);
			for(int nextRelationID : list){
				if(nextRelationID != relationID){
					String relation = relationList.get(nextRelationID);
					Atom atom = new Atom(1,relation,2,"r");
					nextAtoms.add(atom);
				}
			}
		}
		for(Atom atom : nextAtoms){
			rule.addAtom(atom);
			double[] result = cal.compute(rule, coverThreshold);				
			double coverage = result[0]; 
			double confidence = result[1];
			if(coverage >= coverThreshold && confidence >= confThreshold){
				Rule ruleCopy = new Rule();
				ruleCopy.addAtom(rule.body.get(0));
				ruleCopy.addAtom(rule.body.get(1));
				ruleCopy.setConfidence(confidence);
				ruleSet.add(ruleCopy);
				System.out.println(ruleCopy.toString() + "\t" + coverage + "\t" + confidence);
			}
			rule.remove();
		}
	}
	
	public void FindAllThreeAtomRule_1(Atom head){
		//<X,r,Y> + <X,r,Y> = <X,r,Y>
		Rule rule = new Rule();
		rule.addAtom(head);
		int relationID = relation_ID.get(head.relation);
		ArrayList<Triple> tripleList = relationMap_R.get(relationID);
		HashSet<Atom> nextAtoms = new HashSet<Atom>();
		for(Triple triple : tripleList){
			String enPair = triple.entityID + "_" + triple.propID;
			if(findRelationMap_R.containsKey(enPair)){
				ArrayList<Integer> list = findRelationMap_R.get(enPair);
				for(int nextRelationID : list){
					if(nextRelationID != relationID){
						String relation = relationList.get(nextRelationID);
						Atom atom = new Atom(1,relation,2,"r");
						nextAtoms.add(atom);
					}
				}
			}
			if(findRelationMap_K.containsKey(enPair)){
				ArrayList<Integer> list = findRelationMap_K.get(enPair);
				for(int nextRelationID : list){
					if(nextRelationID != relationID){
						String relation = relationList.get(nextRelationID);
						Atom atom = new Atom(1,relation,2,"k");
						nextAtoms.add(atom);
					}
				}
			}
		}
		ArrayList<Atom> nextAtomList = new ArrayList<Atom>(nextAtoms);
		for(int i = 0 ; i < nextAtoms.size()-1; i++){
			rule.addAtom(nextAtomList.get(i));
			for(int j = i+1; j < nextAtoms.size(); j++){
				rule.addAtom(nextAtomList.get(j));
				double[] result = cal.compute(rule, coverThreshold);				
				double coverage = result[0]; 
				double confidence = result[1];
				if(coverage >= coverThreshold && confidence >= confThreshold){
					Rule ruleCopy = new Rule();
					ruleCopy.addAtom(rule.body.get(0));
					ruleCopy.addAtom(rule.body.get(1));
					ruleCopy.setConfidence(confidence);
					ruleSet.add(ruleCopy);
					System.out.println(ruleCopy.toString() + "\t" + coverage + "\t" + confidence);
				}
				rule.remove();
			}
			rule.remove();
		}
		
		//<Y,r,X> + <Y,r,X> = <X,r,Y>
		rule = new Rule();
		rule.addAtom(head);
		relationID = relation_ID.get(head.relation);
		tripleList = relationMap_R.get(relationID);
		nextAtoms = new HashSet<Atom>();
		for(Triple triple : tripleList){
			String enPair = triple.propID + "_" + triple.entityID;
			if(findRelationMap_R.containsKey(enPair)){
				ArrayList<Integer> list = findRelationMap_R.get(enPair);
				for(int nextRelationID : list){
					if(nextRelationID != relationID){
						String relation = relationList.get(nextRelationID);
						Atom atom = new Atom(2,relation,1,"r");
						nextAtoms.add(atom);
					}
				}
			}
			if(findRelationMap_K.containsKey(enPair)){
				ArrayList<Integer> list = findRelationMap_K.get(enPair);
				for(int nextRelationID : list){
					if(nextRelationID != relationID){
						String relation = relationList.get(nextRelationID);
						Atom atom = new Atom(2,relation,1,"k");
						nextAtoms.add(atom);
					}
				}
			}
		}
		nextAtomList = new ArrayList<Atom>(nextAtoms);
		for(int i = 0 ; i < nextAtoms.size()-1; i++){
			rule.addAtom(nextAtomList.get(i));
			for(int j = i+1; j < nextAtoms.size(); j++){
				rule.addAtom(nextAtomList.get(j));
				double[] result = cal.compute(rule, coverThreshold);				
				double coverage = result[0]; 
				double confidence = result[1];
				if(coverage >= coverThreshold && confidence >= confThreshold){
					Rule ruleCopy = new Rule();
					ruleCopy.addAtom(rule.body.get(0));
					ruleCopy.addAtom(rule.body.get(1));
					ruleCopy.setConfidence(confidence);
					ruleSet.add(ruleCopy);
					System.out.println(ruleCopy.toString() + "\t" + coverage + "\t" + confidence);
				}
				rule.remove();
			}
			rule.remove();
		}
		
		//<X,r,Y> + <Y,r,X> = <X,r,Y>
		rule = new Rule();
		rule.addAtom(head);
		relationID = relation_ID.get(head.relation);
		tripleList = relationMap_R.get(relationID);
		nextAtoms = new HashSet<Atom>();
		for(Triple triple : tripleList){
			String enPair = triple.entityID + "_" + triple.propID;
			if(findRelationMap_R.containsKey(enPair)){
				ArrayList<Integer> list = findRelationMap_R.get(enPair);
				for(int nextRelationID : list){
					if(nextRelationID != relationID){
						String relation = relationList.get(nextRelationID);
						Atom atom = new Atom(1,relation,2,"r");
						nextAtoms.add(atom);
					}
				}
			}
			if(findRelationMap_K.containsKey(enPair)){
				ArrayList<Integer> list = findRelationMap_K.get(enPair);
				for(int nextRelationID : list){
					if(nextRelationID != relationID){
						String relation = relationList.get(nextRelationID);
						Atom atom = new Atom(1,relation,2,"k");
						nextAtoms.add(atom);
					}
				}
			}
		}
		nextAtomList = new ArrayList<Atom>(nextAtoms);
		HashSet<Atom> nextAtoms1 = new HashSet<Atom>();
		for(Triple triple : tripleList){
			String enPair = triple.propID + "_" + triple.entityID;
			if(findRelationMap_R.containsKey(enPair)){
				ArrayList<Integer> list = findRelationMap_R.get(enPair);
				for(int nextRelationID : list){
					if(nextRelationID != relationID){
						String relation = relationList.get(nextRelationID);
						Atom atom = new Atom(2,relation,1,"r");
						nextAtoms1.add(atom);
					}
				}
			}
			if(findRelationMap_K.containsKey(enPair)){
				ArrayList<Integer> list = findRelationMap_K.get(enPair);
				for(int nextRelationID : list){
					if(nextRelationID != relationID){
						String relation = relationList.get(nextRelationID);
						Atom atom = new Atom(2,relation,1,"k");
						nextAtoms1.add(atom);
					}
				}
			}
		}
		ArrayList<Atom> nextAtomList1 = new ArrayList<Atom>(nextAtoms1);
		for(int i = 0 ; i < nextAtoms.size(); i++){
			rule.addAtom(nextAtomList.get(i));
			for(int j = 0; j < nextAtoms1.size(); j++){
				rule.addAtom(nextAtomList1.get(j));
				double[] result = cal.compute(rule, coverThreshold);				
				double coverage = result[0]; 
				double confidence = result[1];
				if(coverage >= coverThreshold && confidence >= confThreshold){
					Rule ruleCopy = new Rule();
					ruleCopy.addAtom(rule.body.get(0));
					ruleCopy.addAtom(rule.body.get(1));
					ruleCopy.setConfidence(confidence);
					ruleSet.add(ruleCopy);
					System.out.println(ruleCopy.toString() + "\t" + coverage + "\t" + confidence);
				}
				rule.remove();
			}
			rule.remove();
		}
	}

	public void FindAllThreeAtomRule_2(Atom head){
		//<X,r,Z> + <Y,r,Z> = <X,r,Y>
		//<X,r,Z> + <Z,r,Y> = <X,r,Y>
		Rule rule = new Rule();
		rule.addAtom(head);
		int relationID = relation_ID.get(head.relation);
		ArrayList<Triple> tripleList = relationMap_R.get(relationID);
		for(Triple triple : tripleList){
			ArrayList<Triple> tripleList1 = new ArrayList<Triple>();
			if(invertedLists_R.containsKey(triple.entityID)){
				ArrayList<Triple> list = invertedLists_R.get(triple.entityID);
				for(Triple triple1 : list){
					if(triple1.entityID == triple.entityID){
						tripleList1.add(triple1);
					}
				}
			}
			if(invertedLists_K.containsKey(triple.entityID)){
				ArrayList<Triple> list = invertedLists_K.get(triple.entityID);
				for(Triple triple1 : list){
					if(triple1.entityID == triple.entityID){
						tripleList1.add(triple1);
					}
				}
			}
			
			for(Triple triple1 : tripleList1){
				if(invertedLists_R.containsKey(triple1.propID)){
					ArrayList<Triple> list = invertedLists_R.get(triple1.propID);
					for(Triple triple2 : list){
						//<X,r,Z> + <Y,r,Z> = <X,r,Y>
						if(triple2.propID == triple1.propID && triple2.entityID == triple.propID){
							Atom atom1 = new Atom(1, relationList.get(triple1.relationID), 3, triple1.source);
							Atom atom2 = new Atom(2, relationList.get(triple2.relationID), 3, triple2.source);
							rule.addAtom(atom1); rule.addAtom(atom2);
							if(!ruleSet.contains(rule)){
								double[] result = cal.compute(rule, coverThreshold);				
								double coverage = result[0]; 
								double confidence = result[1];
								if(coverage >= coverThreshold && confidence >= confThreshold){
									Rule ruleCopy = new Rule();
									ruleCopy.addAtom(rule.body.get(0));
									ruleCopy.addAtom(rule.body.get(1));
									ruleCopy.addAtom(rule.body.get(2));
									ruleCopy.setConfidence(confidence);
									ruleSet.add(ruleCopy);
									System.out.println(ruleCopy.toString() + "\t" + coverage + "\t" + confidence);
								}
							}
							rule.remove(); rule.remove();
						}
						//<X,r,Z> + <Z,r,Y> = <X,r,Y>
						if(triple2.entityID == triple1.propID && triple2.propID == triple.propID){
							Atom atom1 = new Atom(1, relationList.get(triple1.relationID), 3, triple1.source);
							Atom atom2 = new Atom(3, relationList.get(triple2.relationID), 2, triple2.source);
							rule.addAtom(atom1); rule.addAtom(atom2);
							if(!ruleSet.contains(rule)){
								double[] result = cal.compute(rule, coverThreshold);				
								double coverage = result[0]; 
								double confidence = result[1];
								if(coverage >= coverThreshold && confidence >= confThreshold){
									Rule ruleCopy = new Rule();
									ruleCopy.addAtom(rule.body.get(0));
									ruleCopy.addAtom(rule.body.get(1));
									ruleCopy.addAtom(rule.body.get(2));
									ruleCopy.setConfidence(confidence);
									ruleSet.add(ruleCopy);
									System.out.println(ruleCopy.toString() + "\t" + coverage + "\t" + confidence);
								}
							}
							rule.remove(); rule.remove();
						}
					}
				}
				if(invertedLists_K.containsKey(triple1.propID)){
					ArrayList<Triple> list = invertedLists_K.get(triple1.propID);
					for(Triple triple2 : list){
						//<X,r,Z> + <Y,r,Z> = <X,r,Y>
						if(triple2.propID == triple1.propID && triple2.entityID == triple.propID){
							Atom atom1 = new Atom(1, relationList.get(triple1.relationID), 3, triple1.source);
							Atom atom2 = new Atom(2, relationList.get(triple2.relationID), 3, triple2.source);
							rule.addAtom(atom1); rule.addAtom(atom2);
							if(!ruleSet.contains(rule)){
								double[] result = cal.compute(rule, coverThreshold);				
								double coverage = result[0]; 
								double confidence = result[1];
								if(coverage >= coverThreshold && confidence >= confThreshold){
									Rule ruleCopy = new Rule();
									ruleCopy.addAtom(rule.body.get(0));
									ruleCopy.addAtom(rule.body.get(1));
									ruleCopy.addAtom(rule.body.get(2));
									ruleCopy.setConfidence(confidence);
									ruleSet.add(ruleCopy);
									System.out.println(ruleCopy.toString() + "\t" + coverage + "\t" + confidence);
								}
							}
							rule.remove(); rule.remove();
						}
						//<X,r,Z> + <Z,r,Y> = <X,r,Y>
						if(triple2.entityID == triple1.propID && triple2.propID == triple.propID){
							Atom atom1 = new Atom(1, relationList.get(triple1.relationID), 3, triple1.source);
							Atom atom2 = new Atom(3, relationList.get(triple2.relationID), 2, triple2.source);
							rule.addAtom(atom1); rule.addAtom(atom2);
							if(!ruleSet.contains(rule)){
								double[] result = cal.compute(rule, coverThreshold);				
								double coverage = result[0]; 
								double confidence = result[1];
								if(coverage >= coverThreshold && confidence >= confThreshold){
									Rule ruleCopy = new Rule();
									ruleCopy.addAtom(rule.body.get(0));
									ruleCopy.addAtom(rule.body.get(1));
									ruleCopy.addAtom(rule.body.get(2));
									ruleCopy.setConfidence(confidence);
									ruleSet.add(ruleCopy);
									System.out.println(ruleCopy.toString() + "\t" + coverage + "\t" + confidence);
								}
							}
							rule.remove(); rule.remove();
						}
					}
				}
			}
			break;
		}
		
		//<Z,r,X> + <Y,r,Z> = <X,r,Y>
		//<Z,r,X> + <Z,r,Y> = <X,r,Y>
		rule = new Rule();
		rule.addAtom(head);
		relationID = relation_ID.get(head.relation);
		tripleList = relationMap_R.get(relationID);
		for(Triple triple : tripleList){
			ArrayList<Triple> tripleList1 = new ArrayList<Triple>();
			if(invertedLists_R.containsKey(triple.entityID)){
				ArrayList<Triple> list = invertedLists_R.get(triple.entityID);
				for(Triple triple1 : list){
					if(triple1.propID == triple.entityID){
						tripleList1.add(triple1);
					}
				}
			}
			if(invertedLists_K.containsKey(triple.entityID)){
				ArrayList<Triple> list = invertedLists_K.get(triple.entityID);
				for(Triple triple1 : list){
					if(triple1.propID == triple.entityID){
						tripleList1.add(triple1);
					}
				}
			}
			
			for(Triple triple1 : tripleList1){
				if(invertedLists_R.containsKey(triple1.entityID)){
					ArrayList<Triple> list = invertedLists_R.get(triple1.entityID);
					for(Triple triple2 : list){
						//<Z,r,X> + <Y,r,Z> = <X,r,Y>
						if(triple2.propID == triple1.entityID && triple2.entityID == triple.propID){
							Atom atom1 = new Atom(3, relationList.get(triple1.relationID), 1, triple1.source);
							Atom atom2 = new Atom(2, relationList.get(triple2.relationID), 3, triple2.source);
							rule.addAtom(atom1); rule.addAtom(atom2);
							if(!ruleSet.contains(rule)){
								double[] result = cal.compute(rule, coverThreshold);				
								double coverage = result[0]; 
								double confidence = result[1];
								if(coverage >= coverThreshold && confidence >= confThreshold){
									Rule ruleCopy = new Rule();
									ruleCopy.addAtom(rule.body.get(0));
									ruleCopy.addAtom(rule.body.get(1));
									ruleCopy.addAtom(rule.body.get(2));
									ruleCopy.setConfidence(confidence);
									ruleSet.add(ruleCopy);
									System.out.println(ruleCopy.toString() + "\t" + coverage + "\t" + confidence);
								}
							}
							rule.remove(); rule.remove();
						}
						//<Z,r,X> + <Z,r,Y> = <X,r,Y>
						if(triple2.entityID == triple1.entityID && triple2.propID == triple.propID){
							Atom atom1 = new Atom(3, relationList.get(triple1.relationID), 1, triple1.source);
							Atom atom2 = new Atom(3, relationList.get(triple2.relationID), 2, triple2.source);
							rule.addAtom(atom1); rule.addAtom(atom2);
							if(!ruleSet.contains(rule)){
								double[] result = cal.compute(rule, coverThreshold);				
								double coverage = result[0]; 
								double confidence = result[1];
								if(coverage >= coverThreshold && confidence >= confThreshold){
									Rule ruleCopy = new Rule();
									ruleCopy.addAtom(rule.body.get(0));
									ruleCopy.addAtom(rule.body.get(1));
									ruleCopy.addAtom(rule.body.get(2));
									ruleCopy.setConfidence(confidence);
									ruleSet.add(ruleCopy);
									System.out.println(ruleCopy.toString() + "\t" + coverage + "\t" + confidence);
								}
							}
							rule.remove(); rule.remove();
						}
					}
				}
				if(invertedLists_K.containsKey(triple1.entityID)){
					ArrayList<Triple> list = invertedLists_K.get(triple1.entityID);
					for(Triple triple2 : list){
						//<Z,r,X> + <Y,r,Z> = <X,r,Y>
						if(triple2.propID == triple1.entityID && triple2.entityID == triple.propID){
							Atom atom1 = new Atom(3, relationList.get(triple1.relationID), 1, triple1.source);
							Atom atom2 = new Atom(2, relationList.get(triple2.relationID), 3, triple2.source);
							rule.addAtom(atom1); rule.addAtom(atom2);
							if(!ruleSet.contains(rule)){
								double[] result = cal.compute(rule, coverThreshold);				
								double coverage = result[0]; 
								double confidence = result[1];
								if(coverage >= coverThreshold && confidence >= confThreshold){
									Rule ruleCopy = new Rule();
									ruleCopy.addAtom(rule.body.get(0));
									ruleCopy.addAtom(rule.body.get(1));
									ruleCopy.addAtom(rule.body.get(2));
									ruleCopy.setConfidence(confidence);
									ruleSet.add(ruleCopy);
									System.out.println(ruleCopy.toString() + "\t" + coverage + "\t" + confidence);
								}
							}
							rule.remove(); rule.remove();
						}
						//<Z,r,X> + <Z,r,Y> = <X,r,Y>
						if(triple2.entityID == triple1.entityID && triple2.propID == triple.propID){
							Atom atom1 = new Atom(3, relationList.get(triple1.relationID), 1, triple1.source);
							Atom atom2 = new Atom(3, relationList.get(triple2.relationID), 2, triple2.source);
							rule.addAtom(atom1); rule.addAtom(atom2);
							if(!ruleSet.contains(rule)){
								double[] result = cal.compute(rule, coverThreshold);				
								double coverage = result[0]; 
								double confidence = result[1];
								if(coverage >= coverThreshold && confidence >= confThreshold){
									Rule ruleCopy = new Rule();
									ruleCopy.addAtom(rule.body.get(0));
									ruleCopy.addAtom(rule.body.get(1));
									ruleCopy.addAtom(rule.body.get(2));
									ruleCopy.setConfidence(confidence);
									ruleSet.add(ruleCopy);
									System.out.println(ruleCopy.toString() + "\t" + coverage + "\t" + confidence);
								}
							}
							rule.remove(); rule.remove();
						}
					}
				}
				
			}			
			break;
		}
		
	}
	
	
	@Override
	public void run() {
		ArrayList<Integer> list = new ArrayList<Integer>(relationMap_R.keySet());
		for(int pos = start; pos < end; pos++){
			String relation = relationList.get(list.get(pos));
			Atom head = new Atom(1,relation,2,"r");
			FindAllTwoAtomRule(head);
			FindAllThreeAtomRule_1(head);
			FindAllThreeAtomRule_2(head);
		}
	}
}
