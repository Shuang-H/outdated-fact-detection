public class Atom {
	public int variable1 = -1;
	public int variable2 = -1;
	public String relation = null;
	String source = null;//'r' is from revision history, 'k' is from knowledge base
	
	public Atom(int var1, String relation, int var2){
		variable1 = var1;
		this.relation = relation;
		variable2 = var2;
	}
	
	public Atom(int var1, String relation, int var2, String source){
		variable1 = var1;
		this.relation = relation;
		variable2 = var2;
		this.source = source;
	}
	
	public void setRelation(String relation){
		this.relation = relation;
	}
	
	public void setSource(String source){
		this.source = source;
	}
	
	public String toString(){
		return variable1 + " " + relation + " " + variable2 + " " + source;
	}
	
	public boolean equals(Object obj){
		if(obj instanceof Atom){
			Atom atom = (Atom) obj;
			if(relation.equals(atom.relation) && variable1 == atom.variable1 && variable2 == atom.variable2 && source.equals(atom.source)) return true;
		}
		return false;
	}
	
	public int hashCode(){ 
        return Integer.hashCode(variable1) * relation.hashCode() * Integer.hashCode(variable2) * source.hashCode(); 
    }
}
