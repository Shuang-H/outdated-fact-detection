public class Triple {
	int entityID;
	int relationID;
	int propID;
	String source;
	
	public Triple(int entityID, int relationID, int propID, String source){
		this.entityID = entityID;
		this.relationID = relationID;
		this.propID = propID;
		this.source = source;
	}
}
